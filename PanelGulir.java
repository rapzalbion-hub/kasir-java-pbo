import javax.swing.*;
import java.awt.*;

/**
 * Panel yang memiliki tinggi minimum. Jika jendela lebih pendek dari itu
 * (misalnya layar laptop dengan skala 125-150%), muncul scrollbar vertikal
 * sehingga semua bagian tetap bisa dijangkau.
 */
public class PanelGulir extends JPanel implements Scrollable {
    private final int tinggiMin;

    public PanelGulir(LayoutManager lm, int tinggiMin) {
        super(lm);
        this.tinggiMin = tinggiMin;
        setOpaque(false);
    }

    @Override public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, tinggiMin); }
    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() {
        Container p = getParent();
        return p instanceof JViewport && p.getHeight() >= tinggiMin;
    }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(48, r.height - 48); }
}
