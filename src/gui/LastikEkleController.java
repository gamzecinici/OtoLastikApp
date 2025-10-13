package gui;

import database.DatabaseConnection;
import database.DatabaseFunctions;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class LastikEkleController {

    @FXML private ComboBox<String> comboMarka, comboTip, comboEbat, comboHiz, comboYuk;
    @FXML private TextField txtModel, txtAlis, txtSatis, txtAdet;

    @FXML
    public void initialize() {
        refreshCombos();
    }

    private void refreshCombos() {
        comboMarka.setItems(DatabaseFunctions.getMarkalar());
        comboTip.setItems(DatabaseFunctions.getTipler());
        comboEbat.setItems(DatabaseFunctions.getEbatlar());
        comboHiz.setItems(DatabaseFunctions.getHizEndeksleri());
        comboYuk.setItems(DatabaseFunctions.getYukEndeksleri());
    }

    @FXML
    private void handleKaydet() {
        String marka = comboMarka.getValue();
        String model = txtModel.getText();
        String tip = comboTip.getValue();
        String ebat = comboEbat.getValue();
        String hiz = comboHiz.getValue();
        String yuk = comboYuk.getValue();
        String alis = txtAlis.getText();
        String satis = txtSatis.getText();
        String adet = txtAdet.getText();

        if (marka == null || model.isEmpty() || tip == null || ebat == null ||
                hiz == null || yuk == null || alis.isEmpty() || satis.isEmpty() || adet.isEmpty()) {
            alert(Alert.AlertType.WARNING, "Eksik Bilgi", "Lütfen tüm alanları doldurun!");
            return;
        }

        try (Connection conn = DatabaseConnection.baglan()) {
            int markaId = DatabaseFunctions.getIdByName("markalar", marka, conn);
            int tipId = DatabaseFunctions.getIdByName("tipler", tip, conn);
            int ebatId = DatabaseFunctions.getIdByName("ebatlar", ebat, conn);
            int hizId = DatabaseFunctions.getIdByName("hizEndeksleri", hiz, conn);
            int yukId = DatabaseFunctions.getIdByName("yukEndeksleri", yuk, conn);

            BigDecimal alisF = new BigDecimal(alis);
            BigDecimal satisF = new BigDecimal(satis);
            int adetInt = Integer.parseInt(adet);

            String check = "SELECT adet FROM urunler WHERE markaId=? AND model=? AND tipId=? AND ebatId=? AND hizEndeksId=? AND yukEndeksId=?";
            PreparedStatement cs = conn.prepareStatement(check);
            cs.setInt(1, markaId);
            cs.setString(2, model);
            cs.setInt(3, tipId);
            cs.setInt(4, ebatId);
            cs.setInt(5, hizId);
            cs.setInt(6, yukId);
            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                int yeni = rs.getInt("adet") + adetInt;
                String update = "UPDATE urunler SET adet=?, alisFiyati=?, satisFiyati=?, guncellenmeTarihi=GETDATE() WHERE markaId=? AND model=? AND tipId=? AND ebatId=? AND hizEndeksId=? AND yukEndeksId=?";
                PreparedStatement ps = conn.prepareStatement(update);
                ps.setInt(1, yeni);
                ps.setBigDecimal(2, alisF);
                ps.setBigDecimal(3, satisF);
                ps.setInt(4, markaId);
                ps.setString(5, model);
                ps.setInt(6, tipId);
                ps.setInt(7, ebatId);
                ps.setInt(8, hizId);
                ps.setInt(9, yukId);
                ps.executeUpdate();
                alert(Alert.AlertType.INFORMATION, "Stok Güncellendi", "Yeni stok: " + yeni);
            } else {
                String insert = "INSERT INTO urunler (markaId, model, tipId, ebatId, hizEndeksId, yukEndeksId, adet, alisFiyati, satisFiyati, eklenmeTarihi, guncellenmeTarihi, aktif) VALUES (?,?,?,?,?,?,?,?,?,GETDATE(),GETDATE(),1)";
                PreparedStatement ps = conn.prepareStatement(insert);
                ps.setInt(1, markaId);
                ps.setString(2, model);
                ps.setInt(3, tipId);
                ps.setInt(4, ebatId);
                ps.setInt(5, hizId);
                ps.setInt(6, yukId);
                ps.setInt(7, adetInt);
                ps.setBigDecimal(8, alisF);
                ps.setBigDecimal(9, satisF);
                ps.executeUpdate();
                alert(Alert.AlertType.INFORMATION, "Başarılı", "Yeni ürün stoğa eklendi.");
            }

            clear();
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Hata", e.getMessage());
        }
    }

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

    @FXML private void handleMarkaEkle() { addNewValue("markalar", "Yeni Marka Ekle"); }
    @FXML private void handleTipEkle() { addNewValue("tipler", "Yeni Tip Ekle"); }
    @FXML private void handleEbatEkle() { addNewValue("ebatlar", "Yeni Ebat Ekle"); }
    @FXML private void handleHizEkle() { addNewValue("hizEndeksleri", "Yeni Hız Endeksi Ekle"); }
    @FXML private void handleYukEkle() { addNewValue("yukEndeksleri", "Yeni Yük Endeksi Ekle"); }

    private void addNewValue(String table, String title) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText("Değer girin:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> {
            DatabaseFunctions.addValue(table, value);
            refreshCombos();
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

    private void alert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
