package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import main.Game;
import Tool.Network;

import java.util.function.Consumer;

public class ClientListCell extends ListCell<Network.Client> {
    private final Consumer<String> onInvite;

    public ClientListCell(Consumer<String> onInvite) {
        this.onInvite = onInvite;
    }

    @Override
    protected void updateItem(Network.Client client, boolean empty) {
        super.updateItem(client, empty);
        super.setBackground(null);
        super.setPadding(Insets.EMPTY);

        if (empty || client == null) {
            setText(null);
            setGraphic(null);
        }
        else {
            ListView<Network.Client> listView = getListView();
            double w = listView.getWidth() - 4;
            double h = 64;

            StackPane root = new StackPane();

            Path path = new Path();
            path.setFill(Color.web("#FFEFCE"));
            path.setStroke(Color.web("#BF9B6E"));
            ShapeHelper.setConcaveRectangle(path, w, h, 8);

            Rectangle rect = new Rectangle(3, 3, w - 6, h - 6);
            rect.setStroke(Color.web("#BF9B6E"));
            rect.setFill(null);

            listView.widthProperty().addListener((_, _, width) -> {
                double w1 = width.doubleValue() - 4;
                ShapeHelper.setConcaveRectangle(path, w1, h, 8);
                rect.setWidth(w1 - 6);
                rect.setHeight(h - 6);
            });

            Font font = Font.font(null, FontWeight.BOLD, 16);
            Label nameLabel = new Label(client.profile().getName());
            nameLabel.setFont(font);
            nameLabel.setTextFill(Color.web("#552E03"));
            nameLabel.setTextAlignment(TextAlignment.CENTER);

            Avatar avatar = new Avatar(Game.getAvatarImage(client.profile().getAvatar()));
            avatar.setRadius(24);

            Pane space = new Pane();
            HBox.setHgrow(space, Priority.ALWAYS);

            Effect shadow = new InnerShadow(2, 0, 1, Color.web("#2A1600"));
            SVGPath stateIcon = new SVGPath();
            stateIcon.setEffect(shadow);
            stateIcon.setScaleX(0.75);
            stateIcon.setScaleY(0.75);

            Text stateText = new Text();
            stateText.setFont(font);
            stateText.setEffect(shadow);
            if (!client.playing()) {
                stateText.setText("空闲中");
                stateText.setFill(Color.web("#40AF29"));
                stateIcon.setFill(Color.web("#40AF29"));
                stateIcon.setContent(SvgUtil.readPathData("/idle.svg"));
            }
            else {
                stateText.setText("对战中");
                stateText.setFill(Color.web("#EA5D58"));
                stateIcon.setFill(Color.web("#EA5D58"));
                stateIcon.setContent(SvgUtil.readPathData("/playing.svg"));
            }

            HBox stateBox = new HBox(stateIcon, stateText);
            stateBox.setAlignment(Pos.CENTER);

            RoundButton button = new RoundButton("邀请");
            button.setFill(Color.web("#D38D52"));
            button.setTextFill(Color.web("#FFE5C9"));
            button.setPadding(new Insets(4, 16, 4, 16));
            button.setOnAction(_ -> onInvite.accept(client.id()));

            HBox layout = new HBox(16, avatar, nameLabel, space, stateBox, button);
            layout.setPadding(new Insets(8, 16, 8, 16));
            layout.setAlignment(Pos.CENTER_LEFT);
            root.getChildren().addAll(path, rect, layout);

            setText(null);
            setGraphic(root);
        }
    }
}
