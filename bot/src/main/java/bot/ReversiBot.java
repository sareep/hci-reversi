package bot;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ReversiBot {

	String[] botNames = new String[] { "Bill", "Jean", "Jaime", "Harry", "Ashley", "Christy" };

	Socket socket = null;
	public String name = "";
	public String my_color = "";

	public ReversiBot() throws URISyntaxException {
		// TODO: figure this socket thing out
		this.socket = IO.socket("https://localhost:8080");
		this.name = "Bot_" + botNames[(int) (Math.random() * (botNames.length))];
	}

	public ReversiBot(String name, String socket) throws URISyntaxException {
		this.socket = IO.socket(socket);
		this.name = name;

	}

	public void setupSocket() {
		this.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				socket.emit("foo", "hi");
				socket.disconnect();
			}

		}).on("game_update", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				JSONObject payload = (JSONObject) args[0];

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
				if (socket.id = black.get("socket")) {// TODO this id is bad, find how to do in java
					my_color = "black";
				} else if (socket.id = white.get("socket")) {// TODO socket id here too
					my_color = "white";
				} else {
					System.err.println("Something weird happened with colors");
					System.exit(1);
				}

				// don't do anything unless it's your turn
				if (my_color == game.get("whose_turn")) {
					return;
				}

				
				// store all legal moves
				HashMap<Integer, HashSet<Integer>> legal_moves = new HashMap<Integer, HashSet<Integer>>(); 

				String[][] legalMoveBoard = (String[][]) game.get("legal_moves");
				for (int i = 0; i < 8; i++) {
					for (int j = 0; j < 8; j++) {
						if(legalMoveBoard[i][j] == my_color.substring(0,1)){
							if(legal_moves.get(i) != null){
								legal_moves.get(i).add(j);
							}else{
								legal_moves.put(i, new HashSet<Integer>(Arrays.asList(j)));
							}
						}
					}
				}

				// TODO implement easy (random), medium (short time to think), hard (longer time/better AI)

				// Find move with best score
				Integer[] best_move = new Integer[]{-1,-1,0};//[row, col, score];
				for(Map.Entry<Integer,HashSet<Integer>> entry : legal_moves.entrySet()){
					Integer row = entry.getKey();
					for(Integer col : entry.getValue()){
						Integer score = calculateUtility(board, row, col);
						if(score > best_move[2]){
							best_move = new Integer[]{row,col,score};
						}
					}
				}

				// Play the best move!
				JSONObject move = new JSONObject(){};
				move.put("row", best_move[0]);
				move.put("column", best_move[1]);
				move.put("color", my_color);

				socket.emit("play_token", move);
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
	 * @param board
	 * @param row
	 * @param col
	 * @return
	 */
	public Integer calculateUtility(String[][] board, Integer row, Integer col){
		Integer score = 0;
		// TODO write all this; use snippet that calculates legal moves
		// Things to evalute: how many it will flip over now, how many it could flip in the future, could end game (with a win), is corner
		return score;
	}

	public static void main(String[] args) {

		ReversiBot bot = null;
		try {
			if (args.length == 2) {
				bot = new ReversiBot(args[0], args[1]);
			} else if (args.length == 0) {
				bot = new ReversiBot();
			} else {
				System.err.println("Expected either 0 args or <bot_name> <socket>");
				System.exit(1);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		bot.setupSocket();
		// bot.run();
	}

	// public void run() {
	// 	setupSocket();
	// 	System.out.println("bot name: " + this.name);
	// 	System.out.println("bot socket: " + this.socket);

		// Boolean gameover = false;

		// while(!gameover){
		// //consume board
		// //determine best move
		// //send move to server
		// }
	// }

}
