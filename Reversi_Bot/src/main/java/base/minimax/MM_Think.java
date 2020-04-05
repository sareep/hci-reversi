/**
 * @author Sam Reep
 * Scope: handles details of when it is the bot's turn to place a tile
 */
package base.minimax;

import java.time.Instant;
import java.util.ArrayList;

import base.GameState;
import base.Reversi_Bot;

@SuppressWarnings("unused")
public class MM_Think {

    private Boolean my_turn = false;
    private State best_move;
    private ArrayList<State> legal_moves = new ArrayList<State>();
    private String[][] original_board = new String[][]{};
    private Instant endTime;
    private static GameState origin;
    private State origin_s;


    public static String[] run(GameState origin_state) {
        origin = origin_state;
        return null;
    }


    /**
     * dlsReturn
     */
    public class DLSReturn {
        public State move;
        public boolean remaining;

        public DLSReturn(State m, boolean b){
            move = m;
            remaining = b;
        }
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
    public State getBestMove() {
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
    public ArrayList<State> getLegalMoves() {
        return new ArrayList<State>(this.legal_moves);
    }


    /********* LOGIC FUNCTIONS *********/

    public State getMove(String difficulty) {

        ArrayList<String> moveList;
        int timeLimit = 0;

        switch (difficulty) {
            case "easy":
                getRandomMove();
                break;

            case "medium":
                // ArrayList<Move> moveList = calculateLegalMoves(Reversi_Bot.my_color, original_board);
                timeLimit = 2;

            case "hard":
                moveList = origin.getLegalMoves();
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
    


    /********* MOVE-PICKING FUNCTIONS *********/

    /**
     * 
     * @return a random legal move
     */
	private State getRandomMove() {
        return this.legal_moves.get((int) (Math.random() * (legal_moves.size())));
    }




    //stuff from wikipedia
    private State IDDFS() {
        DLSReturn returnVal = new DLSReturn(null, false);
        for (int depth = 0; depth < 100000; depth++) {
            if(Instant.now().isBefore(endTime)){
                returnVal = DLS(origin_s, depth);
                
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

    private DLSReturn DLS(State move, int depth) {
        if(depth == 0){
            if(isTerminalMove(move) && (winner(move) == Reversi_Bot.my_color)){
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

    // TODO what if this was in the state? then it can stop calculating once it finds 1 move
    private boolean isTerminalMove(State m) {
        // ArrayList<State> moves_available = calculateLegalMoves(m.next_player.substring(0, 1), m.getResultingBoard()); // TODO change getResulting board to something that exists. create a new state?
        // return (moves_available.size() == 0);
        return true;
    }

    private String winner(State m) {
        String winner = "";
        int black = m.black_count;
        int white = m.white_count;

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
	// 			m.updateBoardState(m.getRow(), m.getCol(), Reversi_Bot.my_color.substring(0,1));
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
	// 			m.updateBoardState(m.getRow(), m.getCol(), Reversi_Bot.opponent_color.substring(0,1));
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