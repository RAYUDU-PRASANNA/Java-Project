package ui;

import util.AppTheme;
import dao.UserDAO;
import model.User;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LoginScreen extends JFrame {

    private JTextField userEmailField, adminEmailField;
    private JPasswordField userPassField, adminPassField;
    private int selectedTab = 0; // 0=User, 1=Admin
    private JPanel tabIndicator;

    public LoginScreen() {
        super("BusGo Express — Login");
        AppTheme.applyGlobalTheme();
        setSize(900, 580);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(AppTheme.BG_LIGHT);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);

        // LEFT PANEL — Branding
        JPanel left = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0,AppTheme.PRIMARY_DARK, getWidth(), getHeight(), new Color(13,71,161));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
                // decorative
                g2.setColor(new Color(255,255,255,12));
                g2.fillOval(-80,-80,300,300);
                g2.fillOval(getWidth()-180, getHeight()-180, 300,300);
                g2.dispose();
            }
        };
        left.setPreferredSize(new Dimension(360, 580));
        left.setLayout(new GridBagLayout());

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));

        // Bus illustration
        JPanel busIllust = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawBus(g2, 30, 10);
                g2.dispose();
            }
            private void drawBus(Graphics2D g2, int x, int y) {
                // Body
                g2.setColor(new Color(255,255,255,220));
                g2.fillRoundRect(x, y+30, 140, 70, 16, 16);
                // Front windshield
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillRoundRect(x+6, y+38, 40, 30, 6, 6);
                // Side windows
                g2.fillRoundRect(x+55, y+38, 24, 20, 4, 4);
                g2.fillRoundRect(x+86, y+38, 24, 20, 4, 4);
                g2.fillRoundRect(x+117, y+38, 18, 20, 4, 4);
                // Door
                g2.setColor(new Color(255,255,255,120));
                g2.fillRoundRect(x+120, y+47, 14, 44, 4, 4);
                // Wheels
                g2.setColor(new Color(30,30,30));
                g2.fillOval(x+14, y+92, 32, 32);
                g2.fillOval(x+90, y+92, 32, 32);
                g2.setColor(new Color(200,200,200));
                g2.fillOval(x+20, y+98, 20, 20);
                g2.fillOval(x+96, y+98, 20, 20);
                // Headlights
                g2.setColor(AppTheme.ACCENT);
                g2.fillRoundRect(x+140, y+45, 8, 8, 3, 3);
                // Road line
                g2.setColor(new Color(255,255,255,60));
                g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{12,8}, 0));
                g2.drawLine(x, y+124, x+200, y+124);
            }
        };
        busIllust.setOpaque(false);
        busIllust.setPreferredSize(new Dimension(220, 135));
        busIllust.setMaximumSize(new Dimension(220,135));
        busIllust.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel("BusGo Express");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Smart Ticket Booking System");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setForeground(new Color(255,255,255,180));
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255,255,255,50));
        sep.setMaximumSize(new Dimension(200, 1));

        // Feature bullets
        String[] features = {"🚌  Real-time seat selection", "🎫  Instant ticket generation", "📊  Admin analytics dashboard", "🔒  Secure & easy booking"};
        brandPanel.add(Box.createVerticalStrut(10));
        brandPanel.add(busIllust);
        brandPanel.add(Box.createVerticalStrut(10));
        brandPanel.add(titleLbl);
        brandPanel.add(Box.createVerticalStrut(6));
        brandPanel.add(subLbl);
        brandPanel.add(Box.createVerticalStrut(16));
        brandPanel.add(sep);
        brandPanel.add(Box.createVerticalStrut(16));

        for (String f : features) {
            JLabel fl = new JLabel(f);
            fl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            fl.setForeground(new Color(255,255,255,180));
            fl.setAlignmentX(Component.CENTER_ALIGNMENT);
            fl.setBorder(BorderFactory.createEmptyBorder(3,0,3,0));
            brandPanel.add(fl);
        }

        left.add(brandPanel);
        root.add(left, BorderLayout.WEST);

        // RIGHT PANEL — Login form
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);

        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JLabel welcome = new JLabel("Welcome Back!");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcome.setForeground(AppTheme.TEXT_PRIMARY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel loginSub = new JLabel("Please login to continue");
        loginSub.setFont(AppTheme.FONT_BODY);
        loginSub.setForeground(AppTheme.TEXT_SECONDARY);
        loginSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Tab switcher
        JPanel tabPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        tabPanel.setBackground(AppTheme.BG_LIGHT);
        tabPanel.setBorder(new RoundBorder(AppTheme.BORDER_COLOR, 10));
        tabPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tabPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton userTab = createTabButton("👤  Passenger", true);
        JButton adminTab = createTabButton("🔧  Admin", false);

        tabPanel.add(userTab);
        tabPanel.add(adminTab);

        // Card panel with CardLayout for switching
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardPanel.add(buildUserForm(), "user");
        cardPanel.add(buildAdminForm(), "admin");

        userTab.addActionListener(e -> {
            selectedTab = 0;
            cardLayout.show(cardPanel, "user");
            userTab.putClientProperty("active", true);
            userTab.setForeground(AppTheme.PRIMARY);
            adminTab.putClientProperty("active", false);
            adminTab.setForeground(AppTheme.TEXT_SECONDARY);
            userTab.repaint(); adminTab.repaint();
        });

        adminTab.addActionListener(e -> {
            selectedTab = 1;
            cardLayout.show(cardPanel, "admin");
            adminTab.putClientProperty("active", true);
            adminTab.setForeground(AppTheme.PRIMARY);
            userTab.putClientProperty("active", false);
            userTab.setForeground(AppTheme.TEXT_SECONDARY);
            adminTab.repaint(); userTab.repaint();
        });

        formWrapper.add(welcome);
        formWrapper.add(Box.createVerticalStrut(4));
        formWrapper.add(loginSub);
        formWrapper.add(Box.createVerticalStrut(20));
        formWrapper.add(tabPanel);
        formWrapper.add(Box.createVerticalStrut(20));
        formWrapper.add(cardPanel);

        right.add(formWrapper);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isSelected() || Boolean.TRUE.equals(getClientProperty("active"))) {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.putClientProperty("active", active);
        btn.setFont(AppTheme.FONT_BOLD);
        btn.setForeground(active ? AppTheme.PRIMARY : AppTheme.TEXT_SECONDARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Remove any icon that the L&F might inject
        btn.setIcon(null);
        btn.setSelectedIcon(null);
        btn.setDisabledIcon(null);
        btn.setRolloverIcon(null);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        return btn;
    }

    private JPanel buildUserForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);

        userEmailField = AppTheme.styledField("Email address");
        userEmailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        userEmailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        userPassField = AppTheme.styledPasswordField("Password");
        userPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        userPassField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel forgotRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        forgotRow.setBackground(Color.WHITE);
        forgotRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        forgotRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel forgot = new JLabel("Forgot password?");
        forgot.setFont(AppTheme.FONT_SMALL);
        forgot.setForeground(AppTheme.PRIMARY);
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgot.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                showForgotPasswordDialog();
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                forgot.setText("<html><u>Forgot password?</u></html>");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                forgot.setText("Forgot password?");
            }
        });
        forgotRow.add(forgot);

        JButton loginBtn = AppTheme.primaryButton("Login as Passenger");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> handleUserLogin());

        JPanel divider = buildDivider();
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton registerBtn = new JButton("Create New Account");
        registerBtn.setFont(AppTheme.FONT_BOLD);
        registerBtn.setForeground(AppTheme.PRIMARY);
        registerBtn.setBackground(Color.WHITE);
        registerBtn.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(AppTheme.PRIMARY, 10),
            BorderFactory.createEmptyBorder(8,0,8,0)
        ));
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.addActionListener(e -> openRegister());

        p.add(fieldLabel("Email Address"));
        p.add(Box.createVerticalStrut(4));
        p.add(userEmailField);
        p.add(Box.createVerticalStrut(12));
        p.add(fieldLabel("Password"));
        p.add(Box.createVerticalStrut(4));
        p.add(userPassField);
        p.add(Box.createVerticalStrut(4));
        p.add(forgotRow);
        p.add(Box.createVerticalStrut(16));
        p.add(loginBtn);
        p.add(Box.createVerticalStrut(16));
        p.add(divider);
        p.add(Box.createVerticalStrut(16));
        p.add(registerBtn);

        return p;
    }

    private JPanel buildAdminForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);

        // Admin warning banner
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        banner.setBackground(new Color(255, 243, 224));
        banner.setBorder(new RoundBorder(new Color(255,200,100), 8));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel bannerLbl = new JLabel("⚠  Restricted Access — Admin Only");
        bannerLbl.setFont(AppTheme.FONT_SMALL);
        bannerLbl.setForeground(new Color(180, 100, 0));
        banner.add(bannerLbl);

        adminEmailField = AppTheme.styledField("Admin username");
        adminEmailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        adminEmailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        adminPassField = AppTheme.styledPasswordField("Admin password");
        adminPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        adminPassField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = AppTheme.accentButton("Login as Admin");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> handleAdminLogin());

        p.add(banner);
        p.add(Box.createVerticalStrut(14));
        p.add(fieldLabel("Username"));
        p.add(Box.createVerticalStrut(4));
        p.add(adminEmailField);
        p.add(Box.createVerticalStrut(12));
        p.add(fieldLabel("Password"));
        p.add(Box.createVerticalStrut(4));
        p.add(adminPassField);
        p.add(Box.createVerticalStrut(20));
        p.add(loginBtn);

        return p;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_BOLD);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel buildDivider() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        JSeparator s1 = new JSeparator(); s1.setForeground(AppTheme.BORDER_COLOR);
        JSeparator s2 = new JSeparator(); s2.setForeground(AppTheme.BORDER_COLOR);
        JLabel or = new JLabel("  OR  ");
        or.setFont(AppTheme.FONT_SMALL);
        or.setForeground(AppTheme.TEXT_LIGHT);
        gbc.gridx = 0; p.add(s1, gbc);
        gbc.gridx = 1; gbc.weightx = 0; p.add(or, gbc);
        gbc.gridx = 2; gbc.weightx = 1; p.add(s2, gbc);
        return p;
    }

    // ─── Forgot Password — 3-step dialog ─────────────────────
    private void showForgotPasswordDialog() {
        JDialog dlg = new JDialog(this, "Reset Password", true);
        dlg.setSize(420, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);
        dlg.setLayout(new BorderLayout());

        // CardLayout for 3 steps inside the dialog
        CardLayout steps = new CardLayout();
        JPanel stepsPanel = new JPanel(steps);
        stepsPanel.setBackground(Color.WHITE);

        dao.UserDAO uDao = new dao.UserDAO();
        final String[] verifiedEmail = {""};  // shared across steps

        // ── Step 1: Enter Email ───────────────────────────────
        JPanel step1 = stepPanel();
        JLabel s1Title = stepTitle("🔑  Forgot Password");
        JLabel s1Sub   = stepSubtitle("Enter your registered email address.");
        JTextField emailInput = AppTheme.styledField("Email Address");
        emailInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel s1Err = errorLabel();

        JButton s1Next = AppTheme.primaryButton("Continue");
        s1Next.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        s1Next.addActionListener(e -> {
            String email = emailInput.getText().trim();
            if (email.isEmpty()) { s1Err.setText("Please enter your email."); return; }
            model.User u = uDao.getByEmail(email);
            if (u == null) { s1Err.setText("No account found with this email."); return; }
            verifiedEmail[0] = email;
            s1Err.setText("");
            steps.show(stepsPanel, "step2");
            dlg.setTitle("Reset Password — Verify Identity");
        });

        step1.add(s1Title);
        step1.add(Box.createVerticalStrut(4));
        step1.add(s1Sub);
        step1.add(Box.createVerticalStrut(20));
        step1.add(new JLabel("Email Address") {{ setFont(AppTheme.FONT_BOLD); setForeground(AppTheme.TEXT_PRIMARY); setAlignmentX(LEFT_ALIGNMENT); }});
        step1.add(Box.createVerticalStrut(4));
        step1.add(emailInput);
        step1.add(Box.createVerticalStrut(6));
        step1.add(s1Err);
        step1.add(Box.createVerticalStrut(16));
        step1.add(s1Next);

        // ── Step 2: Verify DOB ────────────────────────────────
        JPanel step2 = stepPanel();
        JLabel s2Title    = stepTitle("🔐  Verify Identity");
        JLabel s2Sub      = stepSubtitle("Enter your Date of Birth to confirm it's you.");
        JTextField dobInput = AppTheme.styledField("DD/MM/YYYY");
        dobInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel s2Err = errorLabel();

        JButton s2Next = AppTheme.primaryButton("Verify");
        s2Next.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        s2Next.addActionListener(e -> {
            String dob = dobInput.getText().trim();
            if (dob.isEmpty()) { s2Err.setText("Please enter your date of birth."); return; }
            // Convert DD/MM/YYYY → YYYY-MM-DD for comparison
            if (dob.matches("\\d{2}/\\d{2}/\\d{4}")) {
                String[] p = dob.split("/");
                dob = p[2] + "-" + p[1] + "-" + p[0];
            }
            model.User u = uDao.getByEmail(verifiedEmail[0]);
            String storedDob = u != null ? u.getDob() : null;
            if (storedDob == null || storedDob.isEmpty()) {
                // No DOB on file — skip verification, proceed anyway
                s2Err.setText("");
                steps.show(stepsPanel, "step3");
                dlg.setTitle("Reset Password — New Password");
                return;
            }
            if (!dob.equals(storedDob)) {
                s2Err.setText("Date of Birth does not match our records.");
                return;
            }
            s2Err.setText("");
            steps.show(stepsPanel, "step3");
            dlg.setTitle("Reset Password — New Password");
        });

        JButton s2Back = backButton();
        s2Back.addActionListener(e -> { steps.show(stepsPanel, "step1"); dlg.setTitle("Reset Password"); });

        step2.add(s2Title);
        step2.add(Box.createVerticalStrut(4));
        step2.add(s2Sub);
        step2.add(Box.createVerticalStrut(20));
        step2.add(new JLabel("Date of Birth") {{ setFont(AppTheme.FONT_BOLD); setForeground(AppTheme.TEXT_PRIMARY); setAlignmentX(LEFT_ALIGNMENT); }});
        step2.add(Box.createVerticalStrut(4));
        step2.add(dobInput);
        step2.add(Box.createVerticalStrut(6));
        step2.add(s2Err);
        step2.add(Box.createVerticalStrut(16));
        step2.add(s2Next);
        step2.add(Box.createVerticalStrut(8));
        step2.add(s2Back);

        // ── Step 3: New Password ──────────────────────────────
        JPanel step3 = stepPanel();
        JLabel s3Title = stepTitle("✅  New Password");
        JLabel s3Sub   = stepSubtitle("Choose a strong new password.");
        JPasswordField newPassField    = AppTheme.styledPasswordField("New Password");
        JPasswordField confirmPassField= AppTheme.styledPasswordField("Confirm Password");
        newPassField    .setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        confirmPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel s3Err = errorLabel();

        JButton s3Save = AppTheme.primaryButton("Reset Password");
        s3Save.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        s3Save.addActionListener(e -> {
            String np  = new String(newPassField.getPassword()).trim();
            String cnf = new String(confirmPassField.getPassword()).trim();
            if (np.length() < 6) { s3Err.setText("Password must be at least 6 characters."); return; }
            if (!np.equals(cnf)) { s3Err.setText("Passwords do not match."); return; }
            boolean ok = uDao.resetPassword(verifiedEmail[0], np);
            if (ok) {
                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                    "<html>Password reset successfully!<br>You can now login with your new password.</html>",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                // Pre-fill email in login form
                userEmailField.setText(verifiedEmail[0]);
                userPassField.setText("");
                userPassField.requestFocusInWindow();
            } else {
                s3Err.setText("Reset failed. Please try again.");
            }
        });

        JButton s3Back = backButton();
        s3Back.addActionListener(e -> { steps.show(stepsPanel, "step2"); dlg.setTitle("Reset Password — Verify Identity"); });

        step3.add(s3Title);
        step3.add(Box.createVerticalStrut(4));
        step3.add(s3Sub);
        step3.add(Box.createVerticalStrut(16));
        step3.add(new JLabel("New Password") {{ setFont(AppTheme.FONT_BOLD); setForeground(AppTheme.TEXT_PRIMARY); setAlignmentX(LEFT_ALIGNMENT); }});
        step3.add(Box.createVerticalStrut(4));
        step3.add(newPassField);
        step3.add(Box.createVerticalStrut(12));
        step3.add(new JLabel("Confirm Password") {{ setFont(AppTheme.FONT_BOLD); setForeground(AppTheme.TEXT_PRIMARY); setAlignmentX(LEFT_ALIGNMENT); }});
        step3.add(Box.createVerticalStrut(4));
        step3.add(confirmPassField);
        step3.add(Box.createVerticalStrut(6));
        step3.add(s3Err);
        step3.add(Box.createVerticalStrut(16));
        step3.add(s3Save);
        step3.add(Box.createVerticalStrut(8));
        step3.add(s3Back);

        stepsPanel.add(step1, "step1");
        stepsPanel.add(step2, "step2");
        stepsPanel.add(step3, "step3");
        steps.show(stepsPanel, "step1");

        dlg.add(stepsPanel, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    // ─── Forgot password dialog helpers ──────────────────────
    private JPanel stepPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        return p;
    }
    private JLabel stepTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(AppTheme.PRIMARY_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
    private JLabel stepSubtitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppTheme.FONT_SMALL);
        l.setForeground(AppTheme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
    private JLabel errorLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(AppTheme.FONT_SMALL);
        l.setForeground(AppTheme.DANGER);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
    private JButton backButton() {
        JButton b = new JButton("← Back");
        b.setFont(AppTheme.FONT_SMALL);
        b.setForeground(AppTheme.TEXT_SECONDARY);
        b.setBackground(Color.WHITE);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }

    private void handleUserLogin() {
        String email = userEmailField.getText().trim();
        String pass  = new String(userPassField.getPassword());
        if (email.isEmpty() || pass.isEmpty()) {
            showError("Please enter email and password.");
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        dao.UserDAO dao = new dao.UserDAO();
        model.User u = dao.login(email, pass);
        setCursor(Cursor.getDefaultCursor());
        if (u != null) {
            dispose();
            new UserDashboard(u.getFullName(), u.getEmail(), u.getId()).setVisible(true);
        } else {
            showError("Invalid email or password. Please try again.");
            userPassField.setText("");
        }
    }

    private void handleAdminLogin() {
        String user = adminEmailField.getText().trim();
        String pass = new String(adminPassField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            showError("Please enter admin credentials.");
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        dao.UserDAO dao = new dao.UserDAO();
        String adminName = dao.adminLogin(user, pass);
        setCursor(Cursor.getDefaultCursor());
        if (adminName != null) {
            dispose();
            new AdminDashboard().setVisible(true);
        } else {
            showError("Invalid admin credentials.");
            adminPassField.setText("");
        }
    }

    private void openRegister() {
        new RegisterScreen().setVisible(true);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    static class RoundBorder extends AbstractBorder {
        private final Color color; private final int radius;
        RoundBorder(Color c, int r) { color=c; radius=r; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.drawRoundRect(x,y,w-1,h-1,radius,radius); g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c){return new Insets(2,2,2,2);}
    }
}