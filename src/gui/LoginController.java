package gui;

import database.DatabaseConnection;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private ImageView logoImage;

    @FXML
    public void initialize() {
        try {
            // 🔹 Logo yükleme
            String path = "C:/Users/Gamze/Desktop/lastikGUI/images/logo.png";
            FileInputStream input = new FileInputStream(path);
            Image logo = new Image(input);
            logoImage.setImage(logo);
        } catch (Exception e) {
            System.err.println("❌ Logo yüklenemedi: " + e.getMessage());
        }

        // 🔹 Enter tuşuna basıldığında giriş işlemini tetikler
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleLogin();
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleLogin();
        });

        // 🔹 Ortak layout yenileme (tam ekran uyumlu)
        LayoutRefresher.refresh(usernameField);
    }

    @FXML
    private void handleLogin() {
        String u = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String p = passwordField.getText() == null ? "" : passwordField.getText();

        if (u.isEmpty() || p.isEmpty()) {
            errorLabel.setText("Kullanıcı adı ve şifre zorunludur.");
            return;
        }

        boolean girisBasarili = false;
        String sql = "SELECT COUNT(*) FROM dbo.kullanicilar WHERE kullaniciAdi = ? AND sifre = ?";

        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, u);
            stmt.setString(2, p);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) girisBasarili = true;

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Veritabanı hatası: " + e.getMessage());
            return;
        }

        if (girisBasarili) {
            errorLabel.setText("");
            System.out.println("✅ Giriş başarılı: " + u);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/panel.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Lastik Depom - Panel");
                stage.centerOnScreen();
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("Panel yüklenirken hata oluştu.");
            }

        } else {
            errorLabel.setText("Hatalı kullanıcı adı veya şifre.");
        }
    }
}
