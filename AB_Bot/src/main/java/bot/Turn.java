/**
 * @author Sam Reep
 * Scope: handles details of when it is the bot's turn to place a tile
 */
package bot;

import java.time.Instant;
import java.util.ArrayList;

public class Turn {

    private Boolean my_turn = false;
    private Move best_move;
    private ArrayList<Move> legal_moves = new ArrayList<Move>();
    private String[][] original_board = new String[][]{};
    private Instant endTime;
    /**
     * dlsReturn
     */
    public class DLSReturn {
        public Move move;
        public boolean remaining;

        public DLSReturn(Move m, boolean b){
            move = m;
            remaining = b;
        }
    }

    /********* CONSTRUCTORS *********/
    
    /**
     * 
     * @param my_turn
     * @param board
     * @param legal_moves
     */
    public Turn(Boolean my_turn, String[][] board, String legal_moves[][]){
        this.my_turn = my_turn;

        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, this.original_board[i], 0, board[0].length);
        }

        this.best_move = new Move(Bot.my_color);

        this.legal_moves = calculateLegalMoves(Bot.my_color, legal_moves);
    }


    /********* GETTERS AND SETTERS *********/
    
    /**
     * @return if it is the bot's turn to play
     */
    public Boolean isMyTurn() {
        return my_turn;
    }

    /**
     * @return the best_move
     */
    public Move getBestMove() {
        return best_move;
    }
    
    /**
     * @return the board
     */
    public String[][] getBoard() {
        String[][] copy = new String[this.original_board.length][this.original_board[0].length];
        for (int i = 0; i < original_board.length; i++) {
            System.arraycopy(this.original_board[i], 0, copy[i], 0, copy[0].length);
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

    public Move getMove(String difficulty) {

        ArrayList<Move> moveList;
        int timeLimit = 0;

        switch (difficulty) {
            case "easy":
                getRandomMove();
                break;

            case "medium":
                // ArrayList<Move> moveList = calculateLegalMoves(Bot.my_color, original_board);
                timeLimit = 2;

            case "hard":
                moveList = calculateLegalMoves(Bot.my_color, original_board);
                if (timeLimit == 0) {
                    timeLimit = 4;
                }
                endTime = Instant.now().plusSeconds(timeLimit);
                IDDFS();
                
            default:
                getRandomMove();
        }

        return this.best_move;
    }


    /********* MOVE-SCREENING FUNCTIONS *********/
    // TODO hijack these fuctions to count the number of tokens that would be flipped?

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




    //stuff from wikipedia
    private Move IDDFS() {
        DLSReturn returnVal = new DLSReturn(null, false);
        for (int depth = 0; depth < 100000; depth++) {
            if(Instant.now().isBefore(endTime)){
                returnVal = DLS(Bot.lastMove, depth);
                
                if(returnVal.move != null){
                    return returnVal.move;
                }else if(!returnVal.remaining){
                    return null;//TODO return the next best move instead
                }
            }else{
                break;
            }
        }

        //return best move found so far
        return returnVal.move;
    }

    private DLSReturn DLS(Move move, int depth) {
        if(depth == 0){
            if(isTerminalMove(move) && (winner(move) == Bot.my_color)){
                return new DLSReturn(move, true);
            }else{
                return new DLSReturn(null, true);
            }
        }else if(depth > 0){
            boolean any_remaining = false;
            //TODO recursively loop over children states (while loop with var outside that gets updated?)
            do{

            }while(any_remaining);
        }

        return new DLSReturn(null, false);
    }

    // TODO what if this was in the move? then it can stop calculating once it finds 1 move
    private boolean isTerminalMove(Move m) {
        ArrayList<Move> moves_available = calculateLegalMoves(m.getWho().substring(0, 1), m.getResultingBoard());
        return (moves_available.size() == 0);
    }

    private String winner(Move m) {
        String winner = "";
        int black = m.getBlack_score();
        int white = m.getWhite_score();

        if(black > white){
            winner = "black";
        }else if(black < white){
            winner = "white";
        }else if(black == white){
            winner = "tie";
        }else{
            System.out.println("There's a big math error here");
            System.exit(1);
        }

        return winner;
    }



    
    
    
    

    // // stuff from gomoku player
    // private void iterativeDeepening(ArrayList<Move> moves) {
    //     for (int _depth = 0; _depth < 1000000; _depth++) {
	// 		if (Instant.now().isBefore(endTime)) {
	// 			int v = alphaBeta(_depth);
	// 			if (v>best_move.getUtility()) { 
	// 				for (Move m : moves) {
	// 					if (v == m.getUtility()) {
    //                         best_move.overwrite(m);
	// 					}
	// 				}
	// 			}
	// 			// deepestDepth = _depth;
	// 		}else {
	// 			return;
	// 		}
	// 	}
    // }

    // private int alphaBeta(int maxDepth) {
    //     int v = 0;
	// 	v = maxValue(move, -1000000, 1000000, 0, maxDepth);
	// 	// if(!Instant.now().isBefore(endTime)){return v;}
	// 	return v;
    // }

    // /**
	//  * Calculates the highest utility move for this player and returns it
	//  * Used pseudo-code from the textbook, page 711
	//  * @param previousMove the move to test
	//  * @param max the maximum utility
	//  * @param min the minimum utility
	//  * @param currentDepth how deep the search has progressed
	//  * @return value of the best move for this player
	//  */
    // public int maxValue(Move previousMove, int max, int min, int currentDepth, int maxDepth) {
	// 	int v = -1000000;
	// 	for(Move m : legal_moves) {
	// 		if (currentDepth <= maxDepth && Instant.now().isBefore(endTime)){
	// 			m.calculateUtility();
	// 			m.setBoardState(previousMove.getResultingBoard());
	// 			m.updateBoardState(m.getRow(), m.getCol(), Bot.my_color.substring(0,1));
	// 			v = Math.max(v, minValue(m, max, min, currentDepth, maxDepth)); 
	// 			if (v>=min) {
	// 				return v;
	// 			}
	// 			max = Math.max(max, v);
	// 		}else {
	// 			return v;
	// 		}
	// 	}
	// 	return v;
    // }
    
    // /**
	//  * Calculates the highest utility move for the opponent and returns it
	//  * Used pseudo-code from the textbook, page 711
	//  * @param previousMove the move to test
	//  * @param max the maximum utility
	//  * @param min the minimum utility
	//  * @param currentDepth how deep the search has progressed
	//  * @return value of the best move for the opponent
	//  */
	// public  int minValue(Move previousMove, int max, int min, int currentDepth, int maxDepth) {
	// 	int v = 1000000;
	// 	for(Move m : legal_moves){
	// 		if (currentDepth <= maxDepth && Instant.now().isBefore(endTime)) {
	// 			m.calculateUtility();
	// 			m.setBoardState(previousMove.getResultingBoard());
	// 			m.updateBoardState(m.getRow(), m.getCol(), Bot.opponent_color.substring(0,1));
	// 			v = Math.min(v, maxValue(m, max, min, currentDepth+1, maxDepth));
	// 			if (v<=max) {
	// 				return v;
	// 			}
	// 			min = Math.min(min, v);
	// 		}else{
	// 			return v;
	// 		}
	// 	}
	// 	return v;
	// }
}