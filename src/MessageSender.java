import java.util.LinkedList;

public class MessageSender implements Runnable {

	private int messageLimit = 100;
	private int messageTime = 31;

	private LinkedList<String> messageList;
	private LinkedList<Integer> timeList;
	private MyBot bot;
	private String channel;

	public MessageSender(MyBot bot, String channel) {
		this.bot = bot;
		this.channel = channel;
		messageList = new LinkedList<>();
		timeList = new LinkedList<>();
	}

	@Override
	public void run() {
		while (true) {
			if (!messageList.isEmpty()) {
				while (!timeList.isEmpty() && timeList.getFirst() < ((int) (System.currentTimeMillis() / 1000) - messageTime)) {
					timeList.removeFirst();
				}

				while ((timeList.size() < messageLimit)
						&& (!messageList.isEmpty())) {
					bot.sendMessage(channel, messageList.removeFirst());
					timeList.addLast((int) (System.currentTimeMillis() / 1000));
				}

				try {
					Thread.sleep(35);
				} catch (InterruptedException ex) {
					System.err.println("Thread can/'t sleep.");
				}
			} else {
				try {
					Thread.sleep(250);
				} catch (InterruptedException ex) {
					System.err.println("Thread can/'t sleep.");
				}
			}
		}
	}

	public void sendMessage(String message) {
		if (timeList.size() < messageLimit) {
			bot.sendMessage(channel, message);
			timeList.addLast((int) (System.currentTimeMillis() / 1000));
		} else {
			if (!messageList.contains(message)) {
				messageList.addLast(message);
				System.out.println("Current message queue: "
						+ messageList.size());
			} else{
				System.out.println("Not sending repeated message.");
			}
		}
	}

}
