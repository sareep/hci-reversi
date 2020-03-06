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

package base;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Hello world!
 *
 */
public class Reversi_Bot {

	public static final int MAX_GAMES = 2;// TODO figure out why bot dies after 2 games
	public static final int MAX_JOIN_ATTEMPTS = 5;

	public static String aiType = null; // ab or rl

	public static int gamesPlayed = 0;
	public static int join_lobby_fail_count = 0;
	public static Instant first_invite_time = null;
	public static Instant latest_invite_time = null;

	public static Socket socket = null;
	public static int port = 8080;

	public static String room = "lobby";
	public static String username = "Bot";
	public static String trainee = null;
	public static String role = null;
	public static String difficulty = null;
	public static String my_color = null;
	public static String opp_color = null;

	/**
	 * Main method run on spawn from server
	 * 
	 * @param args [port, username, difficulty]
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws URISyntaxException {

		JSONObject payload = new JSONObject(args[0]);

		// Screen payload
		String[] req_keys = new String[] { "port", "ai_type", "username", "difficulty" };
		JSONObject eval = validatePayload(payload, req_keys);
		if (!eval.getString("result").equals("valid")) {
			// broadcast failure
			JSONObject response = new JSONObject();
			response.put("result", "fail");
			response.put("message", eval.getString("message"));
			socket.emit("spawn_bot_response", response);

			// log failure locally
			err(eval.getString("message"));
			err("Terminating");
			System.exit(3);
		}

		// Pull out info from payload
		port = payload.getInt("port");
		aiType = payload.getString("ai_type");
		username = payload.getString("username");
		difficulty = payload.getString("difficulty");
		/**role = payload.getString("role"); //TODO save for later? once figure out how
		training will work
		out("role is " + role);*/

		out("Assigned params");

