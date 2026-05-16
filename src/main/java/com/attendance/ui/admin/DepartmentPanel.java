package com.attendance.ui.admin;

import com.attendance.dao.DepartmentDAO;
import com.attendance.model.Department;
import com.attendance.ui.common.BaseCrudPanel;
import com.attendance.util.UIUtil;
import com.attendance.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DepartmentPanel extends BaseCrudPanel {

    private final DepartmentDAO dao = new DepartmentDAO();

    public DepartmentPanel() {
        super("Departments", new String[]{"ID", "Name", "Description"});
    }

    @Override
    protected void loadData() {
        clearTable();
        try {
            for (Department d : dao.findAll())
                tableModel.addRow(new Object[]{d.getId(), d.getName(), d.getDescription()});
            afterLoad();
        } catch (Exception ex) { UIUtil.showError(this, "Load failed: " + ex.getMessage()); }
    }

    @Override
    protected void openAddDialog() {
        DepartmentDialog dlg = new DepartmentDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    @Override
    protected void openEditDialog() {
        long id = getSelectedId();
        try {
            Department d = dao.findById(id);
            DepartmentDialog dlg = new DepartmentDialog(SwingUtilities.getWindowAncestor(this), d);
            dlg.setVisible(true);
            if (dlg.isSaved()) loadData();
        } catch (Exception ex) {
            UIUtil.showError(this, ex.getMessage());
        }
    }

    @Override
    protected void deleteSelected() {
        try {
            dao.delete(getSelectedId());
            loadData();
        } catch (Exception ex) {
            UIUtil.showError(this, "Delete failed: " + ex.getMessage());
        }
    }

    // ---- Inner dialog ----
    static class DepartmentDialog extends JDialog {
        private boolean saved;
        private final JTextField nameField = UIUtil.styledField(24);
        private final JTextArea descArea   = new JTextArea(3, 24);
        private final DepartmentDAO dao    = new DepartmentDAO();
        private final Department existing;

        DepartmentDialog(Window owner, Department d) {
            super(owner, d == null ? "Add Department" : "Edit Department", ModalityType.APPLICATION_MODAL);
            this.existing = d;
            setSize(420, 280);
            setLocationRelativeTo(owner);
            setResizable(false);

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
            p.setBackground(Color.WHITE);

            descArea.setFont(UIUtil.FONT_BODY);
            descArea.setBorder(nameField.getBorder());
            descArea.setLineWrap(true);

            if (d != null) {
                nameField.setText(d.getName());
                descArea.setText(d.getDescription());
            }

            p.add(UIUtil.formRow("Name *", nameField));
            p.add(Box.createVerticalStrut(10));
            p.add(UIUtil.formRow("Description", new JScrollPane(descArea)));
            p.add(Box.createVerticalStrut(16));

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btns.setOpaque(false);
            JButton save   = UIUtil.primaryButton("Save");
            JButton cancel = UIUtil.secondaryButton("Cancel");
            btns.add(cancel);
            btns.add(save);
            p.add(btns);

            save.addActionListener(e -> doSave());
            cancel.addActionListener(e -> dispose());
            setContentPane(p);
        }

        private void doSave() {
            try {
                String name = ValidationUtil.requireNonBlank(nameField.getText(), "Name");
                Department d = existing != null ? existing : new Department();
                d.setName(name);
                d.setDescription(descArea.getText().trim());
                if (existing == null) dao.insert(d);
                else dao.update(d);
                saved = true;
                dispose();
            } catch (Exception ex) {
                UIUtil.showError(this, ex.getMessage());
            }
        }

        boolean isSaved() { return saved; }
    }
}
