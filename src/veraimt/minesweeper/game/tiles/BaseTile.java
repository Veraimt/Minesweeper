package veraimt.minesweeper.game.tiles;

public class BaseTile {

    public final int x;
    public final int y;
    public boolean hasFlag;

    public BaseTile(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
