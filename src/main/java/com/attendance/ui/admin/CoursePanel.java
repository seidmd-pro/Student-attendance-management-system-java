package com.attendance.ui.admin;

import com.attendance.dao.CourseDAO;
import com.attendance.dao.DepartmentDAO;
import com.attendance.model.Course;
import com.attendance.model.Department;
import com.attendance.ui.common.BaseCrudPanel;
import com.attendance.util.UIUtil;
import com.attendance.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class CoursePanel extends BaseCrudPanel {

    private final CourseDAO dao = new CourseDAO();

    public CoursePanel() {
        super("Courses", new String[]{"ID", "Code", "Name", "Department", "Credit Hours", "Description"});
    }

    @Override
    protected void loadData() {
        clearTable();
        try {
            for (Course c : dao.findAll())
                tableModel.addRow(new Object[]{c.getId(), c.getCode(), c.getName(),
                    c.getDepartmentName(), c.getCreditHours(), c.getDescription()});
            afterLoad();
        } catch (Exception ex) { UIUtil.showError(this, ex.getMessage()); }
    }

    @Override protected void openAddDialog() { showDialog(null); }
    @Override protected void openEditDialog() {
        try {
            long id = getSelectedId();
            showDialog(dao.findById(id));
        } catch (Exception ex) { UIUtil.showError(this, ex.getMessage()); }
    }
    @Override protected void deleteSelected() {
        try { dao.delete(getSelectedId()); loadData(); }
        catch (Exception ex) { UIUtil.showError(this, "Delete failed: " + ex.getMessage()); }
    }

    private void showDialog(Course existing) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add Course" : "Edit Course",
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(460, 380);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JTextField nameField  = UIUtil.styledField(20);
        JTextField codeField  = UIUtil.styledField(10);
        JSpinner creditSpinner= new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        JTextArea descArea    = new JTextArea(3, 20);
        descArea.setFont(UIUtil.FONT_BODY);
        descArea.setLineWrap(true);

        JComboBox<Department> deptCombo = new JComboBox<>();
        deptCombo.setFont(UIUtil.FONT_BODY);
        try { for (Department d : new DepartmentDAO().findAll()) deptCombo.addItem(d); }
        catch (Exception ignored) {}

        if (existing != null) {
            nameField.setText(existing.getName());
            codeField.setText(existing.getCode());
            creditSpinner.setValue(existing.getCreditHours());
            descArea.setText(existing.getDescription());
            for (int i = 0; i < deptCombo.getItemCount(); i++) {
                if (deptCombo.getItemAt(i).getId() == existing.getDepartmentId()) {
                    deptCombo.setSelectedIndex(i); break;
                }
            }
        }

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        p.setBackground(Color.WHITE);
        p.add(UIUtil.formRow("Department *",  deptCombo));    p.add(Box.createVerticalStrut(10));
        p.add(UIUtil.formRow("Course Name *", nameField));    p.add(Box.createVerticalStrut(10));
        p.add(UIUtil.formRow("Code *",        codeField));    p.add(Box.createVerticalStrut(10));
        p.add(UIUtil.formRow("Credit Hours",  creditSpinner));p.add(Box.createVerticalStrut(10));
        p.add(UIUtil.formRow("Description",   new JScrollPane(descArea))); p.add(Box.createVerticalStrut(16));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false);
        JButton save = UIUtil.primaryButton("Save");
        JButton cancel = UIUtil.secondaryButton("Cancel");
        btns.add(cancel); btns.add(save);
        p.add(btns);

        save.addActionListener(e -> {
            try {
                String name = ValidationUtil.requireNonBlank(nameField.getText(), "Course Name");
                String code = ValidationUtil.requireNonBlank(codeField.getText(), "Code");
                Department dept = (Department) deptCombo.getSelectedItem();
                if (dept == null) throw new IllegalArgumentException("Select a department.");
                Course c = existing != null ? existing : new Course();
                c.setName(name);
                c.setCode(code.toUpperCase());
                c.setDepartmentId(dept.getId());
                c.setCreditHours((int) creditSpinner.getValue());
                c.setDescription(descArea.getText().trim());
                if (existing == null) dao.insert(c); else dao.update(c);
                dlg.dispose(); loadData();
            } catch (Exception ex) { UIUtil.showError(dlg, ex.getMessage()); }
        });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.setContentPane(new JScrollPane(p));
        dlg.setVisible(true);
    }
}
