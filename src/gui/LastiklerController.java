package gui;

import database.DatabaseConnection;
import database.DatabaseFunctions;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.Lastik;
import model.MusteriLite;
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
        // 🔹 Tablo sütunlarını model property’leriyle eşleştir
        colMarka.setCellValueFactory(data -> data.getValue().markaProperty());
        colTip.setCellValueFactory(data -> data.getValue().tipProperty());
        colEbat.setCellValueFactory(data -> data.getValue().ebatProperty());
        colHiz.setCellValueFactory(data -> data.getValue().hizProperty());
        colYuk.setCellValueFactory(data -> data.getValue().yukProperty());
        colAlis.setCellValueFactory(data -> data.getValue().alisFiyatiProperty().asObject());
        colSatis.setCellValueFactory(data -> data.getValue().satisFiyatiProperty().asObject());
        colAdet.setCellValueFactory(data -> data.getValue().adetProperty().asObject());
        colTarih.setCellValueFactory(data -> data.getValue().tarihProperty());

        // 🔹 Tablo verilerini yükle
        lastikleriYukle();

        // 🔹 Filtre uygula
        Platform.runLater(() -> TableFilter.forTableView(tableLastikler).apply());

        // 🔹 Ortak layout yenileme (tam ekran uyumlu)
        LayoutRefresher.refresh(tableLastikler);
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
    private void handleUrunGuncelle() {
        Lastik secilen = tableLastikler.getSelectionModel().getSelectedItem();
        if (secilen == null) {
            showWarning("Ürün Güncelleme", "Lütfen önce bir ürün seçin.");
            return;
        }

        Dialog<Pair<String[], String>> dialog = new Dialog<>();
        dialog.setTitle("Ürün Güncelle");
        dialog.setHeaderText("Seçilen ürün: " + secilen.getMarka() + " " + secilen.getTip());

        ButtonType okButtonType = new ButtonType("Kaydet", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField adetField = new TextField();
        adetField.setPromptText("Yeni adet (isteğe bağlı)");

        TextField alisField = new TextField();
        alisField.setPromptText("Yeni alış fiyatı (isteğe bağlı)");

        TextField satisField = new TextField();
        satisField.setPromptText("Yeni satış fiyatı (isteğe bağlı)");

        grid.add(new Label("Yeni adet:"), 0, 0);
        grid.add(adetField, 1, 0);
        grid.add(new Label("Yeni alış fiyatı:"), 0, 1);
        grid.add(alisField, 1, 1);
        grid.add(new Label("Yeni satış fiyatı:"), 0, 2);
        grid.add(satisField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(adetField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Pair<>(new String[]{adetField.getText(), alisField.getText(), satisField.getText()}, "");
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String[] values = result.getKey();
            String adetStr = values[0].trim();
            String alisStr = values[1].trim();
            String satisStr = values[2].trim();

            if (adetStr.isEmpty() && alisStr.isEmpty() && satisStr.isEmpty()) {
                showWarning("Boş Güncelleme", "Güncellemek için en az bir alan doldurun.");
                return;
            }

            StringBuilder sql = new StringBuilder("UPDATE urunler SET ");
            boolean first = true;

            if (!adetStr.isEmpty()) {
                sql.append("adet = ?");
                first = false;
            }
            if (!alisStr.isEmpty()) {
                if (!first) sql.append(", ");
                sql.append("alisFiyati = ?");
                first = false;
            }
            if (!satisStr.isEmpty()) {
                if (!first) sql.append(", ");
                sql.append("satisFiyati = ?");
            }

            sql.append(", guncellenmeTarihi = GETDATE() WHERE id = ?");

            try (Connection conn = DatabaseConnection.baglan();
                 PreparedStatement ps = conn.prepareStatement(sql.toString())) {

                int index = 1;

                if (!adetStr.isEmpty()) {
                    ps.setInt(index++, Integer.parseInt(adetStr));
                }
                if (!alisStr.isEmpty()) {
                    ps.setDouble(index++, Double.parseDouble(alisStr));
                }
                if (!satisStr.isEmpty()) {
                    ps.setDouble(index++, Double.parseDouble(satisStr));
                }

                ps.setInt(index, secilen.getId());
                ps.executeUpdate();

                bilgiMesaji("Ürün bilgileri başarıyla güncellendi!");
                lastikleriYukle();

            } catch (NumberFormatException e) {
                hataMesaji("Lütfen geçerli sayı formatı kullanın.");
            } catch (Exception e) {
                hataMesaji("Ürün güncellenirken hata oluştu:\n" + e.getMessage());
            }
        });
    }


    // ======================================================
    //  SATIŞ YAP (TOPLU VE KESİN DÜZELTME)
    // ======================================================
    @FXML
    private void handleSatisYap() {
        Lastik seciliLastik = tableLastikler.getSelectionModel().getSelectedItem();
        if (seciliLastik == null) {
            showWarning("Ürün Seçilmedi", "Lütfen satış yapmak için bir ürün seçin.");
            return;
        }

        ObservableList<MusteriLite> musterilerlite = DatabaseFunctions.musterileriGetirLite();

        // 🔹 Haritayı bir kez oluştur (performans için)
        MusteriLite.initializeMap(musterilerlite);

        if (musterilerlite == null || musterilerlite.isEmpty()) {
            showWarning("Müşteri Yok", "Veritabanında kayıtlı müşteri bulunamadı. Lütfen önce bir müşteri ekleyin.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Satış İşlemi");
        dialog.setHeaderText("Seçilen Ürün: " + seciliLastik.getMarka() + " - " + seciliLastik.getTip());

        ComboBox<MusteriLite> comboMusterilite = new ComboBox<>();
        comboMusterilite.setEditable(true);
        comboMusterilite.setPromptText("Müşteri Seç veya Ara...");
        comboMusterilite.setPrefWidth(320);

        FilteredList<MusteriLite> filtreliListe = new FilteredList<>(musterilerlite, p -> true);
        comboMusterilite.setItems(filtreliListe);

        // 🔹 Satılacak adet alanı
        TextField txtAdet = new TextField();
        txtAdet.setPromptText("Satılacak adet");
        txtAdet.setPrefWidth(150);

        // 🔹 Birim fiyat alanı
        TextField txtBirimFiyat = new TextField();
        double birimFiyat = seciliLastik.getSatisFiyati();
        txtBirimFiyat.setText(String.format("%.2f", birimFiyat));
        txtBirimFiyat.setPrefWidth(150);
        txtBirimFiyat.setEditable(false); // elle değiştirilemez

        // 🔹 Toplam tutar alanı (otomatik hesaplanır)
        TextField txtToplam = new TextField();
        txtToplam.setPromptText("Toplam Tutar (₺)");
        txtToplam.setPrefWidth(150);

        TextField txtAlinan = new TextField();
        txtAlinan.setPromptText("Alinan Tutar (₺)");
        txtAlinan.setPrefWidth(150);

        // 🔹 Sadece adet değiştiğinde otomatik toplam hesaplama
        txtAdet.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int adet = Integer.parseInt(newVal);
                double toplam = adet * birimFiyat;
                txtToplam.setText(String.format("%.2f", toplam));
            } catch (NumberFormatException e) {
                txtToplam.clear(); // Geçersiz sayı girilirse temizle
            }
        });

        // 🔹 Formu bir araya getir
        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Müşteri Seç:"), comboMusterilite,
                new Label("Satılacak Adet:"), txtAdet,
                new Label("Birim Fiyat:"), txtBirimFiyat,
                new Label("Toplam Tutar:"), txtToplam,
                new Label("Alınan Tutar:"), txtAlinan
        );
        dialog.getDialogPane().setContent(content);

        // Butonları ekle
        ButtonType satBtn = new ButtonType("Sat", ButtonBar.ButtonData.OK_DONE);
        ButtonType iptalBtn = new ButtonType("İptal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(satBtn, iptalBtn);

        // 🔹 Diyalog gösterilir ve kullanıcı bir seçim yaparsa burada döner
        dialog.showAndWait().ifPresent(result -> {
            if (result == satBtn) {
                Object m = comboMusterilite.getSelectionModel().getSelectedItem();
                if (m != null) {
                    Object secilenAd = comboMusterilite.getSelectionModel().getSelectedItem();
                    long musteriId = MusteriLite.getIdFromGorunenAd(secilenAd);
                    long urunId = seciliLastik.getId();

                    try {
                        // 🔹 Boş veya hatalı girişleri önle
                        String adetStr = txtAdet.getText().trim();
                        String toplamStr = txtToplam.getText().trim().replace(",", ".");
                        String alinanStr = txtAlinan.getText().trim().replace(",", ".");

                        // 🔹 Boş olanları 0 yap
                        if (adetStr.isEmpty()) adetStr = "0";
                        if (toplamStr.isEmpty()) toplamStr = "0";
                        if (alinanStr.isEmpty()) alinanStr = "0";

                        // 🔹 Tür dönüşümleri
                        int adet = Integer.parseInt(adetStr);
                        double toplamTutar = Double.parseDouble(toplamStr);
                        double alinanTutar = Double.parseDouble(alinanStr);

                        // 🔹 Kalan tutar
                        double kalan = toplamTutar - alinanTutar;

                        boolean odendi = false;

                        if(kalan == 0){
                            odendi = true;
                        }

                        // 🔹 Konsola yaz
                        System.out.println("Seçilen Lastik ID  : " + urunId);
                        System.out.println("Seçilen Müşteri ID : " + musteriId);
                        System.out.println("Adet          : " + adet);
                        System.out.println("Toplam Tutar  : " + String.format("%.2f", toplamTutar) + " ₺");
                        System.out.println("Alınan Tutar  : " + String.format("%.2f", alinanTutar) + " ₺");
                        System.out.println("Kalan         : " + String.format("%.2f", kalan) + " ₺");
                        System.out.println("Ödendi        : " + odendi);

                        if(adet > seciliLastik.getAdet()){
                            showWarning("Stok Aşımı", "Stokta Yeterli Ürün Yok.\nMevcut Stok: " + seciliLastik.getAdet());
                            return;
                        }

                        boolean satisEklendi = DatabaseFunctions.satisEkle(urunId, musteriId, adet, toplamTutar, alinanTutar, odendi);
                        bilgiMesaji("Satış Başarıyla Yapıldı.");
                        lastikleriYukle();

                    } catch (Exception e) {
                        System.out.println("⚠️ Lütfen sayısal alanlara sadece sayı girin!");
                    }
                } else {
                    showWarning("Müşteri Seçilmedi", "Lütfen bir müşteri seçin.");
                }
            }
        });
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
