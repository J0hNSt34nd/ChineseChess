package ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import main.Board;
import main.Game;
import Tool.Network;
//import PieceType;

import java.util.Base64;

public class NetworkPage extends ModePage {
    private static final String UNCONNECTED = "未连接到服务器";
    private static final String CONNECTING = "正在连接服务器";

    private final Network network = Game.getNetwork();

    private final Path serverBackground = new Path();
    private final GridPane serverLayout = new GridPane();
    private final Label hostLabel = new Label("服务器地址");
    private final Label portLabel = new Label("服务器端口");
    private final TextField hostField = createTextField(Game.getHostName());
    private final TextField portField = createTextField(String.valueOf(Game.getHostPort()));
    private final RoundButton connectButton = new RoundButton("连接");
    private final Label message = new Label(UNCONNECTED);
    private final ListView<Network.Client> listView = new ListView<>();

    private InvitingDialog invitingDialog;
    private InvitedDialog invitedDialog;

    private EventHandler<ActionEvent> onStart;

    public NetworkPage() {
        network.setOnInvited(this::onInvited);
        network.setOnRejected(this::onRejected);
        network.setOnAccepted(this::onAccepted);
        network.setOnError(() -> message.setText(network.getError()));
        network.status.addListener((_, _, status) -> onStatusChanged(status));

        serverBackground.setFill(Color.web("#FFEFCE"));
        serverBackground.setStroke(Color.web("#BF9B6E"));
        serverBackground.setEffect(new InnerShadow(4, 0, 1, Color.web("#2A1600")));

        hostLabel.setTextFill(Color.web("#9A5F2D"));
        portLabel.setTextFill(Color.web("#9A5F2D"));

        connectButton.setFill(Color.web("#D38D52"));
        connectButton.setTextFill(Color.web("#FFE5C9"));
        connectButton.setOnAction(_ -> {
            if (network.getStatus() == Network.Status.UNCONNECTED)
                Game.connectToHost(hostField.getText(), Integer.parseInt(portField.getText()));
            else if (network.getStatus() == Network.Status.CONNECTED)
                network.disconnect();
        });

        message.setTextFill(Color.web("#552E03"));
        message.setTextAlignment(TextAlignment.CENTER);
        message.setWrapText(true);
        StackPane.setAlignment(message, Pos.CENTER);

        GridPane.setRowIndex(portLabel, 1);
        GridPane.setRowIndex(portField, 1);
        GridPane.setColumnIndex(portField, 1);
        GridPane.setColumnIndex(hostField, 1);
        GridPane.setRowIndex(connectButton, 2);
        GridPane.setColumnIndex(connectButton, 1);
        GridPane.setHalignment(connectButton, HPos.CENTER);
        serverLayout.getChildren().addAll(hostLabel, portLabel, hostField, portField, connectButton);

        String cssContent =
                ".list-view .scroll-bar:vertical," +
                        ".list-view .scroll-bar:horizontal {" +
                        "    -fx-background-color: transparent;" +
                        "    -fx-pref-width: 0;" +
                        "    -fx-max-width: 0;" +
                        "    -fx-pref-height: 0;" +
                        "    -fx-max-height: 0;" +
                        "    -fx-opacity: 0;" +
                        "    -fx-padding: 0;" +
                        "}";
        String cssUrl = "data:text/css;base64," + Base64.getEncoder().encodeToString(cssContent.getBytes());

        listView.setBackground(null);
        listView.getStylesheets().add(cssUrl);
        listView.setCellFactory(_ -> new ClientListCell(this::invite));
        listView.setItems(network.clients);

        addContents(new StackPane(serverBackground, serverLayout), new StackPane(message, listView));
    }

    public void setOnStart(EventHandler<ActionEvent> onStart) { this.onStart = onStart; }

