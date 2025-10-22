package model;

/**
 * Ürün alım geçmişi verilerini temsil eder.
 * Marka, model, alım tarihi, alış fiyatı, adet ve açıklama bilgilerini içerir.
 */
public class AlimGecmisi {
    private String marka;
    private String model;
    private String alimTarihi;
    private double alisFiyati;
    private int alinanAdet;
    private String aciklama;

    // 🔹 Constructor
    public AlimGecmisi(String marka, String model, String alimTarihi, double alisFiyati, int alinanAdet, String aciklama) {
        this.marka = marka;
        this.model = model;
        this.alimTarihi = alimTarihi;
        this.alisFiyati = alisFiyati;
        this.alinanAdet = alinanAdet;
        this.aciklama = aciklama;
    }

    // 🔹 Getter'lar
    public String getMarka() { return marka; }
    public String getModel() { return model; }
    public String getAlimTarihi() { return alimTarihi; }
    public double getAlisFiyati() { return alisFiyati; }
    public int getAlinanAdet() { return alinanAdet; }
    public String getAciklama() { return aciklama; }
}
