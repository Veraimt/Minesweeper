package veraimt.minesweeper;

import veraimt.minesweeper.game.Game;
import veraimt.minesweeper.ui.GUI;
import veraimt.minesweeper.ui.GameWindow;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicReference;

public class Minesweeper {
    private static Game game;
    public static void main(String[] args) {
        createGame(15, 15, 7);

        AtomicReference<GameWindow> w = new AtomicReference<>();

        //w.set(new GameWindow(game));
        //w.get().setVisible(true);


        SwingUtilities.invokeLater(() -> {
            new GUI();
        });

    }

    private static final Runnable ON_WIN = () -> {
        System.out.println("Win");

    };
    private static void createGame(int width, int height, int bombs) {
        game = new Game(width, height, bombs);
        game.addWinListener(ON_WIN);
    }
}
