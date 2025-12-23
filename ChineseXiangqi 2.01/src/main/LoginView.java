package main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import main.Game;
import java.io.*;
import ui.PopupPane;

import javax.swing.*;
import java.util.Properties;

public class LoginView extends PopupPane {
    private static final String SETTINGS = "chess.properties";
    private Properties settings = new Properties();

    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private CheckBox rememberCheckBox = new CheckBox("记住我(勾选登录才能存档)");

    private Runnable onLoginSuccess;
    private Stage ownerStage;

    public LoginView(Stage stage) {
        super(2.0);
        this.ownerStage = stage;
        setFixedSize(460, 520);
        loadSettings();
        initLayout();
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    private void loadSettings() {
        File file = new File(SETTINGS);
        settings.clear();
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                settings.load(input);
                if (Boolean.parseBoolean(settings.getProperty("remember", "false"))) {
                    usernameField.setText(settings.getProperty("written_User", ""));
                    passwordField.setText(settings.getProperty("written_Password", ""));
//                    usernameField.addActionListener(e -> passwordField.requestFocus());
//                    passwordField.addActionListener(e -> handleLogin());
                    rememberCheckBox.setSelected(true);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void initLayout() {
        VBox container = new VBox(25);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(60, 50, 50, 50));

        Label titleLabel = new Label("中 国 象 棋");
        titleLabel.setFont(Font.font("KaiTi", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#874802"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        Label uLabel = createLabel("用户名：");
        Label pLabel = createLabel("密  码：");
        grid.add(uLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(pLabel, 0, 1);
        grid.add(passwordField, 1, 1);

        rememberCheckBox.setTextFill(Color.web("#552E03"));

        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(15);
        buttonGrid.setVgap(15);
        buttonGrid.setAlignment(Pos.CENTER);

        Button loginBtn = createStyledButton("登录", "#B93B3B");
        Button resetBtn = createStyledButton("重置", "#428BCA");
        Button regBtn = createStyledButton("注册", "#D87093");
        Button guestBtn = createStyledButton("游客模式", "#F0AD4E");

        buttonGrid.add(loginBtn, 0, 0);
        buttonGrid.add(resetBtn, 1, 0);
        buttonGrid.add(regBtn, 0, 1);
        buttonGrid.add(guestBtn, 1, 1);

        loginBtn.setOnAction(e -> handleLogin());
        resetBtn.setOnAction(e -> { usernameField.clear(); passwordField.clear(); });
        regBtn.setOnAction(e -> handleRegister());
        guestBtn.setOnAction(e -> handleGuest());

        container.getChildren().addAll(titleLabel, grid, rememberCheckBox, buttonGrid);
        getChildren().add(container);

        final double[] xOffset = new double[1];
        final double[] yOffset = new double[1];
        this.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        this.setOnMouseDragged(event -> {
            ownerStage.setX(event.getScreenX() - xOffset[0]);
            ownerStage.setY(event.getScreenY() - yOffset[0]);
        });
    }

    private Label createLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 16));
        l.setTextFill(Color.web("#552E03"));
        return l;
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(110, 35);
        btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;", color));
        return btn;
    }

    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();
        String confirmPass = settings.getProperty("RegisteredUser_" + user);

        if (confirmPass != null && confirmPass.equals(pass)) {
            if (rememberCheckBox.isSelected()) {
                settings.setProperty("written_User", user);
                settings.setProperty("written_Password", pass);
                settings.setProperty("remember", "true");
            } else {
                settings.remove("written_User");
                settings.remove("written_Password");
                settings.setProperty("remember", "false");
            }
            saveProperties();
            Game.setUserName(user);
            if (onLoginSuccess != null) onLoginSuccess.run();
        } else {
            showInfo("错误", "用户名或密码错误！");
        }
    }

    private void handleGuest() {
        settings.remove("written_User");
        settings.remove("written_Password");
        settings.setProperty("remember", "false");
        try (OutputStream output = new FileOutputStream(SETTINGS))
        {
            settings.store(output, "Chess Users Settings");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "游客模式刷新用户失败！", "错误", JOptionPane.WARNING_MESSAGE);
        }
        Game.setUserName("Tourist");
        if (onLoginSuccess != null) onLoginSuccess.run();
    }

    private void handleRegister() {
        Stage regStage = new Stage();
        regStage.initOwner(ownerStage);
        regStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        regStage.initStyle(StageStyle.TRANSPARENT);

        RegisterView regView = new RegisterView(regStage);
        regView.setOnRegistrationSuccess(registeredName -> {
            usernameField.setText(registeredName);
            loadSettings();
        });

        Scene s = new Scene(regView);
        s.setFill(Color.TRANSPARENT);
        regStage.setScene(s);

        final double[] xOffset = new double[1];
        final double[] yOffset = new double[1];
        regView.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        regView.setOnMouseDragged(event -> {
            regStage.setX(event.getScreenX() - xOffset[0]);
            regStage.setY(event.getScreenY() - yOffset[0]);
        });

        regStage.showAndWait();
    }

    private void saveProperties() {
        try (OutputStream out = new FileOutputStream(SETTINGS)) {
            settings.store(out, null);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}