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

public class Bot {

	public Socket socket = null;
	public String name = "";
	public String my_color = "";
	public String opponent_color = "";
	public String difficulty = ""; // easy, medium, hard


	/********* CONSTRUCTORS *********/
	
	/**
	 * 
	 * @throws URISyntaxException
	 */
	public Bot() throws URISyntaxException {
		// TODO: figure this socket thing out
		this.socket = IO.socket("https://localhost:8080");
		this.name = "Bot_" + Manager.botNames[(int) (Math.random() * (Manager.botNames.length))];
		this.difficulty = "easy";
	}

	/**
	 * 
	 * @param name
	 * @param socket
	 * @param difficulty
	 * @throws URISyntaxException
	 */
	public Bot(String name, String socket, String difficulty) throws URISyntaxException {
		this.socket = IO.socket(socket);
		this.name = name;
		this.difficulty = difficulty;
	}


	/********* LOGIC FUNCTIONS *********/
	
	/**
	 * 
	 */
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
				Turn newTurn = prepareTurn((JSONObject) args[0]);

				// don't do anything unless it's your turn
				if(!newTurn.isMyMove()){
					return;
				}

				// TODO remove this
				
				// store all legal moves
				// HashMap<Integer, HashSet<Integer>> legal_moves = new HashMap<Integer, HashSet<Integer>>(); 
				// String[][] legalMoveBoard = (String[][]) game.get("legal_moves");
				// for (int i = 0; i < 8; i++) {
				// 	for (int j = 0; j < 8; j++) {
				// 		if(legalMoveBoard[i][j] == my_color.substring(0,1)){
				// 			if(legal_moves.get(i) != null){
				// 				legal_moves.get(i).add(j);
				// 			}else{
				// 				legal_moves.put(i, new HashSet<Integer>(Arrays.asList(j)));
				// 			}
				// 		}
				// 	}
				// }
				// end removal

				
				// TODO implement easy (random), medium (short time to think), hard (longer time/better AI)
				


				// // Find move with best score
				// Integer[] best_move = new Integer[]{-1,-1,0};//[row, col, score];
				// for(Map.Entry<Integer,HashSet<Integer>> entry : legal_moves.entrySet()){
				// 	Integer row = entry.getKey();
				// 	for(Integer col : entry.getValue()){
				// 		Integer score = calculateUtility(board, row, col);
				// 		if(score > best_move[2]){
				// 			best_move = new Integer[]{row,col,score};
				// 		}
				// 	}
				// }

				Move best_move = newTurn.getMove(difficulty);
				
				// Play the best move!
				JSONObject move = new JSONObject(){};
				move.put("row", best_move.getRow());
				move.put("column", best_move.getCol());
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
	 * @param payload
	 * @return
	 */
	public Turn prepareTurn(JSONObject payload){
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
			opponent_color = "white";
		} else if (socket.id = white.get("socket")) {// TODO socket id here too
			my_color = "white";
			opponent_color = "black";
		} else {
			System.err.println("Something weird happened with colors");
			System.exit(1);
		}

		Boolean my_turn = (my_color == game.get("whose_turn"));
		//TODO move my turn logic to right here. either return turn or exit and handle an empty object

		return new Turn(my_turn, board, (String[][]) game.get("legal_moves"));
	}

}
