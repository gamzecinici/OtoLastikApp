package gui;

import database.DatabaseConnection;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Ana panel ekranını yöneten controller sınıfı.
 * Tarih ve saat bilgisini gösterir, ayrıca stok, ekleme ve satış sayfalarına geçiş sağlar.
 */
public class PanelController {

    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private Label toplamUrunLabel;
    @FXML private Label toplamSatisLabel;
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

        // Veritabanından özet bilgileri çek
        veritabaniOzetleriniGetir();
    }

    /**
     * Veritabanından toplam ürün ve satış bilgilerini çeker.
     * (urunler, satislar tablolarına dayanır.)
     */
    private void veritabaniOzetleriniGetir() {
        String urunSorgu = "SELECT COUNT(*) AS toplamUrun FROM dbo.urunler WHERE aktif = 1";
        String satisSorgu = "SELECT COUNT(*) AS toplamSatis FROM dbo.satislar";

        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement stmt1 = conn.prepareStatement(urunSorgu);
             PreparedStatement stmt2 = conn.prepareStatement(satisSorgu)) {

            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                int toplamUrun = rs1.getInt("toplamUrun");
                toplamUrunLabel.setText("Toplam Ürün: " + toplamUrun);
            }

            ResultSet rs2 = stmt2.executeQuery();
            if (rs2.next()) {
                int toplamSatis = rs2.getInt("toplamSatis");
                toplamSatisLabel.setText("Toplam Satış: " + toplamSatis);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Veri Hatası");
            alert.setHeaderText("Veritabanı bağlantısı başarısız!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * "Lastikler" butonuna tıklanınca stok sayfasını açar.
     */
    @FXML
    private void handleLastikler() {
        sayfaGecisYap("/gui/Lastikler.fxml", "Stoktaki Lastikler");
    }

    /**
     * "Lastik Ekle" butonuna tıklanınca yeni lastik ekleme ekranına geçiş yapar.
     */
    @FXML
    private void handleLastikEkle() {
        sayfaGecisYap("/gui/LastikEkle.fxml", "Yeni Lastik Ekle");
    }

    /**
     * "Satışlar" butonuna tıklanınca satışlar sayfasına yönlendirir.
     */
    @FXML
    private void handleSatislar() {
        sayfaGecisYap("/gui/Satislar.fxml", "Satışlar");
    }

    /**
     * FXML sayfa geçişlerini tek yerden yöneten yardımcı fonksiyon.
     */
    private void sayfaGecisYap(String fxmlYolu, String baslik) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlYolu));
            Parent root = loader.load();

            Stage stage = (Stage) dateLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(baslik);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText(baslik + " sayfası açılamadı!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
