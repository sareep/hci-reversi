/**
 * @author Sam Reep
 * An object class that calculates and contains details for playing a tile at board[row][col]
 */
package bot;

public class Move {

    private int row;
    private int col;
    private int score;
    private String who;
    private String[][] boardState;
    
    public Move(String who) {
        this.row = -1;
        this.col = -1;
        this.who = who;
        this.score = -100000;
    }

    public Move(int row, int col, String who) {
        this.row = row;
        this.col = col;
        this.who = who;
    }

    public void overwrite(Move m) {
        this.row = m.getRow();
        this.col = m.getCol();
        this.score = m.getScore();
        this.boardState = m.getBoardState();
    }
    
	/********* GETTERS/SETTERS *********/
    
    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    
    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String[][] getBoardState() {
        String[][] copy = new String[this.boardState.length][this.boardState[0].length];
        for (int i = 0; i < boardState.length; i++) {
            System.arraycopy(this.boardState[i], 0, copy[i], 0, copy[0].length);
        }
        return copy;
    }

    public void setBoardState(String[][] boardState) {
        this.boardState = boardState;
    }

    public void updateBoardState(int row, int col, String who){
        this.boardState[row][col] = who;
    }

    /********* LOGIC *********/

    /**
     * 
     * @return the utility score of playing at board[row][col]
     */
	public void calculateUtility(){
		Integer score = 0;
		// TODO write all this; use snippet that calculates legal moves
		// Things to evalute: how many it will flip over now, how many it could flip in the future, could end game (with a win), is corner
		this.score = score;
	}


}