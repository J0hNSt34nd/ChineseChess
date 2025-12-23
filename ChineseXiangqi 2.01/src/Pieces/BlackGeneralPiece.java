package Pieces;

import Ai.Ai;
import javafx.scene.image.Image;
import main.Board;

import javax.imageio.ImageIO;
import java.io.IOException;

public class BlackGeneralPiece extends Piece {

    Board board;


    public BlackGeneralPiece(Board board , int rows, int cols) {

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

        image = new Image(getClass().getResourceAsStream("/General/general_black.png"));

    }
    public boolean isFaceToFace(int row, int col) {
        if(!isSameLine(row,col))
            return false;
        else
        {
            for(int i=row+1;i<=10;i++)
            {
                if(board.getPieceAt(i,col) != null)
                {
                    if(board.getPieceAt(i,col).getClass() != RedGeneralPiece.class)
                        return false;
                }
            }
            return true;
        }
    }

    public boolean isSameLine(int row,int col)
    {
        for(int i=row+1;i<=10;i++)
        {
            if(board.getPieceAt(i,col) != null)
            {
                if(board.getPieceAt(i,col).getClass() == RedGeneralPiece.class)
                    return true;
            }
        }
        return false;
    }

    public boolean canMoveTo(int newRow,int newCol, Board board)
    {
        if (Math.abs(newCol - _cols) + Math.abs(newRow - _rows) == 1 && opponent(board.getPieceAt(newRow,newCol)) && newRow <= 3 && newCol <= 6 && newCol >= 4 && !isFaceToFace(newRow, newCol))
        {
            return true;
        }

        return false;
    }

    public boolean isChecked()
    {
        for(Piece piece : board.getPieces())
        {
            if(piece.isChecking(_rows,_cols,board))
            {
                return true;
            }
        }
        return false;
    }


    public boolean isStaleMated()
    {
        if(isChecked())
        {

        }
        return false;
    }


    public int[][] getPst()
    {
        return tool.verticleInverse(Ai.GENERAL_PST);
    }

    public int pieceValue() {
        return TYPE_GENERAL;
    }


//    public void update() {
//
//    }
}
