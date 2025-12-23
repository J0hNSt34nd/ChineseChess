package ui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;

public class ResignDialog extends Dialog {
    public ResignDialog(Window window, String title, Runnable resign) {
        super(window, title);

        Label label = new Label("你确定认输吗？");
        label.setTextFill(Color.web("#552E03"));
        label.setFont(Font.font(null, FontWeight.BOLD, 16));
        GridPane.setHalignment(label, HPos.CENTER);

        Insets padding = new Insets(8, 48, 8, 48);
        RoundButton cancelButton = new RoundButton("取消");
        cancelButton.setPadding(padding);
        cancelButton.setOnAction(_ -> close());

        RoundButton okButton = new RoundButton("确定");
        okButton.setPadding(padding);
        okButton.setFill(Color.web("#8F3B3B"));
        okButton.setOnAction(_ -> {
            close();
            resign.run();
        });

        GridPane.setColumnSpan(label, 2);
        GridPane.setRowIndex(cancelButton, 1);
        GridPane.setRowIndex(okButton, 1);
        GridPane.setColumnIndex(okButton, 1);

        GridPane grid = new GridPane(32, 64);
        grid.setAlignment(Pos.CENTER);
        grid.getChildren().addAll(label, cancelButton, okButton);
        getChildren().add(grid);
    }
}

