public class Minuman extends Produk {
    private String ukuran;

    public Minuman(String kode, String nama, double harga, int stok, String ukuran) {
        super(kode, nama, harga, stok);
        this.ukuran = ukuran;
    }

    @Override public String getKategori() { return "Minuman"; }
    @Override public String getInfoTambahan() { return "Ukuran: " + ukuran; }
}
