package com.attendance.ui.admin;

import com.attendance.dao.TeacherDAO;
import com.attendance.dao.UserDAO;
import com.attendance.model.Teacher;
import com.attendance.model.User;
import com.attendance.service.AuthService;
import com.attendance.ui.common.BaseCrudPanel;
import com.attendance.util.UIUtil;
import com.attendance.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

public class TeacherManagementPanel extends BaseCrudPanel {

    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final UserDAO    userDAO    = new UserDAO();
    private final AuthService auth      = AuthService.getInstance();

    public TeacherManagementPanel() {
        super("Teacher Management", new String[]{"ID", "Employee ID", "Name", "Email", "Phone", "Specialization", "Status"});
    }

    @Override
    protected void loadData() {
        clearTable();
        try {
            for (Teacher t : teacherDAO.findAll())
                tableModel.addRow(new Object[]{t.getId(), t.getEmployeeId(), t.getFullName(),
                    t.getEmail(), t.getPhone(), t.getSpecialization(), t.isStatus() ? "Active" : "Inactive"});
            afterLoad();
        } catch (Exception ex) { UIUtil.showError(this, ex.getMessage()); }
    }

    @Override protected void openAddDialog()  { showDialog(null); }
    @Override protected void openEditDialog() {
        try { showDialog(teacherDAO.findById(getSelectedId())); }
        catch (Exception ex) { UIUtil.showError(this, ex.getMessage()); }
    }
    @Override protected void deleteSelected() {
        try {
            Teacher t = teacherDAO.findById(getSelectedId());
            userDAO.delete(t.getUserId());
            loadData();
        } catch (Exception ex) { UIUtil.showError(this, "Delete failed: " + ex.getMessage()); }
    }

    @Override
    protected void extraButtons(JPanel btnPanel) {
        JButton resetPwBtn = UIUtil.secondaryButton("🔑 Reset Password");
        resetPwBtn.addActionListener(e -> {
            if (table.getSelectedRow() < 0) { UIUtil.showError(this, "Select a teacher first."); return; }
            try {
                Teacher t = teacherDAO.findById(getSelectedId());
                User u = userDAO.findById(t.getUserId());
                new PasswordResetDialog(SwingUtilities.getWindowAncestor(this), u).setVisible(true);
            } catch (Exception ex) { UIUtil.showError(this, ex.getMessage()); }
        });
        btnPanel.add(resetPwBtn, 0);
    }

    private void showDialog(Teacher existing) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add Teacher" : "Edit Teacher",
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(460, 440);
        dlg.setLocationRelativeTo(this);

        JTextField firstNameField = UIUtil.styledField(18);
        JTextField lastNameField  = UIUtil.styledField(18);
        JTextField emailField     = UIUtil.styledField(18);
        JTextField phoneField     = UIUtil.styledField(18);
        JTextField empIdField     = UIUtil.styledField(18);
        JTextField specField      = UIUtil.styledField(18);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        JPasswordField passwordField  = UIUtil.styledPasswordField(18);
        statusCombo.setFont(UIUtil.FONT_BODY);

        if (existing != null) {
            firstNameField.setText(existing.getFirstName());
            lastNameField.setText(existing.getLastName());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhone());
            empIdField.setText(existing.getEmployeeId());
            specField.setText(existing.getSpecialization());
            statusCombo.setSelectedItem(existing.isStatus() ? "Active" : "Inactive");
        }

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        p.setBackground(Color.WHITE);
        p.add(UIUtil.formRow("First Name *",   firstNameField)); p.add(Box.createVerticalStrut(8));
        p.add(UIUtil.formRow("Last Name *",    lastNameField));  p.add(Box.createVerticalStrut(8));
        p.add(UIUtil.formRow("Email *",        emailField));     p.add(Box.createVerticalStrut(8));
        p.add(UIUtil.formRow("Phone",          phoneField));     p.add(Box.createVerticalStrut(8));
        p.add(UIUtil.formRow("Employee ID *",  empIdField));     p.add(Box.createVerticalStrut(8));
        p.add(UIUtil.formRow("Specialization", specField));      p.add(Box.createVerticalStrut(8));
        p.add(UIUtil.formRow("Status",         statusCombo));    p.add(Box.createVerticalStrut(8));
        if (existing == null) { p.add(UIUtil.formRow("Password *", passwordField)); p.add(Box.createVerticalStrut(8)); }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false);
        JButton save = UIUtil.primaryButton("Save");
        JButton cancel = UIUtil.secondaryButton("Cancel");
        btns.add(cancel); btns.add(save);
        p.add(btns);

        save.addActionListener(e -> {
            try {
                String firstName = ValidationUtil.requireNonBlank(firstNameField.getText(), "First Name");
                String lastName  = ValidationUtil.requireNonBlank(lastNameField.getText(), "Last Name");
                String email     = ValidationUtil.requireNonBlank(emailField.getText(), "Email");
                String empId     = ValidationUtil.requireNonBlank(empIdField.getText(), "Employee ID");
                if (!ValidationUtil.isValidEmail(email)) throw new IllegalArgumentException("Invalid email format.");
                if (existing == null) {
                    String pw = new String(passwordField.getPassword());
                    if (!ValidationUtil.isStrongPassword(pw)) throw new IllegalArgumentException("Password must be at least 6 characters.");
                    User u = new User();
                    u.setFirstName(firstName); u.setLastName(lastName); u.setEmail(email);
                    u.setPhone(phoneField.getText().trim()); u.setRole("TEACHER"); u.setStatus(true);
                    u.setPassword(auth.hashPassword(pw));
                    long uid = userDAO.insert(u);
                    Teacher t = new Teacher();
                    t.setUserId(uid); t.setEmployeeId(empId); t.setSpecialization(specField.getText().trim());
                    teacherDAO.insert(t);
                } else {
                    User u = userDAO.findById(existing.getUserId());
                    u.setFirstName(firstName); u.setLastName(lastName); u.setEmail(email);
                    u.setPhone(phoneField.getText().trim());
                    u.setStatus("Active".equals(statusCombo.getSelectedItem()));
                    userDAO.update(u);
                    existing.setEmployeeId(empId); existing.setSpecialization(specField.getText().trim());
                    teacherDAO.update(existing);
                }
                dlg.dispose(); loadData();
            } catch (Exception ex) { UIUtil.showError(dlg, ex.getMessage()); }
        });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.setContentPane(new JScrollPane(p));
        dlg.setVisible(true);
    }
}
