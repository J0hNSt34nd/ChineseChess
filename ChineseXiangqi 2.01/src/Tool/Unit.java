package Tool;

import main.Board;

public class Unit {
    public static int coltoX(int col, int size) {
        int x = (int) (Board.rightMargin + Board.cellSize * (col - 1) - 19.2) - size/2;
        return x;
    }

    public static int rowtoY(int row, int size) {
        int y = (int) (Board.upperMargin + Board.cellSize * (row - 1) - 19.2) - size/2;
        return y;
    }
}
