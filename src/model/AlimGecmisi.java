package model;

public class AlimGecmisi {
    private int urunId;
    private String alimTarihi;
    private double alisFiyati;
    private int alinanAdet;
    private String aciklama;

    public AlimGecmisi(int urunId, String alimTarihi, double alisFiyati, int alinanAdet, String aciklama) {
        this.urunId = urunId;
        this.alimTarihi = alimTarihi;
        this.alisFiyati = alisFiyati;
        this.alinanAdet = alinanAdet;
        this.aciklama = aciklama;
    }

    public int getUrunId() { return urunId; }
    public String getAlimTarihi() { return alimTarihi; }
    public double getAlisFiyati() { return alisFiyati; }
    public int getAlinanAdet() { return alinanAdet; }
    public String getAciklama() { return aciklama; }
}
