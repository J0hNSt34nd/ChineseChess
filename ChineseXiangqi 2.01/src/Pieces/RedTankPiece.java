package Pieces;

import Ai.Ai;
import javafx.scene.image.Image;
import main.Board;

import javax.imageio.ImageIO;
import java.io.IOException;

public class RedTankPiece extends Piece
{
    Board board;
    //构造器
    public RedTankPiece(Board board,int rows,int cols)
    {
        this.board=board;

        team=1;
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

        chosable = true;
    }
    public void getPlayerImage()
    {
        image= new Image(getClass().getResourceAsStream("/Tank/chariot_red.png"));
    }

    //bmj注：以下所有Blocked位置包含Blocked的位置本身，而不是前一个位置
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

    @Override

    public int pieceValue() {
        return TYPE_TANK;
    }

    public boolean canMoveTo(int newRow,int newCol,Board board)
    {

        if(newRow==_rows)
        {
            if(newCol<=BlockedCol_Right(_cols) && newCol>=BlockedCol_Left(_cols) && opponent(board.getPieceAt(newRow,newCol)))
            {
                return true;
            }
        }
        if (newCol == _cols)
        {
            if(newRow<=BlockedRow_Down(_rows) && newRow>=BlockedRow_Up(_rows) && opponent(board.getPieceAt(newRow,newCol)))
            {
                return true;
            }
        }
        return false;
    }

    public int[][] getPst()
    {
        return Ai.CHARIOT_PST;
    }
}
