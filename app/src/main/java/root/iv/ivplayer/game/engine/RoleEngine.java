package root.iv.ivplayer.game.engine;

/**
 * Движок для игр, в которой игроку присовена роль. Даёт возможность:
 * 1. Задавть проль текущему игроку
 * 2. Узнавать роль текущего игрока
 */
public interface RoleEngine<R> {
    R getCurrentRole();
    void setCurrentRole(R r);
}
