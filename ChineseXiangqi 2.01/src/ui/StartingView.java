package ui;

import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;


public class StartingView extends Pane {
    private final Title title = new Title();
    private final ImageView leftDoor = new ImageView();
    private final ImageView rightDoor = new ImageView();
    private final ImageView scrollPainting = new ImageView();
    private final VBox content = new VBox();
    private final LocalPage aiPage = new LocalPage();
    private final NetworkPage networkPage = new NetworkPage();

    private final ToolButton aiButton = new ToolButton();
    private final ToolButton networkButton = new ToolButton();
    private final Button startButton = new ImageButton(
            "/org/imed/chinesechess/images/start_up.png",
            "/org/imed/chinesechess/images/start_down.png");

    public StartingView() {
        leftDoor.setImage(new Image(getResource("desk.png")));
        rightDoor.setImage(new Image(getResource("desk.png")));
        rightDoor.setScaleX(-1);

        aiButton.setDisable(true);
        aiButton.setIconSource("/org/imed/chinesechess/images/computer.svg");
        networkButton.setIconSource("/org/imed/chinesechess/images/human.svg");

        aiButton.setOnAction(e -> {
            networkPage.setVisible(false);
            networkButton.setDisable(false);
            aiPage.setVisible(true);
            aiButton.setDisable(true);
        });
        networkButton.setOnAction(e -> {
            aiPage.setVisible(false);
            aiButton.setDisable(false);
            networkPage.setVisible(true);
            networkButton.setDisable(true);
        });

        scrollPainting.setImage(new Image(getResource("starting_background.png")));
        scrollPainting.setPreserveRatio(true);

        ImageView separator2 = new ImageView();
        separator2.setImage(new Image(getResource("separator.png")));
        separator2.setPreserveRatio(true);
        separator2.fitWidthProperty().bind(content.prefWidthProperty().multiply(0.95));

        content.setAlignment(Pos.TOP_CENTER);
        content.getChildren().addAll(new StackPane(aiPage, networkPage), separator2, startButton);

        getChildren().addAll(leftDoor, rightDoor, title, aiButton, networkButton, scrollPainting, content);

        widthProperty().addListener((obs, oldVal, newVal) -> updateLayout());
        heightProperty().addListener((obs, oldVal, newVal) -> updateLayout());
    }

    public void toggleVisible(Duration duration) {
        double w = getWidth()*0.5;
        double h = getHeight();
        Duration d = duration.multiply(0.5);

        TranslateTransition moveLeft = new TranslateTransition(d, leftDoor);
        TranslateTransition moveRight = new TranslateTransition(d, rightDoor);
        TranslateTransition moveTitle = new TranslateTransition(d, title);
        TranslateTransition movePainting = new TranslateTransition(d, scrollPainting);
        TranslateTransition moveContent = new TranslateTransition(d, content);
        ParallelTransition pt = new ParallelTransition(moveLeft, moveRight, moveTitle, movePainting, moveContent);

        if (!isVisible()) {
            leftDoor.setTranslateX(-w); rightDoor.setTranslateX(w);
            title.setTranslateY(-h); scrollPainting.setTranslateY(-h); content.setTranslateY(-h);
            moveTitle.setDelay(d); movePainting.setDelay(d); moveContent.setDelay(d);
            setVisible(true);
        }
        else {
            w = -w; h = -h;
            moveLeft.setDelay(d); moveRight.setDelay(d);
            pt.setOnFinished(e -> setVisible(false));
        }
        moveLeft.setByX(w); moveRight.setByX(-w);
        moveTitle.setByY(h); movePainting.setByY(h); moveContent.setByY(h);

        pt.play();
    }

    public void setOnStart(EventHandler<ActionEvent> onStart) { startButton.setOnAction(onStart); }

    public boolean isNetworkMode() { return networkPage.isVisible(); }
    public LocalPage getLocalPage() { return aiPage; }
    public NetworkPage getNetworkPage() { return networkPage; }

    private String getResource(String name) {
        return String.valueOf(getClass().getResource("/org/imed/chinesechess/images/" + name));
    }

    private void updateLayout() {
        double w = getWidth();
        double h = getHeight();

        leftDoor.setFitWidth(w/2);
        leftDoor.setFitHeight(h);
        rightDoor.setFitWidth(w/2);
        rightDoor.setFitHeight(h);
        rightDoor.setLayoutX(w/2);

        double titleY = 32;
        double titleH = h * 0.08;
        title.setPrefWidth(w);
        title.setPrefHeight(titleH);
        title.setLayoutY(titleY);

        scrollPainting.setFitWidth(w);
        scrollPainting.setFitHeight(h - titleY - titleH);
        Bounds bounds = scrollPainting.getBoundsInLocal();
        double x = (w - bounds.getWidth())/2;
        scrollPainting.setLayoutX(x);
        scrollPainting.setLayoutY(h - bounds.getHeight());

        double iconSize = titleH * 0.5;
        aiButton.setIconSize(iconSize);
        networkButton.setIconSize(iconSize);
        aiButton.setFill(Color.web("#CFAA72"));
        networkButton.setFill(Color.web("#CFAA72"));
        double y = titleY + (titleH - iconSize) * 0.6;
        x *= 1.2;
        aiButton.setLayoutX(x);
        aiButton.setLayoutY(y);
        networkButton.setLayoutX(w - x - iconSize);
        networkButton.setLayoutY(y);

        double contentW = bounds.getWidth() * 0.78;
        double contentH = bounds.getHeight() * 0.845;
        content.setSpacing(contentH * 0.05);
        content.setPrefWidth(contentW);
        content.setMinWidth(contentW);
        content.setMaxWidth(contentW);
        content.setLayoutX((w - contentW) / 2);
        content.setLayoutY(h - contentH);

        aiPage.setPrefWidth(contentW);

        networkPage.setPrefWidth(contentW);

        startButton.setPrefWidth(contentW * 0.3);
    }
}