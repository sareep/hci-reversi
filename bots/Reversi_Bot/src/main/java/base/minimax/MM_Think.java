/**
 * @author Sam Reep
 * Scope: handles details of when it is the bot's turn to place a tile
 */
package base.minimax;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import base.GameState;
import base.Reversi_Bot;
import base.Utils;

// TODO Misc Cleanup
@SuppressWarnings("unused")
public class MM_Think {

    private Instant startTime;
    private Instant endTime;
    private static MM_State origin;
    private MM_State currentState;
    private MM_State origin_s;

    public static String[] run(GameState origin_state) {
        origin = (MM_State) origin_state;

        String[] best_move;
        // int timeLimit = 0;

        switch (Reversi_Bot.difficulty) {
            case "easy":
                Utils.out("Playing Random");
                best_move = origin.getRandomMove().split("");
                break;

            case "medium":
                // Greedy First
                Utils.out("Playing Greedy");
                best_move = simpleGreedyFirst(origin);
                // timeLimit = 2;
                break;

            case "hard":
                // if (timeLimit == 0) {
                // timeLimit = 4;
                // }
                // endTime = Instant.now().plusSeconds(timeLimit);
                // IDDFS();

            default:
                Utils.out("Playing Random");
                best_move = origin.getRandomMove().split("");
        }

        return best_move;
    }

    /**
     * Picks the move that will immediately flip the most tiles
     */
    private static String[] simpleGreedyFirst(MM_State state) {
        String[] greediestMove = null;
        int greediestCount = 0;
        for (String moveString : state.move_list) {
            String[] move = moveString.split("");
            MM_State child = new MM_State(origin, Integer.parseInt(move[0]), Integer.parseInt(move[1]),
                    origin.next_player);
            int originalCount = origin.getScore(Reversi_Bot.my_color);
            int newCount = child.getScore(Reversi_Bot.my_color);
            int tilesFlipped = newCount - originalCount;
            if (tilesFlipped > greediestCount) {
                greediestCount = tilesFlipped;
                greediestMove = move;
            }
        }
        return greediestMove;
    }

    /**
     * dlsReturn //
     */
    // public class DLSReturn {
    // public MM_State move;
    // public boolean remaining;

    // public DLSReturn(MM_State m, boolean b){
    // move = m;
    // remaining = b;
    // }
    // }

    /********* LOGIC FUNCTIONS *********/

    // public String getMove(String difficulty) {
    // String best_move;
    // // int timeLimit = 0;

    // switch (difficulty) {
    // case "easy":
    // best_move = getRandomMove();
    // break;

    // case "medium":
    // // Greedy First
    // best_move =
    // // timeLimit = 2;

    // case "hard":
    // // if (timeLimit == 0) {
    // // timeLimit = 4;
    // // }
    // // endTime = Instant.now().plusSeconds(timeLimit);
    // // IDDFS();

    // default:
    // getRandomMove();
    // }

    // return best_move;
    // }

    /********* MOVE-PICKING FUNCTIONS *********/

    // stuff from wikipedia
    // TODO rename and finish up
    // private MM_State IDDFS() {
    // DLSReturn returnVal = new DLSReturn(null, false);
    // for (int depth = 0; depth < 100000; depth++) {
    // if(Instant.now().isBefore(endTime)){
    // returnVal = DLS(origin_s, depth);

    // if(returnVal.move != null){
    // return returnVal.move;
    // }else if(!returnVal.remaining){
    // return null;//TODO return the next best move instead
    // }
    // }else{
    // break;
    // }
    // }

    // //return best move found so far
    // return returnVal.move;
    // }

    // private DLSReturn DLS(MM_State move, int depth) {
    // if(depth == 0){
    // if(isTerminalMove(move) && (winner(move) == Reversi_Bot.my_color)){
    // return new DLSReturn(move, true);
    // }else{
    // return new DLSReturn(null, true);
    // }
    // }else if(depth > 0){
    // boolean any_remaining = false;
    // //TODO recursively loop over children states (while loop with var outside
    // that gets updated?)
    // do{

    // }while(any_remaining);
    // }

    // return new DLSReturn(null, false);
    // }

    // TODO Depricated, remove

    // // stuff from gomoku player
    // private void iterativeDeepening(ArrayList<Move> moves) {
    // for (int _depth = 0; _depth < 1000000; _depth++) {
    // if (Instant.now().isBefore(endTime)) {
    // int v = alphaBeta(_depth);
    // if (v>best_move.getUtility()) {
    // for (Move m : moves) {
    // if (v == m.getUtility()) {
    // best_move.overwrite(m);
    // }
    // }
    // }
    // // deepestDepth = _depth;
    // }else {
    // return;
    // }
    // }
    // }

    // private int alphaBeta(int maxDepth) {
    // int v = 0;
    // v = maxValue(move, -1000000, 1000000, 0, maxDepth);
    // // if(!Instant.now().isBefore(endTime)){return v;}
    // return v;
    // }

    // /**
    // * Calculates the highest utility move for this player and returns it
    // * Used pseudo-code from the textbook, page 711
    // * @param previousMove the move to test
    // * @param max the maximum utility
    // * @param min the minimum utility
    // * @param currentDepth how deep the search has progressed
    // * @return value of the best move for this player
    // */
    // public int maxValue(Move previousMove, int max, int min, int currentDepth,
    // int maxDepth) {
    // int v = -1000000;
    // for(Move m : legal_moves) {
    // if (currentDepth <= maxDepth && Instant.now().isBefore(endTime)){
    // m.calculateUtility();
    // m.setBoardState(previousMove.getResultingBoard());
    // m.updateBoardState(m.getRow(), m.getCol(),
    // Reversi_Bot.my_color.substring(0,1));
    // v = Math.max(v, minValue(m, max, min, currentDepth, maxDepth));
    // if (v>=min) {
    // return v;
    // }
    // max = Math.max(max, v);
    // }else {
    // return v;
    // }
    // }
    // return v;
    // }

    // /**
    // * Calculates the highest utility move for the opponent and returns it
    // * Used pseudo-code from the textbook, page 711
    // * @param previousMove the move to test
    // * @param max the maximum utility
    // * @param min the minimum utility
    // * @param currentDepth how deep the search has progressed
    // * @return value of the best move for the opponent
    // */
    // public int minValue(Move previousMove, int max, int min, int currentDepth,
    // int maxDepth) {
    // int v = 1000000;
    // for(Move m : legal_moves){
    // if (currentDepth <= maxDepth && Instant.now().isBefore(endTime)) {
    // m.calculateUtility();
    // m.setBoardState(previousMove.getResultingBoard());
    // m.updateBoardState(m.getRow(), m.getCol(),
    // Reversi_Bot.opponent_color.substring(0,1));
    // v = Math.min(v, maxValue(m, max, min, currentDepth+1, maxDepth));
    // if (v<=max) {
    // return v;
    // }
    // min = Math.min(min, v);
    // }else{
    // return v;
    // }
    // }
    // return v;
    // }
}