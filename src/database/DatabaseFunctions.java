package database;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import model.KeyValue;
import model.Musteri;
import model.MusteriLite;

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

    // -------------------------------------------------------------------
    // 🔹 TÜM MÜŞTERİLERİ GETİR
    // -------------------------------------------------------------------
    public static ObservableList<Musteri> musterileriGetir() {
        ObservableList<Musteri> liste = FXCollections.observableArrayList();
        String sql = "SELECT id, adi, soyadi, telefon, email, adres, kayitTarihi, borc FROM musteriler ORDER BY adi ASC";

        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Musteri m = new Musteri(
                        rs.getLong("id"),
                        rs.getString("adi"),
                        rs.getString("soyadi"),
                        rs.getString("telefon"),
                        rs.getString("email"),
                        rs.getString("adres"),
                        rs.getString("kayitTarihi"),
                        rs.getDouble("borc")
                );
                liste.add(m);
            }

        } catch (SQLException e) {
            System.err.println("❌ Müşteri listesi alınırken hata: " + e.getMessage());
        }

        return liste;
    }

    public static ObservableList<MusteriLite> musterileriGetirLite() {
        ObservableList<MusteriLite> musteriler = FXCollections.observableArrayList();
        String sql = "SELECT id, adi, soyadi, telefon, borc FROM musteriler ORDER BY adi ASC";

        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                musteriler.add(new MusteriLite(
                        rs.getLong("id"),
                        rs.getString("adi"),
                        rs.getString("soyadi"),
                        rs.getString("telefon"),
                        rs.getDouble("borc")
                ));
            }

        } catch (SQLException e) {
            System.err.println("❌ Müşterileri getirirken hata: " + e.getMessage());
        }

        return musteriler;
    }


    // -------------------------------------------------------------------
    // 🔹 MÜŞTERİ EKLEME METODU
    // -------------------------------------------------------------------
    public static boolean musteriEkle(Musteri m) {
        String sql = "INSERT INTO musteriler (adi, soyadi, telefon, email, adres, borc) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getAdi());
            ps.setString(2, m.getSoyadi());
            ps.setString(3, m.getTelefon());
            ps.setString(4, m.getEmail());
            ps.setString(5, m.getAdres());
            ps.setDouble(6, m.getBorc());

            int etkilenen = ps.executeUpdate();
            return etkilenen > 0;

        } catch (SQLException e) {
            String msg = e.getMessage();

            // 🔹 Unique constraint kontrolü
            if (msg != null && msg.contains("UQ__musteril")) {
                // özel hata fırlat
                throw new RuntimeException("Bu telefon numarasıyla zaten kayıtlı bir müşteri var!");
            } else {
                System.err.println("❌ Müşteri eklenirken hata: " + msg);
                throw new RuntimeException("Veritabanı hatası: " + msg);
            }
        }
    }

    // -------------------------------------------------------------------
    // 🔹 MÜŞTERİ GÜNCELLEME METODU
    // -------------------------------------------------------------------
    public static boolean musteriGuncelle(Musteri m) {
        String sql = "UPDATE musteriler SET adi=?, soyadi=?, telefon=?, email=?, adres=?, borc=? WHERE id=?";

        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getAdi());
            ps.setString(2, m.getSoyadi());
            ps.setString(3, m.getTelefon());
            ps.setString(4, m.getEmail());
            ps.setString(5, m.getAdres());
            ps.setDouble(6, m.getBorc());
            ps.setLong(7, m.getId());

            int etkilenen = ps.executeUpdate();
            return etkilenen > 0;

        } catch (Exception e) {
            System.err.println("❌ Müşteri güncellenirken hata: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------
    // 🔹 MÜŞTERİ SİLME METODU
    // -------------------------------------------------------------------
    public static boolean musteriSil(long musteriId) {
        String sql = "DELETE FROM musteriler WHERE id = ?";

        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, musteriId);
            int etkilenen = ps.executeUpdate();
            return etkilenen > 0;

        } catch (SQLException e) {
            System.err.println("❌ Müşteri silinirken hata: " + e.getMessage());
            return false;
        }
    }

}
