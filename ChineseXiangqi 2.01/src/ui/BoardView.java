package ui;

import Ai.Book;
import Command.Switch;
import Pieces.Piece;
import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import main.Board;
import main.Game;

public class BoardView extends Pane {
    private final ImageView background = new ImageView();
    private final ImageView light = new ImageView();
    private final Rectangle boardFrame = new Rectangle();
    private final Rectangle gridFrame = new Rectangle();
    private final Board gameBoard;
    private int loadcount = 1;
    private final IntegerProperty turn = new SimpleIntegerProperty(0);
    public Switch aSwitch;
    private boolean needAi;

    private AnimationTimer gameLoop;

    public BoardView() {
        Book book = new Book();

        gameBoard = new Board(book);

        Game.initNetworkListeners(gameBoard);

        if (Game.isNetworkMode()) {
            gameBoard.setSide(Game.getisRed());
        } else {
            gameBoard.setSide(true);
        }

        var bookStream = getClass().getResourceAsStream("/games.pgns");

        if (bookStream != null) {
            book.load(bookStream);
        } else {
            System.err.println("警告：未找到 /games.pgns 文件，开局库将无法使用。");
        }


        aSwitch = new Switch(gameBoard);

        gameBoard.setVisible(true);

        try {
            var bgStream = getClass().getResourceAsStream("/board_background.png");
            if (bgStream != null) background.setImage(new Image(bgStream));

            var lightStream = getClass().getResourceAsStream("/board_light.png");
            if (lightStream != null) light.setImage(new Image(lightStream));
            light.setOpacity(0.5);
        } catch (Exception e) {
            System.err.println("资源加载错误: " + e.getMessage());
        }

        boardFrame.setFill(Color.web("#331C00"));
        boardFrame.setEffect(new DropShadow(20, Color.web("#381E09")));

        gridFrame.setStroke(Color.web("#785940"));
        gridFrame.setFill(null);

        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setRadius(5.0);
        innerShadow.setChoke(0.0);
        innerShadow.setOffsetX(1.0);
        innerShadow.setOffsetY(3.0);
        innerShadow.setColor(Color.web("#000000CC"));
        gridFrame.setEffect(innerShadow);

        background.setMouseTransparent(true);
        light.setMouseTransparent(true);
        gridFrame.setMouseTransparent(true);

        getChildren().addAll(boardFrame, background, gameBoard, gridFrame, light);

        widthProperty().addListener((obs, oldVal, newVal) -> updateLayout());
        heightProperty().addListener((obs, oldVal, newVal) -> updateLayout());
    }

    public Board getGameBoard()
    {
        return gameBoard;
    }


    public void start(int startTurn) {
        gameBoard.ai.setLevel(Game.getAILevel());
        gameBoard.setOpacity(1);
        if (Game.isNetworkMode()) gameBoard.setSide(Game.getisRed());
        gameBoard.aSwitch.forceproceed();

        if (gameLoop == null) {
            gameLoop = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    gameBoard.update();
                    gameBoard.draw();

                    if (turn.get() != gameBoard.getTurn()) {
                        turn.set(gameBoard.getTurn());
                    }
                }
            };
        }
        gameLoop.start();
        turn.set(startTurn);
