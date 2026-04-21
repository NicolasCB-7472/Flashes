package com.flashcards;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class LogsView extends JFrame {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color BG        = new Color(18, 18, 30);
    private static final Color CARD_BG   = new Color(28, 28, 46);
    private static final Color CARD_SEL  = new Color(38, 35, 80);
    private static final Color TEXT_MAIN = new Color(240, 240, 255);
    private static final Color TEXT_SUB  = new Color(150, 145, 200);

    private static final Path LOGS_DIR =
            Paths.get(System.getProperty("user.dir"), "Docs", "Logs");

    private JPanel   listPanel;
    private JTextArea contentArea;
    private JLabel   emptyHint;

    public LogsView() {
        setTitle("Flashes — Logs");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(760, 500);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(28, 40, 24, 40));

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildBody(),    BorderLayout.CENTER);

        setContentPane(root);
        refreshList();
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("📋  Logs de sesión");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_MAIN);

        header.add(title, BorderLayout.WEST);
        return header;
    }

    // ── Body: lista izq. + contenido der. ─────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);

        // ── Panel izquierdo: lista de archivos ────────────────────────────────
        listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JScrollPane listScroll = new JScrollPane(listPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listScroll.setBorder(null);
        listScroll.setOpaque(false);
        listScroll.getViewport().setOpaque(false);
        listScroll.getVerticalScrollBar().setUnitIncrement(12);
        styleScrollBar(listScroll.getVerticalScrollBar());
        listScroll.setPreferredSize(new Dimension(200, 0));

        // ── Panel derecho: contenido del log ─────────────────────────────────
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        contentArea.setBackground(CARD_BG);
        contentArea.setForeground(TEXT_MAIN);
        contentArea.setCaretColor(TEXT_MAIN);
        contentArea.setBorder(new EmptyBorder(16, 16, 16, 16));
        contentArea.setLineWrap(false);

        JScrollPane contentScroll = new JScrollPane(contentArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentScroll.setBorder(null);
        contentScroll.setOpaque(false);
        contentScroll.getViewport().setBackground(CARD_BG);
        styleScrollBar(contentScroll.getVerticalScrollBar());
        styleScrollBar(contentScroll.getHorizontalScrollBar());

        // Rounded wrapper for content panel
        JPanel contentWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        contentWrapper.setOpaque(false);
        contentWrapper.add(contentScroll, BorderLayout.CENTER);

        emptyHint = new JLabel("<html><div style='text-align:center;'>Seleccioná un log<br>para ver su contenido</div></html>",
                SwingConstants.CENTER);
        emptyHint.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        emptyHint.setForeground(TEXT_SUB);
        contentWrapper.add(emptyHint, BorderLayout.NORTH);

        body.add(listScroll,    BorderLayout.WEST);
        body.add(contentWrapper, BorderLayout.CENTER);
        return body;
    }

    // ── Lista de logs ─────────────────────────────────────────────────────────

    private void refreshList() {
        listPanel.removeAll();

        List<Path> logs = new ArrayList<>();
        if (Files.isDirectory(LOGS_DIR)) {
            try {
                logs = Files.list(LOGS_DIR)
                        .filter(p -> p.getFileName().toString().endsWith(".txt"))
                        .sorted(Comparator.comparing(Path::getFileName).reversed())
                        .collect(Collectors.toList());
            } catch (IOException ignored) {}
        }

        if (logs.isEmpty()) {
            JLabel none = new JLabel("Sin logs todavía");
            none.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            none.setForeground(TEXT_SUB);
            none.setBorder(new EmptyBorder(8, 4, 0, 0));
            listPanel.add(none);
        } else {
            for (Path p : logs) {
                listPanel.add(makeLogEntry(p));
                listPanel.add(Box.createVerticalStrut(6));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JButton makeLogEntry(Path logPath) {
        String name = logPath.getFileName().toString().replace(".txt", "");
        JButton btn = new JButton(name) {
            private Color bg = CARD_BG;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setForeground(TEXT_MAIN);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setHorizontalAlignment(SwingConstants.LEFT);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
                setPreferredSize(new Dimension(180, 38));
                setBorder(new EmptyBorder(0, 12, 0, 8));

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { bg = CARD_SEL; repaint(); }
                    @Override public void mouseExited (MouseEvent e) { bg = CARD_BG;  repaint(); }
                });
                addActionListener(e -> loadLog(logPath));
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        return btn;
    }

    private void loadLog(Path logPath) {
        try {
            String content = Files.readString(logPath, StandardCharsets.UTF_8);
            emptyHint.setVisible(false);
            contentArea.setText(content);
            contentArea.setCaretPosition(0);
        } catch (IOException ex) {
            contentArea.setText("Error al leer el archivo:\n" + ex.getMessage());
        }
    }

    // ── Scroll bar styling ────────────────────────────────────────────────────

    private void styleScrollBar(JScrollBar bar) {
        bar.setBackground(BG);
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(80, 78, 130);
                trackColor = BG;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }
}
