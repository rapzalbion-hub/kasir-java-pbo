/**
 * Kelas abstrak induk semua produk (ABSTRACTION + ENCAPSULATION).
 * Subclass wajib menentukan kategori sendiri (POLYMORPHISM).
 */
public abstract class Produk {
    private final String kode;
    private String nama;
    private double harga;
    private int stok;

    public Produk(String kode, String nama, double harga, int stok) {
        this.kode = kode;
        this.nama = nama;
        this.harga = harga;
        this.stok = stok;
    }

    public String getKode() { return kode; }
    public String getNama() { return nama; }
    public double getHarga() { return harga; }
    public int getStok() { return stok; }

    public void setNama(String nama) { this.nama = nama; }
    public void setHarga(double harga) { this.harga = harga; }

    public void tambahStok(int jumlah) { this.stok += jumlah; }

    public void kurangiStok(int jumlah) {
        if (jumlah > stok)
            throw new IllegalStateException("Stok " + nama + " tidak cukup");
        this.stok -= jumlah;
    }

    // Method abstrak: wajib di-override oleh subclass
    public abstract String getKategori();

    // Method yang boleh di-override
    public String getInfoTambahan() { return "-"; }

    @Override
    public String toString() { return kode + " - " + nama; }
}
