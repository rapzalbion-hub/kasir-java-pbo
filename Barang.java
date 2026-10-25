public class Barang extends Produk {
    private String merek;

    public Barang(String kode, String nama, double harga, int stok, String merek) {
        super(kode, nama, harga, stok);
        this.merek = merek;
    }

    @Override public String getKategori() { return "Barang"; }
    @Override public String getInfoTambahan() { return "Merek: " + merek; }
}
