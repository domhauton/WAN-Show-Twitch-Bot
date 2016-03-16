package parsing;

import message.MessageTree;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Dominic H on 30/08/2015 at 17:58.
 *
 * Test of Message Tree
 */
public class MessageTreeTest {
    @Test
    public void testMaxCount(){
        String testString = "HellomyHelloName is hello";
        MessageTree messageTree = new MessageTree(testString);
        Integer actualCount = messageTree.getMaxCount(5);
        long startTime = System.currentTimeMillis();
        System.out.printf("Parse Time: %dms\n", System.currentTimeMillis()-startTime);
        Integer expectedCount = 3;
        Assert.assertEquals(expectedCount, actualCount);
    }
}
