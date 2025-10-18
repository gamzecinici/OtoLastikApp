package gui;

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Lastik;
import org.controlsfx.control.table.TableFilter;

import java.sql.*;

/**
 * Stoktaki lastikleri gösterir ve stok / satış / iade işlemlerini yönetir.
 */
public class LastiklerController {

    @FXML private TableView<Lastik> tableLastikler;
    @FXML private TableColumn<Lastik, String> colMarka;
    @FXML private TableColumn<Lastik, String> colTip;
    @FXML private TableColumn<Lastik, String> colEbat;
    @FXML private TableColumn<Lastik, String> colHiz;
    @FXML private TableColumn<Lastik, String> colYuk;
    @FXML private TableColumn<Lastik, Double> colAlis;
    @FXML private TableColumn<Lastik, Double> colSatis;
    @FXML private TableColumn<Lastik, Integer> colAdet;
    @FXML private TableColumn<Lastik, String> colTarih;

    private final ObservableList<Lastik> lastikListesi = FXCollections.observableArrayList();

    // ======================================================
    //  BAŞLATMA
    // ======================================================
    @FXML
    public void initialize() {
        colMarka.setCellValueFactory(data -> data.getValue().markaProperty());
        colTip.setCellValueFactory(data -> data.getValue().tipProperty());
        colEbat.setCellValueFactory(data -> data.getValue().ebatProperty());
        colHiz.setCellValueFactory(data -> data.getValue().hizProperty());
        colYuk.setCellValueFactory(data -> data.getValue().yukProperty());
        colAlis.setCellValueFactory(data -> data.getValue().alisFiyatiProperty().asObject());
        colSatis.setCellValueFactory(data -> data.getValue().satisFiyatiProperty().asObject());
        colAdet.setCellValueFactory(data -> data.getValue().adetProperty().asObject());
        colTarih.setCellValueFactory(data -> data.getValue().tarihProperty());

        lastikleriYukle();
        Platform.runLater(() -> TableFilter.forTableView(tableLastikler).apply());
    }

