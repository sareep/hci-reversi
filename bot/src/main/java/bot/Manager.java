package bot;

import java.net.URISyntaxException;

/**
 * BotManager
 */
public class Manager {

	public static String[] botNames = new String[] { "Bill", "Jean", "Jaime", "Harry", "Ashley", "Christy" };

	// TODO i think this should be just put in the bot
    public static void main(String[] args) {

		Bot bot = null;
		try {
			if (args.length == 3) {
				bot = new Bot(args[0], args[1], args[2]);
			} else if (args.length == 0) {
				bot = new Bot();
			} else {
				System.err.println("Expected either 0 args or <bot_name> <socket>");
				System.exit(1);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		bot.setupSocket();
	}
}