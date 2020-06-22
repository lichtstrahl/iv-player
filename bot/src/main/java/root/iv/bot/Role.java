package root.iv.bot;

public enum Role {
    WHITE, BLACK, NONE;
    public Role foe(){
        return WHITE==this ? BLACK : BLACK==this ? WHITE : NONE;
    }
}
