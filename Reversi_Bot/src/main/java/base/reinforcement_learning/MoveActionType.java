package base.reinforcement_learning;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;
import burlap.mdp.core.state.annotations.DeepCopyState;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.model.statemodel.SampleStateModel;

@SuppressWarnings("unused")
public class MoveActionType implements ActionType {

  public static String BASE_ACTION_NAME = "moveAction";

  @Override
  public String typeName() {
    return BASE_ACTION_NAME;
  }

  @Override
  public Action associatedAction(String strRep) {
    return new MoveAction(0); // TODO: what's this
  }

  @Override
  public List<Action> allApplicableActions(State state) {
    List<Action> applicableActions = new ArrayList<>();
    RL_State revState = (RL_State) state;

    String gameStatus = (String) revState.get(RL_State.VAR_GAME_STATUS);
    if (gameStatus.equals(RL_State.GAME_STATUS_IN_PROGRESS)) {
      String gameBoard = (String) revState.get(RL_State.VAR_GAME_BOARD);
      for (int i = 0; i < RL_State.NUM_CELLS; i++) {
        if (gameBoard.charAt(i) == RL_State.EMPTY) {
          applicableActions.add(new MoveAction(i));
        }
      }
    }
    return applicableActions;
  }
}
