package ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;

public class InvitedDialog extends Dialog {
    private final Label inviterLabel = new Label();
    private final RoundButton rejectButton = new RoundButton("拒绝");
    private final RoundButton acceptButton = new RoundButton("接受");

    public InvitedDialog(Window window) {
        super(window, "");

        Paint fill = Color.web("#552E03");
        Label title = new Label("有人邀请你对战！");
        title.setFont(Font.font(null, FontWeight.BOLD, 20));
        title.setTextFill(fill);
        Label prefix = new Label("玩家");
        prefix.setFont(Font.font(null, FontWeight.NORMAL, 16));
        prefix.setTextFill(fill);
        Label suffix = new Label("想请你来一局联机对战！");
        suffix.setFont(Font.font(null, FontWeight.NORMAL, 16));
        suffix.setTextFill(fill);
        Label ask = new Label("是否同意？");
        ask.setFont(Font.font(null, FontWeight.NORMAL, 16));
        ask.setTextFill(fill);
        inviterLabel.setFont(Font.font(null, FontWeight.BOLD, 16));
        inviterLabel.setTextFill(fill);

        HBox content = new HBox(8, prefix, inviterLabel, suffix);
        content.setAlignment(Pos.CENTER);

        Insets padding = new Insets(8, 40, 8, 40);
        rejectButton.setPadding(padding);
        acceptButton.setPadding(padding);
        acceptButton.setFill(Color.web("#8F3B3B"));

        HBox buttonBox = new HBox(16, rejectButton, acceptButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(16, title, content, ask, buttonBox);
        layout.setAlignment(Pos.CENTER);

        getChildren().add(layout);
    }

    public void setInviterName(String name) { inviterLabel.setText(name); }
    public void setOnReject(EventHandler<ActionEvent> onReject) { rejectButton.setOnAction(onReject); }
    public void setOnAccept(EventHandler<ActionEvent> onAccept) { acceptButton.setOnAction(onAccept); }
}
