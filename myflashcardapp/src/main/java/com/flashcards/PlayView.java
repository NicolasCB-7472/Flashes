package com.flashcards;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class PlayView extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color BG         = new Color(18, 18, 30);
    private static final Color CARD_FRONT = new Color(28, 28, 46);
    private static final Color CARD_BACK  = new Color(30, 24, 58);
    private static final Color ACCENT     = new Color(108, 99, 255);
    private static final Color ACCENT_DIM = new Color(108, 99, 255, 160);
    private static final Color TEXT_MAIN  = new Color(240, 240, 255);
    private static final Color TEXT_SUB   = new Color(150, 145, 200);
    private static final Color SUCCESS    = new Color(56, 185, 130);
    private static final Color SUCCESS_H  = new Color(85, 210, 155);
    private static final Color DANGER     = new Color(220, 75, 90);
    private static final Color DANGER_H   = new Color(240, 100, 115);
    private static final Color GHOST      = new Color(38, 38, 60);
    private static final Color GHOST_H    = new Color(58, 58, 88);

    // ── State ─────────────────────────────────────────────────────────────────
    private List<String[]> cards = new ArrayList<>();  // [0]=primera [1]=segunda [2]=indice_fila
    private int            currentIndex  = 0;
    private boolean        showingFront  = true;
    private final Set<Integer> correctSet   = new HashSet<>();
    private final Set<Integer> incorrectSet = new HashSet<>();

    // ── Flip animation ────────────────────────────────────────────────────────
    private float   scaleX    = 1.0f;
    private boolean shrinking = true;
    private boolean flipping  = false;
    private javax.swing.Timer flipTimer;

    // ── UI refs ───────────────────────────────────────────────────────────────
    private JPanel  cardPanel;
    private JLabel  cardTextLabel;
    private JLabel  sideLabel;
    private JLabel  progressLabel;
    private JLabel  correctCountLabel;
    private NavButton prevBtn;
    private NavButton nextBtn;

    public PlayView() {
        String path = AppState.getInstance().getSelectedDeckPath();
        if (path == null) {
            JOptionPane.showMessageDialog(null,
                    "No hay ninguna baraja seleccionada.\nVe a Barajas y selecciona una primero.",
                    "Sin baraja", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        try {
            cards = parseCsv(path);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error al leer el CSV:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        if (cards.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "La baraja no contiene tarjetas.", "Vacía",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }

        if (AppState.getInstance().isRandomizarBaraja()) {
            Collections.shuffle(cards);
        } else {
            cards.sort(Comparator.comparingInt(c -> Integer.parseInt(c[2])));
        }

        setTitle("Flashes — Play");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(820, 500);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { writeLog(); }
        });

        buildUI();
        updateCard();
        setVisible(true);
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(24, 48, 24, 48));

        root.add(buildTopBar(),    BorderLayout.NORTH);
        root.add(buildCardArea(),  BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 16, 0));

        String deckName = Paths.get(AppState.getInstance().getSelectedDeckPath())
                .getFileName().toString();
        JLabel deckLabel = new JLabel("⚡  " + deckName);
        deckLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        deckLabel.setForeground(TEXT_MAIN);

        progressLabel = new JLabel("", SwingConstants.CENTER);
        progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        progressLabel.setForeground(TEXT_SUB);

        correctCountLabel = new JLabel("", SwingConstants.RIGHT);
        correctCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        correctCountLabel.setForeground(SUCCESS);

        bar.add(deckLabel,        BorderLayout.WEST);
        bar.add(progressLabel,    BorderLayout.CENTER);
        bar.add(correctCountLabel, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildCardArea() {
        // The card panel overrides paint() so the flip scale affects all children
        cardPanel = new JPanel(new GridBagLayout()) {
            @Override
            public void paint(Graphics g) {
                // Guard: scale(0, y) produces a degenerate clip → NPE in SunGraphics2D
                if (scaleX <= 0.01f) return;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Horizontal flip: scale around center
                double cx = getWidth()  / 2.0;
                double cy = getHeight() / 2.0;
                g2.translate(cx, cy);
                g2.scale(scaleX, 1.0);
                g2.translate(-cx, -cy);

                super.paint(g2);
                g2.dispose();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = showingFront ? CARD_FRONT : CARD_BACK;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 22, 22));

                // Top accent bar
                g2.setColor(ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 5, 5, 5));

                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Inner content (side label + text)
        sideLabel = new JLabel("PREGUNTA", SwingConstants.CENTER);
        sideLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sideLabel.setForeground(ACCENT_DIM);

        cardTextLabel = new JLabel("", SwingConstants.CENTER);
        cardTextLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        cardTextLabel.setForeground(TEXT_MAIN);
        cardTextLabel.setMaximumSize(new Dimension(640, Integer.MAX_VALUE));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setMaximumSize(new Dimension(660, Integer.MAX_VALUE));
        sideLabel.setAlignmentX(CENTER_ALIGNMENT);
        cardTextLabel.setAlignmentX(CENTER_ALIGNMENT);
        content.add(sideLabel);
        content.add(Box.createVerticalStrut(14));
        content.add(cardTextLabel);

        // Hint at the bottom of the card
        JLabel hint = new JLabel("clic para girar", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(new Color(90, 87, 130));
        hint.setAlignmentX(CENTER_ALIGNMENT);
        content.add(Box.createVerticalStrut(20));
        content.add(hint);

        cardPanel.add(content);

        cardPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { flipCard(); }
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 16, 0));
        wrapper.add(cardPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new GridBagLayout());
        bar.setOpaque(false);

        prevBtn = makeNavButton("←");
        prevBtn.addActionListener(e -> navigate(-1));

        JButton correctBtn = makeActionButton("✓", SUCCESS, SUCCESS_H);
        correctBtn.setToolTipText("Correcta  (+1)");
        correctBtn.addActionListener(e -> markAndAdvance(true));

        JButton wrongBtn = makeActionButton("✕", DANGER, DANGER_H);
        wrongBtn.setToolTipText("Incorrecta");
        wrongBtn.addActionListener(e -> markAndAdvance(false));

        nextBtn = makeNavButton("→");
        nextBtn.addActionListener(e -> navigate(1));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 12, 0, 12);
        bar.add(prevBtn,    gbc);
        bar.add(correctBtn, gbc);
        bar.add(wrongBtn,   gbc);
        bar.add(nextBtn,    gbc);

        return bar;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────

    private void flipCard() {
        if (flipping) return;
        flipping  = true;
        shrinking = true;

        // Stop any stale timer before creating a new one
        if (flipTimer != null) flipTimer.stop();

        // Capture in final local so the lambda never dereferences a reassigned field
        final javax.swing.Timer t = new javax.swing.Timer(12, null);
        flipTimer = t;

        t.addActionListener(e -> {
            if (shrinking) {
                scaleX = Math.max(0f, scaleX - 0.13f);
                if (scaleX <= 0f) {
                    scaleX = 0f;
                    showingFront = !showingFront;
                    updateCardText();
                    shrinking = false;
                }
            } else {
                scaleX = Math.min(1f, scaleX + 0.13f);
                if (scaleX >= 1f) {
                    scaleX = 1f;
                    flipping = false;
                    t.stop();
                }
            }
            cardPanel.repaint();
        });
        flipTimer.start();
    }

    private void navigate(int dir) {
        int next = currentIndex + dir;
        if (next < 0 || next >= cards.size()) return;
        currentIndex = next;
        showingFront = true;
        scaleX = 1.0f;
        updateCard();
    }

    private void markAndAdvance(boolean correct) {
        if (correct) {
            correctSet.add(currentIndex);
            incorrectSet.remove(currentIndex);
        } else {
            correctSet.remove(currentIndex);
            incorrectSet.add(currentIndex);
        }
        updateCounters();
        if (currentIndex < cards.size() - 1) navigate(1);
    }

    private void updateCard() {
        showingFront = true;
        updateCardText();
        updateCounters();
        updateNavButtons();
    }

    private void updateCardText() {
        String[] card = cards.get(currentIndex);
        String text = showingFront ? card[0] : card[1];

        // Adaptive font size: shorter text = larger font
        int len = text.length();
        int fontSize = len <= 20  ? 32
                     : len <= 50  ? 24
                     : len <= 100 ? 18
                     : 14;

        cardTextLabel.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
        cardTextLabel.setText("<html><div style='text-align:center; max-width:580px;'>"
                + escapeHtml(text) + "</div></html>");
        sideLabel.setText(showingFront ? "PREGUNTA" : "RESPUESTA");
        cardPanel.repaint();
    }

    private void updateCounters() {
        progressLabel.setText((currentIndex + 1) + " / " + cards.size());
        correctCountLabel.setText("✓  " + correctSet.size()
                + " / " + cards.size());
    }

    private void updateNavButtons() {
        prevBtn.setEnabled(currentIndex > 0);
        nextBtn.setEnabled(currentIndex < cards.size() - 1);
        prevBtn.setAlpha(currentIndex > 0 ? 1f : 0.3f);
        nextBtn.setAlpha(currentIndex < cards.size() - 1 ? 1f : 0.3f);
    }

    // ── Log Writer ────────────────────────────────────────────────────────────

    private void writeLog() {
        try {
            Path logsDir = Paths.get(System.getProperty("user.dir"), "Docs", "Logs");
            Files.createDirectories(logsDir);

            AppState state = AppState.getInstance();
            String tag      = state.getUserTag().isEmpty() ? "DEFAULT"
                              : state.getUserTag().replaceAll("[\\[\\]]", "");
            String nombre   = state.getUserName().isBlank() ? "User" : state.getUserName();
            String datetime = LocalDateTime.now()
                              .format(DateTimeFormatter.ofPattern("ddMMyy_HHmm"));

            // Sanitize: no spaces or path separators in filename
            nombre = nombre.replaceAll("[/\\\\\\s]", "_");

            String fileName = tag + "_" + nombre + "_" + datetime + ".txt";
            Path logFile = logsDir.resolve(fileName);

            // Security: ensure file stays inside logsDir
            if (!logFile.normalize().startsWith(logsDir.normalize())) return;

            StringBuilder sb = new StringBuilder();
            sb.append("=== CORRECTAS ===\n");
            for (int i : sorted(correctSet))   appendEntry(sb, i);
            sb.append("\n=== INCORRECTAS ===\n");
            for (int i : sorted(incorrectSet)) appendEntry(sb, i);
            sb.append("\n=== A DETERMINAR ===\n");
            for (int i = 0; i < cards.size(); i++) {
                if (!correctSet.contains(i) && !incorrectSet.contains(i)) appendEntry(sb, i);
            }

            Files.writeString(logFile, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            // Log write failure is non-critical; silently ignore
        }
    }

    private void appendEntry(StringBuilder sb, int index) {
        String[] c = cards.get(index);
        sb.append(c[0]).append(" — ").append(c[1]).append("\n");
    }

    private List<Integer> sorted(Set<Integer> set) {
        List<Integer> list = new ArrayList<>(set);
        Collections.sort(list);
        return list;
    }

    // ── CSV Parser ────────────────────────────────────────────────────────────

    private List<String[]> parseCsv(String path) throws IOException {
        List<String[]> result = new ArrayList<>();
        int firstCol = -1, secondCol = -1;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) return result;

            String[] headers = splitCsvLine(header);
            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].trim().toLowerCase();
                if (h.equals("primera_columna")) firstCol = i;
                if (h.equals("segunda_columna"))  secondCol = i;
            }

            if (firstCol < 0 || secondCol < 0) {
                throw new IOException(
                        "El CSV debe tener columnas 'primera_columna' y 'segunda_columna'.");
            }

            String line;
            int rowIndex = 1;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] fields = splitCsvLine(line);
                int maxIdx = Math.max(firstCol, secondCol);
                if (fields.length > maxIdx) {
                    result.add(new String[]{
                            fields[firstCol].trim(),
                            fields[secondCol].trim(),
                            String.valueOf(rowIndex)
                    });
                    rowIndex++;
                }
            }
        }
        return result;
    }

    private String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ── Button factories ──────────────────────────────────────────────────────

    /**
     * Nav buttons (← →) with alpha support for disabled visual state.
     */
    private NavButton makeNavButton(String text) {
        NavButton btn = new NavButton(text, GHOST, GHOST_H);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btn.setPreferredSize(new Dimension(56, 56));
        return btn;
    }

    private JButton makeActionButton(String text, Color base, Color hover) {
        return new JButton(text) {
            private Color current = base;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 18));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(62, 62));
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
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 50, 50));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
    }

    // ── Inner: NavButton with alpha ────────────────────────────────────────────
    private static class NavButton extends JButton {
        private Color current;
        private final Color base;
        private final Color hover;
        private float alpha = 1f;

        NavButton(String text, Color base, Color hover) {
            super(text);
            this.base    = base;
            this.hover   = hover;
            this.current = base;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { if (isEnabled()) { current = hover;        repaint(); } }
                @Override public void mouseExited (MouseEvent e) { if (isEnabled()) { current = base;         repaint(); } }
                @Override public void mousePressed(MouseEvent e) { if (isEnabled()) { current = base.darker();repaint(); } }
                @Override public void mouseReleased(MouseEvent e){ if (isEnabled()) { current = hover;        repaint(); } }
            });
        }

        void setAlpha(float a) { this.alpha = a; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(current);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 50, 50));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
