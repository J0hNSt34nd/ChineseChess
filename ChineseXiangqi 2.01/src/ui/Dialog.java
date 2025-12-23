package ui;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class Dialog extends Stage {
    private final PopupPane background = new PopupPane(8);

    public Dialog(Window window, String title) {
        initOwner(window);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.TRANSPARENT);
        setTitle(title);

        Scene scene = new Scene(background);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    public ObservableList<Node> getChildren() {
        return background.getChildren();
    }
}
