package bot.view.cli.executors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import bot.channel.ChannelManager;
import bot.channel.ChannelOperationException;
import bot.channel.settings.enums.ChannelSettingInteger;
import bot.view.cli.BotCommandException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitch.chat.data.OutboundTwitchMessage;

/**
 * Created by Dominic Hauton on 05/09/2016.
 *
 * Will repeat a shortened version of the given link
 */
public class LinkRepeater implements CommandExecutor {
  private Logger log = LogManager.getLogger();

  @Override
  public BotCommandResult executeCommand(ImmutableSet<Character> flags,
                                         ImmutableList<String> args,
                                         ChannelManager channelManager) throws BotCommandException {
    Integer repeatCount = channelManager.getChannelSetting(ChannelSettingInteger.LINK_REPEAT_COUNT);
    if (flags.contains('f')) {
      return messageRepeatForced(args, channelManager, repeatCount);
    } else {
      return messageRepeatWithConvert(args, channelManager, repeatCount);
    }
  }

  private BotCommandResult messageRepeatWithConvert(ImmutableList<String> args,
                                                    ChannelManager channelManager,
                                                    Integer linkRepeatCount)
      throws BotCommandException {
    Optional<String> optionalURL = args.stream()
        .filter(string -> string.startsWith("http://") || string.startsWith("https://"))
        .findFirst();
    if (optionalURL.isPresent()) {
      try {
        String convertedURL = channelManager.customiseURL(optionalURL.get());
        log.info("Sending converted url {} to channel {} times", convertedURL, linkRepeatCount);
        return new BotCommandResult(
            getTwitchMessages(channelManager, convertedURL, linkRepeatCount),
            "Your url was converted and repeated " + linkRepeatCount + " times. " + convertedURL);
      } catch (ChannelOperationException e) {
        log.info("Failed to convert url: {}", optionalURL::get);
        throw new BotCommandException("Try with flag 'f' + Failed to convert URL " + optionalURL);
      }
    } else {
      log.info("Could not repeat given string as not url: {}", optionalURL::get);
      throw new BotCommandException("Your commands should begin with http:// or https://");
    }
  }

  private BotCommandResult messageRepeatForced(ImmutableList<String> args,
                                               ChannelManager channelManager,
                                               Integer linkRepeatCount) {
    String appendedArgs = args.stream().collect(Collectors.joining(" "));
    log.info("Repeating string \"{}\" {} times.", appendedArgs, linkRepeatCount);
    return new BotCommandResult(getTwitchMessages(channelManager, appendedArgs, linkRepeatCount),
        "Your message was repeated.");
  }

  private List<OutboundTwitchMessage> getTwitchMessages(ChannelManager channelManager,
                                                        String messageToRepeat, Integer linkRepeatCount) {
    if (linkRepeatCount == 0) {
      log.warn("Overriding the link repetition count to 1. Cannot repeat 0 times.");
    }
    linkRepeatCount = linkRepeatCount <= 0 ? 1 : linkRepeatCount;
    return IntStream.range(0, linkRepeatCount)
        .boxed()
        .map(x -> new OutboundTwitchMessage(messageToRepeat, channelManager.getChannelName()))
        .collect(Collectors.toList());
  }
}
