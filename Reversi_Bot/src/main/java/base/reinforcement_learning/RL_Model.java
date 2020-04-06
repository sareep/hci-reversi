package base.reinforcement_learning;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.SampleModel;

public class RL_Model implements SampleModel {

    @Override
    public EnvironmentOutcome sample(State s, Action a) {        
        String who = a.actionName().substring(0,1);
        int row = Integer.parseInt(a.actionName().substring(1, 2));
        int col = Integer.parseInt(a.actionName().substring(2));

        RL_State child = new RL_State((RL_State) s, row, col, who);

        EnvironmentOutcome eo = new EnvironmentOutcome(s, a, child, child.utility, child.gameStatus.equals(RL_State.GAME_STATUS_TERMINAL));
        return eo;
    }

    @Override
    public boolean terminal(State s) {
        RL_State rs = (RL_State) s;
        return rs.gameStatus.equals(RL_State.GAME_STATUS_TERMINAL);
    }

}