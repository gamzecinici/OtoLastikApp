package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 💻 Uygulamanın ana giriş noktasıdır.
 * İlk olarak login.fxml yüklenir.
 */
public class MainApp extends Application {

    private static Stage primaryStage; // 🔹 tüm sahnelerde aynı stage kullanılacak

    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage; // 🔹 stage referansını sakla

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Yılmaz & Ünal Oto Lastik - Giriş");

            // 🔹 Pencere boyutu sınırları
            stage.setMinWidth(800);
            stage.setMinHeight(500);
            stage.setMaxWidth(1600);
            stage.setMaxHeight(1000);

            // 🔹 Başlangıç boyutu
            stage.setWidth(1100);
            stage.setHeight(700);

            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 🔄 Sahneler arasında geçiş yaparken aynı Stage'i kullanır.
     */
    public static void changeScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // 🔹 Aynı stage’i kullanıyoruz
            primaryStage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
