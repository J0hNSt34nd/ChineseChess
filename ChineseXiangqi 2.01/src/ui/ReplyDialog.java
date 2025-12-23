package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;
import main.Game;
import Tool.Network;

public class ReplyDialog extends Dialog {
    public static final String REJECT = "Reject";
    public static final String ACCEPT = "Accept";

    private final Label label = new Label();
    private final Network network = Game.getNetwork();

    private Runnable onAccept;
    private Runnable onReject;

    public ReplyDialog(Window window) {
        super(window, "");

        label.setFont(Font.font(null, FontWeight.BOLD, 20));
        label.setTextFill(Color.web("#552E03"));

        RoundButton rejectButton = new RoundButton("拒绝");
        RoundButton acceptButton = new RoundButton("同意");

        Insets padding = new Insets(8, 40, 8, 40);
        rejectButton.setPadding(padding);
        acceptButton.setPadding(padding);
        acceptButton.setFill(Color.web("#8F3B3B"));

        HBox buttonBox = new HBox(16, rejectButton, acceptButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(32, label, buttonBox);
        layout.setAlignment(Pos.CENTER);

        getChildren().add(layout);

        rejectButton.setOnAction(_ -> {
            network.postReply(REJECT);
            close();
            if (onReject != null) onReject.run();
        });

        acceptButton.setOnAction(_ -> {
            network.postReply(ACCEPT);
            close();
            if (onAccept != null) onAccept.run();
        });

        network.setOnCancel(this::close);
    }

    public void showRequest(String content, Runnable onAccept, Runnable onReject) {
        this.label.setText(content);
        this.onAccept = onAccept;
        this.onReject = onReject;
        this.showAndWait();
    }
}