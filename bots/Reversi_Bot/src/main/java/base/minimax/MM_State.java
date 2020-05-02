/**
 * @author Sam Reep
 */
package base.minimax;

import base.GameState;
import base.Utils;

public class MM_State extends GameState {

    public MM_State() {
        super();
    }

    /**
     * For creating States from server game_update payloads
     * 
     * @param board the board given by the server
     */
    public MM_State(String[][] board, String next_player) {
        this.next_player = next_player;

        this.board = board;

        // Count tiles for each player
        countTiles();

        // Find what moves are available
        move_list = calculateLegalMoves(next_player, board);

        gameStatus = move_list.size() > 0 ? GAME_STATUS_CONTINUING : GAME_STATUS_TERMINAL;
    }

    public MM_State(MM_State parent, int row, int col, String who) {
        super(parent, row, col, who);
    }

    public int getScore(String who) {
        if (who.equals(MM_State.BLACK)) {
            return this.black_count;
        } else if (who.equals(MM_State.WHITE)) {
            return this.white_count;
        } else {
            Utils.shut_down(1, "Bad color: " + who);
            return -1;
        }
    }

}