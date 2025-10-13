package model;

import javafx.beans.property.*;

public class Lastik {
    private final IntegerProperty id;
    private final StringProperty marka;
    private final StringProperty tip;
    private final StringProperty ebat;
    private final StringProperty hiz;
    private final StringProperty yuk;
    private final DoubleProperty alisFiyati;
    private final DoubleProperty satisFiyati;
    private final IntegerProperty adet;
    private final StringProperty tarih;

    public Lastik(int id, String marka, String tip, String ebat, String hiz, String yuk,
                  double alis, double satis, int adet, String tarih) {
        this.id = new SimpleIntegerProperty(id);
        this.marka = new SimpleStringProperty(marka);
        this.tip = new SimpleStringProperty(tip);
        this.ebat = new SimpleStringProperty(ebat);
        this.hiz = new SimpleStringProperty(hiz);
        this.yuk = new SimpleStringProperty(yuk);
        this.alisFiyati = new SimpleDoubleProperty(alis);
        this.satisFiyati = new SimpleDoubleProperty(satis);
        this.adet = new SimpleIntegerProperty(adet);
        this.tarih = new SimpleStringProperty(tarih);
    }

    // --- Getter / Setter / Property metotları ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty markaProperty() { return marka; }
    public StringProperty tipProperty() { return tip; }
    public StringProperty ebatProperty() { return ebat; }
    public StringProperty hizProperty() { return hiz; }
    public StringProperty yukProperty() { return yuk; }
    public DoubleProperty alisFiyatiProperty() { return alisFiyati; }
    public DoubleProperty satisFiyatiProperty() { return satisFiyati; }
    public IntegerProperty adetProperty() { return adet; }
    public StringProperty tarihProperty() { return tarih; }
}
