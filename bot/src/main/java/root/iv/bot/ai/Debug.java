package root.iv.bot.ai;

import root.iv.bot.Move;

public class Debug {
    private static final Boolean T = Boolean.TRUE;
    private static final Boolean F = Boolean.FALSE;
    public static void main(String[] args){
        Main ai = new Main();
        Main ai2 = new Main();
        ai.init(true, 9, 5, (m,b)-> {
            System.out.println("White turn: "+m);
            printBoard(b, true);
        }, b-> System.out.println("White lost"), Behaviour.LONGEST);
        ai2.init(false, 9, 5, (m,b)-> {
            System.out.println("Black turn: "+m);
            printBoard(b, false);
        }, b-> System.out.println("Black lost"), Behaviour.NO_THINKING);
        Move m;
        for(int i=0;;++i){
            m = ai.takeTurn();
            if(m==null){
                return;
            }
            ai2.acceptTurn(m);
            m = ai2.takeTurn();
            if(m==null){
                return;
            }
            ai.acceptTurn(m);
            if(i>10000){
                return;
            }
        }
    }
    public static void printBoard(Boolean[][] board, boolean white){
        for(int i=0; i<board.length; ++i){
            for(int j=0; j<board[i].length; ++j){
                if(board[i][j]==T){
                    System.out.print(white ? 'W' : 'B');
                }else if(board[i][j]==F){
                    System.out.print(white ? 'B' : 'W');
                }else{
                    System.out.print(' ');
                }
            }
            System.out.println("");
        }
    }
}
