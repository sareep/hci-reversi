package base.reinforcement_learning;

import base.Utils;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * Represents an entire game/set of games
 */
@SuppressWarnings("unused")
public class RL_Env implements Environment {

    private RL_State currentObservationState;
    private String gameState;
    private String[][] board;
    private double reward;
    private boolean isTerminal;

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
        return reward;
    }

    @Override
    public boolean isInTerminalState() {
        return isTerminal;
    }

    @Override
    public void resetEnvironment() {
        board = Utils.newBoard();
    }

}
