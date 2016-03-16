import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class MessageRepeater {
    private Logger log = LogManager.getLogger();
    private Random randomNumberGenerator = new Random();
    private MessageSender messageSender;
    private ImmutableList<String> messages;
    private int timeSec = 210;
    private boolean on = true;

    public MessageRepeater(MessageSender messageSender) {
        this.messageSender = messageSender;
        this.messages = new ImmutableList.Builder<String>()
                .add("Want to support Luke?  http://teespring.com/stores/linusmediagroup")
                .add("Want to support Linus?  http://teespring.com/stores/linusmediagroup")
                .add("Like turnips? Click here: http://teespring.com/stores/linusmediagroup")
                .add("Hate the ads on the forum? Become a contributor http://linustechtips.com/main/store/")
                .add("Want to support Linus Media Group directly? Become a contributor on the forum http://linustechtips.com/main/store/")
                .build();
    }

    public void start() {
        log.info("Running repeater scheduler");
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("message-repeater-thread-%d")
                .build();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
        scheduledExecutorService.scheduleAtFixedRate(this::sendRandomMessage, 60L, timeSec, TimeUnit.SECONDS);
    }

    private void sendRandomMessage() {
        if(on){
            final ImmutableList<String> messageCopy = ImmutableList.copyOf(messages);
            Integer indexOfMessage = randomNumberGenerator.nextInt(messageCopy.size());
            String messageToSend = messageCopy.get(indexOfMessage);
            log.info("Sending repeated message: {}", messageToSend);
            messageSender.sendMessage(messageToSend);
        } else {
            log.info("Not sending message as repeater is off.");
        }
    }

    public void setFrequency(int freq) {
        this.timeSec = freq;
    }

    public void toggleState() {
        if (on) {
            on = false;
        } else {
            on = true;
        }
    }

    public void clearAll() {
        log.info("Removing all {} messages.", messages.size());
        messages = new ImmutableList.Builder<String>().build();
        messageSender.sendMessage("All messages removed.");
        log.debug("Removed all messages successfully.", messages.size());
    }

    public void clearLast() {
        log.info("Removing last message.");
        if (messages.size() == 0) {
                clearAll();
        } else {
            messages = messages.subList(0, messages.size()-1);
            messageSender.sendMessage("Last Message Removed");
        }
    }

    public void addMessage(String newMessage) {
        messages = new ImmutableList.Builder<String>().addAll(messages).add(newMessage).build();
        messageSender.sendMessage("Example of Message:");
        messageSender.sendMessage(newMessage);
    }
}
