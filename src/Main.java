import database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Veritabanı bağlantısını başlat
            System.out.println("🚀 Program başlatılıyor...");
            DatabaseConnection.baglan();
            System.out.println("✅ Veritabanına bağlantı başarılı!");

            // Login ekranını yükle
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Login.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);
            stage.setTitle("Yılmaz & Ünal Oto Lastik - Giriş");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Program başlatılırken hata oluştu!");
        }
    }

    public static void main(String[] args) {
        launch(); // JavaFX uygulamasını başlatır
    }
}
