package gui;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Satis;

import java.io.IOException;
import java.sql.*;

/**
 * Satış kayıtlarını listeler, ödeme durumlarını gösterir ve güncelleme / iptal işlemlerini yönetir.
 */
public class SatislarController {

    @FXML private TableView<Satis> tableSatislar;

    @FXML private TableColumn<Satis, String> colMarka;
    @FXML private TableColumn<Satis, String> colModel;
    @FXML private TableColumn<Satis, String> colEbat;
    @FXML private TableColumn<Satis, String> colHiz;
    @FXML private TableColumn<Satis, String> colYuk;
    @FXML private TableColumn<Satis, String> colMusteri;
    @FXML private TableColumn<Satis, String> colTelefon;
    @FXML private TableColumn<Satis, Integer> colAdet;
    @FXML private TableColumn<Satis, Double> colAlinacak;
    @FXML private TableColumn<Satis, Double> colAlinan;
    @FXML private TableColumn<Satis, Double> colKalan;
    @FXML private TableColumn<Satis, String> colTarih;
    @FXML private TableColumn<Satis, Boolean> colOdendi;

    private ObservableList<Satis> satisListesi = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 🔹 Tablo sütunlarını model sınıfıyla eşleştir
        colMarka.setCellValueFactory(new PropertyValueFactory<>("marka"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colEbat.setCellValueFactory(new PropertyValueFactory<>("ebat"));
        colHiz.setCellValueFactory(new PropertyValueFactory<>("hizEndeksi"));
        colYuk.setCellValueFactory(new PropertyValueFactory<>("yukEndeksi"));
        colMusteri.setCellValueFactory(new PropertyValueFactory<>("musteriAdiSoyadi"));
        colTelefon.setCellValueFactory(new PropertyValueFactory<>("musteriTelefon"));
        colAdet.setCellValueFactory(new PropertyValueFactory<>("satilanAdet"));
        colAlinacak.setCellValueFactory(new PropertyValueFactory<>("alinacakTutar"));
        colAlinan.setCellValueFactory(new PropertyValueFactory<>("alinanTutar"));
        colKalan.setCellValueFactory(new PropertyValueFactory<>("kalanTutar"));
        colTarih.setCellValueFactory(new PropertyValueFactory<>("tarih"));

        colOdendi.setCellValueFactory(new PropertyValueFactory<>("odendi"));
        colOdendi.setCellFactory(tc -> new CheckBoxTableCell<>());

        verileriGetir();
    }

    /**
     * Veritabanından satış kayıtlarını çeker ve tabloya yükler.
     */
    private void verileriGetir() {
        satisListesi.clear();

        String sql = """
                SELECT\s
                    s.id,
                    ma.markaAdi AS marka,
                    u.model,
                    CONCAT(e.genislik, '/', e.yukseklik, ' R', e.jant) AS ebat,
                    hizEndeks AS hizEndeksi,
                    yukEndeks AS yukEndeksi,
                    t.tip AS tip,
                    m.adi + ' ' + m.soyadi AS musteriAdiSoyadi,
                    m.telefon,
                    s.satilanAdet,
                    s.alinacakTutar,
                    s.alinanTutar,
                    (s.alinacakTutar - ISNULL(s.alinanTutar, 0)) AS kalan,
                    FORMAT(s.tarih, 'dd.MM.yyyy HH:mm') AS tarih,
                    s.odendi
                FROM satislar s
                JOIN urunler u ON s.urunId = u.id
                JOIN markalar ma ON u.markaId = ma.id
                JOIN tipler t ON u.tipId = t.id
                JOIN ebatlar e ON u.ebatId = e.id
                JOIN hizEndeksleri h ON u.hizEndeksId = h.id
                JOIN yukEndeksleri y ON u.yukEndeksId = y.id
                JOIN musteriler m ON s.musteriId = m.id
                ORDER BY s.tarih DESC;
                """;

        try (Connection conn = DatabaseConnection.baglan();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                satisListesi.add(new Satis(
                        rs.getLong("id"),
                        rs.getString("marka"),
                        rs.getString("model"),
                        rs.getString("ebat"),
                        rs.getString("hizEndeksi"),
                        rs.getString("yukEndeksi"),
                        rs.getString("musteriAdiSoyadi"),
                        rs.getString("telefon"),
                        rs.getInt("satilanAdet"),
                        rs.getDouble("alinacakTutar"),
                        rs.getDouble("alinanTutar"),
                        rs.getDouble("kalan"),
                        rs.getString("tarih"),
                        rs.getBoolean("odendi")
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
     * Satış kaydının tutar veya ödeme bilgisini güncelleme.
     */
    @FXML
    private void handleSatisGuncelle() {
        Satis secilen = tableSatislar.getSelectionModel().getSelectedItem();

        if (secilen == null) {
            uyari("Satış Güncelleme", "Lütfen güncellenecek bir satış seçin.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(secilen.getAlinanTutar()));
        dialog.setTitle("Satış Güncelleme");
        dialog.setHeaderText("Müşteri: " + secilen.getMusteriAdiSoyadi());
        dialog.setContentText("Yeni alınan tutarı girin (₺):");

        dialog.showAndWait().ifPresent(deger -> {
            try {
                double yeniAlinan = Double.parseDouble(deger);
                boolean odendi = yeniAlinan >= secilen.getAlinacakTutar();

                try (Connection conn = DatabaseConnection.baglan()) {
                    String updateSql = "UPDATE satislar SET alinanTutar = ?, odendi = ? WHERE id = ?";
                    PreparedStatement ps = conn.prepareStatement(updateSql);
                    ps.setDouble(1, yeniAlinan);
                    ps.setBoolean(2, odendi);
                    ps.setLong(3, secilen.getId());
                    ps.executeUpdate();
                }

                bilgi("Başarılı", "Satış bilgisi güncellendi!");
                verileriGetir();

            } catch (NumberFormatException e) {
                uyari("Geçersiz Giriş", "Lütfen geçerli bir sayı girin.");
            } catch (SQLException e) {
                hata("Güncelleme Hatası", e.getMessage());
            }
        });
    }

    /**
     * Seçilen satışı iptal eder (veritabanından siler).
     */
    @FXML
    private void handleSatisIptal() {
        Satis secilen = tableSatislar.getSelectionModel().getSelectedItem();

        if (secilen == null) {
            uyari("Satış İptali", "Lütfen iptal edilecek bir satış seçin.");
            return;
        }

        Alert onay = new Alert(Alert.AlertType.CONFIRMATION);
        onay.setTitle("Satış İptali");
        onay.setHeaderText("Satış silinecek!");
        onay.setContentText(secilen.getMusteriAdiSoyadi() + " adlı müşterinin satışı iptal edilsin mi?");
        if (onay.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
            return;

        try (Connection conn = DatabaseConnection.baglan()) {
            String silSql = "DELETE FROM satislar WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(silSql);
            ps.setLong(1, secilen.getId());
            ps.executeUpdate();

            bilgi("Satış Silindi", "Satış başarıyla iptal edildi!");
            verileriGetir();

        } catch (SQLException e) {
            hata("Silme Hatası", e.getMessage());
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
            hata("Geri Dönüş Hatası", "Panel.fxml yüklenemedi!\n" + e.getMessage());
        }
    }

    // 🔸 Yardımcı metodlar
    private void bilgi(String baslik, String icerik) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(baslik);
        alert.setHeaderText(null);
        alert.setContentText(icerik);
        alert.showAndWait();
    }

    private void uyari(String baslik, String icerik) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(baslik);
        alert.setHeaderText(null);
        alert.setContentText(icerik);
        alert.showAndWait();
    }

    private void hata(String baslik, String icerik) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(baslik);
        alert.setHeaderText(null);
        alert.setContentText(icerik);
        alert.showAndWait();
    }
}
