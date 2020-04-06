/**
 * @author Sam Reep
 * Scope: handles details of when it is the bot's turn to place a tile
 */
package base;

import java.util.ArrayList;

import org.json.JSONArray;

// TODO: Reorganize this class to split up helper stuff/constants and actual State Info
@SuppressWarnings("unused")
public class GameState extends Utils {

    public ArrayList<String> move_list = new ArrayList<String>();
    public int utility;
    public String[][] board;
    // public String[][] legal_moves_board;
    public String next_player;
    public String previous_player;
    public int black_count = 0;
    public int white_count = 0;
    public String gameStatus;

    public static final String GAME_STATUS_TERMINAL = "terminal";
    public static final String GAME_STATUS_CONTINUING = "continuing";
    public static final String GAME_STATUS_NEW = "new";

    public static final String BLACK = "black";
    public static final String WHITE = "white";

    public static final String BLACK_TILE = "b";
    public static final String WHITE_TILE = "w";
    public static final String EMPTY_TILE = " ";

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

    // /**
    // *
    // * @param my_turn
    // * @param board
    // * @param legal_moves
    // */
    // public GameState(Boolean my_turn, JSONArray legal_moves) {
    // this.move_list = condenseLegalMoves(Reversi_Bot.my_color, legal_moves);

    // if (this.move_list.size() == 0) {
    // this.gameStatus = GAME_STATUS_TERMINAL;
    // }
    // }
    /********* CONSTRUCTORS *********/
    public GameState() {
        board = newBoard();
        black_count = 2;
        white_count = 2;
        next_player = BLACK_TILE;
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
        board[row][col] = who.substring(0, 1);

        // Store who should play next
        next_player = otherPlayer(who);

        // Count tiles for each player
        countTiles();

        // Find what moves are available
        move_list = calculateLegalMoves(next_player, board);

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
    protected void countTiles(){
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j].equals(BLACK_TILE)) {
                    black_count++;
                } else if (board[i][j].equals(WHITE_TILE)) {
                    white_count++;
                }
            }
        }
    }
    
    /********* MOVE-PICKING FUNCTIONS *********/

    /**
     * 
     * @return a random legal move
     */
    public String getRandomMove() {
        return this.move_list.get((int) (Math.random() * (move_list.size())));
    }

    /******* LEGAL MOVES ********/

    /**
     * Turns the 2D array into a list of row,col pairs
     * 
     * @param who
     * @param board
     * @return
     */
    private ArrayList<String> condenseLegalMoves(String who, JSONArray board) {
        ArrayList<String> moves = new ArrayList<String>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String value = board.getJSONArray(row).getString(col);
                if (value.equals(who.substring(0, 1))) {
                    moves.add(row + "," + col);
                }
            }
        }

        return moves;
    }

    /**
     * Turns the 2D array into a list of row,col pairs
     * 
     * @param who
     * @param board
     * @return
     */
    private ArrayList<String> condenseLegalMoves(String who, String[][] board) {
        ArrayList<String> moves = new ArrayList<String>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String value = board[row][col];
                if (value.equals(who.substring(0, 1))) {
                    moves.add(row + "," + col);
                }
            }
        }

        return moves;
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

    // TODO DON'T hijack these fuctions to count the number of tokens that would be flipped - instead, compare bscore and wscore between parent/child!
    // 
    // /**
    //  * Find all legal moves for player 'who'
    //  * 
    //  * @param who
    //  * @return
    //  */
    // public static String[][] calculateLegalMoves(String who, String[][] board) {
    //     String[][] valid_moves = emptyBoard();

    //     // For each coordinate, check a line in each direction
    //     for (int i = 0; i < 8; i++) {
    //         for (int j = 0; j < 8; j++) {
    //             if (board[i][j].equals(" ")) {
    //                 Boolean nw = valid_move(who, -1, -1, i, j, board);
    //                 Boolean nn = valid_move(who, -1, 0, i, j, board);
    //                 Boolean ne = valid_move(who, -1, 1, i, j, board);

    //                 Boolean ww = valid_move(who, 0, -1, i, j, board);
    //                 Boolean ee = valid_move(who, 0, 1, i, j, board);

    //                 Boolean sw = valid_move(who, 1, -1, i, j, board);
    //                 Boolean ss = valid_move(who, 1, 0, i, j, board);
    //                 Boolean se = valid_move(who, 1, 1, i, j, board);
    //                 if (nw || nn || ne || ww || ee || sw || ss || se) {
    //                     valid_moves[i][j] = who;
    //                 }
    //             }
    //         }
    //     }

    //     return valid_moves;
    // }

    public static ArrayList<String> calculateLegalMoves(String who, String[][] board) {
        ArrayList<String> valid_moves = new ArrayList<String>();

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
                        valid_moves.add(i+""+j);
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