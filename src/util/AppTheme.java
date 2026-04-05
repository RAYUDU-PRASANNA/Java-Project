package util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class AppTheme {
    // Color Palette
    public static final Color PRIMARY       = new Color(13, 71, 161);    // Deep Blue
    public static final Color PRIMARY_LIGHT = new Color(63, 114, 198);
    public static final Color PRIMARY_DARK  = new Color(0, 40, 110);
    public static final Color ACCENT        = new Color(255, 160, 0);    // Amber
    public static final Color ACCENT_DARK   = new Color(200, 120, 0);
    public static final Color SUCCESS       = new Color(46, 125, 50);
    public static final Color DANGER        = new Color(198, 40, 40);
    public static final Color WARNING       = new Color(230, 150, 0);
    public static final Color BG_LIGHT      = new Color(245, 247, 252);
    public static final Color BG_WHITE      = Color.WHITE;
    public static final Color CARD_BG       = new Color(255, 255, 255);
    public static final Color BORDER_COLOR  = new Color(220, 225, 235);
    public static final Color TEXT_PRIMARY  = new Color(20, 30, 60);
    public static final Color TEXT_SECONDARY= new Color(100, 110, 140);
    public static final Color TEXT_LIGHT    = new Color(160, 170, 195);
    public static final Color SIDEBAR_BG    = new Color(10, 25, 65);
    public static final Color SIDEBAR_HOVER = new Color(30, 55, 110);
    public static final Color SIDEBAR_ACTIVE= new Color(13, 71, 161);

    // Fonts
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_HEADING   = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBHEAD   = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BOLD      = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_LABEL     = new Font("Segoe UI", Font.PLAIN, 12);

    public static void applyGlobalTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        UIManager.put("Panel.background", BG_LIGHT);
        UIManager.put("OptionPane.background", BG_WHITE);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
    }

    // --- Reusable Component Factories ---

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(PRIMARY_DARK);
                else if (getModel().isRollover()) g2.setColor(PRIMARY_LIGHT);
                else g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 40));
        return btn;
    }

    public static JButton accentButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(ACCENT_DARK);
                else if (getModel().isRollover()) g2.setColor(ACCENT.brighter());
                else g2.setColor(ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(TEXT_PRIMARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 40));
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? DANGER.darker() : getModel().isRollover() ? new Color(220,60,60) : DANGER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 36));
        return btn;
    }

    public static JTextField styledField(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(TEXT_LIGHT);
                    g2.setFont(FONT_LABEL);
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(BORDER_COLOR, 8),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.setPreferredSize(new Dimension(200, 40));
        return field;
    }

    public static JPasswordField styledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(TEXT_LIGHT);
                    g2.setFont(FONT_LABEL);
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(BORDER_COLOR, 8),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.setPreferredSize(new Dimension(200, 40));
        return field;
    }

    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(Color.WHITE);
        combo.setBorder(new RoundBorder(BORDER_COLOR, 8));
        combo.setPreferredSize(new Dimension(200, 40));
        return combo;
    }

    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SUBHEAD);
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    public static JPanel card(int arc) {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    // Round border helper
    public static class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        public RoundBorder(Color c, int r) { color = c; radius = r; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(4,4,4,4); }
    }
}
