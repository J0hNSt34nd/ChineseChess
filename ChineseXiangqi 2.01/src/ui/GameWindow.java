package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import main.Game;

public class GameWindow extends StackPane {
    private double xOffset = 0;
    private double yOffset = 0;
    private static final double WINDOW_CORNER_SIZE = 32;
    private static final double WINDOW_SHADOW_SIZE = 20;
    private static final Effect WINDOW_SHADOW = new DropShadow(WINDOW_SHADOW_SIZE, 0, 4, Color.web("#000", 0.8));

    private final Rectangle rootClip = new Rectangle();
    private final StartupPage startupPage = new StartupPage();
    private final EntryPage entryPage = new EntryPage();
    private final LocalPage localPage = new LocalPage();
    private final NetworkPage networkPage = new NetworkPage();
    private final GamePage gamePage = new GamePage();

    public GameWindow(Stage stage) {
        StackPane rootPane = new StackPane();
        rootClip.widthProperty().bind(rootPane.widthProperty());
        rootClip.heightProperty().bind(rootPane.heightProperty());
        rootPane.setClip(rootClip);
        rootPane.setBackground(new Background(new BackgroundFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#D49364")),
                new Stop(0.39, Color.web("#C57C46")),
                new Stop(1, Color.web("#91552A"))),
                new CornerRadii(0), Insets.EMPTY)));

        rootPane.getChildren().addAll(gamePage, entryPage, localPage, networkPage, startupPage, new WindowButtonBox());
        getChildren().add(rootPane);

        setBackground(Background.EMPTY);
        updateWindow(stage.isFullScreen());

        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    if (!startupPage.isVisible()) {
                        startupPage.toggleVisible();
                    }
                });
            }
        });

        stage.fullScreenProperty().addListener((_, _, fullScreen) ->
                updateWindow(fullScreen));

        startupPage.setVisible(false);
        entryPage.setVisible(false);
        localPage.setVisible(false);
        networkPage.setVisible(false);
        gamePage.setVisible(false);

        this.setOnMousePressed(event -> {
            if (!stage.isFullScreen()) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        this.setOnMouseDragged(event -> {
            if (!stage.isFullScreen()) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        startupPage.setOnStart(_ -> {
            startupPage.toggleVisible();
            entryPage.toggleVisible();
        });
        entryPage.setOnLocalMode(_ -> {
            Game.setNetworkMode(false);
            entryPage.toggleVisible();
            localPage.toggleVisible();
        });
        entryPage.setOnNetworkMode(_ -> {
            Game.setNetworkMode(true);
            entryPage.toggleVisible();
            networkPage.toggleVisible();
        });
        localPage.setOnStart(_ -> {
            boolean isBlack = localPage.isBlackSide();
            gamePage.setBlackSide(isBlack);
            localPage.toggleVisible();
            gamePage.toggleVisible();
        });
        localPage.setOnGoBack(_ -> {
            localPage.toggleVisible();
            entryPage.toggleVisible();
        });
        networkPage.setOnStart(_ -> {
            networkPage.toggleVisible();
            gamePage.toggleVisible();
        });
        networkPage.setOnGoBack(_ -> {
            networkPage.toggleVisible();
            entryPage.toggleVisible();
        });
        gamePage.setOnGoBack(_ -> {
            gamePage.toggleVisible();
            if (Game.isNetworkMode())
                networkPage.toggleVisible();
            else
                localPage.toggleVisible();
        });
    }

    private void updateWindow(boolean fullScreen) {
        if (fullScreen) {
            rootClip.setArcWidth(0);
            rootClip.setArcHeight(0);
            setPadding(Insets.EMPTY);
            setEffect(null);
        }
        else {
            rootClip.setArcWidth(WINDOW_CORNER_SIZE);
            rootClip.setArcHeight(WINDOW_CORNER_SIZE);
            setPadding(new Insets(WINDOW_SHADOW_SIZE));
            setEffect(WINDOW_SHADOW);
        }
    }
}
