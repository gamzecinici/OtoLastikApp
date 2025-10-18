package model;

import javafx.beans.property.*;

/**
 * Satış kayıtlarını temsil eden model sınıfı.
 */
public class Satis {
    private final IntegerProperty id;
    private final StringProperty marka;
    private final StringProperty tip;
    private final StringProperty ebat;
    private final DoubleProperty satisFiyati;
    private final IntegerProperty adet;
    private final StringProperty tarih;
    private final StringProperty musteri;

    // --- Constructor ---
    public Satis(int id, String marka, String tip, String ebat, double satisFiyati, int adet, String tarih, String musteri) {
        this.id = new SimpleIntegerProperty(id);
        this.marka = new SimpleStringProperty(marka);
        this.tip = new SimpleStringProperty(tip);
        this.ebat = new SimpleStringProperty(ebat);
        this.satisFiyati = new SimpleDoubleProperty(satisFiyati);
        this.adet = new SimpleIntegerProperty(adet);
        this.tarih = new SimpleStringProperty(tarih);
        this.musteri = new SimpleStringProperty(musteri);
    }

    // --- Getter & Setter ---
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getMarka() { return marka.get(); }
    public void setMarka(String value) { marka.set(value); }
    public StringProperty markaProperty() { return marka; }

    public String getTip() { return tip.get(); }
    public void setTip(String value) { tip.set(value); }
    public StringProperty tipProperty() { return tip; }

    public String getEbat() { return ebat.get(); }
    public void setEbat(String value) { ebat.set(value); }
    public StringProperty ebatProperty() { return ebat; }

    public double getSatisFiyati() { return satisFiyati.get(); }
    public void setSatisFiyati(double value) { satisFiyati.set(value); }
    public DoubleProperty satisFiyatiProperty() { return satisFiyati; }

    public int getAdet() { return adet.get(); }
    public void setAdet(int value) { adet.set(value); }
    public IntegerProperty adetProperty() { return adet; }

    public String getTarih() { return tarih.get(); }
    public void setTarih(String value) { tarih.set(value); }
    public StringProperty tarihProperty() { return tarih; }

    public String getMusteri() { return musteri.get(); }
    public void setMusteri(String value) { musteri.set(value); }
    public StringProperty musteriProperty() { return musteri; }
}
