package base.reinforcement_learning;

// import burlap.mdp.core.state.MoveActionType;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;
import burlap.mdp.core.state.annotations.DeepCopyState;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.model.statemodel.SampleStateModel;

@SuppressWarnings("unused")
public class MoveActionType {

  //   public static String BASE_ACTION_NAME = "moveAction";

  //   @Override
  //   public String typeName() {
  //       return BASE_ACTION_NAME;
  //   }

  //   @Override
  // public Action associatedAction(String strRep) {
  //   return new MoveAction(0); //TODO: what's this
  // }

  // @Override
  // public List<Action> allApplicableActions(State state) {
  //   List<Action> applicableActions = new ArrayList<>();
  //   TicTacToeState tttState = (TicTacToeState)state;

  //   String gameStatus = (String)tttState.get(TicTacToeState.VAR_GAME_STATUS);
  //   if (gameStatus.equals(TicTacToeState.GAME_STATUS_IN_PROGRESS)) {
  //     String gameBoard = (String)tttState.get(TicTacToeState.VAR_GAME_BOARD);
  //     for (int i = 0; i < TicTacToeState.NUM_CELLS; i++) {
  //       if (gameBoard.charAt(i) == TicTacToeState.EMPTY) {
  //         applicableActions.add(new MoveAction(i));
  //       }
  //     }
  //   }
  //   return applicableActions;
  // }
}
