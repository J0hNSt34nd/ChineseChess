package ui;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.Objects;

public class LevelSlider extends StackPane {
    private static final String[] LEVELS = {"菜鸟", "入门", "棋士", "大师", "棋圣"};
    private final StackPane handle = new StackPane();
    private final ImageView handleFace = new ImageView();
    private final Label handleLabel = new Label();
    private final TranslateTransition animation = new TranslateTransition(Duration.millis(300));
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    public LevelSlider() {
        ImageView background = new ImageView(new Image(String.valueOf(getClass().getResource("/level_slider.png"))));
        background.fitWidthProperty().bind(prefWidthProperty());
        background.setPreserveRatio(true);

        InputStream stream = Objects.requireNonNull(getClass().getResourceAsStream("/level_face.png"));
        handleFace.setImage(new Image(stream));
        handleFace.setPreserveRatio(true);
        handleFace.setScaleX(1.1);

        handleLabel.setAlignment(Pos.CENTER);
        handleLabel.setTextFill(Color.web("#7E490E"));
        handleLabel.setText(LEVELS[value.get()]);
        handleLabel.setEffect(new InnerShadow(3.0, 1, 2, Color.web("#000", 0.5)));

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetY(1.0);
        dropShadow.setColor(Color.web("#482E0F"));

        handle.setEffect(dropShadow);
        handle.setAlignment(Pos.CENTER_LEFT);
        handle.getChildren().addAll(handleFace, handleLabel);

        animation.setNode(handle);
        animation.setOnFinished(e -> {
            handleLabel.setText(LEVELS[value.get()]);
        });

        getChildren().addAll(background, handle);
        setOnMouseClicked(this::onClick);

        widthProperty().addListener((obs, oldVal, width) -> updateLayout(width.doubleValue()));
    }

    public int getValue() { return value.get(); }
    public void setValue(int value) {
        if (animation.getStatus() == Animation.Status.RUNNING || value < 0 || value >= LEVELS.length || this.value.get() == value)
            return;

        animation.setByX((value - this.value.get()) * getWidth() / LEVELS.length);
        animation.play();

        this.value.set(value);
    }
    public IntegerProperty valueProperty() { return value; }

    public String getText() { return handleLabel.getText(); }
    public StringProperty textProperty() {
        return handleLabel.textProperty();
    }

    private void updateLayout(double width) {
        width /= LEVELS.length;
        handleFace.setFitWidth(width);
        handleLabel.setPrefWidth(width);
        handle.setTranslateX(width * value.get());

        Bounds bounds = handleFace.getLayoutBounds();
        handleLabel.setFont(Font.font(null, FontWeight.BOLD, bounds.getHeight() * 0.5));
    }

    private void onClick(MouseEvent event) {
        setValue((int) Math.floor(LEVELS.length * event.getX() / getWidth()));
    }
}
