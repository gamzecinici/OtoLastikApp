package gui;

import database.DatabaseConnection;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.AlimGecmisi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Ürün alım geçmişini listeler.
 */
public class AlimGecmisiController {

    @FXML private TableView<AlimGecmisi> tableAlimGecmisi;
    @FXML private TableColumn<AlimGecmisi, String> colMarka;
    @FXML private TableColumn<AlimGecmisi, String> colModel;
    @FXML private TableColumn<AlimGecmisi, String> colAlimTarihi;
    @FXML private TableColumn<AlimGecmisi, Double> colAlisFiyati;
    @FXML private TableColumn<AlimGecmisi, Integer> colAlinanAdet;
    @FXML private TableColumn<AlimGecmisi, String> colAciklama;

    private final ObservableList<AlimGecmisi> alimListesi = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 🔹 Tablo sütunlarını model alanlarına bağla
        colMarka.setCellValueFactory(new PropertyValueFactory<>("marka"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colAlimTarihi.setCellValueFactory(new PropertyValueFactory<>("alimTarihi"));
        colAlisFiyati.setCellValueFactory(new PropertyValueFactory<>("alisFiyati"));
        colAlinanAdet.setCellValueFactory(new PropertyValueFactory<>("alinanAdet"));
        colAciklama.setCellValueFactory(new PropertyValueFactory<>("aciklama"));

        // 🔹 Verileri yükle
        verileriYukle();

        // 🔹 Ortak layout yenileme (tam ekran uyumlu)
        LayoutRefresher.refresh(tableAlimGecmisi);
    }

    /**
     * Veritabanından ürün alım geçmişi kayıtlarını çeker.
     */
    private void verileriYukle() {
        alimListesi.clear();

        String sql = """
            SELECT 
                m.markaAdi AS marka,
                u.model AS model,
                FORMAT(g.alimTarihi, 'dd.MM.yyyy HH:mm:ss') AS alimTarihi,
                g.alisFiyati,
                g.alinanAdet,
                g.aciklama
            FROM urunAlimGecmisi g
            JOIN urunler u ON g.urunId = u.id
            JOIN markalar m ON u.markaId = m.id
            ORDER BY g.alimTarihi DESC
        """;

        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AlimGecmisi a = new AlimGecmisi(
                        rs.getString("marka"),
                        rs.getString("model"),
                        rs.getString("alimTarihi"),
                        rs.getDouble("alisFiyati"),
                        rs.getInt("alinanAdet"),
                        rs.getString("aciklama")
                );
                alimListesi.add(a);
            }

            tableAlimGecmisi.setItems(alimListesi);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Geri dön butonu — Panel ekranına yönlendirir.
     */
    @FXML
    private void handleGeriDon() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/panel.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableAlimGecmisi.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Yılmaz & Ünal Oto Lastik - Panel");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
