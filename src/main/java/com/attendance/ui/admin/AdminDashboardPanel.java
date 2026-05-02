package com.attendance.ui.admin;

import com.attendance.dao.*;
import com.attendance.service.SystemConfigService;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Set;

public class AdminDashboardPanel extends JPanel {

    private final UserDAO userDAO             = new UserDAO();
    private final ClassRoomDAO classDAO       = new ClassRoomDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final SystemConfigService config  = new SystemConfigService();

    private JLabel studCount, teachCount, classCount, lowCount;

    public AdminDashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = UIUtil.headerLabel("Admin Dashboard");
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // ---- Stat cards ----
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        statsRow.setOpaque(false);
        studCount  = new JLabel("...");
        teachCount = new JLabel("...");
        classCount = new JLabel("...");
        lowCount   = new JLabel("...");
        statsRow.add(buildStatCard("Total Students",  studCount,  new Color(63, 81, 181)));
        statsRow.add(buildStatCard("Total Teachers",  teachCount, new Color(0, 150, 136)));
        statsRow.add(buildStatCard("Total Classes",   classCount, new Color(156, 39, 176)));
        statsRow.add(buildStatCard("Low Attendance",  lowCount,   new Color(244, 67, 54)));
        center.add(statsRow);
        center.add(Box.createVerticalStrut(20));

