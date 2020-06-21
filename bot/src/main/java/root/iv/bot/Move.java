package root.iv.bot;

import lombok.ToString;

@ToString
public class Move {
    public int y;
    public int x;
    public int dy;
    public int dx;
    public Eats eats = Eats.NO;
    public Move cont = null;

    public Move(int y, int x, int dy, int dx){
        this.y = y;
        this.x = x;
        this.dy = dy;
        this.dx = dx;
    }

    public Move cpWithEatsAtk(){
        Move m = cp();
        m.eats = Eats.ATK;
        return m;
    }
    public Move cpWithEatsWth(){
        Move m = cp();
        m.eats = Eats.WTH;
        return m;
    }
    public Move cpWithCont(Move cont){
        Move m = cp();
        m.cont = cont;
        return m;
    }
    public Move cp(){
        Move m = new Move(y, x, dy, dx);
        m.eats = eats;
        m.cont = cont!=null ? cont.cp() : null;
        return m;
    }

    public boolean eating(){
        return this.eats!=Eats.NO;
    }

    public enum Eats{
        NO,ATK,WTH
    }
}
