package base.reinforcement_learning;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.mdp.core.state.State;

/**
 * RL_StateFeatures
 */
public class RL_StateFeatures implements DenseStateFeatures {

    @Override
    public double[] features(State s) {
        return null;
    }

    @Override
    public DenseStateFeatures copy() {
        return null;
    }

    // TODO implement this class: create these features - black/white score, corners, sides
}