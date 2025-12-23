package ui;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;
import main.Game;
import Tool.Network;

public class RequestDialog extends Dialog{
    private static final String RETRACT = "对方请求悔棋， 你同意吗？";
    private static final String DRAW = "对方与你议和， 你同意吗？";
    private static final String RESTART = "对方请求再战， 你同意吗？";

    private final Label label = new Label();
    private final RoundButton cancelButton = new RoundButton();
    private final Network network = Game.getNetwork();
    private Runnable onAccept;
    private Runnable onReject;

    public RequestDialog(Window window) {
        super(window, "");

        label.setFont(Font.font(null, FontWeight.BOLD, 20));
        label.setTextFill(Color.web("#552E03"));

        cancelButton.setPadding(new Insets(8, 40, 8, 40));

        VBox layout = new VBox(32, label, cancelButton);
        layout.setAlignment(Pos.CENTER);

        getChildren().add(layout);

        network.setOnReply(reply -> {
            if (reply.equals(ReplyDialog.ACCEPT)) {
                close();
                onAccept.run();
            }
            else {
                label.setText("对方拒绝了你的请求！");
                cancelButton.setText("确定");
                cancelButton.setFill(Color.web("#8F3B3B"));
                cancelButton.setOnAction(this::onReject);
            }
        });
    }

    public void request(String content, Runnable onAccept, Runnable onReject) {
        network.postRequest(content);
        label.setText("请求已发送，正在等待对方回复……");
        cancelButton.setText("取消");
        cancelButton.setFill(Color.web("#654C2B"));
        cancelButton.setOnAction(this::onCancel);
        this.onAccept = onAccept;
        this.onReject = onReject;
        showAndWait();
    }

    public void requestRetract(Runnable onAccept) {
        request(RETRACT, onAccept, null);
    }

    public void requestDraw(Runnable onAccept) {
        request(DRAW, onAccept, null);
    }

    public void requestRestart(Runnable onAccept, Runnable onReject) {
        request(RESTART, onAccept, onReject);
    }

    private void onCancel(ActionEvent e) {
        close();
        network.cancelRequest();
    }

    private void onReject(ActionEvent e) {
        close();
        if (onReject != null)
            onReject.run();
    }
}
