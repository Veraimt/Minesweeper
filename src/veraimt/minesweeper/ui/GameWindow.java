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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GameWindow extends JFrame {
    private final GUI host;

    //Resources
    private final Image flagImg;
    private final Image bombImg;

    private final InfoPanel infoPanel;

    private int timer = 0;
    //Other
    public Game game;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private ScheduledFuture<?> timerTask;

    public GameWindow(GUI host, Game game) {
        super("Minesweeper");
        this.host = host;
        this.game = game;

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


        //Adding Components
        add(infoPanel, BorderLayout.PAGE_START);
        add(gameCanvas);


        pack();


        game.addStateChangeListener(gameState -> {
            //if the game has ended (player won or lost) the Status Display is updated and the timer is cancelled
            switch (gameState) {
                case WIN -> infoPanel.statusDisplay.setStatus(InfoPanel.StatusDisplay.Status.WIN);
                case LOSE -> infoPanel.statusDisplay.setStatus(InfoPanel.StatusDisplay.Status.DEAD);
                default -> {
                    return;
                }
            }
            timerTask.cancel(true);
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                host.setVisible(true);
                onWindowClose();
            }
        });

    }

    /**
     * Increments the timer and updates the timer Label
     */
    private void incrementTimer() {
        timer++;
        String s = String.valueOf(timer);

        infoPanel.timerLabel.setText("0".repeat(Math.max(0, 4 - s.length())) + s);
    }

    /**
     * This method must be called, when this window is about to be closed
     */
    private void onWindowClose() {
        executor.shutdownNow();
    }

    /**
     * Extension of {@link JPanel} made for displaying infos about the game
     */

    private class InfoPanel extends JPanel {
        private static final int FONT_SIZE = 18;

        public final JLabel timerLabel;
        public final StatusDisplay statusDisplay;
        public final JLabel flagCountLabel;
        public InfoPanel() {
            timerLabel = new JLabel("0000");
            statusDisplay = new StatusDisplay();
            flagCountLabel = new JLabel();


            //Configuring Components

            //Timer Panel and Components
            JPanel timerPanel = new JPanel();
            timerPanel.setBackground(Color.DARK_GRAY);
            coolBorder(timerPanel);

            Font font = new Font("Agency FB", Font.BOLD, FONT_SIZE);
            timerLabel.setFont(font);
            timerLabel.setForeground(Color.GREEN);

            GroupLayout timerPanelLayout = new GroupLayout(timerPanel);
            timerPanel.setLayout(timerPanelLayout);
            timerPanelLayout.setHorizontalGroup(
                    timerPanelLayout.createSequentialGroup()
                            .addComponent(timerLabel, -1, Short.MAX_VALUE, Short.MAX_VALUE)
                            .addGap(0, 0, Short.MAX_VALUE)
            );
            timerPanelLayout.setVerticalGroup(
                    timerPanelLayout.createParallelGroup()
                            .addComponent(timerLabel)
            );


            //Flags Panel and Components
            JPanel flagsPanel = new JPanel();
            flagsPanel.setOpaque(false);

            Font font1 = flagCountLabel.getFont();
            font1 = font1.deriveFont((float) FONT_SIZE);
            flagCountLabel.setFont(font1);
            flagCountLabel.setText(String.valueOf(game.flags));
            JLabel flagIconLabel = new JLabel(new ImageIcon(flagImg));

            flagsPanel.add(flagIconLabel);
            flagsPanel.add(flagCountLabel);


            //This Panel
            GroupLayout layout = new GroupLayout(this);
            setLayout(layout);
            setBackground(Color.LIGHT_GRAY);
            coolBorder(this);

            //Layout
            layout.setHorizontalGroup(
                    layout.createParallelGroup()
                            .addGroup(
                                    layout.createSequentialGroup()
                                            .addGap(0, 0, Short.MAX_VALUE)
                                            .addComponent(statusDisplay)
                                            .addGap(0, 0, Short.MAX_VALUE)
                            )
                            .addGroup(
                                    layout.createSequentialGroup()
                                            .addComponent(timerPanel, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                            .addContainerGap(0, Short.MAX_VALUE)
                                            .addContainerGap(0, Short.MAX_VALUE)
                                            .addComponent(flagsPanel, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))

            );

            layout.setVerticalGroup(
                    layout.createParallelGroup()
                            .addComponent(timerPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(statusDisplay, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(flagsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            );
        }

        private class StatusDisplay extends JLabel {
            private Status status = Status.ALIVE;

            public StatusDisplay() {
                setIcon(status.icon);

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        super.mousePressed(e);

                        //If the game has ended (player is either dead or has won)
                        //The old game window is discarded and a new one is created
                        switch (status) {
                            case DEAD, WIN -> {
                                GameWindow.this.setVisible(false);
                                host.gameWindow = new GameWindow(host, host.createGame());
                                host.gameWindow.setLocationRelativeTo(GameWindow.this);
                                host.gameWindow.setVisible(true);
                                onWindowClose();
                            }
                        }
                    }
                });
            }

            public void setStatus(Status status) {
                this.status = status;
                setIcon(status.icon);
            }

            /**
             * Enum representing all possible Statuses with their corresponding Icon
             */
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

        //Size Constants
        private static final int CELL_SIZE = 30;
        private static final int IMG_SIZE = CELL_SIZE * 2 / 3;

        //Text colors for numbers on Cells
        private static final Color[] COLORS = {Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW,
                Color.ORANGE, Color.RED, Color.MAGENTA, Color.BLACK};
        //Font for numbers on Cells
        private static final Font FONT = new Font("Serif", Font.BOLD, (int) (CELL_SIZE * 0.8));

        /**
         * Returns the Color for the number on the corresponding {@link Tile} or null if {@link Tile#getCount()} != 0
         * @param tile Tile to get the Color for
         * @return the Color for the Tile if {@link Tile#getCount()} != 0 otherwise null
         */
        private static Color getTileColor(Tile tile) {
            if (tile.getCount() == 0 || tile.getCount() > COLORS.length)
                return null;
            return COLORS[tile.getCount()-1];
        }

        public GameCanvas() {
            setPreferredSize(new Dimension(game.width * CELL_SIZE, game.height * CELL_SIZE));
            setBackground(Color.GRAY);

            game.addTileUpdateListener(baseTiles -> {
                //When Tiles get updated by the game, the corresponding area is repainted
                for (var tile : baseTiles) {
                    repaint(tile.x* CELL_SIZE, tile.y* CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            });

            addMouseListener(new MouseAdapter() {
                //Date Formatter - for DEBUG only
                private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                @Override
                public void mousePressed(MouseEvent event) {
                    super.mousePressed(event);

                    //New Thread to reduce latency
                    new Thread(() -> {

                        //DEBUG
                        System.out.printf(sdf.format(new Date())
                                + "-> Click at x=%1$d, y=%2$d, click=%3$d\n", event.getX(), event.getY(), event.getButton());

                        //Getting position of the clicked Tile
                        Point point = getTilePosAt(event.getX(), event.getY());
                        if (point == null)
                            return;

                        switch (game.getState()) {
                            case BLANK -> {
                                //game grid is blank, so the first click randomizes it
                                game.randomize(point.x, point.y);
                                //starting timer
                                timerTask = executor
                                        .scheduleAtFixedRate(GameWindow.this::incrementTimer, 1, 1, TimeUnit.SECONDS);
                            }
                            case WIN, LOSE -> {
                                return;
                            }

                        }



                        switch (event.getButton()) {
                            //Left-Click
                            case 1 -> game.search(point.x, point.y);
                            //Right-Click
                            case 3 -> game.toggleFlag(point.x, point.y);

                        }
                        //updating Flag count
                        infoPanel.flagCountLabel.setText(String.valueOf(game.flags));
                    }).start();
                }
            });

        }

        @Override
        public void update(Graphics g) {
            //DEBUG
            long time = System.currentTimeMillis();
            System.out.println("--Call to Update--");
            System.out.println("Bounds: " + g.getClipBounds());

            //Converting coordinates of Area to be updated into game coordinates
            Rectangle clip = g.getClipBounds();

            int xStart = clip.x / CELL_SIZE;
            int xEnd = xStart + (clip.width-1) / CELL_SIZE;

            int yStart = clip.y / CELL_SIZE;
            int yEnd = yStart + (clip.height-1) / CELL_SIZE;

            //clearing Area
            g.clearRect(clip.x, clip.y, clip.width, clip.height);
            //drawing Area
            drawArea(g, xStart, xEnd, yStart, yEnd);

            //DEBUG
            System.out.println("Update took " + (System.currentTimeMillis() - time) + "ms");
        }

        @Override
        public void paint(Graphics g) {
            //DEBUG
            long time = System.currentTimeMillis();
            System.out.println("--Call to paint--");
            System.out.println("Bounds: " + g.getClipBounds());

            //drawing whole Game Grid
            drawArea(g, 0, game.width-1, 0, game.height-1);

            //DEBUG
            System.out.println("Paint took " + (System.currentTimeMillis() - time) + "ms");
        }

        /**
         * Draws the given part of the game grid with the given Graphics
         * @param g Graphics used for drawing
         * @param xStart Start x-coordinate
         * @param xEnd End x-coordinate
         * @param yStart Start y-coordinate
         * @param yEnd End y-coordinate
         */
        private void drawArea(Graphics g, int xStart, int xEnd, int yStart, int yEnd) {
            for (int x = xStart; x <= xEnd; x++) {
                for (int y = yStart; y <= yEnd; y++) {
                    //Tile being processed in current iteration
                    BaseTile currentTile = game.grid[x][y];

                    //pixel coordinates: origin of the cell (top-left corner)
                    int xOrigin = x* CELL_SIZE;
                    int yOrigin = y* CELL_SIZE;

                    //drawing Cell
                    g.setColor(Color.LIGHT_GRAY);
                    //                                                 Cell is raised if the Tile is not visible
                    g.fill3DRect(xOrigin, yOrigin, CELL_SIZE, CELL_SIZE, !currentTile.isVisible);

                    //pixel coordinates: center of the cell
                    int xCenter = xOrigin + CELL_SIZE /2;
                    int yCenter = yOrigin + CELL_SIZE /2;


                    if (!currentTile.isVisible) {
                        if (currentTile.hasFlag) {
                            //Drawing Flag Image onto Cell
                            drawImage(g, flagImg, xCenter, yCenter);
                        }
                        //Tile is not visible, no more processing required
                        continue;
                    }

                    //vvv Tile visible vvv

                    if (currentTile instanceof Tile tile) {
                        //drawing count of the Tile onto the Cell

                        if (getTileColor(tile) == null)
                            //if the Color is null the count of the Tile is 0, so number needs to be drawn
                            continue;
                        g.setFont(FONT);
                        g.setColor(getTileColor(tile));

                        String num = String.valueOf(tile.getCount());

                        //determining width and height of the number
                        FontMetrics fm = g.getFontMetrics();
                        int w = fm.stringWidth(num);
                        int h = fm.getAscent();

                        //drawing the number at the center of the cell with offset, so it appears at the right position
                        g.drawString(num, xCenter - (w / 2), yCenter + (h / 4));
                    }
                    else if (currentTile instanceof Bomb) {
                        //if the Bomb has a Flag, the Flag Image is drawn
                        if (currentTile.hasFlag)
                            drawImage(g, flagImg, xCenter, yCenter);
                        //otherwise the Bomb Image is drawn
                        else
                            drawImage(g, bombImg, xCenter, yCenter);
                    }

                }
            }
        }

        /**
         * Draws the given {@link Image} at the given pixel-coordinates
         * @param g Graphics used for drawing
         * @param img The Image to be drawn
         * @param xCenter x-coordinate of the Image center
         * @param yCenter y-coordinate of the Image center
         */
        private void drawImage(Graphics g, Image img, int xCenter, int yCenter) {
            g.drawImage(img, xCenter- IMG_SIZE /2, yCenter- IMG_SIZE /2, IMG_SIZE, IMG_SIZE, this);
        }

        /**
         * Converts pixel-coordinates to {@link Game} coordinates
         * @param x x-pixel-coordinate
         * @param y y-pixel-coordinate
         * @return a Point whose coordinates are meant to be used by the {@link Game}
         */

        private Point getTilePosAt(int x, int y) {
            int tileX = x / CELL_SIZE;
            int tileY = y / CELL_SIZE;

            BaseTile tile = game.getTileAt(tileX,tileY);

            if (tile == null)
                return null;
            return new Point(tile.x, tile.y);
        }

    }

    /**
     * Decorates the given {@link JComponent} with a cool Border
     * @param component The Component to retrieve a Border
     */
    public static void coolBorder(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2, true),
                BorderFactory.createEmptyBorder(5,5,5,5)));
    }
}
