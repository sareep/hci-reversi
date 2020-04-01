package base.reinforcement_learning;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

public class RL_Env implements Environment {

    private RL_State currentObservationState;

    @Override
    public State currentObservation() {
        return currentObservationState;
    }

    @Override
    public EnvironmentOutcome executeAction(Action action) {
        return null;
    }

    @Override
    public double lastReward() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isInTerminalState() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void resetEnvironment() {
        // TODO Auto-generated method stub

    }

}
