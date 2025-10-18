package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Musteri;
import database.DatabaseFunctions;

public class MusterilerController {

    @FXML private TableView<Musteri> tableMusteriler;
    @FXML private TableColumn<Musteri, Long> colId;
    @FXML private TableColumn<Musteri, String> colAdi, colSoyadi, colTelefon, colEmail, colAdres;
    @FXML private TableColumn<Musteri, String> colKayitTarihi;
    @FXML private TableColumn<Musteri, Double> colBorc;

    private ObservableList<Musteri> musteriListesi = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        musteriListesiniYukle();
    }

    private void musteriListesiniYukle() {
        // 🔹 Veritabanından çek
        ObservableList<Musteri> musteriListesi = DatabaseFunctions.musterileriGetir();

        // 🔹 Sütunları bağla (ilk çalışmada)
        colId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colAdi.setCellValueFactory(data -> data.getValue().adiProperty());
        colSoyadi.setCellValueFactory(data -> data.getValue().soyadiProperty());
        colTelefon.setCellValueFactory(data -> data.getValue().telefonProperty());
        colEmail.setCellValueFactory(data -> data.getValue().emailProperty());
        colAdres.setCellValueFactory(data -> data.getValue().adresProperty());
        colKayitTarihi.setCellValueFactory(data -> data.getValue().kayitTarihiProperty());
        colBorc.setCellValueFactory(data -> data.getValue().borcProperty().asObject());

        // 🔹 TableView’e veriyi ata
        tableMusteriler.setItems(musteriListesi);
    }

    @FXML
    private void handleMusteriEkle() {
        // 🔹 Popup oluştur
        Dialog<Musteri> dialog = new Dialog<>();
        dialog.setTitle("Yeni Müşteri Ekle");
        dialog.setHeaderText("Yeni müşteri bilgilerini giriniz");
        dialog.getDialogPane().setPrefWidth(420);

        ButtonType ekleButton = new ButtonType("Ekle", ButtonBar.ButtonData.OK_DONE);
        ButtonType iptalButton = new ButtonType("İptal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(ekleButton, iptalButton);

        // 🔹 Boş form alanları
        TextField txtAdi = new TextField();
        TextField txtSoyadi = new TextField();
        TextField txtTelefon = new TextField();
        TextField txtEmail = new TextField();
        TextField txtAdres = new TextField();

        txtAdi.setPromptText("Adı");
        txtSoyadi.setPromptText("Soyadı");
        txtTelefon.setPromptText("Telefon");
        txtEmail.setPromptText("E-posta");
        txtAdres.setPromptText("Adres");

        // 🔹 Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 30, 10, 10));

        grid.add(new Label("Adı:"), 0, 0);     grid.add(txtAdi, 1, 0);
        grid.add(new Label("Soyadı:"), 0, 1);  grid.add(txtSoyadi, 1, 1);
        grid.add(new Label("Telefon:"), 0, 2); grid.add(txtTelefon, 1, 2);
        grid.add(new Label("E-posta:"), 0, 3); grid.add(txtEmail, 1, 3);
        grid.add(new Label("Adres:"), 0, 4);   grid.add(txtAdres, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // 🔹 Sonuç (Kaydet basınca)
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ekleButton) {
                if (txtAdi.getText().isEmpty() || txtSoyadi.getText().isEmpty()) {
                    Alert uyar = new Alert(Alert.AlertType.WARNING);
                    uyar.setTitle("Eksik Bilgi");
                    uyar.setHeaderText("Ad ve Soyad alanları boş bırakılamaz!");
                    uyar.showAndWait();
                    return null;
                }

                // otomatik tarih
                String bugun = java.time.LocalDate.now().toString();

                return new Musteri(
                        0, // id veritabanında otomatik artıyor
                        txtAdi.getText().trim(),
                        txtSoyadi.getText().trim(),
                        txtTelefon.getText().trim(),
                        txtEmail.getText().trim(),
                        txtAdres.getText().trim(),
                        bugun,
                        0.0 // borç = 0
                );
            }
            return null;
        });

        // 🔹 Veritabanına ekle
        dialog.showAndWait().ifPresent(yeni -> {
            try {
                boolean basarili = DatabaseFunctions.musteriEkle(yeni);

                if (basarili) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Başarılı");
                    alert.setHeaderText(null);
                    alert.setContentText("Yeni müşteri başarıyla eklendi!");
                    alert.showAndWait();
                    musteriListesiniYukle(); // tabloyu yenile
                }

            } catch (RuntimeException ex) {
                // 🔹 Burada özel hata mesajını göstereceğiz
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Müşteri Eklenemedi");
                alert.setHeaderText("Kayıt işlemi başarısız!");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void handleMusteriGuncelle() {
        Musteri m = getSeciliMusteri(); // Seçili müşteri kontrolü
        if (m == null) return;

        // 🔹 Popup oluştur
        Dialog<Musteri> dialog = new Dialog<>();
        dialog.setTitle("Müşteri Güncelle");
        dialog.setHeaderText("Seçili Müşteri: " + m.getAdi() + " " + m.getSoyadi());
        dialog.getDialogPane().setPrefWidth(420);

        ButtonType kaydetButton = new ButtonType("Kaydet", ButtonBar.ButtonData.OK_DONE);
        ButtonType iptalButton = new ButtonType("İptal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(kaydetButton, iptalButton);

        // 🔹 Form alanları
        TextField txtAdi = new TextField(m.getAdi());
        TextField txtSoyadi = new TextField(m.getSoyadi());
        TextField txtTelefon = new TextField(m.getTelefon());
        TextField txtEmail = new TextField(m.getEmail());
        TextField txtAdres = new TextField(m.getAdres());
        TextField txtBorc = new TextField(String.valueOf(m.getBorc()));

        txtAdi.setPromptText("Adı");
        txtSoyadi.setPromptText("Soyadı");
        txtTelefon.setPromptText("Telefon");
        txtEmail.setPromptText("E-posta");
        txtAdres.setPromptText("Adres");
        txtBorc.setPromptText("Borç (₺)");

        // 🔹 Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 30, 10, 10));

        grid.add(new Label("Adı:"), 0, 0);     grid.add(txtAdi, 1, 0);
        grid.add(new Label("Soyadı:"), 0, 1);  grid.add(txtSoyadi, 1, 1);
        grid.add(new Label("Telefon:"), 0, 2); grid.add(txtTelefon, 1, 2);
        grid.add(new Label("E-posta:"), 0, 3); grid.add(txtEmail, 1, 3);
        grid.add(new Label("Adres:"), 0, 4);   grid.add(txtAdres, 1, 4);
        grid.add(new Label("Borç:"), 0, 5);    grid.add(txtBorc, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // 🔹 Kaydet butonuna basılınca
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == kaydetButton) {

                // Boş alan kontrolü
                if (txtAdi.getText().isEmpty() || txtSoyadi.getText().isEmpty()) {
                    Alert uyar = new Alert(Alert.AlertType.WARNING);
                    uyar.setTitle("Eksik Bilgi");
                    uyar.setHeaderText("Ad ve Soyad alanları boş bırakılamaz!");
                    uyar.showAndWait();
                    return null;
                }

                m.setAdi(txtAdi.getText().trim());
                m.setSoyadi(txtSoyadi.getText().trim());
                m.setTelefon(txtTelefon.getText().trim());
                m.setEmail(txtEmail.getText().trim());
                m.setAdres(txtAdres.getText().trim());

                try {
                    m.setBorc(Double.parseDouble(txtBorc.getText().trim()));
                } catch (NumberFormatException e) {
                    m.setBorc(0);
                }
                return m;
            }
            return null;
        });

        // 🔹 Popup sonucu
        dialog.showAndWait().ifPresent(guncellenen -> {
            boolean basarili = DatabaseFunctions.musteriGuncelle(guncellenen);

            if (basarili) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Başarılı");
                alert.setHeaderText(null);
                alert.setContentText("Müşteri başarıyla güncellendi!");
                alert.showAndWait();
                musteriListesiniYukle(); // tabloyu yenile
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Hata");
                alert.setHeaderText("Güncelleme başarısız!");
                alert.setContentText("Veritabanına kaydedilirken bir hata oluştu.");
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void handleMusteriSil() {
        Musteri secili = getSeciliMusteri(); // Seçili müşteri kontrolü
        if (secili == null) return;

        Alert onay = new Alert(Alert.AlertType.CONFIRMATION);
        onay.setTitle("Silme Onayı");
        onay.setHeaderText("Müşteri Silinecek");
        onay.setContentText("Bu müşteriyi silmek istediğine emin misin?\n\n"
                + "👤 " + secili.getAdi() + " " + secili.getSoyadi() + "\n📞 " + secili.getTelefon());

        ButtonType evet = new ButtonType("Evet", ButtonBar.ButtonData.OK_DONE);
        ButtonType hayir = new ButtonType("Hayır", ButtonBar.ButtonData.CANCEL_CLOSE);
        onay.getButtonTypes().setAll(evet, hayir);

        onay.showAndWait().ifPresent(cevap -> {
            if (cevap == evet) {
                boolean basarili = DatabaseFunctions.musteriSil(secili.getId());

                if (basarili) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Başarılı");
                    alert.setHeaderText(null);
                    alert.setContentText("Müşteri başarıyla silindi.");
                    alert.showAndWait();
                    musteriListesiniYukle(); // tabloyu yenile
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Hata");
                    alert.setHeaderText("Silme işlemi başarısız!");
                    alert.setContentText("Veritabanı işlemi sırasında hata oluştu.");
                    alert.showAndWait();
                }
            }
        });
    }

    @FXML
    private void handleGeriDon() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("panel.fxml"));
            Stage stage = (Stage) tableMusteriler.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Musteri getSeciliMusteri() {
        Musteri secili = tableMusteriler.getSelectionModel().getSelectedItem();

        if (secili == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setHeaderText("Müşteri Seçilmedi");
            alert.setContentText("Lütfen önce tablodan bir müşteri seçiniz.");
            alert.showAndWait();
            return null;
        }

        return secili;
    }
}
