package com.attendance.ui.admin;

import com.attendance.dao.ClassRoomDAO;
import com.attendance.dao.CourseDAO;
import com.attendance.model.ClassRoom;
import com.attendance.model.Course;
import com.attendance.ui.common.BaseCrudPanel;
import com.attendance.util.UIUtil;
import com.attendance.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ClassPanel extends BaseCrudPanel {

    private final ClassRoomDAO dao = new ClassRoomDAO();

    public ClassPanel() {
        super("Classes / Sections", new String[]{"ID", "Name", "Course", "Year", "Semester"});
    }

    @Override
    protected void loadData() {
        clearTable();
        try {
            for (ClassRoom cl : dao.findAll())
                tableModel.addRow(new Object[]{cl.getId(), cl.getName(), cl.getCourseName(), cl.getYear(), cl.getSemester()});
            afterLoad();
        } catch (Exception ex) { UIUtil.showError(this, ex.getMessage()); }
    }

    @Override protected void openAddDialog() {
        showDialog(null);
    }
    @Override protected void openEditDialog() {
        try {
            long id = getSelectedId();
            ClassRoom cl = dao.findById(id);
            showDialog(cl);
        } catch (Exception ex) { UIUtil.showError(this, ex.getMessage()); }
    }
    @Override protected void deleteSelected() {
        try { dao.delete(getSelectedId()); loadData(); }
        catch (Exception ex) { UIUtil.showError(this, "Delete failed: " + ex.getMessage()); }
    }

    private void showDialog(ClassRoom existing) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add Class" : "Edit Class", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JTextField nameField = UIUtil.styledField(20);
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        JSpinner semSpinner  = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        JComboBox<Course> courseCombo = new JComboBox<>();
        courseCombo.setFont(UIUtil.FONT_BODY);

        try {
            for (Course c : new CourseDAO().findAll()) courseCombo.addItem(c);
        } catch (Exception ignored) {}

        if (existing != null) {
            nameField.setText(existing.getName());
            yearSpinner.setValue(existing.getYear());
            semSpinner.setValue(existing.getSemester());
            for (int i = 0; i < courseCombo.getItemCount(); i++) {
                if (courseCombo.getItemAt(i).getId() == existing.getCourseId()) {
                    courseCombo.setSelectedIndex(i); break;
                }
            }
        }

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        p.setBackground(Color.WHITE);
        p.add(UIUtil.formRow("Course *", courseCombo));
        p.add(Box.createVerticalStrut(10));
        p.add(UIUtil.formRow("Class Name *", nameField));
        p.add(Box.createVerticalStrut(10));
        p.add(UIUtil.formRow("Year", yearSpinner));
        p.add(Box.createVerticalStrut(10));
        p.add(UIUtil.formRow("Semester", semSpinner));
        p.add(Box.createVerticalStrut(16));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false);
        JButton save = UIUtil.primaryButton("Save");
        JButton cancel = UIUtil.secondaryButton("Cancel");
        btns.add(cancel); btns.add(save);
        p.add(btns);

        save.addActionListener(e -> {
            try {
                String name = ValidationUtil.requireNonBlank(nameField.getText(), "Class Name");
                Course course = (Course) courseCombo.getSelectedItem();
                if (course == null) throw new IllegalArgumentException("Select a course.");
                ClassRoom cl = existing != null ? existing : new ClassRoom();
                cl.setName(name);
                cl.setCourseId(course.getId());
                cl.setYear((int) yearSpinner.getValue());
                cl.setSemester((int) semSpinner.getValue());
                if (existing == null) dao.insert(cl); else dao.update(cl);
                dlg.dispose();
                loadData();
            } catch (Exception ex) { UIUtil.showError(dlg, ex.getMessage()); }
        });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.setContentPane(p);
        dlg.setVisible(true);
    }
}
