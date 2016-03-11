import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.*;
import org.joda.time.*;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import util.BitlyDecorator;

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

	private int maxMsg = 20;
	private int linkRepeatCountHost = 7;
	private int linkRepeatCountMod = 5;
	private int voteBanMax = 2;
	private int messageCap = 8;
	private int rPostVal = 8;
	private float msgpersec = 2.5f;
	private int longestSubStringAllowed = 13;
	private int repetitionSearch = 4;
	boolean banASCII = true;
	
	private DateTime showStartTime = new DateTime(2016, 3, 11, 16, 30, DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Vancouver"))); //The set time the show should start every week.
	private long timeLastTTLsent, timeLastLinusLink, timeLastHelp; //Saves the last time each help message was sent.
	private HashMap<String, LinkedList<String[]>> messageHistory = new HashMap<>();
	private LinkedList<String[]> mainMessageHistory = new LinkedList<>();
	private HashMap<String, ArrayList<String>> banVotes = new HashMap<>();
	private String alphabetList = "abcdefghijklmnopqrstuvwxyz.!@$%123454567890";
	private HashMap<String, Integer> banHistory = new HashMap<>();
	//Sets up the file writers.
	private FileWriter outBans = null;
	private FileWriter outSettings = null;
	private FileWriter timeStampFile = null;
	//To store the timestamps.
	private HashMap<String, ArrayList<String>> timeStamps = new HashMap<>();
	//Stores the actual show start time.
	private long startOfShow;
	//Stores blacklisted words.
	private List<String> wordBlacklist;
	private List<String> messageBlacklist;
	//Help message that is posted if someone asks for the help command.
	private String helpMessage = "You can find out more about the bot here: http://bit.ly/1DnLq9M. If you want to request an unban please tweet @deadfire19";
	
	private List<String> commandWords = new ArrayList<String>(Arrays.asList("!ttl", "!lll", "!help", "!ttt"));
	//VIP Lists
	//tier 0 is reserved for hosts.
	//tier 1 is reserved for bot operators.
	//tier 2 is reserved for elevated permission moderators.
	//tier 3 is reserved for people immune to auto moderation.
	private List<String> tier0, tier1, tier2, tier3;
	
	private MessageSender msgSender;
	private Thread msgSenderThread;
	private MessageRepeater msgRep;
	private Thread msgRepThread;

	private BitlyDecorator bitlyDecorator;
	
	private String blacklistFileName = "settings.txt";
	
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
		this.setName(twitchUsername);
		this.setMessageDelay(50);
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
		msgSenderThread = new Thread(msgSender);
		msgSenderThread.start();
		msgRep = new MessageRepeater(msgSender);
		msgRepThread = new Thread(msgRep);
		msgRepThread.start();
	}

	/**
	 * How the bot will react to messages received.
	 */
	public void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		addMsgToLog(sender, message.toLowerCase());
		messageLog.info("{} : {}", sender, message.toLowerCase()); //Stores the message in the chat log.
		if(message.startsWith("!")){//Checks if the message is a command
			userCommands(sender, message.substring(1)); //Checks if the message contains keywords for the bot.
		}
		if(tier0.contains(sender)) hostCommands(sender, message);
		if(tier1.contains(sender) && message.startsWith("!bot")) sendMessageP(botCommand(message));
		if(tier2.contains(sender)) operatorCommands(sender, message);
		message = message.toLowerCase();
		if(!tier3.contains(sender)){
			messageChecker(sender, message); //Checks if the message is allowed.
			spamDetector(sender, message);
		}
	}
	
	/**
	 * Adds a message to the messageHistory hashtable
	 * @param sender
	 * @param message
	 */
	private void addMsgToLog(String sender, String message){
		LinkedList<String[]> messagelist;
		if(!messageHistory.containsKey(sender)){
			messagelist = new LinkedList<String[]>();
		} else {
			messagelist = messageHistory.get(sender);
		}
		String[] messageToStore = {String.valueOf(System.currentTimeMillis()/1000), message}; 
		while(messagelist.size() > maxMsg-1) messagelist.removeLast();
		messagelist.addFirst(messageToStore);
		messageHistory.put(sender, messagelist);
		//Add the message to the main history
		String[] mainMessageToStore = {String.valueOf(System.currentTimeMillis()/1000), message, sender}; 
		while(mainMessageHistory.size() > maxMsg*10) mainMessageHistory.removeLast();
		mainMessageHistory.addFirst(mainMessageToStore);
	}

	/**
	 * Used to add a word to the blacklist
	 * 
	 * @param word
	 */
	public String botCommand(String word) {
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
			}else if(sCommand[0].equalsIgnoreCase("repetitionSearch")){
				if(newVal > 1 && newVal <= maxMsg-1){
					repetitionSearch = (int) newVal;
					return "repetitionSearch set to " + repetitionSearch;
				} else {
					return "repetitionSearch must be between 1 and maxMsg";
				}
			}else if(sCommand[0].equalsIgnoreCase("banASCII")){
				if(newVal == 0){
					banASCII = false;
					return "Bot will no longer ban ASCII";
				} else if(newVal == 1){
					banASCII = true;
					return "Bot will ban ASCII art.";
				} else {
					return "banASCII must be 0 or 1";
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
	private String bLWord(String word){
		word = word.toLowerCase();
		if(word.length()<3) return "Word not long enough.";
		if(wordBlacklist.contains(word))
			return word + " already on blacklist.";
		wordBlacklist.add(word);
		int historyCheck = mainMessageHistory.size();
		if (historyCheck > 200) historyCheck = 200;
		String message, sender;
		for(int idx1 = 0; idx1 < historyCheck; idx1++){
			message = mainMessageHistory.get(idx1)[1];
			sender = mainMessageHistory.get(idx1)[2];
			if (message.contains(word) && !tier3.contains(sender)) {
				ban(sender, message, 45, "Blacklisted word: " + word, "");
			}
		}
		addSetting("-w " + word);
		return word + " added to blacklist. Previous messages breaching rule this will be banned.";
	}
	/**
	 * Removes a word from the blacklist if possible. If not possible it is ignored.
	 * @param word word to remove.
	 * @return response message.
	 */
	private String removeBLWord(String word){
		if(wordBlacklist.contains(word)){
			wordBlacklist.remove(word);
			return word + " removed from the blacklist.";
		}
		return word + " not found on the blacklist";
	}
	
	/**
	 * Adds a word to the blacklist.
	 * @param word Word to add to blacklist
	 * @return response word
	 */
	private String bLMsg(String word){
		word = word.toLowerCase();
		if(messageBlacklist.contains(word))
			return word + " already on blacklist.";
		messageBlacklist.add(word);
		int historyCheck = mainMessageHistory.size();
		if (historyCheck > 100) historyCheck = 100;
		String message, sender;
		for(int idx1 = 0; idx1 < historyCheck; idx1++){
			message = mainMessageHistory.get(idx1)[1];
			sender = mainMessageHistory.get(idx1)[2];
			if (message.equals(word) && !tier3.contains(sender)) {
				ban(sender, message, 45, "Blacklisted message: " + word, "");
			}
		}
		addSetting("-m" + word);
		return word + " added to message blacklist. Previous messages breaching this rule will be banned.";
	}
	/**
	 * Removes a word from the blacklist if possible. If not possible it is ignored.
	 * @param word word to remove.
	 * @return response word.
	 */
	private String removeBLMsg(String word){
		if(wordBlacklist.contains(word)){
			wordBlacklist.remove(word);
			return word + " removed from the blacklist.";
		}
		return word + " not found on the blacklist";
	}

	private String addOperator(String command){
		int tier;
		String[] splitCommand = command.split(" ");
		if (splitCommand.length != 2) return "Syntax Error.";
		try{
			tier = Integer.parseInt(splitCommand[0]);
			switch(tier){
			case 0:
				if(!tier0.contains(splitCommand[1])) tier0.add(splitCommand[1]);
				if(!tier1.contains(splitCommand[1])) tier1.add(splitCommand[1]);
				if(!tier2.contains(splitCommand[1])) tier2.add(splitCommand[1]);
				if(!tier3.contains(splitCommand[1])) tier3.add(splitCommand[1]);
				addSetting("-o 0 " + splitCommand[1]);
				return splitCommand[1] + " added to tier0";
			case 1:
				if(!tier1.contains(splitCommand[1])) tier1.add(splitCommand[1]);
				if(!tier2.contains(splitCommand[1])) tier2.add(splitCommand[1]);
				if(!tier3.contains(splitCommand[1])) tier3.add(splitCommand[1]);
				addSetting("-o 1 " + splitCommand[1]);
				return splitCommand[1] + " added to tier1";
			case 2:
				if(!tier2.contains(splitCommand[1])) tier2.add(splitCommand[1]);
				if(!tier3.contains(splitCommand[1])) tier3.add(splitCommand[1]);
				addSetting("-o 2 " + splitCommand[1]);
				return splitCommand[1] + " added to tier2";
			case 3:
				if(!tier3.contains(splitCommand[1])) tier3.add(splitCommand[1]);
				addSetting("-o 3 " + splitCommand[1]);
				return splitCommand[1] + " added to tier3";
			default:
				return "Error: tier" + splitCommand[0] + " does not exist.";
			}
		}catch(Exception e){
			return "Syntax Error.";
		}
	}
	
	private String rmOperator(String name){
		if(tier0.contains(name) && tier0.size() == 1) return "You cannot remove the last tier0 operator";
		tier3.remove(name);
		tier2.remove(name);
		tier1.remove(name);
		tier0.remove(name);
		return name + " is no longer an operator.";
	}
	
	private String setStartTime(){
		startOfShow = System.currentTimeMillis()/1000;
		timeStamps = null;
		System.gc();
		timeStamps = new HashMap<>();
		return "Show Start time has been set.";
	}

	/**
	 * You should send messages using this. It saves all the files to a log.
	 * @param message
	 *            Message being sent.
	 */
	public void sendMessageP(String message) {
		messageLog.info("Sending message: {}", message);
		msgSender.sendMessage(message);
	}

	/**
	 * Checks if the message sent is a current request
	 * 
	 * @param sender
	 *            Message sender.
	 * @param message
	 *            Message sent.
	 */
	private void userCommands(String sender, String message) {
		if (message.equalsIgnoreCase("TTL") || message.equalsIgnoreCase("TTT")) {
			String timeTillLive = getTimeTillLive();
			if( !Objects.isNull(timeTillLive) )
				sendMessageP( timeTillLive );
		} else if (message.equalsIgnoreCase("LLL"))
			lastLinusLink();
		else if (message.equalsIgnoreCase("HELP"))
			sendHelpMessage();
		else if (message.startsWith("ts"))
			timeStamp(sender, message.substring(3)); 
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
	public String getTimeTillLive() {
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

	private void hostCommands(String sender, String message){
		//Repeats messages starting with HTTP:// & HTTPS://
		if(message.startsWith("http://") || message.startsWith("https://")){
			try{
				message = bitlyDecorator.shortenURL(message);
			} catch (Exception e){
				//Do Nothing (BAD)
			}
			addMsgToLog("Host Link Log", message);
			for (int x = 0; x < linkRepeatCountHost; x++) sendMessageP(message);
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
			if (messageHistory.containsKey("Host Link Log")) {
				String lastLink = messageHistory.get("Host Link Log").getFirst()[1];
				sendMessageP("Linus' Last Link: " + lastLink);
			} else {
				sendMessageP("Linus has not posted a link recently.");
			}
		}
		timeLastLinusLink = System.currentTimeMillis() / 1000;
	}

	/**
	 * Sends a command list to the users
	 */
	private void sendHelpMessage() {
		if ((System.currentTimeMillis() / 1000) > (timeLastHelp + 30)) {
			sendMessageP(helpMessage);
		}
		timeLastHelp = System.currentTimeMillis() / 1000;
	}

	/**
	 * Checks if a message is in the blacklist
	 * 
	 * @param sender
	 * @param message
	 */
	private void messageChecker(String sender, String message) {
		for (int x = 0; x < this.wordBlacklist.size(); x++) {
			if (message.contains(wordBlacklist.get(x))) {
				ban(sender, message, 45, "Blacklisted word: " + wordBlacklist.get(x), "Timeout - Blacklisted word");
			}
		}
		for (int x = 0; x < this.messageBlacklist.size(); x++) {
			if (message.equals(messageBlacklist.get(x))) {
				ban(sender, message, 45, "Blacklisted message: " + messageBlacklist.get(x), "Timeout - Blacklisted Message");
			}
		}
	}

	/**
	 * Generates the blacklist
	 */
	private void loadSettings() {
        String[] wordBlacklistPrimitive = {"nigger", "nigga", "nazi", "strawpoll.me"};
		wordBlacklist = Arrays.stream(wordBlacklistPrimitive).collect(Collectors.toList());
		messageBlacklist = new ArrayList<>();
		String[] streamerArray = {"slick_pc", "linustech", "luke_lafr"};
        tier0 = Arrays.stream(streamerArray).collect(Collectors.toList());
        String[] opArray = {"nicklmg", "lttghost"};
		tier1 = Arrays.stream(opArray).collect(Collectors.toList());
		tier2 = new ArrayList<>();
        String[] modArray = {"airdeano", "alpenwasser", "antvenom", "blade_of_grass", "colonel_mortis", "daveholla", "dezeltheintern", "dvoulcaris", "ecs_community", "ericlee30", "foxhound590", "glenwing", "ixi_your_face", "linusbottips", "looneyschnitzel", "ltt_bot", "mg2r", "prolemur", "rizenfrmtheashes",  "str_mape", "wh1skers", "whaler_99", "windspeed36", "woodenmarker", "wrefur"};
		tier3 = Arrays.stream(modArray).collect(Collectors.toList());
	}
	
	private void addSetting(String setting){
		try{
            InputStream inputStream;
            if((inputStream = getClass().getResourceAsStream(blacklistFileName)) == null)
                throw new FileNotFoundException();
            Scanner scanner = new Scanner(inputStream);
			while(scanner.hasNextLine()){
				if(scanner.nextLine().equals(setting))
					scanner.close();
					return;
			}
			scanner.close();
			outSettings.write(setting + "\r\n");
			outSettings.flush();
		} catch (IOException e) {
			System.err.println("Failed to add setting to file.");
		}
	}

	/**
	 * If a sender sends the same message 3 times in a row they are timed out.
	 */
	private void spamDetector(String sender, String message) {
		LinkedList<String[]> userMessages = messageHistory.get(sender);
		
		char[] messagecharArray = message.toLowerCase().toCharArray();
		int validCharCount = 0;
		for(char msgchar : messagecharArray){
			if(alphabetList.contains(String.valueOf(msgchar))){
				validCharCount++;
			}
		}
		if((message.length() > 5) && (((float)validCharCount/(float)message.length()) < 0.1) && banASCII){
			ban(sender, message, 20, "ASCII art ban", "Timeout - Excessive symbol use.");
		}
		
		if(userMessages == null) return;//This should never happen.
		if(userMessages.size() < 5) return;//If a user has <5 messages it ignores them.
		int postingFrequency;
		if(userMessages.size() > rPostVal){
			postingFrequency = Integer.parseInt(userMessages.getFirst()[0]) - Integer.parseInt(userMessages.get(userMessages.size()-rPostVal)[0]);
				if(postingFrequency == 0) postingFrequency++;
			postingFrequency = rPostVal/postingFrequency;
		} else {
			postingFrequency = Integer.parseInt(userMessages.getFirst()[0]) - Integer.parseInt(userMessages.getLast()[0]);
			if(postingFrequency == 0) postingFrequency++;
			postingFrequency = userMessages.size()/postingFrequency;
		}
		if(postingFrequency > msgpersec){
			ban(sender, message, 20, "More than " + msgpersec + " messages/second", "Timeout - Flooding Chat");
			return;
		}
		
		if(commandWords.contains(message)) return;
		if(findExactMessage(userMessages)){
			ban(sender, message, 20, "Repeated Message Found", "Timeout - Message Repetition");
			return;
		}
		if(findExactMessage(mainMessageHistory)){
			ban(sender, message, 20, "Repeated Message Found", "Timeout - Message Repetition");
			return;
		}
	}
	
	private void timeStamp(String sender, String message){
		ArrayList<String> currentList;
		if(timeStamps.containsKey(sender)){
			currentList = timeStamps.get(sender);
		} else {
			currentList = new ArrayList<String>();
		}
		if(message.equalsIgnoreCase("save") && timeStamps.containsKey(sender) && currentList.size()>0){
			try{
				timeStampFile.write("Time Stamps by: " + sender + "\r\n");
				for (String timeStamp: currentList){
					timeStampFile.write(timeStamp + "\r\n");
				}
				timeStampFile.write("\r\n");
				timeStampFile.flush();
				sendMessageP("You have submitted " + Integer.toString(currentList.size()) + " timestamps.");
				timeStamps.remove(sender);
			} catch(Exception e){
				System.err.println("Failed to save timestamps");
			}
			return;
		}
		long timeIntoShow = (System.currentTimeMillis()/1000)-startOfShow;
		String time = "";
		long hours = timeIntoShow / (60 * 60);
		if (0<=hours && hours<=9) time += "0";
		time += Integer.toString((int) hours) + ":";
		timeIntoShow -= (hours * 60 * 60);
		long minutes = timeIntoShow / (60);
		if (0<=minutes && minutes<=9)time += "0";
		time += Integer.toString((int) minutes) + ":";
		long seconds = timeIntoShow - (minutes * 60);
		if (0<=seconds && seconds<=9)time += "0";
		time += Integer.toString((int) seconds) + " ";
		currentList.add(time + message);
		timeStamps.put(sender, currentList);
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
    
	private boolean findExactMessage(LinkedList<String[]> messageList){
		String lastMessage = messageList.getFirst()[1].replaceAll(" ", "");
		for(int idx1 = 1; idx1<repetitionSearch+1; idx1++){
			if(lastMessage.equals(messageList.get(idx1)[1].replaceAll(" ", ""))) return true;
		}
		return false;
	}
	
	private String compareStrings(String s1, String s2){
		String longestMatch = "";//Stores the longest match
		int cnt1, tillEnd, tillEnd2, idx1, idx2;
		for(idx1 = 0; idx1 < s1.length(); idx1++){
			for(idx2 = 0; idx2 < s2.length(); idx2++){
				cnt1 = 0; //Reset counter
				//Find chars till the end.
				tillEnd = s1.length()-idx1;
				tillEnd2 = s2.length()-idx2;
				if(tillEnd2 < tillEnd) tillEnd = tillEnd2;
				//Loop forward through the strings while the chars are the same
				while(cnt1<tillEnd && s1.charAt(idx1 +cnt1) == s2.charAt(idx2+cnt1)){
					cnt1++;
				}
				//If the result is longer than the previous best put it in instead.
				if(cnt1>longestMatch.length()) longestMatch = s1.substring(idx1, idx1+cnt1);
			}
		}
		return longestMatch;
	}
}
