package ui;

import util.AppTheme;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class UserDashboard extends JFrame {

    private String userName;
    private final String userEmail;
    private final int userId;
    private JPanel contentPanel;
    private CardLayout contentLayout;
    private JButton activeNavBtn;
    private java.util.List<String> allCities = new java.util.ArrayList<>();

    public UserDashboard(String userName, String userEmail, int userId) {
        super("BusGo Express — Passenger Dashboard");
        this.userName = userName;
        this.userEmail = userEmail;
        this.userId = userId;
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
        // Load cities once at startup for autocomplete
        try { allCities = new dao.ScheduleDAO().getAllCities(); } catch (Exception ignored) {}
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_LIGHT);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildHeader(), BorderLayout.NORTH);

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(AppTheme.BG_LIGHT);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        contentPanel.add(buildSearchPanel(), "search");
        contentPanel.add(buildBookingHistoryPanel(), "history");
        JPanel profilePanel = buildProfilePanel();
        profilePanel.setName("profile");
        contentPanel.add(profilePanel, "profile");

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ─── Sidebar ───────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(AppTheme.SIDEBAR_BG);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Logo area
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        logo.setOpaque(false);
        logo.setPreferredSize(new Dimension(220, 70));
        logo.setMaximumSize(new Dimension(220, 70));
        JLabel logoIcon = new JLabel("🚌");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        JLabel logoText = new JLabel("<html><b style='color:white;font-size:14px'>BusGo</b><br><span style='color:#8899cc;font-size:10px'>Express</span></html>");
        logo.add(logoIcon);
        logo.add(logoText);
        sidebar.add(logo);

        // Nav separator
        sidebar.add(navSeparator("PASSENGER MENU"));

        // Nav buttons
        JButton[] navBtns = {
            navButton("🏠", "Book Tickets", "search"),
            navButton("🎫", "My Bookings", "history"),
            navButton("👤", "My Profile", "profile"),
        };

        // Wire "My Bookings" to always rebuild the panel fresh on click
        navBtns[1].addActionListener(e -> refreshHistoryPanel());

        activeNavBtn = navBtns[0];
        setNavActive(navBtns[0]);

        for (JButton btn : navBtns) sidebar.add(btn);

        sidebar.add(Box.createVerticalGlue());

        // Logout
        JButton logoutBtn = navButton("🚪", "Logout", "logout");
        logoutBtn.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,"Are you sure you want to logout?","Logout",JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) { dispose(); new LoginScreen().setVisible(true); }
        });
        sidebar.add(navSeparator(""));
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(16));

        return sidebar;
    }

    private JButton navButton(String icon, String label, String card) {
        JButton btn = new JButton(icon + "  " + label) {
            boolean active = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(AppTheme.SIDEBAR_ACTIVE);
                    g2.fillRoundRect(8,2,getWidth()-16,getHeight()-4,10,10);
                    // left indicator
                    g2.setColor(AppTheme.ACCENT);
                    g2.fillRoundRect(0,8,4,getHeight()-16,4,4);
                } else if (getModel().isRollover()) {
                    g2.setColor(AppTheme.SIDEBAR_HOVER);
                    g2.fillRoundRect(8,2,getWidth()-16,getHeight()-4,10,10);
                }
                super.paintComponent(g);
                g2.dispose();
            }
            public void setActive(boolean a) { active=a; repaint(); }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(180, 200, 240));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setMaximumSize(new Dimension(220, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (!card.equals("logout")) {
                setNavActive(btn);
                if (card.equals("profile")) {
                    // Safely find and remove old profile panel, then rebuild fresh from DB
                    java.awt.Component toRemove = null;
                    for (java.awt.Component comp : contentPanel.getComponents()) {
                        if (comp instanceof JPanel && "profile".equals(comp.getName())) {
                            toRemove = comp;
                            break;
                        }
                    }
                    if (toRemove != null) contentPanel.remove(toRemove);
                    JPanel freshProfile = buildProfilePanel();
                    freshProfile.setName("profile");
                    contentPanel.add(freshProfile, "profile");
                    contentPanel.revalidate();
                    contentPanel.repaint();
                }
                contentLayout.show(contentPanel, card);
            }
        });
        return btn;
    }

    private void setNavActive(JButton btn) {
        if (activeNavBtn != null) {
            activeNavBtn.setForeground(new Color(180,200,240));
            ((JButton)activeNavBtn).putClientProperty("active", false);
            activeNavBtn.repaint();
        }
        activeNavBtn = btn;
        btn.setForeground(Color.WHITE);
        btn.putClientProperty("active", true);
        btn.repaint();
    }

    private JPanel navSeparator(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(220, 32));
        p.setBorder(BorderFactory.createEmptyBorder(6,16,2,16));
        if (!text.isEmpty()) {
            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("Segoe UI",Font.BOLD,10));
            lbl.setForeground(new Color(80,100,150));
            p.add(lbl, BorderLayout.CENTER);
        }
        return p;
    }

    // ─── Header ───────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,AppTheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(0,24,0,24)
        ));
        header.setPreferredSize(new Dimension(0, 64));

        JLabel pageName = new JLabel("Welcome, " + userName + " 👋");
        pageName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pageName.setForeground(AppTheme.TEXT_PRIMARY);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        JLabel avatarLabel = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY);
                g2.fillOval(0,0,getWidth(),getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI",Font.BOLD,16));
                String init = userName.substring(0,1).toUpperCase();
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(init,(getWidth()-fm.stringWidth(init))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        avatarLabel.setPreferredSize(new Dimension(38,38));

        JLabel nameTag = new JLabel("<html><b style='color:#141e3c'>" + userName + "</b><br><small style='color:#6478a0'>" + userEmail + "</small></html>");
        nameTag.setFont(AppTheme.FONT_BODY);

        rightPanel.add(nameTag);
        rightPanel.add(avatarLabel);

        header.add(pageName, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    // ─── Search / Book Tickets ─────────────────────────────────
    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // Search card
        JPanel searchCard = AppTheme.card(16);
        searchCard.setLayout(new BorderLayout(0, 14));
        searchCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("🔍  Search Buses");
        title.setFont(AppTheme.FONT_HEADING);
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 6, 0, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Autocomplete fields
        JTextField fromField = AppTheme.styledField("From — Source City");
        JTextField toField   = AppTheme.styledField("To — Destination City");
        JTextField dateField = AppTheme.styledField("Journey Date (DD/MM/YYYY)");
        JComboBox<String> timeCombo = AppTheme.styledCombo(new String[]{"Any Time", "Morning (AM)", "Evening (PM)"});
        JButton searchBtn = AppTheme.primaryButton("  🔍  Search  ");

        fromField.setPreferredSize(new Dimension(0, 40));
        toField  .setPreferredSize(new Dimension(0, 40));
        dateField.setPreferredSize(new Dimension(0, 40));
        timeCombo.setPreferredSize(new Dimension(0, 40));

        gbc.gridx = 0; gbc.weightx = 1.2; fields.add(wrapWithDropdown(fromField, allCities, panel), gbc);
        gbc.gridx = 1; gbc.weightx = 1.2; fields.add(wrapWithDropdown(toField,   allCities, panel), gbc);
        gbc.gridx = 2; gbc.weightx = 0.9; fields.add(dateField, gbc);
        gbc.gridx = 3; gbc.weightx = 0.7; fields.add(timeCombo, gbc);
        gbc.gridx = 4; gbc.weightx = 0;   fields.add(searchBtn, gbc);

        searchCard.add(title, BorderLayout.NORTH);
        searchCard.add(fields, BorderLayout.CENTER);

        // Results area
        JPanel resultsArea = new JPanel(new BorderLayout(0, 12));
        resultsArea.setOpaque(false);

        JLabel resultsTitle = new JLabel("Available Buses");
        resultsTitle.setFont(AppTheme.FONT_SUBHEAD);
        resultsTitle.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel busCards = new JPanel();
        busCards.setLayout(new BoxLayout(busCards, BoxLayout.Y_AXIS));
        busCards.setOpaque(false);

        // Show all active schedules on load
        loadAllSchedules(busCards);

        JScrollPane scroll = new JScrollPane(busCards);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        resultsArea.add(resultsTitle, BorderLayout.NORTH);
        resultsArea.add(scroll, BorderLayout.CENTER);

        // Search action
        searchBtn.addActionListener(e -> {
            String from = fromField.getText().trim();
            String to   = toField  .getText().trim();
            String date = dateField.getText().trim();
            String time = (String) timeCombo.getSelectedItem();

            if (from.isEmpty() || from.equals("From — Source City") ||
                to  .isEmpty() || to  .equals("To — Destination City") ||
                date.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please enter source city, destination and journey date.",
                    "Missing Fields", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                java.sql.Date sqlDate = new java.sql.Date(sdf.parse(date).getTime());
                String timePref = time.startsWith("Morning") ? "AM"
                                : time.startsWith("Evening") ? "PM" : "";

                java.util.List<model.Schedule> results =
                    new dao.ScheduleDAO().searchSchedules(from, to, sqlDate, timePref);

                busCards.removeAll();
                if (results.isEmpty()) {
                    JLabel none = new JLabel(
                        "No buses found for " + from + " → " + to + " on " + date,
                        SwingConstants.CENTER);
                    none.setFont(AppTheme.FONT_BODY);
                    none.setForeground(AppTheme.TEXT_SECONDARY);
                    none.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
                    busCards.add(none);
                } else {
                    for (model.Schedule sch : results) {
                        String[] bd = {
                            String.valueOf(sch.getId()),
                            sch.getBusName(),
                            sch.getSource() + " → " + sch.getDestination(),
                            sch.getDepartureTime(),
                            sch.getArrivalTime(),
                            sch.getRunDays() != null ? sch.getRunDays() : "",
                            sch.getPriceFormatted(),
                            String.valueOf(sch.getAvailableSeats())
                        };
                        busCards.add(buildBusCard(bd));
                        busCards.add(Box.createVerticalStrut(10));
                    }
                }
                busCards.revalidate();
                busCards.repaint();
            } catch (java.text.ParseException ex) {
                JOptionPane.showMessageDialog(this,
                    "Invalid date format. Use DD/MM/YYYY (e.g. 15/03/2026).",
                    "Date Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(searchCard,  BorderLayout.NORTH);
        panel.add(resultsArea, BorderLayout.CENTER);
        return panel;
    }

    /** Loads all active schedules from DB and shows them as bus cards */
    private void loadAllSchedules(JPanel busCards) {
        busCards.removeAll();
        java.util.List<model.Schedule> all = new dao.ScheduleDAO().getAllSchedules();
        if (all.isEmpty()) {
            JLabel none = new JLabel("No buses available yet.", SwingConstants.CENTER);
            none.setFont(AppTheme.FONT_BODY);
            none.setForeground(AppTheme.TEXT_SECONDARY);
            none.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
            busCards.add(none);
        } else {
            for (model.Schedule sch : all) {
                String[] bd = {
                    String.valueOf(sch.getId()),
                    sch.getBusName(),
                    sch.getSource() + " → " + sch.getDestination(),
                    sch.getDepartureTime(),
                    sch.getArrivalTime(),
                    sch.getRunDays() != null ? sch.getRunDays() : "",
                    sch.getPriceFormatted(),
                    String.valueOf(sch.getAvailableSeats())
                };
                busCards.add(buildBusCard(bd));
                busCards.add(Box.createVerticalStrut(10));
            }
        }
        busCards.revalidate();
        busCards.repaint();
    }

    /**
     * Wraps a JTextField with a live autocomplete popup.
     * Queries the DB on every keystroke so newly added cities appear immediately.
     */
    private JPanel wrapWithDropdown(JTextField field, java.util.List<String> ignored, JPanel rootPanel) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(field, BorderLayout.CENTER);

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR));
        popup.setBackground(Color.WHITE);
        popup.setFocusable(false); // keep focus on text field

        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { SwingUtilities.invokeLater(this::suggest); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { SwingUtilities.invokeLater(this::suggest); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { SwingUtilities.invokeLater(this::suggest); }

            void suggest() {
                String text = field.getText().trim();
                popup.setVisible(false);
                popup.removeAll();
                if (text.length() < 1) return;

                java.util.List<String> matches = new dao.RouteDAO().searchCities(text);
                if (matches.isEmpty()) return;

                for (String city : matches) {
                    JMenuItem item = new JMenuItem(city);
                    item.setFont(AppTheme.FONT_BODY);
                    item.setBackground(Color.WHITE);
                    item.setForeground(AppTheme.TEXT_PRIMARY);
                    item.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                    item.setFocusable(false);
                    item.addActionListener(ev -> {
                        field.setText(city);
                        popup.setVisible(false);
                    });
                    popup.add(item);
                }

                // Show below the field using field's position — works reliably in nested layouts
                popup.show(field, 0, field.getHeight());
            }
        });

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                // Delay hiding so a click on a menu item registers first
                javax.swing.Timer t = new javax.swing.Timer(200, ev -> popup.setVisible(false));
                t.setRepeats(false);
                t.start();
            }
        });

        return wrapper;
    }

    private JPanel buildBusCard(String[] data) {
        // data: [busNo, name, route, dep, arr, dur, price, seats]
        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                g2.setColor(AppTheme.BORDER_COLOR);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14,18,14,18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Left: bus info
        JPanel left = new JPanel(new GridLayout(2,1,0,4));
        left.setOpaque(false);
        JLabel name = new JLabel("<html>🚌&nbsp;&nbsp;" + data[1] + "&nbsp;&nbsp;&nbsp;<span style='color:#6478a0;font-size:11px'>" + data[0] + "</span></html>");
        name.setFont(AppTheme.FONT_SUBHEAD);
        name.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel route = new JLabel(data[2]);
        route.setFont(AppTheme.FONT_BODY);
        route.setForeground(AppTheme.TEXT_SECONDARY);
        left.add(name); left.add(route);

        // Center: timings
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        center.setOpaque(false);
        center.add(timeBlock(data[3], "Departure"));
        center.add(arrowLine(data[4]));
        center.add(timeBlock(data[4], "Arrival"));

        // Right: price + book
        JPanel right = new JPanel(new BorderLayout(0,6));
        right.setOpaque(false);
        JLabel price = new JLabel(data[6]);
        price.setFont(new Font("Segoe UI",Font.BOLD,20));
        price.setForeground(AppTheme.SUCCESS);
        price.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel seats = new JLabel(data[7] + " seats left");
        seats.setFont(AppTheme.FONT_SMALL);
        seats.setForeground(Integer.parseInt(data[7]) < 10 ? AppTheme.DANGER : AppTheme.TEXT_SECONDARY);
        seats.setHorizontalAlignment(SwingConstants.CENTER);
        JButton bookBtn = AppTheme.accentButton("Book Now");
        bookBtn.setPreferredSize(new Dimension(110, 36));
        bookBtn.addActionListener(e -> openBookingPanel(data));

        right.add(price, BorderLayout.NORTH);
        right.add(seats, BorderLayout.CENTER);
        right.add(bookBtn, BorderLayout.SOUTH);

        card.add(left, BorderLayout.WEST);
        card.add(center, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    new AppTheme.RoundBorder(AppTheme.PRIMARY_LIGHT, 14),
                    BorderFactory.createEmptyBorder(13,17,13,17)));
                card.repaint();
            }
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createEmptyBorder(14,18,14,18));
                card.repaint();
            }
        });

        return card;
    }

    private JPanel timeBlock(String time, String label) {
        JPanel p = new JPanel(new GridLayout(2,1,0,2));
        p.setOpaque(false);
        JLabel t = new JLabel(time, SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI",Font.BOLD,16));
        t.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setFont(AppTheme.FONT_SMALL);
        l.setForeground(AppTheme.TEXT_SECONDARY);
        p.add(t); p.add(l);
        return p;
    }

    private JPanel arrowLine(String duration) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(AppTheme.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                int mid = getHeight()/2 - 8;
                g2.drawLine(0,mid,getWidth(),mid);
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillPolygon(new int[]{getWidth()-8,getWidth(),getWidth()-8},new int[]{mid-5,mid,mid+5},3);
                g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
                g2.setColor(AppTheme.TEXT_LIGHT);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(duration,(getWidth()-fm.stringWidth(duration))/2,mid+18);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(100,50));
        return p;
    }

    private void openBookingPanel(String[] busData) {
        new BookingWizard(this, busData, userName, userId).setVisible(true);
    }

    // ─── Refresh history panel live ───────────────────────────
    private void refreshHistoryPanel() {
        // History panel is always at index 1 in contentPanel (search=0, history=1, profile=2)
        contentPanel.remove(1);
        contentPanel.add(buildBookingHistoryPanel(), "history", 1);
        contentLayout.show(contentPanel, "history");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // ─── Booking History Panel ─────────────────────────────────
    private JPanel buildBookingHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0,16));
        panel.setOpaque(false);

        JLabel title = new JLabel("My Bookings");
        title.setFont(AppTheme.FONT_HEADING);
        title.setForeground(AppTheme.TEXT_PRIMARY);

        // Load bookings from DB
        dao.BookingDAO bookingDAO = new dao.BookingDAO();
        java.util.List<model.Booking> bookings = bookingDAO.getBookingsByUser(userId);

        long confirmed = bookings.stream().filter(b->"Confirmed".equals(b.getStatus())).count();
        long cancelled = bookings.stream().filter(b->"Cancelled".equals(b.getStatus())).count();

        // Stats row
        JPanel stats = new JPanel(new GridLayout(1,3,16,0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        stats.add(statCard("Total Trips", String.valueOf(bookings.size()), AppTheme.PRIMARY));
        stats.add(statCard("Upcoming",    String.valueOf(confirmed),        AppTheme.SUCCESS));
        stats.add(statCard("Cancelled",   String.valueOf(cancelled),        AppTheme.DANGER));

        // Build table rows from DB
        String[] cols = {"Booking ID","Bus","Route","Date","Passengers","Amount","Status","Action"};
        Object[][] rows = bookings.stream().map(b -> new Object[]{
            b.getBookingRef(),
            b.getBusName(),
            b.getSource() + " → " + b.getDestination(),
            b.getJourneyDate() != null ? b.getJourneyDate().toString() : "",
            b.getNumPassengers(),
            b.getTotalFormatted(),
            b.getStatus(),
            ""
        }).toArray(Object[][]::new);
        if (rows.length == 0) {
            rows = new Object[][]{{"—","No bookings yet","—","—","—","—","—",""}};
        }

        DefaultTableModel model = new DefaultTableModel(rows, cols) {
            public boolean isCellEditable(int r, int c) { return c == 7; }
        };
        JTable table = new JTable(model);
        table.setFont(AppTheme.FONT_BODY);
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,0));
        table.setBackground(Color.WHITE);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.getTableHeader().setFont(AppTheme.FONT_BOLD);
        table.getTableHeader().setBackground(AppTheme.BG_LIGHT);
        table.getTableHeader().setForeground(AppTheme.TEXT_SECONDARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0,AppTheme.BORDER_COLOR));

        // Status column coloring
        table.getColumnModel().getColumn(6).setCellRenderer((t, val, sel, foc, row, col) -> {
            JLabel lbl = new JLabel(val.toString(), SwingConstants.CENTER);
            lbl.setFont(AppTheme.FONT_BOLD);
            lbl.setOpaque(true);
            String v = val.toString();
            if (v.equals("Confirmed")) { lbl.setBackground(new Color(232,245,233)); lbl.setForeground(AppTheme.SUCCESS); }
            else if (v.equals("Cancelled")) { lbl.setBackground(new Color(255,235,238)); lbl.setForeground(AppTheme.DANGER); }
            else { lbl.setBackground(new Color(232,240,254)); lbl.setForeground(AppTheme.PRIMARY); }
            lbl.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
            return lbl;
        });

        // Action column
        // Cancel button renderer
        table.getColumnModel().getColumn(7).setCellRenderer((t, val, sel, foc, row, col) -> {
            String status = table.getValueAt(row, 6).toString();
            if ("Confirmed".equals(status)) {
                JButton btn = AppTheme.dangerButton("Cancel");
                btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
                return btn;
            } else {
                JLabel lbl = new JLabel("—", SwingConstants.CENTER);
                lbl.setForeground(AppTheme.TEXT_LIGHT);
                return lbl;
            }
        });

        // Cancel button editor — fires actual cancel logic
        table.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private int clickedRow;

            @Override
            public boolean isCellEditable(java.util.EventObject e) {
                if (e instanceof MouseEvent) {
                    clickedRow = table.rowAtPoint(((MouseEvent) e).getPoint());
                    String status = table.getValueAt(clickedRow, 6).toString();
                    return "Confirmed".equals(status);
                }
                return false;
            }

            @Override
            public Object getCellEditorValue() { return ""; }

            @Override
            public java.awt.Component getTableCellEditorComponent(
                    JTable t, Object value, boolean isSelected, int row, int col) {
                clickedRow = row;
                String bookingRef = table.getValueAt(row, 0).toString();
                String route      = table.getValueAt(row, 2).toString();

                // Confirm dialog
                int confirm = JOptionPane.showConfirmDialog(
                    UserDashboard.this,
                    "<html>Cancel booking <b>" + bookingRef + "</b>?<br>"
                    + "Route: " + route + "<br><br>"
                    + "<span style='color:red'>Refund: 80% of total amount</span></html>",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    // Find booking id from DB by booking_ref
                    dao.BookingDAO bkDao = new dao.BookingDAO();
                    java.util.List<model.Booking> userBookings = bkDao.getBookingsByUser(userId);
                    int bookingId = -1;
                    for (model.Booking b : userBookings) {
                        if (bookingRef.equals(b.getBookingRef())) {
                            bookingId = b.getId();
                            break;
                        }
                    }
                    if (bookingId != -1) {
                        boolean ok = bkDao.cancelBooking(bookingId, "Cancelled by passenger");
                        if (ok) {
                            JOptionPane.showMessageDialog(
                                UserDashboard.this,
                                "<html>Booking <b>" + bookingRef + "</b> cancelled successfully!<br>"
                                + "Refund of 80% will be processed within 5-7 business days.</html>",
                                "Cancelled", JOptionPane.INFORMATION_MESSAGE
                            );
                            refreshHistoryPanel(); // ← live refresh
                        } else {
                            JOptionPane.showMessageDialog(
                                UserDashboard.this,
                                "Cancellation failed. Please try again.",
                                "Error", JOptionPane.ERROR_MESSAGE
                            );
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                            UserDashboard.this,
                            "Could not find booking record. Please refresh.",
                            "Error", JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
                fireEditingStopped();
                return new JLabel("");
            }
        });

        // Row striping
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                c.setBackground(sel ? new Color(232,240,254) : (row%2==0 ? Color.WHITE : new Color(248,250,254)));
                ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new AppTheme.RoundBorder(AppTheme.BORDER_COLOR, 12));
        scroll.setBackground(Color.WHITE);
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        top.add(stats, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel statCard(String label, String value, Color color) {
        JPanel p = AppTheme.card(12);
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12,16,12,16));
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI",Font.BOLD,28));
        val.setForeground(color);
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_BODY);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        p.add(val, BorderLayout.CENTER);
        p.add(lbl, BorderLayout.SOUTH);
        return p;
    }

    // ─── Profile Panel ─────────────────────────────────────────
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setOpaque(false);

        // Load current user data from DB
        dao.UserDAO uDao = new dao.UserDAO();
        model.User currentUser = uDao.getById(userId);
        String dbName   = currentUser != null && currentUser.getFullName()         != null ? currentUser.getFullName()         : userName;
        String dbEmail  = currentUser != null && currentUser.getEmail()             != null ? currentUser.getEmail()             : userEmail;
        String dbPhone  = currentUser != null && currentUser.getPhone()             != null ? currentUser.getPhone()             : "";
        String dbDob    = currentUser != null && currentUser.getDob()               != null ? currentUser.getDob()               : "";
        String dbGender = currentUser != null && currentUser.getGender()            != null ? currentUser.getGender()            : "";
        String dbEmerg  = currentUser != null && currentUser.getEmergencyContact()  != null ? currentUser.getEmergencyContact()  : "";

        // ── Avatar card ───────────────────────────────────────
        JPanel left = AppTheme.card(16);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        left.setPreferredSize(new Dimension(220, 0));

        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
                String init = dbName.substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(init, (getWidth() - fm.stringWidth(init)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(90, 90));
        avatar.setMaximumSize(new Dimension(90, 90));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameL  = new JLabel(dbName);
        nameL.setFont(AppTheme.FONT_SUBHEAD);
        nameL.setForeground(AppTheme.TEXT_PRIMARY);
        nameL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emailL = new JLabel(dbEmail);
        emailL.setFont(AppTheme.FONT_SMALL);
        emailL.setForeground(AppTheme.TEXT_SECONDARY);
        emailL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        badge.setBackground(new Color(232, 240, 254));
        badge.setBorder(new AppTheme.RoundBorder(AppTheme.PRIMARY_LIGHT, 10));
        badge.setMaximumSize(new Dimension(150, 28));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel badgeLbl = new JLabel("⭐  Premium Member");
        badgeLbl.setFont(AppTheme.FONT_SMALL);
        badgeLbl.setForeground(AppTheme.PRIMARY);
        badge.add(badgeLbl);

        left.add(avatar);
        left.add(Box.createVerticalStrut(12));
        left.add(nameL);
        left.add(Box.createVerticalStrut(4));
        left.add(emailL);
        left.add(Box.createVerticalStrut(12));
        left.add(badge);

        // ── Profile form ──────────────────────────────────────
        JPanel right = AppTheme.card(16);
        right.setLayout(new BorderLayout());
        right.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel formTitle = new JLabel("Edit Profile");
        formTitle.setFont(AppTheme.FONT_HEADING);
        formTitle.setForeground(AppTheme.TEXT_PRIMARY);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 6, 6, 6);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // Convert stored YYYY-MM-DD → DD/MM/YYYY for display
        String dobDisplay = dbDob;
        if (dbDob.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] p = dbDob.split("-");
            dobDisplay = p[2] + "/" + p[1] + "/" + p[0];
        }

        // Fields wired to actual variables
        JTextField nameField  = AppTheme.styledField("Full Name");        nameField .setText(dbName);
        JTextField emailField = AppTheme.styledField("Email");             emailField.setText(dbEmail); emailField.setEnabled(false);
        JTextField phoneField = AppTheme.styledField("Phone");             phoneField.setText(dbPhone);
        JTextField dobField   = AppTheme.styledField("e.g. 10/06/2006");  dobField  .setText(dobDisplay);
        JTextField genderField= AppTheme.styledField("Male / Female / Other"); genderField.setText(dbGender);
        JTextField emergField = AppTheme.styledField("Emergency Contact"); emergField.setText(dbEmerg);

        String[][] rows = {
            {"Full Name", ""}, {"Email", ""}, {"Phone", ""},
            {"Date of Birth  (DD/MM/YYYY)", ""}, {"Gender", ""}, {"Emergency Contact", ""}
        };
        JTextField[] fields = {nameField, emailField, phoneField, dobField, genderField, emergField};

        for (int i = 0; i < rows.length; i++) {
            gbc.gridx = 0; gbc.gridy = i * 2;
            JLabel lbl = new JLabel(rows[i][0]);
            lbl.setFont(AppTheme.FONT_BOLD);
            lbl.setForeground(AppTheme.TEXT_PRIMARY);
            form.add(lbl, gbc);
            gbc.gridx = 1;
            form.add(fields[i], gbc);
            // Add format hint below DOB field
            if (i == 3) {
                gbc.gridx = 1; gbc.gridy = i * 2 + 1;
                JLabel hint = new JLabel("Format: DD/MM/YYYY  e.g. 10/06/2006");
                hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                hint.setForeground(new Color(120, 140, 180));
                form.add(hint, gbc);
            }
        }

        JButton saveBtn = AppTheme.primaryButton("Save Changes");
        saveBtn.addActionListener(e -> {
            String newName   = nameField  .getText().trim();
            String newPhone  = phoneField .getText().trim();
            String newDob    = dobField   .getText().trim();
            String newGender = genderField.getText().trim();
            String newEmerg  = emergField .getText().trim();

            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (newPhone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Phone cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Convert dob from DD/MM/YYYY → YYYY-MM-DD for MySQL DATE column
            if (!newDob.isEmpty() && newDob.matches("\\d{2}/\\d{2}/\\d{4}")) {
                String[] parts = newDob.split("/");
                newDob = parts[2] + "-" + parts[1] + "-" + parts[0]; // YYYY-MM-DD
            } else if (!newDob.isEmpty() && !newDob.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Date of Birth must be DD/MM/YYYY (e.g. 10/06/2006).", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Normalise gender to match ENUM('Male','Female','Other')
            if (!newGender.isEmpty()) {
                String g = newGender.substring(0,1).toUpperCase() + newGender.substring(1).toLowerCase();
                if (!g.equals("Male") && !g.equals("Female") && !g.equals("Other")) g = "Male";
                newGender = g;
            } else {
                newGender = "Male"; // default to satisfy NOT NULL enum
            }

            model.User upd = new model.User();
            upd.setId              (userId);
            upd.setFullName        (newName);
            upd.setEmail           (dbEmail);
            upd.setPhone           (newPhone);
            upd.setDob             (newDob.isEmpty() ? null : newDob);
            upd.setGender          (newGender);
            upd.setEmergencyContact(newEmerg.isEmpty() ? null : newEmerg);

            boolean ok = uDao.updateProfile(upd);
            if (ok) {
                userName = newName;
                nameL.setText(newName);
                JOptionPane.showMessageDialog(this,
                    "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Update failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setOpaque(false);
        btnRow.add(saveBtn);

        right.add(formTitle, BorderLayout.NORTH);
        right.add(form, BorderLayout.CENTER);
        right.add(btnRow, BorderLayout.SOUTH);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.CENTER);
        return panel;
    }
}