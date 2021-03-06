/**
 * A bot to play Reversi using AI.
 * Author: Sam Reep, Westmont College
 */

package base;

import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import base.reinforcement_learning.Learner;
import base.reinforcement_learning.RL_State;
import base.minimax.MM_State;
import base.minimax.MM_Think;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.client.IO.Options;
import io.socket.emitter.Emitter;

public class Reversi_Bot {

	/** Constants **/
	public static final int MAX_JOIN_ATTEMPTS = 5;

	/** Server Fields **/
	public static int reconnect_count = 0;
	public static int join_lobby_fail_count = 0;
	public static Instant first_invite_time = null;
	public static Instant latest_invite_time = null;
	public static Instant green_light_to_move = null;
	public static int move_buffer_time = 2; // in seconds
	public static Socket socket = null;
	public static int port = 8080;
	public static String current_room = "nowhere";
	public static String new_room = "lobby";

	/** Bot Parameters **/
	public static String username = "Bot";
	public static String aiType = null; // mm or rl
	public static String difficulty = "easy";
	public static String role = "learn";
	public static String my_color = "b";
	public static String my_color_full = "black";
	public static int max_games = 2;
	public static int gamesPlayed = 0;

	/** Opponent Info **/
	public static String opponent_bot = null;
	public static String opp_color = "w";
	public static String opp_color_full = "w";

	/** Learner Instance **/
	public static Learner learner = null;

