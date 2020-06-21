package root.iv.bot.ai;

import root.iv.bot.Move;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Main {
    private static final Boolean T = Boolean.TRUE;
    private static final Boolean F = Boolean.FALSE;
    public Boolean[][] board;
    public Behaviour behaviour;
    private int dimX;
    private int dimY;
    private BiConsumer<Move,Boolean[][]> afterMove=null;
    private Consumer<Boolean[][]> afterLoss=null;

    public Main init(boolean first, int dimX, int dimY,
                     BiConsumer<Move,Boolean[][]> afterMove,
                     Consumer<Boolean[][]> afterLoss,
                     Behaviour behaviour){
        this.behaviour = behaviour;
        this.afterLoss=afterLoss;
        this.afterMove=afterMove;
        this.dimX=dimX;
        this.dimY=dimY;
        this.board = new Boolean[dimY][dimX];
        for(int i=0; i<((dimY-1)/2); ++i){
            for(int j=0; j<dimX; ++j){
                board[i][j]= first;
            }
        }
        {
            int i=(dimY-1)/2;
            for(int j=0; j<((dimX-1)/2); ++j){
                board[i][j]= j % 2 == (first ? 0 : 1);
            }
            board[i][(dimX-1)/2] = null;
            for(int j=((dimX+1)/2); j<dimX; ++j){
                board[i][j]= j % 2 != (first ? 0 : 1);
            }
        }
        for(int i=((dimY+1)/2); i<dimY; ++i){
            for(int j=0; j<dimX; ++j){
                board[i][j]= !first;
            }
        }
        return this;
    }

    public Move takeTurn(){
        List<Move> moves = new ArrayList<>();
        for(int i=0; i<board.length; ++i){
            for(int j=0; j<board[i].length; ++j){
                if(board[i][j]!=T){
                    continue;
                }
                moves.addAll(lsMoves(i,j, board, Collections.emptyList(), true));
            }
        }
        if(moves.stream().anyMatch(Move::eating)){
            moves = moves.stream().filter(Move::eating).collect(Collectors.toList());
        }
        if(moves.isEmpty()){
            emitLoss();
            return null;
        }
        Move m = behaviour.pick(moves, board);
        emitTurn(m);
        return m;
    }

    public void acceptTurn(Move m){
        renderTurn(m, F, board);
    }

    private List<Move> lsMoves(int y, int x, Boolean[][] board, List<int[]> restricted, boolean canHungry){
        List<Move> res = new ArrayList<>();
        for(int i=y-1; i<=y+1; ++i){
            if(i<0 || i>=dimY){
                continue;
            }
            for(int j=x-1; j<=x+1; ++j){
                if(j<0 || j>=dimX){
                    continue;
                }
                final int i_=i;
                final int j_=j;
                if(restricted.stream().anyMatch(it-> it[0]==i_ && it[1]==j_)){
                    continue;
                }
                if(board[i][j]==null){
                    Move m = new Move(y,x,i-y, j-x);
                    boolean eats = false;
                    if(!(y+2*m.dy <0 || y+2*m.dy >=dimY || x+2*m.dx <0 || x+2*m.dx >=dimX)){
                        if(board[y+2*m.dy][x+2*m.dx]==F){
                            eats = true;
                            List<int[]> restricted2 = new ArrayList<>(restricted);
                            int[] restriction = {i,j};
                            restricted2.add(restriction);
                            Move mm = m.cpWithEatsAtk();
                            res.add(mm);
                            res.addAll(expandContinuations(mm, board, restricted2));
                        }
                    }
                    if(!(y-m.dy <0 || y-m.dy >=dimY || x-m.dx <0 || x-m.dx >=dimX)){
                        if(board[y-m.dy][x-m.dx]==F){
                            eats = true;
                            List<int[]> restricted2 = new ArrayList<>(restricted);
                            int[] restriction = {i,j};
                            restricted2.add(restriction);
                            Move mm = m.cpWithEatsWth();
                            res.add(mm);
                            res.addAll(expandContinuations(mm, board, restricted2));
                        }
                    }
                    if(!eats && canHungry){
                        res.add(m);
                    }
                }
            }
        }
        return res;
    }
    private List<Move> expandContinuations(Move m, Boolean[][] board, List<int[]> restricted){
        Boolean[][] boardPred = Arrays.stream(board).map(Boolean[]::clone).toArray(Boolean[][]::new);
        return lsMoves(m.y +m.dy, m.x +m.dx, boardPred, restricted, true).stream()
                .filter(Move::eating)
                .map(m::cpWithCont)
                .collect(Collectors.toList());
    }

    private void emitTurn(Move m){
        renderTurn(m, T, board);
        if(this.afterMove!=null){
            this.afterMove.accept(m, this.board);
        }
    }

    private void renderTurn(Move m, Boolean own, Boolean[][] board){
        if(board[m.y][m.x] != own){
            throw new RuntimeException("Invalid move: "+m);
        }
        board[m.y][m.x]=null;
        board[m.y +m.dy][m.x +m.dx]=own;
        if(m.eats==Move.Eats.ATK){
            for(int i=2; ;++i){
                int y = m.y +i*m.dy;
                int x = m.x +i*m.dx;
                if(y<0 || x<0 || y>=dimY || x>=dimX){
                    break;
                }
                if(board[y][x] == null || board[y][x] == own){
                    break;
                }
                board[y][x] = null;
            }
        }else if(m.eats==Move.Eats.WTH){
            for(int i=1; ;++i){
                int y = m.y -i*m.dy;
                int x = m.x -i*m.dx;
                if(y<0 || x<0 || y>=dimY || x>=dimX){
                    break;
                }
                if(board[y][x] == null || board[y][x] == own){
                    break;
                }
                board[y][x] = null;
            }
        }
        if(m.cont!=null){
            renderTurn(m.cont, own, board);
        }
    }

    private void emitLoss(){
        if(this.afterLoss!=null){
            this.afterLoss.accept(this.board);
        }
    }

}