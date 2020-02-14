/**
 * @author Sam Reep
 * An object class that calculates and contains details for playing a tile at board[row][col]
 */
package bot;

public class Move {

    private int row;
    private int col;
    private int utility;
    private String who;
    private String[][] resulting_board;
    private int black_score;
    private int white_score;

    public Move(String who) {
        this.row = -1;
        this.col = -1;
        this.who = who;
        this.utility = -100000;
        this.black_score = score("b");
        this.white_score = score("w");
    }

    public Move(int row, int col, String who) {
        this.row = row;
        this.col = col;
        this.who = who;
        this.black_score = score("b");
        this.white_score = score("w");
    }

    public void overwrite(Move m) {
        this.row = m.getRow();
        this.col = m.getCol();
        this.utility = m.getUtility();
        this.resulting_board = m.getResultingBoard();
        this.black_score = m.getBlack_score();
        this.white_score = m.getWhite_score();
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

    public int getUtility() {
        return utility;
    }

    public void setUtility(int score) {
        this.utility = score;
    }

    public int getBlack_score() {
        return black_score;
    }

    public void setBlack_score(int black_score) {
        this.black_score = black_score;
    }

    public int getWhite_score() {
        return white_score;
    }

    public void setWhite_score(int white_score) {
        this.white_score = white_score;
    }
    
    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String[][] getResultingBoard() {
        String[][] copy = new String[this.resulting_board.length][this.resulting_board[0].length];
        for (int i = 0; i < resulting_board.length; i++) {
            System.arraycopy(this.resulting_board[i], 0, copy[i], 0, copy[0].length);
        }
        return copy;
    }

    public void setBoardState(String[][] boardState) {
        this.resulting_board = boardState;
    }

    public void updateBoardState(int row, int col, String who){
        this.resulting_board[row][col] = who;
        String[][] changing_board = getResultingBoard();
        flip_board(who.substring(0,1), row, col, changing_board);
    }

    /********* LOGIC *********/

	private int score(String color) {
        int score = 0;

        for (int i = 0; i < resulting_board.length; i++) {
            for (int j = 0; j < resulting_board[0].length; j++) {
                if (resulting_board[row][col] == color) {
                    score++;
                }
            }
        }

        return score;
    }
    
    private void flip_board(String who, int row, int col, String[][] board){
        
        flip_line(who, -1,-1,row,col,board);
        flip_line(who, -1, 0,row,col,board);
        flip_line(who, -1, 1,row,col,board);

        flip_line(who,  0,-1,row,col,board);
        flip_line(who,  0, 1,row,col,board);

        flip_line(who,  1,-1,row,col,board);
        flip_line(who,  1, 0,row,col,board);
        flip_line(who,  1, 1,row,col,board);

        this.resulting_board = board;

    }

    private boolean flip_line(String who, int dr, int dc, int r, int c, String[][] board){
        
        if( (r+dr < 0) || (r+dr > 7) || (c+dc < 0) || (c+dc > 7) || (board[r+dr][c+dc] == " ")){
            return false;
        }

        if(board[r+dr][c+dc] == who){
            return true;
        }else if(flip_line(who,dr,dc,r+dr,c+dc,board)){
            board[r+dr][c+dc] = who;
            return true;
        }else{
            return false;
        }
        
    }

    /**
     * 
     * @return the utility score of playing at board[row][col]
     */
    // TODO figure out how to include this immediate utility with the other utilities
	public void calculateUtility(){
        Integer score = 0;
		// TODO write all this; use snippet that calculates legal moves
		// Things to evalute: how many it will flip over now, how many it could flip in the future, could end game (with a win), is corner
		this.utility = score;
	}


}