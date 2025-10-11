package model;

import javafx.beans.property.*;

public class Satis {

    private final StringProperty marka;
    private final StringProperty tip;
    private final StringProperty ebat;
    private final IntegerProperty adet;
    private final DoubleProperty alisFiyat;
    private final DoubleProperty satisFiyat;
    private final IntegerProperty stok;
    private final StringProperty tarih;

    public Satis(String marka, String tip, String ebat, int adet,
                 double alisFiyat, double satisFiyat, int stok, String tarih) {
        this.marka = new SimpleStringProperty(marka);
        this.tip = new SimpleStringProperty(tip);
        this.ebat = new SimpleStringProperty(ebat);
        this.adet = new SimpleIntegerProperty(adet);
        this.alisFiyat = new SimpleDoubleProperty(alisFiyat);
        this.satisFiyat = new SimpleDoubleProperty(satisFiyat);
        this.stok = new SimpleIntegerProperty(stok);
        this.tarih = new SimpleStringProperty(tarih);
    }

    public StringProperty markaProperty() { return marka; }
    public StringProperty tipProperty() { return tip; }
    public StringProperty ebatProperty() { return ebat; }
    public IntegerProperty adetProperty() { return adet; }
    public DoubleProperty alisFiyatProperty() { return alisFiyat; }
    public DoubleProperty satisFiyatProperty() { return satisFiyat; }
    public IntegerProperty stokProperty() { return stok; }
    public StringProperty tarihProperty() { return tarih; }
}
