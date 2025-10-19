package model;

public class Satis {
    private long id;
    private String marka;
    private String model;
    private String ebat;
    private String hizEndeksi;
    private String yukEndeksi;
    private String musteriAdiSoyadi;
    private String musteriTelefon;
    private int satilanAdet;
    private double alinacakTutar;
    private double alinanTutar;
    private double kalanTutar;
    private String tarih;
    private boolean odendi;

    public Satis(long id, String marka, String model, String ebat, String hizEndeksi, String yukEndeksi,
                 String musteriAdiSoyadi, String musteriTelefon, int satilanAdet,
                 double alinacakTutar, double alinanTutar, double kalanTutar,
                 String tarih, boolean odendi) {
        this.id = id;
        this.marka = marka;
        this.model = model;
        this.ebat = ebat;
        this.hizEndeksi = hizEndeksi;
        this.yukEndeksi = yukEndeksi;
        this.musteriAdiSoyadi = musteriAdiSoyadi;
        this.musteriTelefon = musteriTelefon;
        this.satilanAdet = satilanAdet;
        this.alinacakTutar = alinacakTutar;
        this.alinanTutar = alinanTutar;
        this.kalanTutar = kalanTutar;
        this.tarih = tarih;
        this.odendi = odendi;
    }

    // 🔹 Getter – Setter
    public long getId() { return id; }
    public String getMarka() { return marka; }
    public String getModel() { return model; }
    public String getEbat() { return ebat; }
    public String getHizEndeksi() { return hizEndeksi; }
    public String getYukEndeksi() { return yukEndeksi; }
    public String getMusteriAdiSoyadi() { return musteriAdiSoyadi; }
    public String getMusteriTelefon() { return musteriTelefon; }
    public int getSatilanAdet() { return satilanAdet; }
    public double getAlinacakTutar() { return alinacakTutar; }
    public double getAlinanTutar() { return alinanTutar; }
    public double getKalanTutar() { return kalanTutar; }
    public String getTarih() { return tarih; }
    public boolean isOdendi() { return odendi; }

    public void setAlinanTutar(double alinanTutar) { this.alinanTutar = alinanTutar; }
    public void setOdendi(boolean odendi) { this.odendi = odendi; }
}
