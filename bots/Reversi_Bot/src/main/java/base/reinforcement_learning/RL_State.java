package base.reinforcement_learning;

import java.util.Arrays;
import java.util.List;

import base.GameState;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;
import burlap.mdp.core.state.annotations.ShallowCopyState;

/**
 * Represents the state of the game - board, whose turn, etc
 */

@ShallowCopyState
public class RL_State extends GameState implements MutableState {

    public final static String VAR_GAME_BOARD = "board";
    public final static String VAR_GAME_STATUS = "gameStatus";
    private final static List<Object> keys = Arrays.<Object>asList(VAR_GAME_BOARD, VAR_GAME_STATUS);

    public RL_State() {
        super();
    }

    public RL_State(String[][] board, String next_player) {
        this.board = board;
        this.next_player = next_player;
        countTiles();
        this.move_list = calculateLegalMoves(next_player, board);
    }

    public RL_State(GameState parent, int row, int col, String who) {
        super(parent, row, col, who);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        if (variableKey.equals(VAR_GAME_BOARD)) {
            this.board = (String[][]) value;
        } else if (variableKey.equals(VAR_GAME_STATUS)) {
            this.gameStatus = (String) value;
        } else {
            throw new UnknownKeyException(variableKey);
        }
        return this;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if (variableKey.equals(VAR_GAME_BOARD)) {
            return this.board;
        } else if (variableKey.equals(VAR_GAME_STATUS)) {
            return this.gameStatus;
        }
        throw new UnknownKeyException(variableKey);
    }

    @Override
    public RL_State copy() {
        return new RL_State(board, next_player);
    }

    @Override
    public String toString() {
        return StateUtilities.stateToString(this);
    }
}
