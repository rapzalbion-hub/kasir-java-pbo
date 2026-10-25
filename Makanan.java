/** Pewarisan: Makanan IS-A Produk. */
public class Makanan extends Produk {
    private String kadaluarsa;

    public Makanan(String kode, String nama, double harga, int stok, String kadaluarsa) {
        super(kode, nama, harga, stok);
        this.kadaluarsa = kadaluarsa;
    }

    @Override public String getKategori() { return "Makanan"; }
    @Override public String getInfoTambahan() { return "Exp: " + kadaluarsa; }
}
