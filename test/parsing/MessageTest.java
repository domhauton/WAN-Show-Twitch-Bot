package parsing;

import message.InboundMessage;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dominic H on 12/08/2015 at 22:28.
 *
 * Tests for message enricher
 */
public class MessageTest extends InboundMessage {

    public MessageTest() {
        super("          hello   hello my    my name name name is test 1.�$(*^(!*", "dummyOwner");
        enrichMessage();
    }

    @Test
    public void testGenerateWordMap() throws Exception {
        HashMap<String, Long> expected = new HashMap<>();
        expected.put("hello", 2l);
        expected.put("my", 2l);
        expected.put("name", 3l);
        expected.put("is", 1l);
        expected.put("test", 1l);
        expected.put("1", 1l);
        Map<String, Long> actual = getWordFrequency();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testRemovePunctuationAndNormalize() throws Exception {
        String expected = "hello hello my my name name name is test 1";
        Assert.assertEquals(expected, getNormalizedPayload());
    }
}