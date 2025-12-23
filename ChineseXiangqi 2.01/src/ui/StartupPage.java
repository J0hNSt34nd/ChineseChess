package ui;

import javafx.animation.ParallelTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class StartupPage extends StackPane {
    private final StackPane logoPane;
    private final Node startButton = createStartButton();
    private EventHandler<ActionEvent> onStart;

    public StartupPage() {
        ImageView background = new ImageView(new Image(String.valueOf(getClass().getResource(
                "/startup_background.png"))));
        background.setBlendMode(BlendMode.SCREEN);
        background.setOpacity(0.4);
        background.fitWidthProperty().bind(widthProperty());
        background.fitHeightProperty().bind(heightProperty());
        background.setManaged(false);

        Circle light = new Circle(0, 0, 10, new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FFE09E", 0.5)), new Stop(0.64, Color.TRANSPARENT)));
        light.radiusProperty().bind(heightProperty().divide(2));
        light.layoutXProperty().bind(widthProperty().divide(2));
        light.layoutYProperty().bind(heightProperty().multiply(0.55));
        light.setEffect(new GaussianBlur(400));
        light.setManaged(false);

        ImageView logo = new ImageView(new Image(String.valueOf(getClass().getResource(
                "/logo.png"))));
        logo.fitHeightProperty().bind(heightProperty().multiply(0.4));
        logo.setPreserveRatio(true);

        ImageView seal = new ImageView(new Image(String.valueOf(getClass().getResource(
                "/seal.png"))));
        seal.fitHeightProperty().bind(heightProperty().multiply(0.15));
        seal.setPreserveRatio(true);
        seal.translateXProperty().bind(heightProperty().multiply(0.1));
        seal.translateYProperty().bind(heightProperty().multiply(0.11));

        ImageView sealText = new ImageView(new Image(String.valueOf(getClass().getResource(
                "/seal_text.png"))));
        sealText.fitHeightProperty().bind(heightProperty().multiply(0.1));
        sealText.setPreserveRatio(true);
        sealText.translateXProperty().bind(heightProperty().multiply(0.1));
        sealText.translateYProperty().bind(heightProperty().multiply(0.11));

        logoPane = new StackPane(logo, seal, sealText);

        VBox content = new VBox(logoPane, startButton);
        content.spacingProperty().bind(heightProperty().multiply(0.05));
        content.setAlignment(Pos.CENTER);

        getChildren().addAll(background, light,
                createHalo(0.4, 2),
                createHalo(0.34, 25),
                createHalo(0.2, 15),
                content);
    }

    public void setOnStart(EventHandler<ActionEvent> handler) { onStart = handler; }

    public void toggleVisible() {
        logoPane.setVisible(isVisible());
        startButton.setVisible(isVisible());
        startButton.setDisable(isVisible());

        GameTransitions.play(isVisible(),
                GameTransitions.fade(0.5, this),
                new ParallelTransition(
                        GameTransitions.zoom(0.5, 20, logoPane),
                        GameTransitions.fade(0.5, logoPane),
                        GameTransitions.fade(0.5, startButton)
                ));
    }

    private Circle createHalo(double ratio, double width) {
        Circle halo = new Circle(0, 0, 0, null);
        halo.setStrokeWidth(width);
        halo.setStroke(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FFEBC0", 0.9)), new Stop(0.64, Color.TRANSPARENT)));
        halo.radiusProperty().bind(heightProperty().multiply(ratio));
        halo.layoutXProperty().bind(widthProperty().divide(2));
        halo.layoutYProperty().bind(heightProperty().multiply(0.6));
        halo.setManaged(false);
        return halo;
    }

    private Node createStartButton() {
        Paint normalFill = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#E88017")), new Stop(1, Color.web("#893000")));
        Paint pressedFill = Color.web("#E88017").interpolate(Color.web("#893000"), 0.5);

        Path border = new Path();
        ShapeHelper.setConcaveRectangle(border, 200, 60, 10);
        border.setFill(normalFill);
        border.setStroke(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#F1C650")), new Stop(0.5, Color.web("#FFF6CC")), new Stop(1, Color.web("#E0B13D"))));

        Text text = new Text("开 始 游 戏");
        text.setFill(Color.WHITE);
        text.setFont(Font.font(null, FontWeight.BOLD, 20));

        StackPane button = new StackPane(border, text);

        button.setOnMousePressed(e -> {
            border.setFill(pressedFill);
            button.setScaleX(0.96);
            button.setScaleY(0.96);
        });
        button.setOnMouseReleased(e -> {
            border.setFill(normalFill);
            button.setScaleX(1);
            button.setScaleY(1);
        });
        button.setOnMouseClicked(e -> {
            if (onStart != null) onStart.handle(new ActionEvent());
        });
        return button;
    }
}
