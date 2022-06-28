package veraimt.minesweeper.game.tiles;

public abstract class BaseTile {

    public final int x;
    public final int y;
    public boolean hasFlag;
    public boolean isVisible;

    public BaseTile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Evaluates the count of this Tile according to the game rules
     * @param grid game grid
     */
    public void evaluateCounts(BaseTile[][] grid) {
        int xMin = Math.max(0, x-1);
        int xMax = Math.min(grid.length-1, x+1);

        int yMin = Math.max(0, y-1);
        int yMax = Math.min(grid.length-1, y+1);

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                evaluateTile(grid[x][y]);
            }
        }
    }

    protected abstract void evaluateTile(BaseTile tile);

    @Override
    public String toString() {
        return "BaseTile{" +
                "x=" + x +
                ", y=" + y +
                ", hasFlag=" + hasFlag +
                '}';
    }
}
