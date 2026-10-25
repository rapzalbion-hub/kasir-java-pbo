import javax.swing.text.*;

/**
 * Filter input angka: hanya menerima digit dan otomatis memberi titik
 * pemisah ribuan saat mengetik (50000 -> 50.000).
 */
public class FilterRibuan extends DocumentFilter {
    private static final int MAKS_DIGIT = 12;
    private final JTextComponent komponen;

    private FilterRibuan(JTextComponent komponen) { this.komponen = komponen; }

    /** Pasang filter pada sebuah text field. */
    public static void pasang(javax.swing.JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new FilterRibuan(field));
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String teks, AttributeSet a) throws BadLocationException {
        replace(fb, offset, 0, teks, a);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int panjang, String teks, AttributeSet a) throws BadLocationException {
        String lama = fb.getDocument().getText(0, fb.getDocument().getLength());
        String sisip = teks == null ? "" : teks;
        String baru = lama.substring(0, offset) + sisip + lama.substring(offset + panjang);
        tulis(fb, baru, offset + sisip.length(), a);
    }

    @Override
    public void remove(FilterBypass fb, int offset, int panjang) throws BadLocationException {
        String lama = fb.getDocument().getText(0, fb.getDocument().getLength());
        String baru = lama.substring(0, offset) + lama.substring(offset + panjang);
        tulis(fb, baru, offset, null);
    }

    private void tulis(FilterBypass fb, String baru, int posisiKaret, AttributeSet a) throws BadLocationException {
        int digitSebelumKaret = hitungDigit(baru.substring(0, Math.min(posisiKaret, baru.length())));
        String digit = baru.replaceAll("[^0-9]", "");
        if (digit.length() > MAKS_DIGIT) return;                 // terlalu panjang: abaikan ketikan
        digit = digit.replaceFirst("^0+(?!$)", "");              // buang nol di depan
        digitSebelumKaret = Math.min(digitSebelumKaret, digit.length());

        String hasil = format(digit);
        fb.replace(0, fb.getDocument().getLength(), hasil, a);

        // letakkan kursor setelah digit yang sama seperti sebelum diformat
        int pos = 0, hitung = 0;
        while (pos < hasil.length() && hitung < digitSebelumKaret) {
            if (Character.isDigit(hasil.charAt(pos))) hitung++;
            pos++;
        }
        komponen.setCaretPosition(pos);
    }

    private static int hitungDigit(String s) {
        int n = 0;
        for (char c : s.toCharArray()) if (Character.isDigit(c)) n++;
        return n;
    }

    static String format(String digit) {
        StringBuilder sb = new StringBuilder();
        int n = digit.length();
        for (int i = 0; i < n; i++) {
            if (i > 0 && (n - i) % 3 == 0) sb.append('.');
            sb.append(digit.charAt(i));
        }
        return sb.toString();
    }
}
