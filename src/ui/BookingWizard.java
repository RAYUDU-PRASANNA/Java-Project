package ui;

import util.AppTheme;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class BookingWizard extends JDialog {

    private final String[] busData;
    private final String userName;
    private final int userId;
    private int currentStep = 0;
    private int numPassengers = 1;

    private JPanel wizardContent;
    private CardLayout wizardLayout;
    private JLabel[] stepLabels;
    private JPanel stepIndicator;
    private String confirmedRef = "BK-00000";

    // Booking data
    private List<JTextField> passengerNames = new ArrayList<>();
    private List<JTextField> passengerAges = new ArrayList<>();
    private List<JComboBox<String>> passengerGenders = new ArrayList<>();
    private List<Integer> selectedSeats = new ArrayList<>();
    private JTextField passengerCountField;

    private static final int SEATS_PER_ROW = 4;
    private int TOTAL_ROWS = 10; // updated dynamically from DB
    private JButton[][] seatButtons;
    // seatType: 0=normal, 1=window, 2=elder, 3=disabled, 4=selected, 5=booked
    private int[][] seatType;

    private String[] steps = {"Passengers", "Seat Selection", "Review & Pay", "Confirmation"};

    public BookingWizard(JFrame parent, String[] busData, String userName, int userId) {
        super(parent, "Book Ticket — " + busData[1], true);
        this.busData = busData;
        this.userName = userName;
        this.userId = userId;
        setSize(800, 620);
        setLocationRelativeTo(parent);
        setResizable(false);
        initSeatData();
        initUI();
    }

    private void initSeatData() {
        int scheduleId = -1;
        try { scheduleId = Integer.parseInt(busData[0].trim()); } catch (NumberFormatException ignored) {}

        // Step 1: Load schedule → get bus_id and capacity
        int busId   = -1;
        int capacity = 40; // default
        if (scheduleId > 0) {
            try {
                model.Schedule sch = new dao.ScheduleDAO().getById(scheduleId);
                if (sch != null) {
                    busId    = sch.getBusId();
                    capacity = sch.getCapacity() > 0 ? sch.getCapacity() : 40;
                }
            } catch (Exception e) {
                System.err.println("[BookingWizard] Failed to load schedule: " + e.getMessage());
            }
        }

        TOTAL_ROWS = (int) Math.ceil(capacity / (double) SEATS_PER_ROW);
        seatType   = new int[TOTAL_ROWS][SEATS_PER_ROW];

        // Step 2: Load seat layout types from DB (window/elder/disabled markings)
        if (busId > 0) {
            try {
                java.util.List<model.SeatLayout> layouts = new dao.SeatLayoutDAO().getSeatsByBus(busId);
                for (model.SeatLayout sl : layouts) {
                    // seat_number format: "A1", "B3", etc.
                    String sn = sl.getSeatNumber();
                    if (sn == null || sn.length() < 2) continue;
                    int row = sn.charAt(0) - 'A';
                    int col;
                    try { col = Integer.parseInt(sn.substring(1)) - 1; } catch (NumberFormatException e) { continue; }
                    if (row < 0 || row >= TOTAL_ROWS || col < 0 || col >= SEATS_PER_ROW) continue;
                    switch (sl.getSeatType().toLowerCase()) {
                        case "window":   seatType[row][col] = 1; break;
                        case "elder":
                        case "senior":   seatType[row][col] = 2; break;
                        case "disabled": seatType[row][col] = 3; break;
                        default:         seatType[row][col] = 0; break;
                    }
                }
            } catch (Exception e) {
                System.err.println("[BookingWizard] Failed to load seat layout: " + e.getMessage());
            }
        } else {
            // No layout in DB — apply sensible defaults based on capacity
            if (TOTAL_ROWS > 0) {
                seatType[0][0] = 2; // elder front-left
                if (SEATS_PER_ROW > 3) seatType[0][3] = 2; // elder front-right
            }
            if (TOTAL_ROWS > 1) {
                seatType[1][0] = 3; // disabled
                if (SEATS_PER_ROW > 3) seatType[1][3] = 3;
            }
            for (int r = 2; r < Math.min(TOTAL_ROWS, 5); r++) {
                seatType[r][0] = 1; // window left
                if (SEATS_PER_ROW > 3) seatType[r][3] = 1; // window right
            }
        }

        // Step 3: Mark already-booked seats from DB
        if (scheduleId > 0) {
            try {
                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                java.util.List<String> bookedSeats =
                    new dao.BookingDAO().getBookedSeats(scheduleId, today);
                for (String sn : bookedSeats) {
                    if (sn == null || sn.length() < 2) continue;
                    int row = sn.charAt(0) - 'A';
                    int col;
                    try { col = Integer.parseInt(sn.substring(1)) - 1; } catch (NumberFormatException e) { continue; }
                    if (row >= 0 && row < TOTAL_ROWS && col >= 0 && col < SEATS_PER_ROW)
                        seatType[row][col] = 5; // booked
                }
            } catch (Exception e) {
                System.err.println("[BookingWizard] Failed to load booked seats: " + e.getMessage());
            }
        }
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,AppTheme.PRIMARY_DARK,getWidth(),0,AppTheme.PRIMARY);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(12,20,12,20));
        header.setPreferredSize(new Dimension(0,70));

        JLabel busName = new JLabel("🚌  " + busData[1]);
        busName.setFont(new Font("Segoe UI",Font.BOLD,18));
        busName.setForeground(Color.WHITE);

        JLabel busInfo = new JLabel(busData[2] + "   •   " + busData[3] + " → " + busData[4] + "   •   " + busData[6] + " per seat");
        busInfo.setFont(AppTheme.FONT_BODY);
        busInfo.setForeground(new Color(255,255,255,200));

        JPanel hInfo = new JPanel(new GridLayout(2,1));
        hInfo.setOpaque(false);
        hInfo.add(busName); hInfo.add(busInfo);
        header.add(hInfo, BorderLayout.CENTER);

        // Step indicator
        stepIndicator = buildStepIndicator();

        // Wizard content
        wizardLayout = new CardLayout();
        wizardContent = new JPanel(wizardLayout);
        wizardContent.setBackground(AppTheme.BG_LIGHT);
        wizardContent.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));

        wizardContent.add(buildPassengerCountStep(), "step0");
        wizardContent.add(buildSeatSelectionStep(), "step1");
        wizardContent.add(buildReviewStep(), "step2");
        wizardContent.add(buildConfirmationStep(), "step3");

        // Footer buttons
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,AppTheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(12,24,12,24)
        ));

        JButton prevBtn = new JButton("← Back");
        prevBtn.setFont(AppTheme.FONT_BOLD);
        prevBtn.setForeground(AppTheme.TEXT_SECONDARY);
        prevBtn.setBorderPainted(false);
        prevBtn.setContentAreaFilled(false);
        prevBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        prevBtn.addActionListener(e -> {
            if (currentStep > 0) { currentStep--; showStep(); }
            else dispose();
        });

        JButton nextBtn = AppTheme.primaryButton("Continue →");
        nextBtn.setPreferredSize(new Dimension(140, 42));
        nextBtn.addActionListener(e -> {
            if (currentStep == 2) {
                // Save to database before showing confirmation
                boolean saved = saveBookingToDB();
                if (!saved) {
                    JOptionPane.showMessageDialog(this,
                        "Booking failed. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            if (currentStep < steps.length-1) { currentStep++; showStep(); }
            else dispose();
        });

        footer.add(prevBtn, BorderLayout.WEST);
        footer.add(nextBtn, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);
        root.add(stepIndicator, BorderLayout.CENTER);
        root.add(wizardContent, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        // Fix: properly stack header+steps+content
        JPanel centerStack = new JPanel(new BorderLayout());
        centerStack.add(stepIndicator, BorderLayout.NORTH);
        centerStack.add(wizardContent, BorderLayout.CENTER);

        root.remove(stepIndicator);
        root.remove(wizardContent);
        root.add(centerStack, BorderLayout.CENTER);

        setContentPane(root);
        showStep();
    }

    private JPanel buildStepIndicator() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(0,0,1,0,AppTheme.BORDER_COLOR));

        stepLabels = new JLabel[steps.length];
        for (int i=0; i<steps.length; i++) {
            final int idx = i;

            JPanel step = new JPanel(new FlowLayout(FlowLayout.CENTER,6,0));
            step.setOpaque(false);

            JLabel circle = new JLabel(String.valueOf(i+1), SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(idx <= currentStep ? AppTheme.PRIMARY : AppTheme.BORDER_COLOR);
                    g2.fillOval(0,0,getWidth(),getHeight());
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI",Font.BOLD,11));
                    FontMetrics fm=g2.getFontMetrics();
                    String t = getText();
                    g2.drawString(t,(getWidth()-fm.stringWidth(t))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            circle.setPreferredSize(new Dimension(26,26));

            stepLabels[i] = new JLabel(steps[i]);
            stepLabels[i].setFont(AppTheme.FONT_SMALL);
            stepLabels[i].setForeground(i == currentStep ? AppTheme.PRIMARY : AppTheme.TEXT_LIGHT);

            step.add(circle);
            step.add(stepLabels[i]);
            p.add(step);

            if (i < steps.length-1) {
                JPanel line = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        g.setColor(AppTheme.BORDER_COLOR);
                        g.drawLine(0,getHeight()/2,getWidth(),getHeight()/2);
                    }
                };
                line.setOpaque(false);
                line.setPreferredSize(new Dimension(50,26));
                p.add(line);
            }
        }
        return p;
    }

    private void showStep() {
        wizardLayout.show(wizardContent, "step" + currentStep);
        stepIndicator.repaint();
        // Rebuild step3 (confirmation) dynamically
        if (currentStep == 2) {
            wizardContent.remove(wizardContent.getComponent(2));
            wizardContent.add(buildReviewStep(), "step2");
            wizardLayout.show(wizardContent,"step2");
        }
    }

    // ─── Step 0: Passenger count & details ────────────────────
    private JPanel buildPassengerCountStep() {
        JPanel panel = new JPanel(new BorderLayout(0,16));
        panel.setBackground(AppTheme.BG_LIGHT);

        JLabel title = new JLabel("Passenger Details");
        title.setFont(AppTheme.FONT_HEADING);
        title.setForeground(AppTheme.TEXT_PRIMARY);

        // Count selector
        JPanel countRow = AppTheme.card(12);
        countRow.setLayout(new FlowLayout(FlowLayout.LEFT,12,12));

        JLabel countLbl = new JLabel("Number of Passengers:");
        countLbl.setFont(AppTheme.FONT_BOLD);
        countLbl.setForeground(AppTheme.TEXT_PRIMARY);

        JButton minus = new JButton("−");
        minus.setFont(new Font("Segoe UI",Font.BOLD,18));
        minus.setPreferredSize(new Dimension(36,36));
        minus.setFocusPainted(false);

        JLabel countDisplay = new JLabel("1", SwingConstants.CENTER);
        countDisplay.setFont(new Font("Segoe UI",Font.BOLD,20));
        countDisplay.setForeground(AppTheme.PRIMARY);
        countDisplay.setPreferredSize(new Dimension(40,36));

        JButton plus = new JButton("+");
        plus.setFont(new Font("Segoe UI",Font.BOLD,18));
        plus.setPreferredSize(new Dimension(36,36));
        plus.setFocusPainted(false);

        JPanel detailContainer = new JPanel();
        detailContainer.setLayout(new BoxLayout(detailContainer, BoxLayout.Y_AXIS));
        detailContainer.setOpaque(false);

        Runnable updateDetails = () -> {
            detailContainer.removeAll();
            passengerNames.clear(); passengerAges.clear(); passengerGenders.clear();
            for (int i=0; i<numPassengers; i++) {
                detailContainer.add(buildPassengerRow(i));
                detailContainer.add(Box.createVerticalStrut(8));
            }
            detailContainer.revalidate(); detailContainer.repaint();
        };

        minus.addActionListener(e -> { if (numPassengers>1) { numPassengers--; countDisplay.setText(String.valueOf(numPassengers)); updateDetails.run(); }});
        plus.addActionListener(e -> { if (numPassengers<6) { numPassengers++; countDisplay.setText(String.valueOf(numPassengers)); updateDetails.run(); }});

        countRow.add(countLbl); countRow.add(minus); countRow.add(countDisplay); countRow.add(plus);

        JScrollPane scroll = new JScrollPane(detailContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(title, BorderLayout.NORTH);
        panel.add(countRow, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(new JLabel("Passenger Information:"), BorderLayout.NORTH);
        bottom.add(scroll, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout(0,12));
        wrapper.setOpaque(false);
        wrapper.add(countRow, BorderLayout.NORTH);
        wrapper.add(bottom, BorderLayout.CENTER);
        panel.remove(panel.getComponent(0));

        updateDetails.run();

        JPanel outer = new JPanel(new BorderLayout(0,12));
        outer.setBackground(AppTheme.BG_LIGHT);
        outer.add(title, BorderLayout.NORTH);
        outer.add(wrapper, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildPassengerRow(int index) {
        JPanel row = AppTheme.card(10);
        row.setLayout(new GridBagLayout());
        row.setBorder(BorderFactory.createEmptyBorder(10,14,10,14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0,6,0,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel numLbl = new JLabel("P" + (index+1));
        numLbl.setFont(new Font("Segoe UI",Font.BOLD,13));
        numLbl.setForeground(AppTheme.PRIMARY);
        numLbl.setPreferredSize(new Dimension(30,36));

        JTextField nameF = AppTheme.styledField("Full Name");
        nameF.setPreferredSize(new Dimension(200,36));
        JTextField ageF = AppTheme.styledField("Age");
        ageF.setPreferredSize(new Dimension(60,36));
        JComboBox<String> genderC = AppTheme.styledCombo(new String[]{"Gender","Male","Female","Other"});
        genderC.setPreferredSize(new Dimension(100,36));

        passengerNames.add(nameF);
        passengerAges.add(ageF);
        passengerGenders.add(genderC);

        gbc.gridx=0; gbc.weightx=0; row.add(numLbl,gbc);
        gbc.gridx=1; gbc.weightx=1; row.add(nameF,gbc);
        gbc.gridx=2; gbc.weightx=0; row.add(ageF,gbc);
        gbc.gridx=3; row.add(genderC,gbc);

        return row;
    }

    // ─── Step 1: Seat Selection ────────────────────────────────
    private JPanel buildSeatSelectionStep() {
        JPanel panel = new JPanel(new BorderLayout(16,0));
        panel.setBackground(AppTheme.BG_LIGHT);

        JLabel title = new JLabel("Select Seats");
        title.setFont(AppTheme.FONT_HEADING);
        title.setForeground(AppTheme.TEXT_PRIMARY);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        legend.setOpaque(false);
        String[][] legendItems = {{"Available","#ffffff","#0d47a1"},{"Selected","#fffde7","#f9a825"},{"Booked","#ffebee","#c62828"},{"Window","#e8f5e9","#2e7d32"},{"Elder","#e3f2fd","#1565c0"},{"Disabled","#f3e5f5","#6a1b9a"}};
        for (String[] li : legendItems) {
            JLabel dot = new JLabel("⬛");
            try { dot.setForeground(Color.decode(li[2])); } catch(Exception ignored){}
            dot.setFont(new Font("Segoe UI",Font.PLAIN,14));
            JLabel lbl = new JLabel(li[0]);
            lbl.setFont(AppTheme.FONT_SMALL);
            lbl.setForeground(AppTheme.TEXT_SECONDARY);
            legend.add(dot); legend.add(lbl);
        }

        // Bus frame with seats
        JPanel busFront = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                // Steering wheel area
                g2.setColor(AppTheme.BG_LIGHT);
                g2.fillRoundRect(10,10,getWidth()-20,50,12,12);
                g2.setColor(AppTheme.BORDER_COLOR);
                g2.drawRoundRect(10,10,getWidth()-20,50,12,12);
                g2.setFont(new Font("Segoe UI",Font.PLAIN,20));
                g2.drawString("🚌  Driver",20,42);
                g2.dispose();
            }
        };
        busFront.setOpaque(false);
        busFront.setPreferredSize(new Dimension(0,70));

        JPanel seatGrid = new JPanel(new GridBagLayout());
        seatGrid.setOpaque(false);
        seatButtons = new JButton[TOTAL_ROWS][SEATS_PER_ROW];

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);

        // Column layout: gridx 0=col0, 1=col1, 2=AISLE, 3=col2, 4=col3
        // seat col 0 → gridx 0
        // seat col 1 → gridx 1
        // aisle      → gridx 2
        // seat col 2 → gridx 3
        // seat col 3 → gridx 4
        for (int row = 0; row < TOTAL_ROWS; row++) {
            // Add aisle label once per row
            gbc.gridx = 2; gbc.gridy = row;
            JLabel aisle = new JLabel("", SwingConstants.CENTER);
            aisle.setPreferredSize(new Dimension(24, 40));
            seatGrid.add(aisle, gbc);

            for (int col = 0; col < SEATS_PER_ROW; col++) {
                int gridX = col < 2 ? col : col + 1; // shift cols 2,3 right past aisle
                final int r = row, c = col;
                JButton seat = buildSeatButton(row, col);
                seatButtons[row][col] = seat;
                gbc.gridx = gridX; gbc.gridy = row;
                seatGrid.add(seat, gbc);
            }
        }

        JPanel seatPanel = new JPanel(new BorderLayout());
        seatPanel.setOpaque(false);
        seatPanel.add(busFront, BorderLayout.NORTH);
        seatPanel.add(seatGrid, BorderLayout.CENTER);

        seatPanel.setBorder(BorderFactory.createEmptyBorder(10,14,10,14)); // padding here instead
        JScrollPane seatScroll = new JScrollPane(seatPanel);
        seatScroll.setBorder(new AppTheme.RoundBorder(AppTheme.BORDER_COLOR,14));
        seatScroll.setBackground(Color.WHITE);
        seatScroll.getViewport().setBackground(Color.WHITE);

        // Selection info
        JPanel info = new JPanel(new BorderLayout(0,8));
        info.setOpaque(false);
        info.setPreferredSize(new Dimension(200,0));
        JLabel infoTitle = new JLabel("Your Selection");
        infoTitle.setFont(AppTheme.FONT_SUBHEAD);
        infoTitle.setForeground(AppTheme.TEXT_PRIMARY);

        JTextArea selDisplay = new JTextArea("No seats selected yet.");
        selDisplay.setFont(AppTheme.FONT_BODY);
        selDisplay.setEditable(false);
        selDisplay.setBackground(AppTheme.BG_LIGHT);
        selDisplay.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        selDisplay.setLineWrap(true);
        selDisplay.setWrapStyleWord(true);

        info.add(infoTitle, BorderLayout.NORTH);
        info.add(selDisplay, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(0,8));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(legend, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(seatScroll, BorderLayout.CENTER);
        panel.add(info, BorderLayout.EAST);
        return panel;
    }

    private JButton buildSeatButton(int row, int col) {
        int type = seatType[row][col];
        String seatNum = (char)('A'+row) + String.valueOf(col+1);

        Color bg, border, fg;
        String tooltip;
        boolean enabled = type != 5;

        switch(type) {
            case 1: bg=new Color(232,245,233); border=new Color(46,125,50); fg=new Color(46,125,50); tooltip="Window Seat"; break;
            case 2: bg=new Color(227,242,253); border=new Color(21,101,192); fg=new Color(21,101,192); tooltip="Elder/Senior Seat"; break;
            case 3: bg=new Color(243,229,245); border=new Color(106,27,154); fg=new Color(106,27,154); tooltip="Disabled-Accessible Seat"; break;
            case 5: bg=new Color(255,235,238); border=AppTheme.DANGER; fg=AppTheme.DANGER; tooltip="Already Booked"; break;
            default: bg=Color.WHITE; border=AppTheme.BORDER_COLOR; fg=AppTheme.TEXT_PRIMARY; tooltip="Standard Seat"; break;
        }

        JButton btn = new JButton(seatNum) {
            boolean sel = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgC = sel ? new Color(255,243,200) : bg;
                Color borderC = sel ? AppTheme.ACCENT : border;
                g2.setColor(bgC);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(borderC);
                g2.setStroke(new BasicStroke(sel?2:1));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.setColor(sel ? new Color(180,120,0) : fg);
                g2.setFont(new Font("Segoe UI",Font.BOLD,10));
                FontMetrics fm=g2.getFontMetrics();
                String t=getText();
                g2.drawString(t,(getWidth()-fm.stringWidth(t))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
            public void setSelected2(boolean s) { sel=s; repaint(); }
            public boolean isSelected2() { return sel; }
        };
        btn.setPreferredSize(new Dimension(46,40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setEnabled(enabled);
        btn.setToolTipText(tooltip + " — Seat " + seatNum);
        btn.setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

        if (enabled) {
            btn.addActionListener(e -> {
                try {
                    java.lang.reflect.Method m = btn.getClass().getMethod("isSelected2");
                    boolean curSel = (boolean)m.invoke(btn);
                    java.lang.reflect.Method sm = btn.getClass().getMethod("setSelected2", boolean.class);
                    int seatId = row * SEATS_PER_ROW + col;
                    if (curSel) {
                        sm.invoke(btn, false);
                        selectedSeats.remove((Integer)seatId);
                    } else if (selectedSeats.size() < numPassengers) {
                        sm.invoke(btn, true);
                        selectedSeats.add(seatId);
                    } else {
                        JOptionPane.showMessageDialog(this, "You can only select " + numPassengers + " seat(s).", "Seat Limit", JOptionPane.WARNING_MESSAGE);
                    }
                } catch(Exception ex) { ex.printStackTrace(); }
            });
        }
        return btn;
    }

    // ─── Step 2: Review & Pay ─────────────────────────────────
    private JPanel buildReviewStep() {
        JPanel panel = new JPanel(new BorderLayout(0,16));
        panel.setBackground(AppTheme.BG_LIGHT);

        JLabel title = new JLabel("Review & Confirm");
        title.setFont(AppTheme.FONT_HEADING);
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JPanel receipt = AppTheme.card(14);
        receipt.setLayout(new BoxLayout(receipt, BoxLayout.Y_AXIS));
        receipt.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));

        receipt.add(receiptRow("Bus", busData[1]));
        receipt.add(receiptRow("Route", busData[2]));
        receipt.add(receiptRow("Departure", busData[3]));
        receipt.add(receiptRow("Arrival", busData[4]));
        receipt.add(receiptSeparator());
        receipt.add(receiptRow("Passengers", String.valueOf(numPassengers)));
        receipt.add(receiptRow("Price per Seat", busData[6]));
        receipt.add(receiptSeparator());

        int priceNum = Integer.parseInt(busData[6].replace("₹","").replace(",",""));
        int total = priceNum * numPassengers;
        int gst = (int)(total * 0.05);
        int grand = total + gst;

        receipt.add(receiptRow("Subtotal", "₹" + total));
        receipt.add(receiptRow("GST (5%)", "₹" + gst));
        receipt.add(receiptSeparator());

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel totalLabel = new JLabel("TOTAL AMOUNT");
        totalLabel.setFont(new Font("Segoe UI",Font.BOLD,14));
        totalLabel.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel totalAmt = new JLabel("₹" + grand);
        totalAmt.setFont(new Font("Segoe UI",Font.BOLD,20));
        totalAmt.setForeground(AppTheme.SUCCESS);
        totalRow.add(totalLabel, BorderLayout.WEST);
        totalRow.add(totalAmt, BorderLayout.EAST);
        receipt.add(totalRow);
        receipt.add(Box.createVerticalStrut(16));

        // Payment method
        receipt.add(receiptSectionHeader("Payment Method"));
        JPanel payRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        payRow.setOpaque(false);
        String[] payModes = {"💳 Card","📱 UPI","🏦 Net Banking","💵 Cash"};
        ButtonGroup bg = new ButtonGroup();
        for (String pm : payModes) {
            JRadioButton rb = new JRadioButton(pm);
            rb.setFont(AppTheme.FONT_BODY);
            rb.setForeground(AppTheme.TEXT_PRIMARY);
            rb.setOpaque(false);
            bg.add(rb); payRow.add(rb);
            if (pm.contains("Card")) rb.setSelected(true);
        }
        receipt.add(payRow);

        JScrollPane scroll = new JScrollPane(receipt);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel receiptRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(4,0,4,0));
        JLabel l = new JLabel(label);
        l.setFont(AppTheme.FONT_BODY);
        l.setForeground(AppTheme.TEXT_SECONDARY);
        JLabel v = new JLabel(value);
        v.setFont(AppTheme.FONT_BOLD);
        v.setForeground(AppTheme.TEXT_PRIMARY);
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private JSeparator receiptSeparator() {
        JSeparator s = new JSeparator();
        s.setForeground(AppTheme.BORDER_COLOR);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        return s;
    }

    private JLabel receiptSectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppTheme.FONT_BOLD);
        l.setForeground(AppTheme.TEXT_SECONDARY);
        l.setBorder(BorderFactory.createEmptyBorder(8,0,4,0));
        return l;
    }

    // ─── Save Booking to Database ─────────────────────────────
    private boolean saveBookingToDB() {
        try {
            // busData[0] = scheduleId (numeric), busData[6] = price like "₹850"
            int scheduleId;
            try {
                scheduleId = Integer.parseInt(busData[0].trim());
            } catch (NumberFormatException ex) {
                System.err.println("[BookingWizard] busData[0] is not a valid scheduleId: " + busData[0]);
                System.err.println("  → Defaulting to scheduleId=1. Fix: busData[0] must be the numeric schedule ID.");
                scheduleId = 1; // fallback for demo/sample cards
            }

            // Strip ₹ symbol and commas from price string
            String priceStr = busData[6].replaceAll("[₹,\\s]", "").trim();
            double price    = Double.parseDouble(priceStr);
            double subtotal = price * numPassengers;
            double gst      = Math.round(subtotal * 0.05 * 100.0) / 100.0;
            double total    = subtotal + gst;

            model.Booking booking = new model.Booking();
            booking.setUserId(userId);
            booking.setScheduleId(scheduleId);
            booking.setJourneyDate(new java.sql.Date(System.currentTimeMillis()));
            booking.setNumPassengers(numPassengers);
            booking.setSubtotal(subtotal);
            booking.setGstAmount(gst);
            booking.setTotalAmount(total);
            booking.setPaymentMethod("Card");
            booking.setStatus("Confirmed");

            // Add passenger objects with seat assignments
            char[] seatRows = "ABCDEFGHIJ".toCharArray();
            for (int i = 0; i < numPassengers; i++) {
                String name   = (i < passengerNames.size())   ? passengerNames.get(i).getText().trim()   : "";
                String ageStr = (i < passengerAges.size())    ? passengerAges.get(i).getText().trim()    : "";
                String gender = (i < passengerGenders.size()) ? (String)passengerGenders.get(i).getSelectedItem() : "Male";

                if (name.isEmpty())   name   = "Passenger " + (i + 1);
                if ("Gender".equals(gender)) gender = "Male";
                int age = 25;
                try { if (!ageStr.isEmpty()) age = Integer.parseInt(ageStr); } catch (NumberFormatException ignored) {}

                String seat;
                if (selectedSeats.size() > i) {
                    int seatIdx = selectedSeats.get(i);
                    seat = String.valueOf(seatRows[seatIdx / 4]) + ((seatIdx % 4) + 1);
                } else {
                    seat = String.valueOf(seatRows[i]) + "1"; // fallback: A1, B1, C1...
                }
                booking.addPassenger(new model.Passenger(name, age, gender, seat));
            }

            dao.BookingDAO bookingDAO = new dao.BookingDAO();
            int id = bookingDAO.createBooking(booking);
            if (id > 0) {
                confirmedRef = booking.getBookingRef();
                System.out.println("[BookingWizard] Booking saved: " + confirmedRef);
                return true;
            } else {
                System.err.println("[BookingWizard] createBooking returned -1 (DB insert failed)");
            }
        } catch (Exception ex) {
            System.err.println("[BookingWizard] saveBookingToDB exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    // ─── Step 3: Confirmation ──────────────────────────────────
    private JPanel buildConfirmationStep() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.BG_LIGHT);

        JPanel card = AppTheme.card(20);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(30,40,30,40));

        JLabel icon = new JLabel("✅") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(232,245,233));
                g2.fillOval(0,0,getWidth(),getHeight());
                g2.setFont(new Font("Segoe UI Emoji",Font.PLAIN,38));
                g2.drawString("✅",8,46);
                g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(64,64));
        icon.setMaximumSize(new Dimension(64,64));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel conf = new JLabel("Booking Confirmed!");
        conf.setFont(new Font("Segoe UI",Font.BOLD,22));
        conf.setForeground(AppTheme.SUCCESS);
        conf.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel bid = new JLabel("Booking ID: BK-" + (int)(Math.random()*90000+10000));
        bid.setFont(AppTheme.FONT_BODY);
        bid.setForeground(AppTheme.TEXT_SECONDARY);
        bid.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subL = new JLabel("Your ticket has been booked successfully.");
        subL.setFont(AppTheme.FONT_BODY);
        subL.setForeground(AppTheme.TEXT_SECONDARY);
        subL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton printBtn = AppTheme.primaryButton("🖨  Print / Download Receipt");
        printBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        printBtn.addActionListener(e -> {
            // Generate and print receipt
            StringBuilder sb = new StringBuilder();
            sb.append("==============================\n");
            sb.append("      BusGo Express Receipt    \n");
            sb.append("==============================\n");
            sb.append("Booking Ref: ").append(confirmedRef).append("\n");
            sb.append("Bus: ").append(busData[1]).append("\n");
            sb.append("Route: ").append(busData[2]).append("\n");
            sb.append("Departure: ").append(busData[3]).append("\n");
            sb.append("Passengers: ").append(numPassengers).append("\n");
            sb.append("Total: ").append(busData[6]).append(" x ").append(numPassengers).append("\n");
            sb.append("==============================\n");
            JTextArea ta = new JTextArea(sb.toString());
            ta.setFont(new java.awt.Font("Monospaced",java.awt.Font.PLAIN,12));
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Receipt — " + confirmedRef, JOptionPane.PLAIN_MESSAGE);
        });

        card.add(icon);
        card.add(Box.createVerticalStrut(16));
        card.add(conf);
        card.add(Box.createVerticalStrut(6));
        card.add(bid);
        card.add(Box.createVerticalStrut(8));
        card.add(subL);
        card.add(Box.createVerticalStrut(24));
        card.add(printBtn);

        panel.add(card);
        return panel;
    }
}