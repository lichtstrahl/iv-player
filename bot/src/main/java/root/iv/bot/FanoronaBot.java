package root.iv.bot;


import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FanoronaBot implements BotAPI {
    private int rowCount;
    private int columnCount;
    private Role role;

    public static FanoronaBot defaultSize(Role role) {
        return new FanoronaBot(5, 9, role);
    }

    @Override
    public void processEnemyStep(int from, int to) {

    }

    @Override
    public void processEnemyProgress(List<Progress> progresses) {

    }

    @Override
    public List<Progress> progress() {
        return null;
    }
}
