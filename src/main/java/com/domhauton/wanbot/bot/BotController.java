package com.domhauton.wanbot.bot;

import com.domhauton.wanbot.bot.channel.ChannelManager;
import com.domhauton.wanbot.bot.channel.ChannelOperationException;
import com.domhauton.wanbot.bot.channel.TwitchUser;
import com.domhauton.wanbot.bot.channel.message.ImmutableTwitchMessageList;
import com.domhauton.wanbot.bot.channel.message.TwitchMessage;
import com.domhauton.wanbot.bot.channel.permissions.UserPermission;
import com.domhauton.wanbot.bot.channel.timeouts.TimeoutReason;
import com.domhauton.wanbot.bot.util.DateTimeUtil;
import com.domhauton.wanbot.chat.data.InboundTwitchMessage;
import com.domhauton.wanbot.chat.data.OutboundTwitchMessage;
import com.domhauton.wanbot.chat.data.OutboundTwitchTimeout;
import com.domhauton.wanbot.chat.data.OutboundTwitchWhisper;
import com.domhauton.wanbot.url.URLConverter;
import com.domhauton.wanbot.url.URLConverterImpl;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Period;

import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BotController implements Runnable, Closeable {
  private final Logger log = LogManager.getLogger();
  private final Logger messageLog = LogManager.getLogger("Message Log");
  private final Logger actionLog = LogManager.getLogger("Action Log");
  private final MessageRepeater messageRepeater;
  private final Consumer<OutboundTwitchMessage> twitchMessageConsumer;

  private ChannelManager channelManager;
  private Set<String> blockedWords;
  private Set<String> blockedMessages;

  private String lastHostLink;

  private int maximumMessagesToAverageTime = 20;
  private int linkRepeatCountHost = 7;
  private int linkRepeatCountMod = 5;
  private float messagesPerSecond = 2.5f;
  private int repetitionSearch = 4;

  private DateTime showStartTime = new DateTime(2016, 3, 11, 16, 30, DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Vancouver"))); //The set time the show should run every week.
  private DateTime commandTimeTTL, commandTimeLLL, commandTimeHelp, streamStartTime;
  private ImmutableSet<Character> permittedChars;

  private List<String> commandWords = new ArrayList<>(Arrays.asList("!ttl", "!lll", "!help", "!ttt"));

  private URLConverter urlConverter = new URLConverterImpl();

  public BotController(Consumer<OutboundTwitchMessage> twitchMessageConsumer, String channelName) {
    channelManager = new ChannelManager(channelName);
    permittedChars = ImmutableSet.copyOf("abcdefghijklmnopqrstuvwxyz.!@$%123454567890".chars().mapToObj(a -> (char) a).collect(Collectors.toList()));
    this.blockedMessages = new HashSet<>();
    this.blockedWords = new HashSet<>();
    this.urlConverter = urlConverter;
    this.twitchMessageConsumer = twitchMessageConsumer;

    commandTimeTTL = commandTimeLLL = commandTimeHelp = DateTime.now().minusSeconds(60);
    streamStartTime = new DateTime(2016, 3, 25, 16, 30, DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Vancouver")));

    messageRepeater = new MessageRepeater(this::sendChatMessage, MessageRepeater.getDefault());
    loadSettings();
  }

  /**
   * Processes the given twitchMessage as required for the channel.
   */
  public void processMessage(InboundTwitchMessage inboundTwitchMessage) {
    TwitchMessage twitchMessage = (TwitchMessage) inboundTwitchMessage;
    try {
      channelManager.addChannelMessage(twitchMessage);
      //TODO Blacklist check!
    } catch (ChannelOperationException e) {
      //TODO Panic?
    }

    messageLog.info(twitchMessage::toString); //Stores the MESSAGE in the chat log.
    //TODO Direct some to Bot Command Executor.
  }


  /**
   * Checks if the MESSAGE sent is a current request
   */
  private void userCommands(TwitchMessage twitchMessage) {
    String message = twitchMessage.getMessage().substring(1);
    if (message.equalsIgnoreCase("TTL") || message.equalsIgnoreCase("TTT")) {
      String senderUsername = twitchMessage.getUsername();
      String timeTillLive = getTimeTillLive();
      if (!Strings.isNullOrEmpty(timeTillLive)) {
        OutboundTwitchMessage outboundChannelMessage = new OutboundTwitchMessage(timeTillLive, twitchMessage.getTwitchChannel());
        twitchMessageConsumer.accept(outboundChannelMessage);
        OutboundTwitchWhisper outboundWhisper = new OutboundTwitchWhisper(timeTillLive, senderUsername);
        twitchMessageConsumer.accept(outboundWhisper);
      }
    } else if (message.equalsIgnoreCase("LLL"))
      lastLinusLink(twitchMessage.getUsername(), twitchMessage.getTwitchChannel());
    else if (message.equalsIgnoreCase("HELP"))
      sendHelpMessage(twitchMessage.getUsername(), twitchMessage.getTwitchChannel());
    else if (message.startsWith("uptime"))
      uptime(twitchMessage.getUsername(), twitchMessage.getTwitchChannel());
  }

  private void uptime(String senderUserName, String twitchChannelName) {
    Period periodSinceStreamStart = new Period(streamStartTime, DateTime.now());
    boolean liveWithinLastMin = periodSinceStreamStart.toStandardSeconds().getSeconds() < 60;
    String outboundMessagePayload = liveWithinLastMin ? "Linus last went live in the last minute." : "Linus last went live: " + DateTimeUtil.convertPeriodToHumanReadableString(periodSinceStreamStart) + " ago.";

    boolean sendToChannel = new Period(commandTimeTTL, DateTime.now()).toStandardSeconds().getSeconds() >= 40;

    if (sendToChannel) {
      OutboundTwitchMessage outboundChannelMessage = new OutboundTwitchMessage(outboundMessagePayload, twitchChannelName);
      twitchMessageConsumer.accept(outboundChannelMessage);
      commandTimeTTL = DateTime.now();
    } else {
      OutboundTwitchWhisper outboundUserWhisper = new OutboundTwitchWhisper(outboundMessagePayload, senderUserName);
      twitchMessageConsumer.accept(outboundUserWhisper);
    }
  }

  /**
   * Sends a Message to chat displaying how long till the show begins.
   */
  private String getTimeTillLive() {
    if (new Period(commandTimeTTL, DateTime.now()).toStandardSeconds().getSeconds() < 40) {
      return null;
    }

    commandTimeTTL = DateTime.now();
    //TODO Tacky Solution - Could crash
    while (showStartTime.isBeforeNow()) {
      showStartTime = showStartTime.plusDays(7);
    }
    Period periodTillShow = new Period(DateTime.now(), showStartTime);

    if (periodTillShow.toStandardDays().getDays() > 5) {
      return null;
    } else if (periodTillShow.toStandardSeconds().getSeconds() < 60) {
      return "The next WAN Show should begin soon.";
    } else {
      return "The next WAN Show should begin in: " + DateTimeUtil.convertPeriodToHumanReadableString(periodTillShow);
    }
  }

  private Collection<OutboundTwitchMessage> hostCommands(TwitchMessage twitchMessage) {
    String message = twitchMessage.getMessage();
    if (message.startsWith("http://") || message.startsWith("https://")) {
      lastHostLink = twitchMessage.getMessage();
      return linkRepeater(
          message,
          twitchMessage.getTwitchChannel(),
          twitchMessage.getUsername(), linkRepeatCountHost);
    }
    return Collections.emptyList();
  }

  private Collection<OutboundTwitchMessage> operatorCommands(TwitchMessage twitchMessage) {
    String message = twitchMessage.getMessage();
    Collection<OutboundTwitchMessage> outboundTwitchMessages = new LinkedList<>();
    if (message.startsWith("!link")) {
      Collection<OutboundTwitchMessage> repeatedLinks = linkRepeater(
          message.substring(6),
          twitchMessage.getTwitchChannel(),
          twitchMessage.getUsername(), linkRepeatCountMod);
      outboundTwitchMessages.addAll(repeatedLinks);
    } else if (message.startsWith("!loop add")) {
      messageRepeater.addMessage(message.substring(10));
    } else if (message.startsWith("!loop removeLast")) {
      messageRepeater.clearLast();
    } else if (message.startsWith("!loop removeAll")) {
      messageRepeater.clearAll();
    }
    return outboundTwitchMessages;
  }

  /**
   * Used to repeat links by com.domhauton.wanbot.bot operators.
   */
  private Collection<OutboundTwitchMessage> linkRepeater(
      String url,
      String twitchChannel,
      String sender,
      Integer repeatCount) {
    if (url.startsWith("http://") || url.startsWith("https://")) {
      try {
        url = urlConverter.convertLink(url);
      } catch (Exception e) {
        log.warn("Failed to convert bitly link: {}", url);
      }
    }
    String newMessage = sender + " : " + url;
    return IntStream.range(0, repeatCount)
        .mapToObj(ignore -> new OutboundTwitchMessage(newMessage, twitchChannel))
        .collect(Collectors.toList());
  }

  /**
   * Sends the last link Linus sent out;
   */
  private void lastLinusLink(String sourceUserUsername, String sourceChannel) {
    boolean lastLinkExists = !Strings.isNullOrEmpty(lastHostLink);
    String outboundMessagePayload = lastLinkExists ? "Linus' Last Link: " + lastHostLink : "Linus has not posted a link recently.";
    OutboundTwitchWhisper outboundWhisper = new OutboundTwitchWhisper(outboundMessagePayload, sourceUserUsername);
    twitchMessageConsumer.accept(outboundWhisper);

    boolean sendToChannel = new Period(commandTimeLLL, DateTime.now()).toStandardSeconds().getSeconds() > 40;
    if (sendToChannel) {
      OutboundTwitchMessage outboundTwitchMessage = new OutboundTwitchMessage(outboundMessagePayload, sourceChannel);
      twitchMessageConsumer.accept(outboundTwitchMessage);
      commandTimeLLL = DateTime.now();
    }
  }

  /**
   * Sends a command list to the users
   */
  private void sendHelpMessage(String sourceUserUsername, String sourceChannel) {
    String outboundMessagePayload = "You can find out more about the com.domhauton.wanbot.bot here: http://bit.ly/1DnLq9M. If you want to request an unban please tweet @deadfire19";
    OutboundTwitchWhisper outboundWhisper = new OutboundTwitchWhisper(outboundMessagePayload, sourceUserUsername);
    twitchMessageConsumer.accept(outboundWhisper);

    boolean sendToChannel = new Period(commandTimeHelp, DateTime.now()).toStandardSeconds().getSeconds() > 30;
    if (sendToChannel) {
      OutboundTwitchMessage outboundTwitchMessage = new OutboundTwitchMessage(outboundMessagePayload, sourceChannel);
      twitchMessageConsumer.accept(outboundTwitchMessage);
      commandTimeHelp = DateTime.now();
    }
  }

  /**
   * Checks if a MESSAGE is in the blacklist
   */
  private void isMessagePermitted(TwitchMessage twitchMessage, Set<String> blockedWords, Set<String> blockedMessages) {
    boolean containsBlacklistedWord = blockedWords.stream().anyMatch(twitchMessage::containsString);
    boolean isBlacklistedMessage = blockedMessages.stream().anyMatch(twitchMessage::equalsSimplePayload);
    boolean messagePermitted = containsBlacklistedWord || isBlacklistedMessage;
    if (messagePermitted) {
      timeoutUser(twitchMessage.getTwitchUser(),
          twitchMessage.getTwitchChannel(),
          TimeoutReason.BLACKLISTED_WORD);
    }
  }

  /**
   * Generates the blacklist
   */
  private void loadSettings() {
    blockedWords.addAll(Stream.of("nigger", "nigga", "nazi", "strawpoll.me", "bit.do", "t.co", "lnkd.in", "db.tt", "qr.ae", "adf.ly", "goo.gl", "bitly.com", "cur.lv", "tinyurl.com", "ow.ly", "bit.ly", "adcrun.ch", "ity.im", "q.gs", "viralurl.com", "is.gd", "vur.me", "bc.vc", "twitthis.com", "u.to", "j.mp", "buzurl.com", "cutt.us", "u.bb", "yourls.org", "crisco.com", "x.co", "adcraft.co").collect(Collectors.toList()));
    Stream.of("slick_pc", "linustech", "luke_lafr")
        .map(TwitchUser::new)
        .forEach(user -> channelManager.setPermission(user, UserPermission.ChannelOwner));
    Stream.of("nicklmg", "lttghost", "antvenom")
        .map(TwitchUser::new)
        .forEach(user -> channelManager.setPermission(user, UserPermission.BotAdmin));
    Stream.of("airdeano", "alpenwasser", "blade_of_grass", "colonel_mortis", "daveholla", "dezeltheintern", "dvoulcaris", "eccommunity", "ericlee30", "foxhound590", "glenwing", "ixi_your_face", "linusbottips", "looneyschnitzel", "ltt_bot", "mg2r", "prolemur", "rizenfrmtheashes", "str_mape", "wh1skers", "whaler_99", "windspeed36", "woodenmarker", "wrefur")
        .map(TwitchUser::new)
        .forEach(user -> channelManager.setPermission(user, UserPermission.ChannelModerator));
  }

  /**
   * If a senderOrChannel sends the same MESSAGE 3 times in a row they are timed out.
   */
  private void spamDetector(TwitchMessage twitchMessage) {
    ImmutableTwitchMessageList userMessages = channelManager
        .getMessageSnapshot(twitchMessage.getTwitchUser());

    if (twitchMessage.getMessage().length() > 5 && twitchMessage.getLegalCharRatio(permittedChars) < 0.1)
      timeoutUser(twitchMessage.getTwitchUser(),
          twitchMessage.getTwitchChannel(),
          TimeoutReason.EXCESSIVE_SYMBOLS);

    if (userMessages.size() > 2 && (float) userMessages.size() / (float) userMessages.getMessageTimePeriod().toStandardSeconds().getSeconds() > messagesPerSecond) {
      timeoutUser(twitchMessage.getTwitchUser(),
          twitchMessage.getTwitchChannel(),
          TimeoutReason.MESSAGE_RATE);
      return;
    }

    if (commandWords.contains(twitchMessage.getMessage())) return;
    if (channelManager.getMessageSnapshot().containsSimplePayload(twitchMessage.getSimpleMessagePayload()) >= repetitionSearch)
      timeoutUser(twitchMessage.getTwitchUser(),
          twitchMessage.getTwitchChannel(),
          TimeoutReason.CHAT_REPETITION);
    else if (userMessages.containsSimplePayload(twitchMessage.getSimpleMessagePayload()) >= 2) {
      timeoutUser(twitchMessage.getTwitchUser(),
          twitchMessage.getTwitchChannel(),
          TimeoutReason.MESSAGE_REPETITION);
    }
  }

  /**
   * This MESSAGE to the timeoutUser log file.
   */
  private void timeoutUser(
      TwitchUser twitchUser,
      String channel,
      TimeoutReason timeoutReason) {
    OutboundTwitchWhisper privateUserBanNotification = new OutboundTwitchWhisper(timeoutReason.getMessage(), twitchUser
        .getUsername());
    twitchMessageConsumer.accept(privateUserBanNotification);
    Duration timeoutDuration = channelManager.addUserTimeout(twitchUser.getUsername(), timeoutReason);
    OutboundTwitchMessage twitchTimeout = new OutboundTwitchTimeout(channel, twitchUser.getUsername(), timeoutDuration);
    twitchMessageConsumer.accept(twitchTimeout);
    actionLog.info("Timeout {} for {}. Reason: {}. Message: {}",
        twitchUser::toString,
        timeoutDuration::toString,
        timeoutReason::toString,
        timeoutReason::getMessage);
  }

  private void sendChatMessage(String message) {
    OutboundTwitchMessage privateUserBanNotification = new OutboundTwitchMessage(message, channelManager.getChannelName());
    twitchMessageConsumer.accept(privateUserBanNotification);
  }

  public void setUrlConverter(URLConverter urlConverter) {
    this.urlConverter = urlConverter;
  }

  @Override
  public void close() {

  }

  @Override
  public void run() {
    messageRepeater.run();
  }
}
