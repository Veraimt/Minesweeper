package veraimt.minesweeper.ui;

import veraimt.minesweeper.game.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class GUI extends JFrame {

    private GameWindow gameWindow;

    private final GridSizeSliderPanel sliderPanel = new GridSizeSliderPanel();
    private final JComboBox<Difficulty> difficultySelector = new JComboBox<>(Difficulty.values());
    private final JButton startButton = new JButton();

    public GUI() {
        super("Minesweeper");
        //Window settings
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);



        //Difficulty Panel
        difficultySelector.addActionListener(l -> {
            System.out.println(difficultySelector.getSelectedItem());
        });

        JPanel difficultyPanel = new JPanel(new GridLayout());
        difficultyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Difficulty"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        difficultyPanel.add(difficultySelector);


        //Start Button
        startButton.setText("START");
        startButton.addActionListener(l -> {
            gameWindow = new GameWindow(new Game(sliderPanel.getValue(), sliderPanel.getValue(),
                    (int) (Math.pow(sliderPanel.getValue(), 2) *
                            ((Difficulty) Objects.requireNonNull(difficultySelector.getSelectedItem())).fac)));
            gameWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    super.windowClosed(e);
                    setVisible(true);
                }
            });
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
                        .addComponent(startButton)
        );

        add(mainPanel);


        //finalizing Window
        pack();

        setMinimumSize(getSize());

        setVisible(true);
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


    private abstract class SliderPanel extends JPanel {
        protected final JLabel label = new JLabel();
        protected final JSlider slider = new JSlider();

        public SliderPanel(String panelName) {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(panelName),
                    BorderFactory.createEmptyBorder(5,5,5,5)));

            GroupLayout layout = new GroupLayout(this);

            setLayout(layout);

            label.setText(String.valueOf(slider.getValue()));

            slider.setPaintLabels(true);
            slider.setName(panelName);

            slider.addChangeListener(e -> {
                label.setText(String.valueOf(slider.getValue()));
            });



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
