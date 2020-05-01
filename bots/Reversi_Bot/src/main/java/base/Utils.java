package base;

/**
 * Utils
 */
public class Utils {

    /**** Constants ****/
    public static final String PATH_TO_EPISODES = "C:/Users/reeps/Documents/Westmont/Classwork/Fall 2019/HCIT/reversi/bots/learning_episodes/"
            + Reversi_Bot.aiType + "/" + Reversi_Bot.difficulty + "/";

    /**** Print shortcuts ****/

    public static void out(String message) {
        print(message, "out");
    }

    public static void err(String message) {
        print(message, "err");
    }

    /**
     * Title each message for easier debugging on server
     * 
     * @param message
     * @param type
     */
    public static void print(String message, String type) {
        String output = "**" + Reversi_Bot.username + ": ";
        if (Reversi_Bot.port == 8080) {
            message += "\n";
        }

        switch (type) {
            case "out":
                output += message;
                System.out.print(output);
                break;
            case "err":
                output += "ERROR: ";
                output += message;
                System.err.print(output);
                break;
            default:
                break;
        }
    }

}