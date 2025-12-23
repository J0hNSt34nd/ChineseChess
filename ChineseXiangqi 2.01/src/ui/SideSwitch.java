package ui;

import Pieces.Piece;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.Objects;

public class SideSwitch extends StackPane {
    private static final Paint offFill = Color.web("#865D31");
    private static final Paint onFill = Color.WHITE;
    private final PieceLabel redLabel = new PieceLabel("红棋", "/General/general_red.png");
    private final PieceLabel blackLabel = new PieceLabel("黑棋", "/General/general_black.png");
    private final ImageView handleFace = new ImageView();
    private final TranslateTransition animation = new TranslateTransition(Duration.millis(300));

    private final BooleanProperty isBlackProperty = new SimpleBooleanProperty(true);

    public SideSwitch() {
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setRadius(15.0);
        innerShadow.setOffsetY(2.0);
        innerShadow.setColor(Color.web("#452707b0"));

        Rectangle background = new Rectangle();
        background.widthProperty().bind(prefWidthProperty());
        background.heightProperty().bind(prefWidthProperty().multiply(0.15));
        background.arcHeightProperty().bind(heightProperty().multiply(0.4));
        background.arcWidthProperty().bind(heightProperty().multiply(0.4));
        background.setFill(Color.web("#FFEFCE"));
        background.setEffect(innerShadow);

        InputStream stream = Objects.requireNonNull(getClass().getResourceAsStream("/side_face.png"));
        handleFace.setImage(new Image(stream));

        handleFace.fitWidthProperty().bind(prefWidthProperty().multiply(0.5));
        handleFace.setPreserveRatio(true);
        handleFace.setScaleX(0.95);
        handleFace.setScaleY(0.95);

        redLabel.maxWidthProperty().bind(widthProperty().multiply(0.5));
        redLabel.maxHeightProperty().bind(background.heightProperty());

        blackLabel.maxWidthProperty().bind(widthProperty().multiply(0.5));
        blackLabel.maxHeightProperty().bind(background.heightProperty());

        background.setMouseTransparent(true);
        redLabel.setMouseTransparent(true);
        blackLabel.setMouseTransparent(true);

        setAlignment(handleFace, Pos.CENTER_LEFT);
        setAlignment(redLabel, Pos.CENTER_LEFT);
        setAlignment(blackLabel, Pos.CENTER_RIGHT);
        getChildren().addAll(background, handleFace, redLabel, blackLabel);


        animation.setNode(handleFace);
        animation.setOnFinished(e -> updateFill());

        updateFill();
        setOnMouseClicked(this::onClick);
        widthProperty().addListener((obs, oldVal, width) -> updateLayout());
    }

    public BooleanProperty isBlackProperty() {
        return isBlackProperty;
    }

    public boolean getIsBlack() {return  isBlackProperty.get(); }
    public void setIsBlack(boolean value) {
        if (isBlackProperty.get() == value || animation.getStatus() == Animation.Status.RUNNING)
            return;

        animation.setByX(value ? getWidth() / 2 : -getWidth() / 2);
        animation.play();

        isBlackProperty.set(value);
        System.out.println("Side changed to Black: " + value);
    }

    private void updateLayout() {
        if (isBlackProperty.get())
            handleFace.setTranslateX(getWidth() / 2);
        else
            handleFace.setTranslateX(0);
    }

    private void updateFill() {
        if (isBlackProperty.get()) {
            redLabel.setFill(offFill);
            blackLabel.setFill(onFill);
        } else {
            redLabel.setFill(onFill);
            blackLabel.setFill(offFill);
        }
    }

    private void onClick(MouseEvent event) {
        System.out.println(isBlackProperty);
        setIsBlack(event.getX() > getWidth() / 2);
    }
}
