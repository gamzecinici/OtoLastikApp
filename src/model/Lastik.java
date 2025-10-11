package model;

import javafx.beans.property.*;

public class Lastik {
    private final StringProperty marka;
    private final StringProperty tip;
    private final StringProperty ebat;
    private final DoubleProperty alisFiyati;
    private final DoubleProperty satisFiyati;
    private final IntegerProperty adet;
    private final StringProperty tarih;

    public Lastik(String marka, String tip, String ebat, double alis, double satis, int adet, String tarih) {
        this.marka = new SimpleStringProperty(marka);
        this.tip = new SimpleStringProperty(tip);
        this.ebat = new SimpleStringProperty(ebat);
        this.alisFiyati = new SimpleDoubleProperty(alis);
        this.satisFiyati = new SimpleDoubleProperty(satis);
        this.adet = new SimpleIntegerProperty(adet);
        this.tarih = new SimpleStringProperty(tarih);
    }

    public StringProperty markaProperty() { return marka; }
    public StringProperty tipProperty() { return tip; }
    public StringProperty ebatProperty() { return ebat; }
    public DoubleProperty alisFiyatiProperty() { return alisFiyati; }
    public DoubleProperty satisFiyatiProperty() { return satisFiyati; }
    public IntegerProperty adetProperty() { return adet; }
    public StringProperty tarihProperty() { return tarih; }
}
