package gui;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.sql.ResultSet;

public class LastikEkleController {

    @FXML private TextField txtMarka;
    @FXML private ComboBox<String> comboTip;
    @FXML private TextField txtEbat;
    @FXML private TextField txtAlis;
    @FXML private TextField txtSatis;
    @FXML private TextField txtTarih;

    private final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final Pattern EBAT_FULL = Pattern.compile("^\\d{3}/\\d{2}\\s?R\\d{2}$");

    @FXML
    public void initialize() {
        comboTip.getItems().setAll("Yaz", "Kış", "4 Mevsim");

        comboTip.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: #F2F2F2; -fx-font-size: 15px;");
            }
        });

        comboTip.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: #1b1b1b; -fx-font-size: 15px;");
            }
        });

        UnaryOperator<TextFormatter.Change> moneyFilter =
                ch -> ch.getControlNewText().matches("[0-9.,]*") ? ch : null;
        txtAlis.setTextFormatter(new TextFormatter<>(moneyFilter));
        txtSatis.setTextFormatter(new TextFormatter<>(moneyFilter));

        txtEbat.setTextFormatter(new TextFormatter<>((UnaryOperator<TextFormatter.Change>) ch -> {
            String after = ch.getControlNewText().toUpperCase();
            if (!after.matches("[0-9/ R]*")) return null;
            ch.setText(ch.getText().toUpperCase());
            return ch;
        }));

        txtTarih.setTextFormatter(new TextFormatter<>((UnaryOperator<TextFormatter.Change>) ch -> {
            String t = ch.getControlNewText();
            if (!t.matches("[0-9.]*") || t.length() > 10) return null;
            if (!t.matches("^$|^\\d{1,2}$|^\\d{1,2}\\.$|^\\d{1,2}\\.\\d{1}$|^\\d{1,2}\\.\\d{2}$|^\\d{1,2}\\.\\d{2}\\.$|^\\d{1,2}\\.\\d{2}\\.\\d{1,4}$"))
                return null;
            return ch;
        }));
    }

    @FXML private TextField txtAdet;

    @FXML
    private void handleKaydet() {
        String marka = safe(txtMarka.getText());
        String tip   = comboTip.getValue() == null ? "" : comboTip.getValue();
        String ebat  = safe(txtEbat.getText());
        String alisS = safe(txtAlis.getText());
        String satisS= safe(txtSatis.getText());
        String tarihS= safe(txtTarih.getText());
        String adetS = safe(txtAdet.getText()); // 🔹 Yeni: kullanıcıdan adet al

        if (marka.isEmpty() || tip.isEmpty() || ebat.isEmpty() || alisS.isEmpty() || satisS.isEmpty() || tarihS.isEmpty() || adetS.isEmpty()) {
            alert(Alert.AlertType.WARNING, "Eksik Bilgi", "Lütfen tüm alanları doldurun (adet dahil).");
            return;
        }

        BigDecimal alis, satis;
        int adet;
        try {
            alis  = new BigDecimal(alisS.replace(",", "."));
            satis = new BigDecimal(satisS.replace(",", "."));
            adet = Integer.parseInt(adetS);
            if (adet <= 0) throw new Exception();
        } catch (Exception e) {
            alert(Alert.AlertType.WARNING, "Hatalı Giriş", "Adet sayısı pozitif bir tam sayı olmalıdır.");
            return;
        }

        LocalDate ld;
        try {
            ld = LocalDate.parse(tarihS, DATE_FMT);
        } catch (Exception e) {
            alert(Alert.AlertType.WARNING, "Tarih Hatası", "Tarih yalnızca gg.aa.yyyy formatında olmalıdır.");
            return;
        }

        try (Connection conn = DatabaseConnection.baglan()) {

            // 🔹 1. Aynı lastik var mı kontrol et
            String checkSql = "SELECT adet FROM Lastikler WHERE marka=? AND tip=? AND ebat=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, marka);
            checkStmt.setString(2, tip);
            checkStmt.setString(3, ebat);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // 🔹 2. Varsa mevcut adedi girilen kadar artır
                int mevcutAdet = rs.getInt("adet");
                int yeniAdet = mevcutAdet + adet;

                String updateSql = "UPDATE Lastikler SET adet=?, alis_fiyati=?, satis_fiyati=?, tarih=? WHERE marka=? AND tip=? AND ebat=?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, yeniAdet);
                updateStmt.setBigDecimal(2, alis);
                updateStmt.setBigDecimal(3, satis);
                updateStmt.setDate(4, java.sql.Date.valueOf(ld));
                updateStmt.setString(5, marka);
                updateStmt.setString(6, tip);
                updateStmt.setString(7, ebat);
                updateStmt.executeUpdate();

                alert(Alert.AlertType.INFORMATION, "Stok Güncellendi", adet + " adet daha eklendi. Yeni stok: " + yeniAdet);

            } else {
                // 🔹 3. Yoksa yeni kayıt ekle
                String insertSql = "INSERT INTO Lastikler (marka, tip, ebat, alis_fiyati, satis_fiyati, adet, tarih) VALUES (?,?,?,?,?,?,?)";
                PreparedStatement ps = conn.prepareStatement(insertSql);
                ps.setString(1, marka);
                ps.setString(2, tip);
                ps.setString(3, ebat);
                ps.setBigDecimal(4, alis);
                ps.setBigDecimal(5, satis);
                ps.setInt(6, adet); // 🔹 Artık 0 değil, kullanıcı girişi kadar
                ps.setDate(7, java.sql.Date.valueOf(ld));
                ps.executeUpdate();

                alert(Alert.AlertType.INFORMATION, "Başarılı", "Yeni lastik stoğa eklendi (" + adet + " adet).");
            }

            clearFields();

        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Hata", "Kayıt sırasında hata oluştu:\n" + ex.getMessage());
        }
    }




    @FXML
    private void handleGeri() {
        try {
            Stage stage = (Stage) txtMarka.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/gui/panel.fxml"))));
            stage.centerOnScreen();
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Navigasyon Hatası", "Panel açılamadı:\n" + e.getMessage());
        }
    }

    private void clearFields() {
        txtMarka.clear();
        comboTip.getSelectionModel().clearSelection();
        txtEbat.clear();
        txtAlis.clear();
        txtSatis.clear();
        txtTarih.clear();
        txtMarka.requestFocus();
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
}
