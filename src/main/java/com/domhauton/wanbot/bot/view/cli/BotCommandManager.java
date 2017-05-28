package com.domhauton.wanbot.bot.view.cli;

import com.domhauton.wanbot.bot.channel.ChannelManager;
import com.domhauton.wanbot.bot.channel.TwitchUser;
import com.domhauton.wanbot.bot.view.cli.executors.BotCommandResult;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Dominic Hauton on 27/08/2016.
 *
 * An adapter for incoming command messages.
 */
public class BotCommandManager {
  private final ChannelManager channelManager;

  public BotCommandManager(ChannelManager channelManager) {
    this.channelManager = channelManager;
  }

  public BotCommandResult parseCommand(TwitchUser twitchUser, String command) throws BotCommandException {
    List<String> splitCommand = splitCommand(command);
    BotCommand botCommand = new BotCommand(twitchUser, splitCommand);
    if(botCommand.getBotCommandType() == BotCommandType.UNKNOWN) {
      throw new BotCommandException("Could not find matching command to: " + command);
    } else {
      return botCommand
          .getBotCommandType()
          .getCommandExecutor()
          .executeCommand(botCommand.getFlags(), botCommand.getArgs(), channelManager);
    }
  }

  /**
   * Splits a command on whitespaces. Preserves whitespace in quotes. Trims excess whitespace.
   * Supports quote escape within quotes.
   *
   * @return List of split commands
   */
  static List<String> splitCommand(String inputString) {
    List<String> matchList = new LinkedList<>();
    LinkedList<Character> charList = inputString.chars()
        .mapToObj(i -> (char) i)
        .collect(Collectors.toCollection(LinkedList::new));

    // Finite-State Automaton for parsing.

    CommandSplitterState state = CommandSplitterState.BeginningChunk;
    LinkedList<Character> chunkBuffer = new LinkedList<>();

    for (Character currentChar : charList) {
      switch (state) {
        case BeginningChunk:
          switch (currentChar) {
            case '"':
              state = CommandSplitterState.ParsingQuote;
              break;
            case ' ':
              break;
            default:
              state = CommandSplitterState.ParsingWord;
              chunkBuffer.add(currentChar);
          }
          break;
        case ParsingWord:
          switch (currentChar) {
            case ' ':
              state = CommandSplitterState.BeginningChunk;
              String newWord = chunkBuffer.stream().map(Object::toString).collect(Collectors.joining());
              matchList.add(newWord);
              chunkBuffer = new LinkedList<>();
              break;
            default:
              chunkBuffer.add(currentChar);
          }
          break;
        case ParsingQuote:
          switch (currentChar) {
            case '"':
              state = CommandSplitterState.BeginningChunk;
              String newWord = chunkBuffer.stream().map(Object::toString).collect(Collectors.joining());
              matchList.add(newWord);
              chunkBuffer = new LinkedList<>();
              break;
            case '\\':
              state = CommandSplitterState.EscapeChar;
              break;
            default:
              chunkBuffer.add(currentChar);
          }
          break;
        case EscapeChar:
          switch (currentChar) {
            case '"': // Intentional fall through
            case '\\':
              state = CommandSplitterState.ParsingQuote;
              chunkBuffer.add(currentChar);
              break;
            default:
              state = CommandSplitterState.ParsingQuote;
              chunkBuffer.add('\\');
              chunkBuffer.add(currentChar);
          }
      }
    }

    if (state != CommandSplitterState.BeginningChunk) {
      String newWord = chunkBuffer.stream().map(Object::toString).collect(Collectors.joining());
      matchList.add(newWord);
    }
    return matchList;
  }

  private enum CommandSplitterState {
    BeginningChunk, ParsingWord, ParsingQuote, EscapeChar
  }
}
