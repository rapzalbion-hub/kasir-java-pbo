import javax.swing.*;
import java.awt.*;

/** Panel dengan sudut membulat (dipakai sebagai "kartu"). */
public class PanelBulat extends JPanel {
    private final Color warna;
    private final int radius;
    private Color tepi = Tema.GARIS;

    public PanelBulat(LayoutManager lm, Color warna, int radius) {
        super(lm);
        this.warna = warna;
        this.radius = radius;
        setOpaque(false);
    }

    public void setTepi(Color c) { this.tepi = c; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(warna);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.setColor(tepi);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}
