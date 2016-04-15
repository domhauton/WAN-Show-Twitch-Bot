import channel.ChannelManager;
import channel.message.ImmutableTwitchMessageList;
import channel.data.TwitchMessage;
import channel.data.TwitchUser;
import channel.permissions.UserPermission;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import irc.sender.PrivateMessageSender;
import irc.sender.PublicMessageSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.ISODateTimeFormat;
import util.BitlyDecorator;
import util.DateTimeUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BotController {
    private String twitchUsername;
    private String twitchChannelName;
    private String oAuthToken;
    private String ircServer;
    private Integer ircPort;


	private Logger log = LogManager.getLogger();
	private Logger messageLog = LogManager.getLogger("Message Log");
	private Logger actionLog = LogManager.getLogger("Action Log");

    private DateTimeUtil dateTimeUtil;
    private MessageRepeater messageRepeater;
	private PrivateMessageSender privateMessageSender;
	private PublicMessageSender publicMessageSender;

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
    private DateTime commandTimeTTL, commandTimeLLL, commandTimeHelp, streamStartTime;
	private ImmutableSet<Character> permittedChars;
	private HashMap<String, Integer> banHistory = new HashMap<>();

    private List<String> commandWords = new ArrayList<>(Arrays.asList("!ttl", "!lll", "!help", "!ttt"));
	
	//private MessageSender msgSender;

    private BitlyDecorator bitlyDecorator;

	@Inject
	public BotController(@Named("twitch.irc.public.twitchChannel") String twitchChannelName,
						 @Named("twitch.username") String twitchUsername,
						 @Named("twitch.oauth.token") String oAuthToken,
						 @Named("twitch.irc.public.server") String ircServer,
						 @Named("twitch.irc.public.port") Integer ircPort,
						 BitlyDecorator bitlyDecorator,
						 DateTimeUtil dateTimeUtil,
						 PrivateMessageSender privateMessageSender,
						 PublicMessageSender publicMessageSender,
                         MessageRepeater messageRepeater) {
		log.info("Starting bot for channel {} on server {}", twitchChannelName, ircServer);
		channelManager = new ChannelManager();
		permittedChars = ImmutableSet.copyOf("abcdefghijklmnopqrstuvwxyz.!@$%123454567890".chars().mapToObj(a -> (char) a).collect(Collectors.toList()));
		this.blockedMessage = new HashSet<>();
		this.blockedWords = new HashSet<>();

		this.bitlyDecorator = bitlyDecorator;
		this.dateTimeUtil = dateTimeUtil;


		this.twitchChannelName = twitchChannelName;
		this.twitchUsername = twitchUsername;
		this.oAuthToken = oAuthToken;
		this.ircServer = ircServer;
		this.ircPort = ircPort;

		this.privateMessageSender = privateMessageSender;
		this.publicMessageSender = publicMessageSender;

		commandTimeTTL = commandTimeLLL = commandTimeHelp = DateTime.now().minusSeconds(60);
		streamStartTime = new DateTime(2016, 3, 25, 16, 30, DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Vancouver")));

		loadSettings();
		this.messageRepeater = messageRepeater;
		this.messageRepeater.start();
	}

    /**
     * Processes the given twitchMessage as required for the channel.
     * @param twitchMessage TwitchMessage to process.
     */
	public void processMessage(TwitchMessage twitchMessage) {
		channelManager.addChannelMessage(twitchMessage);

		messageLog.info(twitchMessage::toString); //Stores the message in the chat log.

        if(twitchMessage.getMessagePayload().startsWith("!")){//Checks if the message is a command
			userCommands(twitchMessage.getSender().getUsername(), twitchMessage.getMessagePayload().substring(1)); //Checks if the message contains keywords for the bot.
		}

		if( channelManager.checkPermission(twitchMessage.getSender(), UserPermission.ChannelOwner ) )
			hostCommands( twitchMessage );

		if( channelManager.checkPermission(twitchMessage.getSender(), UserPermission.BotAdmin ) && twitchMessage.getSimpleMessagePayload().startsWith("!bot") )
			sendMessageP( botCommand( twitchMessage.getMessagePayload() ) );

		if( channelManager.checkPermission(twitchMessage.getSender(), UserPermission.BotModerator ) )
			operatorCommands( twitchMessage.getSender().getUsername(), twitchMessage.getMessagePayload() );

		if( !channelManager.checkPermission(twitchMessage.getSender(), UserPermission.ChannelModerator ) ) {
			messageChecker( twitchMessage ); //Checks if the message is allowed.
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
                	messageRepeater.setFrequency((int) newVal);
					return "messageFrequency set to " + (int) newVal;
				} else {
					return "messageFrequency must be more than 60";
				}
			} else if(sCommand[0].equalsIgnoreCase("messageRepToggle")){
                messageRepeater.toggleState();
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
        channelManager.getMessageSnapshot()
                .stream()
                .filter(message -> !channelManager.checkPermission(message.getSender(), UserPermission.ChannelModerator))
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
        channelManager.getMessageSnapshot()
                .stream()
                .filter(message -> !channelManager.checkPermission(message.getSender(), UserPermission.ChannelModerator))
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
			channelManager.setPermission(new TwitchUser(username), userPermission);
			return String.format("Added %s to %s", username, userPermission);
		} catch(Exception e){
			return "Syntax Error.";
		}
	}
	
	private String rmOperator(String name){
		channelManager.setPermission(new TwitchUser(name), UserPermission.getDefaultPermission());
		return name + " is no longer an operator.";
	}
	
	private String setStartTime(){
		streamStartTime = DateTime.now();
		return "Show Start time has been set.";
	}

	/**
	 * You should send messages using this. It saves all the files to a log.
	 * @param message
	 *            Message being sent.
	 */
    private void sendMessageP(String message) {
		messageLog.info("Sending message: {}", message);
		publicMessageSender.sendMessageAsync(twitchChannelName, message);
	}

	/**
	 * Checks if the message sent is a current request
	 *
	 * @param message
	 *            Message sent.
	 */
	private void userCommands(String sender, String message) {
		if (message.equalsIgnoreCase("TTL") || message.equalsIgnoreCase("TTT")) {
			String timeTillLive = getTimeTillLive(sender);
			if( !Objects.isNull(timeTillLive) ){
                sendMessageP( timeTillLive );
                privateMessageSender.sendWhisperAsync(sender, timeTillLive);
            }
		} else if (message.equalsIgnoreCase("LLL"))
			lastLinusLink(sender);
		else if (message.equalsIgnoreCase("HELP"))
			sendHelpMessage();
		else if (message.startsWith("uptime"))
			uptime();
	}

	private void uptime(){
        if( new Period(commandTimeTTL, DateTime.now()).toStandardSeconds().getSeconds() < 40 ){
            return;
        }

        Period periodSinceStreamStart = new Period(streamStartTime, DateTime.now());

        if (periodSinceStreamStart.toStandardSeconds().getSeconds() < 60){
            sendMessageP("Linus last went live in the last minute.");
        } else {
            sendMessageP("Linus last went live: " + dateTimeUtil.periodToString(periodSinceStreamStart) + " ago.");
        }

        commandTimeTTL = DateTime.now();
    }
	
	/**
	 * Sends a Message to chat displaying how long till the show begins.
	 */
	private String getTimeTillLive(String sender) {
        if( new Period(commandTimeTTL, DateTime.now()).toStandardSeconds().getSeconds() < 40 ) {
            return null;
        }

        commandTimeTTL = DateTime.now();
        //TODO Tacky Solution - Could crash
		while (showStartTime.isBeforeNow()) {
			showStartTime = showStartTime.plusDays(7);
		}
        Period periodTillShow = new Period(DateTime.now(), showStartTime);

        if(periodTillShow.toStandardDays().getDays() > 5){
            return null;
        } else if(periodTillShow.toStandardSeconds().getSeconds() < 60){
			return "The next WAN Show should begin soon.";
		} else {
            return "The next WAN Show should begin in: " + dateTimeUtil.periodToString(periodTillShow);
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
		else if(message.startsWith("!loop add")) messageRepeater.addMessage(message.substring(10));
		else if(message.startsWith("!loop removeLast")) messageRepeater.clearLast();
		else if(message.startsWith("!loop removeAll")) messageRepeater.clearAll();
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
				log.warn("Failed to convert bitly link: {}", message);
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
	private void lastLinusLink(String sender) {
        if( new Period(commandTimeLLL, DateTime.now()).toStandardSeconds().getSeconds() > 40 ){
			if (lastHostLink != null) {
				sendMessageP("Linus' Last Link: " + lastHostLink);
                privateMessageSender.sendWhisperAsync(sender, "Linus' Last Link: " + lastHostLink);
			} else {
				sendMessageP("Linus has not posted a link recently.");
			}
            commandTimeLLL = DateTime.now();
		} else {
            if (lastHostLink != null) {
                privateMessageSender.sendWhisperAsync(sender, "Linus' Last Link: " + lastHostLink);
            } else {
                privateMessageSender.sendWhisperAsync(sender, "Linus has not posted a link recently.");
            }
        }
	}

	/**
	 * Sends a command list to the users
	 */
	private void sendHelpMessage() {
        if( new Period(commandTimeHelp, DateTime.now()).toStandardSeconds().getSeconds() > 30 ){
            String helpMessage = "You can find out more about the bot here: http://bit.ly/1DnLq9M. If you want to request an unban please tweet @deadfire19";
            sendMessageP(helpMessage);
            commandTimeHelp = DateTime.now();
		}
	}

	/**
	 * Checks if a message is in the blacklist
	 */
	private void messageChecker(TwitchMessage twitchMessage) {
        if(blockedWords.stream().anyMatch(twitchMessage::containsString)){
            ban(
                    twitchMessage.getSender().getUsername(),
                    twitchMessage.getMessagePayload(),
                    45,
                    "Matched blacklisted word", "Timeout - Blacklisted word"
            );
        }
		if( blockedMessage.stream().anyMatch(twitchMessage::equalsSimplePayload) ){
            ban(
                    twitchMessage.getSender().getUsername(),
                    twitchMessage.getMessagePayload(),
                    45,
                    "Matched blacklisted message", "Timeout - Blacklisted Message"
            );
        }
    }

	/**
	 * Generates the blacklist
	 */
	private void loadSettings() {
		blockedWords.addAll( Stream.of( "nigger", "nigga", "nazi", "strawpoll.me", "bit.do", "t.co", "lnkd.in", "db.tt", "qr.ae", "adf.ly", "goo.gl", "bitly.com", "cur.lv", "tinyurl.com", "ow.ly", "bit.ly", "adcrun.ch", "ity.im", "q.gs", "viralurl.com", "is.gd", "vur.me", "bc.vc", "twitthis.com", "u.to", "j.mp", "buzurl.com", "cutt.us", "u.bb", "yourls.org", "crisco.com", "x.co", "adcraft.co" ).collect(Collectors.toList()) );
        Stream.of( "slick_pc", "linustech", "luke_lafr")
                .map(TwitchUser::new)
                .forEach(user -> channelManager.setPermission(user, UserPermission.ChannelOwner));
		Stream.of( "nicklmg", "lttghost", "antvenom" )
                .map(TwitchUser::new)
                .forEach(user -> channelManager.setPermission(user, UserPermission.BotAdmin));
		Stream.of( "airdeano", "alpenwasser", "blade_of_grass", "colonel_mortis", "daveholla", "dezeltheintern", "dvoulcaris", "ecs_community", "ericlee30", "foxhound590", "glenwing", "ixi_your_face", "linusbottips", "looneyschnitzel", "ltt_bot", "mg2r", "prolemur", "rizenfrmtheashes",  "str_mape", "wh1skers", "whaler_99", "windspeed36", "woodenmarker", "wrefur" )
                .map(TwitchUser::new)
                .forEach(user -> channelManager.setPermission(user, UserPermission.ChannelModerator));
	}

	/**
	 * If a senderOrChannel sends the same message 3 times in a row they are timed out.
	 */
	private void spamDetector(TwitchMessage twitchMessage) {
        ImmutableTwitchMessageList userMessages = channelManager
				.getMessageSnapshot(twitchMessage.getSender());

		if(twitchMessage.getMessagePayload().length() > 5 && twitchMessage.getLegalCharRatio(permittedChars) < 0.1)
			ban(twitchMessage.getSender().getUsername(), twitchMessage.getMessagePayload(), 20, "ASCII art ban", "You have been timed out for posting ASCII art.");

		if(userMessages.size() > 2 && (float) userMessages.size()/(float) userMessages.getMessageTimePeriod().toStandardSeconds().getSeconds() > msgpersec){
			ban(twitchMessage.getSender().getUsername(), twitchMessage.getMessagePayload(), 20, "More than " + msgpersec + " messages/second", "You have been timed out for posting messages to quickly.");
			return;
		}
		
		if(commandWords.contains(twitchMessage.getMessagePayload())) return;
		if(channelManager.getMessageSnapshot().containsSimplePayload(twitchMessage.getSimpleMessagePayload()) >= repetitionSearch)
			ban(twitchMessage.getSender().getUsername(), twitchMessage.getMessagePayload(), 20, "Repeated Message Found", "You have been timed out. Your message has been posted in the chat recently.");
        else if (userMessages.containsSimplePayload(twitchMessage.getSimpleMessagePayload()) >= 2) {
            ban(twitchMessage.getSender().getUsername(), twitchMessage.getMessagePayload(), 20, "Repeated Message Found", "You have been timed out for repeating the same message.");
        }
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
		if(officialReason.length() != 0) privateMessageSender.sendWhisperAsync(sender, officialReason);
		publicMessageSender.sendMessage(twitchChannelName, ".timeout " + sender + " " + banLength);
		banHistory.put(sender, banLength);
		actionLog.info("Timeout {} for {}s. Reason: {}. Message: {}", sender, banLength, reason, message);
	}
}
