import channel.ChannelManager;
import channel.message.ImmutableMessageList;
import channel.message.TwitchMessage;
import channel.users.TwitchUser;
import channel.users.UserPermission;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import util.BitlyDecorator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MyBot extends PircBot {
	private Logger log = LogManager.getLogger();
	private Logger messageLog = LogManager.getLogger("Message Log");
	private Logger actionLog = LogManager.getLogger("Action Log");

    PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
            .printZeroNever()
            .appendDays()
            .appendSuffix(" day, ", " days, ")
            .appendHours()
            .appendSuffix(" hour ", " hours ")
            .appendSeparatorIfFieldsBefore( "and " )
            .appendMinutes()
            .appendSuffix(" minute.", " minutes.")
            .toFormatter();

	private ChannelManager channelManager;
	private Set<String> blockedWords;
	private Set<String> blockedMessage;

	private String lastHostLink;

	private int maxMsg = 20;
	private int linkRepeatCountHost = 7;
	private int linkRepeatCountMod = 5;
	private int voteBanMax = 2;
	private int messageCap = 8;
	private int rPostVal = 8;
	private float msgpersec = 2.5f;
	private int longestSubStringAllowed = 13;
	private int repetitionSearch = 4;
	
	private DateTime showStartTime = new DateTime(2016, 3, 11, 16, 30, DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Vancouver"))); //The set time the show should start every week.
	private long timeLastTTLsent, timeLastLinusLink, timeLastHelp; //Saves the last time each help message was sent.
	private ImmutableSet<Character> permittedChars;
	private HashMap<String, Integer> banHistory = new HashMap<>();

	private long startOfShow;

    private List<String> commandWords = new ArrayList<>(Arrays.asList("!ttl", "!lll", "!help", "!ttt"));
	
	private MessageSender msgSender;
    private MessageRepeater msgRep;

    private BitlyDecorator bitlyDecorator;

	@Inject
	public MyBot( @Named("twitch.irc.channel") String twitchChannelName,
				  @Named("twitch.username") String twitchUsername,
				  @Named("twitch.oauth.token") String oAuthToken,
				  @Named("twitch.irc.server") String ircServer,
				  @Named("twitch.irc.port") Integer ircPort,
				  BitlyDecorator bitlyDecorator) {
        log.info("Starting bot for channel {} on server {}", twitchChannelName, ircServer);
        Objects.requireNonNull(bitlyDecorator);
        this.bitlyDecorator = bitlyDecorator;
		channelManager = new ChannelManager();
        permittedChars = ImmutableSet.copyOf("abcdefghijklmnopqrstuvwxyz.!@$%123454567890".chars().mapToObj(a -> (char) a).collect(Collectors.toList()));
		this.blockedMessage = new HashSet<>();
		this.blockedWords	= new HashSet<>();

		setName(twitchUsername);
		setMessageDelay(50);
		log.info("Connecting to twitch irc servers at {}@{}:{}", twitchUsername, ircServer, ircPort);
		try {
			super.connect(ircServer, 6667, oAuthToken);
			super.joinChannel(twitchChannelName);
		} catch (IOException e) {
			log.fatal("Failed to connect to twitch due to IOException: {}", e.getMessage());
			throw new UncheckedIOException(e);
		} catch (IrcException e) {
			log.fatal("Failed to connect to twitch due to IRCException: {}", e.getMessage());
			throw new UncheckedExecutionException(e);
		}
        log.info("Connected successfully to {}@{}:{}", twitchUsername, ircServer, ircPort);
		this.timeLastTTLsent = this.timeLastLinusLink = this.timeLastHelp = 0;
		startOfShow = System.currentTimeMillis()/1000;
		loadSettings();
		msgSender = new MessageSender(this, twitchChannelName);
        Thread msgSenderThread = new Thread(msgSender);
		msgSenderThread.start();
		msgRep = new MessageRepeater(msgSender);
        Thread msgRepThread = new Thread(msgRep);
		msgRepThread.start();
	}

	/**
	 * How the bot will react to messages received.
	 */
	public void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		TwitchUser twitchUser = channelManager.getTwitchUserManager().getUser(hostname);
		TwitchMessage twitchMessage = new TwitchMessage(message, twitchUser, DateTime.now(), channel, hostname);

		channelManager.getMessageManager().getMessageBuffer().addMessageToBuffer(twitchMessage);

		messageLog.info( "{} : {}", twitchMessage.getSender().getUsername(), twitchMessage.getMessagePayload().toLowerCase() ); //Stores the message in the chat log.
		if(twitchMessage.getMessagePayload().startsWith("!")){//Checks if the message is a command
			userCommands(twitchMessage.getMessagePayload().substring(1)); //Checks if the message contains keywords for the bot.
		}

		if( twitchUser.getUserPermission().checkPermission( UserPermission.ChannelOwner ) )
			hostCommands( twitchMessage );

		if( twitchUser.getUserPermission().checkPermission( UserPermission.BotAdmin ) && message.startsWith("!bot") )
			sendMessageP( botCommand( twitchMessage.getMessagePayload() ) );

		if( twitchUser.getUserPermission().checkPermission( UserPermission.BotModerator ) )
			operatorCommands( twitchMessage.getSender().getUsername(), twitchMessage.getMessagePayload() );

		if( !twitchUser.getUserPermission().checkPermission( UserPermission.ChannelModerator ) ) {
			messageChecker( sender, twitchMessage.getMessagePayload().toLowerCase() ); //Checks if the message is allowed.
			spamDetector(twitchMessage);
		}
	}

	/**
	 * Used to add a word to the blacklist
	 */
    private String botCommand(String word) {
		if(word.startsWith("!bot")){
			word = word.substring(5); //remove the !bot section.
			if(word.startsWith("blw")){
				return bLWord(word.substring(4));
			} else if(word.startsWith("rmblw")){
				return removeBLWord(word.substring(6));
			} else if(word.startsWith("blm")){
				return bLMsg(word.substring(4));
			} else if(word.startsWith("rmblm")){
				return removeBLMsg(word.substring(6));
			} else if(word.startsWith("addop")){
				return addOperator(word.substring(6));
			} else if(word.startsWith("rmop")){
				return rmOperator(word.substring(5));
			} else if(word.startsWith("sstart")){
				return setStartTime();
			} else if(word.startsWith("set")){
				return setVariables(word.substring(4));
			} else if(word.startsWith("resetbans")){
				return resetBans();
			}
		}
		return "Unknown Command Entered.";
	}
	
	private String resetBans(){
		banHistory = new HashMap<>();
		return "Ban History reset";
	}
	
	private String setVariables(String command){
		float newVal;
		String[] sCommand = command.split(" ");
		if (sCommand.length != 2) return "Syntax Error.";
		try{
			newVal = Float.parseFloat(sCommand[1]);
			if(sCommand[0].equalsIgnoreCase("maxmsg")){
				if(newVal > 0 && newVal <= 200){
					maxMsg = (int) newVal;
					if(maxMsg>repetitionSearch-1)repetitionSearch= maxMsg-1;
					return "maxMsg set to " + maxMsg;
				} else {
					return "maxMsg must be between 0 and 200";
				}
			} else if(sCommand[0].equalsIgnoreCase("linkRepeatCountHost")){
				if(newVal > 0 && newVal <= 40){
					linkRepeatCountHost = (int) newVal;
					return "linkRepeatCountHost set to " + linkRepeatCountHost;
				} else {
					return "linkRepeatCountHost must be between 0 and 40";
				}
			} else if(sCommand[0].equalsIgnoreCase("linkRepeatCountMod")){
				if(newVal > 0 && newVal <= 40){
					linkRepeatCountMod = (int) newVal;
					return "linkRepeatCountMod set to " + linkRepeatCountMod;
				} else {
					return "linkRepeatCountMod must be between 0 and 40";
				}
			} else if(sCommand[0].equalsIgnoreCase("voteBanMax")){
				if(newVal > 0 && newVal <= 50){
					voteBanMax = (int) newVal;
					return "voteBanMax set to " + voteBanMax;
				} else {
					return "voteBanMax must be between 0 and 50";
				}
			} else if(sCommand[0].equalsIgnoreCase("messageCap")){
				if(newVal > 0 && newVal <= 50){
					messageCap = (int) newVal;
					return "messageCap set to " + messageCap;
				} else {
					return "messageCap must be between 0 and 50";
				}
			} else if(sCommand[0].equalsIgnoreCase("rPostVal")){
				if(newVal > 0 && newVal <= 50){
					rPostVal = (int) newVal;
					return "rPostVal set to " + rPostVal;
				} else {
					return "rPostVal must be between 0 and 50";
				}
			} else if(sCommand[0].equalsIgnoreCase("msgpersec")){
				if(newVal > 0 && newVal <= 50){
					msgpersec = newVal;
					return "secpermsg set to " + msgpersec;
				} else {
					return "secpermsg must be between 0 and 50";
				}
			} else if(sCommand[0].equalsIgnoreCase("longestSubStringAllowed")){
				if(newVal > 3 && newVal <= 50){
					longestSubStringAllowed = (int) newVal;
					return "longestSubStringAllowed set to " + longestSubStringAllowed;
				} else {
					return "longestSubStringAllowed must be between 3 and 50";
				}
			} else if(sCommand[0].equalsIgnoreCase("repetitionSearch")){
				if(newVal > 1 && newVal <= maxMsg-1){
					repetitionSearch = (int) newVal;
					return "repetitionSearch set to " + repetitionSearch;
				} else {
					return "repetitionSearch must be between 1 and maxMsg";
				}
			} else if(sCommand[0].equalsIgnoreCase("messageFrequency")){
                if(newVal > 60){
                	msgRep.setFrequency((int) newVal);
					return "messageFrequency set to " + (int) newVal;
				} else {
					return "messageFrequency must be more than 60";
				}
			} else if(sCommand[0].equalsIgnoreCase("messageRepToggle")){
                msgRep.toggleState();
                return "messageRepetition Toggled.";
			} else if(sCommand[0].equalsIgnoreCase("addStartTime")){
                    showStartTime = showStartTime.plusSeconds((int) newVal);
                    return "Show start time set to: " + showStartTime.toString(ISODateTimeFormat.basicOrdinalDateTimeNoMillis());
            } else{
				return "Variable name not found";
			}
		}catch(Exception e){
			return "Syntax Error.";
		}
	}
	
	/**
	 * Adds a word to the blacklist.
	 * @param word Word to add to blacklist
	 * @return response message
	 */
	private String bLWord(final String word){
        final String lowerCaseWord = word.toLowerCase();
		if(lowerCaseWord.length()<3) return "Word not long enough.";
		if(blockedWords.contains(lowerCaseWord))
			return lowerCaseWord + " already on blacklist.";
		blockedWords.add(lowerCaseWord);
        channelManager.getMessageManager()
                .getMessageBuffer()
                .getMessageBufferSnapshot()
                .getTwitchMessages()
                .stream()
                .filter(message -> !message.getSender().getUserPermission().checkPermission(UserPermission.ChannelModerator))
                .filter(message -> message.getMessagePayload().toLowerCase().contains(lowerCaseWord))
                .forEach(message -> ban(message.getSender().getUsername(), message.getMessagePayload(), 45, "Blacklisted word: " + word, ""));
		return lowerCaseWord + " added to blacklist. Previous messages breaching rule this will be banned.";
	}

	/**
	 * Removes a word from the blacklist if possible. If not possible it is ignored.
	 * @param word word to remove.
	 * @return response message.
	 */
	private String removeBLWord(String word){
		if(blockedWords.contains(word)){
			blockedWords.remove(word);
			return word + " removed from the blacklist.";
		}
		return word + " not found on the blacklist";
	}
	
	/**
	 * Adds a word to the blacklist.
	 * @param word Word to add to blacklist
	 * @return response word
	 */
	private String bLMsg(final String word){
        final String lowerCaseMessage = word.toLowerCase();
		if(blockedMessage.contains(lowerCaseMessage))
			return lowerCaseMessage + " already on blacklist.";
		blockedMessage.add(lowerCaseMessage);
        ImmutableMessageList channelMessageHistory = channelManager.getMessageManager().getMessageBuffer().getMessageBufferSnapshot();
        channelMessageHistory.getTwitchMessages().stream()
                .filter(message -> !message.getSender().getUserPermission().checkPermission(UserPermission.ChannelModerator))
                .filter(message -> message.getMessagePayload().equalsIgnoreCase(lowerCaseMessage))
                .forEach(message -> ban(message.getSender().getUsername(), message.getMessagePayload(), 45, "Blacklisted message: " + word, ""));
		return lowerCaseMessage + " added to message blacklist. Previous messages breaching this rule will be banned.";
	}
	/**
	 * Removes a word from the blacklist if possible. If not possible it is ignored.
	 * @param word word to remove.
	 * @return response word.
	 */
	private String removeBLMsg(String word){
		if(blockedWords.contains(word)){
			blockedWords.remove(word);
			return word + " removed from the blacklist.";
		}
		return word + " not found on the blacklist";
	}

	private String addOperator(String command){
		String[] splitCommand = command.split(" ");
		if (splitCommand.length != 2) return "Syntax Error.";
		try{
			String tier = splitCommand[0];
			String username = splitCommand[1];
			UserPermission userPermission = UserPermission.valueOf(tier);
			TwitchUser twitchUser = new TwitchUser(username, userPermission);
			channelManager.getTwitchUserManager().addUser(twitchUser);
			return String.format("Added %s to %s", twitchUser.getUsername(), twitchUser.getUserPermission().toString());
		} catch(Exception e){
			return "Syntax Error.";
		}
	}
	
	private String rmOperator(String name){
		TwitchUser twitchUser = new TwitchUser(name, UserPermission.ChannelUser);
		channelManager.getTwitchUserManager().addUser(twitchUser);
		return name + " is no longer an operator.";
	}
	
	private String setStartTime(){
		startOfShow = System.currentTimeMillis()/1000;
		return "Show Start time has been set.";
	}

	/**
	 * You should send messages using this. It saves all the files to a log.
	 * @param message
	 *            Message being sent.
	 */
    private void sendMessageP(String message) {
		messageLog.info("Sending message: {}", message);
		msgSender.sendMessage(message);
	}

	/**
	 * Checks if the message sent is a current request
	 * 
	 * @param message
	 *            Message sent.
	 */
	private void userCommands(String message) {
		if (message.equalsIgnoreCase("TTL") || message.equalsIgnoreCase("TTT")) {
			String timeTillLive = getTimeTillLive();
			if( !Objects.isNull(timeTillLive) )
				sendMessageP( timeTillLive );
		} else if (message.equalsIgnoreCase("LLL"))
			lastLinusLink();
		else if (message.equalsIgnoreCase("HELP"))
			sendHelpMessage();
		else if (message.startsWith("uptime"))
			uptime();
	}

	private void uptime(){
		if ((System.currentTimeMillis() / 1000) < (timeLastTTLsent + 40))
			return;
		timeLastTTLsent = ((System.currentTimeMillis()/1000));
		String message = "Linus last went live: ";
		long seconds = (System.currentTimeMillis()/1000) - startOfShow;
		if(seconds < 60){
			sendMessageP("Linus last went live in the last minute.");
			timeLastTTLsent = System.currentTimeMillis() / 1000;
			return;
		}
		if(seconds > 60*60*72)return;
		long days = seconds / (60 * 60 * 24);
		if (days == 6)
			return;
		else if (days == 1)
			message += "1 day ";
		else if (days != 0)
			message += Integer.toString((int) days) + " days ";
		seconds -= (days * 60 * 60 * 24);
		long hours = seconds / (60 * 60);
		if (hours == 1)
			message += "1 hour ";
		else if (hours != 0)
			message += Integer.toString((int) hours) + " hours ";
		seconds -= (hours * 60 * 60);
		long minutes = seconds / (60);
		if (hours > 1 && minutes > 1)
			message += "and ";
		if (minutes == 1)
			message += "1 minute";
		else if (minutes != 0)
			message += Integer.toString((int) minutes) + " minutes ";
		sendMessageP(message + " ago.");
		timeLastTTLsent = System.currentTimeMillis() / 1000;
	}
	
	/**
	 * Sends a Message to chat displaying how long till the show begins.
	 */
	private String getTimeTillLive() {
		if ((System.currentTimeMillis() / 1000) < (timeLastTTLsent + 40))
			return null;
		while (showStartTime.getMillis() < System.currentTimeMillis() ) {
			showStartTime = showStartTime.plusDays(7);
		}
        Interval intervalToShow = new Interval(DateTime.now(), showStartTime);
        Period periodTillShow = new Period(intervalToShow);
		timeLastTTLsent = System.currentTimeMillis() / 1000;
		if(periodTillShow.toStandardDays().getDays() > 5){
			return null;
		}
		if(periodTillShow.toStandardSeconds().getSeconds() < 60){
			return "The next WAN Show should begin soon.";
		} else {
            return "The next WAN Show should begin in: " + periodTillShow.toString(periodFormatter);
        }
	}

	private void hostCommands(TwitchMessage twitchMessage){
        final String message = twitchMessage.getMessagePayload();
		//Repeats messages starting with HTTP:// & HTTPS://
		if(message.startsWith("http://") || message.startsWith("https://")){
			try{
				final String shortenedURL = bitlyDecorator.shortenURL(message);
                lastHostLink = shortenedURL;
                IntStream.range(0, linkRepeatCountHost).forEach(ignore -> sendMessageP(shortenedURL));
			} catch (Exception e){
                // Send un-shortened anyway.
                IntStream.range(0, linkRepeatCountHost).forEach(ignore -> sendMessageP(message));
			}

		}	
	}
	
	private void operatorCommands(String sender, String message){
		if(message.startsWith("!link")) linkRepeater(sender, message.substring(6));
		else if(message.startsWith("!loop add")) msgRep.addMessage(message.substring(10));
		else if(message.startsWith("!loop removeLast")) msgRep.clearLast();
		else if(message.startsWith("!loop removeAll")) msgRep.clearAll();
	}
	
	/**
	 * Used to spam links by bot operators.
	 * 
	 * @param sender
	 *            The sender of the message
	 * @param message
	 *            The link sent
	 */
	private void linkRepeater(String sender, String message) {
		if (message.startsWith("http://") || message.startsWith("https://")) {
			try {
				message = bitlyDecorator.shortenURL(message);
			} catch (Exception e) {
				//Do Nothing (BAD)
			}
		}
		String newMessage = sender + " : " + message;
		for (int x = 0; x < linkRepeatCountMod; x++) {
			sendMessageP(newMessage);
		}
	}

	/**
	 * Sends the last link Linus sent out;
	 */
	private void lastLinusLink() {
		if ((System.currentTimeMillis() / 1000) > (timeLastLinusLink + 30)) {

			if (lastHostLink != null) {
				sendMessageP("Linus' Last Link: " + lastHostLink);
			} else {
				sendMessageP("Linus has not posted a link recently.");
			}
		}
		timeLastLinusLink = System.currentTimeMillis() / 1000;
	}

	/**
	 * Sends a command list to the channel.users
	 */
	private void sendHelpMessage() {
		if ((System.currentTimeMillis() / 1000) > (timeLastHelp + 30)) {
            String helpMessage = "You can find out more about the bot here: http://bit.ly/1DnLq9M. If you want to request an unban please tweet @deadfire19";
            sendMessageP(helpMessage);
		}
		timeLastHelp = System.currentTimeMillis() / 1000;
	}

	/**
	 * Checks if a message is in the blacklist
	 */
	private void messageChecker(String sender, String message) {
		if( blockedWords.stream().anyMatch(message::contains) )
			ban(sender, message, 45, "Matched blacklisted word", "Timeout - Blacklisted word");
		if( blockedMessage.stream().anyMatch(message::equalsIgnoreCase) )
			ban(sender, message, 45, "Matched blacklisted message", "Timeout - Blacklisted Message");
	}

	/**
	 * Generates the blacklist
	 */
	private void loadSettings() {
		blockedWords.addAll( Stream.of( "nigger", "nigga", "nazi", "strawpoll.me" ).collect(Collectors.toList()) );
        Stream.of( "slick_pc", "linustech", "luke_lafr")
			.map(username -> new TwitchUser(username, UserPermission.ChannelOwner))
			.forEach(user -> channelManager.getTwitchUserManager().addUser(user));
		Stream.of( "nicklmg", "lttghost" )
			.map(username -> new TwitchUser(username, UserPermission.BotAdmin))
			.forEach(user -> channelManager.getTwitchUserManager().addUser(user));
		Stream.of( "airdeano", "alpenwasser", "antvenom", "blade_of_grass", "colonel_mortis", "daveholla", "dezeltheintern", "dvoulcaris", "ecs_community", "ericlee30", "foxhound590", "glenwing", "ixi_your_face", "linusbottips", "looneyschnitzel", "ltt_bot", "mg2r", "prolemur", "rizenfrmtheashes",  "str_mape", "wh1skers", "whaler_99", "windspeed36", "woodenmarker", "wrefur" )
			.map(username -> new TwitchUser(username, UserPermission.ChannelModerator))
			.forEach(user -> channelManager.getTwitchUserManager().addUser(user));
	}

	/**
	 * If a sender sends the same message 3 times in a row they are timed out.
	 */
	private void spamDetector(TwitchMessage twitchMessage) {
        ImmutableMessageList userMessages = channelManager.getMessageManager()
                .getMessageBuffer()
                .getMessageBufferSnapshot()
                .filterUser(twitchMessage.getSender());

		if(twitchMessage.getMessagePayload().length() > 5 && twitchMessage.getLegalCharRatio(permittedChars) < 0.1)
			ban(twitchMessage.getSender().getUsername(), twitchMessage.getMessagePayload(), 20, "ASCII art ban", "Timeout - Excessive symbol use.");

        assert userMessages != null;
		if(userMessages.size() < 5) return; //If a user has <5 messages it ignores them. //TODO Improve Logic

		if(userMessages.getTimeSpanSeconds() > msgpersec){
			ban(twitchMessage.getSender().getUsername(), twitchMessage.getMessagePayload(), 20, "More than " + msgpersec + " messages/second", "Timeout - Flooding Chat");
			return;
		}
		
		if(commandWords.contains(twitchMessage.getMessagePayload())) return;
		if(findExactMessage(twitchMessage, channelManager.getMessageManager().getMessageBuffer().getMessageBufferSnapshot().getTwitchMessages()))
			ban(twitchMessage.getSender().getUsername(), twitchMessage.getMessagePayload(), 20, "Repeated Message Found", "Timeout - Message Repetition");
	}
    
	/**
	 * This message to the ban log file.
	 * 
	 * @param sender
	 *            Sender of the offending message.
	 * @param message
	 *            Offending message.
	 * @param banLength
	 *            Length of Ban resulting from message.
	 * @param reason
	 *            Reason for ban.
	 */
	private void ban(String sender, String message, int banLength,
			String reason, String officialReason) {
		Integer previousBanTotal = banHistory.get(sender);
		if(previousBanTotal == null) previousBanTotal = 0;
		banLength += previousBanTotal;
		if(banLength > 60) banLength += 120;
		if(officialReason.length() != 0) sendMessageP(officialReason);
		msgSender.sendMessage(".timeout " + sender + " " + banLength);
		banHistory.put(sender, banLength);
		actionLog.info("Timeout {} for {}s. Reason: {}. Message: {}", sender, banLength, reason, message);
	}

    /**
     * Checks if the given message payload exactly (ignoreCase,ignoreSpaces) matches any in the list
     * @param currentMessage    Message to search for
     * @param messageList       Message list to search in
     * @return                  true if any payload matches
     */
	private boolean findExactMessage(TwitchMessage currentMessage, Collection<TwitchMessage> messageList){
		String lastMessage = currentMessage.getMessagePayload().replaceAll(" ", "");
		return messageList.stream()
                .map(TwitchMessage::getMessagePayload)
                .map(message -> message.replaceAll(" ", ""))
                .anyMatch(lastMessage::equalsIgnoreCase);
	}
}
