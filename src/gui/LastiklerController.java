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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Lastik;
import model.MusteriLite;
import org.controlsfx.control.table.TableFilter;
import java.sql.*;
import javafx.beans.value.ChangeListener;
import java.util.stream.Collectors;
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

        // ComboBox'u diyalog penceresine ekle
        VBox content = new VBox(10, new Label("Müşteri Seç:"), comboMusterilite);
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
                    System.out.println("Seçilen Müşteri ID: " + musteriId);
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
