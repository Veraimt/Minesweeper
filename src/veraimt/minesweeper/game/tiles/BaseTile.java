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

    @Override
    public String toString() {
        return "BaseTile{" +
                "x=" + x +
                ", y=" + y +
                ", hasFlag=" + hasFlag +
                '}';
    }
}
