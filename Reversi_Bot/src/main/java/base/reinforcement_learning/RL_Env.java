package base.reinforcement_learning;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * Represents an entire game/set of games
 */
// TODO make sure this is all implemented fine
@SuppressWarnings("unused")
public class RL_Env implements Environment {

    private RL_State currentObservationState;
    private String gameStatus;
    private String[][] board;
    private double reward;
    private boolean isTerminal;

    @Override
    public State currentObservation() {
        return currentObservationState;
    }

    @Override
    public EnvironmentOutcome executeAction(Action action) {
        String who = action.actionName().substring(0,1);
        int row = Integer.parseInt(action.actionName().substring(1, 2));
        int col = Integer.parseInt(action.actionName().substring(2));

        RL_State parent = new RL_State(board, who);

        RL_State child = new RL_State(parent, row, col, who);

        EnvironmentOutcome eo = new EnvironmentOutcome(parent, action, child, child.utility, child.gameStatus.equals(RL_State.GAME_STATUS_TERMINAL));
        return eo;
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
        board = RL_State.newBoard();
        gameStatus = RL_State.GAME_STATUS_CONTINUING;
    }

}
