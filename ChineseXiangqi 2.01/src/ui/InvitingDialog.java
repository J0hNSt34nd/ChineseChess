package ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Window;

public class InvitingDialog extends Dialog {
    private final Avatar userAvatar = new Avatar();
    private final Label userLabel = new Label();
    private final Avatar invitingAvatar = new Avatar();
    private final Label invitingLabel = new Label();
    private final RoundButton cancelButton = new RoundButton("取消");

    public InvitingDialog(Window window) {
        super(window, "");

        Paint fill = Color.web("#552E03");
        Label title = new Label("已发出邀请！");
        title.setFont(Font.font(null, FontWeight.BOLD, 20));
        title.setTextFill(fill);

        Font font = Font.font(null, FontWeight.NORMAL, 16);
        userLabel.setFont(font);
        userLabel.setTextFill(fill);
        invitingLabel.setFont(font);
        invitingLabel.setTextFill(fill);

        userAvatar.setRadius(24);
        invitingAvatar.setRadius(24);

        VBox userBox = new VBox(8, userAvatar, userLabel);
        userBox.setAlignment(Pos.CENTER);
        VBox invitingBox = new VBox(8, invitingAvatar, invitingLabel);
        invitingBox.setAlignment(Pos.CENTER);

        Text vs = new Text("VS");
        vs.setFont(Font.font(null, FontWeight.BOLD, 32));
        vs.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.1, Color.web("#D54B10")),
                new Stop(0.5, Color.web("#FF7A52")),
                new Stop(0.9, Color.web("#EA7A34"))));

        HBox content = new HBox(16, userBox, vs, invitingBox);
        content.setAlignment(Pos.CENTER);

        cancelButton.setPadding(new Insets(8, 40, 8, 40));

        VBox layout = new VBox(16, title, content, cancelButton);
        layout.setAlignment(Pos.CENTER);

        getChildren().add(layout);
    }

    public void setUserAvatar(Image image) { userAvatar.setImage(image); }
    public void setUserName(String name) { userLabel.setText(name); }
    public void setInvitingAvatar(Image image) { invitingAvatar.setImage(image); }
    public void setInvitingName(String name) { invitingLabel.setText(name); }
    public void setOnCancel(EventHandler<ActionEvent> onCancel) { cancelButton.setOnAction(onCancel); }
}