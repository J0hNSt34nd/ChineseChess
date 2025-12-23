package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class WindowButtonBox extends StackPane {
    public WindowButtonBox() {
        double height = 32;
        double width = height * 4;
        Rectangle background = new Rectangle(width, height);
        background.setArcWidth(height);
        background.setArcHeight(height);
        background.setStroke(Color.web("#FFF", 0.6));
        background.setFill(Color.web("#000", 0.16));
        background.setEffect(new InnerShadow(2, 0, 1, Color.web("#381E09", 0.8)));

        ToolButton min = createButton("/min.svg");
        ToolButton max = createButton("/max.svg");
        ToolButton close = createButton("/close.svg");
        min.setOnAction(e -> ((Stage) getScene().getWindow()).setIconified(true));
        max.setOnAction(e -> toggleMaximize());
        close.setOnAction(e -> Platform.exit());
        HBox layout = new HBox(min, max, close);
        layout.setAlignment(Pos.CENTER);

        getChildren().addAll(background, layout);
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);

        StackPane.setAlignment(this, Pos.TOP_RIGHT);
        StackPane.setMargin(this, new Insets(height / 2, height / 2, 0, 0));
    }

    private ToolButton createButton(String fileName) {
        ToolButton button = new ToolButton();
        button.setDisplay(ToolButton.Display.IconOnly);
        button.setFill(Color.web("#FFEFCE"));
        button.setIconSource(fileName);
        button.setIconEffect(null);
        return button;
    }

    private void toggleMaximize() {
        Stage stage = (Stage) getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }
}
