package root.iv.bot;

import lombok.AllArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class Board {
    public Role[][] state;
    public int rows;
    public int cols;

    public static Board init(int rows, int cols){
        Role[][] state = new Role[rows][cols];
        for(int i=0; i<((rows-1)/2); ++i){
            for(int j=0; j<cols; ++j){
                state[i][j]= B;
            }
        }
        {
            int i=(rows-1)/2;
            for(int j=0; j<((cols-1)/2); ++j){
                state[i][j]= j % 2 == 0 ? B : W;
            }
            state[i][(cols-1)/2] = N;
            for(int j=((cols+1)/2); j<cols; ++j){
                state[i][j]= j % 2 != 0 ? B : W;
            }
        }
        for(int i=((rows+1)/2); i<rows; ++i){
            for(int j=0; j<cols; ++j){
                state[i][j]= W;
            }
        }
        return new Board(state, rows, cols);
    }

    public void renderMove(Move m){
        Role clr = state[m.y][m.x];
        state[m.y][m.x]=N;
        state[m.y +m.dy][m.x +m.dx]=clr;
        if(m.eats== Eats.ATK){
            eatLine(m.y+m.dy, m.x+m.dx, m.dy, m.dx, clr.foe());
        }else if(m.eats== Eats.WTH){
            eatLine(m.y, m.x, -m.dy, -m.dx, clr.foe());
        }
        if(m.cont!=null){
            renderMove(m.cont);
        }
    }

    public Board afterMove(Move m){
        Role[][] newCells = new Role[rows][cols];
        for(int i=0; i<rows; ++i){
            System.arraycopy(state[i],0, newCells[i], 0, cols);
        }
        Board newBoard = new Board(newCells, rows, cols);
        newBoard.renderMove(m);
        return newBoard;
    }

    public List<Progress> moveToProgresses(Move m){
        List<Progress> r = new ArrayList<>();
        for(; m!=null; m=m.cont){
            r.add(moveToProgress(m));
        }
        val role = r.get(r.size()-1).role;
        r.forEach(it-> it.role=role);
        r.forEach(System.out::println);
        return r;
    }
    public Progress moveToProgress(Move m){
        return new Progress(m.y*cols+m.x, (m.y+m.dy)*cols+m.x+m.dx, state[m.y+m.dy][m.x+m.dx],m.eats);
    }

    public Move progressesToMove(List<Progress> p){
        Move r = progressToMove(p.get(0));
        p.stream().skip(1).forEach(it-> r.cont=progressToMove(it));
        return r;
    }
    public Move progressToMove(Progress p){
        Move m = new Move(p.from/cols, p.from%cols, (p.to/cols-p.from/cols), (p.to%cols-p.from%cols));
        m.eats = p.eats;
        return m;
    }

    private void eatLine(int y, int x, int dy, int dx, Role clr){
        for(;;){
            y += dy;
            x += dx;
            if(y<0 || x<0 || y>=rows || x>=cols){
                break;
            }
            if(state[y][x]!=clr){
                break;
            }
            state[y][x] = N;
        }
    }

    public void printToConsole(){
        System.out.println("------------\n\n"+ Arrays.stream(state).map(it-> Arrays.stream(it).map(
                c->c==W ? "W" : c==B ? "B" : " "
        ).collect(Collectors.joining())+"\n").collect(Collectors.joining()));
    }

    private static final Role W = Role.WHITE;
    private static final Role B = Role.BLACK;
    private static final Role N = Role.NONE;
}
