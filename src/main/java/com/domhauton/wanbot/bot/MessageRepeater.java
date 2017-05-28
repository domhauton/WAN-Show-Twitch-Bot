package com.domhauton.wanbot.bot;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class MessageRepeater implements Runnable, Closeable {
  private Logger log = LogManager.getLogger();
  private Random randomNumberGenerator = new Random();
  private LinkedList<String> messages;
  private int timeSec = 210;
  private boolean on = true;

  private Consumer<String> messageConsumer;
  private final ScheduledExecutorService scheduledExecutorService;

  MessageRepeater(Consumer<String> messageConsumer, Set<String> messages) {
    this.messageConsumer = messageConsumer;
    this.messages = new LinkedList<>(messages);
    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
        .setNameFormat("MESSAGE-repeater-thread-%d")
        .build();
    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
  }

  public void run() {
    log.info("Running repeater scheduler");
    scheduledExecutorService.scheduleAtFixedRate(this::sendRandomMessage, 60L, timeSec, TimeUnit.SECONDS);
  }

  private void sendRandomMessage() {
    if (on) {
      ImmutableList<String> messageCopy = ImmutableList.copyOf(messages);
      Integer indexOfMessage = randomNumberGenerator.nextInt(messageCopy.size());
      String messageToSend = messageCopy.get(indexOfMessage);
      log.info("Sending repeated MESSAGE: {}", messageToSend);
      messageConsumer.accept(messageToSend);
    } else {
      log.info("Not sending MESSAGE as repeater is off.");
    }
  }

  void setFrequency(int freq) {
    this.timeSec = freq;
  }

  void toggleState() {
    on = !on;
  }

  void clearAll() {
    log.info("Removing all {} messages.", messages.size());
    messages.clear();
  }

  synchronized String clearLast() {
    log.info("Removing last MESSAGE.");
    try {
      return messages.removeLast();
    } catch (NoSuchElementException e) {
      log.warn("No messages to remove. Ignoring removal request.");
      return "n/a";
    }
  }

  synchronized void addMessage(String newMessage) {
    messages.add(newMessage);
  }

  static public Set<String> getDefault() {
    return new HashSet<>(Arrays.asList("Want to support Luke?  http://teespring.com/stores/linusmediagroup",
        "Want to support Linus?  http://teespring.com/stores/linusmediagroup",
        "Like turnips? Click here: http://teespring.com/stores/linusmediagroup",
        "Hate the ads on the forum? Become a contributor http://linustechtips.com/main/store/",
        "Want to support Linus Media Group directly? Become a contributor on the forum http://linustechtips.com/main/store/"));
  }

  @Override
  public void close() {
    log.info("Shutting down message repeater.");
    scheduledExecutorService.shutdown();
  }
}
