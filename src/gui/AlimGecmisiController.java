package gui;

import database.DatabaseConnection;
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
import model.AlimGecmisi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AlimGecmisiController {

    @FXML private TableView<AlimGecmisi> tableAlimGecmisi;
    @FXML private TableColumn<AlimGecmisi, Integer> colUrunId;
    @FXML private TableColumn<AlimGecmisi, String> colAlimTarihi;
    @FXML private TableColumn<AlimGecmisi, Double> colAlisFiyati;
    @FXML private TableColumn<AlimGecmisi, Integer> colAlinanAdet;
    @FXML private TableColumn<AlimGecmisi, String> colAciklama;

    private ObservableList<AlimGecmisi> alimListesi = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Sütunları bağla
        colUrunId.setCellValueFactory(new PropertyValueFactory<>("urunId"));
        colAlimTarihi.setCellValueFactory(new PropertyValueFactory<>("alimTarihi"));
        colAlisFiyati.setCellValueFactory(new PropertyValueFactory<>("alisFiyati"));
        colAlinanAdet.setCellValueFactory(new PropertyValueFactory<>("alinanAdet"));
        colAciklama.setCellValueFactory(new PropertyValueFactory<>("aciklama"));

        verileriYukle();
    }

    private void verileriYukle() {
        alimListesi.clear();
        String sql = "SELECT urunId, alimTarihi, alisFiyati, alinanAdet, aciklama FROM urunAlimGecmisi ORDER BY alimTarihi DESC";

        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AlimGecmisi a = new AlimGecmisi(
                        rs.getInt("urunId"),
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

    // Geri dön butonu
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
