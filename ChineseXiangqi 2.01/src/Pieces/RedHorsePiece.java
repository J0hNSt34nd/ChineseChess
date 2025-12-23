package Pieces;

import Ai.Ai;
import javafx.scene.image.Image;
import main.Board;

import javax.imageio.ImageIO;
import java.io.IOException;

public class RedHorsePiece extends Piece {
    Board board;


    public RedHorsePiece(Board board , int rows, int cols) {

        this.board = board;

        team = 1;
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

        chosable = true;
    }

    public void getPlayerImage() {

        image = new Image(getClass().getResourceAsStream("/Horse/horse_red.png"));

    }

    public boolean slip(int row, int col) {
        int xPosition = 0;
        int yPosition = 0;
        if (col < _cols) {

            xPosition = 1;
        }else{

            xPosition = -1;
        }
        if (row < _rows)
            yPosition = 1;
        else
            yPosition = -1;

        if (board.getPieceAt(row + yPosition, col + xPosition) != null)
            return true;
        return false;
    }

    @Override
    public int pieceValue() {
        return TYPE_HORSE;
    }

    public boolean canMoveTo(int newRow,int newCol, Board board) {
        if (Math.abs((newRow - _rows)*(newCol - _cols)) == 2 && opponent(board.getPieceAt(newRow,newCol))) {
            return !slip(newRow, newCol);
        }
        return false;
    }

    public int[][] getPst()
    {
        return Ai.HORSE_PST;
    }
}
