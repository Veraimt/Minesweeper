package veraimt.minesweeper.game.tiles;

public class Bomb extends BaseTile{
    public Bomb(int x, int y) {
        super(x, y);
    }

    public void evaluateCounts(BaseTile[][] grid) {
        int xMin = Math.max(0, x-1);
        int xMax = Math.min(grid.length, x+1);

        int yMin = Math.max(0, y-1);
        int yMax = Math.min(grid.length, y+1);

        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                if (!(grid[x][y] instanceof Tile tile))
                    continue;

                tile.incrementCount();
            }
        }
    }
}
