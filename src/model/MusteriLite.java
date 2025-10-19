package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusteriLite {
    private final long id;
    private final String adi;
    private final String soyadi;
    private final String telefon;
    private final double borc;
    private final String gorunenAd;

    // 🔹 Tüm müşteriler için gorunenAd → id eşleştirmesini tutan statik map
    private static final Map<String, Long> gorunenAdIdMap = new HashMap<>();

    public MusteriLite(long id, String adi, String soyadi, String telefon, double borc) {
        this.id = id;
        this.adi = adi;
        this.soyadi = soyadi;
        this.telefon = telefon;
        this.borc = borc;
        this.gorunenAd = this.adi + " " + this.soyadi + " (" + this.telefon + ")";
    }

    public long getId() { return id; }
    public String getAdi() { return adi; }
    public String getSoyadi() { return soyadi; }
    public String getTelefon() { return telefon; }
    public double getBorc() { return borc; }
    public String getGorunenAd() { return gorunenAd; }

    // 🔹 Haritayı başlatmak için (veritabanından çekilen müşteri listesi ile)
    public static void initializeMap(List<MusteriLite> musteriListesi) {
        gorunenAdIdMap.clear();
        for (MusteriLite m : musteriListesi) {
            gorunenAdIdMap.put(m.getGorunenAd(), m.getId());
        }
    }

    // 🔹 Görünen ad üzerinden id getiren optimize edilmiş metod
    public static long getIdFromGorunenAd(Object gorunenAd) {
        return gorunenAdIdMap.getOrDefault(String.valueOf(gorunenAd), -1L);
    }

    @Override
    public String toString() {
        return gorunenAd;
    }
}
