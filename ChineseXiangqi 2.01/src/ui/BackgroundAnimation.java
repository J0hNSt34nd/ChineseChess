package ui;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Bounds;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class BackgroundAnimation extends Pane {
    public BackgroundAnimation() {
        ImageView leftBamboo = new ImageView(String.valueOf(getClass().getResource("" +
                "/bamboo1.png")));
        leftBamboo.setPreserveRatio(true);
        leftBamboo.setOpacity(0.3);
        getChildren().add(leftBamboo);

        ImageView rightBamboo = new ImageView(String.valueOf(getClass().getResource("/bamboo1.png")));
        rightBamboo.setPreserveRatio(true);
        rightBamboo.setOpacity(0.3);
        rightBamboo.setScaleX(-1);
        getChildren().add(rightBamboo);

        Duration duration = Duration.seconds(10);
        RotateTransition rotation = new RotateTransition(duration, leftBamboo);
        rotation.setInterpolator(Interpolator.EASE_BOTH);
        rotation.setByAngle(15);
        rotation.setCycleCount(10000);
        rotation.setAutoReverse(true);
        rotation.play();
        rotation = new RotateTransition(duration, rightBamboo);
        rotation.setInterpolator(Interpolator.EASE_BOTH);
        rotation.setByAngle(-15);
        rotation.setCycleCount(10000);
        rotation.setAutoReverse(true);
        rotation.play();

        List<Leaf> leaves = new ArrayList<>();
        for (int i = 0; i < 40; ++i) {
            leaves.add(new Leaf());
            getChildren().add(leaves.get(i));
        }
        heightProperty().addListener(observable -> {
            double w = getWidth();
            double h = getHeight();

            leftBamboo.setFitHeight(h/2);
            Bounds bounds = leftBamboo.getBoundsInLocal();
            leftBamboo.setLayoutX(-bounds.getWidth()*0.55);
            leftBamboo.setLayoutY((h - bounds.getHeight())*0.7);

            rightBamboo.setFitHeight(h*0.5);
            bounds = rightBamboo.getBoundsInLocal();
            rightBamboo.setLayoutX(w - bounds.getWidth()*0.7);
            rightBamboo.setLayoutY(h - bounds.getHeight());

            for (Leaf leaf : leaves)
                leaf.startFloating(w, h);
        });
    }
}
