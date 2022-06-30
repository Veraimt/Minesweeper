package veraimt.minesweeper.ui;

import veraimt.minesweeper.game.Game;
import veraimt.minesweeper.game.tiles.BaseTile;
import veraimt.minesweeper.game.tiles.Bomb;
import veraimt.minesweeper.game.tiles.Tile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameWindow extends JFrame {

    //Resources
    private final Image flagImg;
    private final Image bombImg;

    private final InfoPanel infoPanel;
    //Other
    public Game game;

    public GameWindow(Game game) {
        super("Minesweeper");
        this.game = game;

        //JFrame Metadata
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        //Loading Resources
        try {
            flagImg = ImageIO.read(getClass().getClassLoader().getResource("flag.png"));
            bombImg = ImageIO.read(getClass().getClassLoader().getResource("bomb.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Instantiating Components
        infoPanel = new InfoPanel();
        GameCanvas gameCanvas = new GameCanvas();




        System.out.println(infoPanel.getHeight());
        System.out.println(gameCanvas.getHeight());

        //Adding Components
        add(infoPanel, BorderLayout.PAGE_START);
        add(gameCanvas);


        pack();

        game.addStateChangeListener(gameState -> {
            switch (gameState) {
                case WIN -> infoPanel.statusDisplay.setStatus(InfoPanel.StatusDisplay.Status.WIN);
                case LOSE -> infoPanel.statusDisplay.setStatus(InfoPanel.StatusDisplay.Status.DEAD);
            }
        });
    }

    private class InfoPanel extends JPanel {

        public final JLabel timerLabel;
        public final StatusDisplay statusDisplay;
        public final JLabel flagCountLabel;
        public InfoPanel() {
            timerLabel = new JLabel("0");
            statusDisplay = new StatusDisplay();
            flagCountLabel = new JLabel();


            //Configuring Components
            Font font = timerLabel.getFont();
            font = font.deriveFont(18f);

            timerLabel.setFont(font);
            coolBorder(timerLabel);

            flagCountLabel.setFont(font);
            flagCountLabel.setText(String.valueOf(game.flags));


            JLabel flagIconLabel = new JLabel(new ImageIcon(flagImg));

            GroupLayout layoutInfoPanel = new GroupLayout(this);
            setLayout(layoutInfoPanel);
            setBackground(Color.LIGHT_GRAY);
            coolBorder(this);


            layoutInfoPanel.setHorizontalGroup(
                    layoutInfoPanel.createParallelGroup()
                            .addGroup(layoutInfoPanel.createSequentialGroup()
                                    .addComponent(timerLabel))
                            .addGroup(layoutInfoPanel.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(statusDisplay)
                                    .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layoutInfoPanel.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(flagIconLabel)
                                    .addComponent(flagCountLabel))
            );

            layoutInfoPanel.setVerticalGroup(
                    layoutInfoPanel.createParallelGroup()
                            .addComponent(timerLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(statusDisplay)
                            .addComponent(flagIconLabel)
                            .addComponent(flagCountLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            );
        }

        private class StatusDisplay extends JLabel {

            public StatusDisplay() {
                setIcon(Status.ALIVE.icon);
            }

            public void setStatus(Status status) {
                setIcon(status.icon);
            }

            public enum Status {
                ALIVE("alive.png"),
                DEAD("dead.png"),
                WIN("win.png");

                public final ImageIcon icon;

                Status(String ressource) {
                    icon = new ImageIcon(getClass().getClassLoader().getResource(ressource));
                }
            }
        }
    }


    /**
     * An Extension of {@link JComponent} used to display the Game
     */
    private class GameCanvas extends JComponent {

        private static final int CELL_SIZE = 30;
        private final int imgSize = CELL_SIZE * 2 / 3;


        private static final Color[] COLORS = {Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW,
                Color.ORANGE, Color.RED, Color.MAGENTA, Color.BLACK};

        private static final Font FONT = new Font("Serif", Font.BOLD, (int) (CELL_SIZE * 0.8));

        private static Color getTileColor(Tile tile) {
            if (tile.getCount() == 0)
                return null;
            return COLORS[tile.getCount()-1];
        }

        public GameCanvas() {
            setPreferredSize(new Dimension(game.width * CELL_SIZE, game.height * CELL_SIZE));

            setBackground(Color.GRAY);

            game.addTileUpdateListener(baseTiles -> {
                for (var tile : baseTiles) {
                    repaint(tile.x* CELL_SIZE, tile.y* CELL_SIZE, CELL_SIZE, CELL_SIZE);

                }
            });

            addMouseListener(new MouseAdapter() {
                private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);

                    //New Thread to reduce latency
                    new Thread(() -> {


                        System.out.printf(sdf.format(new Date())
                                + "-> Click at x=%1$d, y=%2$d, click=%3$d\n", e.getX(), e.getY(), e.getButton());

                        Point point = getTilePosAt(e.getX(), e.getY());
                        if (point == null)
                            return;

                        switch (game.getState()) {
                            case BLANK:
                                game.randomize(point.x, point.y);
                                break;
                            case WIN:
                            case LOSE:
                                return;
                        }



                        switch (e.getButton()) {
                            //Left-Click
                            case 1 -> game.search(point.x, point.y);
                            //Right-Click
                            case 3 -> game.toggleFlag(point.x, point.y);

                        }
                        infoPanel.flagCountLabel.setText(String.valueOf(game.flags));
                    }).start();
                }
            });

        }

        @Override
        public void update(Graphics g) {
            System.out.println("Call to Update");
            System.out.println("Bounds: " + g.getClipBounds());

            long time = System.currentTimeMillis();
            Rectangle clip = g.getClipBounds();

            int xStart = clip.x / CELL_SIZE;
            int xEnd = xStart + (clip.width-1) / CELL_SIZE;

            int yStart = clip.y / CELL_SIZE;
            int yEnd = yStart + (clip.height-1) / CELL_SIZE;

            g.clearRect(xStart, yStart, xEnd-xStart, yEnd-yStart);
            drawArea(g, xStart, xEnd, yStart, yEnd);

            System.out.println("Update took " + (System.currentTimeMillis() - time) + "ms");
        }

        @Override
        public void paint(Graphics g) {
            long time = System.currentTimeMillis();
            System.out.println("Call to paint");
            System.out.println("Bounds: " + g.getClipBounds());


            drawArea(g, 0, game.width-1, 0, game.height-1);

            System.out.println("Paint took " + (System.currentTimeMillis() - time) + "ms");
        }

        private void drawArea(Graphics g, int xStart, int xEnd, int yStart, int yEnd) {
            for (int x = xStart; x <= xEnd; x++) {
                for (int y = yStart; y <= yEnd; y++) {
                    //Tile being processed in current iteration
                    BaseTile currentTile = game.grid[x][y];

                    int xOrigin = x* CELL_SIZE;
                    int yOrigin = y* CELL_SIZE;

                    g.setColor(Color.LIGHT_GRAY);
                    g.fill3DRect(xOrigin, yOrigin, CELL_SIZE, CELL_SIZE, !currentTile.isVisible);

                    int xCenter = xOrigin + CELL_SIZE /2;
                    int yCenter = yOrigin + CELL_SIZE /2;


                    if (!currentTile.isVisible) {
                        if (currentTile.hasFlag) {
                            drawImage(g, flagImg, xCenter, yCenter);
                        }
                        continue;
                    }


                    if (currentTile instanceof Tile tile) {

                        if (getTileColor(tile) == null)
                            continue;
                        g.setFont(FONT);
                        g.setColor(getTileColor(tile));

                        FontMetrics fm = g.getFontMetrics();
                        int w = fm.stringWidth(String.valueOf(tile.getCount()));
                        int h = fm.getAscent();

                        g.drawString(String.valueOf(tile.getCount()), xCenter - (w / 2), yCenter + (h / 4));
                    }
                    else if (currentTile instanceof Bomb) {
                        if (currentTile.hasFlag)
                            drawImage(g, flagImg, xCenter, yCenter);
                        else
                            drawImage(g, bombImg, xCenter, yCenter);
                    }

                }
            }
        }

        private void drawImage(Graphics g, Image img, int xCenter, int yCenter) {
            g.drawImage(img, xCenter-imgSize/2, yCenter-imgSize/2, imgSize, imgSize, this);
        }

        private Point getTilePosAt(int x, int y) {
            int tileX = x / CELL_SIZE;
            int tileY = y / CELL_SIZE;

            BaseTile tile = game.getTileAt(tileX,tileY);

            if (tile == null)
                return null;
            return new Point(tile.x, tile.y);
        }

    }

    public static void coolBorder(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2, true),
                BorderFactory.createEmptyBorder(5,5,5,5)));
    }
}
