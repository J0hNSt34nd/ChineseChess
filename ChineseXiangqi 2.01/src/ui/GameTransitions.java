package ui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.util.Duration;


public class GameTransitions {
    public static final Duration DURATION = Duration.millis(1000);

    public static Transition fade(double times, Node node) {
        return fade(times, node, null);
    }

    public static Transition fade(double times, Node node, EventHandler<ActionEvent> onInFinished) {
        FadeTransition transition = new FadeTransition(DURATION.multiply(times), node);
        if (!node.isVisible()) {
            node.setOpacity(0);
            transition.setToValue(1);
            transition.setOnFinished(onInFinished);
            Platform.runLater(() -> node.setVisible(true));
        }
        else {
            transition.setToValue(0);
            transition.setOnFinished(e -> node.setVisible(false));
        }
        return transition;
    }

    public static Transition zoom(double times, double scale, Node node) {
        ScaleTransition transition = new ScaleTransition(DURATION.multiply(times), node);
        if (!node.isVisible() || Math.abs(node.getScaleX() - 1) > 0.001) {
            node.setScaleX(scale);
            node.setScaleY(scale);
            transition.setToX(1);
            transition.setToY(1);
        }
        else {
            transition.setToX(scale);
            transition.setToY(scale);
        }
        return transition;
    }

    public static Transition move(double times, double dirX, double dirY, Node node) {
        TranslateTransition transition = new TranslateTransition(DURATION.multiply(times), node);
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        double x = 0, y = 0;
        if (dirX < 0)
            x = bounds.getMaxX() * dirX;
        else if (dirX > 0)
            x = (node.getScene().getWidth() - bounds.getMinX()) * dirX;
        if (dirY < 0)
            y = bounds.getMaxX() * dirY;
        else
            y = (node.getScene().getHeight() - bounds.getMinY()) * dirY;

        if (!node.isVisible()) {
            node.setTranslateX(x);
            node.setTranslateY(y);
            transition.setToX(0);
            transition.setToY(0);
            Platform.runLater(() -> node.setVisible(true));
        }
        else {
            transition.setToX(x);
            transition.setToY(y);
            transition.setOnFinished(e -> {
                node.setVisible(false);
                node.setTranslateX(0);
                node.setTranslateY(0);
            });
        }
        return transition;
    }

    public static Transition flip(double times, Node node1, Node node2) {
        Node from = node1.isVisible() ? node1 : node2;
        Node to = node1.isVisible() ? node2 : node1;
        ScaleTransition hide = new ScaleTransition(DURATION.multiply(times / 2), from);
        ScaleTransition show = new ScaleTransition(DURATION.multiply(times / 2), to);
        SequentialTransition transition = new SequentialTransition(hide, show);

        show.setToY(1);
        hide.setToY(0);
        hide.setOnFinished(e -> {
            from.setVisible(false);
            to.setScaleY(0);
            to.setVisible(true);
        });
        return transition;
    }

    public static Transition play(boolean reversed, Transition... transitions) {
        if (reversed) {
            for (int i = 0, j = transitions.length - 1; i < j; ++i, --j) {
                Transition t = transitions[i];
                transitions[i] = transitions[j];
                transitions[j] = t;
            }
        }
        Transition sequence = new SequentialTransition(transitions);
        sequence.play();
        return sequence;
    }
}
