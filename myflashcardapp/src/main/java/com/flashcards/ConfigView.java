package com.flashcards;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

public class ConfigView extends JFrame {

    // ── Palette (misma que el resto de la app) ────────────────────────────────
    private static final Color BG        = new Color(18, 18, 30);
    private static final Color CARD_BG   = new Color(28, 28, 46);
    private static final Color ACCENT    = new Color(108, 99, 255);
    private static final Color TEXT_MAIN = new Color(240, 240, 255);
    private static final Color TEXT_SUB  = new Color(150, 145, 200);
    private static final Color INPUT_BG  = new Color(38, 36, 65);

    private static final String[] AVATAR_FILES = {
        "Geografia.png", "Investigacion.png", "Lenguajes.png", "Medicina.png", "Software.png"
    };
    private static final String[] AVATAR_TAGS = {
        "[GEO]", "[INV]", "[LEN]", "[MED]", "[SOF]"
    };
    private static final Path AVATARES_DIR =
            Paths.get(System.getProperty("user.dir"), "Docs", "Avatares");

    private JLabel         tagLabel;
    private JTextField     userNameField;
    private AvatarButton[] avatarBtns;

    public ConfigView() {
        setTitle("Flashes — Configuración");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(520, 600);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(28, 40, 28, 40));

        JLabel title = new JLabel("⚙  Configuración");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel card = buildSettingRow(
                "Randomizar Baraja",
                "Mezcla las tarjetas aleatoriamente al iniciar",
                AppState.getInstance().isRandomizarBaraja(),
                AppState.getInstance()::setRandomizarBaraja
        );

        JButton logsBtn = makeLogsButton();

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(buildProfileCard());
        center.add(Box.createVerticalStrut(14));
        center.add(card);
        center.add(Box.createVerticalStrut(14));
        center.add(logsBtn);
        center.add(Box.createVerticalStrut(20));
        center.add(buildAceptarButton());

        root.add(title,  BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
    }

    // ── Tarjeta de perfil ─────────────────────────────────────────────────────

