package main;

import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Button;
import javafx.scene.image.Image;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameToolBar extends StackPane {

    private Runnable onMenu;
    private Runnable onUndo;

    public GameToolBar() {

        Rectangle bgRect = new Rectangle();
        bgRect.setWidth(400);
        bgRect.setHeight(60);
        bgRect.setArcWidth(30);
        bgRect.setArcHeight(30);
        bgRect.setFill(Color.rgb(105 , 63, 16, 0.5));

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnMenu = createStyleButton("/button/Menu.png");
        Button btnUndo = createStyleButton("/button/Undo.png");

        btnMenu.setOnAction(e -> {
            if (onMenu != null)
                onMenu.run();
        });

        btnUndo.setOnAction(e -> {
            if (onUndo != null)
                onUndo.run();
        });

        buttonBox.getChildren().addAll(btnMenu, btnUndo);

        bgRect.widthProperty().bind(buttonBox.widthProperty().add(60));

        this.getChildren().addAll(bgRect, buttonBox);

        buttonBox.setMaxSize(1320, 119);
    }

    private Button createStyleButton(String imagePath) {
        Button btn = new Button();

        try{
            Image img = new Image(getClass().getResourceAsStream(imagePath));
            ImageView iconView = new ImageView(img);

            iconView.setFitWidth(50);
            iconView.setFitHeight(50);
            iconView.setPreserveRatio(true);
            iconView.setSmooth(true);

            btn.setGraphic(iconView);
        } catch (Exception e) {
            System.err.println("无法加载图标");
        }
        String normalStyle =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-content-display: LEFT;"; // 图片在左，文字在右

        String hoverStyle =
                "-fx-background-color: rgba(255,255,255,0.3);" + // 背景变亮
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-content-display: LEFT;";

        btn.setStyle(normalStyle);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(hoverStyle);

            btn.setCursor(javafx.scene.Cursor.HAND);
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(normalStyle);
            btn.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        return btn;
    }

    public void setOnMenu(Runnable action) {
        this.onMenu = action;
    }

    public void setOnUndo(Runnable action) {
        this.onUndo = action;
    }
}
