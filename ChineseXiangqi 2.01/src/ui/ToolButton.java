package ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public class ToolButton extends GridPane {
    private final Circle background = new Circle();
    private final SVGPath icon = new SVGPath();
    private final Label label = new Label();
    private final StackPane iconPane = new StackPane(background, icon);
    private String iconSource;
    private EventHandler<ActionEvent> onAction;

    public enum Display {
        TextUnderIcon,
        TextBesideIcon,
        TextOnly,
        IconOnly
    }

    public ToolButton() {
        Color color = Color.web("#F6C084");

        background.setFill(Color.TRANSPARENT);
        icon.setFill(color);
        icon.setEffect(new InnerShadow(2, 0, 1, Color.web("#000", 0.8)));

        label.setTextFill(color);
        label.setFont(Font.font(null, FontWeight.BOLD, 16));
        label.setOpacity(0.7);
        label.setMouseTransparent(true);
        iconPane.setMouseTransparent(true);

        iconPane.managedProperty().bind(iconPane.visibleProperty());
        label.managedProperty().bind(label.visibleProperty());


        setHgap(12);
        setVgap(12);
        setIconSize(32);
        setAlignment(Pos.CENTER);
        GridPane.setRowIndex(label, 1);
        getChildren().addAll(iconPane, label);

        setOnMouseEntered(this::onMouseEntered);
        setOnMouseExited(this::onMouseExited);
        setOnMousePressed(this::onMousePressed);
        setOnMouseReleased(this::onMouseReleased);
        setOnMouseClicked(e -> {
            if (onAction!= null) onAction.handle(new ActionEvent());
        });
    }

    public Display getDisplay() {
        if (!iconPane.isVisible())
            return Display.TextOnly;
        if (!label.isVisible())
            return Display.IconOnly;
        if (GridPane.getColumnIndex(label) == 1)
            return Display.TextBesideIcon;
        return Display.TextUnderIcon;
    }



    public boolean isHorizontal() { return GridPane.getColumnIndex(label) == 1; }
    public void setHorizontal(boolean h) {
        if (h) {
            GridPane.setRowIndex(label, 0);
            GridPane.setColumnIndex(label, 1);
        }
        else {
            GridPane.setRowIndex(label, 1);
            GridPane.setColumnIndex(label, 0);
        }
    }

    public void setDisplay(Display display) {
        iconPane.setVisible(display != Display.TextOnly);
        label.setVisible(display != Display.IconOnly);
        if (display == Display.TextUnderIcon) {
            GridPane.setRowIndex(label, 1);
            GridPane.setColumnIndex(label, 0);
        }
        else if (display == Display.TextBesideIcon) {
            GridPane.setRowIndex(label, 0);
            GridPane.setColumnIndex(label, 1);
        }
    }

    public String getIconData() { return icon.getContent(); }
    public void setIconData(String data) {
        icon.setContent(data);
        updateIcon();
    }

    public String getIconSource() { return iconSource; }
    public void setIconSource(String url) {
        iconSource = url;
        try {
            InputStream stream = getClass().getResourceAsStream(url);
            if (stream == null)
                throw new RuntimeException("找不到资源文件: " + url);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(stream);

            StringBuilder content = new StringBuilder();
            NodeList pathNodes = document.getElementsByTagName("path");
            for (int i = 0; i < pathNodes.getLength(); i++) {
                Element pathElement = (Element) pathNodes.item(i);
                String dAttribute = pathElement.getAttribute("d");
                if (!dAttribute.isEmpty())
                    content.append(dAttribute);
            }
            icon.setContent(content.toString());
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public Effect getIconEffect() { return icon.getEffect(); }
    public void setIconEffect(Effect effect) { icon.setEffect(effect); }

    public String getText() { return label.getText(); }
    public void setText(String text) { label.setText(text); }

    public Paint getFill() { return icon.getFill(); }
    public void setFill(Paint fill) {
        icon.setFill(fill);
        label.setTextFill(fill);
    }

    public Font getFont() { return label.getFont(); }
    public void setFont(Font font) { label.setFont(font); }

    public double getIconSize() { return background.getRadius()*2; }
    public void setIconSize(double size) {
        background.setRadius(size * 0.5);
        updateIcon();
    }

    public EventHandler<ActionEvent> getOnAction() { return onAction; }
    public void setOnAction(EventHandler<ActionEvent> handler) {  onAction = handler; }

    private void onMouseEntered(MouseEvent e) {
        background.setFill(Color.web("#FFF", 0.1));
        iconPane.setScaleX(1.1);
        iconPane.setScaleY(1.1);
    }

    private void onMouseExited(MouseEvent e) {
        background.setFill(Color.TRANSPARENT);
        iconPane.setScaleX(1);
        iconPane.setScaleY(1);
    }

    private void onMousePressed(MouseEvent e) {
        background.setFill(Color.web("#000", 0.1));
        iconPane.setScaleX(1);
        iconPane.setScaleY(1);
    }

    private void onMouseReleased(MouseEvent e) {
        if (this.isHover()) onMouseEntered(e);
        else onMouseExited(e);
    }

    private void updateIcon() {
        Bounds box = icon.getBoundsInLocal();
        if (!box.isEmpty()) {
            double size = background.getRadius() * 2;
            icon.setScaleX(size / box.getWidth());
            icon.setScaleY(size / box.getHeight());
        }
    }
}

