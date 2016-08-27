package bot;

import org.junit.Before;
import org.mockito.Mockito;

import bot.channel.TwitchUser;
import bot.util.BitlyDecorator;
import twitch.chat.sender.TwitchMessageRouter;

/**
 * Created by Dominic Hauton on 02/05/2016.
 *
 * A test for all of the BotController functions
 */
public class BotControllerTest {

  private static final String twitchChannel1 = "channel1";
  private static final String user1 = "user1";
  private static final TwitchUser twitchUser1 = new TwitchUser(user1);
  private static final String messagePayload1 = "messagePayload1";

  private BotController botController;
  private BitlyDecorator bitlyDecoratorMock;
  private TwitchMessageRouter twitchMessageRouterMock;
  private MessageRepeater messageRepeaterMock;

  @Before
  public void setUp() {
    twitchMessageRouterMock = Mockito.mock(TwitchMessageRouter.class);
    bitlyDecoratorMock = Mockito.mock(BitlyDecorator.class);
    messageRepeaterMock = Mockito.mock(MessageRepeater.class);
    botController = new BotController(bitlyDecoratorMock, twitchMessageRouterMock, messageRepeaterMock, twitchChannel1);
  }
}