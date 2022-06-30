package veraimt.minesweeper.ui;

import veraimt.minesweeper.game.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class GUI extends JFrame {

    GameWindow gameWindow;

    private final GridSizeSliderPanel sliderPanel = new GridSizeSliderPanel();
    private final JComboBox<Difficulty> difficultySelector = new JComboBox<>(Difficulty.values());

    public GUI() {
        super("Minesweeper");
        //Window settings
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                System.exit(1);
            }
        });



        //Difficulty Panel
        BorderedPanel difficultyPanel = new BorderedPanel("Difficulty");
        difficultyPanel.setLayout(new GridLayout());
        difficultyPanel.add(difficultySelector);


        //Start Button
        JButton startButton = new JButton();
        startButton.setText("START");
        startButton.addActionListener(l -> {
            gameWindow = new GameWindow(this, createGame());
            gameWindow.setVisible(true);
            this.setVisible(false);
        });


        //Main Panel + Layout
        JPanel mainPanel = new JPanel();
        GroupLayout layout = new GroupLayout(mainPanel);

        mainPanel.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(sliderPanel,  GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(difficultyPanel,  GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(startButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(sliderPanel)
                        .addComponent(difficultyPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, Short.MAX_VALUE)
                        .addComponent(startButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        add(mainPanel);


        //finalizing Window
        pack();

        setMinimumSize(getSize());

        setVisible(true);
    }

    Game createGame() {
        return new Game(sliderPanel.getValue(), sliderPanel.getValue(),
                (int) (Math.pow(sliderPanel.getValue(), 2) *
                        ((Difficulty) Objects.requireNonNull(difficultySelector.getSelectedItem())).fac));
    }

    private class GridSizeSliderPanel extends SliderPanel {

        public GridSizeSliderPanel() {
            super("Grid Size");
            slider.setMinimum(5);
            slider.setMaximum(40);
            slider.setValue(20);
            slider.setToolTipText("Defines the size of the Game Grid");
        }
    }


    private abstract class SliderPanel extends BorderedPanel {
        protected final JLabel label = new JLabel();
        protected final JSlider slider = new JSlider();

        public SliderPanel(String panelName) {
            super(panelName);

            GroupLayout layout = new GroupLayout(this);

            setLayout(layout);

            label.setText(String.valueOf(slider.getValue()));

            slider.setPaintLabels(true);
            slider.setName(panelName);

            slider.addChangeListener(e -> label.setText(String.valueOf(slider.getValue())));



            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(label,  GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(slider,  GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            );

            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .addComponent(slider)
                            .addComponent(label)
            );

        }

        public int getValue() {
            return slider.getValue();
        }
    }

    private class BorderedPanel extends JPanel {

        public BorderedPanel(String panelName) {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(panelName),
                    BorderFactory.createEmptyBorder(5,5,5,5)));
        }
    }

    private enum Difficulty {
        EASY(.05f),
        MEDIUM(.1f),
        HARD(.15f),
        ULTRA(.25f);

        public final float fac;
        Difficulty(float fac) {
            this.fac = fac;
        }
    }
}
