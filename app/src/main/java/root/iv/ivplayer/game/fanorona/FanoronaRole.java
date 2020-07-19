package root.iv.ivplayer.game.fanorona;

import root.iv.bot.Role;

public enum FanoronaRole {
    FREE, WHITE, BLACK;

    public static FanoronaRole of(Role role) {
        if (role == Role.WHITE) return WHITE;
        if (role == Role.BLACK) return BLACK;
        if (role == Role.NONE) return FREE;

        throw new IllegalArgumentException("Не удалось определить роль");
    }

    public Role export() {
        if (this == BLACK) return Role.BLACK;
        if (this == WHITE) return Role.WHITE;
        if (this == FREE) return Role.NONE;

        throw new IllegalStateException("Не удалось определить роль");
    }
}
