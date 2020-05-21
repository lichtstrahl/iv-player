package root.iv.ivplayer.game.tictac.scene;

import root.iv.ivplayer.game.tictac.TicTacTextures;

public class TicTacSceneFactory {
    private static final int SQUARE_DEFAULT_SIZE = 100;
    private static final int MARGIN_DEFAULT = 150;

    private TicTacSceneFactory() { }

    public static TicTacSceneFactory newFactory() {
        return new TicTacSceneFactory();
    }

    // Размеры по умолчанию. Тестовый вариант, чтобы хоть как-то отобразить сцену
    public TicTacScene defaultScene(TicTacTextures textures) {
        return new TicTacScene(textures, SQUARE_DEFAULT_SIZE, MARGIN_DEFAULT, MARGIN_DEFAULT);
    }

    // Задаются конкретные значения
    public TicTacScene fixSize(TicTacTextures textures, int margin, int size) {
        return new TicTacScene(textures, size, margin, margin);
    }
}
