package ui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.jetbrains.annotations.NotNull;
public class PopupPane extends StackPane {
    private final Polyline[] innerCorner = new Polyline[]{
            new Polyline(), new Polyline(), new Polyline(), new Polyline()
    };

    public PopupPane(double borderWidth) {
        double arc = 16;
        double padding = 32;
        double margin = padding * 2;
        Rectangle frame = new Rectangle();
        frame.setArcWidth(arc + borderWidth);
        frame.setArcHeight(arc + borderWidth);
        frame.setStrokeWidth(borderWidth);
        frame.setStroke(Color.web("#B98242"));
        frame.setFill(Color.web("#F6E4CF"));
        frame.widthProperty().bind(widthProperty().subtract(margin));
        frame.heightProperty().bind(heightProperty().subtract(margin));

        margin += borderWidth * 2;
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty().subtract(margin));
        clip.heightProperty().bind(heightProperty().subtract(margin));
        clip.setArcWidth(arc);
        clip.setArcHeight(arc);

        ImageView background = new ImageView();
        background.setClip(clip);
        background.setOpacity(0.4);
        background.fitWidthProperty().bind(widthProperty().subtract(margin));
        background.fitHeightProperty().bind(heightProperty().subtract(margin));
        background.setImage(new Image(String.valueOf(getClass().getResource("/popup_background.png"))));

        margin += arc * 2;
        Rectangle border = new Rectangle();
        border.setStrokeWidth(2);
        border.setStroke(Color.web("#D8AC71"));
        border.setFill(null);
        border.setEffect(new InnerShadow(1, 1, 1, Color.web("#47310E", 0.25)));
        border.widthProperty().bind(widthProperty().subtract(margin));
        border.heightProperty().bind(heightProperty().subtract(margin));

        margin = borderWidth + arc*1.5;

        setPadding(new Insets(padding));
        setBackground(Background.EMPTY);
        setEffect(new DropShadow(padding - 8, 0, 4, Color.web("#47310E", 0.6)));

        getChildren().addAll(frame, background, border,
                createLine(Pos.TOP_CENTER, margin + 0.5), createLine(Pos.BOTTOM_CENTER, margin),
                createLine(Pos.CENTER_LEFT, margin + 0.5), createLine(Pos.CENTER_RIGHT, margin),
                createCorner(Pos.TOP_LEFT, margin), createCorner(Pos.TOP_RIGHT, margin),
                createCorner(Pos.BOTTOM_RIGHT, margin), createCorner(Pos.BOTTOM_LEFT, margin));
    }

    public void setFixedSize(double w, double h) {
        setPrefSize(w, h);
        setMinSize(w, h);
        setMaxSize(w, h);
    }

    @NotNull
    private Line createLine(Pos pos, double margin) {
        Line line = new Line();
        line.setStrokeWidth(2);
        line.setStroke(Color.web("#D8AC71"));
        setAlignment(line, pos);
        setMargin(line, new Insets(margin));
        if (pos.getHpos() == HPos.CENTER)
            line.endXProperty().bind(widthProperty().subtract(margin*2 + 120));
        else if (pos.getVpos() == VPos.CENTER)
            line.endYProperty().bind(heightProperty().subtract(margin*2 + 120));
        return line;
    }

    @NotNull
    private SVGPath createCorner(Pos pos, double margin) {
        SVGPath corner = new SVGPath();
        corner.setContent("M1 37.5 H15V1H1V15.5H41V1H23V13V25H1V37.5Z");
        corner.setStrokeWidth(2);
        corner.setStroke(Color.web("#D8AC71"));
        corner.setFill(null);
        setAlignment(corner, pos);
        setMargin(corner, new Insets(margin));
        if (pos.getHpos() == HPos.RIGHT)
            corner.setScaleX(-1);
        if (pos.getVpos() ==VPos.BOTTOM)
            corner.setScaleY(-1);
        return corner;
    }
}