//        gameBoard.setSide(!sideSwitch.getIsBlack());
//        System.out.println(gameBoard.isRedFirst());
        if (!gameBoard.isRedFirst()) {
//            for (Piece piece : gameBoard.getPieces()) {
//                if (piece.getTeam() == 1)
//                    piece.chosable = false;
//                else if (piece.getTeam() == 0) {
//                    piece.chosable = true;
//                }
//            }
            gameBoard.record.reset();
            if (!Game.isNetworkMode())
                gameBoard.ai.test(gameBoard);
        }
    }

    public void netGameStart(int startTurn, boolean isRed) {
        gameBoard.setOpacity(1);
        gameBoard.setSide(isRed);


        if (gameLoop == null) {
            gameLoop = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    // 每一帧都手动调用 board 的逻辑和绘图
                    gameBoard.update();
                    gameBoard.draw();

                    if (turn.get() != gameBoard.getTurn()) {
                        turn.set(gameBoard.getTurn());
                    }
                }
            };
        }
        gameLoop.start();
        turn.set(startTurn);
        if (!gameBoard.isRedFirst()) {
            gameBoard.record.reset();
        }
    }

    public void end() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        turn.set(0);
        gameBoard.reset();
    }

    public void redFirst(boolean bf) {
        gameBoard.setSide(bf);
    }

    public void takeBack() {
        gameBoard.undo();
    }

    public ReadOnlyObjectProperty<Bounds> boundsProperty() { return boardFrame.boundsInParentProperty(); }
    public IntegerProperty turnProperty() { return turn; }

    public void clear() {
        end();
        gameBoard.setOpacity(0);

//        gameBoard.reset();
    }

    public void reset(){
        gameBoard.reset();
        gameBoard.draw();
    }

    public void InitializeReplay()
    {
        gameBoard.setIsReplaying(true);
        for(Piece piece:gameBoard.getPieces())
        {
            piece.chosable=false;
        }
        gameBoard.setStepToTurn();
    }

    private void updateLayout() {
        Insets p = getPadding();
        double w = getWidth() - p.getLeft() - p.getRight();
        double h = getHeight() - p.getTop() - p.getBottom();
        double x = p.getLeft();
        double y = p.getTop();

        if (w <= 0 || h <= 0) return;

        double cellWidth = Math.min(w / 9.2, h / 10.2);

        double targetW = cellWidth * 9;
        double targetH = cellWidth * 10;

        double boardOriginalW = gameBoard.getPrefWidth();
        double boardOriginalH = gameBoard.getPrefHeight();

        if (boardOriginalW <= 0) boardOriginalW = 48 * 9;
        if (boardOriginalH <= 0) boardOriginalH = 48 * 12;

        gameBoard.resize(boardOriginalW, boardOriginalH);

        double scale = Math.min(targetW / boardOriginalW, targetH / boardOriginalH);

        gameBoard.setScaleX(scale);
        gameBoard.setScaleY(scale);

        double centerX = x + w / 2;
        double centerY = y + h / 2;

        gameBoard.setLayoutX(centerX - boardOriginalW / 2);
        gameBoard.setLayoutY(centerY - boardOriginalH / 2);

        double bgW = targetW + cellWidth * 1.2;
        double bgH = targetH + cellWidth * 1.2;
        background.setFitWidth(bgW);
        background.setFitHeight(bgH);
        background.setLayoutX(centerX - bgW / 2);
        background.setLayoutY(centerY - bgH / 2);

        light.setFitWidth(bgW);
        light.setFitHeight(bgH);
        light.setLayoutX(background.getLayoutX());
        light.setLayoutY(background.getLayoutY());

        double gridW = cellWidth * 8;
        double gridH = cellWidth * 9;
        double strokeW = cellWidth * 0.07;
        double frameInnerW = gridW + strokeW * 3;
        double frameInnerH = gridH + strokeW * 3;

        gridFrame.setStrokeWidth(strokeW);
        gridFrame.setWidth(frameInnerW);
        gridFrame.setHeight(frameInnerH);
        gridFrame.setLayoutX(centerX - frameInnerW / 2);
        gridFrame.setLayoutY(centerY - frameInnerH / 2);

        strokeW = cellWidth * 0.13;
        double frameOuterW = bgW + strokeW * 2;
        double frameOuterH = bgH + strokeW * 2;
        boardFrame.setArcWidth(strokeW);
        boardFrame.setArcHeight(strokeW);
        boardFrame.setWidth(frameOuterW);
        boardFrame.setHeight(frameOuterH);
        boardFrame.setLayoutX(centerX - frameOuterW / 2);
        boardFrame.setLayoutY(centerY - frameOuterH / 2);
    }

    public void goFirst()
    {
        gameBoard.beginning();
    }

    public void goPrevious()
    {
        gameBoard.showPreviousStep();
    }

    public void startReplay()
    {
        gameBoard.playThisGame();
    }

    public void goNext()
    {
        gameBoard.showNextStep();
    }

    public void goLast()
    {
        gameBoard.endgame();
    }

    public int getTurn() {
        return gameBoard.getTurn();
    }

    public void initSide(boolean isBlack) {
        gameBoard.setSide(isBlack);

        gameBoard.draw();
    }
}