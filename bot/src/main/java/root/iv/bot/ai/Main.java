package root.iv.bot.ai;

import root.iv.bot.Board;
import root.iv.bot.Move;
import root.iv.bot.Role;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Main {
    public Board board;
    public Behaviour behaviour;
    private Role role;
    private BiConsumer<Move,Board> afterMove=null;
    private Consumer<Board> afterLoss=null;

    public Main init(Role role, int dimX, int dimY,
                     BiConsumer<Move,Board> afterMove,
                     Consumer<Board> afterLoss,
                     Behaviour behaviour){
        this.role = role;
        this.behaviour = behaviour;
        this.afterLoss=afterLoss;
        this.afterMove=afterMove;
        this.board = Board.init(dimY, dimX);
        return this;
    }

    public Move takeTurn(){
        List<Move> moves = new ArrayList<>();
        for(int i=0; i<board.rows; ++i){
            for(int j=0; j<board.cols; ++j){
                if(board.state[i][j]!=role){
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
        board.renderMove(m);
        board.printToConsole();
    }

    private List<Move> lsMoves(int y, int x, Board board, List<int[]> visited, boolean canHungry){
        List<Move> res = new ArrayList<>();
        boolean canDiag = (x+y)%2 == 0;
        for(int i=y-1; i<=y+1; ++i){
            if(i<0 || i>=board.rows){
                continue;
            }
            for(int j=x-1; j<=x+1; ++j){
                if(j<0 || j>=board.cols){
                    continue;
                }
                if(!canDiag && i!=y && j!=x){
                    continue;
                }
                final int i_=i;
                final int j_=j;
                if(visited.stream().anyMatch(it-> it[0]==i_ && it[1]==j_)){
                    continue;
                }
                if(board.state[i][j]==Role.NONE){
                    Move m = new Move(y,x,i-y, j-x);
                    boolean eats = false;
                    if(!(y+2*m.dy <0 || y+2*m.dy >=board.rows || x+2*m.dx <0 || x+2*m.dx >=board.cols)){
                        if(board.state[y+2*m.dy][x+2*m.dx]==role.foe()){
                            eats = true;
                            List<int[]> visited2 = new ArrayList<>(visited);
                            visited2.add(new int[]{y,x});
                            Move mm = m.cpWithEatsAtk();
                            res.add(mm);
                            res.addAll(expandContinuations(mm, board, visited2));
                        }
                    }
                    if(!(y-m.dy <0 || y-m.dy >=board.rows || x-m.dx <0 || x-m.dx >=board.cols)){
                        if(board.state[y-m.dy][x-m.dx]==role.foe()){
                            eats = true;
                            List<int[]> visited2 = new ArrayList<>(visited);
                            visited2.add(new int[]{y,x});
                            Move mm = m.cpWithEatsWth();
                            res.add(mm);
                            res.addAll(expandContinuations(mm, board, visited2));
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
    private List<Move> expandContinuations(Move m, Board board, List<int[]> visited){
        Board boardNext = board.afterMove(m);
        return lsMoves(m.y +m.dy, m.x +m.dx, boardNext, visited, false).stream()
                .filter(Move::eating)
                .map(m::cpWithCont)
                .collect(Collectors.toList());
    }

    private void emitTurn(Move m){
        board.renderMove(m);
        board.printToConsole();
        if(this.afterMove!=null){
            this.afterMove.accept(m, board);
        }
    }

    private void emitLoss(){
        if(this.afterLoss!=null){
            this.afterLoss.accept(board);
        }
    }

}