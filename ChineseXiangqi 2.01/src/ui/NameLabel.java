package ui;

import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class NameLabel extends StackPane {
    private final Label label = new Label();
    private final Rectangle background = new Rectangle();
    private final Polygon border = new Polygon();

    public NameLabel() {
        initialize();
    }

    public NameLabel(String text) {
        label.setText(text);
        initialize();
    }

    public StringProperty textProperty() { return label.textProperty(); }

    public String getText() { return label.getText(); }
    public void setText(String text) { label.setText(text); }

    public Font getFont() { return label.getFont(); }
    public void setFont(Font font) { label.setFont(font);}

    private void initialize() {
        background.setStrokeWidth(1);
        border.setStrokeWidth(2);
        background.setFill(null);
        border.setFill(null);
        background.setStroke(Color.web("#8E5210"));
        border.setStroke(Color.web("#8E5210"));
        label.setTextFill(Color.web("#552E03"));
        label.setFont(Font.font(null, FontWeight.BOLD, 20));
        background.widthProperty().bind(prefWidthProperty());
        background.heightProperty().bind(label.heightProperty());

        setAlignment(Pos.CENTER);
        getChildren().addAll(background, border, label);

        background.widthProperty().addListener((obs, oldVal, newVal) -> updateBorder());
        background.heightProperty().addListener((obs, oldVal, newVal) -> updateBorder());
    }

    private void updateBorder() {
        double w = background.getWidth();
        double h = background.getHeight();
        double r = h * 0.1;
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
