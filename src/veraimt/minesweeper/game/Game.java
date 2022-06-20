package veraimt.minesweeper.game;

import veraimt.minesweeper.game.tiles.BaseTile;
import veraimt.minesweeper.game.tiles.Bomb;
import veraimt.minesweeper.game.tiles.Tile;

import java.util.Random;

public class Game {
    //Random singleton
    private static final Random RANDOM = new Random();

    public final int width;
    public final int height;
    //Flags left to place
    public int flags;
    private long timeMillis;

    private final BaseTile[][] grid;

    public Game(int width, int height, int bombs) {
        this.width = width;
        this.height = height;
        this.flags = bombs;
        grid = new BaseTile[width][height];

        //Filling Board with empty tiles
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                grid[x][y] = new BaseTile(x, y);
            }
        }
    }

    public Game(int width, int height) {
        this(width, height, 0);
    }

    public void randomize() {
        //placing Bombs
        for (int i = flags; i > 0; i--) {
            //Determining random x,y Coordinates in bounds of grid array
            int x = RANDOM.nextInt(grid.length);
            int y = RANDOM.nextInt(grid[x].length);

            Bomb b = new Bomb(x, y);
            grid[x][y] = b;
            b.evaluateCounts(grid);
        }
    }

    public void setBomb(int x, int y) {

    }
}
