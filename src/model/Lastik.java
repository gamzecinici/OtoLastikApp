package model;

import javafx.beans.property.*;

/**
 * 🔹 Lastik model sınıfı
 * Veritabanındaki "urunler" tablosuna karşılık gelir.
 * Marka, model, tip, ebat, hız endeksi, yük endeksi, alış/satış fiyatı, adet ve tarih bilgilerini tutar.
 */
public class Lastik {

    // ======================================================
    //  ALANLAR
    // ======================================================
    private final IntegerProperty id;
    private final StringProperty marka;
    private final StringProperty model;        // 💙 Yeni eklendi
    private final StringProperty tip;
    private final StringProperty ebat;
    private final StringProperty hiz;
    private final StringProperty yuk;
    private final DoubleProperty alisFiyati;
    private final DoubleProperty satisFiyati;
    private final IntegerProperty adet;
    private final StringProperty tarih;

    // ======================================================
    //  YAPICI METOT (CONSTRUCTOR)
    // ======================================================
    public Lastik(int id, String marka, String model, String tip, String ebat,
                  String hiz, String yuk, double alis, double satis,
                  int adet, String tarih) {

        this.id = new SimpleIntegerProperty(id);
        this.marka = new SimpleStringProperty(marka);
        this.model = new SimpleStringProperty(model);   // 💙
        this.tip = new SimpleStringProperty(tip);
        this.ebat = new SimpleStringProperty(ebat);
        this.hiz = new SimpleStringProperty(hiz);
        this.yuk = new SimpleStringProperty(yuk);
        this.alisFiyati = new SimpleDoubleProperty(alis);
        this.satisFiyati = new SimpleDoubleProperty(satis);
        this.adet = new SimpleIntegerProperty(adet);
        this.tarih = new SimpleStringProperty(tarih);
    }

    // ======================================================
    //  GETTER / SETTER / PROPERTY METOTLARI
    // ======================================================

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getMarka() { return marka.get(); }
    public void setMarka(String marka) { this.marka.set(marka); }
    public StringProperty markaProperty() { return marka; }

    public String getModel() { return model.get(); }           // 💙 Yeni alan
    public void setModel(String model) { this.model.set(model); }
    public StringProperty modelProperty() { return model; }

    public String getTip() { return tip.get(); }
    public void setTip(String tip) { this.tip.set(tip); }
    public StringProperty tipProperty() { return tip; }

    public String getEbat() { return ebat.get(); }
    public void setEbat(String ebat) { this.ebat.set(ebat); }
    public StringProperty ebatProperty() { return ebat; }

    public String getHiz() { return hiz.get(); }
    public void setHiz(String hiz) { this.hiz.set(hiz); }
    public StringProperty hizProperty() { return hiz; }

    public String getYuk() { return yuk.get(); }
    public void setYuk(String yuk) { this.yuk.set(yuk); }
    public StringProperty yukProperty() { return yuk; }

    public double getAlisFiyati() { return alisFiyati.get(); }
    public void setAlisFiyati(double alisFiyati) { this.alisFiyati.set(alisFiyati); }
    public DoubleProperty alisFiyatiProperty() { return alisFiyati; }

    public double getSatisFiyati() { return satisFiyati.get(); }
    public void setSatisFiyati(double satisFiyati) { this.satisFiyati.set(satisFiyati); }
    public DoubleProperty satisFiyatiProperty() { return satisFiyati; }

    public int getAdet() { return adet.get(); }
    public void setAdet(int adet) { this.adet.set(adet); }
    public IntegerProperty adetProperty() { return adet; }

    public String getTarih() { return tarih.get(); }
    public void setTarih(String tarih) { this.tarih.set(tarih); }
    public StringProperty tarihProperty() { return tarih; }

    // ======================================================
    //  YARDIMCI METOTLAR (İsteğe bağlı)
    // ======================================================
    @Override
    public String toString() {
        return marka.get() + " " + model.get() + " " + tip.get() + " (" + ebat.get() + ")";
    }
}
