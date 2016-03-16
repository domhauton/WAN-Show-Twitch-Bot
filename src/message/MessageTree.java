package message;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Created by Dominic H on 30/08/2015 at 17:05.
 *
 * Uses a tree to find repeating phrases within a message.
 */
public class MessageTree {
    private HashMap<String, Integer> stringMap;
    private String inputMessage;

    public MessageTree(String inputMessage) {
        stringMap = new HashMap<>(inputMessage.length()*inputMessage.length());
        this.inputMessage = inputMessage.toLowerCase();
        parseMessage();
    }

    /**
     * Max occurrences of string above given length
     * @param length (inclusive) length of string
     * @return Number of max occurrences.
     */
    public Integer getMaxCount(Integer length){
        return stringMap.entrySet().stream()
                .filter(entry -> entry.getKey().length() >= length)
                .mapToInt(Map.Entry::getValue)
                .max()
                .orElse(0);
    }

    /**
     * Finds the longest string with a given number of occurrences or more.
     * @param occurrences Number of occurrences string must have.
     * @return Longest string with given occurrences or more.
     */
    public String getLongestRepeated(Integer occurrences){
        return stringMap.entrySet().stream()
                .filter(entry -> entry.getValue() >= occurrences)
                .reduce((entry1, entry2) -> entry1.getKey().length() >= entry2.getKey().length() ? entry1 : entry2)
                .map(Map.Entry::getKey)
                .orElse("");
    }

    private void parseMessage(){
        IntStream.range(0, inputMessage.length())
                .forEach(index -> parseMessageFragment(inputMessage.substring(index, inputMessage.length())));
    }

    /**
     * Put the message into a map.
     */
    private void parseMessageFragment(String message) {
        IntStream.range(1, message.length()+1)
                .forEach(index -> addToMap(message.substring(0, index)));
    }

    /**
     * Adds to the map by incrementing
     * @param fragment fragment to add
     */
    private void addToMap(String fragment){
        Integer oldValue = stringMap.getOrDefault(fragment, 0);
        oldValue += 1;
        stringMap.put(fragment, oldValue);
    }
}
