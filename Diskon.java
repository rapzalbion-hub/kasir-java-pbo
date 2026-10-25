/** Interface: kontrak untuk semua jenis diskon. */
public interface Diskon {
    double hitung(double subtotal);
    String getKeterangan();
}
