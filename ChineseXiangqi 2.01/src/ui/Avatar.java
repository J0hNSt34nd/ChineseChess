package ui;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.util.Duration;
import main.Game;


public class Avatar extends StackPane {
    private final Circle frame = new Circle();
    private final Circle face = new Circle();
    private final Circle clip = new Circle();
    private final ImageView icon = new ImageView();
    private final Path countdownPath = new Path();
    private final Text countdownLabel = new Text();

    private final Color countdownStartColor = Color.web("#CBFF88");
    private final Color countdownEndColor = Color.web("#E22");

    private Timeline timeline;
    private ScaleTransition heartbeatAnimation;
    private final DoubleProperty timeSeconds = new SimpleDoubleProperty(0); // 当前剩余时间
    private double maxSeconds;

    private EventHandler<ActionEvent> onCountdownEnd;

    public Avatar() {
        initialize();
    }

    public Avatar(Image image) {
        setImage(image);
        initialize();
    }

    public DoubleProperty radiusProperty() { return frame.radiusProperty(); }
    public ObjectProperty<Image> imageProperty() { return icon.imageProperty(); }

    public double getRadius() { return frame.getRadius(); }
    public void setRadius(double r) {
        frame.setRadius(r);
        countdownLabel.setFont(Font.font(null, FontWeight.BOLD, r*0.8));

        if (countdownPath.isVisible())
            updateVisuals();
    }

    public String getUrl() {
        Image image = icon.getImage();
        if (image != null)
            return image.getUrl();
        return null;
    }

    public void setUrl(String url) {
        if (url != null && !url.isEmpty())
            setImage(new Image(url));
        else
            icon.setImage(null);
    }

    public Image getImage() { return icon.getImage(); }
    public void setImage(Image image) {
        icon.setImage(image);
        icon.fitWidthProperty().unbind();
        icon.fitHeightProperty().unbind();
        if (image.getWidth() <= image.getHeight())
            icon.fitWidthProperty().bind(frame.radiusProperty().multiply(2));
        else
            icon.fitHeightProperty().bind(frame.radiusProperty().multiply(2));
    }

    public void setOnCountdownEnd(EventHandler<ActionEvent> onCountdownEnd) {
        this.onCountdownEnd = onCountdownEnd;
    }

    public void startCountdown() {
        stopCountdown();

        maxSeconds = Game.getCountdownSeconds();
        timeSeconds.set(maxSeconds);

        countdownPath.setVisible(true);
        countdownLabel.setVisible(true);
        countdownLabel.toFront();

        frame.setOpacity(0);
        face.setOpacity(0.6);

        if (timeline != null) timeline.stop();

        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(timeSeconds, maxSeconds)),
                new KeyFrame(Duration.seconds(maxSeconds), new KeyValue(timeSeconds, 0))
        );

        timeline.play();
    }

    public void stopCountdown() {
        if (timeline != null) timeline.stop();
        if (heartbeatAnimation != null) heartbeatAnimation.stop();

        frame.setOpacity(1);
        face.setOpacity(0);

        countdownLabel.setScaleX(1.0);
        countdownLabel.setScaleY(1.0);
        countdownLabel.setVisible(false);
        countdownPath.setVisible(false);
    }

    private void initialize() {
        frame.setRadius(32);
        frame.setFill(new LinearGradient(0.6, 1, 0.3, 0, true, CycleMethod.NO_CYCLE, new Stop[]{
                new Stop(0, Color.web("#915C21")),
                new Stop(0.14, Color.web("#51310D")),
                new Stop(0.53, Color.web("#B57124")),
                new Stop(1, Color.web("#FFD6A7"))
        }));

        face.setOpacity(0);
        face.setFill(Color.BLACK);
        face.radiusProperty().bind(frame.radiusProperty().multiply(0.85));

        clip.setStrokeWidth(0);
        clip.setFill(Color.BLACK);
        clip.centerXProperty().bind(frame.radiusProperty());
        clip.centerYProperty().bind(frame.radiusProperty());
        clip.radiusProperty().bind(frame.radiusProperty().multiply(0.85));

        icon.setClip(clip);
        icon.setPreserveRatio(true);

        countdownPath.setVisible(false);
        countdownPath.setManaged(false);
        countdownPath.layoutXProperty().bind(widthProperty().divide(2));
        countdownPath.layoutYProperty().bind(heightProperty().divide(2));

        countdownLabel.setVisible(false);
        countdownLabel.setManaged(false);
        countdownLabel.setEffect(new DropShadow(4, Color.web("#000")));

        setAlignment(Pos.CENTER);
        getChildren().addAll(frame, countdownPath, icon, face, countdownLabel);

        heartbeatAnimation = new ScaleTransition(Duration.millis(200), countdownLabel);
        heartbeatAnimation.setFromX(1.0);
        heartbeatAnimation.setFromY(1.0);
        heartbeatAnimation.setToX(1.5);
        heartbeatAnimation.setToY(1.5);
        heartbeatAnimation.setAutoReverse(true);
        heartbeatAnimation.setCycleCount(2);

        timeSeconds.addListener((obs, oldVal, newVal) -> {
            updateVisuals();
        });
    }

    private void updateVisuals() {
        double current = timeSeconds.get();
        double progress = 1.0 - (current / maxSeconds);

        double startAngle = -90;
        double sweepAngle = progress * 360;
        double endAngle = startAngle + sweepAngle;

        double r = getRadius();
        if (r <= 0) return;

        double startX = r * Math.cos(Math.toRadians(startAngle));
        double startY = r * Math.sin(Math.toRadians(startAngle));
        double endX = r * Math.cos(Math.toRadians(endAngle));
        double endY = r * Math.sin(Math.toRadians(endAngle));

        countdownPath.getElements().clear();
        countdownPath.getElements().add(new MoveTo(0, 0)); // 圆心
        countdownPath.getElements().add(new LineTo(startX, startY)); // 起点

        boolean largeArc = sweepAngle > 180;
        countdownPath.getElements().add(new ArcTo(r, r, 0, endX, endY, largeArc, true));
        countdownPath.getElements().add(new ClosePath());

        Color fill = interpolateColor(countdownStartColor, countdownEndColor, progress);
        Color stroke = fill.darker().darker();
        countdownPath.setFill(fill);
        countdownPath.setStroke(stroke);
        countdownLabel.setFill(fill);
        countdownLabel.setStroke(stroke);

        int intSeconds = (int) Math.ceil(current);
        String text = String.valueOf(intSeconds);

        if (!countdownLabel.getText().equals(text)) {
            countdownLabel.setText(text);

            if (intSeconds <= 10 && intSeconds > 0) {
                if (heartbeatAnimation.getStatus() != Animation.Status.RUNNING)
                    heartbeatAnimation.play();
            }
        }

        Bounds bounds = countdownLabel.getBoundsInLocal();
        countdownLabel.setLayoutX((getWidth() - bounds.getWidth())/2);
        countdownLabel.setLayoutY(getHeight()/2 + bounds.getHeight()/4);

        if (current <= 0) {
            stopCountdown();
            if (onCountdownEnd != null)
                onCountdownEnd.handle(new ActionEvent());
        }
    }

    private Color interpolateColor(Color start, Color end, double progress) {
        double red = start.getRed() + (end.getRed() - start.getRed()) * progress;
        double green = start.getGreen() + (end.getGreen() - start.getGreen()) * progress;
        double blue = start.getBlue() + (end.getBlue() - start.getBlue()) * progress;
        double opacity = start.getOpacity() + (end.getOpacity() - start.getOpacity()) * progress;
        return new Color(red, green, blue, opacity);
    }
}
