package base.reinforcement_learning;

import java.util.List;

import base.Reversi_Bot;
import base.Utils;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learnfromdemo.IRLRequest;
import burlap.behavior.singleagent.learnfromdemo.mlirl.commonrfs.LinearStateDifferentiableRF;
import burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.DifferentiableSparseSampling;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;

@SuppressWarnings("unused")
public class RL_Think {

    public static Episode ep = new Episode();
    public Policy policy;
    public RL_Env env;

    /**
     * creates a IRL Bot based on episodes previously played
     * @return
     */
    public static void setup() {

        RL_World world = new RL_World();
        SADomain domain = world.generateDomain();
        List<Episode> episodes = Episode.readEpisodes(Utils.PATH_TO_EPISODES);

        // //create reward function features to use
		// RL_StateFeatures features  = null;//= new RL_StateFeatures(domain, 5); // TODO implement this

		// //create a reward function that is linear with respect to those features and has small random
		// //parameter values to start
		// LinearStateDifferentiableRF rf = new LinearStateDifferentiableRF(features, 5); // TODO how to use this
		// // for(int i = 0; i < rf.numParameters(); i++){
		// // 	rf.setParameter(i, RandomFactory.getMapped(0).nextDouble()*0.2 - 0.1);
		// // }
        // double beta = 10; //idk what this does yet
        // DifferentiableSparseSampling planner = new DifferentiableSparseSampling(domain, rf, 0.99, new SimpleHashableStateFactory(), 10, -1, beta);
		// planner.toggleDebugPrinting(false);

        // IRLRequest request = new IRLRequest(domain, planner, episodes);

        /** TODO use the episodes to analyze the current board state FIND HOW TO DO THIS
        */

        /**
         * What I think will work:
         * 1. QLearning qLearningAgent = new QLearning(RL_World.generateDomain(), double gamma, HashableStateFactory hashFact, double initialQ, double learningRate)
         * 2. EpsilonGreedy epGr = new EpsilonGreedy(qLearningAgent, double epsilon)
         * 3. read in episodes
         *      use ApprenticeShipLearning, IRLRequest, or MLIRL, have it create a policy based on the stored episodes (then store this policy somewhere to avoid re-running
         * )
         * 4. Action best_action = EpGr.action(State currentState); //need to check that currentState is in the policy, if not play a random move (and for hard, add this to the learned episodes)
         * 5. String[] best_move = new String[]{best_action.getRow(), best_action.getCol()};
         *
         * OR
         *  
         * 1. QLearning qLearningAgent = new QLearning(RL_World.generateDomain(), double gamma, HashableStateFactory hashFact, double initialQ, double learningRate)
         * 2. Policy policy = 
         * 3. qLearningAgent.setLearningPolicy(policy);
         */
        
    }
    
    public String[] getMove(){
        String[] best_move = new String[]{};
        
        // PolicyUtils.followAndRecordPolicy(policy, env, ep);
        // convert that move into String[]{row, col}
        return best_move;
    }
}