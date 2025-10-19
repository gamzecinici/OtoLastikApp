package gui;

import javafx.application.Platform;
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
import org.controlsfx.control.table.TableFilter;

public class MusterilerController {

    @FXML private TableView<Musteri> tableMusteriler;
    @FXML private TableColumn<Musteri, String> colAdi, colSoyadi, colTelefon, colEmail, colAdres;
    @FXML private TableColumn<Musteri, String> colKayitTarihi;
    @FXML private TableColumn<Musteri, Double> colBorc;

    private ObservableList<Musteri> musteriListesi = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        musteriListesiniYukle();
    }

    /**
     * Veritabanından müşterileri çeker ve tabloya yükler.
     */
    private void musteriListesiniYukle() {
        ObservableList<Musteri> musteriListesi = DatabaseFunctions.musterileriGetir();

        colAdi.setCellValueFactory(data -> data.getValue().adiProperty());
        colSoyadi.setCellValueFactory(data -> data.getValue().soyadiProperty());
        colTelefon.setCellValueFactory(data -> data.getValue().telefonProperty());
        colEmail.setCellValueFactory(data -> data.getValue().emailProperty());
        colAdres.setCellValueFactory(data -> data.getValue().adresProperty());
        colKayitTarihi.setCellValueFactory(data -> data.getValue().kayitTarihiProperty());
        colBorc.setCellValueFactory(data -> data.getValue().borcProperty().asObject());

        // 🔹 Borç sütununu renklendir (isteğe bağlı, şık görünüm)
        colBorc.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f ₺", item));
                    if (item > 0)
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;"); // kırmızı: borcu var
                    else
                        setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;"); // yeşil: borcu yok
                }
            }
        });

        tableMusteriler.setItems(musteriListesi);
        Platform.runLater(() -> TableFilter.forTableView(tableMusteriler).apply());
    }

    @FXML
    private void handleMusteriEkle() {
        Dialog<Musteri> dialog = new Dialog<>();
        dialog.setTitle("Yeni Müşteri Ekle");
        dialog.setHeaderText("Yeni müşteri bilgilerini giriniz");
        dialog.getDialogPane().setPrefWidth(420);

        ButtonType ekleButton = new ButtonType("Ekle", ButtonBar.ButtonData.OK_DONE);
        ButtonType iptalButton = new ButtonType("İptal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(ekleButton, iptalButton);

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

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ekleButton) {
                if (txtAdi.getText().isEmpty() || txtSoyadi.getText().isEmpty()) {
                    Alert uyar = new Alert(Alert.AlertType.WARNING);
                    uyar.setTitle("Eksik Bilgi");
                    uyar.setHeaderText("Ad ve Soyad alanları boş bırakılamaz!");
                    uyar.showAndWait();
                    return null;
                }

                String bugun = java.time.LocalDate.now().toString();

                return new Musteri(
                        0,
                        txtAdi.getText().trim(),
                        txtSoyadi.getText().trim(),
                        txtTelefon.getText().trim(),
                        txtEmail.getText().trim(),
                        txtAdres.getText().trim(),
                        bugun,
                        0.0
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(yeni -> {
            try {
                boolean basarili = DatabaseFunctions.musteriEkle(yeni);

                if (basarili) {
                    bilgi("Başarılı", "Yeni müşteri başarıyla eklendi!");
                    musteriListesiniYukle();
                }
            } catch (RuntimeException ex) {
                hata("Müşteri Eklenemedi", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleMusteriGuncelle() {
        Musteri m = getSeciliMusteri();
        if (m == null) return;

        Dialog<Musteri> dialog = new Dialog<>();
        dialog.setTitle("Müşteri Güncelle");
        dialog.setHeaderText("Seçili Müşteri: " + m.getAdi() + " " + m.getSoyadi());
        dialog.getDialogPane().setPrefWidth(420);

        ButtonType kaydetButton = new ButtonType("Kaydet", ButtonBar.ButtonData.OK_DONE);
        ButtonType iptalButton = new ButtonType("İptal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(kaydetButton, iptalButton);

        TextField txtAdi = new TextField(m.getAdi());
        TextField txtSoyadi = new TextField(m.getSoyadi());
        TextField txtTelefon = new TextField(m.getTelefon());
        TextField txtEmail = new TextField(m.getEmail());
        TextField txtAdres = new TextField(m.getAdres());
        TextField txtBorc = new TextField(String.valueOf(m.getBorc()));
        txtBorc.setEditable(false);

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

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == kaydetButton) {
                if (txtAdi.getText().isEmpty() || txtSoyadi.getText().isEmpty()) {
                    uyari("Eksik Bilgi", "Ad ve Soyad alanları boş bırakılamaz!");
                    return null;
                }

                m.setAdi(txtAdi.getText().trim());
                m.setSoyadi(txtSoyadi.getText().trim());
                m.setTelefon(txtTelefon.getText().trim());
                m.setEmail(txtEmail.getText().trim());
                m.setAdres(txtAdres.getText().trim());
                return m;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(guncellenen -> {
            boolean basarili = DatabaseFunctions.musteriGuncelle(guncellenen);

            if (basarili)
                bilgi("Başarılı", "Müşteri başarıyla güncellendi!");
            else
                hata("Hata", "Güncelleme sırasında bir hata oluştu.");
            musteriListesiniYukle();
        });
    }

    @FXML
    private void handleMusteriSil() {
        Musteri secili = getSeciliMusteri();
        if (secili == null) return;

        Alert onay = new Alert(Alert.AlertType.CONFIRMATION);
        onay.setTitle("Silme Onayı");
        onay.setHeaderText("Müşteri Silinecek");
        onay.setContentText("Bu müşteriyi silmek istediğine emin misin?\n\n"
                + "👤 " + secili.getAdi() + " " + secili.getSoyadi()
                + "\n📞 " + secili.getTelefon());

        if (onay.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean basarili = DatabaseFunctions.musteriSil(secili.getId());
            if (basarili)
                bilgi("Başarılı", "Müşteri başarıyla silindi.");
            else
                hata("Hata", "Silme işlemi sırasında hata oluştu.");
            musteriListesiniYukle();
        }
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

    // 🔹 Yardımcı Metodlar
    private Musteri getSeciliMusteri() {
        Musteri secili = tableMusteriler.getSelectionModel().getSelectedItem();
        if (secili == null) {
            uyari("Müşteri Seçilmedi", "Lütfen tablodan bir müşteri seçin.");
            return null;
        }
        return secili;
    }

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
