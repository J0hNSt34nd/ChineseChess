package main;

import Effect.JavaFXSound;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ui.GameWindow;
import main.LoginView;
import java.util.Locale;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Locale.setDefault(Locale.CHINA);

        JavaFXSound sound = new JavaFXSound();
        sound.playLoopMusic();

        showLogin(stage);
    }

    private void showLogin(Stage stage) {
        LoginView loginView = new LoginView(stage);

        loginView.setOnLoginSuccess(() -> {
            Platform.runLater(() -> startGame(stage));
        });

        Scene loginScene = new Scene(loginView, 460, 520);
        loginScene.setFill(Color.TRANSPARENT);

        if (stage.getStyle() != StageStyle.TRANSPARENT) {
            stage.initStyle(StageStyle.TRANSPARENT);
        }

        stage.setTitle("中国象棋 - 登录");
        stage.setScene(loginScene);
        stage.centerOnScreen();
        stage.show();
    }

    private void startGame(Stage stage) {
        GameWindow gameWindow = new GameWindow(stage);

        Scene gameScene = new Scene(gameWindow, 1280, 800);
        gameScene.setFill(Color.TRANSPARENT);

        stage.setTitle("中国象棋");
        stage.setScene(gameScene);
        stage.centerOnScreen();
    }

    @Override
    public void stop() {
        if (Game.getNetwork() != null) {
            Game.getNetwork().disconnect();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}