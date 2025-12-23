package ui;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Window;
import main.Game;
import Tool.PlayerRecord;

public class PlayerRecordDialog extends Dialog {
    public PlayerRecordDialog(Window window) {
        super(window, "");

        ToolButton closeButton = new ToolButton();
        closeButton.setIconSource("/close.svg");
        closeButton.setFill(Color.web("#DA5F5F"));
        closeButton.setDisplay(ToolButton.Display.IconOnly);
        closeButton.setOnAction(_ -> close());

        HBox header = new HBox(closeButton);
        header.setAlignment(Pos.TOP_RIGHT);

        Rectangle border = new Rectangle(200, 50);
        border.setArcWidth(12);
        border.setArcHeight(12);
        border.setStroke(Color.web("#D8AC71"));
        border.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FBE5C5", 0.4)),
                new Stop(1, Color.web("#E5C493", 0.4))));

        Text text = new Text();
        text.setFill(Color.web("#552E03"));
        text.setFont(Font.font(null, FontWeight.SEMI_BOLD, 16));

        StackPane textPane = new StackPane(border, text);
        VBox.setMargin(textPane, new Insets(40, 0, 40, 0));

        VBox layout = new VBox(header, new PopupTitle("我的战绩"), textPane);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(48));

        getChildren().add(layout);

        setOnShowing(_ -> {
            PlayerRecord record = Game.getRecord();
            int count = record.getWins() + record.getLosses() + record.getDraws();
            String ratio = count == 0 ? "······" : String.format("%.1f%%", (float) record.getWins() / count);
            ratio = ratio.replaceAll(".0%", "%");
            text.setText(String.format("与【%s】的交手记录\n\n总场次：    %d                胜%d   和%d   负%d\n\n胜   率：    %s",
                    Game.getOpponent().getName(), count, record.getWins(), record.getDraws(), record.getLosses(), ratio));
            Bounds rect = text.getBoundsInLocal();
            border.setWidth(rect.getWidth() + 64);
            border.setHeight(rect.getHeight() + 64);
        });
    }
}