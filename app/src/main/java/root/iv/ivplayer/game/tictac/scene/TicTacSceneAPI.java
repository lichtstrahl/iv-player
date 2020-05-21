package root.iv.ivplayer.game.tictac.scene;

import java.util.List;

import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.tictac.Block;

public interface TicTacSceneAPI extends Scene {

    List<Block> getAllBlocks();
}
