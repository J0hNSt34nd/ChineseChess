package Pieces;

import Ai.Ai;
import Tool.Tool;
import javafx.scene.image.Image;
import main.Board;

import javax.imageio.ImageIO;
import java.io.IOException;

public class BlackCannonPiece extends Piece {

    Board board;

    public BlackCannonPiece(Board board , int rows, int cols) {

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

        image = new Image(getClass().getResourceAsStream("/Cannon/cannon_black.png"));

    }

    //bmj注：先找被挡住位置

    public int BlockedCol_Right(int startcol)
    {
        startcol=_cols;
        int endcol_right=startcol;
        for(int i=startcol;i<9;i++)
        {
            endcol_right++;
            if(board.getPieceAt(_rows,endcol_right) != null)
            {
                break;
            }
        }
        return endcol_right;
    }


    public int BlockedCol_Left(int startcol)
    {
        startcol=_cols;
        int endcol_left=startcol;
        for(int i=startcol;i>1;i--)
        {
            endcol_left--;
            if(board.getPieceAt(_rows,endcol_left) != null)
            {
                break;
            }
        }
        return endcol_left;
    }

    public int BlockedRow_Down(int startrow)
    {
        startrow=_rows;
        int endrow_down=startrow;
        for(int i=startrow;i<10;i++)
        {
            endrow_down++;
            if(board.getPieceAt(endrow_down,_cols) != null)
            {
                break;
            }
        }
        return endrow_down;
    }

    public int BlockedRow_Up(int startrow)
    {
        startrow=_rows;
        int endrow_up=startrow;
        for(int i=startrow;i>1;i--)
        {
            endrow_up--;
            if(board.getPieceAt(endrow_up,_cols) != null)
            {
                break;
            }
        }
        return endrow_up;
    }

    //bmj注：获取被挡住位置的坐标

    public int getBlockedCol_left()
    {
        return BlockedCol_Left(_cols);
    }

    public int getBlockedCol_right()
    {
        return BlockedCol_Right(_cols);
    }

    public int getBlockedRow_Down()
    {
        return BlockedRow_Down(_rows);
    }

    public int getBlockedRow_Up()
    {
        return BlockedRow_Up(_rows);
    }

    //bmj注：获取可吃子位置坐标

    public int TakenCol_Left()
    {
        int tempCol_left=getBlockedCol_left();
        int endCol_left=tempCol_left;
        for(int i=tempCol_left;i>1;i--)
        {
            endCol_left--;
            if(board.getPieceAt(_rows,endCol_left) != null)
            {
                if(opponent(board.getPieceAt(_rows,endCol_left)))
                    return endCol_left;
                else
                    return 0;
            }
        }
        return 0;
    }

    public int TakenCol_Right()
    {
        int tempCol_right=getBlockedCol_right();
        int endCol_right=tempCol_right;
        for(int i=tempCol_right;i<9;i++)
        {
            endCol_right++;
            if( (board.getPieceAt(_rows,endCol_right) != null))
            {
                if(opponent(board.getPieceAt(_rows,endCol_right)))
                    return endCol_right;
                else
                    return 0;
            }
        }
        return 0;
    }

    public int TakenRow_Down()
    {
        int tempRow_down=getBlockedRow_Down();
        int endRow_down=tempRow_down;
        for(int i=tempRow_down;i<10;i++)
        {
            endRow_down++;
            if(board.getPieceAt(endRow_down,_cols) != null)
            {
                if(opponent(board.getPieceAt(endRow_down,_cols)))
                    return endRow_down;
                else
                    return 0;
            }
        }
        return 0;
    }

    public int TakenRow_Up()
    {
        int tempRow_up=getBlockedRow_Up();
        int endRow_up=tempRow_up;
        for(int i=tempRow_up;i>1;i--)
        {
            endRow_up--;
            if(board.getPieceAt(endRow_up,_cols) != null)
            {
                if(opponent(board.getPieceAt(endRow_up,_cols)))
                    return endRow_up;
                else
                    return 0;
            }
        }
        return 0;
    }

    //bmj注：判断可移动位置

    public boolean isMoveable(int toRow,int toCol)
    {
        if(toRow==_rows)
        {
            if(toCol<=BlockedCol_Right(_cols) && toCol>=BlockedCol_Left(_cols) && (board.getPieceAt(toRow,toCol) == null))
            {
                return true;
            }
        }
        if (toCol == _cols)
        {
            if(toRow<=BlockedRow_Down(_rows) && toRow>=BlockedRow_Up(_rows) && (board.getPieceAt(toRow,toCol) == null))
            {
                return true;
            }
        }
        return false;
    }

    //bmj注：判断可吃子位置

    public boolean isTaken(int targetRow,int targetCol)
    {

        if(targetRow==_rows)
        {
            if((targetCol==TakenCol_Left()) || (targetCol==TakenCol_Right()) && opponent(board.getPieceAt(targetRow,targetCol)))
            {
                return true;
            }
        }
        if(targetCol==_cols)
        {
            if((targetRow==TakenRow_Up()) || (targetRow==TakenRow_Down()) && opponent(board.getPieceAt(targetRow,targetCol)))
            {
                return  true;
            }
        }
        return false;
    }

    @Override

    public boolean canMoveTo(int newRow,int newCol,Board board)
    {
        return (isMoveable(newRow,newCol) || isTaken(newRow,newCol));
    }

    public int pieceValue() {
        return TYPE_CANNON;
    }

    public int[][] getPst()
    {
        return tool.verticleInverse(Ai.CANNON_PST);
    }
}