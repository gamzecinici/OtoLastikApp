package gui;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String u = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String p = passwordField.getText() == null ? "" : passwordField.getText();

        if (u.isEmpty() || p.isEmpty()) {
            errorLabel.setText("Kullanıcı adı ve şifre zorunludur.");
            return;
        }

        boolean ok = false;

        String sql = "SELECT COUNT(*) FROM dbo.kullanicilar WHERE kullaniciAdi = ? AND sifre = ?";

        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, u);
            stmt.setString(2, p);

            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                ok = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Veritabanı hatası: " + e.getMessage());
            return;
        }

        if (ok) {
            errorLabel.setText("");
            System.out.println("✅ Giriş başarılı: " + u);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/panel.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Lastikçi Paneli");
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
