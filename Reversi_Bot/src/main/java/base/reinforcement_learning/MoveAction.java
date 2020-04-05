package base.reinforcement_learning;

import burlap.mdp.core.action.Action;

public class MoveAction implements Action {
    private String actionId; // effectively Char[]: [row,col]
    private String row;
    private String col;

    public MoveAction(String actionId) {
        this.actionId = actionId;
        this.row = actionId.substring(0, 1);
        this.col = actionId.substring(1);
    }

    @Override
    public String actionName() {
        return MoveActionType.BASE_ACTION_NAME + actionId;
    }

    @Override
    public Action copy() {
        return new MoveAction(actionId);
    }

    public String getRow() {
        return row;
    }

    public String getCol() {
        return col;
    }
}