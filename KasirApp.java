import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class KasirApp extends JFrame {
    private final ProdukRepository repo = new ProdukRepository();
    private final RiwayatPenjualan riwayat = new RiwayatPenjualan();
    private Transaksi transaksi;
    private List<Produk> tampil;
    private List<CatatanPenjualan> tampilRiwayat;
    private LocalDate filterTgl = LocalDate.now();   // null = semua riwayat

    private DefaultTableModel modelProduk, modelKeranjang, modelRiwayat;
    private JTable tblProduk, tblKeranjang, tblRiwayat;
    private JTextField txtCari, txtDiskon, txtBayar, txtTanggal;
    private JSpinner spnQty;
    private JLabel lblJumlahKeranjang, lblSubtotal, lblDiskon, lblTotal, lblJam, lblRingkasan;
    private JLabel lblStatTrx, lblStatItem, lblStatOmzet;
    private JTextArea areaStruk;

    public KasirApp() {
        super("Aplikasi Kasir - Toko Serba Ada");
        transaksi = new Transaksi(riwayat.nomorBerikut());
        buildUI();
        muatProduk("");
        refreshKeranjang();
        perbaruiStatistik();
        muatRiwayat();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 740);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    // =====================================================================
    //  MEMBANGUN TAMPILAN
    // =====================================================================
    private void buildUI() {
        getContentPane().setBackground(Tema.LATAR);
        setLayout(new BorderLayout());

        JPanel atas = buatHeader();

        JTabbedPane tab = new JTabbedPane();
        tab.setFont(Tema.font(Font.BOLD, 14));
        tab.addTab("   Kasir   ", buatPanelKasir());
        tab.addTab("   Riwayat Penjualan   ", buatPanelRiwayat());
        tab.addChangeListener(e -> { if (tab.getSelectedIndex() == 1) muatRiwayat(); });

        JPanel tengah = new JPanel(new BorderLayout());
        tengah.setOpaque(false);
        tengah.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        tengah.add(tab, BorderLayout.CENTER);

        add(atas, BorderLayout.NORTH);
        add(tengah, BorderLayout.CENTER);
    }

    private JPanel buatHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Tema.GELAP);
        h.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        JLabel judul = new JLabel("Kasir Toko Serba Ada");
        judul.setFont(Tema.font(Font.BOLD, 20));
        judul.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Aplikasi penjualan sederhana");
        sub.setFont(Tema.font(Font.PLAIN, 12));
        sub.setForeground(new Color(0x9CA3AF));
        JPanel kiri = new JPanel(new GridLayout(2, 1));
        kiri.setOpaque(false);
        kiri.add(judul);
        kiri.add(sub);

        lblJam = new JLabel();
        lblJam.setFont(Tema.font(Font.PLAIN, 14));
        lblJam.setForeground(Color.WHITE);
        Runnable ubahJam = () -> lblJam.setText(
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy   HH:mm:ss", Util.ID)));
        ubahJam.run();
        new javax.swing.Timer(1000, e -> ubahJam.run()).start();

        h.add(kiri, BorderLayout.WEST);
        h.add(buatKartuStat(), BorderLayout.CENTER);
        h.add(lblJam, BorderLayout.EAST);
        return h;
    }

    private JPanel buatKartuStat() {
        lblStatTrx = new JLabel("0");
        lblStatItem = new JLabel("0");
        lblStatOmzet = new JLabel("Rp0");
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        p.setOpaque(false);
        p.add(kartu("Transaksi hari ini", new Color(0x60A5FA), lblStatTrx));
        p.add(kartu("Barang terjual hari ini", new Color(0xFBBF24), lblStatItem));
        p.add(kartu("Pendapatan hari ini", new Color(0x4ADE80), lblStatOmzet));
        return p;
    }

    private JPanel kartu(String judul, Color aksen, JLabel nilai) {
        PanelBulat k = new PanelBulat(new BorderLayout(0, 0), new Color(0x1F2937), 12);
        k.setTepi(new Color(0x374151));
        k.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        JLabel l = new JLabel(judul);
        l.setFont(Tema.font(Font.PLAIN, 11));
        l.setForeground(new Color(0x9CA3AF));
        nilai.setFont(Tema.font(Font.BOLD, 16));
        nilai.setForeground(aksen);
        k.add(l, BorderLayout.NORTH);
        k.add(nilai, BorderLayout.CENTER);
        return k;
    }

    private DefaultTableModel buatModel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JLabel nilaiLabel() {
        JLabel l = new JLabel("", SwingConstants.RIGHT);
        l.setFont(Tema.font(Font.PLAIN, 14));
        return l;
    }

    private JLabel teksKecil(String s) {
        JLabel l = new JLabel(s);
        l.setFont(Tema.font(Font.PLAIN, 13));
        l.setForeground(Tema.NETRAL);
        return l;
    }

    private void baris(JPanel p, Component c, int y) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = y; g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(3, 0, 3, 0);
        p.add(c, g);
    }

    // ---------------------------------------------------------------- KASIR
    private JComponent buatPanelKasir() {
        // ===== kiri: produk =====
        PanelBulat kiri = new PanelBulat(new BorderLayout(0, 10), Tema.PUTIH, 16);
        kiri.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        txtCari = new JTextField();
        Tema.gayaField(txtCari);
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { muatProduk(txtCari.getText()); }
            public void removeUpdate(DocumentEvent e) { muatProduk(txtCari.getText()); }
            public void changedUpdate(DocumentEvent e) { muatProduk(txtCari.getText()); }
        });
        JPanel barisCari = new JPanel(new BorderLayout(8, 0));
        barisCari.setOpaque(false);
        barisCari.add(teksKecil("Cari produk"), BorderLayout.WEST);
        barisCari.add(txtCari, BorderLayout.CENTER);

        JPanel atasKiri = new JPanel(new BorderLayout(0, 10));
        atasKiri.setOpaque(false);
        atasKiri.add(Tema.judul("Daftar Produk"), BorderLayout.NORTH);
        atasKiri.add(barisCari, BorderLayout.CENTER);

        modelProduk = buatModel("Kode", "Nama", "Kategori", "Harga", "Stok", "Info");
        tblProduk = new JTable(modelProduk);
        Tema.gayaTabel(tblProduk, 3, 4);
        tblProduk.setPreferredScrollableViewportSize(new Dimension(400, 240));
        tblProduk.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblProduk.getColumnModel().getColumn(1).setPreferredWidth(160);
        tblProduk.getColumnModel().getColumn(2).setPreferredWidth(80);
        tblProduk.getColumnModel().getColumn(4).setPreferredWidth(45);
        tblProduk.getColumnModel().getColumn(5).setPreferredWidth(150);

        spnQty = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spnQty.setPreferredSize(new Dimension(80, 36));
        TombolModern btnTambah = new TombolModern("Tambah ke Keranjang", Tema.UTAMA);
        btnTambah.addActionListener(e -> tambahKeKeranjang());
        JPanel baris1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        baris1.setOpaque(false);
        baris1.add(teksKecil("Jumlah"));
        baris1.add(spnQty);
        baris1.add(btnTambah);

        TombolModern bBaru = new TombolModern("Produk Baru", Tema.SUKSES);
        TombolModern bStok = new TombolModern("Tambah Stok", Tema.PERINGATAN);
        TombolModern bHapus = new TombolModern("Hapus Produk", Tema.BAHAYA);
        bBaru.addActionListener(e -> dialogProdukBaru());
        bStok.addActionListener(e -> tambahStok());
        bHapus.addActionListener(e -> hapusProduk());
        JPanel baris2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        baris2.setOpaque(false);
        baris2.add(bBaru); baris2.add(bStok); baris2.add(bHapus);

        JPanel bawahKiri = new JPanel(new GridLayout(0, 1, 0, 8));
        bawahKiri.setOpaque(false);
        bawahKiri.add(baris1);
        bawahKiri.add(baris2);

        kiri.add(atasKiri, BorderLayout.NORTH);
        kiri.add(Tema.scroll(tblProduk), BorderLayout.CENTER);
        kiri.add(bawahKiri, BorderLayout.SOUTH);

        // ===== kanan: keranjang =====
        PanelBulat kanan = new PanelBulat(new BorderLayout(0, 10), Tema.PUTIH, 16);
        kanan.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        modelKeranjang = buatModel("Nama", "Harga", "Qty", "Subtotal");
        tblKeranjang = new JTable(modelKeranjang);
        Tema.gayaTabel(tblKeranjang, 1, 2, 3);
        tblKeranjang.setPreferredScrollableViewportSize(new Dimension(300, 170));
        tblKeranjang.getColumnModel().getColumn(0).setPreferredWidth(150);
        tblKeranjang.getColumnModel().getColumn(2).setPreferredWidth(40);

        TombolModern bHapusItem = new TombolModern("Hapus Item", Tema.BAHAYA);
        bHapusItem.addActionListener(e -> hapusDariKeranjang());
        JPanel atasKanan = new JPanel(new BorderLayout());
        atasKanan.setOpaque(false);
        lblJumlahKeranjang = new JLabel("0 item");
        lblJumlahKeranjang.setFont(Tema.font(Font.BOLD, 13));
        lblJumlahKeranjang.setForeground(Tema.UTAMA);
        JPanel judulKeranjang = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        judulKeranjang.setOpaque(false);
        judulKeranjang.add(Tema.judul("Keranjang Belanja"));
        judulKeranjang.add(lblJumlahKeranjang);
        atasKanan.add(judulKeranjang, BorderLayout.WEST);
        atasKanan.add(bHapusItem, BorderLayout.EAST);

        txtDiskon = new JTextField("0", 5);
        Tema.gayaField(txtDiskon);
        TombolModern bDiskon = new TombolModern("Terapkan", Tema.NETRAL);
        bDiskon.addActionListener(e -> terapkanDiskon());
        JPanel barisDiskon = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barisDiskon.setOpaque(false);
        barisDiskon.add(teksKecil("Diskon (%)"));
        barisDiskon.add(txtDiskon);
        barisDiskon.add(bDiskon);

        lblSubtotal = nilaiLabel(); lblDiskon = nilaiLabel();
        JPanel rincian = new JPanel(new GridLayout(0, 2, 0, 2));
        rincian.setOpaque(false);
        rincian.add(teksKecil("Subtotal")); rincian.add(lblSubtotal);
        rincian.add(teksKecil("Diskon")); rincian.add(lblDiskon);

        JLabel tTotal = new JLabel("TOTAL");
        tTotal.setFont(Tema.font(Font.BOLD, 16));
        lblTotal = new JLabel("", SwingConstants.RIGHT);
        lblTotal.setFont(Tema.font(Font.BOLD, 24));
        lblTotal.setForeground(Tema.UTAMA);
        JPanel barisTotal = new JPanel(new BorderLayout());
        barisTotal.setOpaque(false);
        barisTotal.add(tTotal, BorderLayout.WEST);
        barisTotal.add(lblTotal, BorderLayout.CENTER);

        txtBayar = new JTextField();
        Tema.gayaField(txtBayar);
        FilterRibuan.pasang(txtBayar);   // otomatis 50000 -> 50.000
        txtBayar.setFont(Tema.font(Font.BOLD, 15));
        txtBayar.addActionListener(e -> bayar());
        TombolModern bPas = new TombolModern("Uang Pas", Tema.NETRAL);
        bPas.addActionListener(e -> txtBayar.setText(String.valueOf((long) transaksi.getTotal())));
        JPanel barisBayar = new JPanel(new BorderLayout(8, 0));
        barisBayar.setOpaque(false);
        barisBayar.add(teksKecil("Uang bayar"), BorderLayout.WEST);
        barisBayar.add(txtBayar, BorderLayout.CENTER);
        barisBayar.add(bPas, BorderLayout.EAST);

        TombolModern bKosong = new TombolModern("Kosongkan", Tema.NETRAL);
        bKosong.addActionListener(e -> { resetTransaksi(); });
        TombolModern bBayar = new TombolModern("BAYAR", Tema.SUKSES);
        bBayar.setFont(Tema.font(Font.BOLD, 16));
        bBayar.addActionListener(e -> bayar());
        JPanel barisAksi = new JPanel(new GridLayout(1, 2, 8, 0));
        barisAksi.setOpaque(false);
        barisAksi.add(bKosong);
        barisAksi.add(bBayar);

        JPanel bawahKanan = new JPanel(new GridBagLayout());
        bawahKanan.setOpaque(false);
        baris(bawahKanan, barisDiskon, 0);
        baris(bawahKanan, rincian, 1);
        baris(bawahKanan, new JSeparator(), 2);
        baris(bawahKanan, barisTotal, 3);
        baris(bawahKanan, barisBayar, 4);
        baris(bawahKanan, barisAksi, 5);

        kanan.add(atasKanan, BorderLayout.NORTH);
        JScrollPane spKeranjang = Tema.scroll(tblKeranjang);
        kanan.add(spKeranjang, BorderLayout.CENTER);
        kanan.add(bawahKanan, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, kiri, kanan);
        split.setBorder(null);
        split.setDividerSize(12);
        split.setResizeWeight(0.58);
        split.setOpaque(false);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.58));

        PanelGulir isi = new PanelGulir(new BorderLayout(), 590);
        isi.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        isi.add(split, BorderLayout.CENTER);

        JScrollPane gulir = new JScrollPane(isi, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        gulir.setBorder(null);
        gulir.setOpaque(false);
        gulir.getViewport().setOpaque(false);
        gulir.getVerticalScrollBar().setUnitIncrement(24);
        return gulir;
    }

    // -------------------------------------------------------------- RIWAYAT
    private JComponent buatPanelRiwayat() {
        PanelBulat p = new PanelBulat(new BorderLayout(0, 12), Tema.PUTIH, 16);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        txtTanggal = new JTextField(LocalDate.now().toString(), 10);
        Tema.gayaField(txtTanggal);
        TombolModern bTampil = new TombolModern("Tampilkan", Tema.UTAMA);
        TombolModern bHariIni = new TombolModern("Hari Ini", Tema.NETRAL);
        TombolModern bSemua = new TombolModern("Semua Riwayat", Tema.NETRAL);
        bTampil.addActionListener(e -> {
            try {
                filterTgl = LocalDate.parse(txtTanggal.getText().trim());
                muatRiwayat();
            } catch (DateTimeParseException ex) {
                error("Format tanggal harus yyyy-MM-dd, contoh: " + LocalDate.now());
            }
        });
        bHariIni.addActionListener(e -> {
            filterTgl = LocalDate.now();
            txtTanggal.setText(filterTgl.toString());
            muatRiwayat();
        });
        bSemua.addActionListener(e -> {
            filterTgl = null;
            txtTanggal.setText("");
            muatRiwayat();
        });

        JPanel barisFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barisFilter.setOpaque(false);
        barisFilter.add(teksKecil("Tanggal (yyyy-MM-dd)"));
        barisFilter.add(txtTanggal);
        barisFilter.add(bTampil);
        barisFilter.add(bHariIni);
        barisFilter.add(bSemua);

        lblRingkasan = new JLabel(" ");
        lblRingkasan.setFont(Tema.font(Font.BOLD, 14));
        lblRingkasan.setForeground(Tema.UTAMA);

        JPanel atas = new JPanel(new GridLayout(0, 1, 0, 8));
        atas.setOpaque(false);
        atas.add(Tema.judul("Riwayat Penjualan"));
        atas.add(barisFilter);
        atas.add(lblRingkasan);

        modelRiwayat = buatModel("No. Transaksi", "Waktu", "Item", "Total", "Bayar", "Kembali");
        tblRiwayat = new JTable(modelRiwayat);
        Tema.gayaTabel(tblRiwayat, 2, 3, 4, 5);
        tblRiwayat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRiwayat.getColumnModel().getColumn(0).setPreferredWidth(170);
        tblRiwayat.getColumnModel().getColumn(1).setPreferredWidth(150);
        tblRiwayat.getColumnModel().getColumn(2).setPreferredWidth(50);
        tblRiwayat.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) tampilkanStrukTerpilih();
        });

        areaStruk = new JTextArea();
        areaStruk.setEditable(false);
        areaStruk.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        areaStruk.setMargin(new Insets(10, 10, 10, 10));
        areaStruk.setText("Pilih transaksi di tabel untuk melihat struknya.");
        JScrollPane spStruk = Tema.scroll(areaStruk);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, Tema.scroll(tblRiwayat), spStruk);
        split.setBorder(null);
        split.setDividerSize(10);
        split.setResizeWeight(0.6);
        split.setOpaque(false);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.6));

        p.add(atas, BorderLayout.NORTH);
        p.add(split, BorderLayout.CENTER);

        JPanel pembungkus = new JPanel(new BorderLayout());
        pembungkus.setOpaque(false);
        pembungkus.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        pembungkus.add(p, BorderLayout.CENTER);
        return pembungkus;
    }

    // =====================================================================
    //  LOGIKA TAMPILAN
    // =====================================================================
    private void muatProduk(String kw) {
        tampil = repo.cariKeyword(kw == null ? "" : kw.trim());
        modelProduk.setRowCount(0);
        for (Produk p : tampil)
            modelProduk.addRow(new Object[]{p.getKode(), p.getNama(), p.getKategori(),
                    Util.rupiah(p.getHarga()), p.getStok(), p.getInfoTambahan()});
    }

    private void refreshKeranjang() {
        modelKeranjang.setRowCount(0);
        for (ItemTransaksi it : transaksi.getItems())
            modelKeranjang.addRow(new Object[]{it.getProduk().getNama(), Util.rupiah(it.getProduk().getHarga()),
                    it.getJumlah(), Util.rupiah(it.getSubtotal())});
        lblJumlahKeranjang.setText(transaksi.getJumlahItem() + " item");
        int terakhir = modelKeranjang.getRowCount() - 1;
        if (terakhir >= 0) tblKeranjang.scrollRectToVisible(tblKeranjang.getCellRect(terakhir, 0, true));
        lblSubtotal.setText(Util.rupiah(transaksi.getSubtotal()));
        lblDiskon.setText("-" + Util.rupiah(transaksi.getNilaiDiskon()));
        lblTotal.setText(Util.rupiah(transaksi.getTotal()));
    }

    private void perbaruiStatistik() {
        LocalDate hari = LocalDate.now();
        lblStatTrx.setText(String.valueOf(riwayat.jumlahTransaksi(hari)));
        lblStatItem.setText(String.valueOf(riwayat.jumlahItem(hari)));
        lblStatOmzet.setText(Util.rupiah(riwayat.totalPendapatan(hari)));
    }

    private void muatRiwayat() {
        tampilRiwayat = riwayat.filter(filterTgl);
        modelRiwayat.setRowCount(0);
        for (CatatanPenjualan c : tampilRiwayat)
            modelRiwayat.addRow(new Object[]{c.getNomor(), c.getWaktu().format(Util.WAKTU), c.getJumlahItem(),
                    Util.rupiah(c.getTotal()), Util.rupiah(c.getBayar()), Util.rupiah(c.getKembali())});

        int item = tampilRiwayat.stream().mapToInt(CatatanPenjualan::getJumlahItem).sum();
        double omzet = tampilRiwayat.stream().mapToDouble(CatatanPenjualan::getTotal).sum();
        String periode = filterTgl == null ? "Semua riwayat" : filterTgl.format(Util.TANGGAL_PANJANG);
        lblRingkasan.setText(periode + "   |   " + tampilRiwayat.size() + " transaksi   |   "
                + item + " barang terjual   |   Pendapatan " + Util.rupiah(omzet));
        areaStruk.setText("Pilih transaksi di tabel untuk melihat struknya.");
    }

    private void tampilkanStrukTerpilih() {
        int r = tblRiwayat.getSelectedRow();
        if (r < 0 || tampilRiwayat == null || r >= tampilRiwayat.size()) return;
        areaStruk.setText(tampilRiwayat.get(r).getStruk());
        areaStruk.setCaretPosition(0);
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private int parseInt(String s) { return Integer.parseInt(s.trim()); }

    private long parseUang(String s) {
        String d = s.replaceAll("[^0-9]", "");
        if (d.isEmpty()) throw new NumberFormatException();
        return Long.parseLong(d);
    }

    private void resetTransaksi() {
        transaksi = new Transaksi(riwayat.nomorBerikut());
        txtBayar.setText("");
        txtDiskon.setText("0");
        refreshKeranjang();
    }

    // =====================================================================
    //  AKSI KASIR
    // =====================================================================
    private void tambahKeKeranjang() {
        int row = tblProduk.getSelectedRow();
        if (row < 0) { error("Pilih produk terlebih dahulu"); return; }
        try {
            transaksi.tambahItem(tampil.get(row), (Integer) spnQty.getValue());
            spnQty.setValue(1);
            refreshKeranjang();
        } catch (RuntimeException ex) {
            error(ex.getMessage());
        }
    }

    private void hapusDariKeranjang() {
        int row = tblKeranjang.getSelectedRow();
        if (row < 0) { error("Pilih item di keranjang"); return; }
        transaksi.hapusItem(row);
        refreshKeranjang();
    }

    private void terapkanDiskon() {
        try {
            double persen = Double.parseDouble(txtDiskon.getText().trim().replace(',', '.'));
            transaksi.setDiskon(persen == 0 ? new TanpaDiskon() : new DiskonPersen(persen));
            refreshKeranjang();
        } catch (NumberFormatException ex) {
            error("Diskon harus berupa angka");
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void bayar() {
        try {
            double uang = parseUang(txtBayar.getText());
            CatatanPenjualan catatan = transaksi.proses(uang);
            riwayat.tambah(catatan);

            JTextArea area = new JTextArea(catatan.getStruk());
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            area.setEditable(false);
            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(430, 420));
            JOptionPane.showMessageDialog(this, sp, "Struk Pembayaran", JOptionPane.INFORMATION_MESSAGE);

            resetTransaksi();
            muatProduk(txtCari.getText());
            perbaruiStatistik();
            muatRiwayat();
        } catch (NumberFormatException ex) {
            error("Masukkan jumlah uang yang valid");
        } catch (RuntimeException ex) {
            error(ex.getMessage());
        }
    }

    // =====================================================================
    //  KELOLA PRODUK
    // =====================================================================
    private void dialogProdukBaru() {
        JTextField kode = new JTextField(), nama = new JTextField(), harga = new JTextField(),
                stok = new JTextField(), extra = new JTextField();
        for (JTextField f : new JTextField[]{kode, nama, harga, stok, extra}) Tema.gayaField(f);
        FilterRibuan.pasang(harga);
        JComboBox<String> kat = new JComboBox<>(new String[]{"Makanan", "Minuman", "Barang"});
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Kode")); p.add(kode);
        p.add(new JLabel("Nama")); p.add(nama);
        p.add(new JLabel("Kategori")); p.add(kat);
        p.add(new JLabel("Harga")); p.add(harga);
        p.add(new JLabel("Stok")); p.add(stok);
        p.add(new JLabel("Info (exp / ukuran / merek)")); p.add(extra);

        if (JOptionPane.showConfirmDialog(this, p, "Produk Baru", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            String k = kode.getText().trim(), n = nama.getText().trim(), x = extra.getText().trim();
            if (k.isEmpty() || n.isEmpty()) throw new IllegalArgumentException("Kode dan nama wajib diisi");
            double h = parseUang(harga.getText());
            int s = parseInt(stok.getText());
            Produk baru;
            switch ((String) kat.getSelectedItem()) {
                case "Makanan": baru = new Makanan(k, n, h, s, x); break;
                case "Minuman": baru = new Minuman(k, n, h, s, x); break;
                default:        baru = new Barang(k, n, h, s, x);
            }
            repo.tambah(baru);
            muatProduk(txtCari.getText());
        } catch (NumberFormatException ex) {
            error("Harga/stok harus berupa angka");
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void tambahStok() {
        int row = tblProduk.getSelectedRow();
        if (row < 0) { error("Pilih produk terlebih dahulu"); return; }
        String in = JOptionPane.showInputDialog(this, "Jumlah stok yang ditambahkan:");
        if (in == null) return;
        try {
            tampil.get(row).tambahStok(parseInt(in));
            muatProduk(txtCari.getText());
        } catch (NumberFormatException ex) {
            error("Masukkan angka yang valid");
        }
    }

    private void hapusProduk() {
        int row = tblProduk.getSelectedRow();
        if (row < 0) { error("Pilih produk terlebih dahulu"); return; }
        Produk p = tampil.get(row);
        if (JOptionPane.showConfirmDialog(this, "Hapus " + p.getNama() + "?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            repo.hapus(p);
            resetTransaksi();   // keranjang direset agar konsisten
            muatProduk(txtCari.getText());
        }
    }
}
