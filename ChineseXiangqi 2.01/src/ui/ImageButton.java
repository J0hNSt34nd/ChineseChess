package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class ImageButton extends Button {
    private final ImageView upView = new ImageView();
    private final ImageView downView = new ImageView();

    public ImageButton(String upImagePath, String downImagePath) {
        upView.setEffect(new DropShadow(12, 0, 4, Color.web("#000", 0.4)));
        upView.fitWidthProperty().bind(prefWidthProperty());
        upView.fitHeightProperty().bind(prefHeightProperty());
        upView.setPreserveRatio(true);

        downView.fitWidthProperty().bind(prefWidthProperty());
        downView.fitHeightProperty().bind(prefHeightProperty());
        downView.setPreserveRatio(true);
        downView.setVisible(false);

        upView.setImage(new Image(String.valueOf(getClass().getResource(upImagePath))));
        downView.setImage(new Image(String.valueOf(getClass().getResource(downImagePath))));

        StackPane pane = new StackPane();
        StackPane.setAlignment(downView, Pos.BOTTOM_CENTER);
        pane.getChildren().addAll(upView, downView);

        setGraphic(pane);
        setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        pressedProperty().addListener((obs, up, down) -> {
            upView.setVisible(up);
            upView.setVisible(down);
        });
    }
}
