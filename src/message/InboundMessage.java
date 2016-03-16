package message;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Dominic H on 09/08/2015 at 22:56.
 *
 * A container for all information stored alongside inbound message;
 */
public class InboundMessage {
    private String messageContent;
    private String owner;
    private LocalDateTime rcvTime;
    private Map<String, Long> wordFrequency;
    private String normalizedPayload;
    private Set<Penalty> penaltySet;

    public InboundMessage(String messageContent, String owner) {
        this.messageContent = messageContent;
        this.owner = owner;
        rcvTime = LocalDateTime.now();
        penaltySet = new HashSet<>();
        enrichMessage();
    }

    public void enrichMessage(){
        normalizedPayload = normalizeSpaces(removePunctuation(messageContent));
        wordFrequency = generateWordMap(normalizedPayload, " ");
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getOwner() {
        return owner;
    }

    public LocalDateTime getRcvTime() {
        return rcvTime;
    }

    public Set<Penalty> getPenaltySet() {
        return penaltySet;
    }

    public Integer getTotalPenaltyTime() {
        return penaltySet.stream()
                .mapToInt(Penalty::getPenalty)
                .sum();
    }

    public void addPenalty(Penalty penalty){
        penaltySet.add(penalty);
    }

    public Map<String, Long> getWordFrequency() {
        return wordFrequency;
    }

    public String getNormalizedPayload() {
        return normalizedPayload;
    }

    protected Map<String, Long> generateWordMap(String message, String delimiter){
        return Arrays.stream(message.split(delimiter))
                .collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()));
    }

    protected String removePunctuation(String input){
        return input.replaceAll("[\\p{P}\\p{S}-[._]]", "");
    }

    protected String normalizeSpaces(String input){
        return input.replaceAll(" +", " ").replaceAll("^ +| +$", "");
    }
}
