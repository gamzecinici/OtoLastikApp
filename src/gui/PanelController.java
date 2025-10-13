package gui;

import database.DatabaseConnection;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Uygulamanın ana panel ekranını yöneten controller sınıfıdır.
 * Tarih, saat ve veritabanı özet bilgilerini dinamik olarak gösterir.
 */
public class PanelController {

    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private Label toplamUrunLabel;
    @FXML private Label toplamSatisLabel;
    @FXML private ImageView logoView;

    @FXML
    public void initialize() {
        // 🔹 Logo yükle (giriş ekranıyla aynı görsel)
        try {
            String logoYolu = "C:/Users/Gamze/Desktop/lastikGUI/images/logo.png";
            FileInputStream input = new FileInputStream(logoYolu);
            Image logo = new Image(input);
            logoView.setImage(logo);

            // Fade efekti ile yavaşça görünmesini sağla
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), logoView);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } catch (Exception e) {
            System.err.println("⚠️ Logo yüklenemedi: " + e.getMessage());
        }

        // 🔹 Saat ve tarih güncellemesi (her saniye)
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime now = LocalDateTime.now();
            dateLabel.setText(now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            timeLabel.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        // 🔹 Veritabanından özet bilgileri çek
        veritabaniOzetleriniGetir();
    }

    /**
     * Veritabanından toplam ürün ve satış sayılarını çeker ve ekrana yazar.
     */
    private void veritabaniOzetleriniGetir() {
        String urunSorgu = "SELECT COUNT(*) AS toplamUrun FROM dbo.urunler WHERE aktif = 1";
        String satisSorgu = "SELECT COUNT(*) AS toplamSatis FROM dbo.satislar";

        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement stmt1 = conn.prepareStatement(urunSorgu);
             PreparedStatement stmt2 = conn.prepareStatement(satisSorgu)) {

            // Ürün sayısı
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                int toplamUrun = rs1.getInt("toplamUrun");
                toplamUrunLabel.setText("📦 Ürünler: " + toplamUrun);
            }

            // Satış sayısı
            ResultSet rs2 = stmt2.executeQuery();
            if (rs2.next()) {
                int toplamSatis = rs2.getInt("toplamSatis");
                toplamSatisLabel.setText("💰 Satışlar: " + toplamSatis);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Veritabanı Hatası");
            alert.setHeaderText("Veritabanı bağlantısı başarısız!");
            alert.setContentText("Bağlantı kurulamadı: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // ================== SAYFA GEÇİŞLERİ ==================

    @FXML
    private void handleLastikler() {
        sayfaGecisYap("/gui/Lastikler.fxml", "Ürünler");
    }

    @FXML
    private void handleLastikEkle() {
        sayfaGecisYap("/gui/LastikEkle.fxml", "Yeni Ürün Ekle");
    }

    @FXML
    private void handleSatislar() {
        sayfaGecisYap("/gui/Satislar.fxml", "Satışlar");
    }

    /**
     * FXML sayfaları arasında geçişleri yöneten fonksiyon.
     * Yeni sahneye geçmeden önce yumuşak bir fade efekti uygular.
     */
    private void sayfaGecisYap(String fxmlYolu, String baslik) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlYolu));
            Parent yeniRoot = loader.load();

            Stage stage = (Stage) dateLabel.getScene().getWindow();

            // Yeni sahneyi oluştur
            Scene yeniSahne = new Scene(yeniRoot);
            stage.setScene(yeniSahne);
            stage.setTitle("Yılmaz & Ünal Oto Lastik - " + baslik);
            stage.centerOnScreen();

            // Fade geçiş efekti
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.6), yeniRoot);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Sayfa Yüklenemedi");
            alert.setHeaderText(baslik + " ekranı açılamadı!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCikisYap() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) dateLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Yılmaz & Ünal Oto Lastik - Giriş");
            stage.centerOnScreen();

            // Fade efekti (yumuşak geçiş)
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.6), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Çıkış Hatası");
            alert.setHeaderText("Giriş ekranı açılamadı!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

}
