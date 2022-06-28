package veraimt.minesweeper.game.tiles;

public class Bomb extends BaseTile{
    public Bomb(int x, int y) {
        super(x, y);
    }

    @Override
    protected void evaluateTile(BaseTile tile) {
        if (tile instanceof Tile t)
            t.evaluateTile(this);
    }


    @Override
    public String toString() {
        return "Bomb{" +
                "x=" + x +
                ", y=" + y +
                ", hasFlag=" + hasFlag +
                '}';
    }
}
