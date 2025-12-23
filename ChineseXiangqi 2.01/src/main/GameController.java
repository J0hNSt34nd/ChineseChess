//package main;
//
//import Ai.simpleBoard;
//import Pieces.Piece;
//import javafx.scene.control.*;
//import javafx.stage.FileChooser;
//import ui.*;
//import javafx.animation.*;
//import javafx.application.Platform;
//import javafx.event.ActionEvent;
//import javafx.event.EventHandler;
//import javafx.geometry.HPos;
//import javafx.geometry.Pos;
//import javafx.geometry.Side;
//import javafx.scene.Node;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.*;
//import javafx.util.Duration;
//import org.jetbrains.annotations.NotNull;
//import ui.Dialog;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.Properties;
//
//public class GameController
//{
//    public StackPane window;
//    public VBox aiBox;
//    public VBox playerBox;
//    public Avatar aiAvatar;
//    public NameLabel aiLabel;
//    public Avatar playerAvatar;
//    public NameLabel playerLabel;
//    public ResultView resultView;
//    public StartingView startingView;
//    public VBox playingView;
//    public BoardView boardView;
//    public HBox playFooter;
//    public HBox resultFooter;
//    public HBox replayFooter;
//    public Label redScoreLabel, blackScoreLabel;
//    public StackPane footer;
//    public ToolButton menuButton;
//    public ToolButton retractButton;
//    public ToolButton homeButton;
//    public ToolButton replayButton;
//    public ToolButton goBackButton;
//    public ToolButton firstButton;
//    public ToolButton previousButton;
//    public ToolButton playButton;
//    public ToolButton nextButton;
//    public ToolButton lastButton;
//    public ToolButton saveButton;
//    public ToolButton loadButton;
//    public RoundButton restartButton;
//
//    private Dialog resignDialog;
//
//    private int winCount = 0;
//    private int loseCount = 0;
//    private int drawCount = 0;
//
//    private final Duration transitionDuration = Duration.millis(1000);
//
//    public void initialize() {
//        startingView.setOnStart(e -> startGame());
//        startingView.setSideSwitch(e -> proceedSwitch ());
//        homeButton.setOnAction(e -> goHome());
//        replayButton.setOnAction(e -> replay());
//        restartButton.setOnAction(e -> restart());
//        goBackButton.setOnAction(e -> goResult());
//        saveButton.setOnAction(e-> save());
//        loadButton.setOnAction(e-> load());
//
//        boardView.boundsProperty().addListener((obs, oldVal, bounds) -> {
//            double r = bounds.getWidth()/10;
//            double w = bounds.getWidth()/4;
//            aiAvatar.setRadius(r);
//            aiLabel.setPrefWidth(w);
//            playerAvatar.setRadius(r);
//            playerLabel.setPrefWidth(w);
//
//            resultView.setPrefHeight(bounds.getHeight() * 0.15 + 64);
//        });
//        playFooter.spacingProperty().bind(window.widthProperty().subtract(128).multiply(0.1));
//        resultFooter.spacingProperty().bind(playFooter.spacingProperty());
//        replayFooter.spacingProperty().bind(playFooter.spacingProperty());
//
//        ContextMenu menu = new ContextMenu();
//        menu.setOpacity(0);
//        menu.getItems().addAll(
//                createMenuItem("认输", "resign.png", this::resign),
//                createMenuItem("重置", "reset.png", this::restart),
//                createMenuItem("退出", "quit.png", e -> Platform.exit()));
//
//        menuButton.setOnAction(e -> {
//            if (menu.isShowing())
//                menu.hide();
//            else {
//                // 不显示和隐藏一次弹出位置不对
//                if (menu.getOpacity() < 0.5) {
//                    menu.show(menuButton, Side.TOP, 0, 0);
//                    menu.hide();
//                    menu.setOpacity(1);
//                }
//                menu.show(menuButton, Side.TOP, -32, -32);
//            }
//        });
//        retractButton.setOnAction(e -> retract());
//    }
//
//    private void restart(ActionEvent event) {
//        boardView.reset();
//    }
//
//    @NotNull
//    private MenuItem createMenuItem(String text, String iconName, EventHandler<ActionEvent> onAction) {
//        MenuItem item = new MenuItem(text);
//        InputStream stream = getClass().getResourceAsStream(iconName);
//        if (stream!=null) item.setGraphic(new ImageView(new Image(stream)));
//        item.setOnAction(onAction);
//        return item;
//    }
//
//    private void startGame() {
//        aiLabel.setText("AI - " + startingView.getLevelName());
////        boardView.setSearchDepth(startingView.getLevel() + 2);
//
//        startingView.toggleVisible(transitionDuration);
//
//        Duration duration = transitionDuration.multiply(0.5);
//        Animation placeBoard = toggleBoard(duration);
//        Animation takeSeat = toggleSeat(duration);
//        takeSeat.setDelay(duration);
//
//        ParallelTransition pt = new ParallelTransition(placeBoard, takeSeat);
//        pt.setOnFinished(e -> boardView.start(1));
//        pt.setDelay(transitionDuration);
//        pt.play();
//    }
//
//    private void proceedSwitch() {
//        boardView.redFirst(startingView.getSideResult());
//    }
//
//    private void endGame(int result) {
//        boardView.end();
//        if (result < 0)
//            ++loseCount;
//        else if (result > 0)
//            ++winCount;
//        else
//            ++drawCount;
//
//        resultView.setAiIcon(String.valueOf(getClass().getResource("/avatar_huangzhong.png")));
//        resultView.setPlayerIcon(String.valueOf(getClass().getResource("/avatar_caocao.png")));
//        resultView.setAiName(aiLabel.getText());
//        resultView.setPlayerName(playerLabel.getText());
//        resultView.setTotal(winCount, loseCount, drawCount);
//        resultView.setResult(result);
//
//        goResult();
//    }
//
//    private void restart() {
//
//        Duration duration = transitionDuration.multiply(0.5);
//        Animation takeSeat = toggleSeat(duration);
//        ParallelTransition transition = new ParallelTransition(
//                switchFooter(duration, resultFooter, playFooter),
//                toggleResult(duration),
//                scaleBoard(duration),
//                takeSeat);
//
//        takeSeat.setDelay(duration);
//        transition.setOnFinished(e -> boardView.start(1));
//        transition.play();
//    }
//
//    private void goHome() {
//        Duration duration = transitionDuration.multiply(0.5);
//        ParallelTransition transition = new ParallelTransition(
//                toggleResult(duration),
//                toggleBoard(duration));
//
//        transition.setOnFinished(e->{
//            startingView.toggleVisible(transitionDuration);
//            resultFooter.setVisible(false);
//            playFooter.setVisible(true);
//            playFooter.setScaleY(1);
//        });
//        transition.play();
//    }
//
//    private void goResult() {
//        Duration duration = transitionDuration.multiply(0.5);
//        Node fromFooter = playFooter.isVisible() ? playFooter : replayFooter;
//        ParallelTransition transition = new ParallelTransition(
//                switchFooter(duration, fromFooter, resultFooter),
//                scaleBoard(duration),
//                toggleResult(duration));
//
//        transition.setDelay(duration);
//        transition.play();
//        toggleSeat(duration).play();
//        if (goBackButton.isVisible())
//            toggleGoBack(duration).play();
//    }
//
//    private void replay() {
//        Duration duration = transitionDuration.multiply(0.5);
//        Animation takeSeat = toggleSeat(duration);
//        Animation showGoBack = toggleGoBack(duration);
//        ParallelTransition transition = new ParallelTransition(
//                switchFooter(duration, resultFooter, replayFooter),
//                toggleResult(duration),
//                scaleBoard(duration),
//                takeSeat, showGoBack);
//
//        takeSeat.setDelay(duration);
//        showGoBack.setDelay(duration);
//        transition.play();
//    }
//
//    private void resign(ActionEvent event) {
//        if (resignDialog == null) {
//            Label label = new Label("你确定认输吗？");
//            GridPane.setHalignment(label, HPos.CENTER);
//
//            Button cancelButton = new Button("取消");
//            cancelButton.setOnAction(e -> resignDialog.close());
//
//            Button okButton = new Button("确定");
//            okButton.getStyleClass().add("highlight");
//            okButton.setOnAction(e -> {
//                resignDialog.close();
//                endGame(-1);
//            });
//
//            GridPane.setColumnSpan(label, 2);
//            GridPane.setRowIndex(cancelButton, 1);
//            GridPane.setRowIndex(okButton, 1);
//            GridPane.setColumnIndex(okButton, 1);
//
//            GridPane grid = new GridPane(32, 64);
//            grid.setAlignment(Pos.CENTER);
//            grid.getChildren().addAll(label, cancelButton, okButton);
//
//            resignDialog = new Dialog(boardView.getScene().getWindow(), "认输");
//            resignDialog.getChildren().add(grid);
//        }
//        resignDialog.showAndWait();
//    }
//
//    private void retract()
//    {
//        boardView.takeBack();
//    }
//
//
//
//    @NotNull
//    private Animation scaleBoard(Duration duration) {
//        ScaleTransition scaleBoard = new ScaleTransition(duration, boardView);
//        TranslateTransition moveBoard = new TranslateTransition(duration, boardView);
//        ParallelTransition transition = new ParallelTransition(scaleBoard, moveBoard);
//
//        if (boardView.getScaleX() < 0.9) {
//            scaleBoard.setToX(1);
//            scaleBoard.setToY(1);
//            moveBoard.setToY(0);
//        }
//        else {
//            scaleBoard.setByX(-0.3);
//            scaleBoard.setByY(-0.3);
//            moveBoard.setByY(boardView.getHeight() * 0.15);
//        }
//        return transition;
//    }
//
//    @NotNull
//    private Animation toggleBoard(Duration duration) {
//        TranslateTransition boardMove = new TranslateTransition(duration, boardView);
//        TranslateTransition footerMove = new TranslateTransition(duration, footer);
//        ParallelTransition transition = new ParallelTransition(boardMove, footerMove);
//
//        double boardDelta = boardView.localToScene(boardView.getBoundsInLocal()).getMaxX();
//        double footerDelta = footer.getHeight();
//        if (!boardView.isVisible()) {
//            boardView.setTranslateY(-boardDelta);
//            boardView.setVisible(true);
//            footer.setTranslateY(footerDelta);
//            footer.setVisible(true);
//            boardMove.setToY(0);
//            footerMove.setToY(0);
//        }
//        else {
//            boardMove.setByY(-boardDelta);
//            footerMove.setByY(footerDelta);
//            transition.setOnFinished(e -> {
//                boardView.clear();
//                boardView.setScaleX(1);
//                boardView.setScaleY(1);
//                boardView.setVisible(false);
//                footer.setVisible(false);
//            });
//        }
//        return transition;
//    }
//
//    @NotNull
//    private Animation toggleSeat(Duration duration) {
//        TranslateTransition aiMove = new TranslateTransition(duration, aiBox);
//        TranslateTransition playerMove = new TranslateTransition(duration, playerBox);
//        ParallelTransition transition = new ParallelTransition(aiMove, playerMove);
//
//        double delta = aiBox.localToScene(aiBox.getBoundsInLocal()).getMaxX();
//        if (!aiBox.isVisible()) {
//            aiBox.setTranslateX(-delta);
//            playerBox.setTranslateX(delta);
//            aiBox.setVisible(true);
//            playerBox.setVisible(true);
//            aiMove.setByX(delta);
//            playerMove.setByX(-delta);
//        }
//        else {
//            aiMove.setByX(-delta);
//            playerMove.setByX(delta);
//            transition.setOnFinished(e -> {
//                aiBox.setVisible(false);
//                playerBox.setVisible(false);
//                aiBox.setTranslateX(0);
//                playerBox.setTranslateX(0);
//            });
//        }
//        return transition;
//    }
//
//    @NotNull
//    private Animation toggleResult(Duration duration) {
//        FadeTransition fade = new FadeTransition(duration, resultView);
//        if (!resultView.isVisible()) {
//            ScaleTransition scale = new ScaleTransition(duration, resultView);
//            ParallelTransition transition = new ParallelTransition(fade, scale);
//
//            fade.setByValue(1);
//            scale.setByX(1);
//            scale.setByY(1);
//            resultView.setScaleX(0);
//            resultView.setScaleY(0);
//            resultView.setOpacity(0);
//            resultView.setVisible(true);
//            return transition;
//        }
//
//        fade.setToValue(0);
//        fade.setOnFinished(e -> resultView.setVisible(false));
//        return fade;
//    }
//
//    @NotNull
//    private Animation toggleGoBack(Duration duration) {
//        TranslateTransition transition = new TranslateTransition(duration, goBackButton);
//        double delta = goBackButton.localToScene(goBackButton.getBoundsInLocal()).getMaxX();
//        if (!goBackButton.isVisible()) {
//            goBackButton.setTranslateX(-delta);
//            goBackButton.setVisible(true);
//            transition.setToX(0);
//        }
//        else {
//            transition.setByX(-delta);
//            transition.setOnFinished(e -> goBackButton.setVisible(false));
//        }
//        return transition;
//    }
//
//    @NotNull
//    private Animation switchFooter(Duration duration, Node from, Node to) {
//        ScaleTransition hide = new ScaleTransition(duration, from);
//        ScaleTransition show = new ScaleTransition(duration, to);
//        SequentialTransition transition = new SequentialTransition(hide, show);
//
//        show.setByY(1);
//        hide.setByY(-1);
//        hide.setOnFinished(e -> {
//            from.setVisible(false);
//            to.setScaleY(0);
//            to.setVisible(true);
//        });
//        return transition;
//    }
//
//    public void save()
//    {
//        if(isGuest())
//        {
//            showError("存档失败","游客模式不能保存");
//            return;
//        }
//
//        //JAVAFX的文件选择器
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("保存游戏");
//        //后缀名保存为.xiangqi
//        fileChooser.getExtensionFilters().add(
//                new FileChooser.ExtensionFilter("存档文件(*.xiangqi)","*.xiangqi")
//        );
//
//        String username = getUsername();
//        //最初文件名为用户名
//        fileChooser.setInitialFileName(username);
//
//        File file = fileChooser.showSaveDialog(window.getScene().getWindow());
//        //防止空文件报错
//        if(file==null)
//        {
//            return;
//        }
//        try
//        {
//            Board thatBoard = boardView.getGameBoard();
//            Data data = new Data();
//            data.isRedFirst = thatBoard.isRedFirst();
//            data.turn = thatBoard.getTurn();
//            data.history = new ArrayList<>(thatBoard.record.history);
//            data.username = username;
//
//            try(ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file)))
//            {
//                output.writeObject(data);
//            }
//            System.out.println("游戏保存到了"+file.getAbsolutePath());
//        }catch(IOException e)
//        {
//            System.out.println("保存游戏失败"+e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//
//    public void load()
//    {
//        if(isGuest())
//        {
//            showError("读取失败","游客模式不能读取");
//            return;
//        }
//
//
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("加载游戏");
//        //后缀名
//        fileChooser.getExtensionFilters().add(
//                new FileChooser.ExtensionFilter("象棋存档文件 (*.xiangqi)", "*.xiangqi")
//        );
//
//        File file = fileChooser.showOpenDialog(window.getScene().getWindow());
//        if(file == null)
//        {
//            showError("警告","未选择文件");
//            return ;
//        }
//
//        try
//        {
//
//            Data data;
//            try(ObjectInputStream input = new ObjectInputStream(new FileInputStream(file)))
//            {
//                data = (Data) input.readObject();
//            }
//            catch (FileNotFoundException e)
//            {
//                showError("存档损坏","文件不存在");
//                System.out.println("报错成功，可以继续游戏");
//                return;
//            }
//            catch (ClassNotFoundException e)
//            {
//                showError("存档损坏","类结构损坏");
//                System.out.println("报错成功，可以继续游戏");
//                return;
//            }
//            catch (StreamCorruptedException e)
//            {
//                showError("存档损坏","流错误");
//                System.out.println("报错成功，可以继续游戏");
//                return;
//            }
//            catch (IOException e)
//            {
//                showError("加载出错","读取错误");
//                System.out.println("报错成功，可以继续游戏");
//                return;
//            }
//
//            if(!isValidData(data))
//            {
//                return;
//            }
//
//            String username = getUsername();
//            if(!username.equals(data.username))
//            {
//                showError("加载失败","不能加载其他用户的存档");
//                return;
//            }
//
//            Board thatBoard = boardView.getGameBoard();
//
//            thatBoard.reset();
//
//            //先手方
//            thatBoard.setSide(data.isRedFirst);
//            System.out.println("读取当前先手方"+data.isRedFirst);
//            //局数
//            thatBoard.setTurn(data.turn);
//            System.out.println("读取当前轮数："+data.turn);
//
//            //history的导入
//            thatBoard.record.history.clear();
//            thatBoard.record.history.addAll(data.history);
//
//            //这个方法也非常方便，直接把棋盘状态设置为当前history的ArrayList最上面的这一个
//            thatBoard.record.syncToLastHistory();
//
//
//            //恢复方法
//            restore(thatBoard,data.history);
//
//            //折磨了好几天的BUG
//            //就是这里！！！！！
//            //重新同步一次防止重置时错乱
//            //因为读取存档后没有紧接着的proceed方法，会导致在下一次走棋时pieceMoving重新被设置一遍，导致本应该走的一方被轮空至默认方
//            //进而导致默认方走两次再轮到后手方，因为后手方本来走的那一次被轮空至默认方（也就是先手方）了
//            thatBoard.aSwitch.forceproceed();
//            thatBoard.draw();
//
//
//            System.out.println("已成功从"+file.getAbsolutePath()+"导入");
//
//        }catch(Exception e)
//        {
//            showError("加载失败","未知原因加载失败");
//            System.out.println("报错成功，可以继续游戏");
//            return;
//        }
//
//
//    }
//
//    //判断封装类data内部数据是否合法
//    private boolean isValidData(Data data)
//    {
//        if (data == null)
//        {
//            showError("存档损坏", "数据为空");
//            System.out.println("报错成功，可以继续游戏");
//            return false;
//        }
//        if(data.history==null)
//        {
//            showError("存档出错","走棋记录为空");
//            System.out.println("报错成功，可以继续游戏");
//        }
//        if(data.turn<=0)
//        {
//            showError("存档损坏","轮数为非正数");
//            System.out.println("报错成功，可以继续游戏");
//        }
//        for(int[][] simpleBoard : data.history)
//        {
//            if(simpleBoard==null || simpleBoard.length!=10 || simpleBoard[0].length != 9)
//            {
//                showError("存档损坏","保存非法的棋盘状态");
//                System.out.println("报错成功，可以继续游戏");
//            }
//        }
//        return true;
//    }
//
//
//    //所有的信息封装内部类
//    private static class Data implements Serializable
//    {
//        boolean isRedFirst;
//        int turn;
//        ArrayList<int[][]> history;
//        String username;
//    }
//
//    //读取恢复方法
//    private void restore(Board board,ArrayList<int[][]> history)
//    {
//        if(history.isEmpty())
//        {
//            return;
//        }
//
//        int[][] latest = history.get(history.size()-1);
//        board.pieces.clear();
//
//        //bmj注：
//        //使用simpleBoard中的编码创建棋子
//        //用到了当初留下的convertToPiece方法
//        //宝贵的遗产
//        for(int row=0;row<10;row++)
//        {
//            for(int col=0;col<9;col++)
//            {
//                int code = latest[row][col];
//
//                Piece piece = simpleBoard.convertToPiece(code,row+1,col+1,board);
//
//                if(piece != null)
//                {
//                    board.pieces.add(piece);
//                }
//            }
//        }
//    }
//
//    private void showError(String title,String error)
//    {
//        //JAVAFX的警告提示
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle(title);
//        alert.setContentText(error);
//        alert.showAndWait();
//    }
//
//    private boolean isGuest()
//    {
//        String username = getUsername();
//        if(username == null)
//        {
//            System.out.println("是游客");
//            return true;
//        }
//        if(username.trim().isEmpty())
//        {
//            System.out.println("是游客");
//            return true;
//        }
//        System.out.println("不是游客");
//        return false;
//    }
//
//    //从保存的property文件里面读取登陆的用户（我加了前缀）
//    private String getUsername()
//    {
//        try
//        {
//            InputStream input = new FileInputStream("chess.properties");
//            Properties temp = new Properties();
//            temp.load(input);
//            return temp.getProperty("written_User","");
//
//        }catch (IOException e)
//        {
//            showError("错误","检验用户名错误");
//            e.printStackTrace();
//            return "";
//        }
//    }
//
//}
//
//
//
