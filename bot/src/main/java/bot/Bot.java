package bot;

import java.net.URISyntaxException;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Bot {

	public static Socket socket = null;
	public static String name = "";
	public static String my_color = "";
	public static String opponent_color = "";
	public static String difficulty = ""; // easy, medium, hard
	static Move lastMove;


	/********* CONSTRUCTORS *********/
	
	// /**
	//  * 
	//  * @throws URISyntaxException
	//  */
	// public Bot() throws URISyntaxException {
	// 	Bot.socket = IO.socket("https://localhost:8080");
	// 	Bot.name = "Bot_" + Utils.botNames[(int) (Math.random() * (Utils.botNames.length))];
	// 	Bot.difficulty = "easy";
	// }

	// /**
	//  * 
	//  * @param name
	//  * @param socket
	//  * @param difficulty
	//  * @throws URISyntaxException
	//  */
	// public Bot(String name, String socket, String difficulty) throws URISyntaxException {
	// 	Bot.socket = IO.socket(socket);
	// 	Bot.name = name;
	// 	Bot.difficulty = difficulty;
	// }


	/********* LOGIC FUNCTIONS *********/
	
	/**
	 * 
	 */
	public static void setupSocket() {
		Bot.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				socket.emit("foo", "hi");
				socket.disconnect();
			}

		}).on("game_update", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				Turn newTurn = prepareTurn((JSONObject) args[0]);

				// don't do anything unless it's your turn
				if(!newTurn.isMyTurn()){
					return;
				}

				// TODO update lastMove to be opponent's move
				Move best_move = newTurn.getMove(difficulty);
				
				// Play the best move!
				JSONObject move = new JSONObject(){};
				move.put("row", best_move.getRow());
				move.put("column", best_move.getCol());
				move.put("color", my_color);

				socket.emit("play_token", move);
				lastMove = best_move;
			}

		}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... args) {
			}

		});
		socket.connect();
	}

	/**
	 * 
	 * @param payload
	 * @return
	 */
	public static Turn prepareTurn(JSONObject payload){
		// Check for good update
		if (payload.get("result") == "fail") {
			System.err.println(payload.get("message"));
			System.exit(1);
		}
		JSONObject game = (JSONObject) payload.get("game");

		// Check for good board
		String[][] board = (String[][]) game.get("board");
		if (board == null) {
			System.err.println("Internal server error: received malformed board update");
			System.exit(1);
		}

		// assign colors
		JSONObject white = (JSONObject) game.get("player_white");
		JSONObject black = (JSONObject) game.get("player_black");
		if (socket.id() == black.get("socket")) {
			my_color = "black";
			opponent_color = "white";
		} else if (socket.id() == white.get("socket")) {
			my_color = "white";
			opponent_color = "black";
		} else {
			System.err.println("Something weird happened with colors");
			System.exit(1);
		}

		Boolean is_my_turn = (my_color == game.get("whose_turn"));

		return new Turn(is_my_turn, board, (String[][]) game.get("legal_moves"));
	}

	public static void main(String[] args) {

		try {
			if (args.length == 3) {
				name = args[0];
				Bot.socket = IO.socket(args[1]);
				Bot.difficulty = args[2];
			} else if (args.length == 0) {
				Bot.name = "Bot_" + Utils.botNames[(int) (Math.random() * (Utils.botNames.length))];
				Bot.socket = IO.socket("https://localhost:8080");
				Bot.difficulty = "easy";
			} else {
				System.err.println("Expected either 0 args or <bot_name> <socket>");
				System.exit(1);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		setupSocket();
	}
}
