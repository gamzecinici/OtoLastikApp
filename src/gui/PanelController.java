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
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PanelController {

    @FXML private Label dateLabel, timeLabel;
    @FXML private Label urunCesidiLabel, toplamUrunLabel, toplamSatisLabel, toplamAlisLabel;
    @FXML private ImageView logoView;

    private final DecimalFormat paraFormat = new DecimalFormat("#,##0.00");

    @FXML
    public void initialize() {
        logoYukle();
        tarihSaatGuncelle();
        veritabaniOzetleriniGetir();
    }

    // 🔹 Logo yükleme
    private void logoYukle() {
        try {
            String logoYolu = "C:/Users/Gamze/Desktop/lastikGUI/images/logo.png";
            Image logo = new Image(new FileInputStream(logoYolu));
            logoView.setImage(logo);

            FadeTransition fade = new FadeTransition(Duration.seconds(1.5), logoView);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        } catch (Exception e) {
            System.err.println("⚠️ Logo yüklenemedi: " + e.getMessage());
        }
    }

    // 🔹 Tarih & saat güncelleme
    private void tarihSaatGuncelle() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime now = LocalDateTime.now();
            dateLabel.setText(now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            timeLabel.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    // 🔹 Veritabanı özet bilgilerini çekme
    private void veritabaniOzetleriniGetir() {

        String sqlCesit = "SELECT COUNT(DISTINCT model) AS urunCesidi FROM urunler WHERE aktif = 1";
        String sqlUrun = "SELECT COALESCE(SUM(adet), 0) AS toplamUrun FROM urunler WHERE aktif = 1";
        String sqlSatis = "SELECT COALESCE(SUM(alinacakTutar), 0) AS toplamSatis FROM satislar";
        String sqlAlis = "SELECT COALESCE(SUM(alisFiyati * adet), 0) AS toplamAlis FROM urunler WHERE aktif = 1";

        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement stmtCesit = conn.prepareStatement(sqlCesit);
             PreparedStatement stmtUrun = conn.prepareStatement(sqlUrun);
             PreparedStatement stmtSatis = conn.prepareStatement(sqlSatis);
             PreparedStatement stmtAlis = conn.prepareStatement(sqlAlis)) {

            ResultSet rsCesit = stmtCesit.executeQuery();
            if (rsCesit.next())
                urunCesidiLabel.setText(String.valueOf(rsCesit.getInt("urunCesidi")));

            ResultSet rsUrun = stmtUrun.executeQuery();
            if (rsUrun.next())
                toplamUrunLabel.setText(String.valueOf(rsUrun.getInt("toplamUrun")));

            ResultSet rsSatis = stmtSatis.executeQuery();
            if (rsSatis.next()) {
                double satis = rsSatis.getDouble("toplamSatis");
                toplamSatisLabel.setText("₺" + paraFormat.format(satis));
                System.out.println("💰 Toplam Satış: " + satis);
            }

            ResultSet rsAlis = stmtAlis.executeQuery();
            if (rsAlis.next()) {
                double alis = rsAlis.getDouble("toplamAlis");
                toplamAlisLabel.setText("₺" + paraFormat.format(alis));
                System.out.println("💸 Toplam Alış: " + alis);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Veritabanı Hatası");
            alert.setHeaderText("Veri çekme hatası!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // === Sayfa geçişleri ===
    @FXML private void handleLastikler() { sayfaGecis("/gui/Lastikler.fxml", "Ürünler"); }
    @FXML private void handleLastikEkle() { sayfaGecis("/gui/LastikEkle.fxml", "Yeni Ürün Ekle"); }
    @FXML private void handleSatislar() { sayfaGecis("/gui/Satislar.fxml", "Satışlar"); }
    @FXML private void handleMusteriler() { sayfaGecis("/gui/Musteriler.fxml", "Müşteriler"); }

    // 🆕 Yeni sayfa geçişi: Alım Geçmişi
    @FXML private void handleAlimGecmisi() { sayfaGecis("/gui/AlimGecmisi.fxml", "Alım Geçmişi"); }

    // 🔁 Sayfa geçiş metodu (hepsi için ortak)
    private void sayfaGecis(String fxml, String baslik) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) dateLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Yılmaz & Ünal Oto Lastik - " + baslik);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔚 Çıkış işlemi
    @FXML
    private void handleCikisYap() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) dateLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Yılmaz & Ünal Oto Lastik - Giriş");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
