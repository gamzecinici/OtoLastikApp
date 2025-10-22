package gui;

import database.DatabaseConnection;
import database.DatabaseFunctions;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.KeyValue;
import javafx.scene.paint.Color;
import javafx.scene.control.cell.*;
import javafx.scene.control.ListCell;

import java.sql.Connection;

public class LastikEkleController {

    @FXML private ComboBox<KeyValue> comboMarka, comboTip, comboEbat, comboHiz, comboYuk;
    @FXML private TextField txtModel, txtAlis, txtSatis, txtAdet;

    @FXML
    public void initialize() {
        // 🔹 ComboBox’ları doldur
        refreshCombos();

        // 🔹 ComboBox yazılarını beyaz yapmak
        makeComboTextWhite(comboMarka);
        makeComboTextWhite(comboTip);
        makeComboTextWhite(comboEbat);
        makeComboTextWhite(comboHiz);
        makeComboTextWhite(comboYuk);

        LayoutRefresher.refresh(comboMarka);
    }


    /**
     * Tüm ComboBox'ları veritabanından yeniler.
     */
    private void refreshCombos() {
        comboMarka.setItems(DatabaseFunctions.markalariGetir());
        comboTip.setItems(DatabaseFunctions.tipleriGetir());
        comboHiz.setItems(DatabaseFunctions.hizGetir());
        comboYuk.setItems(DatabaseFunctions.yukGetir());
        comboEbat.setItems(DatabaseFunctions.ebatlariGetir());
    }

