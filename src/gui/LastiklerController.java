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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Stoktaki lastikleri gösterir ve stok artırma işlemlerini yönetir.
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

        // Tablonun tamamen yüklendiğinden emin olmak için
        Platform.runLater(() -> TableFilter.forTableView(tableLastikler).apply());
    }

    /**
     * Veritabanındaki aktif lastik kayıtlarını tabloya yükler.
     */
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
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Veriler yüklenirken hata oluştu!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * “Stok Artır” butonu tıklandığında seçilen ürünün stok adedini günceller.
     */
    @FXML
    private void handleStokArtir() {
        Lastik secilen = tableLastikler.getSelectionModel().getSelectedItem();

        if (secilen == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setHeaderText("Stok Artırma İşlemi");
            alert.setContentText("Lütfen önce bir ürün seçin.");
            alert.showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Stok Artır");
        dialog.setHeaderText("Seçilen ürün: " + secilen.markaProperty().get() + " " + secilen.tipProperty().get());
        dialog.setContentText("Eklenecek adet miktarını girin:");

        dialog.showAndWait().ifPresent(giris -> {
            try {
                int eklenecek = Integer.parseInt(giris);
                if (eklenecek <= 0) {
                    throw new NumberFormatException();
                }

                String sql = "UPDATE urunler SET adet = adet + ?, guncellenmeTarihi = GETDATE() WHERE id = ?";

                try (Connection conn = DatabaseConnection.baglan();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setInt(1, eklenecek);
                    ps.setInt(2, secilen.idProperty().get());
                    ps.executeUpdate();
                }

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Başarılı");
                info.setHeaderText("Stok Güncellendi");
                info.setContentText("Stok " + eklenecek + " adet artırıldı!");
                info.showAndWait();

                // Tabloyu yenile
                lastikleriYukle();

            } catch (NumberFormatException e) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Hata");
                err.setHeaderText("Geçersiz Giriş");
                err.setContentText("Lütfen geçerli bir sayı girin!");
                err.showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Hata");
                err.setHeaderText("Stok artırılırken hata oluştu!");
                err.setContentText(ex.getMessage());
                err.showAndWait();
            }
        });
    }

    /**
     * “Geri” butonuna basıldığında ana panele döner.
     */
    @FXML
    private void handleGeri() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/Panel.fxml"));
            Stage stage = (Stage) tableLastikler.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Yılmaz & Ünal Oto Lastik - Ana Panel");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Panele geri dönülürken hata oluştu!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleIadeEt() {
        Lastik secilen = tableLastikler.getSelectionModel().getSelectedItem();

        if (secilen == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setHeaderText("İade İşlemi");
            alert.setContentText("Lütfen önce bir ürün seçin.");
            alert.showAndWait();
            return;
        }

        int mevcutAdet = secilen.adetProperty().get();

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("İade Et");
        dialog.setHeaderText("Seçilen ürün: " + secilen.markaProperty().get() + " " + secilen.tipProperty().get());
        dialog.setContentText("İade edilecek adet miktarını girin:");

        dialog.showAndWait().ifPresent(giris -> {
            try {
                int iadeMiktar = Integer.parseInt(giris);
                if (iadeMiktar <= 0) {
                    throw new NumberFormatException();
                }

                if (iadeMiktar > mevcutAdet) {
                    Alert warn = new Alert(Alert.AlertType.WARNING);
                    warn.setTitle("Uyarı");
                    warn.setHeaderText("Geçersiz Miktar");
                    warn.setContentText("İade miktarı stoktaki adetten fazla olamaz!");
                    warn.showAndWait();
                    return;
                }

                // Eğer tamamı iade ediliyorsa silme onayı al
                if (iadeMiktar == mevcutAdet) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Tamamını İade Et");
                    confirm.setHeaderText("Tüm stok iade edilecek!");
                    confirm.setContentText("Bu ürün tamamen stoktan kaldırılacak. Devam etmek istiyor musunuz?");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            urunuTamamenSil(secilen.idProperty().get());
                            bilgiMesaji("Tüm ürün başarıyla iade edildi ve stoktan kaldırıldı!");
                            lastikleriYukle();
                        }
                    });
                } else {
                    // Stoktan düş
                    String sql = "UPDATE urunler SET adet = adet - ?, guncellenmeTarihi = GETDATE() WHERE id = ?";

                    try (Connection conn = DatabaseConnection.baglan();
                         PreparedStatement ps = conn.prepareStatement(sql)) {

                        ps.setInt(1, iadeMiktar);
                        ps.setInt(2, secilen.idProperty().get());
                        ps.executeUpdate();
                    }

                    bilgiMesaji("Stoktan " + iadeMiktar + " adet iade edildi!");
                    lastikleriYukle();
                }

            } catch (NumberFormatException e) {
                hataMesaji("Lütfen geçerli bir sayı girin!");
            } catch (Exception ex) {
                ex.printStackTrace();
                hataMesaji("İade işlemi sırasında hata oluştu!\n" + ex.getMessage());
            }
        });
    }

    @FXML
    private void handleSatisYap() {
        Lastik seciliLastik = tableLastikler.getSelectionModel().getSelectedItem();

        if (seciliLastik == null) {
            showWarning("Ürün Seçilmedi", "Lütfen satış yapmak için tablodan bir ürün seçin.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Satış İşlemi");
        dialog.setHeaderText("Seçilen Ürün: " + seciliLastik.getMarka() + " - " + seciliLastik.getTip());

        ButtonType btnSat = new ButtonType("Satışı Kaydet", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnIptal = new ButtonType("İptal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSat, btnIptal);

        TextField txtAdet = new TextField();
        txtAdet.setPromptText("Satış adedi girin...");

        TextField txtToplam = new TextField();
        txtToplam.setPromptText("Toplam fiyat ₺");
        txtToplam.setEditable(true);

        TextField txtAlinan = new TextField();
        txtAlinan.setPromptText("Müşteriden alınan ₺ (boş bırakılabilir)");

        Label lblFiyat = new Label("Birim fiyat: " + seciliLastik.getSatisFiyati() + " ₺");
        Label lblStok = new Label("Stoktaki mevcut adet: " + seciliLastik.getAdet());

        VBox vbox = new VBox(10,
                lblStok,
                new Label("Satış Adedi:"), txtAdet,
                lblFiyat,
                new Label("Toplam Fiyat (₺):"), txtToplam,
                new Label("Müşteriden Alınan (₺):"), txtAlinan
        );
        vbox.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(vbox);

        // 🔹 Adet girildikçe otomatik hesaplama
        txtAdet.textProperty().addListener((obs, eski, yeni) -> {
            try {
                int adet = Integer.parseInt(yeni);
                if (adet > seciliLastik.getAdet()) {
                    txtAdet.setStyle("-fx-border-color: red;");
                    txtToplam.clear();
                } else {
                    txtAdet.setStyle("");
                    double toplam = adet * seciliLastik.getSatisFiyati();
                    txtToplam.setText(String.format("%.2f", toplam));
                }
            } catch (NumberFormatException e) {
                txtToplam.clear();
                txtAdet.setStyle("");
            }
        });

        // 🔹 Sadece sayı ve virgül girişi
        txtToplam.textProperty().addListener((obs, eski, yeni) -> {
            if (!yeni.matches("[0-9,]*")) txtToplam.setText(eski);
        });
        txtAlinan.textProperty().addListener((obs, eski, yeni) -> {
            if (!yeni.matches("[0-9,]*")) txtAlinan.setText(eski);
        });

        // 🔹 Satış butonunun kapanma davranışını kontrol et
        final Button btnSatButton = (Button) dialog.getDialogPane().lookupButton(btnSat);
        btnSatButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                int adet = Integer.parseInt(txtAdet.getText());
                if (adet <= 0) {
                    showWarning("Hatalı Adet", "Satış adedi 0 veya negatif olamaz!");
                    event.consume(); // 🔸 Pencere kapanmasın
                    return;
                }
                if (adet > seciliLastik.getAdet()) {
                    showWarning("Yetersiz Stok", "Stoktaki adetten fazla satış yapılamaz!");
                    event.consume(); // 🔸 Pencere kapanmasın
                    return;
                }

                double toplam = Double.parseDouble(txtToplam.getText().replace(",", "."));
                double alinan = 0;
                if (!txtAlinan.getText().isEmpty())
                    alinan = Double.parseDouble(txtAlinan.getText().replace(",", "."));

                double kalanBorc = toplam - alinan;

                // 🔹 Satış sonucu penceresi
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Satış Tamamlandı");
                alert.setHeaderText("Satış başarıyla hesaplandı!");
                alert.setContentText(
                        "Ürün: " + seciliLastik.getMarka() + " " + seciliLastik.getEbat() + "\n" +
                                "Adet: " + adet + "\n" +
                                "Toplam Tutar: " + String.format("%.2f ₺", toplam) + "\n" +
                                "Alınan: " + String.format("%.2f ₺", alinan) + "\n" +
                                "Kalan Borç: " + String.format("%.2f ₺", kalanBorc)
                );
                alert.showAndWait();

                // 💾 Burada satış veritabanına kaydedilebilir
            } catch (NumberFormatException e) {
                showWarning("Geçersiz Giriş", "Lütfen geçerli sayılar girin.");
                event.consume(); // 🔸 Pencere kapanmasın
            }
        });

        dialog.showAndWait();
    }

    private void showWarning(String baslik, String mesaj) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(baslik);
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }




    /**
     * Ürünü tamamen veritabanından siler.
     */
    private void urunuTamamenSil(int urunId) {
        String sql = "DELETE FROM urunler WHERE id = ?";
        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, urunId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            hataMesaji("Ürün silinirken hata oluştu!\n" + e.getMessage());
        }
    }

    /**
     * Bilgi mesajı gösterir.
     */
    private void bilgiMesaji(String mesaj) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Bilgi");
        info.setHeaderText(null);
        info.setContentText(mesaj);
        info.showAndWait();
    }

    /**
     * Hata mesajı gösterir.
     */
    private void hataMesaji(String mesaj) {
        Alert err = new Alert(Alert.AlertType.ERROR);
        err.setTitle("Hata");
        err.setHeaderText(null);
        err.setContentText(mesaj);
        err.showAndWait();
    }

}
