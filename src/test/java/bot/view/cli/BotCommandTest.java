package bot.view.cli;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dominic Hauton on 27/08/2016.
 * <p>
 * Checks the adapter to ensure that the commands are valid
 */
public class BotCommandTest {

  @Test
  public void splitCommandSimpleTest() throws Exception {
    String exampleCommand = "This is an example";
    List<String> expectedChunks = Arrays.asList("This", "is", "an", "example");
    List<String> actualChunks = BotCommand.splitCommand(exampleCommand);
    Assert.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  public void splitCommandEmptyTest() throws Exception {
    String exampleCommand = "";
    List<String> expectedChunks = new ArrayList<>();
    List<String> actualChunks = BotCommand.splitCommand(exampleCommand);
    Assert.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  public void splitCommandWhitespaceTest() throws Exception {
    String exampleCommand = "  ";
    List<String> expectedChunks = new ArrayList<>();
    List<String> actualChunks = BotCommand.splitCommand(exampleCommand);
    Assert.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  public void splitCommandMultiWhitespaceTest() throws Exception {
    String exampleCommand = "This   is an  example";
    List<String> expectedChunks = Arrays.asList("This", "is", "an"
        + "", "example");
    List<String> actualChunks = BotCommand.splitCommand(exampleCommand);
    Assert.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  public void splitCommandSimpleQuotesTest() throws Exception {
    String exampleCommand = "This \"is an\"  example";
    List<String> expectedChunks = Arrays.asList("This", "is an", "example");
    List<String> actualChunks = BotCommand.splitCommand(exampleCommand);
    Assert.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  public void splitCommandEscapedQuotesTest() throws Exception {
    String exampleCommand = "This \"is\\\" an\"  example";
    List<String> expectedChunks = Arrays.asList("This", "is\" an", "example");
    List<String> actualChunks = BotCommand.splitCommand(exampleCommand);
    Assert.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  public void splitCommandEscapedEscapeTest() throws Exception {
    String exampleCommand = "This \"is\\\\\" an\"  example";
    List<String> expectedChunks = Arrays.asList("This", "is\\", "an\"", "example");
    List<String> actualChunks = BotCommand.splitCommand(exampleCommand);
    Assert.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  public void splitCommandEmptyQuoteTest() throws Exception {
    String exampleCommand = "This \"\"  example";
    List<String> expectedChunks = Arrays.asList("This", "", "example");
    List<String> actualChunks = BotCommand.splitCommand(exampleCommand);
    Assert.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  public void splitCommandFailedEscapeTest() throws Exception {
    String exampleCommand = "This \"\\a\"  example";
    List<String> expectedChunks = Arrays.asList("This", "\\a", "example");
    List<String> actualChunks = BotCommand.splitCommand(exampleCommand);
    Assert.assertEquals(expectedChunks, actualChunks);
  }
}