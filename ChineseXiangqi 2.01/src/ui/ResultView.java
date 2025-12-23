package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


public class ResultView extends VBox {
    private final Avatar aiAvatar = new Avatar();
    private final NameLabel aiLabel = new NameLabel();
    private final Avatar playerAvatar = new Avatar();
    private final NameLabel playerLabel = new NameLabel();
    private final ImageView imageView = new ImageView();
    private final Label totalLabel = new Label();

    public ResultView() {
        aiAvatar.radiusProperty().bind(prefHeightProperty().multiply(0.3));
        playerAvatar.radiusProperty().bind(prefHeightProperty().multiply(0.3));
        aiLabel.prefWidthProperty().bind(prefHeightProperty().multiply(1));
        playerLabel.prefWidthProperty().bind(prefHeightProperty().multiply(1));
        imageView.fitHeightProperty().bind(prefHeightProperty().multiply(0.8));
        imageView.setPreserveRatio(true);
        imageView.setEffect(new DropShadow(16, 0, 4, Color.web("#513013", 0.6)));

        HBox title = new HBox(
                new HBox(8, aiAvatar, aiLabel), imageView, new HBox(8, playerAvatar, playerLabel)
        );
        title.setAlignment(Pos.CENTER);
        title.spacingProperty().bind(prefHeightProperty().multiply(0.5));

        Stop[] stops = new Stop[] {
                new Stop(0, Color.web("#FFFBF2")),
                new Stop(1, Color.web("#EFB20A")),
        };
        totalLabel.setTextFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops));
        totalLabel.setEffect(new DropShadow(2, 0, 2, Color.web("#3D300B", 0.6)));

        setVisible(false);
        setAlignment(Pos.TOP_CENTER);
        getChildren().addAll(title, totalLabel);

        prefHeightProperty().addListener(observable -> totalLabel.setFont(Font.font(null, FontWeight.BOLD, getPrefHeight() * 0.2)));
    }

    public void setAiIcon(Image image) { aiAvatar.setImage(image); }
    public void setAiName(String name) { aiLabel.setText(name); }
    public void setPlayerIcon(Image image) { playerAvatar.setImage(image); }
    public void setPlayerName(String name) { playerLabel.setText(name);}
    public void setResult(int result) {
        if (result < 0)
            imageView.setImage(new Image(String.valueOf(getClass().getResource("/lose.png"))));
        else if (result > 0) {
            imageView.setImage(new Image(String.valueOf(getClass().getResource("/win.png"))));

        }
    }
    public void setTotal(int win, int lose, int draw) {
        totalLabel.setText(String.format("胜 %d    和 %d    负 %d", win, draw, lose));
    }
}
