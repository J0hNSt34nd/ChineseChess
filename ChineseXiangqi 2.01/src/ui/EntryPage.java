package ui;

import javafx.animation.ParallelTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import main.Game;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EntryPage extends Pane {
    private final Popup avatarPopup;
    private final Node avatarButton;
    private final Node localButton;
    private final Node networkButton;

    private final Avatar userAvatar = new Avatar(Game.getUserAvatarImage());
    private final TextField userNameField = new TextField(Game.getUserName());

    private EventHandler<ActionEvent> onLocalMode;
    private EventHandler<ActionEvent> onNetworkMode;

    public EntryPage() {
        setBackground(new Background(new BackgroundImage(new Image(String.valueOf(getClass().getResource(
                "/entrance_background.png"))),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                new BackgroundSize(1, 1, true, true, false, false))));

        avatarPopup = createAvatarPopup();
        avatarButton = createAvatarButton();

        localButton = createEntryButton("/local_button.png", () -> {
            Game.setUserName(userNameField.getText());
            if (onLocalMode != null) onLocalMode.handle(new ActionEvent());
        });
        networkButton = createEntryButton("/network_button.png", () -> {
            Game.setUserName(userNameField.getText());
            if (onNetworkMode != null) onNetworkMode.handle(new ActionEvent());
        });

        HBox entryBox = new HBox(16, localButton, networkButton);
        entryBox.setAlignment(Pos.CENTER);
        entryBox.prefWidthProperty().bind(widthProperty());
        entryBox.prefHeightProperty().bind(heightProperty());

        getChildren().addAll(entryBox, avatarButton);
    }

    public void setOnLocalMode(EventHandler<ActionEvent> handler) { onLocalMode = handler; }
    public void setOnNetworkMode(EventHandler<ActionEvent> handler) { onNetworkMode = handler; }

    public void toggleVisible() {
        avatarButton.setVisible(isVisible());
        localButton.setVisible(isVisible());
        networkButton.setVisible(isVisible());

        GameTransitions.play(isVisible(),
                GameTransitions.fade(1, this, e -> userNameField.selectEnd()),
                new ParallelTransition(
                        GameTransitions.move(0.5, -1, 0, avatarButton),
                        GameTransitions.move(0.5, -1, 0, localButton),
                        GameTransitions.move(0.5, 1, 0, networkButton)
                ));
    }

    private Popup createAvatarPopup() {
        double gap = 8;
        GridPane grid = new GridPane(gap, gap);
        grid.setPadding(new Insets(gap));
        grid.setMouseTransparent(true);
        grid.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#FBE5C5")), new Stop(1, Color.web("#E5C493"))),
                new CornerRadii(gap), Insets.EMPTY)));
        grid.setBorder(new Border(new BorderStroke(Color.web("#D8AC71"), BorderStrokeStyle.SOLID,
                new CornerRadii(gap), new BorderWidths(2))));
        grid.addRow(0,
                new Avatar(Game.getAvatarImage("female_1.png")),
                new Avatar(Game.getAvatarImage("female_2.png")),
                new Avatar(Game.getAvatarImage("female_3.png")),
                new Avatar(Game.getAvatarImage("female_4.png")));
        grid.addRow(1,
                new Avatar(Game.getAvatarImage("male_1.png")),
                new Avatar(Game.getAvatarImage("male_2.png")),
                new Avatar(Game.getAvatarImage("male_3.png")),
                new Avatar(Game.getAvatarImage("male_4.png")));
        grid.addRow(2,
                new Avatar(Game.getAvatarImage("male_5.png")),
                new Avatar(Game.getAvatarImage("male_6.png")),
                new Avatar(Game.getAvatarImage("male_7.png")),
                new Avatar(Game.getAvatarImage("female_5.png")));

        StackPane pane = new StackPane(grid);
        pane.setPadding(new Insets(gap * 2));
        pane.setBackground(new Background(new BackgroundFill(Color.web("#FFEFCE"),
                new CornerRadii(gap * 2), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(Color.web("#CEA874"), BorderStrokeStyle.SOLID,
                new CornerRadii(gap * 2), new BorderWidths(1))));
        pane.setEffect(new DropShadow(24, 0, 4, Color.web("#47310E", 0.6)));
        pane.setOnMouseMoved(e -> {
            double x = e.getX() - gap * 2;
            double y = e.getY() - gap * 2;
            int col = (int) Math.floor(grid.getColumnCount() * x / grid.getWidth());
            int row = (int) Math.floor(grid.getRowCount() * y / grid.getHeight());
            Node oldNode = (Node) grid.getUserData();
            Node newNode = null;
            for (Node node : grid.getChildren()) {
                if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                    newNode = node;
                    break;
                }
            }
            if (newNode == oldNode)
                return;
            if (oldNode != null) {
                oldNode.setScaleX(1);
                oldNode.setScaleY(1);
            }
            if (newNode != null) {
                newNode.setScaleX(1.1);
                newNode.setScaleY(1.1);
            }
            grid.setUserData(newNode);
        });
        pane.setOnMousePressed(e -> {
            Node node = (Node) grid.getUserData();
            if (node != null) {
                node.setScaleX(0.95);
                node.setScaleY(0.95);
            }
        });
        pane.setOnMouseReleased(e -> {
            Node node = (Node) grid.getUserData();
            if (node != null && pane.isHover()) {
                node.setScaleX(1.1);
                node.setScaleY(1.1);
            }
        });
        pane.setOnMouseClicked(e -> {
            Avatar avatar = (Avatar) grid.getUserData();
            if (avatar != null) {
                Game.setUserAvatarImage(avatar.getImage());
                userAvatar.setImage(avatar.getImage());
            }
        });

        Popup popup = new Popup();
        popup.getContent().add(pane);
        popup.setAutoHide(true);
        popup.setOpacity(0);
        return popup;
    }

    private Node createAvatarButton() {
        userAvatar.radiusProperty().bind(heightProperty().multiply(0.075));

        Path board = new Path();
        Image image = new Image(String.valueOf(getClass().getResource("/avatar_label_fill.png")));
        board.setFill(new ImagePattern(image, 0, 0, image.getWidth() * 0.5, image.getHeight() * 0.5, false));
        board.setStroke(Color.web("#704516"));
        board.strokeWidthProperty().bind(heightProperty().multiply(0.01));

        userNameField.setBackground(null);
        userNameField.setStyle("-fx-text-fill: #552E03;");

        Text date = new Text(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/d")));
        date.setFill(Color.web("#B56711"));

        Line line = new Line();
        line.setStroke(Color.web("#C98C48"));
        line.setStrokeWidth(2);
        line.setManaged(false);
        line.setEffect(new InnerShadow(1, 0, 1, Color.web("#402405")));

        userAvatar.heightProperty().addListener((obs, old, height) -> {
            double w = height.doubleValue() * 1.8;
            double h = height.doubleValue() * 0.6;
            double r1 = h * 0.55;
            double r2 = h * 0.45;
            board.setLayoutX(height.doubleValue() * 0.75);
            board.setLayoutY((height.doubleValue() - h) * 0.5);
            board.getElements().setAll(
                    new MoveTo(0, 0),
                    new LineTo(w, 0),
                    new ArcTo(r1, r1, 90, w - r1, r1, false, true),
                    new ArcTo(r2, r2, 90, w - h, h, false, true),
                    new LineTo(0, h),
                    new ClosePath()
            );

            double x = height.doubleValue() * 1.1;
            userNameField.setFont(Font.font(null, FontWeight.BOLD, h * 0.25));
            userNameField.setLayoutX(x);
            userNameField.setLayoutY(board.getLayoutY() + h * 0.05);

            date.setFont(Font.font(null, FontWeight.NORMAL, h * 0.2));
            date.setLayoutX(height.doubleValue() * 1.1);
            date.setLayoutY(board.getLayoutY() + h * 0.6);
            date.setTextOrigin(VPos.TOP);

            line.setStartX(x);
            line.setStartY(height.doubleValue() * 0.51);
            line.setEndX(board.getLayoutX() + w - r1 - r2 * 0.5);
            line.setEndY(line.getStartY());
            line.getStrokeDashArray().setAll(1.0, 8.0, line.getEndX() - line.getStartX() - 18, 8.0);
        });

        Pane button = new Pane(board, userAvatar, line, userNameField, date);
        button.layoutXProperty().bind(heightProperty().multiply(0.03));
        button.layoutYProperty().bind(heightProperty().multiply(0.03));

        Effect shadow = new DropShadow(8, 0, 2, Color.web("#000", 0.5));
        userAvatar.setOnMouseEntered(e -> {
            userAvatar.setScaleX(1.02);
            userAvatar.setScaleY(1.02);
            userAvatar.setEffect(shadow);
        });
        userAvatar.setOnMouseExited(e -> {
            userAvatar.setScaleX(1);
            userAvatar.setScaleY(1);
            userAvatar.setEffect(null);
        });
        userAvatar.setOnMousePressed(e -> {
            userAvatar.setScaleX(0.98);
            userAvatar.setScaleY(0.98);
            userAvatar.setEffect(null);
        });
        userAvatar.setOnMouseReleased(e -> {
            if (userAvatar.isHover()) {
                userAvatar.setScaleX(1.02);
                userAvatar.setScaleY(1.02);
                userAvatar.setEffect(shadow);
            }
        });
        userAvatar.setOnMouseClicked(e -> {
            if (avatarPopup.isShowing()) {
                avatarPopup.hide();
                return;
            }
            if (avatarPopup.getOpacity() < 0.9) {
                avatarPopup.show(avatarButton, 0, 0);
                avatarPopup.hide();
                avatarPopup.setOpacity(1);
            }
            Point2D point = avatarButton.localToScreen(0, 0);
            avatarPopup.show(avatarButton, point.getX() - 32, point.getY() + avatarButton.getBoundsInLocal().getHeight() - 24);
        });
        return button;
    }

    private Node createEntryButton(String imageFile, Runnable action) {
        ImageView imageView = new ImageView(new Image(String.valueOf(getClass().getResource(imageFile))));
        imageView.fitWidthProperty().bind(widthProperty().multiply(0.34));
        imageView.setPreserveRatio(true);

        Paint normalFill = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FFC189")), new Stop(1, Color.web("#D07320")));
        Paint enterFill = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FFDE81")), new Stop(1, Color.web("#768D25")));
        Effect shadow = new DropShadow(16, 0, 4, Color.web("#000", 0.5));
        Path background = new Path();
        background.setStrokeWidth(0);
        background.setFill(normalFill);
        background.setEffect(shadow);
        background.setManaged(false);

        imageView.boundsInParentProperty().addListener((obs, old, bounds) -> {
            double w = bounds.getWidth();
            double h = bounds.getHeight();
            double r = h * 0.04;
            background.setLayoutX(bounds.getMinX());
            background.setLayoutY(bounds.getMinY());
            ShapeHelper.setConcaveRectangle(background, w, h, r);
        });

        Node button = new StackPane(background, imageView);
        imageView.setOnMouseEntered(e -> {
            background.setFill(enterFill);
            button.setScaleX(1.02);
            button.setScaleY(1.02);
        });
        imageView.setOnMouseExited(e -> {
            background.setFill(normalFill);
            button.setScaleX(1);
            button.setScaleY(1);
        });
        imageView.setOnMousePressed(e -> {
            background.setEffect(null);
            button.setScaleX(0.98);
            button.setScaleY(0.98);
        });
        imageView.setOnMouseReleased(e -> {
            background.setEffect(shadow);
            if (imageView.isHover()) {
                button.setScaleX(1.02);
                button.setScaleY(1.02);
            }
        });
        imageView.setOnMouseClicked(e -> action.run());

        return button;
    }
}