        // ---- System info + Low attendance table side by side ----
        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false);

        // System info card
        JPanel infoCard = UIUtil.card("System Information");
        JPanel infoGrid = new JPanel(new GridLayout(0, 2, 10, 8));
        infoGrid.setOpaque(false);
        infoGrid.add(UIUtil.boldLabel("Academic Year:"));
        infoGrid.add(UIUtil.label(config.getAcademicYear()));
        infoGrid.add(UIUtil.boldLabel("Current Semester:"));
        infoGrid.add(UIUtil.label(String.valueOf(config.getCurrentSemester())));
        infoGrid.add(UIUtil.boldLabel("Attendance Threshold:"));
        infoGrid.add(UIUtil.label(config.get("attendance_threshold", "75") + "%"));
        infoGrid.add(UIUtil.boldLabel("Working Days/Week:"));
        infoGrid.add(UIUtil.label(config.get("working_days", "5")));
        infoCard.add(infoGrid, BorderLayout.CENTER);
        row2.add(infoCard);

        // Low attendance students card
        JPanel lowCard = UIUtil.card("Students Below Threshold");
        String[] lowCols = {"Student", "Code", "Attendance %"};
        DefaultTableModel lowModel = new DefaultTableModel(lowCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable lowTable = new JTable(lowModel);
        UIUtil.styleTable(lowTable);

        // Class filter for the low-attendance table
        JComboBox<String> classFilterCombo = new JComboBox<>();
        classFilterCombo.setFont(UIUtil.FONT_SMALL);
        classFilterCombo.addItem("All Classes");
        try {
            for (com.attendance.model.ClassRoom cl : new com.attendance.dao.ClassRoomDAO().findAll())
                classFilterCombo.addItem(cl.getName());
        } catch (Exception ignored) {}

        JPanel lowHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        lowHeader.setOpaque(false);
        lowHeader.add(UIUtil.label("Filter by class:"));
        lowHeader.add(classFilterCombo);
        lowCard.add(lowHeader, BorderLayout.NORTH);
        lowCard.add(UIUtil.scrollPane(lowTable), BorderLayout.CENTER);
        row2.add(lowCard);

        center.add(row2);
        center.add(Box.createVerticalStrut(16));

        // ---- Recent activity / quick actions ----
        JPanel actionsCard = UIUtil.card("Quick Actions");
        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        actionsRow.setOpaque(false);

        JButton refreshBtn = UIUtil.primaryButton("↺ Refresh Stats");
        JButton notifyBtn  = UIUtil.dangerButton("📢 Notify Low Attendance Students");

        actionsRow.add(refreshBtn);
        actionsRow.add(notifyBtn);
        actionsCard.add(actionsRow, BorderLayout.CENTER);
        center.add(actionsCard);

        add(center, BorderLayout.CENTER);

        // Load data
        loadStats(lowModel, classFilterCombo);

        refreshBtn.addActionListener(e -> loadStats(lowModel, classFilterCombo));
        notifyBtn.addActionListener(e -> sendLowAttendanceNotifications());
        classFilterCombo.addActionListener(e -> loadStats(lowModel, classFilterCombo));
    }

    private JPanel buildStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setPreferredSize(new Dimension(185, 105));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(Color.WHITE);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(255, 255, 255, 200));
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLbl, BorderLayout.SOUTH);
        return card;
    }

    private void loadStats(DefaultTableModel lowModel, JComboBox<String> classFilterCombo) {
        studCount.setText("...");
        teachCount.setText("...");
        classCount.setText("...");
        lowCount.setText("...");
        lowModel.setRowCount(0);

        String selectedClass = (String) classFilterCombo.getSelectedItem();
        boolean filterAll = selectedClass == null || "All Classes".equals(selectedClass);

        SwingWorker<Void, Void> w = new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                int students = userDAO.countByRole("STUDENT");
                int teachers = userDAO.countByRole("TEACHER");
                int classes  = classDAO.count();
                double threshold = config.getAttendanceThreshold();
                List<Object[]> lowList = attendanceDAO.getLowAttendanceStudents(threshold);

                // If a specific class is selected, filter the low list by class name
                List<Object[]> filtered = new java.util.ArrayList<>();
                if (filterAll) {
                    filtered = lowList;
                } else {
                    // Get student IDs enrolled in the selected class
                    Set<Long> classStudentIds = new java.util.HashSet<>();
                    try {
                        for (com.attendance.model.ClassRoom cl : new com.attendance.dao.ClassRoomDAO().findAll()) {
                            if (cl.getName().equals(selectedClass)) {
                                for (com.attendance.model.Student s :
                                        new com.attendance.dao.StudentDAO().findByClass(cl.getId()))
                                    classStudentIds.add(s.getUserId());
                                break;
                            }
                        }
                    } catch (Exception ignored) {}
                    // row[2] = email — we match by userId via student code lookup
                    // Simpler: filter by checking if student code is in the class
                    Set<String> classCodes = new java.util.HashSet<>();
                    try {
                        for (com.attendance.model.ClassRoom cl : new com.attendance.dao.ClassRoomDAO().findAll()) {
                            if (cl.getName().equals(selectedClass)) {
                                for (com.attendance.model.Student s :
                                        new com.attendance.dao.StudentDAO().findByClass(cl.getId()))
                                    classCodes.add(s.getStudentCode());
                                break;
                            }
                        }
                    } catch (Exception ignored) {}
                    for (Object[] row : lowList) {
                        if (classCodes.contains(row[1])) filtered.add(row);
                    }
                }

                final List<Object[]> finalFiltered = filtered;
                SwingUtilities.invokeLater(() -> {
                    studCount.setText(String.valueOf(students));
                    teachCount.setText(String.valueOf(teachers));
                    classCount.setText(String.valueOf(classes));
                    lowCount.setText(String.valueOf(lowList.size())); // total, not filtered
                    for (Object[] row : finalFiltered)
                        lowModel.addRow(new Object[]{row[0], row[1],
                            String.format("%.1f%%", row[4])});
                });
                return null;
            }
        };
        w.execute();
    }

    private void sendLowAttendanceNotifications() {
        SwingWorker<Integer, Void> w = new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception {
                double threshold = config.getAttendanceThreshold();
                List<Object[]> lowList = attendanceDAO.getLowAttendanceStudents(threshold);
                com.attendance.service.NotificationService notifSvc = new com.attendance.service.NotificationService();
                com.attendance.dao.StudentDAO studentDAO = new com.attendance.dao.StudentDAO();
                // Load all students once, build a code→userId map
                java.util.Map<String, Long> codeToUserId = new java.util.HashMap<>();
                for (var s : studentDAO.findAll()) codeToUserId.put(s.getStudentCode(), s.getUserId());
                int sent = 0;
                for (Object[] row : lowList) {
                    String code = (String) row[1];
                    double pct  = (double) row[4];
                    Long userId = codeToUserId.get(code);
                    if (userId != null) {
                        notifSvc.send(userId,
                            String.format("⚠ Your attendance is %.1f%% — below the %.0f%% required threshold.", pct, threshold),
                            "LOW_ATTENDANCE");
                        sent++;
                    }
                }
                return sent;
            }
            @Override protected void done() {
                try {
                    int sent = get();
                    UIUtil.showSuccess(AdminDashboardPanel.this,
                        sent + " notification(s) sent to students with low attendance.");
                } catch (Exception ex) {
                    UIUtil.showError(AdminDashboardPanel.this, ex.getMessage());
                }
            }
        };
        w.execute();
    }
}
