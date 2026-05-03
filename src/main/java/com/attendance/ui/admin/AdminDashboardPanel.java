package com.attendance.ui.admin;

import com.attendance.dao.*;
import com.attendance.model.User;
import com.attendance.service.SystemConfigService;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class AdminDashboardPanel extends JPanel {

    private final UserDAO userDAO             = new UserDAO();
    private final ClassRoomDAO classDAO       = new ClassRoomDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final SystemConfigService config  = new SystemConfigService();

    private JLabel studCount, studSub;
    private JLabel teachCount, teachSub;
    private JLabel classCount, classSub;
    private JLabel lowCount, lowSub;
    private String adminName = "Admin";

    public AdminDashboardPanel() { this(null); }

    public AdminDashboardPanel(User user) {
        if (user != null && user.getFullName() != null && !user.getFullName().isBlank())
            adminName = user.getFullName();

        setLayout(new BorderLayout());
        setBackground(UIUtil.BG);

        // All content in a scrollable page
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(UIUtil.BG);
        page.setBorder(new EmptyBorder(24, 24, 24, 24));

        // ── 1. Header ─────────────────────────────────────────────────────────
        String today = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel hLeft = new JPanel();
        hLeft.setLayout(new BoxLayout(hLeft, BoxLayout.Y_AXIS));
        hLeft.setOpaque(false);

        JLabel welcome = new JLabel("Welcome back, " + adminName + "!");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcome.setForeground(UIUtil.TEXT_PRIMARY);

        JLabel dateLbl = new JLabel("Overview of system-wide attendance for " + today);
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLbl.setForeground(UIUtil.TEXT_SECONDARY);

        hLeft.add(welcome);
        hLeft.add(Box.createVerticalStrut(3));
        hLeft.add(dateLbl);

        JButton refreshBtn = UIUtil.primaryButton("Refresh");
        JPanel hRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        hRight.setOpaque(false);
        hRight.add(refreshBtn);

        header.add(hLeft,  BorderLayout.WEST);
        header.add(hRight, BorderLayout.EAST);
        page.add(header);
        page.add(Box.createVerticalStrut(20));

        // ── 2. KPI cards — 4 in one row, no emoji ────────────────────────────
        studCount  = new JLabel("—"); studSub  = new JLabel("—");
        teachCount = new JLabel("—"); teachSub = new JLabel("—");
        classCount = new JLabel("—"); classSub = new JLabel("—");
        lowCount   = new JLabel("—"); lowSub   = new JLabel("—");

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);
        kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        kpiRow.add(kpiCard("Total Students",       studCount,  studSub,
            new Color(59, 130, 246),  new Color(191, 219, 254), "icons/icons8-student-94.png"));
        kpiRow.add(kpiCard("Total Teachers",        teachCount, teachSub,
            new Color(16, 185, 129),  new Color(167, 243, 208), "icons/icons8-teacher-48.png"));
        kpiRow.add(kpiCard("Total Classes",         classCount, classSub,
            new Color(139, 92, 246),  new Color(196, 181, 253), "icons/icons8-calendar-64.png"));
        kpiRow.add(kpiCard("Low Attendance",        lowCount,   lowSub,
            new Color(239, 68, 68),   new Color(252, 165, 165), "icons/icons8-warning-48.png"));

        page.add(kpiRow);
        page.add(Box.createVerticalStrut(18));

        // ── 3. System Info + Low Attendance table ─────────────────────────────
        JPanel midRow = new JPanel(new GridLayout(1, 2, 12, 0));
        midRow.setOpaque(false);
        midRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // System info card — compact, no wasted space
        JPanel infoCard = sectionCard("System Information", UIUtil.PRIMARY);
        JPanel infoBody = new JPanel(new GridLayout(4, 1, 0, 0));
        infoBody.setOpaque(false);

        infoBody.add(compactInfoRow("Academic Year",    config.getAcademicYear(),                    null,  null));
        infoBody.add(compactInfoRow("Current Semester", "Semester " + config.getCurrentSemester(),   null,  null));
        infoBody.add(compactInfoRow("Working Days/Week",config.get("working_days", "5") + " days",   null,  null));

        // Threshold row — thin progress bar inline
        double threshold = config.getAttendanceThreshold();
        Color thrColor = threshold >= 75 ? new Color(16, 185, 129) : new Color(239, 68, 68);
        infoBody.add(compactInfoRow("Attendance Threshold", (int) threshold + "%", threshold, thrColor));

        infoCard.add(infoBody, BorderLayout.CENTER);
        midRow.add(infoCard);

        // Low attendance table
        String[] lowCols = {"Student", "Code", "Attendance %"};
        DefaultTableModel lowModel = new DefaultTableModel(lowCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable lowTable = new JTable(lowModel);
        lowTable.setRowHeight(32);
        UIUtil.styleTable(lowTable);
        lowTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!sel) {
                    setForeground(UIUtil.DANGER);
                    setBackground(row % 2 == 0 ? new Color(255, 245, 245) : new Color(255, 238, 238));
                }
                return this;
            }
        });

        JComboBox<String> classFilterCombo = new JComboBox<>();
        classFilterCombo.setFont(UIUtil.FONT_SMALL);
        classFilterCombo.addItem("All Classes");
        try {
            for (com.attendance.model.ClassRoom cl : new ClassRoomDAO().findAll())
                classFilterCombo.addItem(cl.getName());
        } catch (Exception ignored) {}

        JPanel lowCard = sectionCard("Students Below Threshold", new Color(239, 68, 68));
        JPanel lowTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        lowTop.setOpaque(false);
        JLabel fLbl = new JLabel("Filter by class:");
        fLbl.setFont(UIUtil.FONT_SMALL);
        fLbl.setForeground(UIUtil.TEXT_SECONDARY);
        lowTop.add(fLbl);
        lowTop.add(classFilterCombo);
        lowCard.add(lowTop, BorderLayout.NORTH);
        lowCard.add(UIUtil.scrollPane(lowTable), BorderLayout.CENTER);
        midRow.add(lowCard);

        page.add(midRow);
        page.add(Box.createVerticalStrut(14));

        // ── 4. Quick Actions __
        JPanel actCard = sectionCard("Quick Actions", new Color(34, 197, 94));
        actCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel actRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        actRow.setOpaque(false);
        JButton notifyBtn = UIUtil.dangerButton("Notify Low Attendance Students");
        actRow.add(notifyBtn);
        actCard.add(actRow, BorderLayout.CENTER);
        page.add(actCard);
        page.add(Box.createVerticalStrut(8));

        // ── Scroll wrapper ────────────────────────────────────────────────────
        add(UIUtil.fastScrollPane(page, UIUtil.BG), BorderLayout.CENTER);

        // ── Wire ─────────────────────────────────────────────────────────────
        loadStats(lowModel, classFilterCombo);
        refreshBtn.addActionListener(e -> loadStats(lowModel, classFilterCombo));
        notifyBtn.addActionListener(e -> sendLowAttendanceNotifications());
        classFilterCombo.addActionListener(e -> loadStats(lowModel, classFilterCombo));
    }

    // ── KPI Card — per prompt spec ────────────────────────────────────────────
    // Top colored border, icon circle left, large number, label, Live/Healthy badge
    private JPanel kpiCard(String title, JLabel numLbl, JLabel subLbl,
                            Color accent, Color tint, String iconPath) {

        // Outer card — white, 12px radius, subtle shadow, colored TOP border
        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 3, getWidth() - 2, getHeight() - 2, 12, 12);
                // White card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                // TOP accent border (4px)
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth() - 2, 4, 4, 4);
                g2.fillRect(0, 2, getWidth() - 2, 2);
                // Outer border
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 16, 14, 16));

        // ── Icon circle (48x48 perfect square → true circle) ─────────────────
        JPanel iconWrap = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Perfect circle using the smaller dimension
                int size = Math.min(getWidth(), getHeight());
                int x = (getWidth()  - size) / 2;
                int y = (getHeight() - size) / 2;
                g2.setColor(tint);
                g2.fillOval(x, y, size, size);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconWrap.setOpaque(false);
        iconWrap.setPreferredSize(new Dimension(52, 52));
        iconWrap.setMinimumSize(new Dimension(52, 52));
        iconWrap.setMaximumSize(new Dimension(52, 52));

        JLabel iconLbl = new JLabel("", SwingConstants.CENTER);
        iconLbl.setOpaque(false);

        // Load icon (28px inside 52px circle)
        ImageIcon icon = UIUtil.loadIcon(iconPath.replace("icons/", ""), 28);
        if (icon != null) {
            iconLbl.setIcon(icon);
        } else {
            iconLbl.setText(title.substring(0, 1));
            iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
            iconLbl.setForeground(accent);
        }
        iconWrap.add(iconLbl);

        // ── Right: title / number / sub-label / badge ─────────────────────────
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(0, 14, 0, 0));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;

        // Title
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLbl.setForeground(UIUtil.TEXT_SECONDARY);
        gc.gridy = 0; gc.insets = new Insets(0, 0, 2, 0);
        right.add(titleLbl, gc);

        // Big number (32px bold)
        numLbl.setFont(new Font("Segoe UI", Font.BOLD, 32));
        numLbl.setForeground(UIUtil.TEXT_PRIMARY);
        gc.gridy = 1; gc.insets = new Insets(0, 0, 0, 0);
        right.add(numLbl, gc);

        // Sub-label (12px gray)
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(UIUtil.TEXT_SECONDARY);
        gc.gridy = 2; gc.insets = new Insets(1, 0, 6, 0);
        right.add(subLbl, gc);

        // Status badge — "Live" green dot
        JPanel badge = buildBadge("Live", new Color(16, 185, 129));
        gc.gridy = 3; gc.insets = new Insets(0, 0, 0, 0);
        right.add(badge, gc);

        card.add(iconWrap, BorderLayout.WEST);
        card.add(right,    BorderLayout.CENTER);
        return card;
    }

    /** Builds a small "Live" or "Healthy" status badge with a colored dot */
    private JPanel buildBadge(String text, Color color) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        badge.setOpaque(false);

        // Dot
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(8, 8));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(color);

        badge.add(dot);
        badge.add(lbl);
        return badge;
    }

    // ── Section card ──────────────────────────────────────────────────────────
    private JPanel sectionCard(String title, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(4, 16));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(UIUtil.TEXT_PRIMARY);

        titleRow.add(bar,      BorderLayout.WEST);
        titleRow.add(titleLbl, BorderLayout.CENTER);
        card.add(titleRow, BorderLayout.NORTH);
        return card;
    }

    // ── Compact info row — fixed height, no wasted space ─────────────────────
    // If progressValue is non-null, shows a thin 6px progress bar instead of just text
    private JPanel compactInfoRow(String label, String valueText,
                                   Double progressValue, Color progressColor) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(9, 2, 9, 2));

        // Label
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(UIUtil.TEXT_SECONDARY);

        // Right side: either plain bold value OR progress bar + value
        JPanel right = new JPanel(new BorderLayout(8, 0));
        right.setOpaque(false);

        JLabel val = new JLabel(valueText);
        val.setFont(new Font("Segoe UI", Font.BOLD, 12));
        val.setForeground(UIUtil.TEXT_PRIMARY);
        val.setHorizontalAlignment(SwingConstants.RIGHT);

        if (progressValue != null && progressColor != null) {
            // Thin horizontal progress bar — fixed 6px height
            JProgressBar bar = new JProgressBar(0, 100) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    // Track
                    g2.setColor(new Color(226, 232, 240));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    // Fill
                    int fillW = (int) (getWidth() * getValue() / 100.0);
                    if (fillW > 0) {
                        g2.setColor(progressColor);
                        g2.fillRoundRect(0, 0, fillW, getHeight(), getHeight(), getHeight());
                    }
                    g2.dispose();
                }
            };
            bar.setValue(progressValue.intValue());
            bar.setStringPainted(false);
            bar.setBorderPainted(false);
            bar.setOpaque(false);
            bar.setPreferredSize(new Dimension(100, 6));
            bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));

            // Status chip next to value
            JPanel chip = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = new Color(progressColor.getRed(),
                        progressColor.getGreen(), progressColor.getBlue(), 20);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        getHeight(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            chip.setLayout(new BorderLayout());
            chip.setOpaque(false);
            chip.setBorder(new EmptyBorder(1, 8, 1, 8));
            JLabel chipLbl = new JLabel(valueText);
            chipLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            chipLbl.setForeground(progressColor);
            chip.add(chipLbl, BorderLayout.CENTER);

            JPanel barStack = new JPanel();
            barStack.setLayout(new BoxLayout(barStack, BoxLayout.Y_AXIS));
            barStack.setOpaque(false);
            barStack.add(bar);

            right.add(barStack, BorderLayout.CENTER);
            right.add(chip,     BorderLayout.EAST);
        } else {
            right.add(val, BorderLayout.EAST);
        }

        row.add(lbl,   BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(241, 245, 249));
        sep.setBackground(new Color(241, 245, 249));

        wrapper.add(row, BorderLayout.CENTER);
        wrapper.add(sep, BorderLayout.SOUTH);
        return wrapper;
    }

    // ── Data loading ──────────────────────────────────────────────────────────
    private void loadStats(DefaultTableModel lowModel, JComboBox<String> classFilterCombo) {
        studCount.setText("—");  studSub.setText("loading...");
        teachCount.setText("—"); teachSub.setText("loading...");
        classCount.setText("—"); classSub.setText("loading...");
        lowCount.setText("—");   lowSub.setText("loading...");
        lowModel.setRowCount(0);

        String selectedClass = (String) classFilterCombo.getSelectedItem();
        boolean filterAll = selectedClass == null || "All Classes".equals(selectedClass);

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                int students  = userDAO.countByRole("STUDENT");
                int teachers  = userDAO.countByRole("TEACHER");
                int classes   = classDAO.count();
                double thr    = config.getAttendanceThreshold();
                List<Object[]> lowList = attendanceDAO.getLowAttendanceStudents(thr);

                int pending = 0;
                try (java.sql.Connection c =
                        com.attendance.config.DatabaseConfig.getConnection();
                     java.sql.PreparedStatement ps = c.prepareStatement(
                        "SELECT COUNT(DISTINCT student_id) FROM enrollments")) {
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next()) pending = Math.max(0, students - rs.getInt(1));
                } catch (Exception ignored) {}
                final int pend = pending;

                List<Object[]> filtered = new java.util.ArrayList<>();
                if (filterAll) {
                    filtered = lowList;
                } else {
                    Set<String> codes = new java.util.HashSet<>();
                    try {
                        for (com.attendance.model.ClassRoom cl : new ClassRoomDAO().findAll()) {
                            if (cl.getName().equals(selectedClass)) {
                                for (com.attendance.model.Student s :
                                        new StudentDAO().findByClass(cl.getId()))
                                    codes.add(s.getStudentCode());
                                break;
                            }
                        }
                    } catch (Exception ignored) {}
                    for (Object[] row : lowList)
                        if (codes.contains(row[1])) filtered.add(row);
                }

                final List<Object[]> fin = filtered;
                final int lowTotal = lowList.size();

                SwingUtilities.invokeLater(() -> {
                    studCount.setText(String.format("%,d", students));
                    studSub.setText("enrolled");

                    teachCount.setText(String.format("%,d", teachers));
                    teachSub.setText("active");

                    classCount.setText(String.format("%,d", classes));
                    classSub.setText(pend > 0 ? pend + " pending enrollments" : "active");

                    lowCount.setText(String.valueOf(lowTotal));
                    lowSub.setText("below " + (int) thr + "% threshold");
                    lowSub.setForeground(lowTotal > 0 ? UIUtil.DANGER : UIUtil.SUCCESS);

                    for (Object[] row : fin)
                        lowModel.addRow(new Object[]{
                            row[0], row[1], String.format("%.1f%%", row[4])});
                });
                return null;
            }
        }.execute();
    }

    private void sendLowAttendanceNotifications() {
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception {
                double thr = config.getAttendanceThreshold();
                List<Object[]> lowList = attendanceDAO.getLowAttendanceStudents(thr);
                com.attendance.service.NotificationService notifSvc =
                    new com.attendance.service.NotificationService();
                com.attendance.dao.StudentDAO studentDAO =
                    new com.attendance.dao.StudentDAO();
                java.util.Map<String, Long> map = new java.util.HashMap<>();
                for (var s : studentDAO.findAll()) map.put(s.getStudentCode(), s.getUserId());
                int sent = 0;
                for (Object[] row : lowList) {
                    Long uid = map.get((String) row[1]);
                    if (uid != null) {
                        notifSvc.send(uid,
                            String.format("Your attendance is %.1f%% - below the %.0f%% required threshold.",
                                (double) row[4], thr),
                            "LOW_ATTENDANCE");
                        sent++;
                    }
                }
                return sent;
            }
            @Override protected void done() {
                try {
                    UIUtil.showSuccess(AdminDashboardPanel.this, get() + " notification(s) sent.");
                } catch (Exception ex) {
                    UIUtil.showError(AdminDashboardPanel.this, ex.getMessage());
                }
            }
        }.execute();
    }
}
