/**
 * A bot to play Reversi using Reinforcement Learning.
 * Author: Sam Reep, Westmont College
 * 
 * 
 * Lifespan:
 * 		1. [ ] Spawned by server, given Port (and Username?)
 * 					var child = require('child_process').spawn(
 * 						'java', ['-jar', 'RL_Bot.jar', 'argument to pass in']
 *					);
 * 		2. [X] Lives in lobby until invited & joins game
 * 		3. [X] Plays game to completion
 * 		4. [X] Rejoins lobby ||  [ ] Dies and new instance is created?? if can store results & learning
 * 		5. loop
 */

package reinforcement_learning.bot;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Hello world!
 *
 */
public class RL_Bot {

	private static Socket socket = null;
	private static String room = null;
	private static String username = null;
	private static String port = null;
	private static int join_lobby_fail_count = 0;
	static String difficulty = null;
	static String my_color = null;
	static String opp_color = null;

	/**
	 * Main method run on spawn from server
	 * 
	 * @param args [port, username, difficulty]
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws URISyntaxException {
		
		//Defaults
		room = "lobby";
		username = "BotRL";
		port = "8080";

		String[] args_split = new String[]{};
		if(args.length > 0){
			out("args0: " + args[0]);
			args_split = args[0].split(",");
		}

		if (args_split.length == 3) {
			port = args_split[0];
			username = args_split[1];
			difficulty = args_split[2];
		} else if (args.length == 0) {
			username = "BotRL_Easy";
			difficulty = "easy";
		} else {
			err("Recieved " + args_split.length
					+ " args: " + args[0] 
					+ ". Expected either 0 args or <port> <username> <difficulty>");
			System.exit(1);
		}

		
		out("Assigned params");
		socket = IO.socket("http://localhost:" + port);
		setupSocket();
	}

	public static void joinRoom(String room_to_join) {
		JSONObject payload = new JSONObject();
		payload.put("room", room_to_join);
		payload.put("username", username);
		payload.put("is_bot", true);
		socket.emit("join_room", payload);
	}

	/**
	 * 
	 * @param payload
	 * @return
	 */
	public static GameState prepareTurn(JSONObject payload) {
		// Check for good update
		if (payload.getString("result") == "fail") {
			err(payload.getString("message"));
			System.exit(1);
		}
		JSONObject game = (JSONObject) payload.get("game");

		// Check for good board
		JSONArray board = (JSONArray) game.get("board");
		if (board == null) {
			err("Internal server error: received malformed board update");
			System.exit(1);
		}

		// assign colors
		JSONObject white = (JSONObject) game.get("player_white");
		JSONObject black = (JSONObject) game.get("player_black");
		if (socket.id().equals(black.getString("socket"))) {
			my_color = "black";
			opp_color = "white";
		} else if (socket.id().equals(white.getString("socket"))) {
			my_color = "white";
			opp_color = "black";
		} else {
			err("Something weird happened with colors");
			System.exit(1);
		}

		Boolean is_my_turn = (my_color.equals(game.getString("whose_turn")));

		return new GameState(is_my_turn, (JSONArray) game.get("legal_moves"));
	}

	public static void setupSocket() {
		out("Setting up socket");

		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				out("Connection success");
				joinRoom(room);
			}

		}).on("join_room_response", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				JSONObject response = (JSONObject) args[0];
				switch (response.getString("result")) {
					case "fail":
						err("didn't join room correctly, going to lobby");
						err(response.getString("message"));

						join_lobby_fail_count++;
						if (join_lobby_fail_count > 5) {
							err("Failed to join lobby 10 times, terminating.");
							System.exit(1);
						} else {
							joinRoom("lobby");
						}
						break;

					case "success":
						if (username.equals(response.getString("username"))) {
							room = response.getString("room");
							out("Joined room " + room);
							join_lobby_fail_count = 0;
						}
						break;

					default:
						err("Malformed join_room response from server, exiting");
						System.exit(1);
						break;
				}
			}

		}).on("invited", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				JSONObject invitation = (JSONObject) args[0];
				out("Invited by" + invitation.getString("socket_id"));
				if (invitation.getString("result") == "fail") {
					err(invitation.getString("message"));
				} else {
					JSONObject payload = new JSONObject();
					payload.put("requested_user", invitation.getString("socket_id"));
					socket.emit("game_start", payload);
				}
			}

		}).on("game_start_response", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				out("Game start result: " + ((JSONObject) args[0]).getString("result"));
				JSONObject response = (JSONObject) args[0];
				if (response.getString("result") == "fail") {
					err(response.getString("message"));
				} else {
					socket.disconnect();
					room = response.getString("game_id");
					socket.connect();
				}
			}

		}).on("game_update", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				out("Game update result: " + ((JSONObject) args[0]).getString("result"));

				GameState game = prepareTurn((JSONObject) args[0]);

				// don't do anything unless it's your turn
				if (!game.isMyTurn() || game.isOver()) {
					out("Not my turn, waiting patiently");
				} else {
					String[] move = game.getRandomMove().split(",");

					// Play the best move!
					JSONObject play = new JSONObject() {
					};
					play.put("row", Integer.valueOf(move[0]));
					play.put("column", Integer.valueOf(move[1]));
					play.put("color", my_color);

					out("Playing token at " + move[0] + "," + move[1]);

					socket.emit("play_token", play);
				}

			}

		}).on("play_token_response", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				JSONObject response = (JSONObject) args[0];
				out("Play token result: " + response.getString("result"));
			}

		}).on("game_over", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				JSONObject response = (JSONObject) args[0];
				out("Game over! Winner is " + response.getString("winner"));
				socket.disconnect();
				room = "lobby";
				socket.connect();
			}

		}).on("terminate", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				JSONObject response = (JSONObject) args[0];
				out("Exiting. Terminated by " + response.getString("terminator"));
				System.exit(0);
			}

		}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				out("Disconnected");
			}

		});

		out("Connecting to socket");
		socket.connect();
	}

	/** Print shortcuts, titles each message for easier debugging on server **/
	public static void out(String message) {
		print(message, "out");
	}

	public static void err(String message) {
		print(message, "err");
	}

	public static void print(String message, String type) {
		String output = "**"+username+": ";
		if(port.equals("8080")){
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
