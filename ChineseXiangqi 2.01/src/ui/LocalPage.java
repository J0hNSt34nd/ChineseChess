package ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import main.Game;
import Tool.PlayerRecord;
import org.jetbrains.annotations.NotNull;

public class LocalPage extends ModePage {
    private final GridPane infoLayout = new GridPane();
    private final ImageView infoBackground = new ImageView();
    private final Avatar aiAvatar = new Avatar();
    private final NameLabel aiLabel = new NameLabel();
    private final Label scoreLabel = new Label("获胜率");
    private final Label scoreRatio = new Label("100 %");
    private final RoundButton scoreButton = new RoundButton("我的战绩");
    private final HBox levelBox = new HBox();
    private final HBox sideBox = new HBox();
    private final ImageView levelLabel = new ImageView();
    private final ImageView sideLabel = new ImageView();
    private final LevelSlider levelSlider = new LevelSlider();
    private final SideSwitch sideSwitch = new SideSwitch();
    private final Button startButton = new ImageButton(
            "/start_up.png",
            "/start_down.png");

    private EventHandler<ActionEvent> onStart;
    private PlayerRecordDialog recordDialog;

    public LocalPage() {
        DropShadow dropShadow = new DropShadow(16, 0, 2, Color.web("#42340B60"));

        infoBackground.setImage(new Image(getResource("/info_background.png")));
        infoBackground.setPreserveRatio(true);
        infoBackground.setEffect(dropShadow);

        GridPane.setRowIndex(aiAvatar, 0);
        GridPane.setColumnIndex(aiAvatar, 0);
        GridPane.setHalignment(aiAvatar, HPos.CENTER);
        GridPane.setValignment(aiAvatar, VPos.CENTER);

        aiLabel.textProperty().bind(levelSlider.textProperty());
        GridPane.setRowIndex(aiLabel, 1);
        GridPane.setColumnIndex(aiLabel, 0);
        GridPane.setHalignment(aiLabel, HPos.CENTER);
        GridPane.setValignment(aiLabel, VPos.CENTER);

        scoreLabel.setTextFill(Color.web("#9A5F2D"));
        scoreRatio.setTextFill(Color.web("#D0822B"));
        scoreRatio.setEffect(new InnerShadow(8, 0, 4, Color.web("#381E09")));

        scoreButton.setFill(Color.web("#D38D52"));
        scoreButton.setTextFill(Color.web("#FFE5C9"));
        scoreButton.setOnAction(e -> {
            if (recordDialog == null)
                recordDialog = new PlayerRecordDialog(getScene().getWindow());
            recordDialog.showAndWait();
        });

        VBox scoreBox = new VBox();
        scoreBox.spacingProperty().bind(scoreLabel.heightProperty().multiply(0.2));
        scoreBox.getChildren().addAll(scoreLabel, scoreRatio);
        GridPane.setRowIndex(scoreBox, 0);
        GridPane.setColumnIndex(scoreBox, 1);

        GridPane.setRowIndex(scoreButton, 1);
        GridPane.setColumnIndex(scoreButton, 1);
        GridPane.setHalignment(scoreButton, HPos.CENTER);
        GridPane.setValignment(scoreButton, VPos.CENTER);

        infoLayout.getChildren().addAll(aiAvatar, aiLabel, scoreBox, scoreButton);

        StackPane infoPane = new StackPane(infoBackground, infoLayout);
        infoPane.setAlignment(Pos.CENTER);

        dropShadow = new DropShadow(4, 0, 2, Color.web("#482E0F"));

        levelLabel.setImage(new Image(getResource("/level_label.png")));
        levelLabel.setPreserveRatio(true);
        levelLabel.setEffect(dropShadow);

        levelBox.setAlignment(Pos.CENTER);
        levelBox.getChildren().addAll(levelLabel, levelSlider);

        sideLabel.setImage(new Image(getResource("/side_label.png")));
        sideLabel.setPreserveRatio(true);
        sideLabel.setEffect(dropShadow);

        sideBox.setAlignment(Pos.CENTER);
        sideBox.getChildren().addAll(sideLabel, sideSwitch);

        addContents(infoPane, levelBox, createSeparator(), sideBox, createSeparator(), startButton);

        aiAvatar.setImage(Game.getAIAvatarImage());
        levelSlider.setValue(Game.getAILevel());
//        sideSwitch.setIsBlack(Game.getUserColor() == -1);
        updateRecord();
        levelSlider.valueProperty().addListener((obs, old, level) -> {
            Game.setAILevel(level.intValue());
            aiAvatar.setImage(Game.getAIAvatarImage());
        });
        levelSlider.textProperty().addListener(observable -> updateRecord());

        startButton.setOnAction(e -> {
            if (onStart != null) {
//                Board.needAi(true);
                onStart.handle(e);
            }
        });
    }

    public void setOnStart(EventHandler<ActionEvent> onStart) { this.onStart = onStart; }

    @Override
    protected void updateLayout() {
        super.updateLayout();

        double w = getContentWidth();
        setContentSpacing(getContentHeight() * 0.05);

        infoBackground.setFitWidth(w * 0.95);
        Bounds bounds = infoBackground.getBoundsInLocal();

        double hgap = w * 0.08;
        double vgap = hgap * 0.3;
        infoLayout.setPadding(new Insets(vgap, vgap, vgap, vgap + hgap));
        infoLayout.setVgap(vgap);
        infoLayout.setHgap(hgap);

        aiAvatar.setRadius(bounds.getHeight() * 0.25);

        aiLabel.setPrefWidth(bounds.getWidth() * 0.3);
        aiLabel.setFont(Font.font(null, FontWeight.BOLD, bounds.getHeight() * 0.1));

        scoreLabel.setFont(Font.font(null, FontWeight.BOLD, bounds.getHeight() * 0.1));
        scoreRatio.setFont(Font.font(null, FontWeight.BOLD, bounds.getHeight() * 0.25));
        updateScoreButton(bounds.getHeight() * 0.09);

        levelBox.setSpacing(hgap);
        sideBox.setSpacing(hgap);

        double labelW = w * 0.175;
        double controlW = w * 0.95 - labelW - hgap;
        levelLabel.setFitWidth(labelW);
        levelSlider.setPrefWidth(controlW);

        sideLabel.setFitWidth(labelW);
        sideSwitch.setPrefWidth(controlW);

        startButton.setPrefWidth(w * 0.3);
    }

    private String getResource(String name) {
        return String.valueOf(getClass().getResource(name));
    }

    @NotNull
    private Node createSeparator() {
        ImageView separator = new ImageView();
        separator.setImage(new Image(getResource("/separator.png")));
        separator.setPreserveRatio(true);
        separator.fitWidthProperty().bind(infoLayout.widthProperty().multiply(0.95));
        return separator;
    }

    private void updateScoreButton(double fontSize) {
        scoreButton.setFont(Font.font(null, FontWeight.BOLD, fontSize));
        scoreButton.setPadding(new Insets(fontSize * 0.4, fontSize * 2, fontSize * 0.4, fontSize * 2));
    }

    private void updateRecord() {
        PlayerRecord record = Game.getRecord();
        int count = record.getWins() + record.getLosses() + record.getDraws();

        if (count > 0)
            scoreRatio.setText(String.format("%d%%", (int) Math.round((double) record.getWins()/count)));
        else
            scoreRatio.setText("······");
    }

    public boolean isBlackSide() {
        System.out.println("beidiaoyong");
        return sideSwitch.getIsBlack();
    }
}
