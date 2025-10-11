package gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PanelController {

    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private ImageView logoView;

    @FXML
    public void initialize() {
        // Tarih ve saat animasyonu
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime now = LocalDateTime.now();
            dateLabel.setText(now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            timeLabel.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    /**
     * "Lastikler" butonuna tıklanınca stok sayfasını açar.
     * Yeni pencere oluşturmaz, mevcut sahne içinde geçiş yapar.
     */
    @FXML
    private void handleLastikler() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Lastikler.fxml"));
            Parent root = loader.load();

            // Mevcut sahneyi al ve yeni görünümü yerleştir
            Stage stage = (Stage) dateLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Lastikler sayfası açılamadı!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * "Lastik Ekle" butonuna tıklanınca yeni lastik ekleme ekranına geçiş yapar.
     */
    @FXML
    private void handleLastikEkle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/LastikEkle.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) timeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Lastik ekleme sayfası açılamadı!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * "Satışlar" butonuna tıklanınca satışlar sayfasına yönlendirir.
     * (Henüz aktif değilse ileride eklenecek şekilde hazırlanmıştır.)
     */
    @FXML
    private void handleSatislar() {
        try {
            // FXML dosyasını yükle
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Satislar.fxml"));
            Parent root = loader.load();

            // Mevcut pencereyi al
            Stage stage = (Stage) dateLabel.getScene().getWindow();

            // Yeni sahneyi ayarla
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Satışlar"); // Pencere başlığı
            stage.show();

            System.out.println("✅ Satışlar sayfası başarıyla açıldı.");

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Satışlar sayfası açılamadı!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

}
