package base.reinforcement_learning;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;

/**
 * BurlapWorld
 */
@SuppressWarnings("unused")

public class ReversiWorld {//implements DomainGenerator {



    // public static int maxGames;

    // /**
    //  * 
    //  * @param args [maxGames]
    //  * 
    //  */
    // public static void run(String args[]) {
    //     ReversiWorld world = new ReversiWorld();
    //     SADomain dom = world.generateDomain();
    //     HashableStateFactory hashFact = new SimpleHashableStateFactory();
    //     LearningAgent agent = new QLearning(dom, .9 /* ? */, hashFact, 0.0 /* ? */, 1.0 /* ? */);
    //     ReversiEnv env = new ReversiEnv();

    //     String outputPath = "output/";
    //     for (int i = 0; i < maxGames; i++) {
    //         Episode e = agent.runLearningEpisode(env);

    //         e.write(outputPath + "ql_" + i);

    //         // reset environment for next learning episode
    //         env.resetEnvironment();
    //     }
    // }

    // @Override
    // public SADomain generateDomain() {
    //     SADomain domain = new SADomain();

    //     domain.addActionType(new MoveActionType());
    //     return domain;
    // }
}