package com.flashcards;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class MainMenu extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(18, 18, 30);
    // private static final Color CARD_BG     = new Color(28, 28, 46);
    private static final Color ACCENT      = new Color(108, 99, 255);
    private static final Color ACCENT_HOV  = new Color(140, 133, 255);
    private static final Color ACCENT_DARK = new Color(75, 68, 200);
    private static final Color TEXT_MAIN   = new Color(240, 240, 255);
    private static final Color TEXT_SUB    = new Color(150, 145, 200);
    private static final Color BTN_GHOST   = new Color(38, 38, 60);
    private static final Color BTN_GHOST_H = new Color(55, 55, 85);
    private static final Color DANGER      = new Color(220, 75, 90);
    private static final Color DANGER_HOV  = new Color(240, 100, 115);

    public MainMenu() {
        setTitle("Flashes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(420, 580);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle radial glow at the top
                RadialGradientPaint glow = new RadialGradientPaint(
                    210, 60, 220,
                    new float[]{0f, 1f},
                    new Color[]{new Color(108, 99, 255, 55), new Color(18, 18, 30, 0)}
                );
                g2.setPaint(glow);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(40, 40, 40, 40));

        // ── Header ───────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("⚡", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        icon.setForeground(ACCENT);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Flashes", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Tu compañero de estudio", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_SUB);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(icon);
        header.add(Box.createVerticalStrut(6));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        // ── Buttons ──────────────────────────────────────────────────────────
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));

        JButton barajasBtn = makeGhostButton("🗂   Barajas", BTN_GHOST, BTN_GHOST_H);
        barajasBtn.addActionListener(e -> new BarajasView().setVisible(true));

        JButton playBtn = makePrimaryButton("▶   Play", ACCENT, ACCENT_HOV, ACCENT_DARK);
        playBtn.addActionListener(e -> new PlayView());

        JButton salirBtn = makePrimaryButton("✕   Salir", DANGER, DANGER_HOV, new Color(180, 50, 65));
        salirBtn.addActionListener(e -> System.exit(0));

        buttons.add(Box.createVerticalStrut(36));
        buttons.add(playBtn);
        buttons.add(Box.createVerticalStrut(14));
        buttons.add(barajasBtn);
        buttons.add(Box.createVerticalStrut(14));
        JButton configBtn = makeGhostButton("⚙️   Configuración", BTN_GHOST, BTN_GHOST_H);
        configBtn.addActionListener(e -> new ConfigView().setVisible(true));
        buttons.add(configBtn);
        buttons.add(Box.createVerticalStrut(14));
        buttons.add(salirBtn);

        // ── Version label ────────────────────────────────────────────────────
        JLabel version = new JLabel("v0.1.0", SwingConstants.CENTER);
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(new Color(80, 78, 110));
        version.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(version);

        root.add(header,  BorderLayout.NORTH);
        root.add(buttons, BorderLayout.CENTER);
        root.add(footer,  BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ── Button factories ─────────────────────────────────────────────────────

    private JButton makePrimaryButton(String text, Color base, Color hover, Color pressed) {
        JButton btn = new JButton(text) {
            private Color current = base;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 15));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setAlignmentX(Component.CENTER_ALIGNMENT);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
                setPreferredSize(new Dimension(300, 52));

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { current = hover;   repaint(); }
                    @Override public void mouseExited (MouseEvent e) { current = base;    repaint(); }
                    @Override public void mousePressed(MouseEvent e) { current = pressed; repaint(); }
                    @Override public void mouseReleased(MouseEvent e){ current = hover;   repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(current);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        return btn;
    }

    private JButton makeGhostButton(String text, Color base, Color hover) {
        JButton btn = new JButton(text) {
            private Color current = base;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setForeground(TEXT_MAIN);
                setFont(new Font("Segoe UI", Font.PLAIN, 15));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setAlignmentX(Component.CENTER_ALIGNMENT);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
                setPreferredSize(new Dimension(300, 52));

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { current = hover; repaint(); }
                    @Override public void mouseExited (MouseEvent e) { current = base;  repaint(); }
                    @Override public void mousePressed(MouseEvent e) { current = new Color(65, 65, 95); repaint(); }
                    @Override public void mouseReleased(MouseEvent e){ current = hover; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(current);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                // Subtle border
                g2.setColor(new Color(80, 78, 130));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth() - 1.2f, getHeight() - 1.2f, 16, 16));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        return btn;
    }
}
