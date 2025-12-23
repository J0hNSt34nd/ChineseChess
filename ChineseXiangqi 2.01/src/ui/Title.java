package ui;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class Title extends StackPane {
    private final Rectangle background = new Rectangle();
    private final Polygon border = new Polygon();

    public Title() {
        Stop[] stops = {
                new Stop(0.0, Color.web("#F1C650")),
                new Stop(0.5, Color.web("#FFEAD2")),
                new Stop(1.0, Color.web("#E0B13D"))
        };
        LinearGradient stroke = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);

        background.setStrokeWidth(1);
        border.setStrokeWidth(4);
        background.setFill(null);
        border.setFill(null);

        background.setStroke(stroke);
        border.setStroke(stroke);

        ImageView imageView = new ImageView(new Image(String.valueOf(getClass().getResource("/title.png"))));
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(prefWidthProperty());
        imageView.fitHeightProperty().bind(prefHeightProperty());

        setAlignment(Pos.CENTER);
        getChildren().addAll(background, border, imageView);

        imageView.boundsInLocalProperty().addListener((observable, oldValue, newValue) -> updateBorder(newValue));
    }

    private void updateBorder(Bounds bounds) {
        double w = bounds.getWidth() * 1.1;
        double h = bounds.getHeight();
        double r = h * 0.1;

        background.setWidth(w);
        background.setHeight(h);
        border.getPoints().setAll(new Double[]{
                -r, r,
                r, r,
                r, -r,

                w-r, -r,
                w-r, r,
                w+r, r,

                w+r, h-r,
                w-r, h-r,
                w-r, h+r,

                r, h+r,
                r, h-r,
                -r, h-r
        });
    }
}

