package veraimt.minesweeper.game.tiles;

public class Tile extends BaseTile {

    private byte count;

    public Tile(int x, int y) {
        super(x, y);
    }

    public void incrementCount() {
        count++;
    }

    public void decrementCount() {
        if (count > 0)
            count--;
    }


    public void evaluateCounts(BaseTile[][] grid) {
        count = 0;
        super.evaluateCounts(grid);
    }

    @Override
    protected void evaluateTile(BaseTile tile) {
        if (tile instanceof Bomb)
            incrementCount();
        else if (tile instanceof Tile t)
            t.decrementCount();
    }

    public byte getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Tile{" +
                "x=" + x +
                ", y=" + y +
                ", hasFlag=" + hasFlag +
                ", count=" + count +
                '}';
    }
}
