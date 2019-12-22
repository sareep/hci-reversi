/**
 * @author Sam Reep
 * Scope: handles details of when it is the bot's turn to place a tile
 */
package bot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class Turn {

    private Boolean my_move = false;
    private String my_color = "";
    private Move best_move;
    private ArrayList<Move> legal_moves = new ArrayList<Move>();
    private String[][] board = new String[][]{};
    private Instant endTime;


    /********* CONSTRUCTORS *********/
    
    /**
     * 
     * @param my_move
     * @param my_color
     * @param board
     * @param legal_moves
     */
    public Turn(Boolean my_move, String my_color, String[][] board, String legal_moves[][]){
        this.my_move = my_move;

        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, board[0].length);
        }

        this.my_color = my_color;

        this.best_move = new Move(my_color);

        this.legal_moves = calculateLegalMoves(my_color, legal_moves);
    }


    /********* GETTERS AND SETTERS *********/
    // TODO do i need most of these?
    
    /**
     * @return the my_move
     */
    public Boolean isMyMove() {
        return my_move;
    }

    /**
     * @return the best_move
     */
    public Move getBestMove() {
        return best_move;
    }

    /**
     * @param my_color the my_color to set
     */
    public void setMyColor(String my_color) {
        this.my_color = my_color;
    }

    /**
     * @return the my_color
     */
    public String getMyColor() {
        return my_color;
    }
    
    /**
     * @return the board
     */
    public String[][] getBoard() {
        String[][] copy = new String[this.board.length][this.board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(this.board[i], 0, copy[i], 0, copy[0].length);
        }
        return copy;
    }
    
    /**
     * @return the legal_moves
     */
    public ArrayList<Move> getLegalMoves() {
        return new ArrayList<Move>(this.legal_moves);
    }


    /********* LOGIC FUNCTIONS *********/
    
    // /**
    //  * TODO do i even need this class? maybe just leave it as a 2d array
    //  * @param board
    //  * @return
    //  */
    // public String[] convertLegalMoves(String[][] board) {

    //     HashSet<String> moves = new HashSet<String>();
    //     for (int i = 0; i < board.length; i++) {
    //         for (int j = 0; j < board[i].length; j++) {
    //             // if(board[i][j] == Bot.this.my_color.substring(0,1)){ //TODO change this to pass in the color or something
    //             //     moves.add(i+"_"+j);
    //             // }
    //         }
    //     }

    //     return (String[]) moves.toArray();
    // }

    /**
     * 
     * @param difficulty
     * @return
     */
    public Move getMove(String difficulty) {
        switch (difficulty) {
            case "easy":
                 getRandomMove();
        
            case "medium":
                //TODO figure out how much logic to put in this one


            case "hard":
                ArrayList<Move> moveList = calculateLegalMoves(my_color, board);
                endTime = Instant.now().plusMillis(2000);
                iterativeDeepening(moveList);
                
            default:
                getRandomMove();
        }

        return this.best_move;

        // String[] moveArray = move.split("_");
        // Integer[] payload = new Integer[moveArray.length];
        // for (int i = 0; i < moveArray.length; i++) {
        //     payload[i] = Integer.valueOf(moveArray[i]);
        // }

        // return payload;
    }


    /********* MOVE-SCREENING FUNCTIONS *********/
    // TODO hijack these fuctions to count the number of tokens that would be flipped?

    /**
     * 
     * @param who
     * @param board
     */
    private ArrayList<Move> calculateLegalMoves(String who, String[][] board){

        ArrayList<Move> legal_moves = new ArrayList<Move>();
        
        for(int row=0;row<8;row++){
            for(int col=0;col<8;col++){
                if(board[row][col] == " "){
                    Boolean nw = valid_move(who, -1,-1,row,col,board);
                    Boolean nn = valid_move(who, -1, 0,row,col,board);
                    Boolean ne = valid_move(who, -1, 1,row,col,board);

                    Boolean ww = valid_move(who,  0,-1,row,col,board);
                    Boolean ee = valid_move(who,  0, 1,row,col,board);
                    
                    Boolean sw = valid_move(who,  1,-1,row,col,board);
                    Boolean ss = valid_move(who,  1, 0,row,col,board);
                    Boolean se = valid_move(who,  1, 1,row,col,board);
                    if(nw || nn || ne || ww || ee || sw || ss || se){
                        legal_moves.add(new Move(row, col, who));
                    }
                }
            }
        }

        return legal_moves;
    }
    
    /**
     * 
     * @param who
     * @param dr
     * @param dc
     * @param r
     * @param c
     * @param board
     * @return
     */
    private Boolean valid_move(String who, int dr, int dc, int r, int c, String[][] board) {
        String other = ""; 
        switch (who){
            case "b":
                other = "w";
                break;
            case "w":
                other = "b";
                break;
            default:
                System.err.println("Color problem: "+who);
                System.exit(1);
                return false;
        }

        if( (r+dr < 0) || (r+dr > 7) 
            || (r+dr+dr < 0) || (r+dr+dr > 7) 
            || (c+dc < 0) || (c+dc > 7) 
            || (c+dc+dc < 0) || (c+dc+dc > 7) 
            || (board[r+dr][c+dc] != other)){
            return false;
        }

         return check_line_match(who,dr,dc,r+dr+dr,c+dc+dc,board);
    }

    /**
     * 
     * @param who
     * @param dr
     * @param dc
     * @param r
     * @param c
     * @param board
     * @return
     */
    private Boolean check_line_match(String who, int dr, int dc, int r, int c, String[][] board) {
        if(board[r][c] == who){
            return true;
        }else if((r+dr < 0) || (r+dr > 7) || (c+dc < 0) || (c+dc > 7) || board[r][c] == " "){
            return false;
        }else{
            return check_line_match(who,dr,dc,r+dr,c+dc,board);
        }
    }


    /********* MOVE-PICKING FUNCTIONS *********/

    /**
     * 
     * @return a random legal move
     */
	private Move getRandomMove() {
        return this.legal_moves.get((int) (Math.random() * (legal_moves.size())));
    }

    private void iterativeDeepening(ArrayList<Move> moves) {
        for (int _depth = 0; _depth < 1000000; _depth++) {
			if (Instant.now().isBefore(endTime)) {
				int v = alphaBeta(_depth);
				if (v>best_move.getScore()) { 
					for (Move m : moves) {
						if (v == m.getScore()) {
                            best_move.overwrite(m);
						}
					}
				}
				// deepestDepth = _depth;
			}else {
				return;
			}
		}
    }

    private int alphaBeta(int maxDepth) {
        int v = 0;
		v = maxValue(move, -1000000, 1000000, 0, maxDepth);
		// if(!Instant.now().isBefore(endTime)){return v;}
		return v;
    }

    /**
	 * Calculates the highest utility move for this player and returns it
	 * Used pseudo-code from the textbook, page 711
	 * @param previousMove the move to test
	 * @param max the maximum utility
	 * @param min the minimum utility
	 * @param currentDepth how deep the search has progressed
	 * @return value of the best move for this player
	 */
    public int maxValue(Move previousMove, int max, int min, int currentDepth, int maxDepth) {
		int v = -1000000;
		for(Move m : legalMoves) {
			if (currentDepth <= maxDepth && Instant.now().isBefore(endTime)){
				m.calculateUtility();
				m.setBoardState(previousMove.getBoardState());
				m.updateBoard(m.getRow(), m.getCol(), myColor.charAt(0));
				v = Math.max(v, minValue(m, max, min, currentDepth, maxDepth)); 
				if (v>=min) {
					return v;
				}
				max = Math.max(max, v);
			}else {
				return v;
			}
		}
		return v;
    }
    
    /**
	 * Calculates the highest utility move for the opponent and returns it
	 * Used pseudo-code from the textbook, page 711
	 * @param previousMove the move to test
	 * @param max the maximum utility
	 * @param min the minimum utility
	 * @param currentDepth how deep the search has progressed
	 * @return value of the best move for the opponent
	 */
	public  int minValue(Move previousMove, int max, int min, int currentDepth, int maxDepth) {
		int v = 1000000;
		for(Move m : legalMoves){
			if (currentDepth <= maxDepth && Instant.now().isBefore(endTime)) {
				m.calculateUtility();
				m.setBoardState(previousMove.getBoardState());
				m.updateBoard(m.getRow(), m.getCol(), oppColor.charAt(0));
				v = Math.min(v, maxValue(m, max, min, currentDepth+1, maxDepth));
				if (v<=max) {
					return v;
				}
				min = Math.min(min, v);
			}else{
				return v;
			}
		}
		return v;
	}
}