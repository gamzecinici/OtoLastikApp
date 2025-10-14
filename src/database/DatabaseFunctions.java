package database;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.KeyValue;
import java.sql.*;

/**
 * Veritabanındaki yardımcı işlemleri (listeleme, ekleme vb.) yöneten sınıf.
 * Tüm ComboBox verileri (marka, tip, ebat, hız, yük) buradan çekilir.
 */
public class DatabaseFunctions {

    // -------------------------------------------------------------------
    // 🔸 GETİRME METOTLARI
    // -------------------------------------------------------------------

    public static ObservableList<KeyValue> markalariGetir() {
        ObservableList<KeyValue> markalar = FXCollections.observableArrayList();
        String sql = "SELECT id, markaAdi FROM markalar ORDER BY markaAdi ASC";
        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                markalar.add(new KeyValue(rs.getInt("id"), rs.getString("markaAdi")));
            }
        } catch (SQLException e) {
            System.err.println("❌ Markaları getirirken hata: " + e.getMessage());
        }
        return markalar;
    }

    public static ObservableList<KeyValue> tipleriGetir() {
        ObservableList<KeyValue> tipler = FXCollections.observableArrayList();
        String sql = "SELECT id, tip FROM tipler ORDER BY tip ASC";
        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tipler.add(new KeyValue(rs.getInt("id"), rs.getString("tip")));
            }
        } catch (SQLException e) {
            System.err.println("❌ Tipleri getirirken hata: " + e.getMessage());
        }
        return tipler;
    }

    public static ObservableList<KeyValue> hizGetir() {
        ObservableList<KeyValue> hizlar = FXCollections.observableArrayList();
        String sql = "SELECT id, hizEndeks FROM hizEndeksleri ORDER BY hizEndeks ASC";
        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hizlar.add(new KeyValue(rs.getInt("id"), rs.getString("hizEndeks")));
            }
        } catch (SQLException e) {
            System.err.println("❌ Hız endekslerini getirirken hata: " + e.getMessage());
        }
        return hizlar;
    }

    public static ObservableList<KeyValue> yukGetir() {
        ObservableList<KeyValue> yukler = FXCollections.observableArrayList();
        String sql = "SELECT id, yukEndeks FROM yukEndeksleri ORDER BY yukEndeks ASC";
        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                yukler.add(new KeyValue(rs.getInt("id"), rs.getString("yukEndeks")));
            }
        } catch (SQLException e) {
            System.err.println("❌ Yük endekslerini getirirken hata: " + e.getMessage());
        }
        return yukler;
    }

    public static ObservableList<KeyValue> ebatlariGetir() {
        ObservableList<KeyValue> ebatlar = FXCollections.observableArrayList();
        String sql = "SELECT id, genislik, yukseklik, jant FROM ebatlar " +
                "ORDER BY genislik ASC, yukseklik ASC, jant ASC";
        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                double g = rs.getDouble("genislik");
                double y = rs.getDouble("yukseklik");
                double j = rs.getDouble("jant");
                String ebatAdi = String.format("%.0f/%.0f/R%.0f", g, y, j);
                ebatlar.add(new KeyValue(id, ebatAdi));
            }
        } catch (SQLException e) {
            System.err.println("❌ Ebatları getirirken hata: " + e.getMessage());
        }
        return ebatlar;
    }

    // -------------------------------------------------------------------
    // 🔸 EKLEME METOTLARI (TÜMÜNDE TEKRAR KONTROL VAR)
    // -------------------------------------------------------------------

    public static boolean markaEkle(String markaAdi) {

        String sql = "INSERT INTO markalar (markaAdi, mensei) VALUES (?, 'Bilinmiyor')";
        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, markaAdi);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Marka ekleme hatası: " + e.getMessage());
            return false;
        }
    }

    public static boolean tipEkle(String tip) {
        String sql = "INSERT INTO tipler (tip) VALUES (?)";
        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tip);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Tip ekleme hatası: " + e.getMessage());
            return false;
        }
    }

    public static boolean hizEkle(String hizEndeks, String maksimumHiz) {

        String sql = "INSERT INTO hizEndeksleri (hizEndeks, maksimumHiz) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hizEndeks);
            ps.setString(2, maksimumHiz);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Hız endeksi ekleme hatası: " + e.getMessage());
            return false;
        }
    }

    public static boolean yukEkle(String yukEndeks, String lastikBasinaDusenKg) {
        String sql = "INSERT INTO yukEndeksleri (yukEndeks, lastikBasinaDusenKg) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, yukEndeks);
            ps.setString(2, lastikBasinaDusenKg);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Yük endeksi ekleme hatası: " + e.getMessage());
            return false;
        }
    }

    public static boolean ebatEkle(double genislik, double yukseklik, double jant) {
        String ebatText = String.format("%.0f/%.0f/R%.0f", genislik, yukseklik, jant);

        String sql = "INSERT INTO ebatlar (genislik, yukseklik, jant) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, genislik);
            ps.setDouble(2, yukseklik);
            ps.setDouble(3, jant);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Ebat ekleme hatası: " + e.getMessage());
            return false;
        }
    }
}
