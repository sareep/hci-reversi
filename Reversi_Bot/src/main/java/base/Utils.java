package base;

/**
 * Utils
 */
public class Utils {

    /**** Constants ****/
    // TODO this might not initialize right if difficulty isn't assigned immediately
    public static final String PATH_TO_EPISODES = "learning_episodes/" + Reversi_Bot.difficulty + "/";

    public static final String GAME_STATUS_TERMINAL = "terminal";
    public static final String GAME_STATUS_CONTINUING = "continuing";
    public static final String GAME_STATUS_NEW = "new";

    public static String BLACK = "black";
    public static String WHITE = "white";

    public static String BLACK_TILE = "b";
    public static String WHITE_TILE = "w";
    public static String EMPTY_TILE = " ";

    static String[][] NEW_BOARD = new String[][] { { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " }, { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", "w", "b", " ", " ", " " }, { " ", " ", " ", "b", "w", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " }, { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " } };

    public static String[][] newBoard() {
        String[][] board = new String[NEW_BOARD.length][NEW_BOARD[0].length];
        for (int i = 0; i < NEW_BOARD.length; i++) {
            System.arraycopy(NEW_BOARD[i], 0, board[i], 0, board[0].length);
        }
        return board;
    }

    /**** Game Logic ****/

    /**
     * Flips sandwiched tiles given who's play at (row, column)
     * 
     * @param who   the player placing a tile
     * @param row   the row at which who plays
     * @param col   the column at which who plays
     * @param board
     * @return
     */
    public String[][] flip_board(String who, int row, int col, String[][] board) {
        String[][] new_board = new String[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, new_board[i], 0, new_board[0].length);
        }

        flip_line(who, -1, -1, row, col, new_board);
        flip_line(who, -1, 0, row, col, new_board);
        flip_line(who, -1, 1, row, col, new_board);

        flip_line(who, 0, -1, row, col, new_board);
        flip_line(who, 0, 1, row, col, new_board);

        flip_line(who, 1, -1, row, col, new_board);
        flip_line(who, 1, 0, row, col, new_board);
        flip_line(who, 1, 1, row, col, new_board);

        return new_board;

    }

    private boolean flip_line(String who, int dr, int dc, int r, int c, String[][] board) {

        if ((r + dr < 0) || (r + dr > 7) || (c + dc < 0) || (c + dc > 7) || (board[r + dr][c + dc] == " ")) {
            return false;
        }

        if (board[r + dr][c + dc] == who) {
            return true;
        } else if (flip_line(who, dr, dc, r + dr, c + dc, board)) {
            board[r + dr][c + dc] = who;
            return true;
        } else {
            return false;
        }

    }

    /**
	 * Check if a certain line begins and ends with who's color
	 * 
	 * @param who   player placing a token
	 * @param dr    north/south change
	 * @param dc    east/west change
	 * @param r     row of location being evaluated
	 * @param c     column of location being evaluated
	 * @param board the original board being evaluated
	 * @return
	 */
	static Boolean check_line_match(String who, int dr, int dc, int r, int c, String[][] board) {
	    if (board[r][c].equals(who)) {
	        return true;
	    } else if ((r + dr < 0) || (r + dr > 7) || (c + dc < 0) || (c + dc > 7) || board[r][c].equals(" ")) {
	        return false;
	    } else {
	        return check_line_match(who, dr, dc, r + dr, c + dc, board);
	    }
	}

	/**
	 * Find if a certain (r,c) is a legal move
	 * 
	 * @param who   player placing a token
	 * @param dr    north/south change
	 * @param dc    east/west change
	 * @param r     row of location being evaluated
	 * @param c     column of location being evaluated
	 * @param board the original board being evaluated
	 * @return
	 */
	static Boolean valid_move(String who, int dr, int dc, int r, int c, String[][] board) {
	    String opp;
	    switch (who) {
	        case "b":
	            opp = "w";
	            break;
	        case "w":
	            opp = "b";
	            break;
	        default:
	            out("Color problem: " + who);
	            return false;
	    }
	
	    if ((r + dr < 0) || (r + dr > 7) || (r + dr + dr < 0) || (r + dr + dr > 7) || (c + dc < 0) || (c + dc > 7)
	            || (c + dc + dc < 0) || (c + dc + dc > 7) || (!board[r + dr][c + dc].equals(opp))) {
	        return false;
	    }
	
	    return check_line_match(who, dr, dc, r + dr + dr, c + dc + dc, board);
	}

	// TODO hijack these fuctions to count the number of tokens that would be
	// flipped?
	/**
	 * Find all legal moves for player 'who'
	 * 
	 * @param who
	 * @return
	 */
	public static String[][] calculateLegalMoves(String who, String[][] board) {
	    String[][] valid_moves = new String[][] { { " ", " ", " ", " ", " ", " ", " ", " " },
	            { " ", " ", " ", " ", " ", " ", " ", " " }, { " ", " ", " ", " ", " ", " ", " ", " " },
	            { " ", " ", " ", " ", " ", " ", " ", " " }, { " ", " ", " ", " ", " ", " ", " ", " " },
	            { " ", " ", " ", " ", " ", " ", " ", " " }, { " ", " ", " ", " ", " ", " ", " ", " " },
	            { " ", " ", " ", " ", " ", " ", " ", " " } };
	
	    // For each coordinate, check a line in each direction
	    for (int i = 0; i < 8; i++) {
	        for (int j = 0; j < 8; j++) {
	            if (board[i][j].equals(" ")) {
	                Boolean nw = valid_move(who, -1, -1, i, j, board);
	                Boolean nn = valid_move(who, -1, 0, i, j, board);
	                Boolean ne = valid_move(who, -1, 1, i, j, board);
	
	                Boolean ww = valid_move(who, 0, -1, i, j, board);
	                Boolean ee = valid_move(who, 0, 1, i, j, board);
	
	                Boolean sw = valid_move(who, 1, -1, i, j, board);
	                Boolean ss = valid_move(who, 1, 0, i, j, board);
	                Boolean se = valid_move(who, 1, 1, i, j, board);
	                if (nw || nn || ne || ww || ee || sw || ss || se) {
	                    valid_moves[i][j] = who;
	                }
	            }
	        }
	    }
	
	    return valid_moves;
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