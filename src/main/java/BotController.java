import twitch.channel.ChannelManager;
import twitch.channel.message.ImmutableTwitchMessageList;
import twitch.channel.data.TwitchMessage;
import twitch.channel.data.TwitchUser;
import twitch.channel.permissions.UserPermission;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import twitch.chat.data.OutboundTwitchMessage;
import twitch.chat.routing.TwitchMessageRouter;
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

class BotController {
	private Logger log = LogManager.getLogger();
	private Logger messageLog = LogManager.getLogger("Message Log");
	private Logger actionLog = LogManager.getLogger("Action Log");
    private MessageRepeater messageRepeater;
    private TwitchMessageRouter twitchMessageRouter;

	private ChannelManager channelManager;
	private Set<String> blockedWords;
	private Set<String> blockedMessages;

	private String lastHostLink;

	private int maxMsg = 20;
	private int linkRepeatCountHost = 7;
	private int linkRepeatCountMod = 5;
    private float messagesPerSecond = 2.5f;
    private int repetitionSearch = 4;
	
	private DateTime showStartTime = new DateTime(2016, 3, 11, 16, 30, DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Vancouver"))); //The set time the show should start every week.
    private DateTime commandTimeTTL, commandTimeLLL, commandTimeHelp, streamStartTime;
	private ImmutableSet<Character> permittedChars;
	private HashMap<String, Integer> banHistory = new HashMap<>();

    private List<String> commandWords = new ArrayList<>(Arrays.asList("!ttl", "!lll", "!help", "!ttt"));
	
	//private MessageSender msgSender;

    private BitlyDecorator bitlyDecorator;

	@Inject
	public BotController(BitlyDecorator bitlyDecorator,
						 TwitchMessageRouter twitchMessageRouter,
                         MessageRepeater messageRepeater) {
		channelManager = new ChannelManager();
		permittedChars = ImmutableSet.copyOf("abcdefghijklmnopqrstuvwxyz.!@$%123454567890".chars().mapToObj(a -> (char) a).collect(Collectors.toList()));
		this.blockedMessages = new HashSet<>();
		this.blockedWords = new HashSet<>();
		this.bitlyDecorator = bitlyDecorator;
        this.twitchMessageRouter = twitchMessageRouter;

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
    void processMessage(TwitchMessage twitchMessage) {
		channelManager.addChannelMessage(twitchMessage);

		messageLog.info(twitchMessage::toString); //Stores the message in the chat log.

        boolean isMessageCommand = twitchMessage.getMessage().startsWith("!");
        if( isMessageCommand ){
			userCommands(twitchMessage);
		}

        UserPermission userPermissionOnChannel = channelManager.getPermission(twitchMessage.getUsername());
        switch (userPermissionOnChannel) {
            case ChannelOwner:
                hostCommands(twitchMessage);
            case BotAdmin:
                boolean isBotCommand = twitchMessage.getSimpleMessagePayload().startsWith("!bot");
                if(isBotCommand) {
                    sendMessageP( botCommand( twitchMessage.getMessage()));
                }
            case BotModerator:
                operatorCommands( twitchMessage.getUsername(), twitchMessage.getMessage());
            case ChannelModerator:
                break;
            default:
                isMessagePermitted( twitchMessage );
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
			} else if(sCommand[0].equalsIgnoreCase("messageCap")){
				if(newVal > 0 && newVal <= 50){
                    int messageCap = (int) newVal;
					return "messageCap set to " + messageCap;
				} else {
					return "messageCap must be between 0 and 50";
				}
			} else if(sCommand[0].equalsIgnoreCase("rPostVal")){
				if(newVal > 0 && newVal <= 50){
                    int rPostVal = (int) newVal;
					return "rPostVal set to " + rPostVal;
				} else {
					return "rPostVal must be between 0 and 50";
				}
			} else if(sCommand[0].equalsIgnoreCase("messagesPerSecond")){
				if(newVal > 0 && newVal <= 50){
					messagesPerSecond = newVal;
					return "secpermsg set to " + messagesPerSecond;
				} else {
					return "secpermsg must be between 0 and 50";
				}
			} else {
                if (sCommand[0].equalsIgnoreCase("longestSubStringAllowed")) {
                    if (newVal > 3 && newVal <= 50) {
                        int longestSubStringAllowed = (int) newVal;
                        return "longestSubStringAllowed set to " + longestSubStringAllowed;
                    } else {
                        return "longestSubStringAllowed must be between 3 and 50";
                    }
                } else if (sCommand[0].equalsIgnoreCase("repetitionSearch")) {
                    if (newVal > 1 && newVal <= maxMsg - 1) {
                        repetitionSearch = (int) newVal;
                        return "repetitionSearch set to " + repetitionSearch;
                    } else {
                        return "repetitionSearch must be between 1 and maxMsg";
                    }
                } else if (sCommand[0].equalsIgnoreCase("messageFrequency")) {
                    if (newVal > 60) {
                        messageRepeater.setFrequency((int) newVal);
                        return "messageFrequency set to " + (int) newVal;
                    } else {
                        return "messageFrequency must be more than 60";
                    }
                } else if (sCommand[0].equalsIgnoreCase("messageRepToggle")) {
                    messageRepeater.toggleState();
                    return "messageRepetition Toggled.";
                } else if (sCommand[0].equalsIgnoreCase("addStartTime")) {
                    showStartTime = showStartTime.plusSeconds((int) newVal);
                    return "Show start time set to: " + showStartTime.toString(ISODateTimeFormat.basicOrdinalDateTimeNoMillis());
                } else {
                    return "Variable name not found";
                }
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
                .filter(message -> !channelManager.checkPermission(message.getUsername(), UserPermission.ChannelModerator))
                .filter(message -> message.getMessage().toLowerCase().contains(lowerCaseWord))
                .forEach(message -> ban(message.getUsername(), message.getMessage(), 45, "Blacklisted word: " + word, ""));
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
		if(blockedMessages.contains(lowerCaseMessage))
			return lowerCaseMessage + " already on blacklist.";
		blockedMessages.add(lowerCaseMessage);
        channelManager.getMessageSnapshot()
                .stream()
                .filter(message -> !channelManager.checkPermission(message.getUsername(), UserPermission.ChannelModerator))
                .filter(message -> message.getMessage().equalsIgnoreCase(lowerCaseMessage))
                .forEach(message -> ban(message.getUsername(), message.getMessage(), 45, "Blacklisted message: " + word, ""));
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
	 * Checks if the message sent is a current request
	 */
	private void userCommands(TwitchMessage twitchMessage) {
        String message = twitchMessage.getMessage().substring(1);
		if (message.equalsIgnoreCase("TTL") || message.equalsIgnoreCase("TTT")) {
            String senderUsername = twitchMessage.getUsername();
			String timeTillLive = getTimeTillLive(senderUsername);
			if( !Strings.isNullOrEmpty(timeTillLive) ){
                OutboundTwitchMessage outboundChannelMessage = new OutboundTwitchMessage(timeTillLive, twitchMessage.getTwitchChannel());
                twitchMessageRouter.sendChannelMessage(outboundChannelMessage);
                OutboundTwitchMessage outboundWhisper = new OutboundTwitchMessage(timeTillLive, senderUsername);
                twitchMessageRouter.sendUserWhisper(outboundWhisper);
            }
		} else if (message.equalsIgnoreCase("LLL"))
			lastLinusLink(twitchMessage.getUsername(), twitchMessage.getTwitchChannel());
		else if (message.equalsIgnoreCase("HELP"))
			sendHelpMessage(twitchMessage.getUsername(), twitchMessage.getTwitchChannel());
		else if (message.startsWith("uptime"))
			uptime(twitchMessage.getUsername(), twitchMessage.getTwitchChannel());
	}

	private void uptime(String senderUserName, String twitchChannelName){
        Period periodSinceStreamStart = new Period(streamStartTime, DateTime.now());
        boolean liveWithinLastMin = periodSinceStreamStart.toStandardSeconds().getSeconds() < 60;
        String outboundMessagePayload = liveWithinLastMin ? "Linus last went live in the last minute." : "Linus last went live: " + DateTimeUtil.convertPeriodToHumanReadableString(periodSinceStreamStart) + " ago.";

        boolean sendToChannel = new Period(commandTimeTTL, DateTime.now()).toStandardSeconds().getSeconds() >= 40;

        if(sendToChannel){
            OutboundTwitchMessage outboundChannelMessage = new OutboundTwitchMessage(outboundMessagePayload, twitchChannelName);
            twitchMessageRouter.sendChannelMessage(outboundChannelMessage);
            commandTimeTTL = DateTime.now();
        } else {
            OutboundTwitchMessage outboundUserWhisper = new OutboundTwitchMessage(outboundMessagePayload, senderUserName);
            twitchMessageRouter.sendUserWhisper(outboundUserWhisper);
        }

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
            return "The next WAN Show should begin in: " + DateTimeUtil.convertPeriodToHumanReadableString(periodTillShow);
        }
	}

	private void hostCommands(TwitchMessage twitchMessage){
        final String message = twitchMessage.getMessage();
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
	private void lastLinusLink(String sourceUserUsername, String sourceChannel) {
        boolean lastLinkExists = !Strings.isNullOrEmpty(lastHostLink);
        String outboundMessagePayload = lastLinkExists ? "Linus' Last Link: " + lastHostLink : "Linus has not posted a link recently.";
        OutboundTwitchMessage outboundWhisper = new OutboundTwitchMessage(outboundMessagePayload, sourceUserUsername);
        twitchMessageRouter.sendUserWhisper(outboundWhisper);

        boolean sendToChannel = new Period(commandTimeLLL, DateTime.now()).toStandardSeconds().getSeconds() > 40;
        if( sendToChannel ){
            OutboundTwitchMessage outboundTwitchMessage = new OutboundTwitchMessage(outboundMessagePayload, sourceChannel);
            twitchMessageRouter.sendChannelMessage(outboundTwitchMessage);
            commandTimeLLL = DateTime.now();
		}
	}

	/**
	 * Sends a command list to the users
	 */
	private void sendHelpMessage(String sourceUserUsername, String sourceChannel) {
        String outboundMessagePayload = "You can find out more about the bot here: http://bit.ly/1DnLq9M. If you want to request an unban please tweet @deadfire19";
        OutboundTwitchMessage outboundWhisper = new OutboundTwitchMessage(outboundMessagePayload, sourceUserUsername);
        twitchMessageRouter.sendUserWhisper(outboundWhisper);

        boolean sendToChannel = new Period(commandTimeLLL, DateTime.now()).toStandardSeconds().getSeconds() > 30;
        if( sendToChannel ){
            OutboundTwitchMessage outboundTwitchMessage = new OutboundTwitchMessage(outboundMessagePayload, sourceChannel);
            twitchMessageRouter.sendChannelMessage(outboundTwitchMessage);
            commandTimeHelp = DateTime.now();
        }
	}

	/**
	 * Checks if a message is in the blacklist
	 */
	private void isMessagePermitted(TwitchMessage twitchMessage, Collection<String> blockedWords, Collection<String> blockedMessages) {
        boolean containsBlacklistedWord = blockedWords.stream().anyMatch(twitchMessage::containsString);
        boolean isBlacklistedMessage = blockedMessages.stream().anyMatch(twitchMessage::equalsSimplePayload);
        boolean messagePermitted = containsBlacklistedWord || isBlacklistedMessage;
        if( messagePermitted ){
            TwitchMessageRouter.
            ban(
                    twitchMessage.getUsername(),
                    45,
                    "Matched blacklisted word", "Timeout - Blacklisted word"
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
				.getMessageSnapshot(twitchMessage.getUsername());

		if(twitchMessage.getMessage().length() > 5 && twitchMessage.getLegalCharRatio(permittedChars) < 0.1)
			ban(twitchMessage.getUsername(), twitchMessage.getMessage(), 20, "ASCII art ban", "You have been timed out for posting ASCII art.");

		if(userMessages.size() > 2 && (float) userMessages.size()/(float) userMessages.getMessageTimePeriod().toStandardSeconds().getSeconds() > messagesPerSecond){
			ban(twitchMessage.getUsername(), twitchMessage.getMessage(), 20, "More than " + messagesPerSecond + " messages/second", "You have been timed out for posting messages to quickly.");
			return;
		}

		if(commandWords.contains(twitchMessage.getMessage())) return;
		if(channelManager.getMessageSnapshot().containsSimplePayload(twitchMessage.getSimpleMessagePayload()) >= repetitionSearch)
			ban(twitchMessage.getUsername(), twitchMessage.getMessage(), 20, "Repeated Message Found", "You have been timed out. Your message has been posted in the chat recently.");
        else if (userMessages.containsSimplePayload(twitchMessage.getSimpleMessagePayload()) >= 2) {
            ban(twitchMessage.getUsername(), twitchMessage.getMessage(), 20, "Repeated Message Found", "You have been timed out for repeating the same message.");
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
		if(!Strings.isNullOrEmpty(officialReason)){
            OutboundTwitchMessage outboundTwitchMessage = new OutboundTwitchMessage(officialReason, sender);
            privateMessageSender.sendWhisperAsync(outboundTwitchMessage);
        }
		publicMessageSender.sendMessage(twitchChannelName, ".timeout " + sender + " " + banLength);
		banHistory.put(sender, banLength);
		actionLog.info("Timeout {} for {}s. Reason: {}. Message: {}", sender, banLength, reason, message);
	}
}
