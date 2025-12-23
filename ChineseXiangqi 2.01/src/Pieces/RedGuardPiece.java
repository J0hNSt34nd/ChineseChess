package Pieces;

import Ai.Ai;
import javafx.scene.image.Image;
import main.Board;

import javax.imageio.ImageIO;
import java.io.IOException;

public class RedGuardPiece extends Piece {

    Board board;


    public RedGuardPiece(Board board , int rows, int cols) {

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

        image = new Image(getClass().getResourceAsStream("/Guard/advisor_red.png"));

    }

    @Override
    public int pieceValue() {
        return TYPE_GUARD;
    }

    public boolean canMoveTo(int newRow,int newCol, Board board) {
        if (Math.abs(newCol - _cols) == 1 & Math.abs(newRow - _rows) == 1 && opponent(board.getPieceAt(newRow,newCol)) && newRow >= 8 && newCol <= 6 && newCol >= 4) {
            return true;
        }

        return false;
    }

    public int[][] getPst()
    {
        return Ai.ADVISOR_PST;
    }


//    public void update() {
//
//    }
}
