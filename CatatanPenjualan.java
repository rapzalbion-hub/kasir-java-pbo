import java.time.LocalDateTime;

/** Data satu transaksi yang sudah selesai (immutable) untuk riwayat penjualan. */
public class CatatanPenjualan {
    private final String nomor;
    private final LocalDateTime waktu;
    private final int jumlahItem;
    private final double total, bayar, kembali;
    private final String struk;

    public CatatanPenjualan(String nomor, LocalDateTime waktu, int jumlahItem,
                            double total, double bayar, double kembali, String struk) {
        this.nomor = nomor; this.waktu = waktu; this.jumlahItem = jumlahItem;
        this.total = total; this.bayar = bayar; this.kembali = kembali; this.struk = struk;
    }

    public String getNomor() { return nomor; }
    public LocalDateTime getWaktu() { return waktu; }
    public int getJumlahItem() { return jumlahItem; }
    public double getTotal() { return total; }
    public double getBayar() { return bayar; }
    public double getKembali() { return kembali; }
    public String getStruk() { return struk; }

    /** Satu baris teks (dipisah TAB) untuk disimpan ke file. */
    public String toBaris() {
        return String.join("\t", nomor, waktu.toString(), String.valueOf(jumlahItem),
                String.valueOf(total), String.valueOf(bayar), String.valueOf(kembali), escape(struk));
    }

    public static CatatanPenjualan dariBaris(String baris) {
        String[] f = baris.split("\t", -1);
        if (f.length < 7) throw new IllegalArgumentException("Format baris salah");
        return new CatatanPenjualan(f[0], LocalDateTime.parse(f[1]), Integer.parseInt(f[2]),
                Double.parseDouble(f[3]), Double.parseDouble(f[4]), Double.parseDouble(f[5]), unescape(f[6]));
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\t", " ").replace("\r", "").replace("\n", "\\n");
    }

    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                sb.append(n == 'n' ? '\n' : n);
            } else sb.append(c);
        }
        return sb.toString();
    }
}
