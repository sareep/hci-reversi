/**
 * @author Sam Reep
 * Scope: handles details of when it is the bot's turn to place a tile
 */
package reinforcement_learning.bot;

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
    public GameState(Boolean my_turn, JSONArray legal_moves){
        this.my_turn = my_turn;

        this.legal_moves = calculateLegalMoves(RL_Bot.my_color, legal_moves);
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

    /********* MOVE-SCREENING FUNCTIONS *********/
    private ArrayList<String> calculateLegalMoves(String who, JSONArray board){
        ArrayList<String> moves = new ArrayList<String>();

        for(int row=0;row<8;row++){
            for(int col=0;col<8;col++){
                String value = board.getJSONArray(row).getString(col);
                if(value.equals(RL_Bot.my_color.substring(0,1))){
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


	public boolean isOver() {
		return this.legal_moves.size() == 0;
	}

}