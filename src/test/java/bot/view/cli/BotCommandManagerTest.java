package bot.view.cli;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dominic Hauton on 27/08/2016.
 * <p>
 * Checks the adapter to ensure that the commands are valid
 */
class BotCommandManagerTest {

  @Test
  void splitCommandSimpleTest() throws Exception {
    String exampleCommand = "This is an example";
    List<String> expectedChunks = Arrays.asList("This", "is", "an", "example");
    List<String> actualChunks = BotCommandManager.splitCommand(exampleCommand);
    Assertions.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  void splitCommandEmptyTest() throws Exception {
    String exampleCommand = "";
    List<String> expectedChunks = new ArrayList<>();
    List<String> actualChunks = BotCommandManager.splitCommand(exampleCommand);
    Assertions.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  void splitCommandWhitespaceTest() throws Exception {
    String exampleCommand = "  ";
    List<String> expectedChunks = new ArrayList<>();
    List<String> actualChunks = BotCommandManager.splitCommand(exampleCommand);
    Assertions.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  void splitCommandMultiWhitespaceTest() throws Exception {
    String exampleCommand = "This   is an  example";
    List<String> expectedChunks = Arrays.asList("This", "is", "an"
        + "", "example");
    List<String> actualChunks = BotCommandManager.splitCommand(exampleCommand);
    Assertions.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  void splitCommandSimpleQuotesTest() throws Exception {
    String exampleCommand = "This \"is an\"  example";
    List<String> expectedChunks = Arrays.asList("This", "is an", "example");
    List<String> actualChunks = BotCommandManager.splitCommand(exampleCommand);
    Assertions.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  void splitCommandEscapedQuotesTest() throws Exception {
    String exampleCommand = "This \"is\\\" an\"  example";
    List<String> expectedChunks = Arrays.asList("This", "is\" an", "example");
    List<String> actualChunks = BotCommandManager.splitCommand(exampleCommand);
    Assertions.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  void splitCommandEscapedEscapeTest() throws Exception {
    String exampleCommand = "This \"is\\\\\" an\"  example";
    List<String> expectedChunks = Arrays.asList("This", "is\\", "an\"", "example");
    List<String> actualChunks = BotCommandManager.splitCommand(exampleCommand);
    Assertions.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  void splitCommandEmptyQuoteTest() throws Exception {
    String exampleCommand = "This \"\"  example";
    List<String> expectedChunks = Arrays.asList("This", "", "example");
    List<String> actualChunks = BotCommandManager.splitCommand(exampleCommand);
    Assertions.assertEquals(expectedChunks, actualChunks);
  }

  @Test
  void splitCommandFailedEscapeTest() throws Exception {
    String exampleCommand = "This \"\\a\"  example";
    List<String> expectedChunks = Arrays.asList("This", "\\a", "example");
    List<String> actualChunks = BotCommandManager.splitCommand(exampleCommand);
    Assertions.assertEquals(expectedChunks, actualChunks);
  }
}