	/**
	 * Main method run on spawn from server
	 * 
	 * @param args [port, username, difficulty]
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws URISyntaxException {

		JSONObject payload = new JSONObject(args[0]);

		// Screen payload
		String[] req_keys = new String[] { "port", "ai_type", "username", "difficulty", "role", "opponent" };
		JSONObject eval = validatePayload(payload, req_keys);

		// deal with bad payload
		if (!eval.getString("result").equals("valid")) {
			// log failure locally
			Utils.shut_down(3, eval.getString("message"));
		}

		// Extract info from payload
		port = payload.getInt("port");
		aiType = payload.getString("ai_type");
		username = payload.getString("username");
		difficulty = payload.getString("difficulty");
		role = payload.getString("role");
		opponent_bot = payload.getString("opponent");

		Utils.out("Assigned params");

		// Set up socket functionality to interact with server
		Options opts = new Options();
		opts.forceNew = true;
		socket = IO.socket("http://localhost:" + port);

		// if (role.equals("learn")) {
		if (aiType.equals("rl")) {
			learner = new Learner();
			learner.learn(difficulty);
		}

		// if (opponent_bot != null) {
		// max_games = 1;
		// }

		setupSocket();

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
				JSONObject payload = new JSONObject();
				payload.put("room", new_room);
				payload.put("username", username);
				payload.put("is_bot", true);
				socket.emit("join_room", payload);
			}

		}).on("join_room_response", new Emitter.Listener() {

			/**
			 * Response from server after a request to join a room
			 */
			@Override
			public void call(Object... args) {
				JSONObject response = (JSONObject) args[0];
				// screen payload
				JSONObject eval = validatePayload(response, new String[] { "result", "username" });
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
								Utils.shut_down(1, "Failed to join lobby " + MAX_JOIN_ATTEMPTS + " times");
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

					case "success":
						// If succeeded, reset failure count
						if (username.equals(response.getString("username"))) {
							current_room = response.getString("room");
							new_room = null;
							Utils.out("Joined room " + current_room);
							join_lobby_fail_count = 0;
						}

						// If role is to initiate game AND room is lobby, invite the opponent
						if (opponent_bot != null && opponent_bot.equals(response.getString("username"))
								&& current_room.equals("lobby") && role.equals("invite")) {
							invite(response.getString("socket_id")); // debug this
							first_invite_time = Instant.now();
						}

						break;

					default:
						Utils.shut_down(1, "Malformed join_room response from server");
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

					if (role.equals("invite")) {
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
							Utils.shut_down(1, "Opponent could not be invited within 2 minutes");
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
						Utils.out("Invited by " + invitation.getString("socket_id"));
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
					joinRoom(response.getString("game_id"));
				}
			}

		}).on("player_disconnected", new Emitter.Listener() {

			// Rejoin the lobby if the opponent quits early
			@Override
			public void call(Object... args) {
				Utils.out("Opponent left. Heading to lobby");
				if (!current_room.equals("lobby")) {
					joinRoom("lobby");
				}
			}

		}).on("game_update", new Emitter.Listener() {

			// Recieve the opponent's play and send a move back
			@Override
			public void call(Object... args) {
				// Start buffer timer (so humans can see their moves)
				green_light_to_move = Instant.now().plusSeconds(move_buffer_time);

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
						Utils.out("Not my turn, waiting patiently for the opponent");
					} else if (state.getGameStatus().equals(GameState.GAME_STATUS_TERMINAL)) {
					} else if (state.move_list.size() <= 0) {
						Utils.out("No moves available, waiting for next update");
					} else {
						// pick a move
						// Should have form {rowInt, colInt}
						String[] move = decideMoveToPlay(state);

						// jsonify the move
						JSONObject play = new JSONObject() {
						};
						play.put("row", Integer.valueOf(move[0]));
						play.put("column", Integer.valueOf(move[1]));
						play.put("color", my_color_full);

						Utils.out("Playing token: " + move[0] + "," + move[1]);

						// send move to server
						if ((opponent_bot == null || opponent_bot.equals("none"))
								&& Instant.now().isBefore(green_light_to_move)) {
							try {
								TimeUnit.MILLISECONDS
										.sleep(Instant.now().until(green_light_to_move, ChronoUnit.MILLIS));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
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

				GameState final_state = prepareTurn(response);
				String game_over_message = "Game Over!";
				if (response.getString("winner").toLowerCase().equals(my_color_full)) {
					game_over_message += " I won!";
				} else if (response.getString("winner").toLowerCase().equals(opp_color_full)) {
					game_over_message += " I lost.";
				} else if (response.getString("winner").toLowerCase().equals("tie game")) {
					game_over_message += " We tied.";
				} else {
					game_over_message += " I don't know who won.";
				}
				game_over_message += " The score was " + final_state.black_count + " to " + final_state.white_count;
				Utils.out(game_over_message);

				gamesPlayed++;
				if (gamesPlayed >= max_games) {
					Utils.shut_down(0, "Reached max games! Shutting down.");
				}

				joinRoom("lobby");
			}

		}).on("terminate", new Emitter.Listener() {

			// Terminate self on Server's order
			@Override
			public void call(Object... args) {
				JSONObject response = (JSONObject) args[0];
				Utils.shut_down(0, "Terminated by " + response.getString("terminator"));
			}

		}).on("error", new Emitter.Listener() {

			// Output any errors
			@Override
			public void call(Object... args) {
				JSONObject response = (JSONObject) args[0];
				Utils.shut_down(1, "Received from server: " + response.getString("reason"));
			}

		}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... args) {
				// JSONObject payload = (JSONObject) args[0];
				// String reason = payload.getString("reason");
				// Utils.out("Disconnected. Reason: " + reason);
			}

		});

		Utils.out("Setup finished, connecting to server");
		socket.connect();
	}

	/**** Misc Helper Functions ****/

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
			Utils.shut_down(1, payload.getString("message"));
		}

		JSONObject game = (JSONObject) payload.get("game");

		// Screen game
		String[] req_keys = new String[] { "board", "player_white", "player_black", "whose_turn" };
		JSONObject eval = validatePayload(game, req_keys);

		// deal with bad payload
		if (!eval.getString("result").equals("valid")) {
			Utils.shut_down(1, "Bad game update: " + eval.getString("message"));
		}

		// Check for good board
		JSONArray JSONboard = (JSONArray) game.get("board");
		String[][] board = new String[][] {};

		board = GameState.JSONArrToStringArr(JSONboard);

		// Assign colors
		JSONObject white = (JSONObject) game.get("player_white");
		JSONObject black = (JSONObject) game.get("player_black");
		if (socket.id().equals(black.getString("socket"))) {
			my_color = GameState.BLACK;
			my_color_full = "black";
			opp_color = GameState.WHITE;
			opp_color_full = "white";
		} else if (socket.id().equals(white.getString("socket"))) {
			my_color = GameState.WHITE;
			my_color_full = "white";
			opp_color = GameState.BLACK;
			opp_color_full = "black";
		} else {
			Utils.shut_down(1, "Something weird happened with colors");
		}

		String next_player = game.getString("whose_turn").substring(0, 1);

		if (aiType.equals("mm")) {
			return new MM_State(board, next_player);
		} else if (aiType.equals("rl")) {
			return new RL_State(board, next_player);
		} else {
			return new GameState(board, next_player);
		}
	}

	/**
	 * 
	 * @param game
	 * @return
	 */
	public static String[] decideMoveToPlay(GameState state) {
		String[] move;
		switch (aiType) {

			case "mm":
				move = MM_Think.run((MM_State) state);
				break;

			case "rl":
				move = learner.play((RL_State) state);
				break;

			default:
				move = state.getRandomMove().split("");
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
			try {
				Object p = payload.get(s);
				if (p == null) {
					if (valid) {
						valid = false;
						message = "Payload was missing item(s): ";
					}
					message += s + " ";
				}
			} catch (Exception e) {
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
		if (current_room.equals(room_to_join)) {
			Utils.err("Whoa there! You're already in room " + current_room);
		} else {
			socket.disconnect();
			new_room = room_to_join;
			socket.connect();
		}
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