    /**
     * ComboBox içindeki metinleri (hem seçili hem açılır liste) beyaz gösterir.
     */
    private <T> void makeComboTextWhite(ComboBox<T> combo) {
        // Açılır liste (list cells)
        combo.setCellFactory(listView -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setTextFill(Color.WHITE); // liste içindeki yazılar beyaz
                }
            }
        });

        // Seçili öğe (kapalı durumdaki görünüm)
        combo.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setTextFill(Color.WHITE); // seçili öğe beyaz
                }
            }
        });
    }

    @FXML
    private void handleKaydet() {
        KeyValue marka = comboMarka.getValue();
        KeyValue tip = comboTip.getValue();
        KeyValue ebat = comboEbat.getValue();
        KeyValue hiz = comboHiz.getValue();
        KeyValue yuk = comboYuk.getValue();

        String model = txtModel.getText().trim();
        String alis = txtAlis.getText().trim();
        String satis = txtSatis.getText().trim();
        String adet = txtAdet.getText().trim();

        if (marka == null || tip == null || ebat == null || hiz == null || yuk == null ||
                model.isEmpty() || alis.isEmpty() || satis.isEmpty() || adet.isEmpty()) {
            alert(Alert.AlertType.WARNING, "Eksik Bilgi", "Lütfen tüm alanları doldurun!");
            return;
        }

        try (Connection conn = DatabaseConnection.baglan()) {

            // 🔹 1. Bu ürün zaten var mı kontrol et
            String kontrolSql = """
            SELECT COUNT(*) FROM urunler
            WHERE markaId = ? AND model = ? AND tipId = ? AND ebatId = ? 
                  AND hizEndeksId = ? AND yukEndeksId = ? AND aktif = 1
            """;

            var kontrolPs = conn.prepareStatement(kontrolSql);
            kontrolPs.setInt(1, marka.getId());
            kontrolPs.setString(2, model);
            kontrolPs.setInt(3, tip.getId());
            kontrolPs.setInt(4, ebat.getId());
            kontrolPs.setInt(5, hiz.getId());
            kontrolPs.setInt(6, yuk.getId());

            var rs = kontrolPs.executeQuery();
            rs.next();
            int sayi = rs.getInt(1);

            if (sayi > 0) {
                alert(Alert.AlertType.WARNING, "Zaten Var",
                        "Bu özelliklere sahip bir ürün zaten sistemde mevcut!");
                return;
            }

            // 🔹 2. Ürün yoksa ekle
            String sql = """
            INSERT INTO urunler 
            (markaId, model, tipId, ebatId, hizEndeksId, yukEndeksId, alisFiyati, satisFiyati, adet, eklenmeTarihi, aktif)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), 1)
            """;

            var ps = conn.prepareStatement(sql);
            ps.setInt(1, marka.getId());
            ps.setString(2, model);
            ps.setInt(3, tip.getId());
            ps.setInt(4, ebat.getId());
            ps.setInt(5, hiz.getId());
            ps.setInt(6, yuk.getId());
            ps.setBigDecimal(7, new java.math.BigDecimal(alis));
            ps.setBigDecimal(8, new java.math.BigDecimal(satis));
            ps.setInt(9, Integer.parseInt(adet));

            ps.executeUpdate();

            alert(Alert.AlertType.INFORMATION, "Başarılı", "Lastik başarıyla eklendi!");
            clear();

        } catch (Exception e) {
            e.printStackTrace();
            alert(Alert.AlertType.ERROR, "Hata", "Kayıt eklenirken bir hata oluştu:\n" + e.getMessage());
        }
    }


    /**
     * Alanları temizler.
     */
    private void clear() {
        txtModel.clear();
        txtAlis.clear();
        txtSatis.clear();
        txtAdet.clear();
        comboMarka.getSelectionModel().clearSelection();
        comboTip.getSelectionModel().clearSelection();
        comboEbat.getSelectionModel().clearSelection();
        comboHiz.getSelectionModel().clearSelection();
        comboYuk.getSelectionModel().clearSelection();
    }

    private void alert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // --- Yeni Ekleme Fonksiyonları ---

    @FXML
    private void handleMarkaEkle() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Yeni Marka Ekle");
        dialog.setHeaderText("Yeni bir marka ekleyin");
        dialog.setContentText("Marka adı:");

        dialog.showAndWait().ifPresent(markaAdi -> {
            if (!markaAdi.trim().isEmpty()) {
                try {
                    boolean basarili = DatabaseFunctions.markaEkle(markaAdi.trim());

                    if (basarili) {
                        alert(Alert.AlertType.INFORMATION, "Başarılı", "Marka eklendi: " + markaAdi);

                        // ComboBox yenileme ve seçili yapma
                        comboMarka.setItems(DatabaseFunctions.markalariGetir());
                        for (var item : comboMarka.getItems()) {
                            if (item.getName().equalsIgnoreCase(markaAdi.trim())) {
                                comboMarka.getSelectionModel().select(item);
                                break;
                            }
                        }
                        makeComboTextWhite(comboMarka);

                    } else {
                        // Marka zaten varsa ya da veritabanı false döndürdüyse
                        alert(Alert.AlertType.WARNING, "Uyarı", "Bu marka zaten mevcut veya eklenemedi!");
                    }

                } catch (Exception e) {
                    // Her türlü veritabanı hatasını burada yakalarız
                    e.printStackTrace();
                    alert(Alert.AlertType.ERROR, "Hata", "Marka eklenirken bir hata oluştu:\n" + e.getMessage());
                }
            } else {
                alert(Alert.AlertType.WARNING, "Eksik Bilgi", "Marka adı boş olamaz!");
            }
        });
    }


    @FXML
    private void handleTipEkle() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Yeni Tip Ekle");
        dialog.setHeaderText("Yeni bir lastik tipi ekleyin");
        dialog.setContentText("Tip adı:");

        dialog.showAndWait().ifPresent(tipAdi -> {
            if (!tipAdi.trim().isEmpty()) {
                try {
                    boolean basarili = DatabaseFunctions.tipEkle(tipAdi.trim());

                    if (basarili) {
                        alert(Alert.AlertType.INFORMATION, "Başarılı", "Tip eklendi: " + tipAdi);

                        // ComboBox’ı yenile ve eklenen tipi seçili yap
                        comboTip.setItems(DatabaseFunctions.tipleriGetir());
                        for (var item : comboTip.getItems()) {
                            if (item.getName().equalsIgnoreCase(tipAdi.trim())) {
                                comboTip.getSelectionModel().select(item);
                                break;
                            }
                        }

                        // Yazı rengini tekrar beyaz yap
                        makeComboTextWhite(comboTip);

                    } else {
                        alert(Alert.AlertType.WARNING, "Uyarı", "Bu tip zaten mevcut veya eklenemedi!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    alert(Alert.AlertType.ERROR, "Hata", "Tip eklenirken bir hata oluştu:\n" + e.getMessage());
                }
            } else {
                alert(Alert.AlertType.WARNING, "Eksik Bilgi", "Tip adı boş olamaz!");
            }
        });
    }


    @FXML
    private void handleHizEkle() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Yeni Hız Endeksi Ekle");
        dialog.setHeaderText("Yeni bir hız endeksi ekleyin");

        TextField txtHiz = new TextField();
        txtHiz.setPromptText("Hız Endeksi (örnek: T, H, V)");

        TextField txtMaksHiz = new TextField();
        txtMaksHiz.setPromptText("Maksimum Hız (örnek: 190, 240)");

        VBox vbox = new VBox(10, txtHiz, txtMaksHiz);
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String hizEndeks = txtHiz.getText().trim();
                String maksHiz = txtMaksHiz.getText().trim();

                if (hizEndeks.isEmpty() || maksHiz.isEmpty()) {
                    alert(Alert.AlertType.WARNING, "Eksik Bilgi", "Lütfen tüm alanları doldurun!");
                    return;
                }

                try {
                    boolean basarili = DatabaseFunctions.hizEkle(hizEndeks, maksHiz);

                    if (basarili) {
                        alert(Alert.AlertType.INFORMATION, "Başarılı",
                                "Hız endeksi eklendi: " + hizEndeks + " (" + maksHiz + " km/s)");

                        // ComboBox’ı yenile
                        comboHiz.setItems(DatabaseFunctions.hizGetir());

                        // Yeni eklenen değeri seçili yap
                        for (var item : comboHiz.getItems()) {
                            if (item.getName().equalsIgnoreCase(hizEndeks)) {
                                comboHiz.getSelectionModel().select(item);
                                break;
                            }
                        }

                        // Yazı rengini tekrar beyaz yap
                        makeComboTextWhite(comboHiz);

                    } else {
                        alert(Alert.AlertType.WARNING, "Uyarı", "Bu hız endeksi zaten mevcut veya eklenemedi!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    alert(Alert.AlertType.ERROR, "Hata", "Hız endeksi eklenirken bir hata oluştu:\n" + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleYukEkle() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Yeni Yük Endeksi Ekle");
        dialog.setHeaderText("Yeni bir yük endeksi ekleyin");

        TextField txtYuk = new TextField();
        txtYuk.setPromptText("Yük Endeksi (örnek: 91, 105)");

        TextField txtKg = new TextField();
        txtKg.setPromptText("Lastik Başına Düşen Kg (örnek: 615, 950)");

        VBox vbox = new VBox(10, txtYuk, txtKg);
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String yukEndeks = txtYuk.getText().trim();
                String kgDegeri = txtKg.getText().trim();

                if (yukEndeks.isEmpty() || kgDegeri.isEmpty()) {
                    alert(Alert.AlertType.WARNING, "Eksik Bilgi", "Lütfen tüm alanları doldurun!");
                    return;
                }

                try {
                    boolean basarili = DatabaseFunctions.yukEkle(yukEndeks, kgDegeri);

                    if (basarili) {
                        alert(Alert.AlertType.INFORMATION, "Başarılı",
                                "Yük endeksi eklendi: " + yukEndeks + " (" + kgDegeri + " kg)");

                        // ComboBox’ı yenile
                        comboYuk.setItems(DatabaseFunctions.yukGetir());

                        // Yeni eklenen değeri seçili yap
                        for (var item : comboYuk.getItems()) {
                            if (item.getName().equalsIgnoreCase(yukEndeks)) {
                                comboYuk.getSelectionModel().select(item);
                                break;
                            }
                        }

                        // Yazı rengini beyaz tut
                        makeComboTextWhite(comboYuk);

                    } else {
                        alert(Alert.AlertType.WARNING, "Uyarı", "Bu yük endeksi zaten mevcut veya eklenemedi!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    alert(Alert.AlertType.ERROR, "Hata", "Yük endeksi eklenirken bir hata oluştu:\n" + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleEbatEkle() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Yeni Ebat Ekle");
        dialog.setHeaderText("Yeni bir lastik ölçüsü ekleyin (örnek: 205 55 16)");

        TextField txtGenislik = new TextField();
        txtGenislik.setPromptText("Genişlik (örnek: 205)");

        TextField txtYukseklik = new TextField();
        txtYukseklik.setPromptText("Yükseklik (örnek: 55)");

        TextField txtJant = new TextField();
        txtJant.setPromptText("Jant (örnek: 16)");

        VBox vbox = new VBox(10, txtGenislik, txtYukseklik, txtJant);
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    // ✅ Kullanıcı girişini sayıya çevir
                    double g = Double.parseDouble(txtGenislik.getText().trim());
                    double y = Double.parseDouble(txtYukseklik.getText().trim());
                    double j = Double.parseDouble(txtJant.getText().trim());

                    // ✅ Veritabanına ekleme işlemi
                    try {
                        boolean basarili = DatabaseFunctions.ebatEkle(g, y, j);

                        if (basarili) {
                            String ebatAdi = String.format("%.0f/%.0f/R%.0f", g, y, j);
                            alert(Alert.AlertType.INFORMATION, "Başarılı", "Ebat eklendi: " + ebatAdi);

                            // ComboBox’ı yenile ve yeni ekleneni seçili yap
                            comboEbat.setItems(DatabaseFunctions.ebatlariGetir());
                            for (var item : comboEbat.getItems()) {
                                if (item.getName().equalsIgnoreCase(ebatAdi)) {
                                    comboEbat.getSelectionModel().select(item);
                                    break;
                                }
                            }

                            // Yazı rengini tekrar beyaz yap
                            makeComboTextWhite(comboEbat);

                        } else {
                            alert(Alert.AlertType.WARNING, "Uyarı", "Bu ebat zaten mevcut veya eklenemedi!");
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        alert(Alert.AlertType.ERROR, "Hata", "Ebat eklenirken bir hata oluştu:\n" + ex.getMessage());
                    }

                } catch (NumberFormatException e) {
                    alert(Alert.AlertType.WARNING, "Geçersiz Giriş", "Lütfen sadece sayısal değerler girin!");
                }
            }
        });
    }

    @FXML
    private void handleGeri() {
        try {
            Stage st = (Stage) txtModel.getScene().getWindow();
            st.setScene(new Scene(FXMLLoader.load(getClass().getResource("/gui/panel.fxml"))));
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Hata", e.getMessage());
        }
    }
}
