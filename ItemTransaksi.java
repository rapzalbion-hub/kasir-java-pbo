/** Satu baris di keranjang belanja (komposisi: memiliki Produk). */
public class ItemTransaksi {
    private final Produk produk;
    private int jumlah;

    public ItemTransaksi(Produk produk, int jumlah) {
        this.produk = produk;
        this.jumlah = jumlah;
    }

    public Produk getProduk() { return produk; }
    public int getJumlah() { return jumlah; }
    public void tambahJumlah(int n) { this.jumlah += n; }

    public double getSubtotal() { return produk.getHarga() * jumlah; }
}
