package ui;

import util.AppTheme;
import javax.swing.*;
import java.awt.*;

public class RegisterScreen extends JFrame {

    private JTextField nameField, emailField, phoneField;
    private JPasswordField passField, confirmPassField;
    private JComboBox<String> genderCombo;

    public RegisterScreen() {
        super("BusGo Express — Create Account");
        setSize(500, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header bar
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,AppTheme.PRIMARY_DARK,getWidth(),0,AppTheme.PRIMARY);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0,70));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(0,24,0,24));

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Join BusGo Express today");
        sub.setFont(AppTheme.FONT_LABEL);
        sub.setForeground(new Color(255,255,255,180));

        JPanel hText = new JPanel(new GridLayout(2,1));
        hText.setOpaque(false);
        hText.add(title); hText.add(sub);
        header.add(hText, BorderLayout.CENTER);

        // Form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(24,32,24,32));

        nameField = AppTheme.styledField("Full Name");
        emailField = AppTheme.styledField("Email Address");
        phoneField = AppTheme.styledField("Phone Number");
        passField = AppTheme.styledPasswordField("Password (min 6 chars)");
        confirmPassField = AppTheme.styledPasswordField("Confirm Password");
        genderCombo = AppTheme.styledCombo(new String[]{"Select Gender", "Male", "Female", "Other"});

        addRow(form, "Full Name", nameField);
        addRow(form, "Email Address", emailField);
        addRow(form, "Phone Number", phoneField);
        addRow(form, "Gender", genderCombo);
        addRow(form, "Password", passField);
        addRow(form, "Confirm Password", confirmPassField);

        form.add(Box.createVerticalStrut(20));

        JButton registerBtn = AppTheme.primaryButton("Create Account");
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.addActionListener(e -> handleRegister());
        form.add(registerBtn);

        form.add(Box.createVerticalStrut(12));

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        backRow.setBackground(Color.WHITE);
        backRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        backRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel backLbl = new JLabel("Already have an account?");
        backLbl.setFont(AppTheme.FONT_SMALL);
        backLbl.setForeground(AppTheme.TEXT_SECONDARY);
        JLabel backLink = new JLabel("Login");
        backLink.setFont(AppTheme.FONT_BOLD);
        backLink.setForeground(AppTheme.PRIMARY);
        backLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { dispose(); }
        });
        backRow.add(backLbl);
        backRow.add(backLink);
        form.add(backRow);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        root.add(header, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void addRow(JPanel p, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_BOLD);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(field);
        p.add(Box.createVerticalStrut(12));
    }

    private void handleRegister() {
        String name    = nameField.getText().trim();
        String email   = emailField.getText().trim();
        String phone   = phoneField.getText().trim();
        String pass    = new String(passField.getPassword());
        String confirm = new String(confirmPassField.getPassword());
        String gender  = (String) genderCombo.getSelectedItem();

        if (name.isEmpty()||email.isEmpty()||phone.isEmpty()||pass.isEmpty()) {
            JOptionPane.showMessageDialog(this,"All fields are required.","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ("Select Gender".equals(gender)) {
            JOptionPane.showMessageDialog(this,"Please select your gender.","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this,"Passwords do not match.","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (pass.length() < 6) {
            JOptionPane.showMessageDialog(this,"Password must be at least 6 characters.","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Check email uniqueness
        dao.UserDAO dao = new dao.UserDAO();
        if (dao.emailExists(email)) {
            JOptionPane.showMessageDialog(this,"An account with this email already exists.","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Save to DB
        model.User u = new model.User();
        u.setFullName(name); u.setEmail(email); u.setPhone(phone);
        u.setPassword(pass); u.setGender(gender);
        int id = dao.register(u);
        if (id > 0) {
            JOptionPane.showMessageDialog(this,"Account created successfully! Please login.","Success",JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,"Registration failed. Please try again.","Error",JOptionPane.ERROR_MESSAGE);
        }
    }
}
