package base.reinforcement_learning;

import java.util.List;

import base.Reversi_Bot;
import base.Utils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;

/**
 * The actual player who does stuff!!
 */
@SuppressWarnings("unused")
public class Learner extends MDPSolver implements LearningAgent, QProvider {
    

    public void run(int numEpsToRun){
        double gamma = .99;
        double initialQ = 0.;
        double learningRate = 1.;

        RL_World world = new RL_World();
        SADomain domain = world.generateDomain();
        RL_State initialState = new RL_State();
        HashableStateFactory hashFact = new SimpleHashableStateFactory();
        LearningAgent agent = new QLearning(domain, gamma, hashFact, initialQ, learningRate);
        SimulatedEnvironment env = new SimulatedEnvironment(domain, initialState);


        for (int i = 0; i < numEpsToRun; i++) {
            Episode e = agent.runLearningEpisode(env);

            e.write(Utils.PATH_TO_EPISODES + "ql_" + i);

            // reset environment for next learning episode
            env.resetEnvironment();
        }


    }

    // TODO implement all of these below
    @Override
    public double qValue(State s, Action a) {
        return 0;
    }

    @Override
    public double value(State s) {
        return 0;
    }

    @Override
    public List<QValue> qValues(State s) {
        return null;
    }

    @Override
    public Episode runLearningEpisode(Environment env) {
        return null;
    }

    @Override
    public Episode runLearningEpisode(Environment env, int maxSteps) {
        return null;
    }

    @Override
    public void resetSolver() {
        
    }
}