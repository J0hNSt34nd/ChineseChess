package Ai;

import Tool.Tool;

import java.util.ArrayList;
import java.util.List;

public class SimBoard {
    simPiece[][] pieces;

    private int rows = 10;
    private int cols = 9;

    public SimBoard(int rows, int cols) {
        pieces = new simPiece[rows][cols]; // 棋盘行列从1开始计数
    }

    public void setPiece(int row, int col, simPiece piece) {
        pieces[row - 1][col - 1] = piece;
    }

    public simPiece getPieceAt(int row, int col) {
        if (row < 1 || row > pieces.length || col < 1 || col > pieces[0].length) {
            return null;
        }
        return pieces[row - 1][col - 1];
    }

    public void reset() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                pieces[i][j] = null;
            }
        }
    }



    public List<Move> generateAllMoves() {
        List<Move> moves = new ArrayList<>(280);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                simPiece piece = this.getPieceAt(i + 1, j + 1);
                if (piece != null) {
                    for (int k = 0; k < rows; k++) {
                        for (int g = 0; g < cols; g++) {
                            if (piece.canMoveTo(k + 1, g + 1, this)) {
                                moves.add(new Move(i + 1, j + 1, k + 1, g + 1, this));
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    public void printDebugBoard() {
        System.out.println("\n=== [AI Internal SimBoard State] ===");
        System.out.println("   1  2  3  4  5  6  7  8  9  (Col)");
        System.out.println("  ---------------------------");

        for (int i = 1; i <= rows; i++) {
            System.out.printf("%2d|", i); // 打印行号
            for (int j = 1; j <= cols; j++) {
                simPiece p = getPieceAt(i, j);
                System.out.print(getPieceName(p) + " ");
            }
            System.out.println("|" + i);
        }
        System.out.println("  ---------------------------");
        System.out.println("====================================\n");


    }


    private String getPieceName(simPiece p) {
        if (p == null) return " ."; // 空位

        if (p.getTeam() == 1) {
            if (p instanceof RedGeneralPiece) return "帅";
            if (p instanceof RedGuardPiece)   return "仕";
            if (p instanceof RedXiangPiece)   return "相";
            if (p instanceof RedHorsePiece)   return "傌";
            if (p instanceof RedTankPiece)    return "俥";
            if (p instanceof RedCannonPiece)  return "炮";
            if (p instanceof RedSoldierPiece) return "兵";
            return "红";
        }
        else {
            if (p instanceof BlackGeneralPiece) return "将";
            if (p instanceof BlackGuardPiece)   return "士";
            if (p instanceof BlackXiangPiece)   return "象";
            if (p instanceof BlackHorsePiece)   return "马";
            if (p instanceof BlackTankPiece)    return "车";
            if (p instanceof BlackCannonPiece)  return "炮";
            if (p instanceof BlackSoldierPiece) return "卒";
            return "黑";
        }
    }

}

abstract class simPiece {

    Tool tool = new Tool();

    public static final int TYPE_GENERAL = 1000000;
    public static final int TYPE_CANNON = 500;
    public static final int TYPE_TANK = 900;
    public static final int TYPE_HORSE = 450;
    public static final int TYPE_XIANG = 200;
    public static final int TYPE_GUARD = 200;
    public static final int TYPE_SOLDIER = 100;

    public int _rows;
    public int _cols;
    protected int team;

    private SimBoard simBoard;

    public void set_cols(int _cols) {
        this._cols = _cols;
    }

    public void set_rows(int _rows) {
        this._rows = _rows;
    }

    public simPiece(int rows, int cols, int team) {
        this._rows = rows;
        this._cols = cols;
        this.team = team;
    }

    public void updatePosition(int row, int col) {
        this._rows = row;
        this._cols = col;
    }

    protected boolean opponent(simPiece piece) {
        if (piece == null) {
            return true;
        }
        return piece.team != this.team;
    }

    public abstract boolean canMoveTo(int newRow, int newCol, SimBoard board);

    public abstract int getPieceValue();

    public int[][] getPst() {
        return null;
    }

    public int getRows() { return _rows; }
    public int getCols() { return _cols; }
    public int getTeam() { return team; }

    public boolean Protected() {
        for (int r = 0; r < 10; r++) {
            for (simPiece piece : simBoard.pieces[r]) {
                if (piece.team != this.team && piece.canMoveTo(this._rows,this._cols, simBoard)){
                    if (piece.team == this.team && !piece.canMoveTo(this._rows,this._cols, simBoard)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

class RedTankPiece extends simPiece {

    public RedTankPiece(int rows, int cols) {
        super(rows, cols, 1);
    }

    @Override
    public int getPieceValue() {
        return TYPE_TANK;
    }

    private int blockedColRight(SimBoard board) {
        int endCol = _cols;
        for (int i = _cols + 1; i <= 9; i++) {
            endCol = i;
            if (board.getPieceAt(_rows, i) != null) {
                break;
            }
        }
        return endCol;
    }

    private int blockedColLeft(SimBoard board) {
        int endCol = _cols;
        for (int i = _cols - 1; i >= 1; i--) {
            endCol = i;
            if (board.getPieceAt(_rows, i) != null) {
                break;
            }
        }
        return endCol;
    }

    private int blockedRowDown(SimBoard board) {
        int endRow = _rows;
        for (int i = _rows + 1; i <= 10; i++) {
            endRow = i;
            if (board.getPieceAt(i, _cols) != null) {
                break;
            }
        }
        return endRow;
    }

    private int blockedRowUp(SimBoard board) {
        int endRow = _rows;
        for (int i = _rows - 1; i >= 1; i--) {
            endRow = i;
            if (board.getPieceAt(i, _cols) != null) {
                break;
            }
        }
        return endRow;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        if (newRow == _rows) {
            int left = blockedColLeft(board);
            int right = blockedColRight(board);
            return newCol >= left && newCol <= right && opponent(board.getPieceAt(newRow, newCol));
        }
        if (newCol == _cols) {
            int up = blockedRowUp(board);
            int down = blockedRowDown(board);
            return newRow >= up && newRow <= down && opponent(board.getPieceAt(newRow, newCol));
        }
        return false;
    }

    @Override
    public int[][] getPst() {
        return Ai.CHARIOT_PST;
    }
}

class BlackTankPiece extends simPiece {
    public BlackTankPiece(int rows, int cols) {
        super(rows, cols, 0);
    }

    @Override
    public int getPieceValue() {
        return TYPE_TANK;
    }

    private int blockedColRight(SimBoard board) {
        int endCol = _cols;
        for (int i = _cols + 1; i <= 9; i++) {
            endCol = i;
            if (board.getPieceAt(_rows, i) != null) {
                break;
            }
        }
        return endCol;
    }

    private int blockedColLeft(SimBoard board) {
        int endCol = _cols;
        for (int i = _cols - 1; i >= 1; i--) {
            endCol = i;
            if (board.getPieceAt(_rows, i) != null) {
                break;
            }
        }
        return endCol;
    }

    private int blockedRowDown(SimBoard board) {
        int endRow = _rows;
        for (int i = _rows + 1; i <= 10; i++) {
            endRow = i;
            if (board.getPieceAt(i, _cols) != null) {
                break;
            }
        }
        return endRow;
    }

    private int blockedRowUp(SimBoard board) {
        int endRow = _rows;
        for (int i = _rows - 1; i >= 1; i--) {
            endRow = i;
            if (board.getPieceAt(i, _cols) != null) {
                break;
            }
        }
        return endRow;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        if (newRow == _rows) {
            int left = blockedColLeft(board);
            int right = blockedColRight(board);
            return newCol >= left && newCol <= right && opponent(board.getPieceAt(newRow, newCol));
        }
        if (newCol == _cols) {
            int up = blockedRowUp(board);
            int down = blockedRowDown(board);
            return newRow >= up && newRow <= down && opponent(board.getPieceAt(newRow, newCol));
        }
        return false;
    }

    @Override
    public int[][] getPst() {
        return Ai.CHARIOT_PST;
    }
}

class RedCannonPiece extends simPiece {
    public RedCannonPiece(int rows, int cols) {
        super(rows, cols, 1);
    }

    @Override
    public int getPieceValue() {
        return TYPE_CANNON;
    }

    private int blockedColRight(SimBoard board) {
        int endCol = _cols;
        for (int i = _cols + 1; i <= 9; i++) {
            endCol = i;
            if (board.getPieceAt(_rows, i) != null) {
                break;
            }
        }
        return endCol;
    }

    private int blockedColLeft(SimBoard board) {
        int endCol = _cols;
        for (int i = _cols - 1; i >= 1; i--) {
            endCol = i;
            if (board.getPieceAt(_rows, i) != null) {
                break;
            }
        }
        return endCol;
    }

    private int blockedRowDown(SimBoard board) {
        int endRow = _rows;
        for (int i = _rows + 1; i <= 10; i++) {
            endRow = i;
            if (board.getPieceAt(i, _cols) != null) {
                break;
            }
        }
        return endRow;
    }

    private int blockedRowUp(SimBoard board) {
        int endRow = _rows;
        for (int i = _rows - 1; i >= 1; i--) {
            endRow = i;
            if (board.getPieceAt(i, _cols) != null) {
                break;
            }
        }
        return endRow;
    }

    private int takenColLeft(SimBoard board) {
        int tempCol = blockedColLeft(board);
        for (int i = tempCol - 1; i >= 1; i--) {
            simPiece p = board.getPieceAt(_rows, i);
            if (p != null) {
                return opponent(p) ? i : 0;
            }
        }
        return 0;
    }

    private int takenColRight(SimBoard board) {
        int tempCol = blockedColRight(board);
        for (int i = tempCol + 1; i <= 9; i++) {
            simPiece p = board.getPieceAt(_rows, i);
            if (p != null) {
                return opponent(p) ? i : 0;
            }
        }
        return 0;
    }

    private int takenRowUp(SimBoard board) {
        int tempRow = blockedRowUp(board);
        for (int i = tempRow - 1; i >= 1; i--) {
            simPiece p = board.getPieceAt(i, _cols);
            if (p != null) {
                return opponent(p) ? i : 0;
            }
        }
        return 0;
    }

    private int takenRowDown(SimBoard board) {
        int tempRow = blockedRowDown(board);
        for (int i = tempRow + 1; i <= 10; i++) {
            simPiece p = board.getPieceAt(i, _cols);
            if (p != null) {
                return opponent(p) ? i : 0;
            }
        }
        return 0;
    }

    private boolean isMoveable(int newRow, int newCol, SimBoard board) {
        if (newRow == _rows) {
            int left = blockedColLeft(board);
            int right = blockedColRight(board);
            return newCol >= left && newCol <= right && board.getPieceAt(newRow, newCol) == null;
        }
        if (newCol == _cols) {
            int up = blockedRowUp(board);
            int down = blockedRowDown(board);
            return newRow >= up && newRow <= down && board.getPieceAt(newRow, newCol) == null;
        }
        return false;
    }

    private boolean isTaken(int newRow, int newCol, SimBoard board) {
        if (newRow == _rows) {
            int left = takenColLeft(board);
            int right = takenColRight(board);
            return (newCol == left || newCol == right) && opponent(board.getPieceAt(newRow, newCol));
        }
        if (newCol == _cols) {
            int up = takenRowUp(board);
            int down = takenRowDown(board);
            return (newRow == up || newRow == down) && opponent(board.getPieceAt(newRow, newCol));
        }
        return false;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        return isMoveable(newRow, newCol, board) || isTaken(newRow, newCol, board);
    }

    @Override
    public int[][] getPst() {
        return Ai.CANNON_PST;
    }
}

class BlackCannonPiece extends simPiece {
    public BlackCannonPiece(int rows, int cols) {
        super(rows, cols, 0);
    }

    @Override
    public int getPieceValue() {
        return TYPE_CANNON;
    }

    private int blockedColRight(SimBoard board) {
        int endCol = _cols;
        for (int i = _cols + 1; i <= 9; i++) {
            endCol = i;
            if (board.getPieceAt(_rows, i) != null) {
                break;
            }
        }
        return endCol;
    }

    private int blockedColLeft(SimBoard board) {
        int endCol = _cols;
        for (int i = _cols - 1; i >= 1; i--) {
            endCol = i;
            if (board.getPieceAt(_rows, i) != null) {
                break;
            }
        }
        return endCol;
    }

    private int blockedRowDown(SimBoard board) {
        int endRow = _rows;
        for (int i = _rows + 1; i <= 10; i++) {
            endRow = i;
            if (board.getPieceAt(i, _cols) != null) {
                break;
            }
        }
        return endRow;
    }

    private int blockedRowUp(SimBoard board) {
        int endRow = _rows;
        for (int i = _rows - 1; i >= 1; i--) {
            endRow = i;
            if (board.getPieceAt(i, _cols) != null) {
                break;
            }
        }
        return endRow;
    }

    private int takenColLeft(SimBoard board) {
        int tempCol = blockedColLeft(board);
        for (int i = tempCol - 1; i >= 1; i--) {
            simPiece p = board.getPieceAt(_rows, i);
            if (p != null) {
                return opponent(p) ? i : 0;
            }
        }
        return 0;
    }

    private int takenColRight(SimBoard board) {
        int tempCol = blockedColRight(board);
        for (int i = tempCol + 1; i <= 9; i++) {
            simPiece p = board.getPieceAt(_rows, i);
            if (p != null) {
                return opponent(p) ? i : 0;
            }
        }
        return 0;
    }

    private int takenRowUp(SimBoard board) {
        int tempRow = blockedRowUp(board);
        for (int i = tempRow - 1; i >= 1; i--) {
            simPiece p = board.getPieceAt(i, _cols);
            if (p != null) {
                return opponent(p) ? i : 0;
            }
        }
        return 0;
    }

    private int takenRowDown(SimBoard board) {
        int tempRow = blockedRowDown(board);
        for (int i = tempRow + 1; i <= 10; i++) {
            simPiece p = board.getPieceAt(i, _cols);
            if (p != null) {
                return opponent(p) ? i : 0;
            }
        }
        return 0;
    }

    private boolean isMoveable(int newRow, int newCol, SimBoard board) {
        if (newRow == _rows) {
            int left = blockedColLeft(board);
            int right = blockedColRight(board);
            return newCol >= left && newCol <= right && board.getPieceAt(newRow, newCol) == null;
        }
        if (newCol == _cols) {
            int up = blockedRowUp(board);
            int down = blockedRowDown(board);
            return newRow >= up && newRow <= down && board.getPieceAt(newRow, newCol) == null;
        }
        return false;
    }

    private boolean isTaken(int newRow, int newCol, SimBoard board) {
        if (newRow == _rows) {
            int left = takenColLeft(board);
            int right = takenColRight(board);
            return (newCol == left || newCol == right) && opponent(board.getPieceAt(newRow, newCol));
        }
        if (newCol == _cols) {
            int up = takenRowUp(board);
            int down = takenRowDown(board);
            return (newRow == up || newRow == down) && opponent(board.getPieceAt(newRow, newCol));
        }
        return false;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        return isMoveable(newRow, newCol, board) || isTaken(newRow, newCol, board);
    }

    @Override
    public int[][] getPst() {
        return Ai.CANNON_PST;
    }
}

class RedHorsePiece extends simPiece {
    public RedHorsePiece(int rows, int cols) {
        super(rows, cols, 1);
    }

    @Override
    public int getPieceValue() {
        return TYPE_HORSE;
    }

    private boolean slip(int newRow, int newCol, SimBoard board) {
        int xDiff = newCol - _cols;
        int yDiff = newRow - _rows;
        int slipRow = _rows;
        int slipCol = _cols;

        if (Math.abs(xDiff) == 1 && Math.abs(yDiff) == 2) {
            slipRow = _rows + (yDiff > 0 ? 1 : -1);
        } else if (Math.abs(xDiff) == 2 && Math.abs(yDiff) == 1) {
            slipCol = _cols + (xDiff > 0 ? 1 : -1);
        }
        return board.getPieceAt(slipRow, slipCol) != null;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        int xDiff = Math.abs(newCol - _cols);
        int yDiff = Math.abs(newRow - _rows);
        if (xDiff * yDiff != 2) {
            return false;
        }
        return !slip(newRow, newCol, board) && opponent(board.getPieceAt(newRow, newCol));
    }

    @Override
    public int[][] getPst() {
        return Ai.HORSE_PST;
    }
}

class BlackHorsePiece extends simPiece {
    public BlackHorsePiece(int rows, int cols) {
        super(rows, cols, 0);
    }

    @Override
    public int getPieceValue() {
        return TYPE_HORSE;
    }

    private boolean slip(int newRow, int newCol, SimBoard board) {
        int xDiff = newCol - _cols;
        int yDiff = newRow - _rows;
        int slipRow = _rows;
        int slipCol = _cols;

        if (Math.abs(xDiff) == 1 && Math.abs(yDiff) == 2) {
            slipRow = _rows + (yDiff > 0 ? 1 : -1);
        } else if (Math.abs(xDiff) == 2 && Math.abs(yDiff) == 1) {
            slipCol = _cols + (xDiff > 0 ? 1 : -1);
        }
        return board.getPieceAt(slipRow, slipCol) != null;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        int xDiff = Math.abs(newCol - _cols);
        int yDiff = Math.abs(newRow - _rows);
        if (xDiff * yDiff != 2) {
            return false;
        }
        return !slip(newRow, newCol, board) && opponent(board.getPieceAt(newRow, newCol));
    }

    @Override
    public int[][] getPst() {
        return Ai.HORSE_PST;
    }
}

class RedXiangPiece extends simPiece {
    public RedXiangPiece(int rows, int cols) {
        super(rows, cols, 1);
    }

    @Override
    public int getPieceValue() {
        return TYPE_XIANG;
    }

    private boolean isCrossedRiver(int newRow) {
        return newRow <= 5;
    }

    private boolean slip(int newRow, int newCol, SimBoard board) {
        int midRow = _rows + (newRow - _rows) / 2;
        int midCol = _cols + (newCol - _cols) / 2;
        return board.getPieceAt(midRow, midCol) != null;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        int xDiff = Math.abs(newCol - _cols);
        int yDiff = Math.abs(newRow - _rows);
        if (xDiff != 2 || yDiff != 2) {
            return false;
        }
        return !isCrossedRiver(newRow) && !slip(newRow, newCol, board) && opponent(board.getPieceAt(newRow, newCol));
    }

    @Override
    public int[][] getPst() {
        return Ai.ELEPHANT_PST;
    }
}

class BlackXiangPiece extends simPiece {
    public BlackXiangPiece(int rows, int cols) {
        super(rows, cols, 0);
    }

    @Override
    public int getPieceValue() {
        return TYPE_XIANG;
    }

    private boolean isCrossedRiver(int newRow) {
        return newRow >= 6;
    }

    private boolean slip(int newRow, int newCol, SimBoard board) {
        int midRow = _rows + (newRow - _rows) / 2;
        int midCol = _cols + (newCol - _cols) / 2;
        return board.getPieceAt(midRow, midCol) != null;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        int xDiff = Math.abs(newCol - _cols);
        int yDiff = Math.abs(newRow - _rows);
        if (xDiff != 2 || yDiff != 2) {
            return false;
        }
        return !isCrossedRiver(newRow) && !slip(newRow, newCol, board) && opponent(board.getPieceAt(newRow, newCol));
    }

    @Override
    public int[][] getPst() {
        return Ai.ELEPHANT_PST;
    }
}

class RedGuardPiece extends simPiece {
    public RedGuardPiece(int rows, int cols) {
        super(rows, cols, 1);
    }

    @Override
    public int getPieceValue() {
        return TYPE_GUARD;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        int xDiff = Math.abs(newCol - _cols);
        int yDiff = Math.abs(newRow - _rows);
        if (xDiff != 1 || yDiff != 1) {
            return false;
        }
        return newRow >= 8 && newRow <= 10 && newCol >= 4 && newCol <= 6 && opponent(board.getPieceAt(newRow, newCol));
    }

    @Override
    public int[][] getPst() {
        return Ai.ADVISOR_PST;
    }
}

class BlackGuardPiece extends simPiece {
    public BlackGuardPiece(int rows, int cols) {
        super(rows, cols, 0);
    }

    @Override
    public int getPieceValue() {
        return TYPE_GUARD;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        int xDiff = Math.abs(newCol - _cols);
        int yDiff = Math.abs(newRow - _rows);
        if (xDiff != 1 || yDiff != 1) {
            return false;
        }
        return newRow >= 1 && newRow <= 3 && newCol >= 4 && newCol <= 6 && opponent(board.getPieceAt(newRow, newCol));
    }
    @Override
    public int[][] getPst() {
        return Ai.ADVISOR_PST;
    }
}

class RedGeneralPiece extends simPiece {

    public RedGeneralPiece(int rows, int cols) {
        super(rows, cols, 1); // 1 代表红方
    }

    @Override
    public int getPieceValue() {
        return TYPE_GENERAL;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        int xDiff = Math.abs(newCol - _cols);
        int yDiff = Math.abs(newRow - _rows);
        if (xDiff + yDiff != 1) {
            return false;
        }

        if (newRow < 8 || newRow > 10 || newCol < 4 || newCol > 6) {
            return false;
        }

        if (!opponent(board.getPieceAt(newRow, newCol))) {
            return false;
        }

        int blackGenRow = -1;
        int blackGenCol = -1;

        searchLoop:
        for (int r = 1; r <= 3; r++) {
            for (int c = 4; c <= 6; c++) {
                simPiece p = board.getPieceAt(r, c);
                if (p != null && p instanceof BlackGeneralPiece) {
                    blackGenRow = r;
                    blackGenCol = c;
                    break searchLoop;
                }
            }
        }

        if (blackGenRow != -1 && newCol == blackGenCol) {
            boolean hasObstacle = false;

            for (int r = blackGenRow + 1; r < newRow; r++) {
                if (board.getPieceAt(r, newCol) != null) {
                    hasObstacle = true;
                    break;
                }
            }


            if (!hasObstacle) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int[][] getPst() {
        return Ai.GENERAL_PST;
    }
}

class BlackGeneralPiece extends simPiece {

    public BlackGeneralPiece(int rows, int cols) {
        super(rows, cols, 0); // 0 代表黑方
    }

    @Override
    public int getPieceValue() {
        return TYPE_GENERAL;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        int xDiff = Math.abs(newCol - _cols);
        int yDiff = Math.abs(newRow - _rows);
        if (xDiff + yDiff != 1) {
            return false;
        }

        if (newRow < 1 || newRow > 3 || newCol < 4 || newCol > 6) {
            return false;
        }

        if (!opponent(board.getPieceAt(newRow, newCol))) {
            return false;
        }

        int redGenRow = -1;
        int redGenCol = -1;

        searchLoop:
        for (int r = 8; r <= 10; r++) {
            for (int c = 4; c <= 6; c++) {
                simPiece p = board.getPieceAt(r, c);
                if (p != null && p instanceof RedGeneralPiece) {
                    redGenRow = r;
                    redGenCol = c;
                    break searchLoop;
                }
            }
        }

        if (redGenRow != -1 && newCol == redGenCol) {
            boolean hasObstacle = false;

            for (int r = newRow + 1; r < redGenRow; r++) {
                if (board.getPieceAt(r, newCol) != null) {
                    hasObstacle = true;
                    break;
                }
            }

            if (!hasObstacle) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int[][] getPst() {
        return Ai.GENERAL_PST;
    }
}

class RedSoldierPiece extends simPiece {
    public RedSoldierPiece(int rows, int cols) {
        super(rows, cols, 1);
    }

    @Override
    public int getPieceValue() {
        return TYPE_SOLDIER;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        int xDiff = Math.abs(newCol - _cols);
        int yDiff = _rows - newRow;

        if (_rows > 5) {
            return yDiff == 1 && xDiff == 0 && opponent(board.getPieceAt(newRow, newCol));
        }
        else {
            return (yDiff == 1 && xDiff == 0) || (yDiff == 0 && xDiff == 1) && opponent(board.getPieceAt(newRow, newCol));
        }
    }

    @Override
    public int[][] getPst() {
        return Ai.SOLDIER_PST;
    }
}

class BlackSoldierPiece extends simPiece {
    public BlackSoldierPiece(int rows, int cols) {
        super(rows, cols, 0);
    }

    @Override
    public int getPieceValue() {
        return TYPE_SOLDIER;
    }

    @Override
    public boolean canMoveTo(int newRow, int newCol, SimBoard board) {
        int xDiff = Math.abs(newCol - _cols);
        int yDiff = newRow - _rows;

        if (_rows < 6) {
            return yDiff == 1 && xDiff == 0 && opponent(board.getPieceAt(newRow, newCol));
        }
        else {
            return (yDiff == 1 && xDiff == 0) || (yDiff == 0 && xDiff == 1) && opponent(board.getPieceAt(newRow, newCol));
        }
    }

    @Override
    public int[][] getPst() {
        return Ai.SOLDIER_PST;
    }
}