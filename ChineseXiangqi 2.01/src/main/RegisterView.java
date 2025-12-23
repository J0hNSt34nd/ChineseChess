package main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import ui.PopupPane;

import java.io.*;
import java.util.Properties;
import java.util.function.Consumer;

public class RegisterView extends PopupPane {
    private TextField regName = new TextField();
    private PasswordField regPass = new PasswordField();
    private PasswordField confirmPass = new PasswordField();
    private Stage stage;
    private Consumer<String> onRegistrationSuccess; // 成功回调

    public RegisterView(Stage stage) {
        super(2.0);
        this.stage = stage;
        setFixedSize(400, 480);
        initLayout();
    }

    public void setOnRegistrationSuccess(Consumer<String> callback) {
        this.onRegistrationSuccess = callback;
    }

    private void initLayout() {
        VBox container = new VBox(25);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(60, 40, 40, 40));

        Label title = new Label("用 户 注 册");
        title.setFont(Font.font("KaiTi", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#874802"));

        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);

        grid.add(createLabel("用户名："), 0, 0);
        grid.add(regName, 1, 0);
        grid.add(createLabel("密  码："), 0, 1);
        grid.add(regPass, 1, 1);
        grid.add(createLabel("确  认："), 0, 2);
        grid.add(confirmPass, 1, 2);

        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);
        Button okBtn = new Button("确认注册");
        Button cancelBtn = new Button("取消");

        okBtn.setPrefSize(100, 35);
        cancelBtn.setPrefSize(100, 35);
        okBtn.setStyle("-fx-background-color: #B93B3B; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        cancelBtn.setStyle("-fx-background-color: #777777; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        btnBox.getChildren().addAll(okBtn, cancelBtn);

        okBtn.setOnAction(_ -> doRegister());
        cancelBtn.setOnAction(_ -> stage.close());

        container.getChildren().addAll(title, grid, btnBox);
        getChildren().add(container);
    }

    private Label createLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#552E03"));
        l.setFont(Font.font("System", FontWeight.BOLD, 16));
        return l;
    }

    private void doRegister() {
        String name = regName.getText().trim();
        String pass = regPass.getText().trim();
        String conf = confirmPass.getText().trim();

        if (name.isEmpty()) {
            showAlert("警告", "用户名不能为空！", Alert.AlertType.WARNING);
            regName.requestFocus();
            return;
        }

        Properties settings = new Properties();
        try (InputStream in = new FileInputStream("chess.properties")) {
            settings.load(in);
        } catch (IOException e) { }

        if (settings.containsKey("RegisteredUser_" + name)) {
            showAlert("警告", "用户名已存在！", Alert.AlertType.WARNING);
            regName.requestFocus();
            return;
        }

        if (pass.isEmpty() || conf.isEmpty()) {
            showAlert("警告", "密码不能为空！", Alert.AlertType.WARNING);
            regPass.requestFocus();
            return;
        }

        if (!pass.equals(conf)) {
            showAlert("警告", "两次输入的密码不一致！", Alert.AlertType.WARNING);
            confirmPass.setText("");
            confirmPass.requestFocus();
            return;
        }

        settings.setProperty("RegisteredUser_" + name, pass);
        try (OutputStream out = new FileOutputStream("chess.properties")) {
            settings.store(out, "Chess Users Settings");

            showAlert("成功", "注册成功！请返回登录", Alert.AlertType.INFORMATION);

            if (onRegistrationSuccess != null) {
                onRegistrationSuccess.accept(name);
            }
            stage.close();
        } catch (IOException e) {
            showAlert("错误", "注册失败，请检查文件权限！", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}