/**
 * @author Sam Reep
 * Scope: handles details of when it is the bot's turn to place a tile
 */
package base;

import java.util.ArrayList;

import org.json.JSONArray;

public class GameState {

    private Boolean my_turn = false;
    private ArrayList<String> legal_moves = new ArrayList<String>();

    /********* CONSTRUCTORS *********/

    /**
     * 
     * @param my_turn
     * @param board
     * @param legal_moves
     */
    public GameState(Boolean my_turn, JSONArray legal_moves) {
        this.my_turn = my_turn;

        this.legal_moves = condenseLegalMoves(Reversi_Bot.my_color, legal_moves);
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
        return new ArrayList<String>(this.legal_moves);
    }

    private ArrayList<String> condenseLegalMoves(String who, JSONArray board){
        ArrayList<String> moves = new ArrayList<String>();

        for(int row=0;row<8;row++){
            for(int col=0;col<8;col++){
                String value = board.getJSONArray(row).getString(col);
                if(value.equals(Reversi_Bot.my_color.substring(0,1))){
                    moves.add(row + "," + col);
                }
            }
        }

        return moves;
    }

    /********* MOVE-PICKING FUNCTIONS *********/

    /**
     * 
     * @return a random legal move
     */
	public String getRandomMove() {
        return this.legal_moves.get((int) (Math.random() * (legal_moves.size())));
    }


    
    
    /* TODO clean up everything under here */
    
    // public static char BLACK_TILE = 'b';
    // public static char WHITE_TILE = 'w';
    // public static char EMPTY_TILE = ' ';
    
    
        public boolean isOver() {
            return this.legal_moves.size() == 0;
        }



    private static String[][] NEW_BOARD = new String[][] { 
        { " ", " ", " ", " ", " ", " ", " ", " " }, 
        { " ", " ", " ", " ", " ", " ", " ", " " },
        { " ", " ", " ", " ", " ", " ", " ", " " }, 
        { " ", " ", " ", "w", "b", " ", " ", " " }, 
        { " ", " ", " ", "b", "w", " ", " ", " " },
        { " ", " ", " ", " ", " ", " ", " ", " " }, 
        { " ", " ", " ", " ", " ", " ", " ", " " },
        { " ", " ", " ", " ", " ", " ", " ", " " } };

    public static String[][] newBoard(){
        String[][] board = new String[NEW_BOARD.length][NEW_BOARD[0].length];
        for (int i = 0; i < NEW_BOARD.length; i++) {
            System.arraycopy(NEW_BOARD[i], 0, board[i], 0, board[0].length);
        }
        return board;
    }

}