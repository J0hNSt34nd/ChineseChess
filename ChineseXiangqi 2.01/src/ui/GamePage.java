package ui;

import Ai.Move;
import Ai.simpleBoard;
import Pieces.Piece;
import Tool.Record;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import main.Board;
import main.Game;
import Tool.PlayerRecord;
import Tool.UserProfile;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GamePage extends StackPane {
    private static final double FOOTER_HEIGHT = 100;
    private static final int TOTAL_VALUE;
    private static boolean isBlackSide = true;

    private final Avatar opponentAvatar = new Avatar();
    private final NameLabel opponentLabel = new NameLabel();
    private final Avatar userAvatar = new Avatar();
    private final NameLabel userLabel = new NameLabel();
    private final VBox opponentView = new VBox(8, opponentAvatar, opponentLabel);
    private final VBox userView = new VBox(8, userAvatar, userLabel);
    private final BoardView boardView = new BoardView();
    private final ResultView resultView = new ResultView();
    private final HBox playingToolBar = new HBox(
            createToolButton("认输", "resign", this::resign),
            createToolButton("悔棋", "retract", this::retract),
//            createToolButton("提示", "tips", this::tips), // 功能还没做出来
            createToolButton("记录", "record",_ -> record() ),
            createToolButton("保存", "Save", _ -> save()),
            createToolButton("加载", "load", _ -> load())
    );
    private final HBox resultToolBar = new HBox(
            createToolButton("返回", "return", _-> goBack()),
            createRoundButton("重新挑战", _-> restart()),
            createToolButton("复盘", "replay", this::replay)
    );
    private final HBox replayToolBar = new HBox(
            createToolButton("开局", "first", _ -> boardView.goFirst()),
            createToolButton("上一步", "previous", _ -> boardView.goPrevious()),
            createToolButton("播放", "play", _ -> boardView.startReplay()),
            createToolButton("下一步", "next", _ -> boardView.goNext()),
            createToolButton("终局", "last", _ -> boardView.goLast())
    );
    private final ToolButton returnButton = createToolButton("返回", "return", this::replay);
    private final StackPane footerView = new StackPane(playingToolBar, resultToolBar, replayToolBar);
    private final DoubleProperty position = new SimpleDoubleProperty(0.5);
    private final Node positionBar = createPositionBar();

    private Dialog resignDialog;
    private RequestDialog requestDialog;
    private ReplyDialog replyDialog;
    private EventHandler<ActionEvent> onGoBack;

    static {
        int total = 0;
        for (int i = 2; i <= 7; ++i)
            total += 1;
        TOTAL_VALUE = total;
    }

    public GamePage() {
        ImageView background = new ImageView(
                new Image(String.valueOf(getClass().getResource("/background.png")))
        );
        background.fitWidthProperty().bind(widthProperty());
        background.fitHeightProperty().bind(heightProperty());
        background.setManaged(false);
        background.setOpacity(0.5);

        opponentView.setAlignment(Pos.TOP_CENTER);
        userView.setAlignment(Pos.BOTTOM_CENTER);

        VBox boardBox = new VBox(32, positionBar, boardView);
        boardBox.setAlignment(Pos.CENTER);

        HBox content = new HBox(48, opponentView, boardBox, userView);
        content.setAlignment(Pos.CENTER);

        playingToolBar.setAlignment(Pos.CENTER);
        resultToolBar.setAlignment(Pos.CENTER);
        replayToolBar.setAlignment(Pos.CENTER);
        resultToolBar.setVisible(false);
        replayToolBar.setVisible(false);

        footerView.setBackground(new Background(new BackgroundFill(Color.web("#714214", 0.6), CornerRadii.EMPTY, Insets.EMPTY)));
        footerView.setPrefHeight(FOOTER_HEIGHT);
        footerView.setMinHeight(FOOTER_HEIGHT);
        footerView.setMaxHeight(FOOTER_HEIGHT);

        VBox layout = new VBox(32, new StackPane(content, resultView), footerView);
        layout.setPadding(new Insets(32, 0, 0, 0));

        returnButton.setDisplay(ToolButton.Display.TextBesideIcon);
        returnButton.setPrefSize(96, 24);
        returnButton.setMinSize(96, 24);
        returnButton.setMaxSize(96, 24);
        returnButton.setIconSize(24);
        returnButton.setFill(Color.web("#874802"));
        returnButton.setVisible(false);
        StackPane.setAlignment(returnButton, Pos.TOP_LEFT);
        StackPane.setMargin(returnButton, new Insets(48));

        getChildren().addAll(background, new BackgroundAnimation(), layout, returnButton);

        boardView.widthProperty().addListener((_, _, width) -> {
            double r = width.doubleValue()/10;
            opponentAvatar.setRadius(r);
            userAvatar.setRadius(r);

            double w = width.doubleValue()/4;
            opponentLabel.setPrefWidth(w);
            userLabel.setPrefWidth(w);

            double s = width.doubleValue()/3;
            playingToolBar.setSpacing(s);
            resultToolBar.setSpacing(s);
            replayToolBar.setSpacing(s);

            resultView.setPrefHeight(w);
        });


        Game.getNetwork().setOnRestart(this::restart);

        Game.getNetwork().setOnRequest(request -> {
            Platform.runLater(() -> {
                if (replyDialog == null) {
                    replyDialog = new ReplyDialog(getScene().getWindow());
                }

                if (request.contains("悔棋")) {
                    replyDialog.showRequest(request,
                            () -> {
                                // 响应方同意后，本地执行 undo
                                boardView.getGameBoard().undo();
                            },
                            null
                    );
                }
                else if (request.contains("再战")) {
                    replyDialog.showRequest(request,
                            this::restart,
                            this::goBack
                    );
                }
                else if (request.contains("议和")) {
                    replyDialog.showRequest(request,
                            () -> endGame(0),
                            null
                    );
                }
            });
        });
    }

    public void setOnGoBack(EventHandler<ActionEvent> handler) { onGoBack = handler; }

    public void toggleVisible() {
        if (!isVisible()) {
            UserProfile opponent = Game.getOpponent();

            System.out.println("本地用户:" + Game.getUserName());
            System.out.println("对手用户:" + (opponent != null ? opponent.getName() : " 未设置"));// 这里不知道为啥有bug， 邀请方无法获取到对方名字

            opponentView.setVisible(false);
            userView.setVisible(false);
            boardView.setVisible(false);
            positionBar.setVisible(false);
            footerView.setVisible(false);

            if (opponent != null) {
                opponentAvatar.setImage(Game.getAvatarImage(opponent.getAvatar()));
                opponentLabel.setText(opponent.getName());
            }
            userAvatar.setImage(Game.getUserAvatarImage());
            userLabel.setText(Game.getUserName());

            if (requestDialog == null)
                requestDialog = new RequestDialog(getScene().getWindow());
            if (replyDialog == null) {
                replyDialog = new ReplyDialog(getScene().getWindow());
            }
        }

        GameTransitions.play(isVisible(),
                        GameTransitions.fade(1, this),
                        new ParallelTransition(
                                GameTransitions.move(0.5, 0, -1, boardView),
                                GameTransitions.move(0.5, 0, 1, footerView)),
                        new ParallelTransition(
                                GameTransitions.move(0.5, -1, 0, opponentView),
                                GameTransitions.move(0.5, 1, 0, userView),
                                GameTransitions.zoom(0.5, 0, positionBar),
                                GameTransitions.fade(0.5, positionBar))).
                setOnFinished(_ -> {
                    if (isVisible()) boardView.start(1);
                    else boardView.clear();
                });
    }

    public void setBlackSide(boolean blackSide) {
        isBlackSide = blackSide;
        boardView.initSide(blackSide);
    }

    private static ToolButton createToolButton(String text, String iconName, EventHandler<ActionEvent> onAction) {
        ToolButton button = new ToolButton();
        button.setText(text);
        button.setIconSource(String.format("/%s.svg", iconName));
        button.setOnAction(onAction);
        return button;
    }

    private static RoundButton createRoundButton(String text, EventHandler<ActionEvent> onAction) {
        RoundButton button = new RoundButton();
        button.setText(text);
        button.setTextFill(Color.web("#7E490E"));
        button.setStroke(Color.web("#C58D46"));
        button.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#FFE6C4")),
                new Stop(0.12, Color.web("#FFCF8E")),
                new Stop(0.86, Color.web("#FFCF8E")),
                new Stop(1.00, Color.web("#DF9646"))));
        button.setOnAction(onAction);
        return button;
    }

    private Node createPositionBar() {
        final double height = 24;
        final double s = 4;
        Rectangle background = new Rectangle(100, height);
        background.setArcWidth(height);
        background.setArcHeight(height);
        background.setStroke(Color.web("#FFF", 0.6));
        background.setFill(Color.web("#000", 0.16));
        background.setEffect(new InnerShadow(2, 0, 1, Color.web("#381E09", 0.8)));
        background.widthProperty().bind(boardView.widthProperty().multiply(0.5));

        LinearGradient stroke = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FFF", 0.3)),
                new Stop(1, Color.web("#000", 0.3)));

        Rectangle blackRect = new Rectangle(0, s, 0, height - s * 2);
        Rectangle redRect = new Rectangle(0, s, 0, height - s * 2);
        blackRect.setArcWidth(height - s * 2);
        blackRect.setArcHeight(height - s * 2);
        redRect.setArcWidth(height - s * 2);
        redRect.setArcHeight(height - s * 2);
        blackRect.setFill(Color.web("#3A3A39"));
        redRect.setFill(Color.web("#CC0000"));
        blackRect.setStroke(stroke);
        redRect.setStroke(stroke);
        blackRect.setManaged(false);
        redRect.setManaged(false);
        blackRect.layoutXProperty().bind(background.layoutXProperty().add(s));
        redRect.layoutXProperty().bind(background.layoutXProperty().add(s));

        Pane pane = new StackPane(background, blackRect, redRect);

        Runnable update = () -> {
            double w = (background.getWidth() - s * 2) * (1 - position.get());
            blackRect.setWidth(w);
            redRect.setWidth(background.getWidth() - s * 2 - w);
            redRect.setX(w);
        };

        background.widthProperty().addListener((_) -> update.run());
        position.addListener((_) -> update.run());
        return pane;
    }

    private void endGame(int result) {
        Game.updateRecord(result);
        PlayerRecord record = Game.getRecord();

        resultView.setAiIcon(opponentAvatar.getImage());
        resultView.setPlayerIcon(userAvatar.getImage());
        resultView.setAiName(opponentLabel.getText());
        resultView.setPlayerName(userLabel.getText());
        resultView.setTotal(record.getWins(), record.getLosses(), record.getDraws());
        resultView.setResult(result);

        toggleResult();
        togglePlaying(_ -> {
            if (Game.isNetworkMode()) {
                Platform.runLater(() -> {
                    requestDialog.requestRestart(this::restart, this::goBack);
                });
            }
        });
    }

    private void resign(ActionEvent e) {
        if (resignDialog == null && (boardView.getGameBoard().isCheckMateResult()==false))
            resignDialog = new ResignDialog(getScene().getWindow(), "认输", ()-> endGame(-1));
        else if (resignDialog == null && (boardView.getGameBoard().isCheckMateResult()==true))
            resignDialog = new ResignDialog(getScene().getWindow(), "认输", ()-> endGame(1));
        resignDialog.showAndWait();
    }

    private void retract(ActionEvent e) {
        if (Game.isNetworkMode()) {
            requestDialog.requestRetract(() -> {
                boardView.getGameBoard().undo();
            });
        }
        else
            boardView.getGameBoard().undo();
    }

    private void tips(ActionEvent e) {
    }

    private void togglePlaying(EventHandler<ActionEvent> onFinished) {
        GameTransitions.play(playingToolBar.isVisible(),
                new ParallelTransition(
                        GameTransitions.zoom(0.5, 0, positionBar),
                        GameTransitions.fade(0.5, positionBar)),
                GameTransitions.flip(0.5, playingToolBar, resultToolBar)
        ).setOnFinished(onFinished);
    }

    private void toggleResult() {
        Duration duration = GameTransitions.DURATION.multiply(0.5);
        GameTransitions.play(resultView.isVisible(),
                new ParallelTransition(
                        GameTransitions.move(0.5, -1, 0, opponentView),
                        GameTransitions.move(0.5, 1, 0, userView)
                ),
                new ParallelTransition(
                        GameTransitions.zoom(0.5, 0, resultView),
                        GameTransitions.fade(0.5, resultView),
                        scaleBoard(duration))
        );
    }

    private void toggleReplay() {
        GameTransitions.play(replayToolBar.isVisible(),
                GameTransitions.move(0.5, -1, 0, returnButton),
                GameTransitions.flip(0.5, replayToolBar, resultToolBar)
        );
        boardView.InitializeReplay();
    }

    private void goBack() {
        toggleResult();
        togglePlaying(onGoBack);

        if (Game.isNetworkMode())
            Game.getNetwork().separate();
        boardView.getGameBoard().setIsReplaying(false);
        boardView.end();
    }

    private void restart() {
        toggleResult();
        togglePlaying(_ -> {
            boardView.getGameBoard().setIsReplaying(false);
            boardView.end();
            boardView.start(boardView.getTurn());
        });
    }

    private void replay(ActionEvent event) {
        toggleResult();
        toggleReplay();
    }

    private Transition scaleBoard(Duration duration) {
        ScaleTransition scaleBoard = new ScaleTransition(duration, boardView);
        TranslateTransition moveBoard = new TranslateTransition(duration, boardView);
        ParallelTransition transition = new ParallelTransition(scaleBoard, moveBoard);

        if (boardView.getScaleX() < 0.9) {
            scaleBoard.setToX(1);
            scaleBoard.setToY(1);
            moveBoard.setToY(0);
        }
        else {
            scaleBoard.setByX(-0.3);
            scaleBoard.setByY(-0.3);
            moveBoard.setByY(boardView.getHeight() * 0.15);
        }
        return transition;
    }

    public void startGame(boolean isBlack) {
        boardView.getGameBoard().setSide(isBlack);
        boardView.getGameBoard().draw();
        boardView.start(1);
    }

    public BoardView getBoardView() {
        return boardView;
    }

    private static class Data implements Serializable
    {
        boolean isRedFirst;
        int turn;
        ArrayList<int[][]> history;
        String username;
    }

    private void showError(String title,String error)
    {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(error);
            alert.showAndWait();
        });
    }

    private boolean isGuest()
    {
        String username = getUsername();
        return username == null || username.trim().isEmpty();
    }

    private boolean isValidData(Data data)
    {
        if (data == null)
        {
            showError("存档损坏", "数据为空");
            return false;
        }
        if(data.history==null)
        {
            showError("存档出错","走棋记录为空");
        }
        if(data.turn<=0)
        {
            showError("存档损坏","轮数为非正数");
        }
        for(int[][] simpleBoard : data.history)
        {
            if(simpleBoard==null || simpleBoard.length!=10 || simpleBoard[0].length != 9)
            {
                showError("存档损坏","保存非法的棋盘状态");
            }
        }
        return true;
    }

    private String getUsername()
    {
        try
        {
            InputStream input = new FileInputStream("chess.properties");
            Properties temp = new Properties();
            temp.load(input);
            return temp.getProperty("written_User","");

        }catch (IOException e)
        {
            return "";
        }
    }

    private void save()
    {
        if(isGuest())
        {
            showError("存档失败","游客模式不能保存");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存游戏");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("存档文件(*.xiangqi)","*.xiangqi")
        );

        String username = getUsername();
        fileChooser.setInitialFileName(username);

        File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        if(file==null) return;

        try
        {
            Board thatBoard = boardView.getGameBoard();
            Data data = new Data();
            data.isRedFirst = thatBoard.isRedFirst();
            data.turn = thatBoard.getTurn();
            data.history = new ArrayList<>(thatBoard.record.history);
            data.username = username;

            try(ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file)))
            {
                output.writeObject(data);
            }
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private void load()
    {
        if(isGuest())
        {
            showError("读取失败","游客模式不能读取");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("加载游戏");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("象棋存档文件 (*.xiangqi)", "*.xiangqi")
        );

        File file = fileChooser.showOpenDialog(this.getScene().getWindow());
        if(file == null) return;

        try
        {
            Data data;
            try(ObjectInputStream input = new ObjectInputStream(new FileInputStream(file)))
            {
                data = (Data) input.readObject();
            }
            catch (Exception e)
            {
                showError("加载出错","读取错误");
                return;
            }

            if(!isValidData(data)) return;

            String username = getUsername();
            if(!username.equals(data.username))
            {
                showError("加载失败","不能加载其他用户的存档");
                return;
            }

            Board thatBoard = boardView.getGameBoard();
            thatBoard.reset();
            thatBoard.setSide(data.isRedFirst);
            thatBoard.setTurn(data.turn);
            thatBoard.record.history.clear();
            thatBoard.record.history.addAll(data.history);
            thatBoard.record.syncToLastHistory();
            restore(thatBoard,data.history);
            thatBoard.aSwitch.forceproceed();
            thatBoard.draw();

        }catch(Exception e)
        {
            showError("加载失败","未知原因加载失败");
        }
    }

    private void restore(Board board,ArrayList<int[][]> history)
    {
        if(history.isEmpty()) return;

        int[][] latest = history.get(history.size()-1);
        board.pieces.clear();

        for(int row=0;row<10;row++)
        {
            for(int col=0;col<9;col++)
            {
                int code = latest[row][col];
                Piece piece = simpleBoard.convertToPiece(code,row+1,col+1,board);
                if(piece != null) board.pieces.add(piece);
            }
        }
    }

    public void record() {
        Board currboard = boardView.getGameBoard();
        Record record = currboard.getRecord();
        if (record.history.size() <= 1) {
            showError("错误", "无走棋记录");
            return;
        }

        StringBuilder text = new StringBuilder();
        text.append("当前棋谱\n\n");
        for (int i = 1; i < record.history.size(); i++) {
            int[][] previous = record.history.get(i - 1);
            int[][] current = record.history.get(i);
            Move move = simpleBoard.addUpForNonUndo(previous, current, currboard);
            if (move == null) continue;
            boolean isRed = (i % 2 == 1);
            text.append(convertToMark(move, previous, isRed));
        }

        javafx.stage.Stage popupStage = new javafx.stage.Stage();
        popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popupStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        popupStage.initOwner(getScene().getWindow());

        PopupPane popupPane = new PopupPane(2.0);
        popupPane.setFixedSize(480, 550);

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(60, 40, 40, 40));

        Label titleLabel = new Label("棋谱记录 (共" + (record.history.size() - 1) + "步)");
        titleLabel.setFont(Font.font("SimSun", javafx.scene.text.FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#874802"));

        TextArea textArea = new TextArea(text.toString());
        textArea.setEditable(false);
        textArea.setPrefSize(380, 320);
        textArea.setFont(Font.font("KaiTi", FontWeight.EXTRA_BOLD ,20));
        textArea.setStyle(
                "-fx-control-inner-background: #F6E4CF; " +
                        "-fx-text-fill: #552E03; " +
                        "-fx-background-color: transparent; " +
                        "-fx-border-color: #D8AC71; " +
                        "-fx-border-radius: 5;"
        );


        Button closeBtn = new Button("关 闭");
        closeBtn.setPrefSize(120, 35);
        closeBtn.setStyle("-fx-background-color: #777777; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> popupStage.close());

        content.getChildren().addAll(titleLabel, textArea, closeBtn);
        popupPane.getChildren().add(content);

        final double[] xOffset = new double[1];
        final double[] yOffset = new double[1];
        popupPane.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        popupPane.setOnMouseDragged(event -> {
            popupStage.setX(event.getScreenX() - xOffset[0]);
            popupStage.setY(event.getScreenY() - yOffset[0]);
        });

        javafx.scene.Scene scene = new javafx.scene.Scene(popupPane);
        scene.setFill(Color.TRANSPARENT);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private String convertToMark(Move move, int[][] previous, boolean isRed)
    {
        Map<Integer,String> red = new HashMap<>();
        red.put(1,"帥"); red.put(2,"仕"); red.put(3,"相"); red.put(4,"傌");
        red.put(5,"俥"); red.put(6,"炮"); red.put(7,"兵");

        Map<Integer,String> black = new HashMap<>();
        black.put(-1,"將"); black.put(-2,"士"); black.put(-3,"象"); black.put(-4,"馬");
        black.put(-5,"車"); black.put(-6,"砲"); black.put(-7,"卒");

        int code = previous[move.getOriRow()][move.getOriCol()];
        boolean isSpecialPiece = Math.abs(code) == 2 || Math.abs(code) == 3 || Math.abs(code) == 4;
        String name = isRed ? red.get(code) : black.get(code);

        int fromRow,fromCol,toRow,toCol;
        if(isRed) {
            fromRow=10-move.getOriRow(); fromCol=9-move.getOriCol();
            toRow=10-move.getNewRow(); toCol=9-move.getNewCol();
        } else {
            fromRow=move.getOriRow()+1; fromCol=move.getOriCol()+1;
            toRow=move.getNewRow()+1; toCol=move.getNewCol()+1;
        }

        String[] positionRed = {"零","一","二","三","四","五","六","七","八","九"};
        String direction = toRow > fromRow ? "进" : (toRow == fromRow ? "平" : "退");

        StringBuilder mark = new StringBuilder().append(name);
        if(isSpecialPiece) {
            mark.append(isRed ? positionRed[fromCol] : fromCol)
                    .append(direction)
                    .append(isRed ? positionRed[toCol] : toCol);
        } else {
            mark.append(isRed ? positionRed[fromCol] : fromCol)
                    .append(direction)
                    .append(direction.equals("平") ? (isRed ? positionRed[toCol] : toCol) : (isRed ? positionRed[Math.abs(toRow-fromRow)] : Math.abs(toRow-fromRow)));
        }
        return mark.append("\n").toString();
    }
}