package gui;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Satis;

import java.sql.*;
import java.time.format.DateTimeFormatter;

public class SatislarController {

    @FXML private ComboBox<String> cmbMarka, cmbTip, cmbEbat;
    @FXML private TextField txtAdet, txtAlis, txtSatis, txtStok;
    @FXML private TableView<Satis> tblSatislar;
    @FXML private TableColumn<Satis, String> colTarih, colMarka, colTip, colEbat;
    @FXML private TableColumn<Satis, Integer> colAdet, colKalan;
    @FXML private TableColumn<Satis, Double> colAlis, colSatis;

    private final ObservableList<Satis> satisListesi = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colTarih.setCellValueFactory(c -> c.getValue().tarihProperty());
        colMarka.setCellValueFactory(c -> c.getValue().markaProperty());
        colTip.setCellValueFactory(c -> c.getValue().tipProperty());
        colEbat.setCellValueFactory(c -> c.getValue().ebatProperty());
        colAdet.setCellValueFactory(c -> c.getValue().adetProperty().asObject());
        colAlis.setCellValueFactory(c -> c.getValue().alisFiyatProperty().asObject());
        colSatis.setCellValueFactory(c -> c.getValue().satisFiyatProperty().asObject());
        colKalan.setCellValueFactory(c -> c.getValue().stokProperty().asObject());

        markaGetir();
        satislariListele();

        cmbMarka.setOnAction(e -> tipGetir());
        cmbTip.setOnAction(e -> ebatGetir());
        cmbEbat.setOnAction(e -> fiyatVeStokGetir());
    }

    private void markaGetir() {
        cmbMarka.getItems().clear();
        try (Connection c = DatabaseConnection.baglan();
             PreparedStatement ps = c.prepareStatement("SELECT DISTINCT marka FROM Lastikler");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cmbMarka.getItems().add(rs.getString("marka"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void tipGetir() {
        cmbTip.getItems().clear();
        String marka = cmbMarka.getValue();
        if (marka == null) return;

        try (Connection c = DatabaseConnection.baglan();
             PreparedStatement ps = c.prepareStatement("SELECT DISTINCT tip FROM Lastikler WHERE marka=?")) {
            ps.setString(1, marka);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) cmbTip.getItems().add(rs.getString("tip"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void ebatGetir() {
        cmbEbat.getItems().clear();
        String marka = cmbMarka.getValue();
        String tip = cmbTip.getValue();
        if (marka == null || tip == null) return;

        try (Connection c = DatabaseConnection.baglan();
             PreparedStatement ps = c.prepareStatement("SELECT DISTINCT ebat FROM Lastikler WHERE marka=? AND tip=?")) {
            ps.setString(1, marka);
            ps.setString(2, tip);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) cmbEbat.getItems().add(rs.getString("ebat"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void fiyatVeStokGetir() {
        String marka = cmbMarka.getValue();
        String tip = cmbTip.getValue();
        String ebat = cmbEbat.getValue();
        if (marka == null || tip == null || ebat == null) return;

        try (Connection c = DatabaseConnection.baglan();
             PreparedStatement ps = c.prepareStatement("SELECT alis_fiyati, satis_fiyati, adet FROM Lastikler WHERE marka=? AND tip=? AND ebat=?")) {
            ps.setString(1, marka);
            ps.setString(2, tip);
            ps.setString(3, ebat);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtAlis.setText(String.valueOf(rs.getDouble("alis_fiyati")));
                txtSatis.setText(String.valueOf(rs.getDouble("satis_fiyati")));
                txtStok.setText(String.valueOf(rs.getInt("adet")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void satisYap() {
        String marka = cmbMarka.getValue();
        String tip = cmbTip.getValue();
        String ebat = cmbEbat.getValue();

        if (marka == null || tip == null || ebat == null || txtAdet.getText().isEmpty()) {
            uyari("Lütfen tüm alanları doldurun!");
            return;
        }

        int adet = Integer.parseInt(txtAdet.getText());
        int stok = Integer.parseInt(txtStok.getText());
        if (adet > stok) {
            uyari("Yeterli stok yok!");
            return;
        }

        double alis = Double.parseDouble(txtAlis.getText());
        double satis = Double.parseDouble(txtSatis.getText());
        int kalan = stok - adet;

        try (Connection c = DatabaseConnection.baglan()) {
            // stok güncelle
            PreparedStatement ps1 = c.prepareStatement("UPDATE Lastikler SET adet = ? WHERE marka=? AND tip=? AND ebat=?");
            ps1.setInt(1, kalan);
            ps1.setString(2, marka);
            ps1.setString(3, tip);
            ps1.setString(4, ebat);
            ps1.executeUpdate();

            // satış kaydı
            PreparedStatement ps2 = c.prepareStatement("""
                INSERT INTO Satislar (lastik_adi, tip, adet, alis_fiyat, satis_fiyat, toplam_alis, toplam_satis, tarih)
                VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE())
                """);
            ps2.setString(1, marka + " " + ebat);
            ps2.setString(2, tip);
            ps2.setInt(3, adet);
            ps2.setDouble(4, alis);
            ps2.setDouble(5, satis);
            ps2.setDouble(6, alis * adet);
            ps2.setDouble(7, satis * adet);
            ps2.executeUpdate();

            // tabloya yeni satır ekle (yenilemeden)
            Satis yeniSatis = new Satis(marka, tip, ebat, adet, alis, satis, kalan,
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            satisListesi.add(0, yeniSatis); // en üste ekler

            uyari("Satış tamamlandı!\nKalan stok: " + kalan);
            temizleAlanlar();

        } catch (Exception e) {
            e.printStackTrace();
            uyari("Hata: " + e.getMessage());
        }
    }

    private void satislariListele() {
        satisListesi.clear();
        String sql = """
                SELECT s.*, l.adet AS kalanStok
                FROM Satislar s
                JOIN Lastikler l ON s.lastik_adi LIKE CONCAT(l.marka, '%') AND s.tip = l.tip
                ORDER BY s.tarih DESC
                """;

        try (Connection c = DatabaseConnection.baglan();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            while (rs.next()) {
                satisListesi.add(new Satis(
                        rs.getString("lastik_adi").split(" ")[0],
                        rs.getString("tip"),
                        rs.getString("lastik_adi").substring(rs.getString("lastik_adi").indexOf(" ") + 1),
                        rs.getInt("adet"),
                        rs.getDouble("alis_fiyat"),
                        rs.getDouble("satis_fiyat"),
                        rs.getInt("kalanStok"),
                        rs.getTimestamp("tarih").toLocalDateTime().format(f)
                ));
            }
            tblSatislar.setItems(satisListesi);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void temizleAlanlar() {
        txtAdet.clear();
        txtAlis.clear();
        txtSatis.clear();
        txtStok.clear();
        cmbEbat.getItems().clear();
        cmbTip.getItems().clear();
        cmbMarka.getSelectionModel().clearSelection();
    }

    @FXML
    private void geriDon() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/panel.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tblSatislar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Lastikçi Paneli");
        } catch (Exception e) {
            uyari("Ana panele dönülürken hata oluştu: " + e.getMessage());
        }
    }

    private void uyari(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
