package ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.ImageView;

public class SvgView extends ImageView {
    private final StringProperty svgPath = new SimpleStringProperty();

    public SvgView() {
        setSmooth(true);
        svgPath.addListener((observable, oldPath, path) -> {
            setImage(SvgImage.load(path));
        });
    }

    public String getSvgPath() {
        return svgPath.get();
    }

    public void setSvgPath(String svgPath) {
        this.svgPath.set(svgPath);
    }

    public StringProperty svgPathProperty() {
        return svgPath;
    }
}
