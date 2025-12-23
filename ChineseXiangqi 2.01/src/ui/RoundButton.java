package ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class RoundButton extends Label {
    private Effect effect = new DropShadow(8, 0, 2, Color.web("#000", 0.2));
    private EventHandler<ActionEvent> onAction;

    public RoundButton() {
        initialize();
    }

    public RoundButton(String text) {
        super(text);

        initialize();
        setText(text);
    }

    private void initialize() {
        setFill(Color.web("#654C2B"));
        setTextFill(Color.web("#FFE5C9"));
        setTextAlignment(TextAlignment.CENTER);
        setFont(Font.font(null, FontWeight.BOLD, 16));
        setPadding(new Insets(8, 32, 8 , 32));
        setEffect(effect);

        setOnMousePressed(this::onMousePressed);
        setOnMouseReleased(this::onMouseReleased);
        setOnMouseClicked(e -> {
            if (onAction != null) onAction.handle(new ActionEvent());
        });
    }

    public Paint getFill() {
        Background background = getBackground();
        if (background == null || background.isEmpty())
            return null;
        return background.getFills().get(0).getFill();
    }

    public void setFill(Paint paint) {
        setBackground(new Background(new BackgroundFill(paint, new CornerRadii(100), Insets.EMPTY)));
    }

    public Paint getStroke() {
        Border border = getBorder();
        if (border == null || border.isEmpty())
            return null;
        return border.getStrokes().get(0).getTopStroke();
    }

    public void setStroke(Paint paint) {
        double width = 1;
        Border border = getBorder();
        if (border != null && !border.isEmpty())
            width = border.getStrokes().get(0).getWidths().getTop();

        setBorder(new Border(new BorderStroke(paint, BorderStrokeStyle.SOLID,
                new CornerRadii(100),
                new BorderWidths(width))));
    }

    public EventHandler<ActionEvent> getOnAction() {return onAction; }
    public void setOnAction (EventHandler<ActionEvent> handler) { onAction = handler; }

    private void onMousePressed(MouseEvent e) {
        effect = getEffect();
        if (effect instanceof DropShadow shadow) {
            setEffect(null);
            setTranslateY(shadow.getOffsetY());
        }
        else
            setEffect(new InnerShadow(2, 0, 2, Color.web("#000", 0.2)));
    }

    private void onMouseReleased(MouseEvent e) {
        setEffect(effect);
        setTranslateY(0);
    }
}
