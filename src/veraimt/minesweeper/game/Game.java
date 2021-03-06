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
    private final HashSet<Bomb> bombs = new HashSet<>();

    //GameState
    private GameState state = GameState.BLANK;


    //Listeners
    private final LinkedList<Consumer<GameState>> stateChangeListeners = new LinkedList<>();
    private final LinkedList<Consumer<Set<? extends BaseTile>>> tileUpdateListeners = new LinkedList<>();

    /**
     * Creates a game with the given width and height and randomly places the given amount of Bombs
     * @param width width of the game grid
     * @param height height of the game grid
     * @param bombs amount of Bombs to be placed
     */
    public Game(int width, int height, int bombs) {
        this(width, height);

        if (bombs > width * height)
            throw new IllegalArgumentException(bombs + " Bombs don't fit into a " + width + "*" + height + " grid!");

        this.flags = bombs;
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

    public void randomize(int xFirstClick, int yFirstClick) {
        randomize(flags, xFirstClick, yFirstClick);
    }

    /**
     * Randomly places the given amount of Bombs onm the grid
     * @param bombs amount of Bombs to be randomly placed
     */
    private void randomize(int bombs, int xFirstClick, int yFirstClick) {
        //variable tile -> Tile that was the first clicked
        if (grid[xFirstClick][yFirstClick] instanceof Tile tile) {
            //placing Bombs
            while (bombs > 0) {
                //Determining random x,y Coordinates in bounds of grid array
                int x = RANDOM.nextInt(grid.length);
                int y = RANDOM.nextInt(grid[x].length);

                //no Bomb at the position of the Tile
                if (x == tile.x && y == tile.y)
                    continue;

                //trying to place the Bomb
                Bomb b = setBomb(x, y);

                //placing the Bomb failed
                if(b == null) {
                    continue;
                }

                //the just placed Bomb caused the count of the Tile to be not 0, so it gets removed
                if (tile.getCount() != 0) {
                    removeBomb(b);
                    continue;
                }


                bombs--;

            }
            changeState(GameState.OK);
        } else throw new IllegalStateException("Game Grid should be blank");


    }

    /**
     * Places a Bomb on the given coordinates, returning the spawned Bomb
     * @param x x-coordinate
     * @param y y-coordinate
     * @return the Bomb if it was placed, otherwise null
     */
    private Bomb setBomb(int x, int y) {
        if (grid[x][y] instanceof Bomb)
            //at the given coordinates is already a Bomb
            return null;

        Bomb b = new Bomb(x, y);
        bombs.add(b);
        grid[x][y] = b;
        b.evaluateCounts(grid);

        return b;
    }

    /**
     * Removes the given Bomb from the game
     * @param bomb the Bomb to be removed
     */
    private void removeBomb(Bomb bomb) {
        bombs.remove(bomb);
        Tile tile = new Tile(bomb.x, bomb.y);
        grid[bomb.x][bomb.y] = tile;
        tile.evaluateCounts(grid);
    }

    /**
     * Spawns a Bomb on the given coordinates and updating the amount of Bombs to find to win the game
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void spawnBomb(int x, int y) {
        if(setBomb(x, y) != null)
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
        //used the correct amount of flags
        if (flags != 0)
            return;

        //all Bombs have a Flag
        if (!bombs.stream().allMatch(bomb -> bomb.hasFlag))
            return;

        //revealing all invisible tiles and adding them to a Set for firing a Tile Update
        Set<BaseTile> tiles = new HashSet<>();
        for (var row : grid)
            for (var tile : row) {
                if (tile instanceof Tile) {
                    if (tile.isVisible)
                        continue;
                    tiles.add(tile);
                    tile.isVisible = true;
                }
            }

        tileUpdate(tiles);
        changeState(GameState.WIN);
    }

    /**
     * Searches the Tile at the given coordinates
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void search(int x, int y) {
        BaseTile tile = grid[x][y];
        if (tile instanceof Bomb) {
            //searching a Bomb -> lose
            lose();
        } else {
            if (!(tile instanceof Tile)) {
                //this should never happen
                return;
            }
            //searching
            floodSearch(x, y);
        }
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

        //tile is visible or was already traversed -> no further processing
        if (tile.isVisible || traversedTiles.contains(tile))
            return;

        //revealing process
        if (tile.hasFlag) {
            //remove the Tiles Flag if it has one
            toggleFlag(tile.x, tile.y);
        }
        tile.isVisible = true;
        traversedTiles.add(tile);

        //only if count == 0 new searches are started
        if (tile.getCount() != 0)
            return;

        //recursive calls

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
    private void lose() {
        //revealing all Bombs
        for (var bomb : bombs)
            bomb.isVisible = true;
        tileUpdate(bombs);
        changeState(GameState.LOSE);
    }

    /**
     * Returns the BaseTile at the given coordinates
     * @param x x-coordinate
     * @param y y-coordinate
     * @return the BaseTile at the given coordinates if they're in bounds of the internal grid, otherwise null
     */
    public BaseTile getTileAt(int x, int y) {
        if (x >= width || y >= height)
            return null;
        return grid[x][y];
    }

    public GameState getState() {
        return state;
    }

    //Listener adding

    public void addStateChangeListener(Consumer<GameState> c) {
        stateChangeListeners.add(c);
    }

    public void addTileUpdateListener(Consumer<Set<? extends BaseTile>> consumer) {
        tileUpdateListeners.add(consumer);
    }

    //firing Events
    private void changeState(GameState newState) {
        state = newState;
        stateChangeListeners.forEach(gameStateConsumer -> gameStateConsumer.accept(newState));
    }

    private void tileUpdate(Set<? extends BaseTile> tiles) {
        tileUpdateListeners.forEach(consumer -> consumer.accept(tiles));
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
                ", state=" + state +
                ", grid=" + s +
                '}';
    }

    public enum GameState{
        BLANK,
        OK,
        WIN,
        LOSE
    }
}
