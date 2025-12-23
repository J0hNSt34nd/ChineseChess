package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.util.Objects;

public class PieceLabel extends HBox {
    private final Label label = new Label();

    public PieceLabel(String text, String iconFile) {
        StackPane icon = new StackPane();
        icon.setAlignment(Pos.CENTER);

        InputStream stream = Objects.requireNonNull(getClass().getResourceAsStream("/piece_face.png"));
        ImageView piece = new ImageView(new Image(stream));
        piece.fitHeightProperty().bind(maxHeightProperty().multiply(0.65));
        piece.setPreserveRatio(true);
        icon.getChildren().add(piece);

        stream = Objects.requireNonNull(getClass().getResourceAsStream(iconFile));
        piece = new ImageView(new Image(stream));
        piece.fitHeightProperty().bind(maxHeightProperty().multiply(0.5));
        piece.setPreserveRatio(true);
        icon.getChildren().add(piece);

        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setRadius(1.0);
        innerShadow.setChoke(0.0);
        innerShadow.setOffsetX(0.0);
        innerShadow.setOffsetY(1.0);
        innerShadow.setColor(Color.web("#AE6410"));

        label.setEffect(innerShadow);
        label.setText(text);

        spacingProperty().bind(piece.fitHeightProperty().multiply(0.25));
        setAlignment(Pos.CENTER);
        getChildren().addAll(icon, label);

        heightProperty().addListener((obs, oldVal, width) -> updateLayout());
    }

    public Paint getFill() { return label.getTextFill(); }
    void setFill(Paint paint) { label.setTextFill(paint); }

    private void updateLayout() {
        label.setFont(Font.font(null, FontWeight.BOLD, getHeight() * 0.4));
    }
}
