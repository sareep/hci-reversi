package base.reinforcement_learning;

import base.Utils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;

/**
 * just visualizer? or does it do anything useful
 */
public class RL_World implements DomainGenerator {



    public static int maxGames;

    /**
     * 
     * @param args [maxGames]
     * 
     */
    public static void run(String args[]) {
        RL_World world = new RL_World();
        SADomain dom = world.generateDomain();
        HashableStateFactory hashFact = new SimpleHashableStateFactory();
        LearningAgent agent = new QLearning(dom, .9 /* ? */, hashFact, 0.0 /* ? */, 1.0 /* ? */);
        RL_Env env = new RL_Env();

        for (int i = 0; i < maxGames; i++) {
            Episode e = agent.runLearningEpisode(env);

            e.write(Utils.PATH_TO_EPISODES + "ql_" + i);

            // reset environment for next learning episode
            env.resetEnvironment();
        }
    }

    @Override
    public SADomain generateDomain() {
        SADomain domain = new SADomain();
        domain.addActionType(new MoveActionType());
        return domain;
    }
}