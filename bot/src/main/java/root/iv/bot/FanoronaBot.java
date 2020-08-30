package root.iv.bot;


import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import root.iv.bot.ai.Behaviour;
import root.iv.bot.ai.Main;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FanoronaBot implements BotAPI {
    private Main ai;
    private int rows;
    private int cols;

    public static FanoronaBot defaultSize(Role role) {
        Main ai = new Main();
        FanoronaBot bot = new FanoronaBot(ai, 5, 9);
        ai.init(role, 9, 5, null, null, Behaviour.LONGEST);
        return bot;
    }

    @Override
    public void processEnemyStep(int from, int to) {
        int[] from_ = coordToYX(from);
        int[] to_ = coordToYX(to);
        ai.acceptTurn(new Move(from_[0], from_[1], to_[0]-from_[0], to_[1]-from_[1]));
    }

    @Override
    public void processEnemyProgress(List<Progress> move) {
        System.out.println(move);
        System.out.println(ai.board.progressesToMove(move));
        ai.acceptTurn(ai.board.progressesToMove(move));
    }

    @Override
    public List<Progress> progress() {
        return ai.board.moveToProgresses(ai.takeTurn());
    }

    private int[] coordToYX(int coord){
        return new int[]{coord/rows,coord%rows};
    }
}
