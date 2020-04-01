package base.reinforcement_learning;

import burlap.mdp.core.action.Action;

public class MoveAction implements Action {
    private int actionId;

    public MoveAction(int actionId) {
        this.actionId = actionId;
    }

    @Override
    public String actionName() {
        return MoveActionType.BASE_ACTION_NAME + actionId;
    }

    @Override
    public Action copy() {
        return new MoveAction(actionId);
    }
}