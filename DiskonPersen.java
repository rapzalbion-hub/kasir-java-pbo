public class DiskonPersen implements Diskon {
    private final double persen;

    public DiskonPersen(double persen) {
        if (persen < 0 || persen > 100)
            throw new IllegalArgumentException("Diskon harus 0 - 100%");
        this.persen = persen;
    }

    @Override public double hitung(double subtotal) { return subtotal * persen / 100.0; }
    @Override public String getKeterangan() { return "Diskon " + persen + "%"; }
}
