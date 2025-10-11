package database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    // Veritabanı bağlantı bilgileri
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=lastikDb;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa"; // SQL Server kullanıcı adı
    private static final String PASSWORD = "123"; // Senin belirlediğin güçlü şifre

    // Bağlantı metodu
    public static Connection baglan() {
        try {
            System.out.println("🔌 Veritabanına bağlanmaya çalışılıyor...");

            // Kullanıcı adı ve şifre ile bağlantı oluştur
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("✅ Veritabanına başarıyla bağlanıldı!");
            return conn;
        } catch (Exception e) {
            System.out.println("❌ Bağlantı hatası: " + e.getMessage());
            return null;
        }
    }
}