    private JPanel buildProfileCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 22, 22, 22));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Subtítulo
        JLabel subtitle = new JLabel("Perfil de Usuario");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subtitle.setForeground(TEXT_SUB);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        // Fila de avatares (GridLayout garantiza 5 en una sola fila)
        JPanel avatarsRow = new JPanel(new GridLayout(1, AVATAR_FILES.length, 12, 0));
        avatarsRow.setOpaque(false);
        avatarsRow.setAlignmentX(LEFT_ALIGNMENT);
        avatarsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        avatarsRow.setMinimumSize(new Dimension(0, 72));
        avatarsRow.setPreferredSize(new Dimension(Short.MAX_VALUE, 72));

        avatarBtns = new AvatarButton[AVATAR_FILES.length];
        int currentAvatar = AppState.getInstance().getSelectedAvatar();
        for (int i = 0; i < AVATAR_FILES.length; i++) {
            Path imgPath = AVATARES_DIR.resolve(AVATAR_FILES[i]);
            BufferedImage img = null;
            try { img = ImageIO.read(imgPath.toFile()); } catch (IOException ignored) {}
            final int idx = i;
            avatarBtns[i] = new AvatarButton(img, i == currentAvatar);
            avatarBtns[i].addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { selectAvatar(idx); }
            });
            avatarsRow.add(avatarBtns[i]);
        }

        // Fila de Tag
        JPanel tagRow = new JPanel(new BorderLayout(10, 0));
        tagRow.setOpaque(false);
        tagRow.setAlignmentX(LEFT_ALIGNMENT);
        tagRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tagRow.setMinimumSize(new Dimension(0, 34));
        tagRow.setPreferredSize(new Dimension(Short.MAX_VALUE, 34));
        tagRow.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel tagTitle = new JLabel("Tag:");
        tagTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tagTitle.setForeground(TEXT_SUB);
        tagTitle.setPreferredSize(new Dimension(52, 22));
        tagTitle.setHorizontalAlignment(SwingConstants.LEFT);

        tagLabel = new JLabel(AppState.getInstance().getUserTag().isEmpty()
                ? "—" : AppState.getInstance().getUserTag());
        tagLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        tagLabel.setForeground(ACCENT);
        tagLabel.setHorizontalAlignment(SwingConstants.LEFT);

        tagRow.add(tagTitle, BorderLayout.WEST);
        tagRow.add(tagLabel, BorderLayout.CENTER);

        // Fila de Nombre
        JPanel nameRow = new JPanel(new BorderLayout(10, 0));
        nameRow.setOpaque(false);
        nameRow.setAlignmentX(LEFT_ALIGNMENT);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        nameRow.setMinimumSize(new Dimension(0, 38));
        nameRow.setPreferredSize(new Dimension(Short.MAX_VALUE, 38));
        nameRow.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel nameTitle = new JLabel("Nombre:");
        nameTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameTitle.setForeground(TEXT_SUB);
        nameTitle.setPreferredSize(new Dimension(66, 28));
        nameTitle.setHorizontalAlignment(SwingConstants.LEFT);

        userNameField = new JTextField(AppState.getInstance().getUserName()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        userNameField.setOpaque(false);
        userNameField.setBorder(new EmptyBorder(4, 10, 4, 10));
        userNameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userNameField.setForeground(TEXT_MAIN);
        userNameField.setCaretColor(TEXT_MAIN);
        userNameField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { AppState.getInstance().setUserName(userNameField.getText()); }
            public void removeUpdate(DocumentEvent e)  { AppState.getInstance().setUserName(userNameField.getText()); }
            public void changedUpdate(DocumentEvent e) { AppState.getInstance().setUserName(userNameField.getText()); }
        });

        nameRow.add(nameTitle,     BorderLayout.WEST);
        nameRow.add(userNameField, BorderLayout.CENTER);

        card.add(subtitle);
        card.add(Box.createVerticalStrut(16));
        card.add(avatarsRow);
        card.add(Box.createVerticalStrut(14));
        card.add(tagRow);
        card.add(Box.createVerticalStrut(10));
        card.add(nameRow);

        return card;
    }

    private void selectAvatar(int idx) {
        AppState.getInstance().setSelectedAvatar(idx);
        for (int i = 0; i < avatarBtns.length; i++) {
            avatarBtns[i].setSelected(i == idx);
        }
        tagLabel.setText(AVATAR_TAGS[idx]);
    }

    // ── Avatar circular ───────────────────────────────────────────────────────

    private static class AvatarButton extends JComponent {
        private static final int SIZE = 80;
        private final BufferedImage image;
        private boolean selected;

        AvatarButton(BufferedImage image, boolean selected) {
            this.image    = image;
            this.selected = selected;
            setPreferredSize(new Dimension(SIZE, SIZE));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape circle = new Ellipse2D.Float(0, 0, SIZE, SIZE);

            if (image != null) {
                g2.setClip(circle);
                g2.drawImage(image, 0, 0, SIZE, SIZE, null);
                g2.setClip(null);
            } else {
                g2.setColor(new Color(55, 53, 90));
                g2.fill(circle);
                g2.setColor(new Color(150, 145, 200));
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
                FontMetrics fm = g2.getFontMetrics();
                String ph = "?";
                g2.drawString(ph, (SIZE - fm.stringWidth(ph)) / 2,
                              (SIZE - fm.getHeight()) / 2 + fm.getAscent());
            }

            // Anillo de selección
            if (selected) {
                g2.setColor(new Color(108, 99, 255));
                g2.setStroke(new BasicStroke(3.0f));
                g2.draw(new Ellipse2D.Float(1.5f, 1.5f, SIZE - 3f, SIZE - 3f));
            }

            g2.dispose();
        }
    }

    private JButton buildAceptarButton() {
        Color base  = new Color(108, 99, 255);
        Color hover = new Color(140, 133, 255);
        JButton btn = new JButton("Aceptar") {
            private Color current = base;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setAlignmentX(LEFT_ALIGNMENT);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
                setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { current = hover;       repaint(); }
                    @Override public void mouseExited (MouseEvent e) { current = base;        repaint(); }
                    @Override public void mousePressed(MouseEvent e) { current = base.darker(); repaint(); }
                    @Override public void mouseReleased(MouseEvent e){ current = hover;       repaint(); }
                });
                addActionListener(e -> dispose());
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(current);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        return btn;
    }

    private JButton makeLogsButton() {
        Color base  = new Color(38, 38, 60);
        Color hover = new Color(55, 55, 85);
        JButton btn = new JButton("📋   Ver Logs") {
            private Color current = base;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setForeground(TEXT_MAIN);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setAlignmentX(LEFT_ALIGNMENT);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
                setPreferredSize(new Dimension(Integer.MAX_VALUE, 48));
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(new EmptyBorder(0, 20, 0, 20));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { current = hover; repaint(); }
                    @Override public void mouseExited (MouseEvent e) { current = base;  repaint(); }
                    @Override public void mousePressed(MouseEvent e) { current = new Color(65, 65, 95); repaint(); }
                    @Override public void mouseReleased(MouseEvent e){ current = hover; repaint(); }
                });
                addActionListener(e -> new LogsView().setVisible(true));
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(current);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(new Color(80, 78, 130));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth()-1.2f, getHeight()-1.2f, 14, 14));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        return btn;
    }

    // ── Fila de configuración ─────────────────────────────────────────────────

    private JPanel buildSettingRow(String name, String desc,
                                   boolean initial, Consumer<Boolean> onChange) {
        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_MAIN);

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SUB);

        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(descLabel);

        ToggleSwitch toggle = new ToggleSwitch(initial);
        toggle.addToggleListener(onChange);

        // Centrar verticalmente el toggle
        JPanel toggleWrapper = new JPanel(new GridBagLayout());
        toggleWrapper.setOpaque(false);
        toggleWrapper.add(toggle);

        card.add(textPanel,    BorderLayout.CENTER);
        card.add(toggleWrapper, BorderLayout.EAST);

        return card;
    }

    // ── Toggle Switch personalizado ───────────────────────────────────────────

    private static class ToggleSwitch extends JComponent {

        private static final int W       = 46;
        private static final int H       = 26;
        private static final int THUMB_D = 20;

        // Colores del track
        private static final Color OFF_TRACK = new Color(55, 53, 90);
        private static final Color ON_TRACK  = new Color(56, 185, 130);

        private boolean on;
        private float   thumbPos;   // 0.0 = apagado, 1.0 = encendido
        private javax.swing.Timer animator;
        private Consumer<Boolean> listener;

        ToggleSwitch(boolean initial) {
            this.on       = initial;
            this.thumbPos = initial ? 1.0f : 0.0f;
            setPreferredSize(new Dimension(W, H));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { toggle(); }
            });
        }

        void addToggleListener(Consumer<Boolean> l) {
            this.listener = l;
        }

        private void toggle() {
            on = !on;
            if (animator != null && animator.isRunning()) animator.stop();
            float target = on ? 1.0f : 0.0f;
            animator = new javax.swing.Timer(12, null);
            animator.addActionListener(e -> {
                float diff = target - thumbPos;
                if (Math.abs(diff) < 0.04f) {
                    thumbPos = target;
                    ((javax.swing.Timer) e.getSource()).stop();
                } else {
                    thumbPos += diff * 0.28f;
                }
                repaint();
            });
            animator.start();
            if (listener != null) listener.accept(on);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Track con color interpolado entre OFF y ON
            float t = thumbPos;
            int r  = (int) (OFF_TRACK.getRed()   + (ON_TRACK.getRed()   - OFF_TRACK.getRed())   * t);
            int gr = (int) (OFF_TRACK.getGreen() + (ON_TRACK.getGreen() - OFF_TRACK.getGreen()) * t);
            int b  = (int) (OFF_TRACK.getBlue()  + (ON_TRACK.getBlue()  - OFF_TRACK.getBlue())  * t);
            g2.setColor(new Color(r, gr, b));
            g2.fill(new RoundRectangle2D.Float(0, 0, W, H, H, H));

            // Thumb deslizante
            int travel   = W - H;                          // pixeles que recorre el thumb
            int thumbLeft = (int) (3 + thumbPos * travel);
            int thumbTop  = (H - THUMB_D) / 2;
            g2.setColor(Color.WHITE);
            g2.fillOval(thumbLeft, thumbTop, THUMB_D, THUMB_D);

            g2.dispose();
        }
    }
}
