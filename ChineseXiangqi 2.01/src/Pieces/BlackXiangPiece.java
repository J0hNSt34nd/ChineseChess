package Pieces;

import Ai.Ai;
import javafx.scene.image.Image;
import main.Board;

import javax.imageio.ImageIO;
import java.io.IOException;

public class BlackXiangPiece extends Piece
{
    Board board;
    //构造器
    public BlackXiangPiece(Board board,int rows,int cols)
    {
        this.board=board;

        team=0;
        _rows=rows;
        _cols=cols;
        setDefaultValues();
        getPlayerImage();

    }
    public void setDefaultValues()
    {
        radius=19.2;

        x=(int)(board.rightMargin+board.cellSize*(_cols-1)-radius);
        y=(int)(board.upperMargin+board.cellSize*(_rows-1)-radius);
    }
    public void getPlayerImage()
    {
        image = new Image(getClass().getResourceAsStream("/Xiang/elephant_black.png"));
    }
    //bmj注：此处的row和col传进来的是目标点
    public boolean slip(int row,int col)
    {
        int xPosition=0,yPosition=0;
        if(col<_cols)
        {
            xPosition=1;
        }
        else
        {
            xPosition=-1;
        }
        if(row<_rows)
        {
            yPosition=1;
        }
        else
        {
            yPosition=-1;
        }
        if(board.getPieceAt(row+yPosition,col+xPosition) != null)
        {
            return true;
        }
        return false;
    }

    public boolean isCrossedRiver(int row)
    {
        if(row>=6)
        {
            return true;
        }
        else
            return false;
    }

    @Override

    public int pieceValue() {
        return TYPE_XIANG;
    }

    public boolean canMoveTo(int newRow,int newCol,Board board)
    {
        if((Math.abs(newRow-_rows)==2) && (Math.abs(newCol-_cols)==2) && opponent(board.getPieceAt(newRow,newCol)))
        {
            return (!slip(newRow,newCol) && !isCrossedRiver(newRow));
        }
        return false;
    }

    public int[][] getPst()
    {
        return tool.verticleInverse(Ai.ELEPHANT_PST);
    }
}
