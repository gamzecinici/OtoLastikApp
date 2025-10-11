package gui;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.Lastik;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class LastiklerController {

    @FXML private TableView<Lastik> tableLastikler;
    @FXML private TableColumn<Lastik, String> colMarka;
    @FXML private TableColumn<Lastik, String> colTip;
    @FXML private TableColumn<Lastik, String> colEbat;
    @FXML private TableColumn<Lastik, Double> colAlis;
    @FXML private TableColumn<Lastik, Double> colSatis;
    @FXML private TableColumn<Lastik, Integer> colAdet;
    @FXML private TableColumn<Lastik, String> colTarih;

    private final ObservableList<Lastik> lastikListesi = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Tablo sütunlarını model sınıfındaki property’lerle eşleştir
        colMarka.setCellValueFactory(data -> data.getValue().markaProperty());
        colTip.setCellValueFactory(data -> data.getValue().tipProperty());
        colEbat.setCellValueFactory(data -> data.getValue().ebatProperty());
        colAlis.setCellValueFactory(data -> data.getValue().alisFiyatiProperty().asObject());
        colSatis.setCellValueFactory(data -> data.getValue().satisFiyatiProperty().asObject());
        colAdet.setCellValueFactory(data -> data.getValue().adetProperty().asObject());
        colTarih.setCellValueFactory(data -> data.getValue().tarihProperty());

        // Verileri yükle
        lastikleriYukle();
    }

    /**
     * Veritabanındaki tüm lastik kayıtlarını tabloya yükler.
     */
    private void lastikleriYukle() {
        lastikListesi.clear();
        String sql = "SELECT marka, tip, ebat, alis_fiyati, satis_fiyati, adet, tarih FROM Lastikler";

        try (Connection conn = DatabaseConnection.baglan();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lastikListesi.add(new Lastik(
                        rs.getString("marka"),
                        rs.getString("tip"),
                        rs.getString("ebat"),
                        rs.getDouble("alis_fiyati"),
                        rs.getDouble("satis_fiyati"),
                        rs.getInt("adet"),
                        rs.getString("tarih")
                ));
            }

            tableLastikler.setItems(lastikListesi);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Veriler yüklenirken hata oluştu!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * “Geri” butonuna basıldığında ana panele döner.
     */
    @FXML
    private void handleGeri() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/panel.fxml"));
            Stage stage = (Stage) tableLastikler.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Panele geri dönülürken hata oluştu!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
