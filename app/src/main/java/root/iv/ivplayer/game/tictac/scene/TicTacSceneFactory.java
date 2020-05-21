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
    public TicTacToeScene defaultScene(TicTacTextures textures) {
        return new TicTacToeScene(textures, SQUARE_DEFAULT_SIZE, MARGIN_DEFAULT, MARGIN_DEFAULT);
    }

    // Задаются конкретные значения
    public TicTacToeScene fixSize(TicTacTextures textures, int margin, int size) {
        return new TicTacToeScene(textures, size, margin, margin);
    }

    // Автоподбор под размеры экрана. Задача: Заполнить всю ширину экрана (экран вертикален)
    public TicTacToeScene autoSize(TicTacTextures textures, int width, int height, double scale) {

        if (width < height) { // Поле расположено вертикально
            int size = width/3;
            int margin = height-width / 2;
            return new TicTacToeScene(textures, size, 0, margin);
        } else { // Поле горизонтально
            int size = height/3;
            int margin = width-height / 2;
            return new TicTacToeScene(textures, size, margin, 0);
        }
    }
}
