public class TanpaDiskon implements Diskon {
    @Override public double hitung(double subtotal) { return 0; }
    @Override public String getKeterangan() { return "Tanpa diskon"; }
}
