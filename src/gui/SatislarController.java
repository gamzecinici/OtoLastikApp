package gui;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Satis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Satış kayıtlarını listeler ve stokların güncel kalmasını sağlar.
 */
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
        // 🔹 Tablo sütunlarını model sınıfıyla eşleştir
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMarka.setCellValueFactory(new PropertyValueFactory<>("marka"));
        colTip.setCellValueFactory(new PropertyValueFactory<>("tip"));
        colEbat.setCellValueFactory(new PropertyValueFactory<>("ebat"));
        colSatisFiyati.setCellValueFactory(new PropertyValueFactory<>("satisFiyati"));
        colAdet.setCellValueFactory(new PropertyValueFactory<>("adet"));
        colTarih.setCellValueFactory(new PropertyValueFactory<>("tarih"));
        colMusteri.setCellValueFactory(new PropertyValueFactory<>("musteri"));

        verileriGetir(); // ilk açılışta listele
    }

    /**
     * Veritabanından satış kayıtlarını çeker ve tabloya yükler.
     */
    private void verileriGetir() {
        satisListesi.clear();

        String sql = """
                SELECT s.id,
                       m.markaAdi AS marka,
                       t.tip AS tip,
                       CONCAT(e.genislik, '/', e.yukseklik, ' R', e.jant) AS ebat,
                       u.satisFiyati,
                       s.satilanAdet AS adet,
                       FORMAT(s.tarih, 'dd.MM.yyyy HH:mm') AS tarih,
                       CONCAT(mus.adi, ' ', mus.soyadi) AS musteri
                FROM satislar s
                JOIN urunler u ON s.urunId = u.id
                JOIN markalar m ON u.markaId = m.id
                JOIN tipler t ON u.tipId = t.id
                JOIN ebatlar e ON u.ebatId = e.id
                JOIN musteriler mus ON s.musteriId = mus.id
                ORDER BY s.tarih DESC;
                """;

        try (Connection conn = DatabaseConnection.baglan();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                satisListesi.add(new Satis(
                        rs.getInt("id"),
                        rs.getString("marka"),
                        rs.getString("tip"),
                        rs.getString("ebat"),
                        rs.getDouble("satisFiyati"),
                        rs.getInt("adet"),
                        rs.getString("tarih"),
                        rs.getString("musteri")
                ));
            }

            tableSatislar.setItems(satisListesi);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Veri Hatası");
            alert.setHeaderText("Satış verileri yüklenemedi!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Yenile butonuna basıldığında verileri tekrar çeker.
     */
    @FXML
    private void handleYenile() {
        verileriGetir();
    }

    /**
     * Satış kaydı seçilirse ilgili ürünün stoktan düştüğünü kontrol eder,
     * stok hâlâ düşmemişse düzeltir.
     */
    @FXML
    private void handleStokGuncelle() {
        Satis secilen = tableSatislar.getSelectionModel().getSelectedItem();

        if (secilen == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Stok Güncelleme");
            alert.setHeaderText("Ürün Seçilmedi");
            alert.setContentText("Lütfen stok kontrolü yapılacak bir satış kaydı seçin.");
            alert.showAndWait();
            return;
        }

        try (Connection conn = DatabaseConnection.baglan()) {
            String kontrolSql = "SELECT adet FROM urunler WHERE id = (SELECT urunId FROM satislar WHERE id = ?)";
            PreparedStatement psKontrol = conn.prepareStatement(kontrolSql);
            psKontrol.setInt(1, secilen.getId());
            ResultSet rs = psKontrol.executeQuery();

            if (rs.next()) {
                int mevcutStok = rs.getInt("adet");
                if (mevcutStok >= secilen.getAdet()) {
                    // stok düşmemişse düzelt
                    String guncelleSql = "UPDATE urunler SET adet = adet - ?, guncellenmeTarihi = GETDATE() WHERE id = (SELECT urunId FROM satislar WHERE id = ?)";
                    PreparedStatement psUpdate = conn.prepareStatement(guncelleSql);
                    psUpdate.setInt(1, secilen.getAdet());
                    psUpdate.setInt(2, secilen.getId());
                    psUpdate.executeUpdate();

                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Stok Güncellendi");
                    info.setHeaderText(null);
                    info.setContentText("Satış sonrası stok miktarı düzeltildi.");
                    info.showAndWait();
                } else {
                    Alert warn = new Alert(Alert.AlertType.WARNING);
                    warn.setTitle("Stok Zaten Güncel");
                    warn.setHeaderText(null);
                    warn.setContentText("Bu satış zaten stoktan düşülmüş görünüyor.");
                    warn.showAndWait();
                }
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Stok güncelleme hatası!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Geri butonuna basıldığında ana panele döner.
     */
    @FXML
    private void handleGeri() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Panel.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tableSatislar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Yılmaz & Ünal Oto Lastik - Ana Panel");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Geri Dönüş Hatası");
            alert.setHeaderText("Ana panele geri dönülürken hata oluştu!");
            alert.setContentText("Panel.fxml yüklenemedi.\n\nDetay: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
