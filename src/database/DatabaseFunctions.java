package database;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class DatabaseFunctions {

    // --- Liste doldurma metodları ---
    public static ObservableList<String> getMarkalar() { return getList("markalar"); }
    public static ObservableList<String> getTipler() { return getList("tipler"); }
    public static ObservableList<String> getEbatlar() { return getList("ebatlar"); }
    public static ObservableList<String> getHizEndeksleri() { return getList("hizEndeksleri"); }
    public static ObservableList<String> getYukEndeksleri() { return getList("yukEndeksleri"); }

    // --- Ortak liste çekme metodu ---
    private static ObservableList<String> getList(String table) {
        ObservableList<String> list = FXCollections.observableArrayList();
        String sql = "SELECT ad FROM " + table + " ORDER BY ad"; // ✅ aktif kolonu kaldırıldı

        try (Connection conn = DatabaseConnection.baglan();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(rs.getString("ad"));
            }

        } catch (Exception e) {
            System.err.println("Liste alınırken hata: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // --- Yeni değer ekleme metodu (örneğin yeni marka/tip/ebat ekleme) ---
    public static void addValue(String table, String value) {
        String sql;

        // aktif sütunu olan tablolara göre dinamik sorgu
        if (hasActiveColumn(table)) {
            sql = "INSERT INTO " + table + " (ad, aktif) VALUES (?, 1)";
        } else {
            sql = "INSERT INTO " + table + " (ad) VALUES (?)";
        }

        try (Connection conn = DatabaseConnection.baglan();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, value);
            ps.executeUpdate();
            System.out.println("Yeni değer eklendi: " + value + " -> " + table);

        } catch (Exception e) {
            System.err.println("Değer eklenemedi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- İsme göre ID döndürür (örn. markalar tablosunda 'Michelin' → id:3) ---
    public static int getIdByName(String table, String name, Connection conn) throws SQLException {
        String sql = "SELECT id FROM " + table + " WHERE ad = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        throw new SQLException("ID bulunamadı: " + name + " (" + table + ")");
    }

    // --- aktif kolonu olup olmadığını anlamak için küçük yardımcı fonksiyon ---
    private static boolean hasActiveColumn(String table) {
        // sadece urunler tablosu haricindeki yardımcı tablolarda kullanılacak
        // bu, SQL sorgusunun çakışmasını önler
        return table.equalsIgnoreCase("markalar")
                || table.equalsIgnoreCase("tipler")
                || table.equalsIgnoreCase("ebatlar");
    }
}
