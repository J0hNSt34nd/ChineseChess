package main;

import Ai.Ai;
import Ai.Book;
import Ai.Move;
import Ai.simpleBoard;
import Command.Judger;
import Command.Switch;
import Pieces.*;
import Tool.Network;
import Tool.Record;
import Tool.Unit;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Board extends Pane {

    public Ai ai;
    Book book;
    private volatile Future<?> aiFuture = null;
    private boolean redFirst = false;
    private boolean loadisEmpty = true;
    private static boolean needAi = true;

    private int pendingUndoSteps = 0;
    public final List<Piece> pieces;

    static final int originalTileSize = 16;
    static final int scale = 3;
    public static final int tileSize = originalTileSize * scale;

    private volatile int operationVersion = 0;
    private boolean selected = false;
    public volatile boolean pieceMoving = false;
    private volatile int turn = 1;

    //复盘用的一些变量
    private volatile int step=0;
    private volatile boolean isPlaying = false;
    private volatile boolean isReplaying=false;
    private Timeline replay;

    final int LOGICAL_WIDTH = tileSize * 9;
    final int LOGICAL_HEIGHT = tileSize * 12;

    private Image backgoundImage;
    private Image selectedImage;
    private Image moveableImage;
    private Image killableImage;
    private Image lightImage;
    private Image checkedImage;

    public static final double cellSize = 48;
    public static final double upperMargin = 66;
    public static final double rightMargin = 20;

    private Piece selectedPiece = null;
    private Piece delatingPiece = null;
    public volatile Piece movingPiece = null;

    public Switch aSwitch = new Switch(this);
    private JLabel hintLabel;
    public Record record;
    public boolean checkMateResult=false;

    private LinkedList<Tool.Record> undoList = new LinkedList();

    private final ExecutorService aiExecutor = Executors.newSingleThreadExecutor();

    private double renderScale = 1.0;
    private int renderOffsetX = 0;
    private int renderOffsetY = 0;
    private final Canvas canvas;

    private volatile boolean isDirty = true;
    private List<int[]> cachedValidMoves = new ArrayList<>();

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public Board(Book book) {
        this.book = book;
        ai = new Ai(this, book);

        //每一个2s关键帧执行播放下一步
        replay = new Timeline(new KeyFrame(Duration.seconds(2), e->
        {
            if(step < record.history.size()-1)
            {
                showNextStep();
            }
            else
            {
                stopReplay();
            }
        }));
        //无限循环
        replay.setCycleCount(Timeline.INDEFINITE);

        canvas = new Canvas();
        this.getChildren().add(canvas);

        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

        this.setOnMouseClicked(e -> {
            int logicalX = (int) ((e.getX() - renderOffsetX) / renderScale);
            int logicalY = (int) ((e.getY() - renderOffsetY) / renderScale);
            handleMouseClick(logicalX, logicalY);
        });

        getBackgoundImage();

        pieces = new CopyOnWriteArrayList<>();
        initializepeices();
        record = new Record();
    }

    public void setHintLabel(JLabel hintLabel) {
        this.hintLabel = hintLabel;
    }

    public void reset() {
        for (Piece piece : pieces) {
            pieces.remove(piece);
        }

        record.reset();
        turn = 1;
        initializepeices();
        book.reset();
        ai.syncFromRealBoard(this);
        isDirty = true;
    }

    public void resetForLoad() {
    }

    public boolean isCheckMateResult()
    {
        return checkMateResult;
    }

    public void setCheckMateResult(boolean checkMateResult)
    {
        this.checkMateResult = checkMateResult;
    }

    public boolean isValidPosition(int row, int col) {
        return row > 0 && col > 0;
    }

    public Piece getPieceAt(int row, int col) {
        for (Piece piece : pieces) {
            if (piece.getRows() == row && piece.getCols() == col) {
                return piece;
            }
        }
        return null;
    }

    private boolean movePiece(Piece piece, int newRow, int newCol) {
        if (!this.isValidPosition(newRow, newCol)) {
            return false;
        }

        if (!piece.canMoveTo(newRow, newCol, this)) {
            return false;
        }

        piece.moveTo(newRow, newCol, this);
        return true;
    }

    public void undo() {
        if (record.history.size() < 1) {
            System.out.println("No moves to undo");
            return;
        }

        if (aiFuture != null && !aiFuture.isDone()) {
            aiFuture.cancel(true);
        }

        operationVersion++;

        if (Game.isNetworkMode()) {
            pendingUndoSteps = 1;
        }
        else {
            if (record.history.size() != 1) {
                pendingUndoSteps = 2;
            } else {
                pendingUndoSteps = 1;
            }
        }

        record.isUndoing = true;
        executeOneUndoStep();
    }

    private void executeOneUndoStep() {
        if (record.history.size() < 2) {
            pendingUndoSteps = 0;
            finishUndo();
            return;
        }

        int[][] currentState = record.history.get(record.history.size() - 1);
        int[][] previousState = record.history.get(record.history.size() - 2);

        Move move = simpleBoard.addup(previousState, currentState, this);

        if (move != null) {
            Piece piece = this.getPieceAt(move.getOriRow() + 1, move.getOriCol() + 1);

            if (piece != null) {
                piece.forceTo(move.getNewRow() + 1, move.getNewCol() + 1, this);
                turn -= 1;
                if (book != null) book.back();
            } else {
                pendingUndoSteps--;
                record.removeLast();
                if (pendingUndoSteps > 0) {
                    executeOneUndoStep();
                } else {
                    finishUndo();
                }
                return;
            }
        }
        record.removeLast();
        isDirty = true;
    }

    private void finishUndo() {
        record.isUndoing = false;
        record.syncToLastHistory();
        turn = record.history.size();
        Judger.judge(this);
        isDirty = true;
    }

    public void setIsReplaying(boolean judge)
    {
        isReplaying=judge;
    }

    public void setStepToTurn()
    {
        step=turn-1;
    }

    public void setStep(int step)
    {
        this.step = step;
    }

    public boolean getIsPlaying()
    {
        return isPlaying;
    }



    private void parsePrevStep()
    {
        System.out.println("当前步："+step);
        if(step<=0)
        {
            System.out.println("已经到达最小值，无法进行上一局");
            return;
        }

        System.out.println("开始上一步");
        int[][] currBoard = record.history.get(step);
        int[][] prevBoard = record.history.get(step-1);

        Move moveForPrev = simpleBoard.addup(prevBoard,currBoard,this);
        if(moveForPrev != null)
        {
            Piece operator = this.getPieceAt(moveForPrev.getOriRow()+1, moveForPrev.getOriCol()+1);
            if(operator != null)
            {
                operator.forceTo(moveForPrev.getNewRow()+1,moveForPrev.getNewCol()+1,this);
            }
        }
        step--;

    }

    private void parseNextStep()
    {

        System.out.println("当前步："+step);
        if(step>=record.history.size()-1)
        {
            System.out.println("已经到达最大值，无法进行下一局");
            return;
        }
//        pieces.clear();
        System.out.println("开始下一步");
        int[][] currBoard = record.history.get(step);
        int[][] nextBoard = record.history.get(step+1);

        Move moveForNext = simpleBoard.addUpForNonUndo(currBoard,nextBoard,this);
        if(moveForNext != null)
        {
            Piece operator = this.getPieceAt(moveForNext.getOriRow()+1,moveForNext.getOriCol()+1);
            if(operator != null)
            {
                operator.moveTo(moveForNext.getNewRow()+1,moveForNext.getNewCol()+1,this);
            }
        }
        step++;
    }

    private void analyzeBoard(int step)
    {
        if(step<0 || step>=record.history.size())
        {
            System.out.println("step越界");
            return;
        }
        pieces.clear();


        int[][] currBoard = record.history.get(step);
        for(int row=0;row<10;row++)
        {
            for(int col=0;col<9;col++)
            {
                int code = currBoard[row][col];
                Piece piece = simpleBoard.convertToPiece(code,row+1,col+1,this);
                if(piece != null)
                {
                    pieces.add(piece);
                }
            }
        }
        for(Piece piece:this.getPieces())
        {
            piece.chosable=false;
        }

        isDirty=true;
        draw();
    }

    public void showPreviousStep()
    {
        if(step>0)
        {
            if(isPlaying)
            {
                stopReplay();
                parsePrevStep();
            }
            else
            {
                parsePrevStep();
            }

        }
        else
        {
            stopReplay();
            return;
        }
    }

    public void showNextStep()
    {
        if(step<record.history.size()-1)
        {
            parseNextStep();
        }
        else
        {
            return;
        }
    }

    public void playThisGame()
    {
        if(isPlaying)
        {
            stopReplay();
        }
        else
        {
            startReplay();
        }
    }

    public void beginning()
    {
        stopReplay();
        analyzeBoard(0);
        setStep(0);
    }

    public void endgame()
    {
        stopReplay();
        analyzeBoard(record.history.size()-1);
        setStep(record.history.size()-1);
    }

    public void startReplay()
    {
        if(record.history.size()<=1)
        {
            return;
        }
        isPlaying=true;
        System.out.println("开始播放");
        replay.play();
    }

    public void stopReplay()
    {
        System.out.println("停止播放");
        isPlaying=false;
        replay.stop();
    }




    private void calculateValidMovesCache() {
        cachedValidMoves.clear();
        if (selectedPiece == null) return;

        for (int r = 1; r <= 10; r++) {
            for (int c = 1; c <= 9; c++) {
                if (selectedPiece.canMoveTo(r, c, this)) {
                    cachedValidMoves.add(new int[]{r, c});
                }
            }
        }
    }

    private void handleMouseClick(int x, int y) {
        int col = (int)((float)(x + 19.2 - rightMargin) / cellSize) + 1;
        int row = (int)((float)(y + 19.2 - upperMargin) / cellSize) + 1;

        if (!isValidPosition(row, col)) return;

        isDirty = true;

        if (selectedPiece == null) {
            Piece p = getPieceAt(row, col);
            if (p != null && p.chosable) {
                selectedPiece = p;
                selected = true;
                calculateValidMovesCache();
            }
        } else {
            Piece target = getPieceAt(row, col);

            if (target == selectedPiece) {
                selectedPiece = null;
                selected = false;
                cachedValidMoves.clear();
            }
            else if (target != null && target.chosable && target.getTeam() == selectedPiece.getTeam()) {
                selectedPiece = target;
                calculateValidMovesCache();
            } else {
                movePiece(selectedPiece, row, col);
                selectedPiece = null;
                selected = false;
                cachedValidMoves.clear();
            }
        }
    }

    private void initializepeices() {
        pieces.add(new BlackSoldierPiece(this, 4, 1));
        pieces.add(new BlackSoldierPiece(this, 4, 3));
        pieces.add(new BlackSoldierPiece(this, 4, 5));
        pieces.add(new BlackSoldierPiece(this, 4, 7));
        pieces.add(new BlackSoldierPiece(this, 4, 9));

        pieces.add(new RedSoldierPiece(this, 7, 1));
        pieces.add(new RedSoldierPiece(this, 7, 3));
        pieces.add(new RedSoldierPiece(this, 7, 5));
        pieces.add(new RedSoldierPiece(this, 7, 7));
        pieces.add(new RedSoldierPiece(this, 7, 9));

        pieces.add(new RedCannonPiece(this, 8, 2));
        pieces.add(new RedCannonPiece(this, 8, 8));
        pieces.add(new BlackCannonPiece(this, 3, 2));
        pieces.add(new BlackCannonPiece(this, 3, 8));

        pieces.add(new BlackGuardPiece(this, 1, 4));
        pieces.add(new BlackGuardPiece(this, 1, 6));
        pieces.add(new RedGuardPiece(this, 10, 4));
        pieces.add(new RedGuardPiece(this, 10, 6));

        pieces.add(new RedGeneralPiece(this, 10, 5));
        pieces.add(new BlackGeneralPiece(this, 1, 5));

        pieces.add(new BlackHorsePiece(this, 1, 2));
        pieces.add(new BlackHorsePiece(this, 1, 8));
        pieces.add(new RedHorsePiece(this, 10, 2));
        pieces.add(new RedHorsePiece(this, 10, 8));

        pieces.add(new RedTankPiece(this, 10, 1));
        pieces.add(new RedTankPiece(this, 10, 9));
        pieces.add(new BlackTankPiece(this, 1, 1));
        pieces.add(new BlackTankPiece(this, 1, 9));

        pieces.add(new RedXiangPiece(this, 10, 3));
        pieces.add(new RedXiangPiece(this, 10, 7));
        pieces.add(new BlackXiangPiece(this, 1, 3));
        pieces.add(new BlackXiangPiece(this, 1, 7));
    }

    public void addPieces(Piece piece) {
        pieces.add(piece);
        isDirty = true;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public void setDelatingPiece(Piece piece) {
        delatingPiece = piece;
    }

    public int getTurn() {
        return turn;
    }

    public void nextTurn() {
        turn++;
    }

    public void updatePieces() {
        for (Piece piece : this.getPieces()) {
            piece.update();
        }
    }

    public void drawPieces(GraphicsContext gc) {
        for (Piece piece : this.getPieces()) {
            piece.draw(gc);
        }
    }

    public void getBackgoundImage() {
        try {
            var bgStream = getClass().getResourceAsStream("/board (3) (1).png");
            if (bgStream != null) backgoundImage = new Image(bgStream);

            var lightStream = getClass().getResourceAsStream("/lighteffect.png");
            if (lightStream != null) lightImage = new Image(lightStream);

            var selectStream = getClass().getResourceAsStream("/Recframe/Shape Square-WF.png");
            if (selectStream != null) selectedImage = new Image(selectStream);

            var moveStream = getClass().getResourceAsStream("/Recframe/icon.png");
            if (moveStream != null) moveableImage = new Image(moveStream);

            var killStream = getClass().getResourceAsStream("/Recframe/killable.png");
            if (killStream != null) killableImage = new Image(killStream);

            var checkStream = getClass().getResourceAsStream("/Recframe/check_mated.png");
            if (checkStream != null) checkedImage = new Image(checkStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if (movingPiece != null) {
            pieceMoving = true;
            movingPiece.addX(movingPiece.getSpeedX());
            movingPiece.addY(movingPiece.getSpeedY());

            boolean arrived = false;

            int destX = Unit.coltoX(movingPiece.getCols(), 0);
            int destY = Unit.rowtoY(movingPiece.getRows(), 0);

            if (Math.abs(movingPiece.getX() - destX) < 10 &&
                    Math.abs(movingPiece.getY() - destY) < 10) {
                movingPiece.setX(destX);
                movingPiece.setY(destY);
                arrived = true;
            }

            if (arrived)
            {
                if (Game.isNetworkMode() && !record.isUndoing)
                {
                    int r1 = movingPiece.getPreRow();
                    int c1 = movingPiece.getPreCol();
                    int r2 = movingPiece.getRows();
                    int c2 = movingPiece.getCols();
                    new Thread(() -> {
                        try {
                            Game.getNetwork().postMove(new Move(r1, c1, r2, c2));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }

                movingPiece.switchMovingStatus(false);
                Piece tempMovingPiece = movingPiece;
                movingPiece = null;

                if (delatingPiece != null) {
                    pieces.remove(delatingPiece);
                    delatingPiece = null;
                }

                if(!isReplaying)
                {
                    if (!record.isUndoing) {
                        nextTurn();
                        aSwitch.proceed();
                        setCheckMateResult(Judger.judge(this));

                        book.updateGameState(
                                tempMovingPiece.getPreRow(),
                                tempMovingPiece.getPreCol(),
                                tempMovingPiece.getRows(),
                                tempMovingPiece.getCols()
                        );
                        record.updateGameState(
                                tempMovingPiece.getPreRow(),
                                tempMovingPiece.getPreCol(),
                                tempMovingPiece.getRows(),
                                tempMovingPiece.getCols()
                        );

                        int currentVersion = ++operationVersion;
                        aiFuture = aiExecutor.submit(() -> {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) { return; }
                            if (currentVersion != operationVersion) return;
                            try {
                                if (!Game.isNetworkMode()) {
                                    ai.test(this);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                    } else {
                        aSwitch.proceed();
                        pendingUndoSteps--;
                        if (pendingUndoSteps > 0) {
                            Platform.runLater(this::executeOneUndoStep);
                        } else {
                            finishUndo();
                        }
                    }
                }

                pieceMoving = false;
                isDirty = true;
            }
        }
        if (pieceMoving) {
            updatePieces();
        }
    }

    public void draw() {
        if (!isDirty && movingPiece == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        double currentWidth = getWidth();
        double currentHeight = getHeight();

        if (currentWidth == 0 || currentHeight == 0) return;

        gc.clearRect(0, 0, currentWidth, currentHeight);

        double scaleX = currentWidth / LOGICAL_WIDTH;
        double scaleY = currentHeight / LOGICAL_HEIGHT;
        renderScale = Math.min(scaleX, scaleY);

        double drawnWidth = LOGICAL_WIDTH * renderScale;
        double drawnHeight = LOGICAL_HEIGHT * renderScale;
        renderOffsetX = (int) ((currentWidth - drawnWidth) / 2);
        renderOffsetY = (int) ((currentHeight - drawnHeight) / 2);

        gc.save();
        gc.translate(renderOffsetX, renderOffsetY);
        gc.scale(renderScale, renderScale);

        if (backgoundImage != null) {
            gc.drawImage(backgoundImage, 0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT);
        }

        drawPieces(gc);

        if (selected && selectedPiece != null) {
            if (selectedImage != null) {
                gc.drawImage(selectedImage
                        , selectedPiece.getX() - 0.5 * tileSize
                        , selectedPiece.getY() - 0.5 * tileSize
                        , 2 * tileSize, 2 * tileSize);
            }

            gc.setGlobalAlpha(0.7);

            for (int[] pos : cachedValidMoves) {
                int r = pos[0];
                int c = pos[1];
                if (this.getPieceAt(r, c) != null) {
                    if (killableImage != null)
                        gc.drawImage(killableImage, Unit.coltoX(c, tileSize), Unit.rowtoY(r, tileSize), 2 * tileSize, 2 * tileSize);
                } else {
                    if (moveableImage != null)
                        gc.drawImage(moveableImage, Unit.coltoX(c, tileSize), Unit.rowtoY(r, tileSize), 2 * tileSize, 2 * tileSize);
                }
            }

            gc.setGlobalAlpha(1.0);
        }

        for (int row = 1; row <= 10; row++) {
            for (int col = 1; col <= 10; col++) {
                Piece p = this.getPieceAt(row, col);
                if (p instanceof RedGeneralPiece) {
                    if (((RedGeneralPiece) p).isChecked()) {
                        gc.drawImage(checkedImage, Unit.coltoX(col, tileSize), Unit.rowtoY(row, tileSize), 2 * tileSize, 2 * tileSize);
                    }
                } else if (p instanceof BlackGeneralPiece) {
                    if (((BlackGeneralPiece) p).isChecked()) {
                        gc.drawImage(checkedImage, Unit.coltoX(col, tileSize), Unit.rowtoY(row, tileSize), 2 * tileSize, 2 * tileSize);
                    }
                }
            }
        }

        gc.restore();

        if (movingPiece == null)
        {
            isDirty = false;
        }
    }

    public void handleNetworkMove(Move move) {
        Piece p = getPieceAt(move.getOriRow(), move.getOriCol());
        if (p == null) return;

        Piece target = getPieceAt(move.getNewRow(), move.getNewCol());
        if (target != null) {
            setDelatingPiece(target);
        }

        p.setPreRow(p.getRows());
        p.setPreCol(p.getCols());
        p.forceTo(move.getNewRow(), move.getNewCol(),this);

        movingPiece = p;
        movingPiece.switchMovingStatus(true);
        isDirty = true;
    }

    public boolean isRedFirst() {
        return redFirst;
    }

    public void setSide(boolean bf) {
        redFirst = bf;
    }

    public static void needAi(boolean bg) {
        needAi = bg;
    }

    public boolean aiNeeded() {
        return needAi;
    }

    public Record getRecord()
    {
        return record;
    }
}