package ui;

import util.AppTheme;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class AdminDashboard extends JFrame {

    private JPanel contentPanel;
    private CardLayout contentLayout;
    private JButton activeNavBtn;

    public AdminDashboard() {
        super("BusGo Express — Admin Dashboard");
        setSize(1200, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 650));
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_LIGHT);

        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(AppTheme.BG_LIGHT);
        mainArea.add(buildHeader(), BorderLayout.NORTH);

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(AppTheme.BG_LIGHT);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        // Index map: analytics=0, buses=1, routes=2, schedule=3, seats=4, bookings=5
        contentPanel.add(buildAnalyticsPanel(),      "analytics");
        contentPanel.add(buildBusManagementPanel(),  "buses");
        contentPanel.add(buildRouteManagementPanel(),"routes");
        contentPanel.add(buildSchedulePanel(),       "schedule");
        contentPanel.add(buildSeatLayoutEditor(),    "seats");
        contentPanel.add(buildBookingsPanel(),       "bookings");

        mainArea.add(contentPanel, BorderLayout.CENTER);
        root.add(mainArea, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ─── Sidebar ───────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(AppTheme.SIDEBAR_BG); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Logo
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        logo.setOpaque(false);
        logo.setPreferredSize(new Dimension(230,70));
        logo.setMaximumSize(new Dimension(230,70));
        JLabel logoIcon = new JLabel("🚌");
        logoIcon.setFont(new Font("Segoe UI Emoji",Font.PLAIN,26));
        JLabel logoText = new JLabel("<html><b style='color:white;font-size:14px'>BusGo Admin</b><br><span style='color:#8899cc;font-size:10px'>Control Panel</span></html>");
        logo.add(logoIcon); logo.add(logoText);
        sidebar.add(logo);

        sidebar.add(sidebarSep("OVERVIEW"));
        JButton analytics   = navBtn("📊","Analytics",  "analytics", 0);
        JButton bookingsBtn = navBtn("🎫","All Bookings","bookings",  5);

        activeNavBtn = analytics;
        setNavActive(analytics);

        sidebar.add(analytics);
        sidebar.add(bookingsBtn);

        sidebar.add(sidebarSep("MANAGEMENT"));
        sidebar.add(navBtn("🚌","Manage Buses",  "buses",    1));
        sidebar.add(navBtn("🗺","Manage Routes", "routes",   2));
        sidebar.add(navBtn("📅","Schedules",     "schedule", 3));
        sidebar.add(navBtn("💺","Seat Layouts",  "seats",    4));

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarSep(""));

        JButton logoutBtn = navBtn("🚪","Logout","logout", -1);
        logoutBtn.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,"Logout from admin panel?","Confirm",JOptionPane.YES_NO_OPTION);
            if (r==JOptionPane.YES_OPTION) { dispose(); new LoginScreen().setVisible(true); }
        });
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(16));
        return sidebar;
    }

    private JButton navBtn(String icon, String label, String card, int panelIndex) {
        JButton btn = new JButton(icon + "  " + label) {
            boolean active=false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(AppTheme.SIDEBAR_ACTIVE);
                    g2.fillRoundRect(8,2,getWidth()-16,getHeight()-4,10,10);
                    g2.setColor(AppTheme.ACCENT);
                    g2.fillRoundRect(0,8,4,getHeight()-16,4,4);
                } else if (getModel().isRollover()) {
                    g2.setColor(AppTheme.SIDEBAR_HOVER);
                    g2.fillRoundRect(8,2,getWidth()-16,getHeight()-4,10,10);
                }
                super.paintComponent(g);
                g2.dispose();
            }
            public void setActive(boolean a){active=a;repaint();}
        };
        btn.setFont(new Font("Segoe UI",Font.PLAIN,13));
        btn.setForeground(new Color(180,200,240));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setMaximumSize(new Dimension(230,46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (!card.equals("logout")) {
                setNavActive(btn);
                refreshPanel(card, panelIndex);
            }
        });
        return btn;
    }

    private void setNavActive(JButton btn) {
        if (activeNavBtn!=null) {
            activeNavBtn.setForeground(new Color(180,200,240));
            try { activeNavBtn.getClass().getMethod("setActive",boolean.class).invoke(activeNavBtn,false); } catch(Exception ignored){}
        }
        activeNavBtn = btn;
        btn.setForeground(Color.WHITE);
        try { btn.getClass().getMethod("setActive",boolean.class).invoke(btn,true); } catch(Exception ignored){}
    }

    /** Rebuilds the panel from DB and swaps it in the CardLayout, then shows it. */
    private void refreshPanel(String card, int index) {
        contentPanel.remove(index);
        JPanel fresh;
        switch (card) {
            case "analytics": fresh = buildAnalyticsPanel();      break;
            case "buses":     fresh = buildBusManagementPanel();  break;
            case "routes":    fresh = buildRouteManagementPanel();break;
            case "schedule":  fresh = buildSchedulePanel();       break;
            case "seats":     fresh = buildSeatLayoutEditor();    break;
            case "bookings":  fresh = buildBookingsPanel();       break;
            default:          fresh = new JPanel();
        }
        contentPanel.add(fresh, card, index);
        contentLayout.show(contentPanel, card);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel sidebarSep(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(230,32));
        p.setBorder(BorderFactory.createEmptyBorder(6,16,2,16));
        if (!text.isEmpty()) {
            JLabel l=new JLabel(text);
            l.setFont(new Font("Segoe UI",Font.BOLD,10));
            l.setForeground(new Color(80,100,150));
            p.add(l,BorderLayout.CENTER);
        }
        return p;
    }

    // ─── Header ───────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Color.WHITE);
        h.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,AppTheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(0,24,0,24)
        ));
        h.setPreferredSize(new Dimension(0,64));

        JLabel title = new JLabel("Admin Control Panel");
        title.setFont(new Font("Segoe UI",Font.BOLD,18));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,12,0));
        right.setOpaque(false);

        // Date & time label
        JLabel time = new JLabel(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm")));
        time.setFont(AppTheme.FONT_BODY);
        time.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel adminBadge = new JLabel("🔧  Administrator");
        adminBadge.setFont(AppTheme.FONT_BOLD);
        adminBadge.setForeground(AppTheme.PRIMARY);

        right.add(time);
        right.add(adminBadge);
        h.add(title, BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    // ─── Analytics Panel ───────────────────────────────────────
    private JPanel buildAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        // Title
        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(AppTheme.FONT_HEADING);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        // ── KPI Row ──────────────────────────────────────────
        dao.BookingDAO bDao = new dao.BookingDAO();
        int[] analytics = bDao.getTodayAnalytics();
        int[] weekly    = bDao.getWeeklyBookings();

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        kpiRow.add(kpiCard("🎫  Today's Bookings", String.valueOf(analytics[0]), "Live from database", AppTheme.PRIMARY));
        kpiRow.add(kpiCard("💰  Revenue Today",    "₹" + String.format("%,d", analytics[1]), "Total collected today", AppTheme.SUCCESS));
        kpiRow.add(kpiCard("🚌  Active Buses",     String.valueOf(analytics[3]), "Currently running", AppTheme.WARNING));
        kpiRow.add(kpiCard("❌  Cancellations",    String.valueOf(analytics[2]), "Today's cancellations", AppTheme.DANGER));

        // ── Middle Row: Bar chart + Recent Bookings ───────────
        JPanel midRow = new JPanel(new GridLayout(1, 2, 14, 0));
        midRow.setOpaque(false);
        midRow.add(buildBarChart(weekly));
        midRow.add(buildRecentBookingsTable());

        // ── Bottom Row: Pie chart + Top Routes ────────────────
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 14, 0));
        bottomRow.setOpaque(false);
        bottomRow.add(buildPieChartPanel());
        bottomRow.add(buildTopRoutesPanel());

        // ── Stack everything in a scroll-friendly box ─────────
        JPanel body = new JPanel(new GridLayout(3, 1, 0, 14));
        body.setOpaque(false);
        body.add(kpiRow);
        body.add(midRow);
        body.add(bottomRow);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel kpiCard(String label, String value, String sub, Color color) {
        JPanel p = AppTheme.card(14);
        p.setLayout(new BorderLayout(0,4));
        p.setBorder(BorderFactory.createEmptyBorder(16,18,16,18));

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_BODY);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI",Font.BOLD,26));
        val.setForeground(color);

        JLabel subL = new JLabel(sub);
        subL.setFont(AppTheme.FONT_SMALL);
        subL.setForeground(AppTheme.TEXT_LIGHT);

        // Color bar on top
        JPanel colorBar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(color);
                g2.fillRoundRect(0,0,getWidth(),4,4,4);
                g2.dispose();
            }
        };
        colorBar.setOpaque(false);
        colorBar.setPreferredSize(new Dimension(0,6));

        p.add(colorBar, BorderLayout.NORTH);
        p.add(lbl, BorderLayout.WEST);
        JPanel mid = new JPanel(new BorderLayout());
        mid.setOpaque(false);
        mid.add(val, BorderLayout.CENTER);
        mid.add(subL, BorderLayout.SOUTH);
        p.add(mid, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildBarChart(int[] weeklyData) {
        JPanel card = AppTheme.card(14);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel t = new JLabel("Bookings This Week");
        t.setFont(AppTheme.FONT_SUBHEAD);
        t.setForeground(AppTheme.TEXT_PRIMARY);

        final int[] data = (weeklyData != null && weeklyData.length == 7) ? weeklyData
                : new int[]{0, 0, 0, 0, 0, 0, 0};
        final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_LIGHT);
                g2.fillRect(0, 0, getWidth(), getHeight());

                int max = 0;
                for (int v : data) if (v > max) max = v;
                if (max == 0) max = 10; // prevent divide-by-zero

                int padL = 34, padB = 24, padT = 12, padR = 10;
                int chartW = getWidth() - padL - padR;
                int chartH = getHeight() - padT - padB;
                int barW   = chartW / data.length;

                // Grid lines
                g2.setStroke(new BasicStroke(0.5f));
                for (int i = 0; i <= 4; i++) {
                    int y = padT + chartH * i / 4;
                    g2.setColor(AppTheme.BORDER_COLOR);
                    g2.drawLine(padL, y, padL + chartW, y);
                    g2.setColor(AppTheme.TEXT_LIGHT);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    g2.drawString(String.valueOf(max - max * i / 4), 2, y + 4);
                }

                // Bars
                for (int i = 0; i < data.length; i++) {
                    int bh = data[i] == 0 ? 2 : (int)((double) data[i] / max * chartH);
                    int bx = padL + i * barW + 4;
                    int by = padT + chartH - bh;
                    GradientPaint gp = new GradientPaint(bx, by, AppTheme.PRIMARY, bx, by + bh, AppTheme.PRIMARY_LIGHT);
                    g2.setPaint(gp);
                    g2.fillRoundRect(bx, by, barW - 8, bh, 6, 6);
                    // Day label
                    g2.setColor(AppTheme.TEXT_SECONDARY);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(days[i], bx + (barW - 8 - fm.stringWidth(days[i])) / 2, getHeight() - 6);
                    // Value on bar
                    if (data[i] > 0) {
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                        String v = String.valueOf(data[i]);
                        fm = g2.getFontMetrics();
                        g2.drawString(v, bx + (barW - 8 - fm.stringWidth(v)) / 2, by + 12);
                    }
                }
                g2.dispose();
            }
        };
        chart.setOpaque(false);
        chart.setPreferredSize(new Dimension(0, 200));

        card.add(t, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRecentBookingsTable() {
        JPanel card = AppTheme.card(14);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel t = new JLabel("Recent Bookings");
        t.setFont(AppTheme.FONT_SUBHEAD);
        t.setForeground(AppTheme.TEXT_PRIMARY);

        // Load last 6 bookings from DB
        java.util.List<model.Booking> recent = new dao.BookingDAO().getAllBookings();
        int limit = Math.min(6, recent.size());
        String[] cols = {"Ref", "Passenger", "Route", "Amount", "Status"};
        Object[][] rows = new Object[limit == 0 ? 1 : limit][5];
        if (limit == 0) {
            rows[0] = new Object[]{"—", "No bookings yet", "—", "—", "—"};
        } else {
            for (int i = 0; i < limit; i++) {
                model.Booking b = recent.get(i);
                String status = "Confirmed".equals(b.getStatus()) ? "✅ " + b.getStatus()
                              : "Cancelled".equals(b.getStatus()) ? "❌ " + b.getStatus()
                              : "⏳ " + b.getStatus();
                rows[i] = new Object[]{
                    b.getBookingRef(),
                    b.getPassengerName() != null ? b.getPassengerName() : "—",
                    b.getSource() + "→" + b.getDestination(),
                    b.getTotalFormatted(),
                    status
                };
            }
        }

        JTable table = buildStyledTable(rows, cols);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setBackground(Color.WHITE);
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(t, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JTable buildStyledTable(Object[][] rows, String[] cols) {
        JTable table = new JTable(rows, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setFont(AppTheme.FONT_BODY);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,0));
        table.setBackground(Color.WHITE);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.getTableHeader().setFont(AppTheme.FONT_BOLD);
        table.getTableHeader().setBackground(AppTheme.BG_LIGHT);
        table.getTableHeader().setForeground(AppTheme.TEXT_SECONDARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0,AppTheme.BORDER_COLOR));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c=super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                c.setBackground(sel?new Color(232,240,254):(row%2==0?Color.WHITE:new Color(248,250,254)));
                ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                return c;
            }
        });
        return table;
    }

    private JPanel buildPieChartPanel() {
        JPanel card = AppTheme.card(14);
        card.setLayout(new BorderLayout(0,8));
        card.setBorder(BorderFactory.createEmptyBorder(16,18,16,18));

        JLabel t = new JLabel("Bookings by Route Type");
        t.setFont(AppTheme.FONT_SUBHEAD);
        t.setForeground(AppTheme.TEXT_PRIMARY);

        JPanel pie = new JPanel() {
            final int[] vals = {40,25,20,15};
            final String[] labels = {"MUM-BLR","DEL-AGR","BLR-CHN","Others"};
            final Color[] colors = {AppTheme.PRIMARY, AppTheme.SUCCESS, AppTheme.WARNING, AppTheme.DANGER};
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int size=Math.min(getWidth(),getHeight())-20;
                int x=(getWidth()-size)/2, y=10;
                double angle=0;
                for (int i=0;i<vals.length;i++) {
                    double sweep=vals[i]*3.6;
                    g2.setColor(colors[i]);
                    g2.fill(new Arc2D.Double(x,y,size,size,angle,sweep,Arc2D.PIE));
                    angle+=sweep;
                }
                // Center hole
                g2.setColor(Color.WHITE);
                int hole=(int)(size*0.42);
                g2.fillOval(x+(size-hole)/2,y+(size-hole)/2,hole,hole);
                // Total
                g2.setColor(AppTheme.TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI",Font.BOLD,14));
                String total="100%";
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(total,x+(size-fm.stringWidth(total))/2,y+(size+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        pie.setOpaque(false);
        pie.setPreferredSize(new Dimension(0,140));

        card.add(t, BorderLayout.NORTH);
        card.add(pie, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTopRoutesPanel() {
        JPanel card = AppTheme.card(14);
        card.setLayout(new BorderLayout(0,8));
        card.setBorder(BorderFactory.createEmptyBorder(16,18,16,18));

        JLabel t = new JLabel("Top Routes Today");
        t.setFont(AppTheme.FONT_SUBHEAD);
        t.setForeground(AppTheme.TEXT_PRIMARY);

        JPanel routes = new JPanel();
        routes.setLayout(new BoxLayout(routes, BoxLayout.Y_AXIS));
        routes.setOpaque(false);

        String[][] routeData = {
            {"Mumbai → Bangalore","85%","42 bookings"},
            {"Delhi → Agra","72%","30 bookings"},
            {"Bangalore → Chennai","61%","25 bookings"},
            {"Hyderabad → Pune","48%","18 bookings"},
        };

        for (String[] rd : routeData) {
            JPanel row = new JPanel(new BorderLayout(0,2));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(4,0,4,0));
            JLabel routeL = new JLabel(rd[0]);
            routeL.setFont(AppTheme.FONT_BODY);
            routeL.setForeground(AppTheme.TEXT_PRIMARY);
            JLabel pctL = new JLabel(rd[1] + "  " + rd[2]);
            pctL.setFont(AppTheme.FONT_SMALL);
            pctL.setForeground(AppTheme.TEXT_SECONDARY);

            int pct = Integer.parseInt(rd[1].replace("%",""));
            JPanel bar = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setColor(AppTheme.BG_LIGHT);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),4,4);
                    g2.setColor(AppTheme.PRIMARY_LIGHT);
                    g2.fillRoundRect(0,0,(int)(getWidth()*pct/100.0),getHeight(),4,4);
                    g2.dispose();
                }
            };
            bar.setOpaque(false);
            bar.setPreferredSize(new Dimension(0,6));

            row.add(routeL, BorderLayout.NORTH);
            row.add(bar, BorderLayout.CENTER);
            row.add(pctL, BorderLayout.SOUTH);
            routes.add(row);
        }

        card.add(t, BorderLayout.NORTH);
        card.add(routes, BorderLayout.CENTER);
        return card;
    }

    // ─── Bus Management ─────────────────────────────────────────
    private JPanel buildBusManagementPanel() {
        dao.BusDAO busDAO = new dao.BusDAO();
        java.util.List<model.Bus> buses = busDAO.getAllBuses();
        Object[][] busRows = buses.stream().map(b -> new Object[]{
            b.getBusNumber(), b.getBusName(), b.getBusType(),
            b.getCapacity(), b.getStatus(), ""
        }).toArray(Object[][]::new);
        if (busRows.length == 0) busRows = new Object[][]{{"—","No buses added","—","—","—",""}};
        return buildCrudPanel("Manage Buses",
            new String[]{"Bus No.","Bus Name","Type","Capacity","Status","Action"},
            busRows,
            new String[]{"Bus Number (e.g. KA01-BUS-001)","Bus Name","Type (AC Seater / AC Sleeper / Non-AC Seater)","Seating Capacity (number)"},
            "buses", 1
        );
    }

    // ─── Route Management ───────────────────────────────────────
    private JPanel buildRouteManagementPanel() {
        dao.RouteDAO routeDAO = new dao.RouteDAO();
        java.util.List<model.Route> routes = routeDAO.getAllRoutes();
        Object[][] routeRows = routes.stream().map(r -> new Object[]{
            r.getId(), r.getSource(), r.getDestination(),
            r.getDistanceKm() + " km", r.getDurationHrs() + " hrs", ""
        }).toArray(Object[][]::new);
        if (routeRows.length == 0) routeRows = new Object[][]{{"—","No routes","—","—","—",""}};
        return buildCrudPanel("Manage Routes",
            new String[]{"ID","Source","Destination","Distance","Duration","Action"},
            routeRows,
            new String[]{"Source City","Destination City","Distance in KM (number)","Duration in Hours (number)"},
            "routes", 2
        );
    }

    // ─── Schedule Panel ─────────────────────────────────────────
    private JPanel buildSchedulePanel() {
        dao.ScheduleDAO schDAO = new dao.ScheduleDAO();
        java.util.List<model.Schedule> schedules = schDAO.getAllSchedules();
        Object[][] schRows = schedules.stream().map(s -> new Object[]{
            s.getId(), s.getBusName(),
            s.getSource() + "→" + s.getDestination(),
            s.getDepartureTime(), s.getArrivalTime(), s.getRunDays(),
            s.getPriceFormatted(), ""
        }).toArray(Object[][]::new);
        if (schRows.length == 0) schRows = new Object[][]{{"—","No schedules","—","—","—","—","—",""}};
        return buildCrudPanel("Manage Schedules",
            new String[]{"ID","Bus","Route","Departure","Arrival","Days","Price","Action"},
            schRows,
            new String[]{"Bus Number (e.g. KA01-BUS-001)","Route ID (number)","Departure Time (HH:MM:SS)","Arrival Time (HH:MM:SS)","Days (Daily / Mon,Wed,Fri)","Ticket Price (₹)"},
            "schedule", 3
        );
    }

    // ─── Generic CRUD Panel ──────────────────────────────────────
    private JPanel buildCrudPanel(String title, String[] cols, Object[][] rows,
                                  String[] formFields, String card, int panelIndex) {
        JPanel panel = new JPanel(new BorderLayout(0,16));
        panel.setOpaque(false);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppTheme.FONT_HEADING);
        titleLbl.setForeground(AppTheme.TEXT_PRIMARY);

        JButton addBtn = AppTheme.primaryButton("+ Add New");
        addBtn.setPreferredSize(new Dimension(120,38));

        topRow.add(titleLbl, BorderLayout.WEST);
        topRow.add(addBtn, BorderLayout.EAST);

        DefaultTableModel model = new DefaultTableModel(rows, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(rows, cols);
        table.setModel(model);

        // Action column renderer — styled Edit / Delete buttons
        table.getColumnModel().getColumn(cols.length-1).setCellRenderer((t, v, sel, foc, row, col) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
            p.setBackground(sel ? new Color(232,240,254) : (row%2==0 ? Color.WHITE : new Color(248,250,254)));

            JLabel editLbl = new JLabel("Edit");
            editLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            editLbl.setForeground(Color.WHITE);
            editLbl.setOpaque(true);
            editLbl.setBackground(AppTheme.PRIMARY);
            editLbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

            JLabel delLbl = new JLabel("Delete");
            delLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            delLbl.setForeground(Color.WHITE);
            delLbl.setOpaque(true);
            delLbl.setBackground(AppTheme.DANGER);
            delLbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

            p.add(editLbl);
            p.add(delLbl);
            return p;
        });

        final JTable tableRef = table;

        // Direct MouseListener — most reliable way to handle action button clicks in JTable
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tableRef.rowAtPoint(e.getPoint());
                int col = tableRef.columnAtPoint(e.getPoint());
                if (row < 0 || col != cols.length - 1) return;

                // Determine Edit vs Delete by click X position within cell
                java.awt.Rectangle cellRect = tableRef.getCellRect(row, col, false);
                boolean isDelete = e.getX() > cellRect.x + cellRect.width / 2;

                if (isDelete) {
                    int confirm = JOptionPane.showConfirmDialog(AdminDashboard.this,
                        "Delete this record?", "Confirm Delete",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        ((DefaultTableModel) tableRef.getModel()).removeRow(row);
                        refreshPanel(card, panelIndex);
                    }
                } else {
                    // Collect current row values for pre-filling the edit dialog
                    String[] currentValues = new String[cols.length - 1];
                    for (int i = 0; i < cols.length - 1; i++) {
                        Object v = tableRef.getModel().getValueAt(row, i);
                        currentValues[i] = v != null ? v.toString() : "";
                    }
                    showEditDialog(title, cols, formFields, currentValues, card, panelIndex);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new AppTheme.RoundBorder(AppTheme.BORDER_COLOR,12));
        scroll.setBackground(Color.WHITE);
        scroll.getViewport().setBackground(Color.WHITE);

        // Ensure Action column is wide enough for Edit + Delete labels
        table.setRowHeight(42);
        table.getColumnModel().getColumn(cols.length-1).setPreferredWidth(160);
        table.getColumnModel().getColumn(cols.length-1).setMinWidth(160);

        addBtn.addActionListener(e -> showAddDialog(title, formFields, card, panelIndex));

        panel.add(topRow, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void showAddDialog(String context, String[] fields, String card, int panelIndex) {
        JDialog dlg = new JDialog(this, "Add " + context.replace("Manage ",""), true);
        dlg.setSize(440, 100 + fields.length * 65);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        java.util.List<JTextField> fieldList = new java.util.ArrayList<>();
        for (String f : fields) {
            JLabel lbl = new JLabel(f);
            lbl.setFont(AppTheme.FONT_BOLD);
            lbl.setForeground(AppTheme.TEXT_PRIMARY);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            JTextField tf = AppTheme.styledField(f);
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            tf.setAlignmentX(Component.LEFT_ALIGNMENT);
            fieldList.add(tf);
            form.add(lbl);
            form.add(Box.createVerticalStrut(4));
            form.add(tf);
            form.add(Box.createVerticalStrut(10));
        }

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER_COLOR));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dlg.dispose());

        JButton save = AppTheme.primaryButton("Save");
        save.addActionListener(e -> {
            // Validate all fields filled
            for (JTextField tf : fieldList) {
                if (tf.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Please fill all fields.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            boolean success = false;
            String errorMsg = "";

            try {
                switch (card) {
                    case "buses": {
                        // fields: Bus Number, Bus Name, Type, Seating Capacity
                        model.Bus bus = new model.Bus();
                        bus.setBusNumber(fieldList.get(0).getText().trim());
                        bus.setBusName  (fieldList.get(1).getText().trim());
                        bus.setBusType  (fieldList.get(2).getText().trim());
                        bus.setCapacity (Integer.parseInt(fieldList.get(3).getText().trim()));
                        bus.setStatus   ("Active");
                        int id = new dao.BusDAO().addBus(bus);
                        success = id > 0;
                        break;
                    }
                    case "routes": {
                        // fields: Source City, Destination City, Distance (km), Duration (hrs)
                        model.Route route = new model.Route();
                        route.setSource     (fieldList.get(0).getText().trim());
                        route.setDestination(fieldList.get(1).getText().trim());
                        route.setDistanceKm (Double.parseDouble(fieldList.get(2).getText().trim()));
                        route.setDurationHrs(Double.parseDouble(fieldList.get(3).getText().trim()));
                        int id = new dao.RouteDAO().addRoute(route);
                        success = id > 0;
                        break;
                    }
                    case "schedule": {
                        // fields: Bus No., Route ID, Departure Time, Arrival Time, Days, Price
                        String busNo   = fieldList.get(0).getText().trim();
                        int routeId    = Integer.parseInt(fieldList.get(1).getText().trim());
                        String depTime = fieldList.get(2).getText().trim();
                        String arrTime = fieldList.get(3).getText().trim();
                        String days    = fieldList.get(4).getText().trim();
                        double price   = Double.parseDouble(fieldList.get(5).getText().trim());

                        java.util.List<model.Bus> busList = new dao.BusDAO().getAllBuses();
                        int busId = busList.stream()
                            .filter(b -> b.getBusNumber().equalsIgnoreCase(busNo))
                            .mapToInt(model.Bus::getId).findFirst().orElse(-1);

                        if (busId == -1) {
                            errorMsg = "Bus number '" + busNo + "' not found. Add it under Manage Buses first.";
                            break;
                        }

                        model.Schedule sch = new model.Schedule();
                        sch.setBusId        (busId);
                        sch.setRouteId      (routeId);
                        sch.setDepartureTime(depTime);
                        sch.setArrivalTime  (arrTime);
                        sch.setRunDays      (days);
                        sch.setPrice        (price);
                        sch.setActive       (true);
                        int id = new dao.ScheduleDAO().addSchedule(sch);
                        success = id > 0;
                        break;
                    }
                    default:
                        success = true; // unsupported panel — just dismiss
                }
            } catch (NumberFormatException ex) {
                errorMsg = "Invalid number format. Please check numeric fields.";
            } catch (Exception ex) {
                errorMsg = "Error: " + ex.getMessage();
                ex.printStackTrace();
            }

            if (!errorMsg.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (success) {
                JOptionPane.showMessageDialog(dlg, "Saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                refreshPanel(card, panelIndex);
            } else {
                JOptionPane.showMessageDialog(dlg, "Save failed. Check console for details.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(cancel);
        footer.add(save);

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    /**
     * Edit dialog — pre-fills fields with current row values and saves via DAO update.
     * cols[]         = table column headers (excluding "Action")
     * formFields[]   = input field labels
     * currentValues[]= current row cell values, mapped to formFields by position
     */
    private void showEditDialog(String context, String[] cols, String[] formFields,
                                String[] currentValues, String card, int panelIndex) {
        JDialog dlg = new JDialog(this, "Edit " + context.replace("Manage ",""), true);
        dlg.setSize(460, 110 + formFields.length * 65);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Map form fields to table columns to pre-fill values
        // cols = table headers (ID, Bus Name, Type, ...) without Action
        // formFields = dialog labels (Bus Number, Bus Name, ...)
        // We try to match each formField to a column by rough name match, fallback to index
        java.util.List<JTextField> fieldList = new java.util.ArrayList<>();
        for (int i = 0; i < formFields.length; i++) {
            JLabel lbl = new JLabel(formFields[i]);
            lbl.setFont(AppTheme.FONT_BOLD);
            lbl.setForeground(AppTheme.TEXT_PRIMARY);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Find best matching column value to pre-fill
            String prefill = "";
            if (i < currentValues.length) {
                prefill = currentValues[i];
            }
            // Special case: for schedule price, currentValues[6] = "₹850" → strip ₹
            if (formFields[i].toLowerCase().contains("price") && prefill.startsWith("₹")) {
                prefill = prefill.replace("₹", "").replace(",", "").trim();
            }
            // For buses: cols[0]=Bus No., cols[1]=Bus Name, cols[2]=Type, cols[3]=Capacity
            // formFields[0]=Bus Number, [1]=Bus Name, [2]=Type, [3]=Capacity
            // currentValues maps 1:1 with cols order — works fine for buses & routes
            // For schedule: cols = ID, Bus, Route, Dep, Arr, Days, Price
            //   formFields  = Bus Number, Route ID, Dep, Arr, Days, Price
            //   We skip col[0]=ID, col[2]=Route (we want raw IDs not "Source→Dest")
            if (card.equals("schedule")) {
                switch (i) {
                    case 0: prefill = currentValues.length > 1 ? currentValues[1] : ""; break; // Bus name (readonly hint)
                    case 1: prefill = ""; break; // Route ID — user must re-enter
                    case 2: prefill = currentValues.length > 3 ? currentValues[3] : ""; break; // Departure
                    case 3: prefill = currentValues.length > 4 ? currentValues[4] : ""; break; // Arrival
                    case 4: prefill = currentValues.length > 5 ? currentValues[5] : ""; break; // Days
                    case 5: // Price
                        String rawPrice = currentValues.length > 6 ? currentValues[6] : "";
                        prefill = rawPrice.replace("₹","").replace(",","").trim();
                        break;
                }
            }

            JTextField tf = AppTheme.styledField(formFields[i]);
            tf.setText(prefill);
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            tf.setAlignmentX(Component.LEFT_ALIGNMENT);
            fieldList.add(tf);

            form.add(lbl);
            form.add(Box.createVerticalStrut(4));
            form.add(tf);
            form.add(Box.createVerticalStrut(10));
        }

        // ID is always currentValues[0] — we need it for UPDATE queries
        final String recordId = currentValues.length > 0 ? currentValues[0] : "";

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER_COLOR));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dlg.dispose());

        JButton save = AppTheme.primaryButton("Update");
        save.addActionListener(e -> {
            for (JTextField tf : fieldList) {
                if (tf.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Please fill all fields.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            boolean success = false;
            String errorMsg = "";
            try {
                switch (card) {
                    case "buses": {
                        // formFields: Bus Number, Bus Name, Type, Capacity
                        // currentValues[0] = Bus Number (used as lookup key since buses table uses bus_number as natural key)
                        model.Bus bus = new model.Bus();
                        bus.setBusNumber(fieldList.get(0).getText().trim());
                        bus.setBusName  (fieldList.get(1).getText().trim());
                        bus.setBusType  (fieldList.get(2).getText().trim());
                        bus.setCapacity (Integer.parseInt(fieldList.get(3).getText().trim()));
                        bus.setStatus   ("Active");
                        // Find the bus ID by original bus number
                        java.util.List<model.Bus> busList = new dao.BusDAO().getAllBuses();
                        int busId = busList.stream()
                            .filter(b -> b.getBusNumber().equalsIgnoreCase(recordId))
                            .mapToInt(model.Bus::getId).findFirst().orElse(-1);
                        if (busId == -1) { errorMsg = "Bus not found in DB."; break; }
                        bus.setId(busId);
                        success = new dao.BusDAO().updateBus(bus);
                        break;
                    }
                    case "routes": {
                        // formFields: Source, Destination, Distance, Duration
                        // currentValues[0] = route ID
                        int routeId = Integer.parseInt(recordId);
                        model.Route route = new model.Route();
                        route.setId         (routeId);
                        route.setSource     (fieldList.get(0).getText().trim());
                        route.setDestination(fieldList.get(1).getText().trim());
                        route.setDistanceKm (Double.parseDouble(fieldList.get(2).getText().trim()));
                        route.setDurationHrs(Double.parseDouble(fieldList.get(3).getText().trim()));
                        success = new dao.RouteDAO().updateRoute(route);
                        break;
                    }
                    case "schedule": {
                        // formFields: Bus Number, Route ID, Dep, Arr, Days, Price
                        // currentValues[0] = schedule ID
                        int scheduleId = Integer.parseInt(recordId);
                        String busNo   = fieldList.get(0).getText().trim();
                        int routeId    = Integer.parseInt(fieldList.get(1).getText().trim());
                        String depTime = fieldList.get(2).getText().trim();
                        String arrTime = fieldList.get(3).getText().trim();
                        String days    = fieldList.get(4).getText().trim();
                        double price   = Double.parseDouble(fieldList.get(5).getText().trim());

                        java.util.List<model.Bus> busList = new dao.BusDAO().getAllBuses();
                        int busId = busList.stream()
                            .filter(b -> b.getBusNumber().equalsIgnoreCase(busNo))
                            .mapToInt(model.Bus::getId).findFirst().orElse(-1);
                        if (busId == -1) { errorMsg = "Bus '" + busNo + "' not found."; break; }

                        model.Schedule sch = new model.Schedule();
                        sch.setId           (scheduleId);
                        sch.setBusId        (busId);
                        sch.setRouteId      (routeId);
                        sch.setDepartureTime(depTime);
                        sch.setArrivalTime  (arrTime);
                        sch.setRunDays      (days);
                        sch.setPrice        (price);
                        sch.setActive       (true);
                        success = new dao.ScheduleDAO().updateSchedule(sch);
                        break;
                    }
                    default: success = true;
                }
            } catch (NumberFormatException ex) {
                errorMsg = "Invalid number format. Check numeric fields.";
            } catch (Exception ex) {
                errorMsg = "Error: " + ex.getMessage();
                ex.printStackTrace();
            }

            if (!errorMsg.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (success) {
                JOptionPane.showMessageDialog(dlg, "Updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                refreshPanel(card, panelIndex);
            } else {
                JOptionPane.showMessageDialog(dlg, "Update failed. Check console.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(cancel);
        footer.add(save);
        dlg.add(new JScrollPane(form), BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ─── Seat Layout Editor ─────────────────────────────────────
    private JPanel buildSeatLayoutEditor() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JLabel title = new JLabel("Seat Layout Editor");
        title.setFont(AppTheme.FONT_HEADING);
        title.setForeground(AppTheme.TEXT_PRIMARY);

        java.util.List<model.Bus> busList = new dao.BusDAO().getAllBuses();
        if (busList.isEmpty()) {
            JLabel empty = new JLabel("No buses found. Add buses first via Manage Buses.");
            empty.setFont(AppTheme.FONT_BODY);
            empty.setForeground(AppTheme.TEXT_SECONDARY);
            panel.add(title, BorderLayout.NORTH);
            panel.add(empty, BorderLayout.CENTER);
            return panel;
        }

        String[] busOptions = busList.stream()
            .map(b -> b.getBusNumber() + "  —  " + b.getBusName() + "  (" + b.getCapacity() + " seats)")
            .toArray(String[]::new);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topRow.setOpaque(false);
        JLabel selLbl = new JLabel("Select Bus:");
        selLbl.setFont(AppTheme.FONT_BOLD);
        selLbl.setForeground(AppTheme.TEXT_PRIMARY);
        JComboBox<String> busCombo = AppTheme.styledCombo(busOptions);
        busCombo.setPreferredSize(new Dimension(380, 38));
        topRow.add(selLbl);
        topRow.add(busCombo);

        // Tool selector
        String[] typeNames  = {"Standard", "Window", "Elder", "Disabled"};
        String[] typeColors = {"#1a73e8", "#2e7d32", "#e65100", "#6a1b9a"};
        final String[] activeTool = {"Standard"};

        JPanel tools = AppTheme.card(12);
        tools.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        JLabel toolsLbl = new JLabel("Select a type, then click seats to assign:");
        toolsLbl.setFont(AppTheme.FONT_BOLD);
        toolsLbl.setForeground(AppTheme.TEXT_PRIMARY);
        tools.add(toolsLbl);
        ButtonGroup toolGroup = new ButtonGroup();
        for (int i = 0; i < typeNames.length; i++) {
            final String key = typeNames[i];
            JToggleButton tb = new JToggleButton(typeNames[i]);
            tb.setFont(AppTheme.FONT_BOLD);
            try { tb.setForeground(Color.decode(typeColors[i])); } catch (Exception ignored) {}
            tb.setFocusPainted(false);
            tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (i == 0) tb.setSelected(true);
            tb.addActionListener(e -> activeTool[0] = key);
            toolGroup.add(tb); tools.add(tb);
        }

        // seatTypeMap: "A1" -> "Window" etc.
        java.util.Map<String, String> seatTypeMap = new java.util.LinkedHashMap<>();

        JPanel gridHolder = new JPanel(new BorderLayout());
        gridHolder.setOpaque(false);

        JLabel statusLbl = new JLabel(" ");
        statusLbl.setFont(AppTheme.FONT_SMALL);
        statusLbl.setForeground(AppTheme.TEXT_SECONDARY);

        JButton saveLayout = AppTheme.primaryButton("\uD83D\uDCBE  Save Layout");

        Color[] bgColors = {new Color(232,240,254), new Color(232,245,233), new Color(255,243,224), new Color(243,229,245)};
        Color[] bdColors = {AppTheme.PRIMARY, new Color(46,125,50), new Color(230,81,0), new Color(106,27,154)};

        Runnable buildGrid = () -> {
            int idx = busCombo.getSelectedIndex();
            if (idx < 0 || idx >= busList.size()) return;
            model.Bus bus = busList.get(idx);
            int capacity = bus.getCapacity();
            int busId    = bus.getId();

            seatTypeMap.clear();
            java.util.List<model.SeatLayout> existing = new dao.SeatLayoutDAO().getSeatsByBus(busId);
            for (model.SeatLayout sl : existing)
                seatTypeMap.put(sl.getSeatNumber(), sl.getSeatType());

            JPanel grid = new JPanel(new GridBagLayout());
            grid.setBackground(Color.WHITE);
            grid.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);

            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 6;
            JLabel driver = new JLabel("  \uD83D\uDE8C  FRONT \u2014 Driver", SwingConstants.LEFT);
            driver.setFont(AppTheme.FONT_BOLD); driver.setForeground(AppTheme.TEXT_SECONDARY);
            driver.setOpaque(true); driver.setBackground(AppTheme.BG_LIGHT);
            driver.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
            grid.add(driver, gbc);
            gbc.gridwidth = 1;

            int rows = (int) Math.ceil(capacity / 4.0);
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < 4; c++) {
                    if (r * 4 + c + 1 > capacity) break;
                    if (c == 2) { gbc.gridx = 3; gbc.gridy = r + 1; grid.add(new JLabel(""), gbc); }
                    String seatLabel = String.valueOf((char)('A' + r)) + (c + 1);
                    String initType  = seatTypeMap.getOrDefault(seatLabel, "Standard");
                    int[] ti = {typeIndexOf(initType)};

                    JButton seat = new JButton() {
                        @Override protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(bgColors[ti[0]]);
                            g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                            g2.setColor(bdColors[ti[0]]);
                            g2.setStroke(new BasicStroke(1.5f));
                            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                            FontMetrics fm = g2.getFontMetrics();
                            g2.drawString(seatLabel,(getWidth()-fm.stringWidth(seatLabel))/2,
                                (getHeight()+fm.getAscent()-fm.getDescent())/2);
                            g2.dispose();
                        }
                    };
                    seat.setPreferredSize(new Dimension(46, 38));
                    seat.setFocusPainted(false); seat.setBorderPainted(false); seat.setContentAreaFilled(false);
                    seat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    seat.setToolTipText(seatLabel + " \u2014 " + initType);
                    seat.addActionListener(ev -> {
                        ti[0] = typeIndexOf(activeTool[0]);
                        seatTypeMap.put(seatLabel, activeTool[0]);
                        seat.setToolTipText(seatLabel + " \u2014 " + activeTool[0]);
                        seat.repaint();
                        statusLbl.setForeground(AppTheme.TEXT_SECONDARY);
                        statusLbl.setText("Modified \u2014 click Save Layout to persist changes.");
                    });
                    gbc.gridx = c + (c >= 2 ? 1 : 0); gbc.gridy = r + 1;
                    grid.add(seat, gbc);
                }
            }

            JScrollPane sp = new JScrollPane(grid);
            sp.setBorder(null); sp.setOpaque(false); sp.getViewport().setOpaque(false);
            gridHolder.removeAll(); gridHolder.add(sp, BorderLayout.CENTER);
            gridHolder.revalidate(); gridHolder.repaint();
            statusLbl.setForeground(AppTheme.TEXT_SECONDARY);
            statusLbl.setText(existing.isEmpty()
                ? "No layout saved yet for " + bus.getBusName() + ". Assign types and save."
                : "Loaded layout for " + bus.getBusName() + " (" + existing.size() + " marked seats).");
        };

        buildGrid.run();
        busCombo.addActionListener(e -> buildGrid.run());

        saveLayout.addActionListener(e -> {
            int idx = busCombo.getSelectedIndex();
            if (idx < 0 || idx >= busList.size()) return;
            int busId = busList.get(idx).getId();
            java.util.List<model.SeatLayout> toSave = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, String> entry : seatTypeMap.entrySet()) {
                model.SeatLayout sl = new model.SeatLayout();
                sl.setBusId(busId); sl.setSeatNumber(entry.getKey()); sl.setSeatType(entry.getValue());
                toSave.add(sl);
            }
            boolean ok = new dao.SeatLayoutDAO().saveSeatLayout(busId, toSave);
            statusLbl.setForeground(ok ? new Color(27,94,32) : AppTheme.DANGER);
            statusLbl.setText(ok
                ? "\u2705  Layout saved for " + busList.get(idx).getBusName() + "!"
                : "\u274C  Save failed. Check console.");
        });

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        legendPanel.setOpaque(false);
        for (int i = 0; i < typeNames.length; i++) {
            JLabel dot = new JLabel("\u2B1B");
            try { dot.setForeground(Color.decode(typeColors[i])); } catch (Exception ignored) {}
            dot.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            JLabel lbl = new JLabel(typeNames[i]);
            lbl.setFont(AppTheme.FONT_SMALL); lbl.setForeground(AppTheme.TEXT_SECONDARY);
            legendPanel.add(dot); legendPanel.add(lbl);
        }

        JPanel leftBtm = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBtm.setOpaque(false);
        leftBtm.add(legendPanel); leftBtm.add(statusLbl);
        JPanel btmRow = new JPanel(new BorderLayout(12, 0));
        btmRow.setOpaque(false);
        btmRow.add(leftBtm, BorderLayout.CENTER);
        btmRow.add(saveLayout, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(tools, BorderLayout.NORTH);
        center.add(gridHolder, BorderLayout.CENTER);
        center.add(btmRow, BorderLayout.SOUTH);

        JPanel hRow = new JPanel(new BorderLayout(0, 10));
        hRow.setOpaque(false);
        hRow.add(title, BorderLayout.NORTH);
        hRow.add(topRow, BorderLayout.CENTER);

        panel.add(hRow, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private int typeIndexOf(String type) {
        if (type == null) return 0;
        switch (type.toLowerCase()) {
            case "window":               return 1;
            case "elder": case "senior": return 2;
            case "disabled":             return 3;
            default:                     return 0;
        }
    }

    private JPanel buildSeatGrid(int capacity) { return new JPanel(); }


    // ─── All Bookings Panel ─────────────────────────────────────
    private JPanel buildBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0,16));
        panel.setOpaque(false);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel title = new JLabel("All Bookings");
        title.setFont(AppTheme.FONT_HEADING);
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        filterRow.setOpaque(false);
        JTextField search = AppTheme.styledField("Search booking...");
        search.setPreferredSize(new Dimension(200,36));
        JComboBox<String> statusFilter = AppTheme.styledCombo(new String[]{"All Status","Confirmed","Pending","Cancelled","Completed"});
        statusFilter.setPreferredSize(new Dimension(150,36));
        filterRow.add(search); filterRow.add(statusFilter);

        topRow.add(title, BorderLayout.WEST);
        topRow.add(filterRow, BorderLayout.EAST);

        dao.BookingDAO bkDAO = new dao.BookingDAO();
        java.util.List<model.Booking> allBookings = bkDAO.getAllBookings();
        String[] cols = {"Booking ID","Passenger","Bus","Route","Date","Seats","Amount","Status"};
        Object[][] rows = allBookings.stream().map(b -> new Object[]{
            b.getBookingRef(), b.getPassengerName(), b.getBusName(),
            b.getSource()+"→"+b.getDestination(),
            b.getJourneyDate()!=null?b.getJourneyDate().toString():"",
            b.getNumPassengers(), b.getTotalFormatted(), b.getStatus()
        }).toArray(Object[][]::new);
        if (rows.length == 0) rows = new Object[][]{{"—","No bookings yet","—","—","—","—","—","—"}};

        JTable table = buildStyledTable(rows, cols);
        // Color status column
        table.getColumnModel().getColumn(7).setCellRenderer((t,v,sel,foc,row,col)->{
            JLabel l=new JLabel(v.toString(),SwingConstants.CENTER);
            l.setFont(AppTheme.FONT_BOLD); l.setOpaque(true);
            switch(v.toString()) {
                case "Confirmed": l.setBackground(new Color(232,245,233)); l.setForeground(AppTheme.SUCCESS); break;
                case "Cancelled": l.setBackground(new Color(255,235,238)); l.setForeground(AppTheme.DANGER); break;
                case "Pending": l.setBackground(new Color(255,243,224)); l.setForeground(AppTheme.WARNING); break;
                default: l.setBackground(new Color(232,240,254)); l.setForeground(AppTheme.PRIMARY); break;
            }
            l.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
            return l;
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new AppTheme.RoundBorder(AppTheme.BORDER_COLOR,12));
        scroll.setBackground(Color.WHITE);
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(topRow, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
}