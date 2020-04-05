package base.reinforcement_learning;

public class Live_Thinking {


    public static String[] run() {
        String[] best_move = new String[]{};

        /** TODO my journal rq:
        * will have to read in the episodes using Episode.readEpisodes(path)
        * use the episodes to analyze the current board state FIND HOW TO DO THIS
        * pick best move
        * parse move into string[]
        */

        /**
         * What I think will work:
         * 1. QLearning qLearningAgent = new QLearning(RL_World.generateDomain(), double gamma, HashableStateFactory hashFact, double initialQ, double learningRate)
         * 2. EpsilonGreedy epGr = new EpsilonGreedy(qLearningAgent, double epsilon)
         * 3. TODO read in episodes?? or runLearningEpisodes if absolutely no other way 
         *      maybe use ApprenticeShipLearning, have it create a policy based on the stored episodes (then store this policy somewhere to avoid re-running
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
        
        return best_move;
    }
}