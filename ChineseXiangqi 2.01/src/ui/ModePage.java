package ui;

import javafx.animation.ParallelTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ModePage extends Pane {
    private final Title title = new Title();
    private final ImageView leftDoor = new ImageView();
    private final ImageView rightDoor = new ImageView();
    private final ImageView scrollPainting = new ImageView();
    private final VBox content = new VBox();
    private final ToolButton goBackButton = new ToolButton();

    private EventHandler<ActionEvent> onGoBack;

    public ModePage() {
        leftDoor.setImage(new Image(getResource("/desk.png")));
        rightDoor.setImage(new Image(getResource("/desk.png")));
        rightDoor.setScaleX(-1);

        scrollPainting.setImage(new Image(getResource("/starting_background.png")));
        scrollPainting.setPreserveRatio(true);

        content.setAlignment(Pos.TOP_CENTER);

        goBackButton.setText("返回");
        goBackButton.setIconSource("/return.svg");
        goBackButton.setDisplay(ToolButton.Display.TextBesideIcon);
        goBackButton.setPrefSize(96, 24);
        goBackButton.setMinSize(96, 24);
        goBackButton.setMaxSize(96, 24);
        goBackButton.setLayoutX(16);
        goBackButton.setLayoutY(20);
        goBackButton.setIconSize(20);
        goBackButton.setFill(Color.web("#FFE9C7"));
        goBackButton.setOnAction(this::onGoBack);

        getChildren().addAll(leftDoor, rightDoor, title, scrollPainting, content, goBackButton);

        widthProperty().addListener((obs, oldVal, newVal) -> updateLayout());
        heightProperty().addListener((obs, oldVal, newVal) -> updateLayout());
    }

    public void setOnGoBack(EventHandler<ActionEvent> handler) { onGoBack = handler; }

    public void toggleVisible() {
        for (Node node : getChildren())
            node.setVisible(isVisible());

        GameTransitions.play(isVisible(),
                GameTransitions.fade(0.5, this),
                new ParallelTransition(
                        GameTransitions.move(0.5, -1, 0, leftDoor),
                        GameTransitions.move(0.5, 1, 0, rightDoor)),
                new ParallelTransition(
                        GameTransitions.move(0.5, 0, -1, goBackButton),
                        GameTransitions.move(0.5, 0, -1, title),
                        GameTransitions.move(0.5, 0, -1, scrollPainting),
                        GameTransitions.move(0.5, 0, -1, content)));
    }

    private String getResource(String name) {
        return String.valueOf(getClass().getResource(name));
    }

    protected void addContents(Node... contents) { content.getChildren().addAll(contents); }

    protected double getContentWidth() { return content.getPrefWidth(); }
    protected double getContentHeight() { return content.getPrefHeight(); }
    protected void setContentSpacing(double gap) { content.setSpacing(gap); }

    protected void updateLayout() {
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
        scrollPainting.setLayoutX((w - bounds.getWidth())/2);
        scrollPainting.setLayoutY(h - bounds.getHeight());

        double contentW = bounds.getWidth() * 0.75;
        double contentH = bounds.getHeight() * 0.845;
        content.setPrefSize(contentW, contentH);
        content.setMinSize(contentW, contentH);
        content.setMaxSize(contentW, contentH);
        content.setLayoutX((w - contentW) / 2);
        content.setLayoutY(h - contentH);
    }

    protected void onGoBack(ActionEvent e) {
        if (onGoBack != null)
            onGoBack.handle(e);
    }
}
