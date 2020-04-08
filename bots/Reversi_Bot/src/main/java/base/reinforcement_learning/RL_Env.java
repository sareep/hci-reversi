package base.reinforcement_learning;

import java.util.LinkedList;
import java.util.List;

import base.GameState;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.extensions.EnvironmentObserver;
import burlap.mdp.singleagent.environment.extensions.EnvironmentServerInterface;

/**
 * Represents an entire game/set of games
 */
@SuppressWarnings("unused")
public class RL_Env implements Environment, EnvironmentServerInterface {
    private static int WIN_REWARD = 1000;
    private static int LOSE_REWARD = -1000;
    private static int MOVE_REWARD = -10;

    private RL_State currentObservationState;
    private String gameStatus;
    private String winner;
    private String[][] board;
    private double reward = 0;

    protected List<EnvironmentObserver> observers = new LinkedList<EnvironmentObserver>();

    public RL_Env(SADomain domain, RL_State initialState) {
        currentObservationState = initialState;
        board = RL_State.newBoard();
        gameStatus = RL_State.GAME_STATUS_CONTINUING;
    }

    @Override
    public State currentObservation() {
        return currentObservationState;
    }

    @Override
    public EnvironmentOutcome executeAction(Action a) {
        
        String[] action = a.actionName().split("");
        String who = action[0];
        int row = Integer.parseInt(action[1]);
        int col = Integer.parseInt(action[2]);

        RL_State parent = new RL_State(board, who);

        RL_State child = new RL_State(parent, row, col, who);
        currentObservationState = child;
        reward = MOVE_REWARD;

        boolean terminal = child.gameStatus.equals(GameState.GAME_STATUS_TERMINAL);
        if (terminal) {
            int score_difference = 0;

            if (child.black_count > child.white_count) {
                this.winner = GameState.BLACK;
                score_difference = child.black_count - child.white_count;
            } else if (child.black_count < child.white_count) {
                this.winner = GameState.WHITE;
                score_difference = child.white_count - child.black_count;
            } else {
                this.winner = "tie";
            }

            if (who.equals(winner)) {
                reward = WIN_REWARD + score_difference;
            } else if (winner.equals("tie")) {
            } else {
                reward = LOSE_REWARD - score_difference;
            }
        }

        EnvironmentOutcome eo = new EnvironmentOutcome(parent, a, child, reward, terminal);
        return eo;
    }

    @Override
    public double lastReward() {
        return reward;
    }

    @Override
    public boolean isInTerminalState() {
        return currentObservationState.gameStatus.equals(GameState.GAME_STATUS_TERMINAL);
    }

    @Override
    public void resetEnvironment() {
        currentObservationState = new RL_State();
        board = RL_State.newBoard();
        gameStatus = RL_State.GAME_STATUS_CONTINUING;
    }

    @Override
    public void addObservers(EnvironmentObserver... observers) {
        for (EnvironmentObserver o : observers) {
            this.observers.add(o);
        }
    }

    @Override
    public void clearAllObservers() {
        this.observers.clear();
    }

    @Override
    public void removeObservers(EnvironmentObserver... observers) {
        for (EnvironmentObserver o : observers) {
            this.observers.remove(o);
        }
    }

    @Override
    public List<EnvironmentObserver> observers() {
        return this.observers;
    }

}
