import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Util {
    public static final Locale ID = Locale.forLanguageTag("id-ID");
    public static final DateTimeFormatter TANGGAL_PANJANG = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", ID);
    public static final DateTimeFormatter WAKTU = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", ID);

    private static final NumberFormat RP = NumberFormat.getCurrencyInstance(ID);
    static { RP.setMaximumFractionDigits(0); }

    private Util() {}

    public static String rupiah(double nilai) { return RP.format(nilai); }
}
