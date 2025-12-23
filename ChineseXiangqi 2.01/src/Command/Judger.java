package Command;

import Effect.JavaFXSound;
import Pieces.*;
import javafx.scene.control.Alert;
import main.Board;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Judger {
    public static boolean judge (Board board)
    {
        BlackGeneralPiece bg = null;
        RedGeneralPiece rg = null;
        for(Piece piece : board.getPieces())
        {
            if(piece.getClass()==BlackGeneralPiece.class)
            {
                 bg= (BlackGeneralPiece) piece;

                 if(bg.isChecked())
                 {
                     JavaFXSound.check();
                     System.out.println("judger判断黑方正在被将军");
                     if(isCheckMated(board,bg,0))
                     {
                         System.out.println("游戏结束，红方获胜");
                         GameOver(board);
                         showInformation("游戏结束","黑方被将死，红方获胜");
                         return true;
                         //JOptionPane.showMessageDialog(null,"黑方被将死，红方获胜","游戏结束",JOptionPane.ERROR_MESSAGE);
                     }
                     else
                     {
                         break;
                     }
                 }
            }
        }

        for(Piece piece : board.getPieces())
        {
            if(piece.getClass()==RedGeneralPiece.class)
            {
                rg = (RedGeneralPiece) piece;
                if(rg.isChecked())
                {
                    System.out.println("judger判断红方正在被将军");

                    if (isCheckMated(board,rg,1))
                    {
                        System.out.println("游戏结束，黑方获胜");
                        GameOver(board);
                        showInformation("游戏结束","红方被将死，黑方获胜");
                        return true;
                        //JOptionPane.showMessageDialog(null,"红方被将死，黑方获胜","游戏结束",JOptionPane.ERROR_MESSAGE);
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }


        int blackGeneral = 0;
        int redGeneral = 0;
        for (Piece piece : board.getPieces())
        {
            if (piece.getClass() == BlackGeneralPiece.class)
                blackGeneral++;
            else if (piece.getClass() == RedGeneralPiece.class)
                redGeneral++;
        }

        if (redGeneral == 0)
        {
            System.out.println("游戏结束，黑方获胜");
            for (Piece piece : board.getPieces())
            {
                piece.chosable = false;
                return true;
            }
        }
        else if (blackGeneral == 0) {
            System.out.println("游戏结束，红方获胜");
            for (Piece piece : board.getPieces()) {
                piece.chosable = false;
                return true;
            }
        }
        return false;
    }

    //被将死锁棋子
    public static void GameOver(Board board)
    {
        for(Piece piece : board.getPieces())
        {
            piece.chosable = false;
        }

    }

    public static boolean isCheckMated(Board board,Piece generalPiece,int team)
    {
        //获取正在将的棋子
        List<Piece> checkingPieces = getcheckingPieces(board,generalPiece.getRows(),generalPiece.getCols(),1-team);

        if(checkingPieces.isEmpty())
        {
            return false;
        }

        if(canEatAttacker(board,generalPiece,checkingPieces))
        {
            System.out.println("可以吃掉");
            return false;
        }

        if(canBlockStrike(board,generalPiece,checkingPieces))
        {
            System.out.println("可以挡住");
            return false;
        }

        if(canNeutralizeCannonAttack(board,generalPiece,checkingPieces,generalPiece.getTeam()))
        {
            System.out.println("可以拆炮架");
            return false;
        }

        if(canEscape(board,generalPiece,checkingPieces))
        {
            System.out.println("能够逃走");
            return false;
        }






        System.out.println("被将死");
        return true;
    }
    private static List<Piece> getcheckingPieces(Board board,int checkingRow,int checkingCol,int anotherTeam)
    {
        List<Piece> checkingPieces = new ArrayList<>();
        for(Piece piece: board.getPieces())
        {
            if(piece.getTeam() == anotherTeam && piece.isChecking(checkingRow,checkingCol,board))
            {
                checkingPieces.add(piece);
            }
        }
        return checkingPieces;
    }

    private static boolean outofBounds(int newRow,int newCol,int team)
    {
        if(team==0)
        {
            if(newRow>=1 && newRow<=3 && newCol>=4 && newCol<=6)
            {
                return false;
            }
        }
        if(team==1)
        {
            if(newRow>=8 && newRow<=10 && newCol>=4 && newCol<=6)
            {
                return false;
            }
        }
        return true;
    }

    private static boolean canEscape(Board board, Piece generalPiece, List<Piece> checkingPieces)
    {
        int row_ = generalPiece.getRows();
        int col_ = generalPiece.getCols();
        int team_ = generalPiece.getTeam();
        int canMove[][] = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for(int[] dir : canMove)
        {
            int newRow = row_ + dir[0];
            int newCol = col_ + dir[1];

            if(outofBounds(newRow,newCol,team_))
            {
                continue;
            }

            Piece target = board.getPieceAt(newRow,newCol);

            if(board.getPieceAt(newRow,newCol) != null && board.getPieceAt(newRow,newCol).getTeam() == team_)
            {
                continue;
            }

            if(target != null)
            {
                board.getPieces().remove(target);
            }

            Piece simulatedGeneral = board.getPieceAt(newRow,newCol);

            int preRow = generalPiece.getRows();
            int preCol = generalPiece.getCols();
            generalPiece.forceTo(newRow,newCol,board);
            //bmj注：
            //重置移动状态，因为forceTo方法最终将移动状态切换为true，会导致board的update方法不断进入if主语句
            //进而导致不断nextTurn和proceed和judge
            board.pieceMoving=false;
            board.movingPiece = null;

            boolean destinationisChecking = false;
            for(Piece piece : board.getPieces())
            {
                if(piece.getTeam() != team_ && piece.isChecking(newRow,newCol,board))
                {
                    destinationisChecking=true;
                    break;
                }
            }

            generalPiece.setPreRow(preRow);
            generalPiece.setPreCol(preCol);
            generalPiece.forceTo(preRow,preCol,board);
            //bmj注：
            //重置移动状态，因为forceTo方法最终将移动状态切换为true，会导致board的update方法不断进入if主语句
            //进而导致不断nextTurn和proceed和judge
            board.pieceMoving=false;
            board.movingPiece = null;


            if(target != null)
            {
                board.getPieces().add(target);
            }

            if(!destinationisChecking)
            {
                return true;
            }
        }
        return false;
    }

    private static boolean canBlockStrike(Board board,Piece generalPiece,List<Piece> checkingPieces)
    {
        int team = generalPiece.getTeam();

        for (Piece checkingPiece : checkingPieces)
        {
                //获取可阻挡位置
                List<int[]> blockPositions = getStrikePath(board, checkingPiece, generalPiece.getRows(), generalPiece.getCols());
                if (blockPositions.isEmpty())
                {
                    continue;
                }

                for (int[] pos : blockPositions)
                {
                    int blockRow = pos[0];
                    int blockCol = pos[1];

                    for (Piece ally : board.getPieces())
                    {
                        if (ally.getTeam() == team && ally != generalPiece)
                        { //排除将帅自身
                            if (ally.canMoveTo(blockRow, blockCol, board))
                            {
                                //模拟阻挡
                                Piece originalPiece = board.getPieceAt(blockRow, blockCol);
                                if (originalPiece != null)
                                {
                                    board.getPieces().remove(originalPiece);
                                }

                                int preRow = ally.getRows();
                                int preCol = ally.getCols();
                                ally.forceTo(blockRow, blockCol, board);
                                //bmj注：
                                //重置移动状态，因为forceTo方法最终将移动状态切换为true，会导致board的update方法不断进入if主语句
                                //进而导致不断nextTurn和proceed和judge
                                board.pieceMoving=false;
                                board.movingPiece = null;

                                //检查阻挡后是否解将
                                boolean stillChecked = false;
                                for (Piece p : board.getPieces())
                                {
                                    if (p.getTeam() != team && p.isChecking(generalPiece.getRows(), generalPiece.getCols(), board))
                                    {
                                        stillChecked = true;
                                        break;
                                    }
                                }

                                ally.setPreRow(preRow);
                                ally.setPreCol(preCol);
                                ally.forceTo(preRow, preCol, board);
                                //bmj注：
                                //重置移动状态，因为forceTo方法最终将移动状态切换为true，会导致board的update方法不断进入if主语句
                                //进而导致不断nextTurn和proceed和judge
                                board.pieceMoving=false;
                                board.movingPiece = null;

                                if (originalPiece != null)
                                {
                                    board.getPieces().add(originalPiece);
                                }

                                if (!stillChecked)
                                {
                                    return true;
                                }
                            }
                        }
                    }
                }
        }
        return false;
    }

    //处理炮的单独将军逻辑
    //虽然在阻挡的逻辑中也考虑了炮，但是这个是专门为拆炮架设计的
    private static boolean canNeutralizeCannonAttack(Board board, Piece general, List<Piece> attackers, int team)
    {
        int gRow = general.getRows();
        int gCol = general.getCols();

        for (Piece attacker : attackers)
        {
            //只处理炮的阻挡（拆炮架）
            if (attacker instanceof RedCannonPiece || attacker instanceof BlackCannonPiece)
            {
                //获取炮到将帅之间的炮架位置
                int[] cannonMount = getCannonMount(board, attacker, gRow, gCol);

                if (cannonMount == null)
                {
                    continue; //没有炮架，不是炮的有效攻击
                }

                int mountRow = cannonMount[0];
                int mountCol = cannonMount[1];
                Piece mountPiece = board.getPieceAt(mountRow, mountCol);

                if (mountPiece == null)
                {
                    continue;
                }

                //拆炮架
                if (mountPiece.getTeam() == team)
                {
                    if (canPieceMoveAway(board, mountPiece, mountRow, mountCol)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    //拆炮架方法
    private static boolean canPieceMoveAway(Board board, Piece mountPiece, int originalRow, int originalCol)
    {
        int team = mountPiece.getTeam();

        //遍历棋盘所有位置，检查炮架棋子是否有可移动的位置（离开当前炮架位置）
        for (int row=1;row<=10;row++)
        {
            for (int col=1;col<=9;col++)
            {
                if (row==originalRow && col==originalCol)
                {
                    continue;//必须移走才算拆架
                }
                if (mountPiece.canMoveTo(row, col, board))
                {
                    //模拟移动炮架棋子
                    Piece targetPiece = board.getPieceAt(row, col);


                    if (targetPiece != null)
                    {
                        board.getPieces().remove(targetPiece);
                    }


                    int preRow = mountPiece.getRows();
                    int preCol = mountPiece.getCols();
                    mountPiece.forceTo(row, col, board);
                    //bmj注：
                    //重置移动状态，因为forceTo方法最终将移动状态切换为true，会导致board的update方法不断进入if主语句
                    //进而导致不断nextTurn和proceed和judge
                    board.pieceMoving=false;
                    board.movingPiece = null;

                    //不用判断了，既然炮架可以被移走，那么将军就会被解除
                    boolean attackRemoved = true;

                    mountPiece.forceTo(originalRow, originalCol, board);
                    mountPiece.setPreRow(preRow);
                    mountPiece.setPreCol(preCol);
                    //bmj注：
                    //重置移动状态，因为forceTo方法最终将移动状态切换为true，会导致board的update方法不断进入if主语句
                    //进而导致不断nextTurn和proceed和judge
                    board.pieceMoving=false;
                    board.movingPiece = null;


                    if (targetPiece != null)
                    {
                        board.getPieces().add(targetPiece);
                    }

                    if (attackRemoved)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    //获取炮架位置
    private static int[] getCannonMount(Board board, Piece cannon, int targetRow, int targetCol)
    {
        int cRow = cannon.getRows();
        int cCol = cannon.getCols();

        //炮必须和目标在同一行或同一列
        //bmj注：
        //这个很像我之前写的炮的攻击逻辑
        if (cRow != targetRow && cCol != targetCol)
        {
            return null;
        }

        List<Piece> betweenPieces = new ArrayList<>();
        //同一行
        if (cRow == targetRow)
        {
            int minCol = Math.min(cCol, targetCol);
            int maxCol = Math.max(cCol, targetCol);
            for (int col=minCol+1;col<maxCol;col++)
            {
                Piece p = board.getPieceAt(cRow,col);
                if (p != null)
                {
                    //中间棋子个数
                    betweenPieces.add(p);
                }
            }
        }

        //同一列
        else
        {
            int minRow = Math.min(cRow, targetRow);
            int maxRow = Math.max(cRow, targetRow);
            for (int row=minRow+1;row<maxRow;row++)
            {
                Piece p = board.getPieceAt(row,cCol);
                if (p != null)
                {
                    betweenPieces.add(p);
                }
            }
        }

        //炮攻击需要且仅有一个炮架
        if (betweenPieces.size() == 1)
        {
            Piece mount = betweenPieces.get(0);
            return new int[]{mount.getRows(), mount.getCols()};
        }
        return null;
    }

    //获取被将路径
    private static List<int[]> getStrikePath(Board board, Piece attacker, int generalRow, int generalCol)
    {
        List<int[]> path = new ArrayList<>();
        int aRow = attacker.getRows();
        int aCol = attacker.getCols();
        int gRow = generalRow;
        int gCol = generalCol;

        //车、炮的阻挡方法
        if (attacker instanceof BlackTankPiece || attacker instanceof RedTankPiece || attacker instanceof BlackCannonPiece || attacker instanceof RedCannonPiece)
        {
            //如果在同一行
            if (aRow == gRow)
            {
                int startCol=Math.min(aCol, gCol) +1;
                int endCol=Math.max(aCol, gCol) -1;
                for (int c=startCol;c<=endCol;c++)
                {
                    path.add(new int[]{aRow,c});
                }
            }
            //如果在同一列
            else if (aCol == gCol)
            {
                int startRow=Math.min(aRow, gRow) + 1;
                int endRow=Math.max(aRow, gRow) - 1;
                for (int r=startRow;r<=endRow;r++)
                {
                    path.add(new int[]{r,aCol});
                }
            }
        }

        return path;
    }

    private static boolean canEatAttacker(Board board, Piece general, List<Piece> attackers)
    {
        int team = general.getTeam();

        for (Piece attacker : attackers)
        {
            int aRow = attacker.getRows();
            int aCol = attacker.getCols();

            //查找可以吃掉attacker的己方棋子
            for (Piece ally : board.getPieces())
            {
                if (ally.getTeam() == team && ally.canMoveTo(aRow, aCol, board))
                {
                    //吃子
                    board.getPieces().remove(attacker);
                    int preRow = ally.getRows();
                    int preCol = ally.getCols();
                    ally.forceTo(aRow, aCol, board);
                    //bmj注：
                    //重置移动状态，因为forceTo方法最终将移动状态切换为true，会导致board的update方法不断进入if主语句
                    //进而导致不断nextTurn和proceed和judge
                    board.pieceMoving=false;
                    board.movingPiece = null;

                    //检查是否解将
                    boolean stillChecked = false;
                    for (Piece p : board.getPieces())
                    {
                        if (p.getTeam() != team && p.isChecking(general.getRows(), general.getCols(), board)) {
                            stillChecked = true;
                            break;
                        }
                    }


                    ally.setPreRow(preRow);
                    ally.setPreCol(preCol);
                    ally.forceTo(preRow, preCol, board);
                    //bmj注：
                    //重置移动状态，因为forceTo方法最终将移动状态切换为true，会导致board的update方法不断进入if主语句
                    //进而导致不断nextTurn和proceed和judge
                    board.pieceMoving=false;
                    board.movingPiece = null;

                    board.getPieces().add(attacker);

                    if (!stillChecked)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void showInformation(String title,String error)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setTitle(title);
        alert.setContentText(error);
        alert.show();
    }
}
