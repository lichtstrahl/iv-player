package root.iv.ivplayer.game.tictac.scene;

import root.iv.ivplayer.game.tictac.TicTacTextures;

public class TicTacSceneFactory {
    private static final int SQUARE_DEFAULT_SIZE = 100;
    private static final int MARGIN_DEFAULT = 150;

    private TicTacSceneFactory() { }

    public static TicTacSceneFactory newFactory() {
        return new TicTacSceneFactory();
    }
    
    public TicTacToeScene defaultScene(TicTacTextures textures) {
        return new TicTacToeScene(textures, SQUARE_DEFAULT_SIZE, MARGIN_DEFAULT, MARGIN_DEFAULT);
    }

    public TicTacToeScene fixSize(TicTacTextures textures, int margin, int size) {
        return new TicTacToeScene(textures, size, margin, margin);
    }

}
