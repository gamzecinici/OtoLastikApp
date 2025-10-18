package model;

import javafx.beans.property.*;

/**
 * Müşteri tablosundaki verileri temsil eder.
 * TableView ile otomatik olarak bağlanabilmesi için JavaFX Property tipleri kullanılmıştır.
 */
public class Musteri {
    private final LongProperty id;
    private final StringProperty adi;
    private final StringProperty soyadi;
    private final StringProperty telefon;
    private final StringProperty email;
    private final StringProperty adres;
    private final StringProperty kayitTarihi;
    private final DoubleProperty borc;

    // 🔹 Yapıcı (Constructor)
    public Musteri(long id, String adi, String soyadi, String telefon, String email, String adres, String kayitTarihi, double borc) {
        this.id = new SimpleLongProperty(id);
        this.adi = new SimpleStringProperty(adi);
        this.soyadi = new SimpleStringProperty(soyadi);
        this.telefon = new SimpleStringProperty(telefon);
        this.email = new SimpleStringProperty(email);
        this.adres = new SimpleStringProperty(adres);
        this.kayitTarihi = new SimpleStringProperty(kayitTarihi);
        this.borc = new SimpleDoubleProperty(borc);
    }

    // --------------------------------------------------------------------
    // 🔹 Property (JavaFX TableView için)
    // --------------------------------------------------------------------
    public LongProperty idProperty() { return id; }
    public StringProperty adiProperty() { return adi; }
    public StringProperty soyadiProperty() { return soyadi; }
    public StringProperty telefonProperty() { return telefon; }
    public StringProperty emailProperty() { return email; }
    public StringProperty adresProperty() { return adres; }
    public StringProperty kayitTarihiProperty() { return kayitTarihi; }
    public DoubleProperty borcProperty() { return borc; }

    // --------------------------------------------------------------------
    // 🔹 Getter Metotları (veri okuma)
    // --------------------------------------------------------------------
    public long getId() { return id.get(); }
    public String getAdi() { return adi.get(); }
    public String getSoyadi() { return soyadi.get(); }
    public String getTelefon() { return telefon.get(); }
    public String getEmail() { return email.get(); }
    public String getAdres() { return adres.get(); }
    public String getKayitTarihi() { return kayitTarihi.get(); }
    public double getBorc() { return borc.get(); }

    // --------------------------------------------------------------------
    // 🔹 Setter Metotları (veri güncelleme)
    // --------------------------------------------------------------------
    public void setAdi(String adi) { this.adi.set(adi); }
    public void setSoyadi(String soyadi) { this.soyadi.set(soyadi); }
    public void setTelefon(String telefon) { this.telefon.set(telefon); }
    public void setEmail(String email) { this.email.set(email); }
    public void setAdres(String adres) { this.adres.set(adres); }
    public void setKayitTarihi(String kayitTarihi) { this.kayitTarihi.set(kayitTarihi); }
    public void setBorc(double borc) { this.borc.set(borc); }
}
