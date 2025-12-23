package Ai;
import Pieces.*;
import main.Board;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Ai {
    private long searchEndTime;
    private static long TIME_LIMIT = 15000;
    private static final int MAX_QS_DEPTH = 6;

    private static final int TT_EXACT = 0;
    private static final int TT_ALPHA = 1;
    private static final int TT_BETA = 2;

    private static class TimeOutException extends RuntimeException {
    }

    public void setLevel(int i) {
        TIME_LIMIT = (i + 1) * 3000L;
    }

    private static class TranspositionEntry {
        long zobristHash;
        int depth;
        int score;
        int flag;
        Move bestMove;

        TranspositionEntry(long hash, int depth, int score, int flag, Move bestMove) {
            this.zobristHash = hash;
            this.depth = depth;
            this.score = score;
            this.flag = flag;
            this.bestMove = bestMove;
        }
    }

    private enum GamePhase {
        OPENING,
        MIDDLE,
        ENDGAME
    }

    SimBoard simBoard;
    Board board;
    Book book;

    private Map<Long, TranspositionEntry> transpositionTable;
    private long[][][] zobristTable;
    private long currentZobristHash;

    private static final int SEARCH_DEPTH = 20;
    private static final int WIN_SCORE = 100000;

    public Ai(Board board, Book book) {
        simBoard = new SimBoard(10, 9);
        initializeBoard();
        this.board = board;
        this.book = book;
        this.transpositionTable = new HashMap<>();
        initializeZobrist();
    }

    private void initializeZobrist() {
        Random random = new Random(12345);
        zobristTable = new long[10][9][14];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                for (int k = 0; k < 14; k++) {
                    zobristTable[i][j][k] = random.nextLong();
                }
            }
        }
    }

    private int getPieceIndex(simPiece piece) {
        if (piece instanceof RedGeneralPiece) return 0;
        if (piece instanceof RedGuardPiece) return 1;
        if (piece instanceof RedXiangPiece) return 2;
        if (piece instanceof RedHorsePiece) return 3;
        if (piece instanceof RedTankPiece) return 4;
        if (piece instanceof RedCannonPiece) return 5;
        if (piece instanceof RedSoldierPiece) return 6;
        if (piece instanceof BlackGeneralPiece) return 7;
        if (piece instanceof BlackGuardPiece) return 8;
        if (piece instanceof BlackXiangPiece) return 9;
        if (piece instanceof BlackHorsePiece) return 10;
        if (piece instanceof BlackTankPiece) return 11;
        if (piece instanceof BlackCannonPiece) return 12;
        if (piece instanceof BlackSoldierPiece) return 13;
        return -1;
    }

    private long calculateZobristHash() {
        long hash = 0;
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 9; j++) {
                simPiece piece = simBoard.getPieceAt(i, j);
                if (piece != null) {
                    int pieceIndex = getPieceIndex(piece);
                    if (pieceIndex != -1) {
                        hash ^= zobristTable[i - 1][j - 1][pieceIndex];
                    }
                }
            }
        }
        return hash;
    }

    private void updateZobristHash(int row, int col, simPiece piece, boolean isAdd) {
        if (piece != null) {
            int pieceIndex = getPieceIndex(piece);
            if (pieceIndex != -1) {
                currentZobristHash ^= zobristTable[row - 1][col - 1][pieceIndex];
            }
        }
    }

    private void initializeBoard() {
        simBoard.setPiece(1, 1, new BlackTankPiece(1, 1));
        simBoard.setPiece(1, 9, new BlackTankPiece(1, 9));
        simBoard.setPiece(1, 2, new BlackHorsePiece(1, 2));
        simBoard.setPiece(1, 8, new BlackHorsePiece(1, 8));
        simBoard.setPiece(1, 3, new BlackXiangPiece(1, 3));
        simBoard.setPiece(1, 7, new BlackXiangPiece(1, 7));
        simBoard.setPiece(1, 4, new BlackGuardPiece(1, 4));
        simBoard.setPiece(1, 6, new BlackGuardPiece(1, 6));
        simBoard.setPiece(1, 5, new BlackGeneralPiece(1, 5));
        simBoard.setPiece(3, 2, new BlackCannonPiece(3, 2));
        simBoard.setPiece(3, 8, new BlackCannonPiece(3, 8));
        simBoard.setPiece(4, 1, new BlackSoldierPiece(4, 1));
        simBoard.setPiece(4, 3, new BlackSoldierPiece(4, 3));
        simBoard.setPiece(4, 5, new BlackSoldierPiece(4, 5));
        simBoard.setPiece(4, 7, new BlackSoldierPiece(4, 7));
        simBoard.setPiece(4, 9, new BlackSoldierPiece(4, 9));

        simBoard.setPiece(10, 1, new RedTankPiece(10, 1));
        simBoard.setPiece(10, 9, new RedTankPiece(10, 9));
        simBoard.setPiece(10, 2, new RedHorsePiece(10, 2));
        simBoard.setPiece(10, 8, new RedHorsePiece(10, 8));
        simBoard.setPiece(10, 3, new RedXiangPiece(10, 3));
        simBoard.setPiece(10, 7, new RedXiangPiece(10, 7));
        simBoard.setPiece(10, 4, new RedGuardPiece(10, 4));
        simBoard.setPiece(10, 6, new RedGuardPiece(10, 6));
        simBoard.setPiece(10, 5, new RedGeneralPiece(10, 5));
        simBoard.setPiece(8, 2, new RedCannonPiece(8, 2));
        simBoard.setPiece(8, 8, new RedCannonPiece(8, 8));
        simBoard.setPiece(7, 1, new RedSoldierPiece(7, 1));
        simBoard.setPiece(7, 3, new RedSoldierPiece(7, 3));
        simBoard.setPiece(7, 5, new RedSoldierPiece(7, 5));
        simBoard.setPiece(7, 7, new RedSoldierPiece(7, 7));
        simBoard.setPiece(7, 9, new RedSoldierPiece(7, 9));
    }

    public void update(Piece movingPiece) {
        if (movingPiece != null) {
            int prevRow = movingPiece.getPreRow();
            int prevCol = movingPiece.getPreCol();
            int currRow = movingPiece.getRows();
            int currCol = movingPiece.getCols();

            simPiece sp = simBoard.pieces[prevRow - 1][prevCol - 1];
            simBoard.pieces[currRow - 1][currCol - 1] = sp;
            simBoard.pieces[prevRow - 1][prevCol - 1] = null;

            if (sp != null) {
                sp.updatePosition(currRow, currCol);
            }
        }
    }

    public static final int[][] GENERAL_PST =  {
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,  -5,  -5,  -5,   0,   0,   0}, // Row 7 (三楼，危险)
            {   0,   0,   0,   0,   5,   0,   0,   0,   0}, // Row 8 (二楼)
            {   0,   0,   0,   5,  10,   5,   0,   0,   0}  // Row 9 (底线，最稳)};
    };


    public static final int[][] ADVISOR_PST = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 10, 0, 10, 0, 0, 0},
            {0, 0, 0, 0, 40, 0, 0, 0, 0},
            {0, 0, 0, 20, 0, 20, 0, 0, 0}};

    public static final int[][] ELEPHANT_PST =  {
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,   0,   0,   0,   0,   0,   0,   0}, // Row 4 (河界，象过不去)
            {   0,   0,  15,   0,   0,   0,  15,   0,   0}, // Row 5 (边象位置)
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {  10,   0,   0,   0,  30,   0,   0,   0,  10}, // Row 7 (中路联防点)
            {   0,   0,   0,   0,   0,   0,   0,   0,   0},
            {   0,   0,  20,   0,   0,   0,  20,   0,   0}  // Row 9 (底象)
    };

    public static final int[][] HORSE_PST =  {
            {   0, -10,   0,   0,   0,   0,   0, -10,   0}, // Row 0 (敌底 - 容易被困)
            {   0,   5,  20,  10,   5,  10,  20,   5,   0}, // Row 1 (卧槽预备)
            {  10,  10,  30,  20,  15,  20,  30,  10,  10}, // Row 2 (卧槽马/挂角马)
            {  15,  20,  25,  30,  30,  30,  25,  20,  15}, // Row 3 (八角马)
            {  10,  20,  30,  40,  40,  40,  30,  20,  10}, // Row 4 (河口马)
            {  10,  20,  35,  40,  40,  40,  35,  20,  10}, // Row 5 (己方河岸)
            {   5,  10,  15,  25,  25,  25,  15,  10,   5}, // Row 6
            {   0,   5,  10,  15,  20,  15,  10,   5,   0}, // Row 7
            {   0, -10,   0,   5,   5,   5,   0, -10,   0}, // Row 8
            { -10, -20, -10,   0,   0,   0, -10, -20, -10}  // Row 9 (己底)
    };

    public static final int[][] CHARIOT_PST = {
            {  60,  80,  80,  80,  80,  80,  80,  80,  60}, // Row 0 (敌底)
            {  60,  80,  80,  80,  80,  80,  80,  80,  60}, // Row 1
            {  60,  80,  80,  80,  80,  80,  80,  80,  60}, // Row 2
            {  60,  80,  85,  90,  90,  90,  85,  80,  60}, // Row 3 (敌兵行 - 压制马腿)
            {  60,  90,  95, 100, 100, 100,  95,  90,  60}, // Row 4 (河口 - 霸王车)
            {  60,  90,  95, 100, 100, 100,  95,  90,  60}, // Row 5 (己方河岸)
            {  50,  60,  70,  75,  75,  75,  70,  60,  50}, // Row 6
            {  30,  40,  50,  60,  70,  60,  50,  40,  30}, // Row 7
            {  10,  20,  30,  40,  50,  40,  30,  20,  10}, // Row 8
            { -20,  10,  10,  20,  20,  20,  10,  10, -20}  // Row 9 (己底)
    };

    public static final int[][] CANNON_PST =  {
            {  10,  20,  20,  20,  20,  20,  20,  20,  10}, // Row 0 (沉底)
            {  10,  20,  10,  10,  10,  10,  10,  20,  10}, // Row 1
            {  10,  20,  20,  30,  30,  30,  20,  20,  10}, // Row 2
            {  10,  20,  20,  20,  20,  20,  20,  20,  10}, // Row 3
            {  10,  30,  40,  50,  50,  50,  40,  30,  10}, // Row 4 (巡河)
            {  10,  30,  40,  50,  50,  50,  40,  30,  10}, // Row 5
            {  20,  30,  30,  40,  40,  40,  30,  30,  20}, // Row 6
            {  30,  30,  40,  50,  80,  50,  40,  30,  30}, // Row 7 (中炮位置！)
            {  10,  20,  20,  20,  20,  20,  20,  20,  10}, // Row 8
            {   0,   5,   5,   0, -10,   0,   5,   5,   0}  // Row 9 (底线，中间窝心炮扣分)
    };


    public static final int[][] SOLDIER_PST =  {
            {  10,  15,  20,  25,  30,  25,  20,  15,  10}, // Row 0 (到底了，威力略减)
            {  20,  30,  50,  70,  80,  70,  50,  30,  20}, // Row 1 (咽喉部位，威力最大)
            {  20,  30,  50,  65,  70,  65,  50,  30,  20}, // Row 2
            {  20,  30,  40,  50,  55,  50,  40,  30,  20}, // Row 3 (兵行线)
            {  10,  15,  20,  30,  35,  30,  20,  15,  10}, // Row 4 (刚过河)
            {   0,   0,   0,   0,   0,   0,   0,   0,   0}, // Row 5 (未过河)
            {   0,   0,   0,   0,   0,   0,   0,   0,   0}, // Row 6
            {   0,   0,   0,   0,   0,   0,   0,   0,   0}, // Row 7
            {   0,   0,   0,   0,   0,   0,   0,   0,   0}, // Row 8
            {   0,   0,   0,   0,   0,   0,   0,   0,   0}  // Row 9
    };

    public void changePieceValue() {
        if (board.getTurn() < 12) {
            return;
        }

        int redBig = 0;
        int blackBig = 0;
        for (Piece piece : board.getPieces()) {
            if (piece.pieceValue() >= 400 && piece.getTeam() == 1) {
                redBig++;
            } else if (piece.pieceValue() >= 400 && piece.getTeam() == 0) {
                blackBig++;
            }
        }
        if (board.getPieces().size() < 16 || (redBig < 4 && blackBig < 4)) {
            Piece.TYPE_CANNON = 600;
            Piece.TYPE_HORSE = 500;
            Piece.TYPE_GUARD = 200;
            Piece.TYPE_XIANG = 300;
        } else {
            Piece.TYPE_HORSE = 500;
        }
    }

    public int pieceScore(simPiece piece, int row, int col) {
        if (piece.team == 1) {
            return piece.getPieceValue() + piece.getPst()[row - 1][col - 1];
        } else {
            return piece.getPieceValue() + piece.getPst()[10 - row][col - 1];
        }
    }

    private GamePhase getGamePhase() {
        int turn = board.getTurn();
        int totalPieces = 0;
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 9; j++) {
                if (simBoard.getPieceAt(i, j) != null) {
                    totalPieces++;
                }
            }
        }

        if (turn < 12) return GamePhase.OPENING;
        if (turn > 30 || totalPieces < 16) return GamePhase.ENDGAME;
        return GamePhase.MIDDLE;
    }

    private int evaluateDevelopment() {
        int score = 0;
        if (simBoard.getPieceAt(3, 2) == null) score += 10;
        if (simBoard.getPieceAt(3, 8) == null) score += 10;
        if (simBoard.getPieceAt(8, 2) == null) score -= 10;
        if (simBoard.getPieceAt(8, 8) == null) score -= 10;
        return score;
    }

    private int evaluateKingSafety() {
        int score = 0;
        int redGeneralRow = -1, redGeneralCol = -1;
        int blackGeneralRow = -1, blackGeneralCol = -1;

        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 9; j++) {
                simPiece p = simBoard.getPieceAt(i, j);
                if (p instanceof RedGeneralPiece) {
                    redGeneralRow = i;
                    redGeneralCol = j;
                } else if (p instanceof BlackGeneralPiece) {
                    blackGeneralRow = i;
                    blackGeneralCol = j;
                }
            }
        }

        if (redGeneralRow > 0) {
            int guardCount = 0;
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = redGeneralRow + dr;
                    int nc = redGeneralCol + dc;
                    if (nr >= 8 && nr <= 10 && nc >= 4 && nc <= 6) {
                        simPiece p = simBoard.getPieceAt(nr, nc);
                        if (p != null && p.team == 1 && (p instanceof RedGuardPiece || p instanceof RedXiangPiece)) {
                            guardCount++;
                        }
                    }
                }
            }
            score -= guardCount * 15;
        }

        if (blackGeneralRow > 0) {
            int guardCount = 0;
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = blackGeneralRow + dr;
                    int nc = blackGeneralCol + dc;
                    if (nr >= 1 && nr <= 3 && nc >= 4 && nc <= 6) {
                        simPiece p = simBoard.getPieceAt(nr, nc);
                        if (p != null && p.team == 0 && (p instanceof BlackGuardPiece || p instanceof BlackXiangPiece)) {
                            guardCount++;
                        }
                    }
                }
            }
            score += guardCount * 15;
        }

        return score;
    }

    private int evaluateAttack() {
        int score = 0;
        int redAttackers = 0;
        int blackAttackers = 0;

        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 9; j++) {
                simPiece p = simBoard.getPieceAt(i, j);
                if (p == null) continue;

                if (p.team == 1 && i <= 5) {
                    redAttackers++;
                } else if (p.team == 0 && i >= 6) {
                    blackAttackers++;
                }
            }
        }

        score += (blackAttackers - redAttackers) * 5;
        return score;
    }

    private int evaluateMobility() {
        int redMoves = 0;
        int blackMoves = 0;

        List<Move> allMoves = simBoard.generateAllMoves();
        for (Move m : allMoves) {
            simPiece p = simBoard.getPieceAt(m.oriRow, m.oriCol);
            if (p != null) {
                if (p.team == 1) redMoves++;
                else blackMoves++;
            }
        }

        return (redMoves - blackMoves);
    }

    public int evaluate(SimBoard simboard) {
        int redValue = 0;
        int blackValue = 0;
        boolean redGeneralExist = false;
        boolean blackGeneralExist = false;

        int redGeneralCol = -1;
        int blackGeneralCol = -1;

        int redBigPieces = 0;
        int blackBigPieces = 0;

        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 9; j++) {
                simPiece piece = simboard.getPieceAt(i, j);
                if (piece != null) {
                    int score = pieceScore(piece, i, j);
                    if (piece.team == 1) {
                        redValue += score;
                        if (piece.getPieceValue() >= 400) redBigPieces++;
                        if (piece instanceof RedGeneralPiece) {
                            redGeneralExist = true;
                            redGeneralCol = j;
                        }
                    } else {
                        blackValue += score;
                        if (piece.getPieceValue() >= 400) blackBigPieces++;
                        if (piece instanceof BlackGeneralPiece) {
                            blackGeneralExist = true;
                            blackGeneralCol = j;
                        }
                    }
                }
            }
        }

        if (!redGeneralExist) return -WIN_SCORE;
        if (!blackGeneralExist) return WIN_SCORE;
        if (redGeneralCol == blackGeneralCol && redGeneralCol != -1) {
            if (isKingsFacing(simboard, redGeneralCol)) {
            }
        }

        int materialDiff = redValue - blackValue;
        int mobilityDiff = evaluateMobility() * 2;

        GamePhase phase = getGamePhase();
        int phaseBonus = 0;

        switch(phase) {
            case OPENING:
                phaseBonus = evaluateDevelopment();
                break;
            case MIDDLE:
                phaseBonus = evaluateAttack();
                break;
            case ENDGAME:
                phaseBonus = evaluateKingSafety() * 2;
                materialDiff = (int)(materialDiff * 1.3);
                break;
        }

        return materialDiff + mobilityDiff + phaseBonus;
    }

    private boolean isKingsFacing(SimBoard sb, int col) {
        boolean seeGeneral = false;
        for (int i = 1; i <= 10; i++) {
            simPiece p = sb.getPieceAt(i, col);
            if (p != null) {
                if (p instanceof BlackGeneralPiece || p instanceof RedGeneralPiece) {
                    if (seeGeneral) {
                        return true;
                    }
                    seeGeneral = true;
                } else {
                    if (seeGeneral) return false;
                }
            }
        }
        return false;
    }

    private boolean isSquareAttacked(int row, int col, int byTeam) {
        List<Move> allMoves = simBoard.generateAllMoves();
        for (Move m : allMoves) {
            simPiece p = simBoard.getPieceAt(m.oriRow, m.oriCol);
            if (p != null && p.team != byTeam && m.newRow == row && m.newCol == col) {
                return true;
            }
        }
        return false;
    }

    private int getCenterControlBonus(int row, int col) {
        int bonus = 0;
        if (col >= 4 && col <= 6) bonus += 10;
        if (row >= 5 && row <= 6) bonus += 10;
        return bonus;
    }

    private boolean isInCheck(int team) {
        int generalRow = -1, generalCol = -1;

        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 9; j++) {
                simPiece p = simBoard.getPieceAt(i, j);
                if (p != null && p.team == team) {
                    if ((team == 1 && p instanceof RedGeneralPiece) ||
                            (team == 0 && p instanceof BlackGeneralPiece)) {
                        generalRow = i;
                        generalCol = j;
                        break;
                    }
                }
            }
            if (generalRow != -1) break;
        }

        if (generalRow == -1) return false;

        return isSquareAttacked(generalRow, generalCol, team);
    }

    public int getMovePriority(Move move) {
        int score = 0;
        simPiece piece = simBoard.getPieceAt(move.oriRow, move.oriCol);
        if (piece == null) return 0;

        simPiece target = simBoard.getPieceAt(move.newRow, move.newCol);
        if (target != null) {
            score += target.getPieceValue() * 100 - piece.getPieceValue();

            if (!isSquareAttacked(move.newRow, move.newCol, piece.team)) {
                score += 500;
            }
        }

        int[][] pst = piece.getPst();
        if (pst != null) {
            int fromPst, toPst;
            if (piece.team == 1) {
                fromPst = pst[move.oriRow - 1][move.oriCol - 1];
                toPst = pst[move.newRow - 1][move.newCol - 1];
            } else {
                fromPst = pst[10 - move.oriRow][move.oriCol - 1];
                toPst = pst[10 - move.newRow][move.oriCol - 1];
            }
            score += (toPst - fromPst) * 10;
        }

        simPiece oriPiece = simBoard.getPieceAt(move.oriRow, move.oriCol);
        simPiece targetPiece = simBoard.getPieceAt(move.newRow, move.newCol);

        updateZobristHash(move.oriRow, move.oriCol, oriPiece, false);
        if (targetPiece != null) {
            updateZobristHash(move.newRow, move.newCol, targetPiece, false);
        }

        move.stimulate(simBoard);

        updateZobristHash(move.newRow, move.newCol, oriPiece, true);

        if (isInCheck(1 - piece.team)) {
            score += 1000;
        }

        move.redo(simBoard);

        updateZobristHash(move.newRow, move.newCol, oriPiece, false);
        updateZobristHash(move.oriRow, move.oriCol, oriPiece, true);
        if (targetPiece != null) {
            updateZobristHash(move.newRow, move.newCol, targetPiece, true);
        }

        int centerBonus = getCenterControlBonus(move.newRow, move.newCol);
        score += centerBonus;

        return score;
    }

    private int alphaBeta(int depth, int alpha, int beta, boolean isRed, int position) {
        if (System.currentTimeMillis() > searchEndTime) {
            throw new TimeOutException();
        }

        long hash = currentZobristHash;
        TranspositionEntry ttEntry = transpositionTable.get(hash);
        if (ttEntry != null && ttEntry.depth >= depth) {
            if (ttEntry.flag == TT_EXACT) {
                return ttEntry.score;
            } else if (ttEntry.flag == TT_ALPHA && ttEntry.score <= alpha) {
                return alpha;
            } else if (ttEntry.flag == TT_BETA && ttEntry.score >= beta) {
                return beta;
            }
        }

        int currentEval = evaluate(simBoard);
        if (Math.abs(currentEval) >= WIN_SCORE - 1000) {
            return currentEval > 0 ? currentEval + depth : currentEval - depth;
        }

        if (depth <= 0) {
            if (isRed) return quiescenceSearch(alpha, beta, true, MAX_QS_DEPTH);
            else {
                return -quiescenceSearch(-beta, -alpha, false, MAX_QS_DEPTH);
            }
        }

        List<Move> allMoves = simBoard.generateAllMoves();
        List<Move> validMoves = new ArrayList<>(300);

        int targetTeam = isRed ? 1 : 0;

        for (Move move : allMoves) {
            simPiece piece = simBoard.getPieceAt(move.oriRow, move.oriCol);
            if (piece != null && piece.team == targetTeam) {
                validMoves.add(move);
            }
        }

        if (validMoves.isEmpty()) {
            if (isRed) return -WIN_SCORE - depth;
            else return WIN_SCORE + depth;
        }

        validMoves.sort((m1, m2) -> getMovePriority(m2) - getMovePriority(m1));

        int maxMoves = validMoves.size();
        if (depth > 5) {
            maxMoves = Math.min(maxMoves, 10);
        } else if (depth > 3) {
            maxMoves = Math.min(maxMoves, 20);
        }

        Move bestMove = null;
        int bestValue;
        int originalAlpha = alpha;

        if (isRed) {
            bestValue = -99999999;
            int count = 0;
            for (Move move : validMoves) {
                if (count >= maxMoves) break;

                simPiece oriPiece = simBoard.getPieceAt(move.oriRow, move.oriCol);
                simPiece targetPiece = simBoard.getPieceAt(move.newRow, move.newCol);

                updateZobristHash(move.oriRow, move.oriCol, oriPiece, false);
                if (targetPiece != null) {
                    updateZobristHash(move.newRow, move.newCol, targetPiece, false);
                }

                move.stimulate(simBoard);

                updateZobristHash(move.newRow, move.newCol, oriPiece, true);

                int value = alphaBeta(depth - 1, alpha, beta, false, position + 1);

                move.redo(simBoard);

                updateZobristHash(move.newRow, move.newCol, oriPiece, false);
                updateZobristHash(move.oriRow, move.oriCol, oriPiece, true);
                if (targetPiece != null) {
                    updateZobristHash(move.newRow, move.newCol, targetPiece, true);
                }

                if (value > bestValue) {
                    bestValue = value;
                    bestMove = move;
                }
                alpha = Math.max(alpha, bestValue);
                if (beta <= alpha) break;

                count++;
            }
        } else {
            bestValue = 99999999;
            int count = 0;
            for (Move move : validMoves) {
                if (count >= maxMoves) break;

                simPiece oriPiece = simBoard.getPieceAt(move.oriRow, move.oriCol);
                simPiece targetPiece = simBoard.getPieceAt(move.newRow, move.newCol);

                updateZobristHash(move.oriRow, move.oriCol, oriPiece, false);
                if (targetPiece != null) {
                    updateZobristHash(move.newRow, move.newCol, targetPiece, false);
                }

                move.stimulate(simBoard);

                updateZobristHash(move.newRow, move.newCol, oriPiece, true);

                int value = alphaBeta(depth - 1, alpha, beta, true, position + 1);

                move.redo(simBoard);

                updateZobristHash(move.newRow, move.newCol, oriPiece, false);
                updateZobristHash(move.oriRow, move.oriCol, oriPiece, true);
                if (targetPiece != null) {
                    updateZobristHash(move.newRow, move.newCol, targetPiece, true);
                }

                if (value < bestValue) {
                    bestValue = value;
                    bestMove = move;
                }
                beta = Math.min(beta, bestValue);
                if (beta <= alpha) break;

                count++;
            }
        }

        int ttFlag;
        if (bestValue <= originalAlpha) {
            ttFlag = TT_ALPHA;
        } else if (bestValue >= beta) {
            ttFlag = TT_BETA;
        } else {
            ttFlag = TT_EXACT;
        }

        transpositionTable.put(hash, new TranspositionEntry(hash, depth, bestValue, ttFlag, bestMove));

        return bestValue;
    }

    private int quiescenceSearch(int alpha, int beta, boolean isRed, int qsDepth) {
        int standPat = evaluate(simBoard);
        if (!isRed) standPat = -standPat;

        if (standPat >= beta) return beta;

        if (standPat > alpha) alpha = standPat;

        if (qsDepth <= 0) return alpha;

        List<Move> allMoves = simBoard.generateAllMoves();
        List<Move> captureMoves = new ArrayList<>();
        int myTeam = isRed ? 1 : 0;

        for (Move mv : allMoves) {
            simPiece p = simBoard.getPieceAt(mv.oriRow, mv.oriCol);
            if (p == null || p.team != myTeam) continue;

            simPiece target = simBoard.getPieceAt(mv.newRow, mv.newCol);
            if (target != null) {
                captureMoves.add(mv);
            }
        }

        captureMoves.sort((m1, m2) -> getMovePriority(m2) - getMovePriority(m1));

        for (Move move : captureMoves) {
            simPiece oriPiece = simBoard.getPieceAt(move.oriRow, move.oriCol);
            simPiece targetPiece = simBoard.getPieceAt(move.newRow, move.newCol);

            updateZobristHash(move.oriRow, move.oriCol, oriPiece, false);
            if (targetPiece != null) {
                updateZobristHash(move.newRow, move.newCol, targetPiece, false);
            }

            move.stimulate(simBoard);

            updateZobristHash(move.newRow, move.newCol, oriPiece, true);

            int score = -quiescenceSearch(-beta, -alpha, !isRed, qsDepth - 1);

            move.redo(simBoard);

            updateZobristHash(move.newRow, move.newCol, oriPiece, false);
            updateZobristHash(move.oriRow, move.oriCol, oriPiece, true);
            if (targetPiece != null) {
                updateZobristHash(move.newRow, move.newCol, targetPiece, true);
            }

            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }

        return alpha;
    }

    public Move findBestMove(Board realBoard) {
        if (Thread.currentThread().isInterrupted()) {
            return null;
        }

        System.out.println("AI Thinking... Turn: " + realBoard.getTurn());

        syncFromRealBoard(realBoard);
        simBoard.printDebugBoard();

        currentZobristHash = calculateZobristHash();
        transpositionTable.clear();

        int aiTeam = board.isRedFirst() ? 0 : 1;

        if (board.isRedFirst()) {
            if (realBoard.getTurn() % 2 != 0) return null;
        } else {
            if (realBoard.getTurn() % 2 != 1) return null;
        }

        Move bookMove = book.query();
        if (bookMove != null) {
            System.out.println(">>> AI hit Book move!");
            return new Move(bookMove.oriRow, bookMove.oriCol, bookMove.newRow, bookMove.newCol, simBoard);
        }

        long startTime = System.currentTimeMillis();
        this.searchEndTime = startTime + TIME_LIMIT;

        Move finalBestMove = null;

        try {
            for (int d = 2; d <= SEARCH_DEPTH; d++) {
                System.out.print("Searching Depth: " + d + " ... ");

                //传给searchRoot
                Move currentDepthMove = searchRoot(d, aiTeam);

                finalBestMove = currentDepthMove;
            }
        } catch (TimeOutException e) {
            System.out.println("Time Out! Using best move from previous depth.");
        }

        //没有move找第一个合法的
        if (finalBestMove == null) {
            List<Move> moves = simBoard.generateAllMoves();
            for (Move m : moves) {
                simPiece p = simBoard.getPieceAt(m.oriRow, m.oriCol);
                if (p != null && p.team == aiTeam) {
                    finalBestMove = m;
                    break;
                }
            }
        }

        System.out.println("Total Time: " + (System.currentTimeMillis() - startTime) + "ms");
        return finalBestMove;
    }


    public void syncFromRealBoard(Board realBoard) {
        for (int i = 0; i < simBoard.pieces.length; i++) {
            for (int j = 0; j < simBoard.pieces[i].length; j++) {
                simBoard.pieces[i][j] = null;
            }
        }

        for (Piece p : realBoard.getPieces()) {
            simPiece sp = convertToSimPiece(p);
            if (sp != null) {
                simBoard.setPiece(p.getRows(), p.getCols(), sp);
            }
        }
    }

    private simPiece convertToSimPiece(Piece p) {
        int r = p.getRows();
        int c = p.getCols();

        if (p instanceof Pieces.RedGeneralPiece) return new RedGeneralPiece(r, c);
        if (p instanceof Pieces.BlackGeneralPiece) return new BlackGeneralPiece(r, c);

        if (p instanceof Pieces.RedGuardPiece) return new RedGuardPiece(r, c);
        if (p instanceof Pieces.BlackGuardPiece) return new BlackGuardPiece(r, c);

        if (p instanceof Pieces.RedXiangPiece) return new RedXiangPiece(r, c);
        if (p instanceof Pieces.BlackXiangPiece) return new BlackXiangPiece(r, c);

        if (p instanceof Pieces.RedHorsePiece) return new RedHorsePiece(r, c);
        if (p instanceof Pieces.BlackHorsePiece) return new BlackHorsePiece(r, c);

        if (p instanceof Pieces.RedTankPiece) return new RedTankPiece(r, c);
        if (p instanceof Pieces.BlackTankPiece) return new BlackTankPiece(r, c);

        if (p instanceof Pieces.RedCannonPiece) return new RedCannonPiece(r, c);
        if (p instanceof Pieces.BlackCannonPiece) return new BlackCannonPiece(r, c);

        if (p instanceof Pieces.RedSoldierPiece) return new RedSoldierPiece(r, c);
        if (p instanceof Pieces.BlackSoldierPiece) return new BlackSoldierPiece(r, c);

        return null;
    }

    private Move searchRoot(int depth, int aiTeam) {
        List<Move> allMoves = simBoard.generateAllMoves();
        List<Move> myMoves = new ArrayList<>();

        for (Move move : allMoves) {
            simPiece piece = simBoard.getPieceAt(move.oriRow, move.oriCol);
            if (piece != null && piece.team == aiTeam) {
                myMoves.add(move);
            }
        }

        if (myMoves.isEmpty()) return null;

        myMoves.sort((m1, m2) -> getMovePriority(m2) - getMovePriority(m1));

        Move bestMove = null;
        int alpha = -99999999;
        int beta = 99999999;

        if (aiTeam == 1) {
            int bestValue = -99999999;

            for (Move move : myMoves) {
                simPiece oriPiece = simBoard.getPieceAt(move.oriRow, move.oriCol);
                simPiece targetPiece = simBoard.getPieceAt(move.newRow, move.newCol);

                updateZobristHash(move.oriRow, move.oriCol, oriPiece, false);
                if (targetPiece != null) {
                    updateZobristHash(move.newRow, move.newCol, targetPiece, false);
                }

                move.stimulate(simBoard);
                updateZobristHash(move.newRow, move.newCol, oriPiece, true);

                int boardValue = alphaBeta(depth - 1, alpha, beta, false, 1);

                move.redo(simBoard);
                updateZobristHash(move.newRow, move.newCol, oriPiece, false);
                updateZobristHash(move.oriRow, move.oriCol, oriPiece, true);
                if (targetPiece != null) {
                    updateZobristHash(move.newRow, move.newCol, targetPiece, true);
                }

                if (boardValue > bestValue) {
                    bestValue = boardValue;
                    bestMove = move;
                }
                alpha = Math.max(alpha, bestValue);
            }
        } else {
            int bestValue = 99999999;

            for (Move move : myMoves) {
                simPiece oriPiece = simBoard.getPieceAt(move.oriRow, move.oriCol);
                simPiece targetPiece = simBoard.getPieceAt(move.newRow, move.newCol);

                updateZobristHash(move.oriRow, move.oriCol, oriPiece, false);
                if (targetPiece != null) {
                    updateZobristHash(move.newRow, move.newCol, targetPiece, false);
                }

                move.stimulate(simBoard);
                updateZobristHash(move.newRow, move.newCol, oriPiece, true);

                int boardValue = alphaBeta(depth - 1, alpha, beta, true, 1);

                move.redo(simBoard);
                updateZobristHash(move.newRow, move.newCol, oriPiece, false);
                updateZobristHash(move.oriRow, move.oriCol, oriPiece, true);
                if (targetPiece != null) {
                    updateZobristHash(move.newRow, move.newCol, targetPiece, true);
                }

                if (boardValue < bestValue) {
                    bestValue = boardValue;
                    bestMove = move;
                }
                beta = Math.min(beta, bestValue);
            }
        }

        return bestMove;
    }


    public void test(Board realBoard) {

        changePieceValue();
        Move best = findBestMove(realBoard);

        if (best != null) {
            javafx.application.Platform.runLater(() -> {
                Piece realPiece = board.getPieceAt(best.oriRow, best.oriCol);
                if (realPiece != null) {
                    realPiece.moveTo(best.newRow, best.newCol, board);
                }
            });
        }
    }
}