package base.reinforcement_learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import base.Reversi_Bot;
import base.Utils;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;

/**
 * Uses Reinforcement Learning to generate leraning episodes. Must be run on a
 * local machine, cannot store episodes on Heroku at present.
 */
public class Learner extends MDPSolver implements LearningAgent, QProvider {

    private int numEpsToRun;
    Map<HashableState, List<QValue>> qValues;
    QFunction qinit;
    double learningRate;
    Policy learningPolicy;

    // Default Learning parameters
    public Learner() {
        this(new RL_World().generateDomain(), 0.1, new SimpleHashableStateFactory(), new ConstantValueFunction(), 0.1,
                0.1);
        switch (Reversi_Bot.difficulty) {
            case "easy":
                numEpsToRun = 150;
                break;

            case "medium":
                numEpsToRun = 300;
                break;

            case "hard":
                numEpsToRun = 600;
                break;

            // TODO implement a custom setting with user input # of eps
            default:
                numEpsToRun = 10;
                break;
        }
    }

    public Learner(SADomain domain, double gamma, HashableStateFactory hashFact, QFunction qinit, double learningRate,
            double epsilon) {
        this.solverInit(domain, gamma, hashingFactory);
        this.qinit = qinit;
        this.hashingFactory = hashFact;
        this.learningRate = learningRate;
        this.qValues = new HashMap<HashableState, List<QValue>>();
        this.learningPolicy = new EpsilonGreedy(this, epsilon);
    }

    public void learn(String difficulty) {

        RL_State initialState = new RL_State();

        SimulatedEnvironment env = new SimulatedEnvironment(domain, initialState);
        // TODO get this one working so you can reward winners?
        // RL_Env env = new RL_Env(domain, initialState);

        for (int i = 0; i < this.numEpsToRun; i++) {
            /* Episode e = */this.runLearningEpisode(env);
            Utils.out("Finished episode " + i);

            /*
             * Commented out for now. Uncomment when implementing Apprenticeship or
             * something similar
             */
            // try {
            // e.write(Utils.PATH_TO_EPISODES + "ql_" + i);
            // } catch (Exception ex) {
            // Utils.err(ex.getMessage());
            // }

            // reset environment for next learning episode
            env.resetEnvironment();
        }

    }

    public String[] play(RL_State state) {
        Action a = this.learningPolicy.action(state);

        String[] move = a.actionName().substring(1).split("");
        return move;
    }

    // Below here was taken from an official BURLAP tutorial on Github. WIll be
    // tweaking it soon!

    @Override
    public Episode runLearningEpisode(Environment env) {
        return runLearningEpisode(env, -1);
    }

    @Override
    public Episode runLearningEpisode(Environment env, int maxSteps) {
        // initialize episode object with the initial state of the environment
        Episode e = new Episode(env.currentObservation());

        // behave until a terminal state or max steps is reached
        RL_State curState = (RL_State) env.currentObservation();
        int steps = 0;
        while (!env.isInTerminalState() && (steps < maxSteps || maxSteps == -1)) {

            // select an action
            Action a = this.learningPolicy.action(curState);

            // take the action and observe outcome
            EnvironmentOutcome eo = env.executeAction(a);

            // record result
            e.transition(eo);

            // get the max Q value of the resulting state if it's not terminal, 0 otherwise
            double maxQ = eo.terminated ? 0. : this.value(eo.op);

            // update the old Q-value
            QValue oldQ = this.storedQ(curState, a);
            oldQ.q = oldQ.q + this.learningRate * (eo.r + this.gamma * maxQ - oldQ.q); // TODO here's the value function

            // update state pointer to next environment state observed
            curState = (RL_State) eo.op;
            steps++;

        }

        return e;
    }

    protected QValue storedQ(State s, Action a) {
        // first get all Q-values
        List<QValue> qs = this.qValues(s);

        // iterate through stored Q-values to find a match for the input action
        for (QValue q : qs) {
            if (q.a.equals(a)) {
                return q;
            }
        }

        throw new RuntimeException("Could not find matching Q-value.");
    }

    @Override
    public double qValue(State s, Action a) {
        return storedQ(s, a).q;
    }

    @Override
    public double value(State s) {
        return QProvider.Helper.maxQ(this, s);
    }

    @Override
    public List<QValue> qValues(State s) {
        // first get hashed state
        HashableState sh = this.hashingFactory.hashState(s);

        // check if we already have stored values
        List<QValue> qs = this.qValues.get(sh);

        // create and add initialized Q-values if we don't have them stored for this
        // state
        if (qs == null) {
            List<Action> actions = this.applicableActions(s);
            qs = new ArrayList<QValue>(actions.size());

            // create a Q-value for each action
            for (Action a : actions) {
                // add q with initialized value
                qs.add(new QValue(s, a, this.qinit.qValue(s, a)));
            }
            // store this for later
            this.qValues.put(sh, qs);
        }

        return qs;
    }

    @Override
    public void resetSolver() {
        this.qValues.clear();
    }
}