package ui;

import util.AppTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class SplashScreen extends JWindow {

    private int progress = 0;
    private float alpha = 0f;
    private Timer fadeTimer, progressTimer;

    public SplashScreen() {
        setSize(580, 360);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        JPanel content = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Drop shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(8, 8, getWidth()-10, getHeight()-10, 24, 24);

                // Background gradient
                GradientPaint bgGrad = new GradientPaint(0, 0, AppTheme.PRIMARY_DARK, getWidth(), getHeight(), new Color(13,71,161));
                g2.setPaint(bgGrad);
                g2.fillRoundRect(0, 0, getWidth()-10, getHeight()-10, 24, 24);

                // Decorative circles
                g2.setColor(new Color(255,255,255,15));
                g2.fillOval(-60, -60, 220, 220);
                g2.fillOval(getWidth()-200, getHeight()-200, 260, 260);

                // Bus icon (drawn)
                drawBusIcon(g2, getWidth()/2 - 45, 50);

                // Title
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 30));
                g2.setColor(Color.WHITE);
                String title = "BusGo Express";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(title, (getWidth()-10-fm.stringWidth(title))/2, 175);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(new Color(255,255,255,180));
                String sub = "Smart Bus Ticket Booking System";
                fm = g2.getFontMetrics();
                g2.drawString(sub, (getWidth()-10-fm.stringWidth(sub))/2, 198);

                // Progress bar background
                int barX = 80, barY = 270, barW = getWidth()-180, barH = 7;
                g2.setColor(new Color(255,255,255,40));
                g2.fillRoundRect(barX, barY, barW, barH, 6, 6);

                // Progress bar fill
                GradientPaint barGrad = new GradientPaint(barX, 0, AppTheme.ACCENT, barX + barW, 0, new Color(255,200,0));
                g2.setPaint(barGrad);
                g2.fillRoundRect(barX, barY, (int)(barW * progress / 100.0), barH, 6, 6);

                // Progress text
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(new Color(255,255,255,150));
                String pct = "Loading... " + progress + "%";
                fm = g2.getFontMetrics();
                g2.drawString(pct, (getWidth()-10-fm.stringWidth(pct))/2, 300);

                // Version
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(new Color(255,255,255,100));
                g2.drawString("v1.0  |  University Mini Project", 20, getHeight()-24);

                g2.dispose();
            }

            private void drawBusIcon(Graphics2D g2, int x, int y) {
                g2.setColor(new Color(255,255,255,200));
                // Bus body
                g2.fillRoundRect(x, y+20, 90, 50, 12, 12);
                // Windshield
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillRoundRect(x+5, y+25, 30, 22, 4, 4);
                // Windows
                g2.fillRoundRect(x+42, y+25, 18, 16, 4, 4);
                g2.fillRoundRect(x+65, y+25, 18, 16, 4, 4);
                // Wheels
                g2.setColor(new Color(30,30,30));
                g2.fillOval(x+10, y+62, 22, 22);
                g2.fillOval(x+58, y+62, 22, 22);
                g2.setColor(new Color(255,255,255,200));
                g2.fillOval(x+16, y+68, 10, 10);
                g2.fillOval(x+64, y+68, 10, 10);
                // Door
                g2.setColor(new Color(255,255,255,100));
                g2.fillRoundRect(x+76, y+33, 10, 30, 3, 3);
            }
        };
        content.setOpaque(false);
        setContentPane(content);

        // Fade in animation
        fadeTimer = new Timer(30, e -> {
            alpha = Math.min(1f, alpha + 0.05f);
            repaint();
            if (alpha >= 1f) fadeTimer.stop();
        });

        // Progress animation
        progressTimer = new Timer(25, e -> {
            progress = Math.min(100, progress + 1);
            repaint();
            if (progress >= 100) {
                progressTimer.stop();
                Timer closeTimer = new Timer(400, ev -> {
                    setVisible(false);
                    dispose();
                    SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
                });
                closeTimer.setRepeats(false);
                closeTimer.start();
            }
        });
    }

    public void showSplash() {
        setVisible(true);
        fadeTimer.start();
        progressTimer.start();
    }

    public static void main(String[] args) {
        AppTheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new SplashScreen().showSplash());
    }
}
