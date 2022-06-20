package veraimt.minesweeper.game.tiles;

public class Tile extends BaseTile {
    private byte count;

    public Tile(int x, int y) {
        super(x, y);
    }

    public void incrementCount() {
        count++;
    }

    public byte getCount() {
        return count;
    }
}
