package veraimt.minesweeper.game.tiles;

import java.awt.*;
import java.util.Optional;

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
