package veraimt.minesweeper.game;

import veraimt.minesweeper.game.tiles.BaseTile;
import veraimt.minesweeper.game.tiles.Bomb;
import veraimt.minesweeper.game.tiles.Tile;

import java.util.*;
import java.util.function.Consumer;

public class Game {
    //Random singleton
    private static final Random RANDOM = new Random();

    public final int width;
    public final int height;
    //Flags left to place
    public int flags;

    //Representation of Game grid
    public final BaseTile[][] grid;

    //List of Bombs
    private final LinkedList<Bomb> bombs = new LinkedList<>();

    //GameState
    private GameState state = GameState.BLANK;


    //Listeners
    private final LinkedList<Runnable> winListeners = new LinkedList<>();
    private final LinkedList<Consumer<Set<BaseTile>>> tileUpdateListeners = new LinkedList<>();

    /**
     * Creates a game with the given width and height and randomly places the given amount of Bombs
     * @param width width of the game grid
     * @param height height of the game grid
     * @param bombs amount of Bombs to be placed
     */
    public Game(int width, int height, int bombs) {
        this(width, height);
        this.flags = bombs;
        randomize(bombs);
    }

    /**
     * Creates a blank game with the given width and height
     * @param width width of the game grid
     * @param height height of the game grid
     */
    public Game(int width, int height) {
        this.width = width;
        this.height = height;
        grid = new BaseTile[width][height];

        //Filling Board with empty tiles
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                grid[x][y] = new Tile(x, y);
            }
        }
    }

    /**
     * Randomly places the given amount of Bombs onm the grid
     * @param bombs amount of Bombs to be randomly placed
     */
    private void randomize(int bombs) {
        //placing Bombs
        for (;bombs > 0; bombs--) {
            //Determining random x,y Coordinates in bounds of grid array
            int x = RANDOM.nextInt(grid.length);
            int y = RANDOM.nextInt(grid[x].length);

            if(!setBomb(x, y)) {
                bombs++;
            }
        }
        state = GameState.OK;
    }

    /**
     * Places a Bomb on the given coordinates, returning its success
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if the Bomb was placed, otherwise false
     */
    private boolean setBomb(int x, int y) {
        if (grid[x][y] instanceof Bomb)
            return false;

        Bomb b = new Bomb(x, y);
        bombs.add(b);
        grid[x][y] = b;
        b.evaluateCounts(grid);

        return true;
    }

    /**
     * Spawns a Bomb on the given coordinates and updating the amount of Bombs to find to win the game
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void spawnBomb(int x, int y) {
        if(setBomb(x, y))
            flags++;
    }

    /**
     * Sets / removes a flag at the given coordinates
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void toggleFlag(int x, int y) {
        BaseTile tile = grid[x][y];
        if (tile.isVisible)
            return;

        if (tile.hasFlag) {
            tile.hasFlag = false;
            flags++;
        } else {
            tile.hasFlag = true;
            flags--;
        }
        checkWin();
        tileUpdate(Set.of(tile));

    }


    /**
     * Checks if the game is won
     */
    private void checkWin() {
        if (flags != 0)
            return;

        if (!bombs.stream().allMatch(bomb -> bomb.hasFlag))
            return;

        for (var row : grid)
            for (var tile : row) {
                if (tile instanceof Tile)
                    tile.isVisible = true;
            }

        winListeners.forEach(Runnable::run);
    }

    /**
     * Searches the Tile at the given coordinates
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void search(int x, int y) {
        BaseTile tile = grid[x][y];
        if (tile instanceof Bomb) {
            lose();
        } else {
            if (!(tile instanceof Tile)) {
                return;
            }
            floodSearch(x, y);
        }
    }

    private void tileUpdate(Set<BaseTile> tiles) {
        tileUpdateListeners.forEach(consumer -> consumer.accept(tiles));
    }

    /**
     * Initiates flood-search (revealing Tiles with flood-fill algorithm) at the given coordinates
     * @param x x-coordinate
     * @param y y-coordinate
     */
    private void floodSearch(int x, int y) {
        HashSet<BaseTile> traversedTiles = new HashSet<>();
        floodSearch(x, y, traversedTiles);

        tileUpdate(traversedTiles);
    }

    /**
     * Recursive flood-search algorithm
     * @param x x-coordinate of current Tile
     * @param y y-coordinate of current Tile
     * @param traversedTiles List containing all traversed Tiles
     */
    private void floodSearch(int x, int y, Set<BaseTile> traversedTiles) {
        if (!(grid[x][y] instanceof Tile tile))
            return;

        if (tile.isVisible || traversedTiles.contains(tile))
            return;

        if (tile.hasFlag) {
            toggleFlag(tile.x, tile.y);
        }
        tile.isVisible = true;
        traversedTiles.add(tile);
        if (tile.getCount() != 0)
            return;

        //left, right
        floodSearch(Math.max(0, x-1), y, traversedTiles);
        floodSearch(Math.min(width-1, x+1), y, traversedTiles);
        //up, down
        floodSearch(x, Math.max(0, y-1), traversedTiles);
        floodSearch(x, Math.min(height-1, y+1), traversedTiles);

        floodSearch(Math.max(0, x-1), Math.min(height-1, y+1), traversedTiles); //tl
        floodSearch(Math.min(width-1, x+1), Math.min(height-1, y+1), traversedTiles); //tr
        floodSearch(Math.max(0, x-1), Math.max(0, y-1), traversedTiles); //bl
        floodSearch(Math.min(width-1, x+1), Math.max(0, y-1), traversedTiles); //br
    }

    /**
     * Executed when the game is lost (searching a Bomb)
     */
    private void lose() {}


    //Listener adding

    /**
     * Add a {@link Runnable } to be executed when the game is won
     * @param r Runnable to be executed
     */
    public void addWinListener(Runnable r) {
        winListeners.add(r);
    }

    /**
     * Returns a List containing all {@link Runnable} to be executed when the game is won
     * @return List of Runnables
     */
    public List<Runnable> getWinListeners() {
        return winListeners;
    }

    /**
     * Remove the given {@link Runnable } to be no longer executed when the game is won
     * @param r Runnable to be no longer executed
     */
    public void removeWinListener(Runnable r) {
        winListeners.remove(r);
    }

    public void addTileUpdateListener(Consumer<Set<BaseTile>> consumer) {
        tileUpdateListeners.add(consumer);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (var v : grid) {
            s.append(Arrays.toString(v));
        }

        return "Game{" +
                "width=" + width +
                ", height=" + height +
                ", flags=" + flags +
                ", grid=" + s +
                '}';
    }

    private enum GameState{
        BLANK,
        OK,
        WIN,
        LOSE;
    }
}
