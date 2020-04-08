package base.reinforcement_learning;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.SimpleAction;
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
        if (gameStatus.equals(RL_State.GAME_STATUS_CONTINUING)) {
            // String[][] moves = RL_State.calculateLegalMoves(reversiState.next_player,
            // reversiState.board);
            // for (int i = 0; i < moves.length; i++) {
            // for (int j = 0; j < moves.length; j++) {
            // // applicableActions.add(new MoveAction(i, j));
            // applicableActions.add(new
            // SimpleAction(reversiState.next_player.substring(0,1)+i+""+j));
            // }
            // }
            ArrayList<String> moves = RL_State.calculateLegalMoves(reversiState.next_player, reversiState.board);
            for (String m : moves) {
                applicableActions.add(new SimpleAction(
                        reversiState.next_player + m.substring(0, 1) + m.substring(1)));
            }

        }
        return applicableActions;
    }

    @Override
    public Action associatedAction(String strRep) {
        return new SimpleAction(strRep);
        // return new MoveAction(Integer.parseInt(strRep.substring(0, 1)),
        // Integer.parseInt(strRep.substring(1)));
    }
}