		// set up socket functionality to interact with server
		socket = IO.socket("http://localhost:" + port);
		setupSocket();
	}
	
	
	/**
	 * Set up connection to server
	 */
	public static void setupSocket() {
		out("Setting up socket");
		
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
			
			/**
			 * Connect to the lobby for the first time
			 */
			@Override
			public void call(Object... args) {
				out("Connection success");
				joinRoom(room);
			}

		}).on("join_room_response", new Emitter.Listener() {

			/**
			 * Response from server after a request to join a room
			 */
			@Override
			public void call(Object... args) {
				JSONObject response = (JSONObject) args[0];
				switch (response.getString("result")) {

					// If failed, try again up to MAX_JOIN_ATTEMPTS times then terminate
					case "fail":
						err("didn't join room correctly, going to lobby");
						err(response.getString("message"));

						join_lobby_fail_count++;
						if (join_lobby_fail_count > MAX_JOIN_ATTEMPTS) {
							err("Failed to join lobby " + MAX_JOIN_ATTEMPTS + " times, terminating.");
							System.exit(1);
						} else {
							try {
								TimeUnit.SECONDS.sleep(2);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							joinRoom("lobby");
						}
						break;

					// If succeeded, reset failure count
					case "success":

						// screen payload
						JSONObject eval = validatePayload(response, new String[]{"username", "room"});
						if(!eval.getString("result").equals("valid")){
							err(eval.getString("message"));
							break;
						}

						if (username.equals(response.getString("username"))) {
							room = response.getString("room");
							out("Joined room " + room);
							join_lobby_fail_count = 0;
						}


						// TODO come back once training is set up
						// If role is to train AND room is lobby, invite the trainee to a game
						// if (role.equals("train") && trainee.equals(response.getString("username"))
						// && room.equals("lobby")) {
						// first_invite_time = Instant.now();
						// invite(trainee); // TODO debug this
						// }

						break;

					default:
						err("Malformed join_room response from server, exiting");
						System.exit(1);
						break;
				}
			}

		}).on("invite_response", new Emitter.Listener() {

			// Response from server after an invitation is sent
			@Override
			public void call(Object... args) {
				JSONObject response = (JSONObject) args[0];
				String result = response.getString("result");
				out("Invitation response: " + result);

				// If invitation failed, wait 10 seconds and try again for a maximum of 2
				// minutes
				if (result.equals("fail")) {
					err(response.getString("message"));

					if (role.equals("train")) {
						Instant now = Instant.now();

						// check that NOW is within the 2 minutes
						if (now.isBefore(first_invite_time.plusSeconds(120))) {

							// 10 second wait between invites
							if (now.isAfter(latest_invite_time.plusSeconds(10))) {
								invite(trainee);
							} else {
								try {
									TimeUnit.SECONDS.sleep(10);
								} catch (InterruptedException e) {
									err(e.getMessage());
									e.printStackTrace();
								}
								invite(trainee);
							}
						} else {
							err("Trainee could not be invited within 2 minutes, terminating");
							System.exit(1);
						}
					}
				}
			}

		}).on("invited", new Emitter.Listener() {

			// Accept all inviations to a game
			@Override
			public void call(Object... args) {
				JSONObject invitation = (JSONObject) args[0];

				if (invitation.getString("result").equals("fail")) {
					err(invitation.getString("message"));
				} else {

					JSONObject eval = validatePayload(invitation, new String[] { "socket_id", "socket_id" });
					if (!eval.getString("result").equals("valid")) {
						err(eval.getString("message"));
					} else {
						out("Invited by" + invitation.getString("socket_id"));
						JSONObject payload = new JSONObject();
						payload.put("requested_user", invitation.getString("socket_id"));
						socket.emit("game_start", payload);
					}
				}
			}

		}).on("game_start_response", new Emitter.Listener() {

			// Join a game room after a game is initialized on the server
			@Override
			public void call(Object... args) {
				out("Game start result: " + ((JSONObject) args[0]).getString("result"));
				JSONObject response = (JSONObject) args[0];
				if (response.getString("result").equals("fail")) {
					err(response.getString("message"));
				} else {
					socket.disconnect();
					room = response.getString("game_id");
					// joinRoom(room);
					socket.connect();
				}
			}

		}).on("player_disconnected", new Emitter.Listener() {

			// Rejoin the lobby if the opponent quits early
			@Override
			public void call(Object... args) {
				if (!room.equals("lobby")) {
					joinRoom("lobby");
				}
			}

		}).on("game_update", new Emitter.Listener() {

			// Recieve the opponent's play and send a move back
			@Override
			public void call(Object... args) {

				out("Received game update");
				JSONObject payload = (JSONObject) args[0];

				// screen payload
				JSONObject eval = validatePayload(payload, new String[] { "result", "game" });
				if (!eval.getString("result").equals("valid")) {
					err(eval.getString("message"));
				} else {

					GameState game = prepareTurn((JSONObject) args[0]);

					// don't do anything unless it's your turn
					if (!game.isMyTurn() || game.isOver()) {
						out("Not my turn, waiting patiently for the human");
					} else {
						// pick a move
						String[] move = decideMoveToPlay(game);

						// jsonify the move
						JSONObject play = new JSONObject() {
						};
						play.put("row", Integer.valueOf(move[0]));
						play.put("column", Integer.valueOf(move[1]));
						play.put("color", my_color);

						out("Playing token at " + move[0] + "," + move[1]);

						// send move to server
						socket.emit("play_token", play);
					}
				}

			}

		}).on("play_token_response", new Emitter.Listener() {

			// Check if play went through
			@Override
			public void call(Object... args) {
				JSONObject response = (JSONObject) args[0];
				String result = response.getString("result");
				out("Play token result: " + result);
				if (result.equals("fail")) {
					err(response.getString("message"));
				}
			}

		}).on("game_over", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				// Get winner
				JSONObject response = (JSONObject) args[0];
				out("Game over! Winner is " + response.getString("winner"));
				socket.disconnect();

				// Real bots rejoin lobby, trainers exit
				gamesPlayed++;
				// if (role.equals("train") && (MAX_GAMES <= gamesPlayed)) {
				// 	out("Training session over! Shutting down.");
				// 	System.exit(0);
				// }
				room = "lobby";
				// joinRoom(room);
				socket.connect();
			}

		}).on("terminate", new Emitter.Listener() {

			// Server's order to end the process
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


	/**** In-Game Processes ****/

	/**
	 * Run checks on game update payload, then put important information into a
	 * GameState
	 * 
	 * @param payload
	 * @return
	 */
	public static GameState prepareTurn(JSONObject payload) {
		// Check for good update
		if (payload.getString("result").equals("fail")) {
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

	/**
	 * 
	 * @param game
	 * @return
	 */
	public static String[] decideMoveToPlay(GameState game) {

		switch (aiType) {
			// TODO call appropriate thinking

			// case "ab":
			// break;

			// case "rl":
			// break;

			default:
				return game.getRandomMove().split(",");
		}

	}


	/**** General Functions ****/

	/**
	 * Validates that the values to specified keys are not null in a JSONObject
	 * 
	 * @param payload
	 * @param keysToCheck
	 * @return
	 */
	public static JSONObject validatePayload(JSONObject payload, String[] keysToCheck) {
		JSONObject eval = new JSONObject();
		Boolean valid = true;
		String message = "";

		for (String s : keysToCheck) {
			if (payload.get(s) == null) {
				if (valid) {
					valid = false;
					message = "Payload was missing item(s): ";
				}
				message += s + " ";
			}
		}

		if (valid) {
			eval.put("result", "valid");
		} else {
			eval.put("result", "invalid");
			eval.put("message", message);
		}

		return eval;
	}

	/**
	 * Create and emit join_room request
	 * 
	 * @param room_to_join
	 */
	public static void joinRoom(String room_to_join) {
		JSONObject payload = new JSONObject();
		payload.put("room", room_to_join);
		payload.put("username", username);
		payload.put("is_bot", true);
		socket.emit("join_room", payload);
	}

	/**
	 * Invite a player to a game (usually bot-to-bot)
	 * @param trainee
	 */
	public static void invite(String trainee) {
		JSONObject payload = new JSONObject();
		payload.put("requested_user", trainee);
		socket.emit("invite", payload);
		latest_invite_time = Instant.now();
	}


	/**** Print shortcuts ****/
	
	public static void out(String message) {
		print(message, "out");
	}
	
	public static void err(String message) {
		print(message, "err");
	}
	
	/**
	 * Title each message for easier debugging on server
	 * @param message
	 * @param type
	 */
	public static void print(String message, String type) {
		String output = "**" + username + ": ";
		if (port == 8080) {
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
