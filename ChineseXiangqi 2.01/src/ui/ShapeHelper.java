package ui;

import javafx.scene.shape.*;
import org.jetbrains.annotations.NotNull;

public class ShapeHelper {
    public static void setConcaveRectangle(@NotNull Path path, double w, double h, double r) {
        path.getElements().setAll(
                new MoveTo(0, r),
                new ArcTo(r, r, -90, r, 0, false, false),
                new LineTo(w - r, 0),
                new ArcTo(r, r, -90, w, r, false, false),
                new LineTo(w, h - r),
                new ArcTo(r, r, -90, w - r, h, false, false),
                new LineTo(r, h),
                new ArcTo(r, r, -90, 0, h - r, false, false),
                new ClosePath()
        );
    }
}

