/**
 * @author Sam Reep
 * Scope: handles details of when it is the bot's turn to place a tile
 */
package base;

import java.util.ArrayList;

import org.json.JSONArray;

public class GameState extends Utils {

    private Boolean my_turn = false;
    private ArrayList<String> moveSet = new ArrayList<String>();
    public int utility;
    public String next_player;
    public String[][] board;
    public int black_count = 0;
    public int white_count = 0;
    public String[][] legal_moves;
    public String gameStatus;

    /********* CONSTRUCTORS *********/
    public GameState() {
        board = new String[][] { { " ", " ", " ", " ", " ", " ", " ", " " }, { " ", " ", " ", " ", " ", " ", " ", " " },
                { " ", " ", " ", " ", " ", " ", " ", " " }, { " ", " ", " ", " ", " ", " ", " ", " " },
                { " ", " ", " ", "w", "b", " ", " ", " " }, { " ", " ", " ", "b", "w", " ", " ", " " },
                { " ", " ", " ", " ", " ", " ", " ", " " }, { " ", " ", " ", " ", " ", " ", " ", " " },
                { " ", " ", " ", " ", " ", " ", " ", " " } };
        black_count = 2;
        white_count = 2;
        next_player = BLACK_TILE;
    }

    /**
     * 
     * @param my_turn
     * @param board
     * @param legal_moves
     */
    public GameState(Boolean my_turn, JSONArray legal_moves) {
        this.my_turn = my_turn;

        this.moveSet = condenseLegalMoves(Reversi_Bot.my_color, legal_moves);

        if(this.moveSet.size() == 0){
            this.gameStatus = GAME_STATUS_TERMINAL;
        }
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
        if (parent.next_player.equals(BLACK)) {
            next_player = WHITE;
        } else if (parent.next_player.equals(WHITE)) {
            next_player = BLACK;
        }

        // Count tiles for each player
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j].equals("b")) {
                    black_count++;
                } else if (board[i][j].equals("w")) {
                    white_count++;
                }
            }
        }

        // Find what moves are available
        legal_moves = calculateLegalMoves(next_player, board);

    }

    /********* GETTERS AND SETTERS *********/

    /**
     * @return if it is the bot's turn to play
     */
    public Boolean isMyTurn() {
        return my_turn;
    }

    /**
     * @return the legal_moves
     */
    public ArrayList<String> getLegalMoves() {
        return new ArrayList<String>(this.moveSet);
    }



    /********* MOVE-PICKING FUNCTIONS *********/

    /**
     * 
     * @return a random legal move
     */
    public String getRandomMove() {
        return this.moveSet.get((int) (Math.random() * (moveSet.size())));
    }

    /* TODO clean up everything under here */



    public String getGameStatus() {
        return this.gameStatus;
    }


    /******* LEGAL MOVES ********/
    
    /**
     * Turns the 2D array into a list of row,col pairs
     * @param who
     * @param board
     * @return
     */
    private ArrayList<String> condenseLegalMoves(String who, JSONArray board) {
        ArrayList<String> moves = new ArrayList<String>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String value = board.getJSONArray(row).getString(col);
                if (value.equals(Reversi_Bot.my_token)) {
                    moves.add(row + "," + col);
                }
            }
        }

        return moves;
    }

    /**
     * Turns the 2D array into a list of row,col pairs
     * @param who
     * @param board
     * @return
     */
    private ArrayList<String> condenseLegalMoves(String who, String[][] board) {
        ArrayList<String> moves = new ArrayList<String>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String value = board[row][col];
                if (value.equals(Reversi_Bot.my_token)) {
                    moves.add(row + "," + col);
                }
            }
        }

        return moves;
    }

}