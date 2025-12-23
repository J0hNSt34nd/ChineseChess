package Ai;

import Pieces.Piece;
import main.Board;

public class Move {
    simPiece piece;
    SimBoard simboard;
    simPiece eatenPiece;

    private int capturedPieceSign = 0;
    private int capturedRow = -1;
    private int capturedCol = -1;

    public void setCapturedInfo(int sign, int r, int c) {
        this.capturedPieceSign = sign;
        this.capturedRow = r;
        this.capturedCol = c;
    }

    public int getCapturedCol() {
        return capturedCol;
    }

    public int getCapturedPieceSign() {
        return capturedPieceSign;
    }

    public int getCapturedRow() {
        return capturedRow;
    }


    int oriRow, oriCol;
    int newRow, newCol;

    public int getNewCol() {
        return newCol;
    }

    public int getNewRow() {
        return newRow;
    }

    public int getOriCol() {
        return oriCol;
    }

    public int getOriRow() {
        return oriRow;
    }

    public Move(int orirow, int oricol, int newrow, int newcol) {
        this.oriRow = orirow;
        this.oriCol = oricol;
        this.newRow = newrow;
        this.newCol = newcol;
    }

    public Move(int orirow, int oricol, int newrow, int newcol, SimBoard simboard) {

        this.simboard = simboard;
        this.oriRow = orirow;
        this.oriCol = oricol;
        this.newRow = newrow;
        this.newCol = newcol;

        this.piece = simboard.getPieceAt(oriRow, oriCol);
    }

    public void stimulate(SimBoard simboard) {

        eatenPiece = simboard.pieces[newRow - 1][newCol - 1];

        simboard.pieces[newRow - 1][newCol - 1] = piece;
        simboard.pieces[oriRow - 1][oriCol - 1] = null;

        if (piece != null) {
            piece.updatePosition(newRow, newCol);
        }
    }

    public void redo(SimBoard simboard) {

        simboard.pieces[oriRow - 1][oriCol - 1] = this.piece;
        simboard.pieces[newRow - 1][newCol - 1] = this.eatenPiece;


        if (this.piece != null) {

            this.piece.updatePosition(oriRow, oriCol);
        }

    }
}