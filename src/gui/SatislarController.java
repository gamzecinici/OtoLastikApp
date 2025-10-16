package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Satis;

import java.io.IOException;

public class SatislarController {

    @FXML private TableView<Satis> tableSatislar;
    @FXML private TableColumn<Satis, Integer> colId;
    @FXML private TableColumn<Satis, String> colMarka;
    @FXML private TableColumn<Satis, String> colTip;
    @FXML private TableColumn<Satis, String> colEbat;
    @FXML private TableColumn<Satis, Double> colSatisFiyati;
    @FXML private TableColumn<Satis, Integer> colAdet;
    @FXML private TableColumn<Satis, String> colTarih;
    @FXML private TableColumn<Satis, String> colMusteri;

    private ObservableList<Satis> satisListesi = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Kolonları bağla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMarka.setCellValueFactory(new PropertyValueFactory<>("marka"));
        colTip.setCellValueFactory(new PropertyValueFactory<>("tip"));
        colEbat.setCellValueFactory(new PropertyValueFactory<>("ebat"));
        colSatisFiyati.setCellValueFactory(new PropertyValueFactory<>("satisFiyati"));
        colAdet.setCellValueFactory(new PropertyValueFactory<>("adet"));
        colTarih.setCellValueFactory(new PropertyValueFactory<>("tarih"));
        colMusteri.setCellValueFactory(new PropertyValueFactory<>("musteri"));

        // Verileri yükle
        verileriGetir();
    }

    private void verileriGetir() {
        // TODO: Burada veritabanından verileri çekip listeye ekleyeceğiz
        satisListesi.clear();
        // satisListesi.add(new Satis(1, "Michelin", "Yaz", "205/55R16", 2500, 2, "2025-10-16", "Ahmet Yılmaz"));
        tableSatislar.setItems(satisListesi);
    }

    @FXML
    private void handleYenile() {
        verileriGetir();
    }

    @FXML
    private void handleGeri() {
        try {
            // Panel ekranını yükle
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Panel.fxml"));
            Parent root = loader.load();

            // Mevcut sahneyi al
            Stage stage = (Stage) tableSatislar.getScene().getWindow();

            // Yeni sahneyi oluştur ve ayarla
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Yılmaz & Ünal Oto Lastik - Ana Panel");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            // Daha açıklayıcı hata mesajı
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Geri Dönüş Hatası");
            alert.setHeaderText("Ana panele geri dönülürken hata oluştu!");
            alert.setContentText("Panel.fxml dosyası yüklenemedi.\n\nDetay: " + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Bilinmeyen Hata");
            alert.setHeaderText("Beklenmeyen bir hata oluştu.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

}
