package veraimt.minesweeper;

import veraimt.minesweeper.ui.GUI;

import javax.swing.*;

public class Minesweeper {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }
}