    // ======================================================
    //  LİSTEYİ YÜKLE
    // ======================================================
    private void lastikleriYukle() {
        lastikListesi.clear();

        String sql = """
                SELECT
                    u.id,
                    m.markaAdi AS marka,
                    t.tip AS tip,
                    CONCAT(e.genislik, '/', e.yukseklik, ' R', e.jant) AS ebat,
                    h.hizEndeks AS hiz,
                    y.yukEndeks AS yuk,
                    u.alisFiyati,
                    u.satisFiyati,
                    u.adet,
                    FORMAT(u.eklenmeTarihi, 'dd.MM.yyyy') AS tarih
                FROM urunler u
                JOIN markalar m ON u.markaId = m.id
                JOIN tipler t ON u.tipId = t.id
                JOIN ebatlar e ON u.ebatId = e.id
                JOIN hizEndeksleri h ON u.hizEndeksId = h.id
                JOIN yukEndeksleri y ON u.yukEndeksId = y.id
                WHERE u.aktif = 1
                ORDER BY u.eklenmeTarihi DESC;
                """;

        try (Connection conn = DatabaseConnection.baglan();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lastikListesi.add(new Lastik(
                        rs.getInt("id"),
                        rs.getString("marka"),
                        rs.getString("tip"),
                        rs.getString("ebat"),
                        rs.getString("hiz"),
                        rs.getString("yuk"),
                        rs.getDouble("alisFiyati"),
                        rs.getDouble("satisFiyati"),
                        rs.getInt("adet"),
                        rs.getString("tarih")
                ));
            }

            tableLastikler.setItems(lastikListesi);

        } catch (Exception e) {
            hataMesaji("Veriler yüklenirken hata oluştu:\n" + e.getMessage());
        }
    }

    // ======================================================
    //  STOK ARTIR
    // ======================================================
    @FXML
    private void handleStokArtir() {
        Lastik secilen = tableLastikler.getSelectionModel().getSelectedItem();
        if (secilen == null) {
            showWarning("Stok Artırma", "Lütfen önce bir ürün seçin.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Stok Artır");
        dialog.setHeaderText("Seçilen ürün: " + secilen.getMarka() + " " + secilen.getTip());
        dialog.setContentText("Eklenecek adet miktarını girin:");

        dialog.showAndWait().ifPresent(giris -> {
            try {
                int eklenecek = Integer.parseInt(giris);
                if (eklenecek <= 0) throw new NumberFormatException();

                String sql = "UPDATE urunler SET adet = adet + ?, guncellenmeTarihi = GETDATE() WHERE id = ?";
                try (Connection conn = DatabaseConnection.baglan();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, eklenecek);
                    ps.setInt(2, secilen.getId());
                    ps.executeUpdate();
                }

                bilgiMesaji("Stok " + eklenecek + " adet artırıldı!");
                lastikleriYukle();

            } catch (Exception e) {
                hataMesaji("Stok artırılırken hata oluştu:\n" + e.getMessage());
            }
        });
    }

    // ======================================================
    //  SATIŞ YAP
    // ======================================================
    @FXML
    private void handleSatisYap() {
        Lastik seciliLastik = tableLastikler.getSelectionModel().getSelectedItem();
        if (seciliLastik == null) {
            showWarning("Ürün Seçilmedi", "Lütfen satış yapmak için bir ürün seçin.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Satış İşlemi");
        dialog.setHeaderText("Seçilen Ürün: " + seciliLastik.getMarka() + " - " + seciliLastik.getTip());

        ButtonType btnKaydet = new ButtonType("Satışı Kaydet", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnIptal = new ButtonType("İptal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnKaydet, btnIptal);

        TextField txtMusteri = new TextField();
        txtMusteri.setPromptText("Müşteri adı soyadı...");

        TextField txtAdet = new TextField();
        txtAdet.setPromptText("Satış adedi...");

        TextField txtAlinan = new TextField();
        txtAlinan.setPromptText("Alınan tutar ₺ (boş bırakılabilir)");

        Label lblBirim = new Label("Birim fiyat: " + seciliLastik.getSatisFiyati() + " ₺");
        Label lblStok = new Label("Stokta: " + seciliLastik.getAdet() + " adet");
        Label lblToplam = new Label("Toplam: 0 ₺");

        VBox vbox = new VBox(10, lblStok,
                new Label("Müşteri Adı Soyadı:"), txtMusteri,
                new Label("Satış Adedi:"), txtAdet,
                lblBirim, lblToplam,
                new Label("Müşteriden Alınan (₺):"), txtAlinan);
        vbox.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(vbox);

        // Toplam hesaplama
        txtAdet.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int adet = Integer.parseInt(newVal);
                if (adet > seciliLastik.getAdet()) {
                    lblToplam.setText("Yetersiz stok!");
                    lblToplam.setStyle("-fx-text-fill: red;");
                } else {
                    double toplam = adet * seciliLastik.getSatisFiyati();
                    lblToplam.setText(String.format("Toplam: %.2f ₺", toplam));
                    lblToplam.setStyle("-fx-text-fill: white;");
                }
            } catch (Exception e) {
                lblToplam.setText("Toplam: 0 ₺");
            }
        });

        dialog.setResultConverter(button -> {
            if (button == btnKaydet) {
                try {
                    String musteriAdi = txtMusteri.getText().trim();
                    int adet = Integer.parseInt(txtAdet.getText().trim());
                    double alinan = txtAlinan.getText().isEmpty() ? 0 : Double.parseDouble(txtAlinan.getText().replace(",", "."));
                    double toplam = adet * seciliLastik.getSatisFiyati();

                    if (musteriAdi.isEmpty()) {
                        showWarning("Eksik Bilgi", "Lütfen müşteri adını girin!");
                        return null;
                    }
                    if (adet <= 0 || adet > seciliLastik.getAdet()) {
                        showWarning("Hatalı Adet", "Geçerli bir satış adedi girin!");
                        return null;
                    }

                    satisKaydet(seciliLastik, musteriAdi, adet, toplam, alinan);
                    bilgiMesaji("Satış başarıyla kaydedildi!");
                    lastikleriYukle();

                } catch (Exception e) {
                    hataMesaji("Satış kaydedilirken hata oluştu:\n" + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Satışı veritabanına kaydeder (stok düşümü SQL tarafında yapılır).
     */
    private void satisKaydet(Lastik lastik, String musteriAdi, int adet, double toplam, double alinan) {
        try (Connection conn = DatabaseConnection.baglan()) {
            conn.setAutoCommit(false);
            int musteriId;

            // 🔹 Ad soyad güvenli şekilde ayır
            String temizAdSoyad = musteriAdi == null ? "" : musteriAdi.trim();
            if (temizAdSoyad.isEmpty()) {
                showWarning("Eksik Bilgi", "Lütfen müşteri adını girin!");
                return;
            }

            String[] parcalar = temizAdSoyad.split("\\s+", 2);
            String ad = parcalar[0].trim();
            String soyad = (parcalar.length > 1) ? parcalar[1].trim() : "Bilinmiyor";

            // 🔹 Var mı kontrol et (case-insensitive)
            String checkSql = """
            SELECT id FROM musteriler
            WHERE LOWER(LTRIM(RTRIM(adi))) = LOWER(?)
              AND LOWER(LTRIM(RTRIM(soyadi))) = LOWER(?)
        """;
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setString(1, ad);
            psCheck.setString(2, soyad);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                // ✅ Aynı müşteri varsa mevcut ID alınır
                musteriId = rs.getInt("id");
            } else {
                // ✅ Yeni müşteri eklenir
                String insertMusteri = """
                INSERT INTO musteriler (adi, soyadi, kayitTarihi, borc)
                VALUES (?, ?, GETDATE(), 0)
            """;
                PreparedStatement psInsert = conn.prepareStatement(insertMusteri, Statement.RETURN_GENERATED_KEYS);
                psInsert.setString(1, ad);
                psInsert.setString(2, soyad);
                psInsert.executeUpdate();

                ResultSet gen = psInsert.getGeneratedKeys();
                gen.next();
                musteriId = gen.getInt(1);
            }

            // 🔹 Satış kaydı
            String insertSatis = """
            INSERT INTO satislar (urunId, musteriId, satilanAdet, tarih, alinacakTutar, alinanTutar, odendi)
            VALUES (?, ?, ?, GETDATE(), ?, ?, ?)
        """;
            PreparedStatement ps = conn.prepareStatement(insertSatis);
            ps.setInt(1, lastik.getId());
            ps.setInt(2, musteriId);
            ps.setInt(3, adet);
            ps.setDouble(4, toplam);
            ps.setDouble(5, alinan);
            ps.setBoolean(6, alinan >= toplam);
            ps.executeUpdate();

            // 🔻 Stok azalt
            String stokSql = "UPDATE urunler SET adet = adet - ?, guncellenmeTarihi = GETDATE() WHERE id = ?";
            PreparedStatement psStok = conn.prepareStatement(stokSql);
            psStok.setInt(1, adet);
            psStok.setInt(2, lastik.getId());
            psStok.executeUpdate();

            conn.commit();

        } catch (Exception e) {
            hataMesaji("Satış işlemi sırasında hata oluştu:\n" + e.getMessage());
        }
    }


    // ======================================================
    //  İADE ET
    // ======================================================
    @FXML
    private void handleIadeEt() { // 🔹 Bu metot artık FXML'de tanımlı!
        Lastik secilen = tableLastikler.getSelectionModel().getSelectedItem();
        if (secilen == null) {
            showWarning("İade İşlemi", "Lütfen önce bir ürün seçin.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("İade Et");
        dialog.setHeaderText("Seçilen ürün: " + secilen.getMarka() + " " + secilen.getTip());
        dialog.setContentText("İade edilecek adet miktarını girin:");

        dialog.showAndWait().ifPresent(giris -> {
            try {
                int iade = Integer.parseInt(giris);
                if (iade <= 0) throw new NumberFormatException();
                if (iade > secilen.getAdet()) {
                    showWarning("Geçersiz Miktar", "İade miktarı stoktan fazla olamaz!");
                    return;
                }

                String sql = "UPDATE urunler SET adet = adet - ?, guncellenmeTarihi = GETDATE() WHERE id = ?";
                try (Connection conn = DatabaseConnection.baglan();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, iade);
                    ps.setInt(2, secilen.getId());
                    ps.executeUpdate();
                }

                bilgiMesaji("Stoktan " + iade + " adet iade edildi!");
                lastikleriYukle();

            } catch (Exception e) {
                hataMesaji("İade sırasında hata oluştu:\n" + e.getMessage());
            }
        });
    }

    // ======================================================
    //  GERİ DÖN
    // ======================================================
    @FXML
    private void handleGeri() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/Panel.fxml"));
            Stage stage = (Stage) tableLastikler.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Yılmaz & Ünal Oto Lastik - Ana Panel");
        } catch (Exception e) {
            hataMesaji("Panele geri dönülürken hata oluştu:\n" + e.getMessage());
        }
    }

    // ======================================================
    //  MESAJLAR
    // ======================================================
    private void showWarning(String baslik, String mesaj) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(baslik);
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }

    private void bilgiMesaji(String mesaj) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Bilgi");
        info.setHeaderText(null);
        info.setContentText(mesaj);
        info.showAndWait();
    }

    private void hataMesaji(String mesaj) {
        Alert err = new Alert(Alert.AlertType.ERROR);
        err.setTitle("Hata");
        err.setHeaderText(null);
        err.setContentText(mesaj);
        err.showAndWait();
    }
}
