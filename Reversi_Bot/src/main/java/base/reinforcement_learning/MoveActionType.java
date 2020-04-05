package base.reinforcement_learning;

import java.util.ArrayList;
import java.util.List;

import base.Utils;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;

public class MoveActionType implements ActionType {

    public static String BASE_ACTION_NAME = "moveAction";

    @Override
    public String typeName() {
        return BASE_ACTION_NAME;
    }

    @Override
    public List<Action> allApplicableActions(State state) {
        List<Action> applicableActions = new ArrayList<>();
        RL_State reversiState = (RL_State) state;

        String gameStatus = (String) reversiState.get(RL_State.VAR_GAME_STATUS);
        if (gameStatus.equals(RL_State.GAME_STATUS_IN_PROGRESS)) {
            String[][] moves = Utils.calculateLegalMoves(reversiState.whoseTurn, reversiState.gameBoard);
            for (int i = 0; i < moves.length; i++) {
                for (int j = 0; j < moves.length; j++) {
                    applicableActions.add(new MoveAction(i, j));
                }
            }
        }
        return applicableActions;
    }

    @Override
    public Action associatedAction(String strRep) {
        return new MoveAction(Integer.parseInt(strRep.substring(0, 1)), Integer.parseInt(strRep.substring(1)));
    }
}
