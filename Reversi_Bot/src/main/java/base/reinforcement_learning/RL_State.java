package base.reinforcement_learning;

import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;
import burlap.mdp.core.state.annotations.DeepCopyState;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.model.statemodel.SampleStateModel;

import java.util.Arrays;
import java.util.List;

/**
 * GameState2
 * 
 * 
 * gonna base my implementation on this guy's:
 * https://github.com/JavaFXpert/tic-tac-toe-rl
 */
@SuppressWarnings("unused")

@DeepCopyState
public class RL_State implements MutableState {

  public final static String VAR_GAME_BOARD = "gameBoard";
  public final static String VAR_GAME_STATUS = "gameStatus";

  public static char BLACK_TILE = 'b';
  public static char WHITE_TILE = 'w';
  public static char EMPTY_TILE = ' ';

  public String gameBoard;
  public String gameStatus;

  private final static List<Object> keys = Arrays.<Object>asList(VAR_GAME_BOARD, VAR_GAME_STATUS);
  public static final Object GAME_STATUS_IN_PROGRESS = null;
  public static final int NUM_CELLS = 0;
  public static final char EMPTY = 0;

  public RL_State() {

  }

  public RL_State(String gameBoard, String gameStatus) {
    this.gameBoard = gameBoard;
    this.gameStatus = gameStatus;
  }

  @Override
  public MutableState set(Object variableKey, Object value) {
    if (variableKey.equals(VAR_GAME_BOARD)) {
      this.gameBoard = (String) value;
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
      return this.gameBoard;
    } else if (variableKey.equals(VAR_GAME_STATUS)) {
      return this.gameStatus;
    }
    throw new UnknownKeyException(variableKey);
  }

  @Override
  public RL_State copy() {
    return new RL_State(gameBoard, gameStatus);
  }
}
