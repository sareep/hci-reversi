/**
 * @author Sam Reep
 */
package base;

import java.util.ArrayList;

public class GameState extends Utils {

    public static final String GAME_STATUS_TERMINAL = "terminal";
    public static final String GAME_STATUS_CONTINUING = "continuing";
    public static final String BLACK = "b";
    public static final String WHITE = "w";

    public String[][] board;
    public String next_player;
    public String previous_player;
    public String gameStatus = GAME_STATUS_CONTINUING;
    public ArrayList<String> move_list = new ArrayList<String>();

    public int black_count = 0;
    public int white_count = 0;
    public int utility;

    static String[][] NEW_BOARD = new String[][] { { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " }, { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", "w", "b", " ", " ", " " }, { " ", " ", " ", "b", "w", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " }, { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " } };

    static String[][] EMPTY_BOARD = new String[][] { { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " }, { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " }, { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " }, { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " } };

    /********* CONSTRUCTORS *********/

    public GameState() {
        board = newBoard();
        black_count = 2;
        white_count = 2;
        next_player = BLACK;
    }

    public GameState(GameState parent, int row, int col, String who) {
        // Screen transition
        if (!parent.next_player.equals(who)) {
            err("Tried to play out of turn");
        }
        if (row > 7 || row < 0) {
            err("Tried to play off of the board at row: " + row);
        }
        if (col > 7 || col < 0) {
            err("Tried to play off of the board at col: " + col);
        }

        /* Populate fields */

        // Create updated board
        board = new String[parent.board.length][parent.board[0].length];
        for (int i = 0; i < parent.board.length; i++) {
            System.arraycopy(parent.board[i], 0, board[i], 0, board[0].length);
        }
        board[row][col] = who;
        board = flip_board(who, row, col, board);

        // Store who should play next
        next_player = otherPlayer(who);

        // Count tiles for each player
        countTiles();

        // Find what moves are available
        move_list = calculateLegalMoves(next_player, board);

        gameStatus = move_list.size() > 0 ? GAME_STATUS_CONTINUING : GAME_STATUS_TERMINAL;

    }

    /********* GETTERS AND SETTERS *********/

    private String otherPlayer(String player) {
        String other = null;
        if (player.equals(BLACK)) {
            other = WHITE;
        } else if (player.equals(WHITE)) {
            other = BLACK;
        } else {
            Utils.err("Someone is using a bad color: " + player);
        }
        return other;
    }

    /**
     * @return if it is the bot's turn to play
     */
    public Boolean isMyTurn() {
        return next_player.equals(Reversi_Bot.my_color);
    }

    /**
     * @return the legal_moves
     */
    public ArrayList<String> getLegalMoves() {
        return new ArrayList<String>(this.move_list);
    }

    public String getGameStatus() {
        return this.gameStatus;
    }

    public static String[][] newBoard() {
        String[][] board = new String[NEW_BOARD.length][NEW_BOARD[0].length];
        for (int i = 0; i < NEW_BOARD.length; i++) {
            System.arraycopy(NEW_BOARD[i], 0, board[i], 0, board[0].length);
        }
        return board;
    }

    public static String[][] emptyBoard() {
        String[][] board = new String[EMPTY_BOARD.length][EMPTY_BOARD[0].length];
        for (int i = 0; i < EMPTY_BOARD.length; i++) {
            System.arraycopy(EMPTY_BOARD[i], 0, board[i], 0, board[0].length);
        }
        return board;
    }

    /***** Misc Helpers *******/
    protected void countTiles() {
        int bc = 0, wc = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j].equals(BLACK)) {
                    bc++;
                } else if (board[i][j].equals(WHITE)) {
                    wc++;
                }
            }
        }
        this.black_count = bc;
        this.white_count = wc;
    }

    /**
     * 
     * @return a random legal move
     */
    public String getRandomMove() {
        return this.move_list.get((int) (Math.random() * (move_list.size())));
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

        if (board[r + dr][c + dc].equals(who)) {
            return true;
        } else if (flip_line(who, dr, dc, r + dr, c + dc, board)) {
            board[r + dr][c + dc] = who;
            return true;
        } else {
            return false;
        }
    }

    /***** LEGAL MOVES *****/
    public static ArrayList<String> calculateLegalMoves(String who, String[][] board) {
        ArrayList<String> valid_moves = new ArrayList<String>();

        // For each coordinate, check a line in each direction
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
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
                        valid_moves.add(i + "" + j);
                    }
                }
            }
        }

        return valid_moves;
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

}