package Pieces;

import Ai.Ai;
import javafx.scene.image.Image;
import main.Board;

import javax.imageio.ImageIO;
import java.io.IOException;

public class BlackSoldierPiece extends Piece {

    Board board;


    public BlackSoldierPiece(Board board , int rows, int cols) {

        this.board = board;

        team = 0;
        _rows = rows;
        _cols = cols;


        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {

        radius = 19.2;

        x = (int) (board.rightMargin + board.cellSize * (_cols - 1) - radius);
                //(int) board.rightBufferSize;
        y = (int) (board.upperMargin + board.cellSize * (_rows - 1) - radius);
    }

    public void getPlayerImage() {

        image = new Image(getClass().getResourceAsStream("/soldier/soldier_black.png"));

    }

    @Override

    public int pieceValue() {
        return TYPE_SOLDIER;
    }

    public boolean canMoveTo(int newRow,int newCol, Board board) {
        if (_rows < 6) {
            if (newRow - _rows == 1 && newCol == _cols && opponent(board.getPieceAt(newRow,newCol)))
                return true;
        }
        else {
            if (newRow - _rows + Math.abs(newCol - _cols) == 1 && Math.abs(newCol - _cols) < 2 && opponent(board.getPieceAt(newRow,newCol)))
                return true;
        }
        return false;
    }

    public int[][] getPst()
    {
        return tool.verticleInverse(Ai.SOLDIER_PST);
    }

//    public void update() {
//
//    }
}
