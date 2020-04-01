package base;

/**
 * Utils
 */
public class Utils {

	/**** Game Logic ****/

	/**
     * Flips sandwiched tiles given who's play at (row, column)
     * @param who the player placing a tile
     * @param row the row at which who plays
     * @param col the column at which who plays
     * @param board
     * @return
     */
    public String[][] flip_board(String who, int row, int col, String[][] board){
        String[][] new_board = new String[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, new_board[i], 0, new_board[0].length);
        }

        flip_line(who, -1,-1,row,col,new_board);
        flip_line(who, -1, 0,row,col,new_board);
        flip_line(who, -1, 1,row,col,new_board);

        flip_line(who,  0,-1,row,col,new_board);
        flip_line(who,  0, 1,row,col,new_board);

        flip_line(who,  1,-1,row,col,new_board);
        flip_line(who,  1, 0,row,col,new_board);
        flip_line(who,  1, 1,row,col,new_board);

        return new_board;

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

	/**** Print shortcuts ****/
	
	public static void out(String message) {
		Utils.print(message, "out");
	}

	public static void err(String message) {
		Utils.print(message, "err");
	}

	/**
	 * Title each message for easier debugging on server
	 * @param message
	 * @param type
	 */
	public static void print(String message, String type) {
		String output = "**" + Reversi_Bot.username + ": ";
		if (Reversi_Bot.port == 8080) {
			message += "\n";
		}
	
		switch (type) {
			case "out":
				output += message;
				System.out.print(output);
				break;
			case "err":
				output += "ERROR: ";
				output += message;
				System.err.print(output);
				break;
			default:
				break;
		}
	}

    
}