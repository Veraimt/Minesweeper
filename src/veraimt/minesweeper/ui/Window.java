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

public class Window extends JFrame {

    private static final int WIDTH = 800, HEIGHT = 600;

    //Resources
    private final Image flagImg;
    private final Image bombImg;

    //Components
    private final JPanel infoPanel;
    private final GameCanvas gameCanvas;
    private final JLabel flags;

    //Other
    public Game game;

    public Window(Game game) {
        super("Minesweeper");
        this.game = game;

        //JFrame Metadata
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        //setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setSize(WIDTH, HEIGHT);
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
        infoPanel = new JPanel();
        gameCanvas = new GameCanvas();
        flags = new JLabel();

        //Configuring Components
        infoPanel.setSize(WIDTH, 50);
        infoPanel.setBackground(Color.CYAN);
        infoPanel.add(flags);


        flags.setText(String.valueOf(game.flags));

        System.out.println(infoPanel.getHeight());
        System.out.println(gameCanvas.getHeight());

        //Adding Components
        add(infoPanel, BorderLayout.PAGE_START);
        add(gameCanvas);


        pack();

        addMouseListener(new MouseAdapter() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                System.out.println("Mouse Event");
                System.out.printf(sdf.format(new Date())
                        + "-> Click at x=%1$d, y=%2$d, click=%3$d\n", e.getX(), e.getY(), e.getButton());

            }

        });

    }



    private class GameCanvas extends JComponent {
        private static final int LINE_THICKNESS = 2;
        private static final int MIN_CELL_SIZE = 30;

        private int cellSize = MIN_CELL_SIZE;
        private int imgSize = 20;


        private static final Color[] COLORS = {Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW,
                Color.ORANGE, Color.RED, Color.MAGENTA, Color.BLACK};

        private static final Font FONT = new Font("Serif", Font.BOLD, 24);

        private static Color getTileColor(Tile tile) {
            if (tile.getCount() == 0)
                return null;
            return COLORS[tile.getCount()-1];
        }

        public GameCanvas() {
            //super(Window.this.getGraphicsConfiguration());

            //setSize(game.width * cellSize, game.height * cellSize);
            //setMinimumSize(new Dimension(game.width * cellSize, game.height * cellSize));
            setPreferredSize(new Dimension(game.width * cellSize, game.height * cellSize));

            setBackground(Color.GRAY);

            game.addTileUpdateListener(baseTiles -> {
                for (var tile : baseTiles) {
                    repaint(tile.x*cellSize, tile.y*cellSize, cellSize, cellSize);

                }
            });

            addMouseListener(new MouseAdapter() {
                private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);

                    new Thread(() -> {


                        System.out.printf(sdf.format(new Date())
                                + "-> Click at x=%1$d, y=%2$d, click=%3$d\n", e.getX(), e.getY(), e.getButton());

                        Point point = getTilePosAt(e.getX(), e.getY());
                        if (point == null)
                            return;


                        switch (e.getButton()) {
                            //Left
                            case 1 -> game.search(point.x, point.y);
                            //Right
                            case 3 -> game.toggleFlag(point.x, point.y);

                        }
                        flags.setText(String.valueOf(game.flags));
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

            int xStart = clip.x / cellSize;
            int xEnd = xStart + (clip.width-1) / cellSize;

            int yStart = clip.y / cellSize;
            int yEnd = yStart + (clip.height-1) / cellSize;

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

                    int xOrigin = x*cellSize;
                    int yOrigin = y*cellSize;

                    g.setColor(Color.LIGHT_GRAY);
                    g.fill3DRect(xOrigin, yOrigin, cellSize, cellSize, !currentTile.isVisible);

                    int xCenter = xOrigin + cellSize/2;
                    int yCenter = yOrigin + cellSize/2;


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
            int tileX = x / cellSize;
            int tileY = y / cellSize;

            BaseTile tile = game.getTileAt(tileX,tileY);

            if (tile == null)
                return null;
            return new Point(tile.x, tile.y);
        }

    }
}
