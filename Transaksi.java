import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Transaksi {
    private final String nomor;
    private final LocalDateTime waktu = LocalDateTime.now();
    private final List<ItemTransaksi> items = new ArrayList<>();
    private Diskon diskon = new TanpaDiskon();   // polymorphism lewat interface

    public Transaksi(String nomor) { this.nomor = nomor; }

    public void tambahItem(Produk p, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Jumlah harus lebih dari 0");
        for (ItemTransaksi it : items) {
            if (it.getProduk() == p) {
                if (it.getJumlah() + qty > p.getStok())
                    throw new IllegalStateException("Stok " + p.getNama() + " tidak cukup (sisa " + p.getStok() + ")");
                it.tambahJumlah(qty);
                return;
            }
        }
        if (qty > p.getStok())
            throw new IllegalStateException("Stok " + p.getNama() + " tidak cukup (sisa " + p.getStok() + ")");
        items.add(new ItemTransaksi(p, qty));
    }

    public void hapusItem(int index) { items.remove(index); }

    public void setDiskon(Diskon diskon) { this.diskon = diskon; }
    public Diskon getDiskon() { return diskon; }
    public List<ItemTransaksi> getItems() { return Collections.unmodifiableList(items); }
    public String getNomor() { return nomor; }

    public int getJumlahItem() { return items.stream().mapToInt(ItemTransaksi::getJumlah).sum(); }
    public double getSubtotal() { return items.stream().mapToDouble(ItemTransaksi::getSubtotal).sum(); }
    public double getNilaiDiskon() { return diskon.hitung(getSubtotal()); }
    /** Total dibulatkan ke rupiah penuh. */
    public double getTotal() { return Math.round(getSubtotal() - getNilaiDiskon()); }

    /** Memproses pembayaran: mengurangi stok dan mengembalikan kembalian. */
    public double bayar(double uang) {
        if (items.isEmpty()) throw new IllegalStateException("Keranjang masih kosong");
        if (uang < getTotal()) throw new IllegalArgumentException("Uang pembayaran kurang");
        for (ItemTransaksi it : items) it.getProduk().kurangiStok(it.getJumlah());
        return uang - getTotal();
    }

    /** Bayar + hasilkan catatan untuk riwayat penjualan. */
    public CatatanPenjualan proses(double uang) {
        double kembali = bayar(uang);
        return new CatatanPenjualan(nomor, waktu, getJumlahItem(), getTotal(), uang, kembali, cetakStruk(uang, kembali));
    }

    public String cetakStruk(double uang, double kembali) {
        StringBuilder sb = new StringBuilder();
        String garis = "------------------------------------------\n";
        sb.append("            TOKO SERBA ADA\n").append(garis);
        sb.append("No     : ").append(nomor).append("\n");
        sb.append("Waktu  : ").append(waktu.format(Util.WAKTU)).append("\n").append(garis);
        for (ItemTransaksi it : items) {
            sb.append(it.getProduk().getNama()).append("\n");
            sb.append(String.format("  %d x %-12s %18s\n", it.getJumlah(),
                    Util.rupiah(it.getProduk().getHarga()), Util.rupiah(it.getSubtotal())));
        }
        sb.append(garis);
        sb.append(String.format("%-20s %21s\n", "Subtotal", Util.rupiah(getSubtotal())));
        sb.append(String.format("%-20s %21s\n", diskon.getKeterangan(), "-" + Util.rupiah(getNilaiDiskon())));
        sb.append(String.format("%-20s %21s\n", "TOTAL", Util.rupiah(getTotal())));
        sb.append(String.format("%-20s %21s\n", "Bayar", Util.rupiah(uang)));
        sb.append(String.format("%-20s %21s\n", "Kembali", Util.rupiah(kembali)));
        sb.append(garis).append("       Terima kasih atas kunjungan Anda\n");
        return sb.toString();
    }
}
