package com.attendance.ui.admin;

import com.attendance.dao.ClassRoomDAO;
import com.attendance.dao.EnrollmentDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.model.ClassRoom;
import com.attendance.model.Student;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class EnrollmentPanel extends JPanel {

    private final EnrollmentDAO enrollDAO  = new EnrollmentDAO();
    private final StudentDAO    studentDAO = new StudentDAO();
    private final ClassRoomDAO  classDAO   = new ClassRoomDAO();

    private static final ClassRoom ALL_CLASSES = new ClassRoom(-1, "— All Classes —");

    private JComboBox<ClassRoom> classCombo;

    private JTable enrolledTable, availableTable;
    private DefaultTableModel enrolledModel, availableModel;
    private TableRowSorter<DefaultTableModel> availableSorter, enrolledSorter;

    private JTextField availableSearch, enrolledSearch;
    private JLabel availableCountLabel, enrolledCountLabel;
    private JLabel statusLabel;
    private JButton enrollBtn, unenrollBtn;

    public EnrollmentPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // ---- Header ----
        JLabel title = UIUtil.headerLabel("Student Enrollment");
        title.setBorder(new EmptyBorder(0, 0, 14, 0));
        add(title, BorderLayout.NORTH);

        // ---- Top bar ----
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel selectorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selectorRow.setOpaque(false);

        classCombo = new JComboBox<>();
        classCombo.setFont(UIUtil.FONT_BODY);
        classCombo.setPreferredSize(new Dimension(380, 34));
        classCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ClassRoom cl) {
                    if (cl.getId() == -1) { setText(cl.getName()); setFont(UIUtil.FONT_BOLD); }
                    else {
                        String d = cl.getName();
                        if (cl.getCourseName() != null && !cl.getCourseName().isEmpty())
                            d = cl.getName() + "  —  " + cl.getCourseName()
                                + "  (Year " + cl.getYear() + ", Sem " + cl.getSemester() + ")";
                        setText(d); setFont(UIUtil.FONT_BODY);
                    }
                }
                return this;
            }
        });

        JButton loadBtn    = UIUtil.primaryButton("Load Students");
        JButton refreshBtn = UIUtil.secondaryButton("↺ Refresh");

        selectorRow.add(UIUtil.boldLabel("Select Class:"));
        selectorRow.add(classCombo);
        selectorRow.add(loadBtn);
        selectorRow.add(refreshBtn);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIUtil.FONT_SMALL);
        statusLabel.setForeground(UIUtil.TEXT_SECONDARY);

        topBar.add(selectorRow, BorderLayout.WEST);
        topBar.add(statusLabel, BorderLayout.SOUTH);

        // ---- Models ----
        availableModel = new DefaultTableModel(
                new Object[]{"ID", "Student Code", "Name", "Email"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        enrolledModel = new DefaultTableModel(
                new Object[]{"ID", "Student Code", "Name", "Email", "Class"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        availableTable = new JTable(availableModel);
        enrolledTable  = new JTable(enrolledModel);
        UIUtil.styleTable(availableTable);
        UIUtil.styleTable(enrolledTable);

        // ---- Multi-select mode ----
        availableTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        enrolledTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Row sorters
        availableSorter = new TableRowSorter<>(availableModel);
        enrolledSorter  = new TableRowSorter<>(enrolledModel);
        availableTable.setRowSorter(availableSorter);
        enrolledTable.setRowSorter(enrolledSorter);

        hideIdColumn(availableTable);
        hideIdColumn(enrolledTable);

        // ---- Search bars ----
        availableSearch     = searchField("Search by name, code or email…");
        enrolledSearch      = searchField("Search by name, code or email…");
        availableCountLabel = countLabel();
        enrolledCountLabel  = countLabel();

        // ---- Available panel ----
        JPanel availablePanel = buildTablePanel(
            "Available Students  (Ctrl+click or Shift+click to select multiple)",
            UIUtil.TEXT_SECONDARY,
            availableSearch, availableCountLabel, availableTable);

        // ---- Enrolled panel ----
        JPanel enrolledPanel = buildTablePanel(
            "Enrolled Students  (Ctrl+click or Shift+click to select multiple)",
            UIUtil.PRIMARY,
            enrolledSearch, enrolledCountLabel, enrolledTable);

        // ---- Action buttons ----
        enrollBtn   = UIUtil.primaryButton("◀  Enroll");
        unenrollBtn = UIUtil.dangerButton("Unenroll  ▶");
        enrollBtn.setPreferredSize(new Dimension(120, 36));
        unenrollBtn.setPreferredSize(new Dimension(120, 36));

        // Select-all buttons
        JButton selectAllAvailBtn   = makeSelectAllBtn("Select All", availableTable);
        JButton selectAllEnrolledBtn = makeSelectAllBtn("Select All", enrolledTable);
        selectAllAvailBtn.setPreferredSize(new Dimension(120, 28));
        selectAllEnrolledBtn.setPreferredSize(new Dimension(120, 28));

        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(0, 8, 0, 8));

        for (JComponent c : new JComponent[]{enrollBtn, unenrollBtn,
                selectAllAvailBtn, selectAllEnrolledBtn})
            c.setAlignmentX(Component.CENTER_ALIGNMENT);

        actionPanel.add(Box.createVerticalGlue());
        actionPanel.add(enrollBtn);
        actionPanel.add(Box.createVerticalStrut(6));
        actionPanel.add(unenrollBtn);
        actionPanel.add(Box.createVerticalStrut(14));
        actionPanel.add(selectAllAvailBtn);
        actionPanel.add(Box.createVerticalStrut(4));
        actionPanel.add(selectAllEnrolledBtn);
        actionPanel.add(Box.createVerticalGlue());

        // ---- Layout ----
        JPanel tablesGrid = new JPanel(new GridLayout(1, 3));
        tablesGrid.setOpaque(false);
        tablesGrid.add(availablePanel);
        tablesGrid.add(actionPanel);
        tablesGrid.add(enrolledPanel);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(topBar, BorderLayout.NORTH);
        center.add(tablesGrid, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ---- Init ----
        populateClassCombo();

        // ---- Wire search ----
        wireSearch(availableSearch, availableSorter, availableModel, availableCountLabel,
                   new int[]{1, 2, 3});
        wireSearch(enrolledSearch,  enrolledSorter,  enrolledModel,  enrolledCountLabel,
                   new int[]{1, 2, 3});

        // ---- Wire buttons ----
        loadBtn.addActionListener(e -> loadStudents());
        refreshBtn.addActionListener(e -> { populateClassCombo(); loadStudents(); });
        classCombo.addActionListener(e -> updateButtonState());

        // Update enroll button label when selection changes
        availableTable.getSelectionModel().addListSelectionListener(e -> updateEnrollLabel());
        enrolledTable.getSelectionModel().addListSelectionListener(e -> updateUnenrollLabel());

        enrollBtn.addActionListener(e -> bulkEnroll());
        unenrollBtn.addActionListener(e -> bulkUnenroll());
    }

    // ---- Bulk enroll ----

    private void bulkEnroll() {
        int[] viewRows = availableTable.getSelectedRows();
        if (viewRows.length == 0) {
            UIUtil.showError(this, "Select one or more students from the Available list.");
            return;
        }
        ClassRoom cl = getSelectedClass();
        if (cl == null || cl.getId() == -1) {
            UIUtil.showError(this, "Select a specific class to enroll into.");
            return;
        }

        // Collect student IDs from selected view rows
        List<Long> ids = new ArrayList<>();
        for (int vr : viewRows) {
            int mr = availableTable.convertRowIndexToModel(vr);
            ids.add((long) availableModel.getValueAt(mr, 0));
        }

        // Confirm if more than 1
        if (ids.size() > 1) {
            if (!UIUtil.confirm(this, "Enroll " + ids.size() + " students into " + cl.getName() + "?"))
                return;
        }

        SwingWorker<int[], Void> w = new SwingWorker<>() {
            @Override protected int[] doInBackground() throws Exception {
                int enrolled = 0, skipped = 0;
                for (long sid : ids) {
                    if (enrollDAO.isEnrolled(sid, cl.getId())) { skipped++; continue; }
                    enrollDAO.enroll(sid, cl.getId());
                    enrolled++;
                }
                return new int[]{enrolled, skipped};
            }
            @Override protected void done() {
                try {
                    int[] result = get();
                    clearSearches();
                    loadStudents();
                    String msg = "✓ " + result[0] + " student(s) enrolled into " + cl.getName() + ".";
                    if (result[1] > 0) msg += "  (" + result[1] + " already enrolled, skipped)";
                    statusLabel.setText(msg);
                    statusLabel.setForeground(UIUtil.SUCCESS);
                } catch (Exception ex) { UIUtil.showError(EnrollmentPanel.this, ex.getMessage()); }
            }
        };
        w.execute();
    }

    // ---- Bulk unenroll ----

    private void bulkUnenroll() {
        int[] viewRows = enrolledTable.getSelectedRows();
        if (viewRows.length == 0) {
            UIUtil.showError(this, "Select one or more students from the Enrolled list.");
            return;
        }
        ClassRoom cl = getSelectedClass();
        if (cl == null || cl.getId() == -1) {
            UIUtil.showError(this, "Select a specific class to unenroll from.");
            return;
        }

        String confirmMsg = viewRows.length == 1
            ? "Remove this student from " + cl.getName() + "?"
            : "Remove " + viewRows.length + " students from " + cl.getName() + "?";
        if (!UIUtil.confirm(this, confirmMsg)) return;

        List<Long> ids = new ArrayList<>();
        for (int vr : viewRows) {
            int mr = enrolledTable.convertRowIndexToModel(vr);
            ids.add((long) enrolledModel.getValueAt(mr, 0));
        }

        SwingWorker<Integer, Void> w = new SwingWorker<>() {
            @Override protected Integer doInBackground() throws Exception {
                int count = 0;
                for (long sid : ids) { enrollDAO.unenroll(sid, cl.getId()); count++; }
                return count;
            }
            @Override protected void done() {
                try {
                    int count = get();
                    clearSearches();
                    loadStudents();
                    statusLabel.setText("✓ " + count + " student(s) unenrolled from " + cl.getName() + ".");
                    statusLabel.setForeground(UIUtil.TEXT_SECONDARY);
                } catch (Exception ex) { UIUtil.showError(EnrollmentPanel.this, ex.getMessage()); }
            }
        };
        w.execute();
    }

    // ---- Dynamic button labels ----

    private void updateEnrollLabel() {
        int n = availableTable.getSelectedRowCount();
        enrollBtn.setText(n > 1 ? "◀  Enroll (" + n + ")" : "◀  Enroll");
    }

    private void updateUnenrollLabel() {
        int n = enrolledTable.getSelectedRowCount();
        unenrollBtn.setText(n > 1 ? "Unenroll (" + n + ")  ▶" : "Unenroll  ▶");
    }

    // ---- UI builders ----

    private JButton makeSelectAllBtn(String label, JTable table) {
        JButton btn = new JButton(label);
        btn.setFont(UIUtil.FONT_SMALL);
        btn.setForeground(UIUtil.PRIMARY);
        btn.setBackground(new Color(235, 238, 255));
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(new Color(180, 190, 230)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (table.getRowCount() > 0) table.selectAll();
        });
        return btn;
    }

    private JPanel buildTablePanel(String title, Color titleColor,
                                   JTextField searchBox, JLabel countLbl, JTable table) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(UIUtil.CARD_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 220)),
            title,
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            UIUtil.FONT_SMALL, titleColor));

        JPanel searchRow = new JPanel(new BorderLayout(4, 0));
        searchRow.setOpaque(false);
        searchRow.setBorder(new EmptyBorder(4, 6, 4, 6));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        searchIcon.setBorder(new EmptyBorder(0, 0, 0, 4));

        JButton clearBtn = new JButton("✕");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        clearBtn.setForeground(UIUtil.TEXT_SECONDARY);
        clearBtn.setBackground(UIUtil.BG);
        clearBtn.setBorderPainted(false);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.setPreferredSize(new Dimension(24, 24));
        clearBtn.addActionListener(e -> searchBox.setText(""));

        searchRow.add(searchIcon, BorderLayout.WEST);
        searchRow.add(searchBox,  BorderLayout.CENTER);
        searchRow.add(clearBtn,   BorderLayout.EAST);

        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        countRow.setOpaque(false);
        countRow.setBorder(new EmptyBorder(0, 6, 4, 6));
        countRow.add(countLbl);

        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setOpaque(false);
        topArea.add(searchRow, BorderLayout.NORTH);
        topArea.add(countRow,  BorderLayout.SOUTH);

        panel.add(topArea, BorderLayout.NORTH);
        panel.add(UIUtil.scrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JTextField searchField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(UIUtil.FONT_BODY);
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 210)),
            new EmptyBorder(4, 6, 4, 6)));
        return f;
    }

    private JLabel countLabel() {
        JLabel l = new JLabel("0 students");
        l.setFont(UIUtil.FONT_SMALL);
        l.setForeground(UIUtil.TEXT_SECONDARY);
        return l;
    }

    // ---- Search wiring ----

    private void wireSearch(JTextField field, TableRowSorter<DefaultTableModel> sorter,
                            DefaultTableModel model, JLabel countLabel, int[] searchCols) {
        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applySearch(); }
            @Override public void removeUpdate(DocumentEvent e)  { applySearch(); }
            @Override public void changedUpdate(DocumentEvent e) { applySearch(); }

            private void applySearch() {
                String text = field.getText().trim();
                if (text.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), searchCols));
                updateCount(sorter, model, countLabel);
            }
        };
        field.getDocument().addDocumentListener(dl);
    }

    private void updateCount(TableRowSorter<DefaultTableModel> sorter,
                              DefaultTableModel model, JLabel label) {
        int total = model.getRowCount();
        int visible = 0;
        for (int i = 0; i < total; i++)
            if (sorter.convertRowIndexToView(i) >= 0) visible++;
        if (visible == total)
            label.setText(total + " student" + (total != 1 ? "s" : ""));
        else
            label.setText(visible + " of " + total + " students");
        label.setForeground(visible < total ? UIUtil.PRIMARY : UIUtil.TEXT_SECONDARY);
    }

    private void refreshCounts() {
        updateCount(availableSorter, availableModel, availableCountLabel);
        updateCount(enrolledSorter,  enrolledModel,  enrolledCountLabel);
    }

    private void clearSearches() {
        availableSearch.setText("");
        enrolledSearch.setText("");
    }

    // ---- Helpers ----

    private void hideIdColumn(JTable t) {
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setMaxWidth(0);
        t.getColumnModel().getColumn(0).setWidth(0);
    }

    private void populateClassCombo() {
        ClassRoom prev = getSelectedClass();
        classCombo.removeAllItems();
        classCombo.addItem(ALL_CLASSES);
        try {
            List<ClassRoom> classes = classDAO.findAll();
            for (ClassRoom cl : classes) classCombo.addItem(cl);
            if (classes.isEmpty()) {
                statusLabel.setText("⚠  No classes found. Create classes first in Admin → Classes.");
                statusLabel.setForeground(UIUtil.WARNING);
            }
            if (prev != null && prev.getId() != -1) {
                for (int i = 0; i < classCombo.getItemCount(); i++) {
                    if (classCombo.getItemAt(i).getId() == prev.getId()) {
                        classCombo.setSelectedIndex(i); break;
                    }
                }
            }
        } catch (Exception ex) {
            statusLabel.setText("Error loading classes: " + ex.getMessage());
            statusLabel.setForeground(UIUtil.DANGER);
        }
        updateButtonState();
    }

    private ClassRoom getSelectedClass() {
        return (ClassRoom) classCombo.getSelectedItem();
    }

    private void updateButtonState() {
        ClassRoom cl = getSelectedClass();
        boolean specific = cl != null && cl.getId() != -1;
        enrollBtn.setEnabled(specific);
        unenrollBtn.setEnabled(specific);
        enrollBtn.setToolTipText(specific ? null : "Select a specific class to enroll students");
        unenrollBtn.setToolTipText(specific ? null : "Select a specific class to unenroll students");
    }

    private void loadStudents() {
        ClassRoom cl = getSelectedClass();
        if (cl == null) return;

        enrolledModel.setRowCount(0);
        availableModel.setRowCount(0);
        hideIdColumn(availableTable);
        hideIdColumn(enrolledTable);

        boolean isAll = cl.getId() == -1;

        try {
            List<Student> allStudents = studentDAO.findAll();

            if (isAll) {
                Map<Long, List<String>> studentClassMap = new LinkedHashMap<>();
                for (Student s : allStudents) studentClassMap.put(s.getId(), new ArrayList<>());
                for (ClassRoom room : classDAO.findAll()) {
                    for (Student s : studentDAO.findByClass(room.getId())) {
                        List<String> names = studentClassMap.get(s.getId());
                        if (names != null) names.add(room.getName());
                    }
                }
                int enrolledCount = 0;
                for (Student s : allStudents) {
                    List<String> classNames = studentClassMap.get(s.getId());
                    if (classNames != null && !classNames.isEmpty()) {
                        enrolledModel.addRow(new Object[]{
                            s.getId(), s.getStudentCode(), s.getFullName(),
                            s.getEmail() != null ? s.getEmail() : "—",
                            String.join(", ", classNames)
                        });
                        enrolledCount++;
                    } else {
                        availableModel.addRow(new Object[]{
                            s.getId(), s.getStudentCode(), s.getFullName(),
                            s.getEmail() != null ? s.getEmail() : "—"
                        });
                    }
                }
                statusLabel.setText("All Classes  |  Enrolled: " + enrolledCount
                    + "  |  Not enrolled: " + (allStudents.size() - enrolledCount));
                statusLabel.setForeground(UIUtil.TEXT_SECONDARY);

            } else {
                List<Student> enrolled = studentDAO.findByClass(cl.getId());
                Set<Long> enrolledIds = new HashSet<>();
                for (Student s : enrolled) {
                    enrolledIds.add(s.getId());
                    enrolledModel.addRow(new Object[]{
                        s.getId(), s.getStudentCode(), s.getFullName(),
                        s.getEmail() != null ? s.getEmail() : "—",
                        cl.getName()
                    });
                }
                for (Student s : allStudents) {
                    if (!enrolledIds.contains(s.getId())) {
                        availableModel.addRow(new Object[]{
                            s.getId(), s.getStudentCode(), s.getFullName(),
                            s.getEmail() != null ? s.getEmail() : "—"
                        });
                    }
                }
                statusLabel.setText("Class: " + cl.getName()
                    + "  |  Enrolled: " + enrolled.size()
                    + "  |  Available: " + (allStudents.size() - enrolled.size()));
                statusLabel.setForeground(UIUtil.TEXT_SECONDARY);
            }
        } catch (Exception ex) {
            UIUtil.showError(this, "Failed to load students: " + ex.getMessage());
        }

        refreshCounts();
        updateEnrollLabel();
        updateUnenrollLabel();
    }
}
