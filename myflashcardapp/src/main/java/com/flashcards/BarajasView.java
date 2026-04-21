package com.flashcards;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class BarajasView extends JFrame {

    // ── Palette (misma que MainMenu) ─────────────────────────────────────────
    private static final Color BG          = new Color(18, 18, 30);
    private static final Color CARD_BG     = new Color(28, 28, 46);
    private static final Color CARD_SEL    = new Color(38, 35, 80);
    private static final Color ACCENT      = new Color(108, 99, 255);
    private static final Color ACCENT_HOV  = new Color(140, 133, 255);
    private static final Color TEXT_MAIN   = new Color(240, 240, 255);
    private static final Color TEXT_SUB    = new Color(150, 145, 200);
    private static final Color SUCCESS     = new Color(56, 185, 130);
    private static final Color SUCCESS_HOV = new Color(85, 210, 155);
    private static final Color DANGER      = new Color(220, 75, 90);
    private static final Color DANGER_HOV  = new Color(240, 100, 115);

    // Directorio de barajas dentro del proyecto
    static final Path BARAJAS_DIR =
            Paths.get(System.getProperty("user.dir"), "Docs", "Barajas");

    private JPanel    deckListPanel;
    private JLabel    selectedLabel;

    public BarajasView() {
        setTitle("Flashes — Barajas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(1080, 460);
        setLocationRelativeTo(null);

        ensureBarajasDir();

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(28, 48, 24, 48));

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildScroll(),  BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);

        setContentPane(root);
        refreshDeckList();
    }

    // ── Secciones ────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 22, 0));

        JLabel title = new JLabel("🗂  Barajas");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);

        JButton addBtn = makeIconButton("➕", ACCENT, ACCENT_HOV);
        addBtn.setFont(new Font("Dialog", Font.BOLD, 22));
        addBtn.setPreferredSize(new Dimension(48, 48));
        addBtn.setToolTipText("Importar baraja (.csv)");
        addBtn.addActionListener(e -> addDeck());

        header.add(title,  BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        return header;
    }

    private JScrollPane buildScroll() {
        deckListPanel = new JPanel();
        deckListPanel.setBackground(BG);
        deckListPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 14, 10));

        JScrollPane scroll = new JScrollPane(deckListPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scroll.getVerticalScrollBar());
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(18, 0, 0, 0));

        // Separador visual
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 48, 80));
        sep.setBackground(BG);

        selectedLabel = new JLabel();
        selectedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        selectedLabel.setForeground(TEXT_SUB);
        refreshSelectedLabel();

        JButton okBtn = new JButton("OK") {
            private Color current = ACCENT;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(80, 34));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { current = ACCENT_HOV;       repaint(); }
                    @Override public void mouseExited (MouseEvent e) { current = ACCENT;           repaint(); }
                    @Override public void mousePressed(MouseEvent e) { current = ACCENT.darker();  repaint(); }
                    @Override public void mouseReleased(MouseEvent e){ current = ACCENT_HOV;       repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(current);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        okBtn.addActionListener(e -> dispose());

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.add(selectedLabel, BorderLayout.WEST);
        bottomRow.add(okBtn,         BorderLayout.EAST);

        footer.add(sep,                          BorderLayout.NORTH);
        footer.add(Box.createVerticalStrut(10),  BorderLayout.CENTER);
        footer.add(bottomRow,                    BorderLayout.SOUTH);
        return footer;
    }

    // ── Lógica de lista ──────────────────────────────────────────────────────

    private void refreshDeckList() {
        deckListPanel.removeAll();

        File[] files = BARAJAS_DIR.toFile().listFiles(
                f -> f.isFile() && f.getName().toLowerCase().endsWith(".csv"));

        if (files == null || files.length == 0) {
            JLabel empty = new JLabel("<html><div style='text-align:center;'>"
                    + "Aún no hay barajas.<br>¡Agrega una con el botón +"
                    + " en la esquina superior!</div></html>",
                    SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            empty.setForeground(TEXT_SUB);
            deckListPanel.add(empty);
        } else {
            for (File f : files) {
                deckListPanel.add(makeDeckCard(f));
            }
        }

        deckListPanel.revalidate();
        deckListPanel.repaint();
        refreshSelectedLabel();
    }

    private JPanel makeDeckCard(File file) {
        boolean isSelected = file.getAbsolutePath()
                .equals(AppState.getInstance().getSelectedDeckPath());

        JPanel card = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected ? CARD_SEL : CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 40, 40));
                if (isSelected) {
                    g2.setColor(ACCENT);
                    g2.setStroke(new BasicStroke(1.6f));
                    g2.draw(new RoundRectangle2D.Float(0.8f, 0.8f,
                            getWidth() - 1.6f, getHeight() - 1.6f, 40, 40));
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 28, 20, 24));
        card.setPreferredSize(new Dimension(300, 80));

        // Icono + info
        JLabel iconLbl = new JLabel("📋");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        JLabel nameLbl = new JLabel(file.getName());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLbl.setForeground(TEXT_MAIN);

        JLabel sizeLbl = new JLabel(formatSize(file.length()));
        sizeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sizeLbl.setForeground(TEXT_SUB);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);
        namePanel.add(nameLbl);
        namePanel.add(Box.createVerticalStrut(2));
        namePanel.add(sizeLbl);

        JPanel info = new JPanel(new BorderLayout(10, 0));
        info.setOpaque(false);
        info.add(iconLbl,   BorderLayout.WEST);
        info.add(namePanel, BorderLayout.CENTER);

        // Botones
        JButton selectBtn = makeIconButton("✔", SUCCESS, SUCCESS_HOV);
        selectBtn.setToolTipText("Seleccionar esta baraja");
        selectBtn.addActionListener(e -> {
            AppState.getInstance().setSelectedDeckPath(file.getAbsolutePath());
            refreshDeckList();
        });

        JButton deleteBtn = makeIconButton("✖", DANGER, DANGER_HOV);
        deleteBtn.setToolTipText("Eliminar esta baraja");
        deleteBtn.addActionListener(e -> deleteDeck(file));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(selectBtn);
        btnPanel.add(deleteBtn);

        card.add(info,     BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.EAST);
        return card;
    }

    // ── Operaciones de archivos ───────────────────────────────────────────────

    private void addDeck() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar archivo CSV de baraja");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File src = chooser.getSelectedFile();

        // Validar extensión (defensa en profundidad)
        if (!src.getName().toLowerCase().endsWith(".csv")) {
            JOptionPane.showMessageDialog(this,
                    "Solo se permiten archivos .csv.",
                    "Archivo inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Sanitizar nombre: eliminar separadores de ruta
        String safeName = src.getName().replaceAll("[/\\\\]", "_");
        Path dest = BARAJAS_DIR.resolve(safeName);

        // Seguridad: verificar que el destino quede dentro de BARAJAS_DIR
        if (!dest.normalize().startsWith(BARAJAS_DIR.normalize())) {
            JOptionPane.showMessageDialog(this,
                    "Destino inválido.",
                    "Error de seguridad", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dest.toFile().exists()) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Ya existe una baraja con el nombre \"" + safeName + "\".\n¿Reemplazar?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
        }

        try {
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            refreshDeckList();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo copiar el archivo:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDeck(File file) {
        int opt = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la baraja \"" + file.getName() + "\"?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;

        // Si la baraja eliminada era la seleccionada, limpiar selección
        if (file.getAbsolutePath().equals(AppState.getInstance().getSelectedDeckPath())) {
            AppState.getInstance().setSelectedDeckPath(null);
        }

        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo eliminar el archivo:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        refreshDeckList();
    }

    // ── Utilidades visuales ──────────────────────────────────────────────────

    private void refreshSelectedLabel() {
        String name = AppState.getInstance().getSelectedDeckDisplayName();
        selectedLabel.setText("Baraja activa:  " + name);
        selectedLabel.setForeground(
                AppState.getInstance().hasSelectedDeck() ? ACCENT : TEXT_SUB);
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        return (bytes / 1024) + " KB";
    }

    private void ensureBarajasDir() {
        try {
            Files.createDirectories(BARAJAS_DIR);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo crear el directorio de Barajas:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton makeIconButton(String text, Color base, Color hover) {
        return new JButton(text) {
            private Color current = base;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setForeground(Color.WHITE);
                setFont(new Font("Dialog", Font.BOLD, 12));
                setMargin(new Insets(0, 0, 0, 0));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(35, 35));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { current = hover;        repaint(); }
                    @Override public void mouseExited (MouseEvent e) { current = base;         repaint(); }
                    @Override public void mousePressed(MouseEvent e) { current = base.darker();repaint(); }
                    @Override public void mouseReleased(MouseEvent e){ current = hover;        repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(current);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    private void styleScrollBar(JScrollBar bar) {
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(80, 78, 130);
                trackColor = BG;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
                g2.dispose();
            }
        });
        bar.setBackground(BG);
    }

    // ── WrapLayout: FlowLayout con wrap correcto en JScrollPane ─────────────
    private static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - insets.left - insets.right - hgap * 2;

                int width = 0, height = 0, rowWidth = 0, rowHeight = 0;

                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component c = target.getComponent(i);
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (rowWidth + d.width > maxWidth && rowWidth > 0) {
                        width = Math.max(width, rowWidth);
                        height += rowHeight + vgap;
                        rowWidth = 0; rowHeight = 0;
                    }
                    rowWidth += d.width + hgap;
                    rowHeight = Math.max(rowHeight, d.height);
                }
                width  = Math.max(width, rowWidth);
                height += rowHeight + insets.top + insets.bottom + vgap * 2;
                return new Dimension(width, height);
            }
        }
    }
}
