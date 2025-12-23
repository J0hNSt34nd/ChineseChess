package ui;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class PopupTitle extends HBox {
    private static final String FRAME_DATA =
            "M22 1 C19.5 1 15 0.84 15 6.6 C13.5 6.6 11 6.44 11 9 V13 C8 13.6675 1 16.52 1 25.8 C1 35.4 8 38.4673 11 38.6 V43.4 C11.1667 44.4672 12.2 46.6 15 46.6 C14.8333 48 15.8 51 21 51" +
                    "M179 1 C181.5 1 186 0.84 186 6.6 C187.5 6.6 190 6.44 190 9 V13 C193 13.6675 200 16.5208 200 25.8 C200 35.4 193 38.4673 190 38.6 V43.4 C189.833 44.4672 188.8 46.6 186 46.6 C186.167 48 185.2 51 180 51" +
                    "M179 51 H21 M22 1 H179.218";

    public PopupTitle(String text) {
        Color lineColor = Color.web("#B98242");

        Line leftLine = new Line(0, 0, 32, 0);
        Line rightLine = new Line(0, 0, 32, 0);
        leftLine.setStroke(lineColor);
        rightLine.setStroke(lineColor);

        SVGPath outerFrame = new SVGPath();
        outerFrame.setContent(FRAME_DATA);
        outerFrame.setStrokeWidth(2);
        outerFrame.setStroke(lineColor);
        outerFrame.setFill(null);

        SVGPath innerFrame = new SVGPath();
        Bounds bounds = outerFrame.getBoundsInLocal();
        double s = 8;
        innerFrame.setContent(FRAME_DATA);
        innerFrame.setStroke(lineColor);
        innerFrame.setFill(null);
        innerFrame.setScaleX(1 - s / bounds.getWidth());
        innerFrame.setScaleY(1 - s / bounds.getHeight());

        Text title = new Text(text);
        title.setFill(Color.web("#552E03"));
        title.setFont(Font.font(null, FontWeight.BOLD, 16));

        StackPane titlePane = new StackPane(outerFrame, innerFrame, title);
        titlePane.setPadding(new Insets(8));

        getChildren().addAll(new Circle(4, lineColor), leftLine, titlePane, rightLine, new Circle(4, lineColor));
        setAlignment(Pos.CENTER);
    }
}
