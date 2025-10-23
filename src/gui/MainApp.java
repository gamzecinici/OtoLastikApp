package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Yılmaz & Ünal Oto Lastik - Giriş");

            // 🔹 Tam ekran tamamen kapalı
            stage.setFullScreen(false);
            stage.setFullScreenExitHint("");      // Alt+Enter ipucunu da kapatır
            stage.setFullScreenExitKeyCombination(null);

            stage.setWidth(1000);
            stage.setHeight(720);
            stage.setMinWidth(900);
            stage.setMinHeight(600);

            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
