//package main;
//
//import Ai.Book;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.*;
//
//public class ui extends StackPane {
//
//    Book book;
//    Board board;
//
//    public ui(Book book)
//    {
//        this.book = book;
//
//        // --- 1. 底层：全局背景图 ---
//        try {
//            Image bgImage = new Image(getClass().getResourceAsStream("/uiBackground.png"));
//
//
//            BackgroundSize bgSize = new BackgroundSize(
//                    1.0, 1.0, true, true, false, false
//            );
//
//            BackgroundImage bg = new BackgroundImage(
//                    bgImage, BackgroundRepeat.NO_REPEAT,
//                    BackgroundRepeat.NO_REPEAT,
//                    BackgroundPosition.CENTER,
//                    bgSize
//            );
//
//            this.setBackground(new Background(bg));
//        } catch (Exception e) {
//            System.err.println("无法加载背景图片: " + e.getMessage());
//        }
//
//        BorderPane mainLayout = new BorderPane();
//        mainLayout.setStyle("-fx-background-color: transparent;");
//
//        Region decorateLeft = createSideRegion("/decorateLeft.png");
//        if (decorateLeft != null) {
//            Insets insets = new Insets(0,100,0,100);
//            mainLayout.setPadding(insets);
//            mainLayout.setLeft(decorateLeft);
//        }
//
//        Region decorateRight = createSideRegion("/decorateRight.png");
//        if (decorateRight != null) {
//            mainLayout.setRight(decorateRight);
//        }
//
//        GameToolBar toolBar = new GameToolBar();
//        toolBar.setOnMenu(() -> System.out.println("菜单被点击"));
//        toolBar.setOnUndo(() ->
//                {System.out.println("准备悔棋");
//                    board.undo();
//                });
//
//        BorderPane boardArea = new BorderPane();
//
//        board = new Board(book);
//        board.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
//        boardArea.setCenter(board);
//
////        VBox centerArea = new VBox(40);
////        centerArea.setAlignment(Pos.CENTER);
////        centerArea.getChildren().addAll(board, toolBar);
//
////        Region topSpacer = new Region();
////        topSpacer.setPrefHeight(100);
////        boardArea.setTop(topSpacer);
////
////        Region bottomSpacer = new Region();
////        bottomSpacer.setPrefHeight(100);
////        boardArea.setBottom(bottomSpacer);
//        toolBar.setMaxSize(board.getWidth(),board.getHeight());
//        boardArea.setBottom(toolBar);
//        this.setPrefSize(1000, 800);
//
//        mainLayout.setCenter(boardArea);
//
//        this.getChildren().add(mainLayout);
//    }
//
//    /**
//     * 创建一个带有背景图的 Region（侧边栏）
//     * 优点：图片不会被拉伸变形，宽度自动适应图片原宽
//     */
//    private Region createSideRegion(String imagePath) {
//        try {
//            Image image = new Image(getClass().getResourceAsStream(imagePath));
//            Region region = new Region();
//
//            region.setPrefWidth(image.getWidth());
//            region.setMinWidth(image.getWidth()); // 防止窗口缩小时被挤没了
//            region.setMinWidth(image.getWidth());
//
//            BackgroundImage bg = new BackgroundImage(
//                    image,
//                    BackgroundRepeat.NO_REPEAT, // 水平不重复
//                    BackgroundRepeat.REPEAT,  // 垂直重复（平铺）
//                    BackgroundPosition.CENTER,  // 居中放置
//                    BackgroundSize.DEFAULT      // 保持原始图片大小，不缩放
//            );
//
//            region.setBackground(new Background(bg));
//            return region;
//        } catch (Exception e) {
//            System.err.println("无法加载装饰图片 " + imagePath + ": " + e.getMessage());
//            return null;
//        }
//    }
//}