package Ai;

import Pieces.BlackCannonPiece;
import Pieces.BlackGeneralPiece;
import Pieces.BlackGuardPiece;
import Pieces.BlackHorsePiece;
import Pieces.BlackSoldierPiece;
import Pieces.BlackTankPiece;
import Pieces.BlackXiangPiece;
import Pieces.Piece;
import Pieces.RedCannonPiece;
import Pieces.RedGeneralPiece;
import Pieces.RedGuardPiece;
import Pieces.RedHorsePiece;
import Pieces.RedSoldierPiece;
import Pieces.RedTankPiece;
import Pieces.RedXiangPiece;
import main.Board;

import java.security.SecureRandom;

public class simpleBoard {
    public static final long[][][] zobristTable;
    private static long initialHash;
    int side = 1;

    static final int[][] iniBoard = {
            {-5, -4, -3, -2, -1, -2, -3, -4, -5}, // 0
            {0, 0, 0, 0, 0, 0, 0, 0, 0},          // 1
            {0, -6, 0, 0, 0, 0, 0, -6, 0},        // 2
            {-7, 0, -7, 0, -7, 0, -7, 0, -7},     // 3
            {0, 0, 0, 0, 0, 0, 0, 0, 0},          // 4
            {0, 0, 0, 0, 0, 0, 0, 0, 0},          // 5
            {7, 0, 7, 0, 7, 0, 7, 0, 7},          // 6
            {0, 6, 0, 0, 0, 0, 0, 6, 0},          // 7
            {0, 0, 0, 0, 0, 0, 0, 0, 0},          // 8
            {5, 4, 3, 2, 1, 2, 3, 4, 5}           // 9
    };

    static {
        SecureRandom secureRandom = new SecureRandom();
        zobristTable = new long[10][9][15];
        initialHash = 0L;

        byte[] randomBytes = new byte[10 * 9 * 15 * 8];
        secureRandom.nextBytes(randomBytes);

        int byteIndex = 0;
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                for (int p = 0; p < 15; p++) {
                    zobristTable[r][c][p] = bytesToLong(randomBytes, byteIndex);
                    byteIndex += 8;
                }
                // 计算初始哈希
                int pieceId = iniBoard[r][c];
                if (pieceId != 0)
                    initialHash ^= zobristTable[r][c][getPieceZobristIndex(pieceId)];
            }
        }
    }

    private static long bytesToLong(byte[] bytes, int offset) {
        long value = 0;
        for (int i = 0; i < 8; i++)
            value = (value << 8) | (bytes[offset + i] & 0xFF);
        return value;
    }

    private static int getPieceZobristIndex(int pieceId) {
        if (pieceId == 0) return 0;
        return pieceId > 0 ? pieceId : -pieceId + 7;
    }

    private long hash;
    private int[][] simpleBoard;

    public simpleBoard() {
        reset();
    }

    public int[][] getBoard() {
        return simpleBoard;
    }

    public void reset() {
        this.hash = initialHash;
        this.simpleBoard = new int[10][9];
        for (int i = 0; i < 10; i++) {
            System.arraycopy(iniBoard[i], 0, this.simpleBoard[i], 0, 9);
        }
    }

    public long getHash() {
        return hash;
    }

    public boolean Move(int oriRow, int oriCol, int newRow, int newCol) {
        if (oriRow < 0 || oriRow > 9 || oriCol < 0 || oriCol > 8 ||
                newRow < 0 || newRow > 9 || newCol < 0 || newCol > 8) {
            return false;
        }

        int srcPiece = simpleBoard[oriRow][oriCol];
        if (srcPiece == 0) return false;

        int dstPiece = simpleBoard[newRow][newCol];

        hash ^= zobristTable[oriRow][oriCol][getPieceZobristIndex(srcPiece)];

        if (dstPiece != 0) {
            hash ^= zobristTable[newRow][newCol][getPieceZobristIndex(dstPiece)];
        }

        hash ^= zobristTable[newRow][newCol][getPieceZobristIndex(srcPiece)];
        hash ^= side;
        side = -side;

        simpleBoard[newRow][newCol] = srcPiece;
        simpleBoard[oriRow][oriCol] = 0;

        return true;
    }

    public static Piece convertToPiece (int sign, int row, int col, Board board) {
        switch (sign){
            case (1): return new RedGeneralPiece(board, row, col);
            case (2): return new RedGuardPiece(board, row, col);
            case (3): return new RedXiangPiece(board, row, col);
            case (4): return new RedHorsePiece(board, row, col);
            case (5): return new RedTankPiece(board, row, col);
            case (6): return new RedCannonPiece(board, row, col);
            case (7): return new RedSoldierPiece(board, row, col);
            case (-1): return new BlackGeneralPiece(board, row, col);
            case (-2): return new BlackGuardPiece(board, row, col);
            case (-3): return new BlackXiangPiece(board, row, col);
            case (-4): return new BlackHorsePiece(board, row, col);
            case (-5): return new BlackTankPiece(board, row, col);
            case (-6): return new BlackCannonPiece(board, row, col);
            case (-7): return new BlackSoldierPiece(board, row, col);
            default:return null;
        }
    }

    public static Move addup(int[][] preBoard, int[][] newBoard,Board board) {
        int fromRow = 0, fromCol = 0, toRow = 0, toCol = 0;

        Piece delatedPiece;

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                if (preBoard[r][c] != newBoard[r][c]) {
                    if (newBoard[r][c] != 0) {
                        if (preBoard[r][c] != 0) {
                            delatedPiece = convertToPiece(preBoard[r][c], r + 1, c + 1, board);
                            board.addPieces(delatedPiece);
                        }
                        fromRow = r;
                        fromCol = c;
                    } else {
                        if (preBoard[r][c] != 0) {
                            toRow = r;
                            toCol = c;
                        }
                    }
                }
            }
        }

        return new Move(fromRow, fromCol, toRow, toCol);
    }

    public static Move addUpForNonUndo(int[][] preBoard, int[][] newBoard,Board board)
    {
        int fromRow = 0, fromCol = 0, toRow = 0, toCol = 0;

        for (int r = 0; r < 10; r++)
        {
            for (int c = 0; c < 9; c++)
            {
                if (preBoard[r][c] != newBoard[r][c])
                {
                    if (newBoard[r][c] != 0)
                    {
                        toRow = r;
                        toCol = c;
                    }
                    else
                    {
                        if (preBoard[r][c] != 0)
                        {
                            fromRow = r;
                            fromCol = c;
                        }
                    }
                }
            }
        }

        return new Move(fromRow, fromCol, toRow, toCol);
    }

    public void setBoard(int[][] newBoardData) {
        for (int i = 0; i < 10; i++) {
            System.arraycopy(newBoardData[i], 0, this.simpleBoard[i], 0, 9);
        }
    }
}
