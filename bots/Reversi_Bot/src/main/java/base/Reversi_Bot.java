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

import base.reinforcement_learning.Learner;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.client.IO.Options;
import io.socket.emitter.Emitter;

/**
 * Hello world!
 *
 */
public class Reversi_Bot {

	/** Constants **/
	public static final int MAX_GAMES = 2;
	public static final int MAX_JOIN_ATTEMPTS = 5;

	/** Server Fields **/
	public static int reconnect_count = 0;
	public static int join_lobby_fail_count = 0;
	public static Instant first_invite_time = null;
	public static Instant latest_invite_time = null;
	public static Socket socket = null;
	public static int port = 8080;
	public static String room = "lobby";

	/** Bot Parameters **/
	public static String username = "Bot";
	public static String aiType = null; // ab or rl
	public static String difficulty = "easy";
	public static String role = "learn";
	public static String my_color = "black";
	public static String my_token = "b";
	public static int gamesPlayed = 0;

	/** Opponent Info **/
	public static String opponent_bot = null;
	public static String opp_color = "white";
	public static String opp_token = "w";

	/**
	 * Main method run on spawn from server
	 * 
	 * @param args [port, username, difficulty]
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws URISyntaxException {
		Learner.run(difficulty);
		System.exit(0);

		JSONObject payload = new JSONObject(args[0]);

		// Screen payload
		String[] req_keys = new String[] { "port", "ai_type", "username", "difficulty", "role" };
		JSONObject eval = validatePayload(payload, req_keys);

		// deal with bad payload
		if (!eval.getString("result").equals("valid")) {
			// broadcast failure
			JSONObject response = new JSONObject();
			response.put("result", "fail");
			response.put("message", eval.getString("message"));
			socket.emit("spawn_bot_response", response);

			// log failure locally
			Utils.err(eval.getString("message"));
			Utils.err("Terminating");
			System.exit(3);
		}

		// Extract info from payload
		port = payload.getInt("port");
		aiType = payload.getString("ai_type");
		username = payload.getString("username");
		difficulty = payload.getString("difficulty");
		role = payload.getString("role");
		opponent_bot = role.equals("teach") ? username.replace("_teacher", "") : null;

		Utils.out("Assigned params");

		// Set up socket functionality to interact with server
		Options opts = new Options();
		opts.forceNew = true;
		socket = IO.socket("http://localhost:" + port);

		if (role.equals("learn")) {
			Learner.run(difficulty);
			System.exit(0);
		} else {
			setupSocket();
		}
	}

	/**
	 * Set up connection to server
	 */
	public static void setupSocket() {
		Utils.out("Setting up socket");

		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

			/**
			 * Connect to a room
			 */
			@Override
			public void call(Object... args) {
				Utils.out("Connection success");
				joinRoom(room);
			}

		}).on("join_room_response", new Emitter.Listener() {

			/**
			 * Response from server after a request to join a room
			 */
			@Override
			public void call(Object... args) {
				JSONObject response = (JSONObject) args[0];
				// screen payload
				JSONObject eval = validatePayload(response, new String[] { "result", "username", "room" });
				if (!eval.getString("result").equals("valid")) {
					Utils.err(eval.getString("message"));
					return;
				}

				switch (response.getString("result")) {

					// If failed, try again up to MAX_JOIN_ATTEMPTS times then terminate
					case "fail":
						if (response.getString("username").equals(username)) {
							Utils.err("Didn't join room correctly, going to lobby");
							Utils.err(response.getString("message"));

							join_lobby_fail_count++;
							if (join_lobby_fail_count > MAX_JOIN_ATTEMPTS) {
								Utils.err("Failed to join lobby " + MAX_JOIN_ATTEMPTS + " times, terminating.");
								System.exit(1);
							} else {
								try {
									TimeUnit.SECONDS.sleep(2);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								joinRoom("lobby");
							}
						}
						break;

					// If succeeded, reset failure count
					case "success":

						if (username.equals(response.getString("username"))) {
							room = response.getString("room");
							Utils.out("Joined room " + room);
							join_lobby_fail_count = 0;
						}

						// If role is to initiate game AND room is lobby, invite the opponent
						if (opponent_bot != null && opponent_bot.equals(response.getString("username"))
								&& room.equals("lobby")) {
							invite(response.getString("socket_id")); // debug this
							first_invite_time = Instant.now();
						}

						break;

					default:
						Utils.err("Malformed join_room response from server, exiting");
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
				Utils.out("Invitation response: " + result);

				// If invitation failed, wait 10 sec and try again for up to 2 min
				if (result.equals("fail")) {
					Utils.err(response.getString("message"));

					if (role.equals("train")) {
						Instant now = Instant.now();

						// check that NOW is within the 2 minutes
						if (now.isBefore(first_invite_time.plusSeconds(120))) {

							// 10 second wait between invites
							if (now.isAfter(latest_invite_time.plusSeconds(10))) {
								invite(opponent_bot);
							} else {
								try {
									TimeUnit.SECONDS.sleep(10);
								} catch (InterruptedException e) {
									Utils.err(e.getMessage());
									e.printStackTrace();
								}
								invite(opponent_bot);
							}
						} else {
							Utils.err("Trainee could not be invited within 2 minutes, terminating");
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
					Utils.err(invitation.getString("message"));
				} else {

					JSONObject eval = validatePayload(invitation, new String[] { "socket_id", "socket_id" });
					if (!eval.getString("result").equals("valid")) {
						Utils.err(eval.getString("message"));
					} else {
						Utils.out("Invited by" + invitation.getString("socket_id"));
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
				JSONObject response = (JSONObject) args[0];
				if (response.getString("result").equals("fail")) {
					Utils.err(response.getString("message"));
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
					room = "lobby";
					joinRoom(room);
				}
			}

		}).on("game_update", new Emitter.Listener() {

			// Recieve the opponent's play and send a move back
			@Override
			public void call(Object... args) {

				Utils.out("Received game update");
				JSONObject payload = (JSONObject) args[0];

				// screen payload
				JSONObject eval = validatePayload(payload, new String[] { "result", "game" });
				if (!eval.getString("result").equals("valid")) {
					Utils.err(eval.getString("message"));
				} else {

					GameState state = prepareTurn((JSONObject) args[0]);

					// don't do anything unless it's your turn
					if (!state.isMyTurn()) {
						Utils.out("Not my turn, waiting patiently for the human");
					} else if (state.getGameStatus().equals(GameState.GAME_STATUS_TERMINAL)) {
					} else {
						// pick a move
						String[] move = decideMoveToPlay(state);

						// jsonify the move
						JSONObject play = new JSONObject() {
						};
						play.put("row", Integer.valueOf(move[0]));
						play.put("column", Integer.valueOf(move[1]));
						play.put("color", my_color);

						Utils.out("Playing token: " + move[0] + "," + move[1]);

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
				if (response.getString("result").equals("fail")) {
					Utils.err("Play token failed: " + response.getString("message"));
				}
			}

		}).on("game_over", new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				// Get winner
				JSONObject response = (JSONObject) args[0];
				Utils.out("Game over! Winner is " + response.getString("winner"));
				socket.disconnect();

				gamesPlayed++;
				// if (role.equals("train") && (MAX_GAMES <= gamesPlayed)) {
				// Utils.out("Training session over! Shutting down.");
				// System.exit(0);
				// }
				room = "lobby";
				// joinRoom(room);
				socket.connect();
			}

		}).on("terminate", new Emitter.Listener() {

			// Terminate self on Server's order
			@Override
			public void call(Object... args) {
				JSONObject response = (JSONObject) args[0];
				Utils.out("Exiting. Terminated by " + response.getString("terminator"));
				System.exit(0);
			}

		}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				String reason = data.getString("disconnect"); // TODO or "reason"?
				Utils.out("Disconnected. Reason: " + reason);
			}

		});

		Utils.out("Setup finished, connecting to server");
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
			Utils.err(payload.getString("message"));
			System.exit(1);
		}
		JSONObject game = (JSONObject) payload.get("game");

		// Check for good board
		JSONArray board = (JSONArray) game.get("board");
		if (board == null) {
			Utils.err("Internal server error: received malformed board update");
			System.exit(1);
		}

		// Assign colors
		JSONObject white = (JSONObject) game.get("player_white");
		JSONObject black = (JSONObject) game.get("player_black");
		if (socket.id().equals(black.getString("socket"))) {
			my_color = GameState.BLACK;
			my_token = GameState.BLACK;
			opp_color = GameState.WHITE;
			opp_token = GameState.WHITE;
		} else if (socket.id().equals(white.getString("socket"))) {
			my_color = GameState.WHITE;
			my_token = GameState.WHITE;
			opp_color = GameState.BLACK;
			opp_token = GameState.BLACK;
		} else {
			Utils.err("Something weird happened with colors");
			System.exit(1);
		}

		return new GameState();
	}

	/**
	 * 
	 * @param game
	 * @return
	 */
	public static String[] decideMoveToPlay(GameState state) {
		String[] move;
		switch (aiType) {

			// case "ab":
			// move = MM_Think.run(state);
			// break;

			// case "rl":
			// move = RL_Think.getMove();
			// break;

			default:
				move = state.getRandomMove().split(",");
		}

		return move;

	}

	/**** Socket.io Helper Functions ****/

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
	 * 
	 * @param requested_user
	 */
	public static void invite(String requested_user) {
		JSONObject payload = new JSONObject();
		payload.put("requested_user", requested_user);
		socket.emit("invite", payload);
		latest_invite_time = Instant.now();
	}

}
