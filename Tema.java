import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/** Warna, font, dan gaya komponen supaya tampilan konsisten. */
public final class Tema {
    public static final Color LATAR = new Color(0xF3F4F6);
    public static final Color GELAP = new Color(0x111827);
    public static final Color UTAMA = new Color(0x2563EB);
    public static final Color SUKSES = new Color(0x16A34A);
    public static final Color BAHAYA = new Color(0xDC2626);
    public static final Color PERINGATAN = new Color(0xD97706);
    public static final Color NETRAL = new Color(0x6B7280);
    public static final Color PUTIH = Color.WHITE;
    public static final Color GARIS = new Color(0xE5E7EB);
    public static final Color GARIS_TUA = new Color(0xD1D5DB);
    public static final Color BARIS_GANJIL = new Color(0xF9FAFB);
    public static final Color PILIH = new Color(0xDBEAFE);

    private Tema() {}

    public static Font font(int style, int size) { return new Font("Segoe UI", style, size); }

    public static void terapkanFontGlobal() {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object k = keys.nextElement();
            if (UIManager.get(k) instanceof FontUIResource)
                UIManager.put(k, new FontUIResource(font(Font.PLAIN, 13)));
        }
    }

    public static void gayaField(JTextField f) {
        f.setFont(font(Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GARIS_TUA, 1), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    public static JLabel judul(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(font(Font.BOLD, 16));
        l.setForeground(GELAP);
        return l;
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(GARIS));
        sp.getViewport().setBackground(PUTIH);
        return sp;
    }

    /** Memberi tabel tampilan modern. Kolom di "kanan" rata kanan (angka). */
    public static void gayaTabel(JTable t, int... kanan) {
        Set<Integer> kolomKanan = new HashSet<>();
        for (int k : kanan) kolomKanan.add(k);

        t.setRowHeight(32);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(PILIH);
        t.setSelectionForeground(GELAP);
        t.setFont(font(Font.PLAIN, 13));
        t.setFillsViewportHeight(true);

        DefaultTableCellRenderer isi = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, v, sel, false, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setHorizontalAlignment(kolomKanan.contains(col) ? RIGHT : LEFT);
                if (!sel) { setBackground(row % 2 == 0 ? PUTIH : BARIS_GANJIL); setForeground(GELAP); }
                return this;
            }
        };
        t.setDefaultRenderer(Object.class, isi);
        t.setDefaultRenderer(Number.class, isi);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, v, false, false, row, col);
                setOpaque(true);
                setBackground(GELAP);
                setForeground(Color.WHITE);
                setFont(font(Font.BOLD, 13));
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setHorizontalAlignment(kolomKanan.contains(col) ? RIGHT : LEFT);
                return this;
            }
        });
    }
}
