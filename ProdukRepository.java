import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Penyimpanan produk (in-memory). */
public class ProdukRepository {
    private final List<Produk> data = new ArrayList<>();

    public ProdukRepository() {
        data.add(new Makanan("MK001", "Roti Tawar", 15000, 30, "2026-12-01"));
        data.add(new Makanan("MK002", "Mie Instan", 3500, 100, "2027-03-15"));
        data.add(new Makanan("MK003", "Biskuit Coklat", 9000, 50, "2027-01-20"));
        data.add(new Minuman("MN001", "Air Mineral", 4000, 80, "600 ml"));
        data.add(new Minuman("MN002", "Teh Botol", 6000, 60, "450 ml"));
        data.add(new Minuman("MN003", "Kopi Susu", 12000, 40, "250 ml"));
        data.add(new Barang("BR001", "Sabun Mandi", 5500, 45, "Lifebuoy"));
        data.add(new Barang("BR002", "Pasta Gigi", 14000, 35, "Pepsodent"));
        data.add(new Barang("BR003", "Baterai AA", 18000, 25, "ABC"));
    }

    public void tambah(Produk p) {
        if (cari(p.getKode()) != null)
            throw new IllegalArgumentException("Kode produk sudah digunakan");
        data.add(p);
    }

    public void hapus(Produk p) { data.remove(p); }

    public Produk cari(String kode) {
        return data.stream().filter(p -> p.getKode().equalsIgnoreCase(kode)).findFirst().orElse(null);
    }

    public List<Produk> cariKeyword(String kw) {
        String k = kw.toLowerCase();
        return data.stream()
                .filter(p -> p.getNama().toLowerCase().contains(k) || p.getKode().toLowerCase().contains(k)
                        || p.getKategori().toLowerCase().contains(k))
                .collect(Collectors.toList());
    }
}
