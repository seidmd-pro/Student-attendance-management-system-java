package com.attendance.ui.admin;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.ClassCourseDAO;
import com.attendance.model.ClassCourse;
import com.attendance.service.SystemConfigService;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminReportPanel extends JPanel {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ClassCourseDAO csDAO        = new ClassCourseDAO();
    private final SystemConfigService config  = new SystemConfigService();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Class-subject filter
    private JComboBox<String> csCombo;
    private List<ClassCourse> csList = new ArrayList<>();

    // Date range controls
    private JComboBox<String> presetCombo;   // quick presets
    private JSpinner fromSpinner, toSpinner;
    private JLabel activeRangeLabel;

    // Table
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel;

    public AdminReportPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = UIUtil.headerLabel("Attendance Reports");
        title.setBorder(new EmptyBorder(0, 0, 14, 0));
        add(title, BorderLayout.NORTH);

        // ---- Table (created first so exportBtn can reference it) ----
        String[] cols = {"Student Name", "Student Code", "Total Sessions", "Present", "Absent", "Late", "Percentage %"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        reportTable = new JTable(tableModel);
        UIUtil.styleTable(reportTable);
        reportTable.setRowHeight(30);

        reportTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(UIUtil.FONT_BOLD);
                if (!sel && val != null) {
                    try {
                        double pct = Double.parseDouble(val.toString());
                        double threshold = config.getAttendanceThreshold();
                        if (pct < threshold) {
                            setForeground(UIUtil.DANGER);
                            setBackground(new Color(255, 235, 235));
                        } else {
                            setForeground(UIUtil.SUCCESS);
                            setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 255));
                        }
                    } catch (NumberFormatException ignored) {}
                }
                return this;
            }
        });

        // ---- Filter panel (two rows) ----
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setOpaque(false);
        filterPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Row 1: Class/Subject + action buttons
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.setOpaque(false);

        csCombo = new JComboBox<>();
        csCombo.setFont(UIUtil.FONT_BODY);
        csCombo.setPreferredSize(new Dimension(300, 34));

        JButton generateBtn = UIUtil.primaryButton("Generate");
        JButton lowBtn      = UIUtil.dangerButton("Low Attendance");
        JButton refreshBtn  = UIUtil.secondaryButton("↺ Refresh");
        JButton exportBtn   = UIUtil.exportButton(this, reportTable, "attendance_report");

        row1.add(UIUtil.boldLabel("Class / Subject:"));
        row1.add(csCombo);
        row1.add(generateBtn);
        row1.add(lowBtn);
        row1.add(refreshBtn);
        row1.add(Box.createHorizontalStrut(6));
        row1.add(exportBtn);

        // Row 2: Date range filter
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row2.setOpaque(false);

        // Preset dropdown
        presetCombo = new JComboBox<>(buildPresets());
        presetCombo.setFont(UIUtil.FONT_BODY);
        presetCombo.setPreferredSize(new Dimension(200, 32));

        // Custom date spinners
        fromSpinner = makeDateSpinner();
        toSpinner   = makeDateSpinner();
        fromSpinner.setPreferredSize(new Dimension(120, 32));
        toSpinner.setPreferredSize(new Dimension(120, 32));

        JButton applyRangeBtn = UIUtil.secondaryButton("Apply");
        applyRangeBtn.setFont(UIUtil.FONT_SMALL);
        applyRangeBtn.setBorder(new EmptyBorder(4, 10, 4, 10));

        activeRangeLabel = new JLabel("All Time");
        activeRangeLabel.setFont(UIUtil.FONT_SMALL);
        activeRangeLabel.setForeground(UIUtil.PRIMARY);
        activeRangeLabel.setBorder(new EmptyBorder(0, 6, 0, 0));

        row2.add(UIUtil.boldLabel("Date Range:"));
        row2.add(presetCombo);
        row2.add(UIUtil.label("  or custom:  From"));
        row2.add(fromSpinner);
        row2.add(UIUtil.label("To"));
        row2.add(toSpinner);
        row2.add(applyRangeBtn);
        row2.add(activeRangeLabel);

        filterPanel.add(row1);
        filterPanel.add(row2);

        // ---- Summary label ----
        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(UIUtil.FONT_SMALL);
        summaryLabel.setForeground(UIUtil.TEXT_SECONDARY);
        summaryLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        // ---- Layout ----
        JPanel center = new JPanel(new BorderLayout(0, 4));
        center.setOpaque(false);
        center.add(filterPanel, BorderLayout.NORTH);
        center.add(UIUtil.scrollPane(reportTable), BorderLayout.CENTER);
        center.add(summaryLabel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // ---- Init ----
        loadCombo();
        applyPreset(); // set spinners to "All Time" default

        // ---- Wire ----
        generateBtn.addActionListener(e -> generateReport());
        lowBtn.addActionListener(e -> loadLowAttendance());
        refreshBtn.addActionListener(e -> loadCombo());

        presetCombo.addActionListener(e -> {
            applyPreset();
            // Auto-generate if table already has data
            if (tableModel.getRowCount() > 0) generateReport();
        });

        applyRangeBtn.addActionListener(e -> {
            presetCombo.setSelectedIndex(0); // reset to "Custom"
            activeRangeLabel.setText("Custom: " + spinnerDate(fromSpinner).format(FMT)
                + " → " + spinnerDate(toSpinner).format(FMT));
            if (tableModel.getRowCount() > 0) generateReport();
        });
    }

    // ---- Preset helpers ----

    private String[] buildPresets() {
        LocalDate today = LocalDate.now();
        List<String> items = new ArrayList<>();
        items.add("Custom");
        items.add("All Time");
        items.add("This Month — " + today.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            + " " + today.getYear());
        items.add("Last Month");
        // Current semester (months 1-6 = Sem 1, 7-12 = Sem 2)
        int sem = config.getCurrentSemester();
        String year = config.getAcademicYear();
        items.add("Semester " + sem + " (" + year + ")");
        // Last 3 months
        items.add("Last 3 Months");
        // Last 6 months
        items.add("Last 6 Months");
        // This year
        items.add("This Year (" + today.getYear() + ")");
        // Individual months (last 12)
        for (int i = 0; i < 12; i++) {
            LocalDate m = today.minusMonths(i);
            items.add(m.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + m.getYear());
        }
        return items.toArray(new String[0]);
    }

    /** Reads the selected preset and updates the spinners accordingly. */
    private void applyPreset() {
        String sel = (String) presetCombo.getSelectedItem();
        if (sel == null || "Custom".equals(sel)) return;

        LocalDate today = LocalDate.now();
        LocalDate from, to;

        if ("All Time".equals(sel)) {
            from = today.minusYears(20);
            to   = today;
            activeRangeLabel.setText("All Time");
        } else if (sel.startsWith("This Month")) {
            from = today.withDayOfMonth(1);
            to   = today.withDayOfMonth(today.lengthOfMonth());
            activeRangeLabel.setText(sel.substring(sel.indexOf("—") + 2));
        } else if ("Last Month".equals(sel)) {
            LocalDate lm = today.minusMonths(1);
            from = lm.withDayOfMonth(1);
            to   = lm.withDayOfMonth(lm.lengthOfMonth());
            activeRangeLabel.setText("Last Month");
        } else if (sel.startsWith("Semester")) {
            int sem = config.getCurrentSemester();
            int year = today.getYear();
            // Sem 1 = Jan–Jun, Sem 2 = Jul–Dec
            if (sem == 1) { from = LocalDate.of(year, 1, 1);  to = LocalDate.of(year, 6, 30); }
            else          { from = LocalDate.of(year, 7, 1);  to = LocalDate.of(year, 12, 31); }
            activeRangeLabel.setText(sel);
        } else if ("Last 3 Months".equals(sel)) {
            from = today.minusMonths(3).withDayOfMonth(1);
            to   = today;
            activeRangeLabel.setText("Last 3 Months");
        } else if ("Last 6 Months".equals(sel)) {
            from = today.minusMonths(6).withDayOfMonth(1);
            to   = today;
            activeRangeLabel.setText("Last 6 Months");
        } else if (sel.startsWith("This Year")) {
            from = LocalDate.of(today.getYear(), 1, 1);
            to   = LocalDate.of(today.getYear(), 12, 31);
            activeRangeLabel.setText(sel);
        } else {
            // Individual month like "April 2026"
            try {
                String[] parts = sel.split(" ");
                Month month = Month.valueOf(parts[0].toUpperCase());
                int year = Integer.parseInt(parts[1]);
                from = LocalDate.of(year, month, 1);
                to   = from.withDayOfMonth(from.lengthOfMonth());
                activeRangeLabel.setText(sel);
            } catch (Exception ex) {
                return; // unrecognised — leave spinners as-is
            }
        }

        setSpinnerDate(fromSpinner, from);
        setSpinnerDate(toSpinner,   to);
    }

    // ---- Combo ----

    private void loadCombo() {
        csCombo.removeAllItems();
        csList.clear();
        try {
            List<ClassCourse> all = csDAO.findAll();
            csCombo.addItem("— ALL Classes & Subjects —");
            csList.add(null);
            for (ClassCourse cs : all) {
                csCombo.addItem(cs.getClassName() + "  /  " + cs.getCourseName()
                    + " (" + cs.getCourseCode() + ")");
                csList.add(cs);
            }
            if (all.isEmpty()) {
                summaryLabel.setText("⚠  No class-subjects found. Assign teachers first.");
                summaryLabel.setForeground(UIUtil.WARNING);
            } else {
                summaryLabel.setText(all.size() + " class-subject(s) loaded. Select one and click Generate.");
                summaryLabel.setForeground(UIUtil.TEXT_SECONDARY);
            }
        } catch (Exception ex) {
            summaryLabel.setText("Error loading: " + ex.getMessage());
            summaryLabel.setForeground(UIUtil.DANGER);
        }
    }

    // ---- Generate ----

    private void generateReport() {
        int idx = csCombo.getSelectedIndex();
        if (idx < 0) { UIUtil.showError(this, "Select a class-subject."); return; }

        LocalDate from = spinnerDate(fromSpinner);
        LocalDate to   = spinnerDate(toSpinner);
        if (from.isAfter(to)) { UIUtil.showError(this, "\"From\" date cannot be after \"To\" date."); return; }

        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{
            "Student Name", "Student Code", "Total Sessions", "Present", "Absent", "Late", "Percentage %"});
        summaryLabel.setText("Loading...");
        summaryLabel.setForeground(UIUtil.TEXT_SECONDARY);

        ClassCourse selected = csList.get(idx);
        String rangeStr = from.format(FMT) + " → " + to.format(FMT);

        SwingWorker<List<Object[]>, Void> w = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                if (selected == null)
                    return attendanceDAO.getAllStudentsAttendanceSummaryInRange(from, to);
                return attendanceDAO.getClassAttendanceSummaryInRange(selected.getId(), from, to);
            }
            @Override protected void done() {
                try {
                    List<Object[]> rows = get();
                    double threshold = config.getAttendanceThreshold();
                    int low = 0;
                    for (Object[] row : rows) {
                        tableModel.addRow(row);
                        if (row[6] != null) {
                            try { if (Double.parseDouble(row[6].toString()) < threshold) low++; }
                            catch (NumberFormatException ignored) {}
                        }
                    }
                    String scope = selected == null ? "All Classes"
                        : selected.getClassName() + " / " + selected.getCourseName();
                    summaryLabel.setText(scope + "  |  " + rangeStr
                        + "  |  " + rows.size() + " students"
                        + "  |  Below " + (int) threshold + "%: " + low);
                    summaryLabel.setForeground(low > 0 ? UIUtil.DANGER : UIUtil.SUCCESS);
                } catch (Exception ex) {
                    summaryLabel.setText("Error: " + ex.getMessage());
                    summaryLabel.setForeground(UIUtil.DANGER);
                }
            }
        };
        w.execute();
    }

    // ---- Low Attendance ----

    private void loadLowAttendance() {
        LocalDate from = spinnerDate(fromSpinner);
        LocalDate to   = spinnerDate(toSpinner);
        if (from.isAfter(to)) { UIUtil.showError(this, "\"From\" date cannot be after \"To\" date."); return; }

        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{
            "Student Name", "Student Code", "Email", "Total Sessions", "Percentage %"});
        summaryLabel.setText("Loading low attendance...");

        String rangeStr = from.format(FMT) + " → " + to.format(FMT);

        SwingWorker<List<Object[]>, Void> w = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                return attendanceDAO.getLowAttendanceStudentsInRange(
                    config.getAttendanceThreshold(), from, to);
            }
            @Override protected void done() {
                try {
                    List<Object[]> rows = get();
                    for (Object[] row : rows) tableModel.addRow(row);
                    summaryLabel.setText("Low attendance  |  " + rangeStr
                        + "  |  Below " + (int) config.getAttendanceThreshold()
                        + "%: " + rows.size() + " students");
                    summaryLabel.setForeground(rows.isEmpty() ? UIUtil.SUCCESS : UIUtil.DANGER);
                } catch (Exception ex) {
                    summaryLabel.setText("Error: " + ex.getMessage());
                    summaryLabel.setForeground(UIUtil.DANGER);
                }
            }
        };
        w.execute();
    }

    // ---- Spinner helpers ----

    private JSpinner makeDateSpinner() {
        SpinnerDateModel m = new SpinnerDateModel();
        JSpinner s = new JSpinner(m);
        s.setEditor(new JSpinner.DateEditor(s, "yyyy-MM-dd"));
        // Default to today
        s.setValue(java.util.Date.from(
            LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        return s;
    }

    private LocalDate spinnerDate(JSpinner spinner) {
        java.util.Date d = (java.util.Date) spinner.getValue();
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private void setSpinnerDate(JSpinner spinner, LocalDate date) {
        spinner.setValue(java.util.Date.from(
            date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
    }
}
