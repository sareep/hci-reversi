/**
 * @author Sam Reep
 */
package base.minimax;

import base.Utils;

public class State {

    public int utility;
    public String next_player;
    public String[][] board;
    public int black_count = 0;
    public int white_count = 0;
    public String[][] legal_moves;

    public State() {
        board = new String[][] { 
            { " ", " ", " ", " ", " ", " ", " ", " " }, 
            { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " }, 
            { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", "w", "b", " ", " ", " " }, 
            { " ", " ", " ", "b", "w", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " }, 
            { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " } };
        black_count = 2;
        white_count = 2;
        next_player = "b";
    }

    /**
     * Constructor for State
     * 
     * @param parent the previous state to this one
     * @param row    the row at which who played to create this state
     * @param col    the column at which who played to create this state
     * @param who    the player who played to created this state
     */
    public State(State parent, int row, int col, String who) {
        // Screen inputs
        if (!parent.next_player.equals(who)) {
            Utils.err("Tried to play out of turn");
        }
        if (row > 7 || row < 0) {
            Utils.err("Tried to play off of the board at row: " + row);
        }
        if (col > 7 || col < 0) {
            Utils.err("Tried to play off of the board at col: " + col);
        }

        /* Populate fields */

        // Create updated board
        board = new String[parent.board.length][parent.board[0].length];
        for (int i = 0; i < parent.board.length; i++) {
            System.arraycopy(parent.board[i], 0, board[i], 0, board[0].length);
        }
        board[row][col] = who.substring(0, 1);

        // Store who should play next
        if (parent.next_player.equals("black")) {
            next_player = "white";
        } else if (parent.next_player.equals("white")) {
            next_player = "black";
        }

        // Count tiles for each player
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j].equals("b")) {
                    black_count++;
                } else if (board[i][j].equals("w")) {
                    white_count++;
                }
            }
        }

        // Find what moves are available
        legal_moves = calculateLegalMoves(next_player);

    }




    // TODO hijack these fuctions to count the number of tokens that would be flipped?

    /**
     * Find all legal moves for player 'who'
     * @param who
     * @return
     */
    private String[][] calculateLegalMoves(String who) {
        String[][] valid_moves = new String[][] { 
            { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " }, 
            { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " }, 
            { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " }, 
            { " ", " ", " ", " ", " ", " ", " ", " " },
            { " ", " ", " ", " ", " ", " ", " ", " " } };

        // For each coordinate, check a line in each direction
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j].equals(" ")) {
                    Boolean nw = valid_move(who, -1, -1, i, j, board);
                    Boolean nn = valid_move(who, -1, 0, i, j, board);
                    Boolean ne = valid_move(who, -1, 1, i, j, board);

                    Boolean ww = valid_move(who, 0, -1, i, j, board);
                    Boolean ee = valid_move(who, 0, 1, i, j, board);

                    Boolean sw = valid_move(who, 1, -1, i, j, board);
                    Boolean ss = valid_move(who, 1, 0, i, j, board);
                    Boolean se = valid_move(who, 1, 1, i, j, board);
                    if (nw || nn || ne || ww || ee || sw || ss || se) {
                        valid_moves[i][j] = who;
                    }
                }
            }
        }

        return valid_moves;
    }

    /**
     * Find if a certain (r,c) is a legal move
     * 
     * @param who   player placing a token
     * @param dr    north/south change
     * @param dc    east/west change
     * @param r     row of location being evaluated
     * @param c     column of location being evaluated
     * @param board the original board being evaluated
     * @return
     */
    private Boolean valid_move(String who, int dr, int dc, int r, int c, String[][] board) {
        String opp;
        switch (who) {
            case "b":
                opp = "w";
                break;
            case "w":
                opp = "b";
                break;
            default:
                Utils.out("Color problem: " + who);
                return false;
        }

        if ((r + dr < 0) || (r + dr > 7) || (r + dr + dr < 0) || (r + dr + dr > 7) || (c + dc < 0) || (c + dc > 7)
                || (c + dc + dc < 0) || (c + dc + dc > 7) || (!board[r + dr][c + dc].equals(opp))) {
            return false;
        }

        return check_line_match(who, dr, dc, r + dr + dr, c + dc + dc, board);
    }

    /**
     * Check if a certain line begins and ends with who's color
     * 
     * @param who   player placing a token
     * @param dr    north/south change
     * @param dc    east/west change
     * @param r     row of location being evaluated
     * @param c     column of location being evaluated
     * @param board the original board being evaluated
     * @return
     */
    private Boolean check_line_match(String who, int dr, int dc, int r, int c, String[][] board) {
        if (board[r][c].equals(who)) {
            return true;
        } else if ((r + dr < 0) || (r + dr > 7) || (c + dc < 0) || (c + dc > 7) || board[r][c].equals(" ")) {
            return false;
        } else {
            return check_line_match(who, dr, dc, r + dr, c + dc, board);
        }
    }

    /********* LOGIC *********/

    // private int score(String color) {
    // int score = 0;

    // for (int i = 0; i < board.length; i++) {
    // for (int j = 0; j < board[0].length; j++) {
    // if (board[row][col] == color) {
    // score++;
    // }
    // }
    // }

    // return score;
    // }

}