import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Menyimpan riwayat penjualan di memori dan di file riwayat_penjualan.txt. */
public class RiwayatPenjualan {
    private static final Path FILE = Paths.get("riwayat_penjualan.txt");
    private final List<CatatanPenjualan> data = new ArrayList<>();

    public RiwayatPenjualan() { muat(); }

    private void muat() {
        if (!Files.exists(FILE)) return;
        try {
            for (String baris : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                if (baris.trim().isEmpty()) continue;
                try { data.add(CatatanPenjualan.dariBaris(baris)); }
                catch (RuntimeException ignored) { /* lewati baris rusak */ }
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca riwayat: " + e.getMessage());
        }
    }

    public void tambah(CatatanPenjualan c) {
        data.add(c);
        try {
            Files.write(FILE, (c.toBaris() + "\n").getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Gagal menyimpan riwayat: " + e.getMessage());
        }
    }

    /** Nomor transaksi berikutnya, contoh: TRX20261024-003 */
    public String nomorBerikut() {
        LocalDate hari = LocalDate.now();
        return String.format("TRX%s-%03d", hari.format(DateTimeFormatter.BASIC_ISO_DATE), filter(hari).size() + 1);
    }

    /** tanggal = null berarti semua riwayat. Hasil diurutkan dari yang terbaru. */
    public List<CatatanPenjualan> filter(LocalDate tanggal) {
        List<CatatanPenjualan> hasil = data.stream()
                .filter(c -> tanggal == null || c.getWaktu().toLocalDate().equals(tanggal))
                .collect(Collectors.toList());
        Collections.reverse(hasil);
        return hasil;
    }

    public int jumlahTransaksi(LocalDate tanggal) { return filter(tanggal).size(); }
    public int jumlahItem(LocalDate tanggal) { return filter(tanggal).stream().mapToInt(CatatanPenjualan::getJumlahItem).sum(); }
    public double totalPendapatan(LocalDate tanggal) { return filter(tanggal).stream().mapToDouble(CatatanPenjualan::getTotal).sum(); }
}