    private static TextField createTextField(String text) {
        CornerRadii corner = new CornerRadii(4);
        BorderWidths widths = new BorderWidths(1);
        TextField textField = new TextField(text);
        textField.setStyle("-fx-text-fill: #9A5F2D;");
        textField.setBackground(new Background(new BackgroundFill(Color.web("#E9D6BA"), corner, Insets.EMPTY)));
        textField.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, corner, widths)));
        textField.focusedProperty().addListener((_, _, focused) -> {
            if (focused)
                textField.setBorder(new Border(new BorderStroke(Color.web("#D38D52"), BorderStrokeStyle.SOLID, corner, widths)));
            else
                textField.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, corner, widths)));
        });
        return textField;
    }

    @Override
    protected void updateLayout() {
        super.updateLayout();

        double w = getContentWidth();
        double gap = w * 0.08;

        serverLayout.setHgap(gap);
        serverLayout.setVgap(gap * 0.3);

        Font font = Font.font(null, FontWeight.BOLD, w * 0.04);
        hostLabel.setFont(font);
        portLabel.setFont(font);
        hostField.setFont(font);
        portField.setFont(font);
        GridPane.setHgrow(hostField, Priority.ALWAYS);

        message.setFont(font);
        connectButton.setFont(font);
        connectButton.setPadding(new Insets(gap * 0.2, gap * 2, gap * 0.2, gap * 2));

        double h = w * 0.4;
        double r = h * 0.1;
        StackPane.setMargin(serverLayout, new Insets(r * 2, r, r * 2, r));
        ShapeHelper.setConcaveRectangle(serverBackground, w, h, r);

        h = getContentHeight() - serverBackground.getBoundsInLocal().getHeight() - gap;
        listView.setPrefSize(w, h);
        listView.setMaxSize(w, h);
        listView.setMinSize(w, h);
    }

    @Override
    protected void onGoBack(ActionEvent e) {
        network.disconnect();
        super.onGoBack(e);
    }

    private void invite(String id) {
        if (invitingDialog == null)
            invitingDialog = new InvitingDialog(getScene().getWindow());

        invitingDialog.setUserAvatar(Game.getUserAvatarImage());
        invitingDialog.setUserName(Game.getUserName());
        Network.Client client = network.getClient(id);
        if (client != null) {
            invitingDialog.setInvitingAvatar(Game.getAvatarImage(client.profile().getAvatar()));
            invitingDialog.setInvitingName(client.profile().getName());
        }
        invitingDialog.setOnCancel(_ -> {
            invitingDialog.close();
            network.rejectInvite(id);
        });
        network.invite(id);
        invitingDialog.showAndWait();
    }

    private void onStatusChanged(Network.Status status) {
        connectButton.setDisable(false);
        if (status == Network.Status.CONNECTED) {
            connectButton.setText("断开");
            connectButton.setTextFill(Color.web("#AA0D08"));
            message.setVisible(false);
            String name = Game.getUserName();
            String avatar = "level_1.png";
            System.out.println("连接成功，准备发送登录请求...");

            try {
                network.login(name, avatar);
                System.out.println("已发送登录指令: " + name);
            } catch (Exception e) {
                System.err.println("发送登录指令时出错: ");
                e.printStackTrace();
            }
        }
        else {
            connectButton.setText("连接");
            if (network.getError() == null) {
                connectButton.setTextFill(Color.web("#FFE5C9"));
                if (status == Network.Status.CONNECTING) {
                    connectButton.setDisable(true);
                    message.setText(CONNECTING);
                }
                else
                    message.setText(UNCONNECTED);
            }
            listView.getItems().clear();
            message.setVisible(true);
        }
    }

    private void onInvited(Network.Client info) {
        if (invitedDialog == null)
            invitedDialog = new InvitedDialog(getScene().getWindow());

        invitedDialog.setInviterName(info.profile().getName());
        invitedDialog.setOnReject(_ -> {
            invitedDialog.close();
            network.rejectInvite(info.id());
        });
        invitedDialog.setOnAccept(e -> {
            Game.setOpponent(info.profile());
            Game.setNetworkMode(true);
            Game.setisRed(false);
            invitedDialog.close();
            network.acceptInvite(info.id());
            if (onStart != null)
                onStart.handle(e);
        });
        invitedDialog.showAndWait();
    }

    private void onRejected(Network.Client info) {
        if (invitingDialog != null)
            invitingDialog.close();
        if (invitedDialog != null)
            invitedDialog.close();
    }

    private void onAccepted(Network.Client info) {
        if (invitingDialog != null)
            invitingDialog.close();
        Game.setOpponent(info.profile());
        Game.setisRed(true);
        if (onStart != null) {
            onStart.handle(new ActionEvent());
        }
    }
}
