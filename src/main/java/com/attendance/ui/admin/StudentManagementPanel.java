package com.attendance.ui.admin;

import com.attendance.dao.StudentDAO;
import com.attendance.dao.UserDAO;
import com.attendance.model.Student;
import com.attendance.model.User;
import com.attendance.service.AuthService;
import com.attendance.ui.common.BaseCrudPanel;
import com.attendance.util.UIUtil;
import com.attendance.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class StudentManagementPanel extends BaseCrudPanel {

    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO    userDAO    = new UserDAO();
    private final AuthService auth      = AuthService.getInstance();

    public StudentManagementPanel() {
        super("Student Management", new String[]{"ID", "Student Code", "Name", "Email", "Phone", "Gender", "Status"});
    }

    @Override
    protected void loadData() {
        clearTable();
        try {
            for (Student s : studentDAO.findAll())
                tableModel.addRow(new Object[]{s.getId(), s.getStudentCode(), s.getFullName(),
                    s.getEmail(), s.getPhone(), s.getGender(), s.isStatus() ? "Active" : "Inactive"});
            afterLoad();
        } catch (Exception ex) { UIUtil.showError(this, ex.getMessage()); }
    }

    @Override protected void openAddDialog()  { showDialog(null); }
    @Override protected void openEditDialog() {
        try { showDialog(studentDAO.findById(getSelectedId())); }
        catch (Exception ex) { UIUtil.showError(this, ex.getMessage()); }
    }
    @Override protected void deleteSelected() {
        try {
            Student s = studentDAO.findById(getSelectedId());
            userDAO.delete(s.getUserId());
            loadData();
        } catch (Exception ex) { UIUtil.showError(this, "Delete failed: " + ex.getMessage()); }
    }

    @Override
    protected void extraButtons(JPanel btnPanel) {
        JButton resetPwBtn = UIUtil.secondaryButton("🔑 Reset Password");
        resetPwBtn.addActionListener(e -> {
            if (table.getSelectedRow() < 0) { UIUtil.showError(this, "Select a student first."); return; }
            try {
                Student s = studentDAO.findById(getSelectedId());
                User u = userDAO.findById(s.getUserId());
                new PasswordResetDialog(SwingUtilities.getWindowAncestor(this), u).setVisible(true);
            } catch (Exception ex) { UIUtil.showError(this, ex.getMessage()); }
        });
        btnPanel.add(resetPwBtn, 0);
    }
    private void showDialog(Student existing) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add Student" : "Edit Student",
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480, 500);
        dlg.setLocationRelativeTo(this);

        JTextField firstNameField   = UIUtil.styledField(18);
        JTextField lastNameField    = UIUtil.styledField(18);
        JTextField emailField       = UIUtil.styledField(18);
        JTextField phoneField       = UIUtil.styledField(18);
        JTextField studentCodeField = UIUtil.styledField(18);
        JTextField dobField         = UIUtil.styledField(18);
        dobField.putClientProperty("JTextField.placeholderText", "YYYY-MM-DD");
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"", "Male", "Female", "Other"});
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        JPasswordField passwordField  = UIUtil.styledPasswordField(18);
        genderCombo.setFont(UIUtil.FONT_BODY);
        statusCombo.setFont(UIUtil.FONT_BODY);

        if (existing != null) {
            firstNameField.setText(existing.getFirstName());
            lastNameField.setText(existing.getLastName());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhone());
            studentCodeField.setText(existing.getStudentCode());
            if (existing.getDateOfBirth() != null) dobField.setText(existing.getDateOfBirth().toString());
            genderCombo.setSelectedItem(existing.getGender() != null ? existing.getGender() : "");
            statusCombo.setSelectedItem(existing.isStatus() ? "Active" : "Inactive");
        }

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        p.setBackground(Color.WHITE);
        p.add(UIUtil.formRow("First Name *", firstNameField));  p.add(Box.createVerticalStrut(8));
        p.add(UIUtil.formRow("Last Name *",  lastNameField));   p.add(Box.createVerticalStrut(8));
        p.add(UIUtil.formRow("Email *",      emailField));      p.add(Box.createVerticalStrut(8));
        p.add(UIUtil.formRow("Phone",        phoneField));      p.add(Box.createVerticalStrut(8));
        p.add(UIUtil.formRow("Student Code *", studentCodeField)); p.add(Box.createVerticalStrut(8));
        p.add(UIUtil.formRow("Date of Birth",  dobField));      p.add(Box.createVerticalStrut(8));
        p.add(UIUtil.formRow("Gender",       genderCombo));     p.add(Box.createVerticalStrut(8));
        p.add(UIUtil.formRow("Status",       statusCombo));     p.add(Box.createVerticalStrut(8));
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
                String code      = ValidationUtil.requireNonBlank(studentCodeField.getText(), "Student Code");
                if (!ValidationUtil.isValidEmail(email)) throw new IllegalArgumentException("Invalid email format.");
                LocalDate dob = null;
                if (!dobField.getText().isBlank()) {
                    try { dob = LocalDate.parse(dobField.getText().trim()); }
                    catch (DateTimeParseException ex2) { throw new IllegalArgumentException("Date must be YYYY-MM-DD."); }
                }
                if (existing == null) {
                    String pw = new String(passwordField.getPassword());
                    if (!ValidationUtil.isStrongPassword(pw)) throw new IllegalArgumentException("Password must be at least 6 characters.");
                    User u = new User();
                    u.setFirstName(firstName); u.setLastName(lastName); u.setEmail(email);
                    u.setPhone(phoneField.getText().trim()); u.setRole("STUDENT"); u.setStatus(true);
                    u.setPassword(auth.hashPassword(pw));
                    long uid = userDAO.insert(u);
                    Student s = new Student();
                    s.setUserId(uid); s.setStudentCode(code); s.setDateOfBirth(dob);
                    s.setGender((String) genderCombo.getSelectedItem());
                    studentDAO.insert(s);
                } else {
                    User u = userDAO.findById(existing.getUserId());
                    u.setFirstName(firstName); u.setLastName(lastName); u.setEmail(email);
                    u.setPhone(phoneField.getText().trim());
                    u.setStatus("Active".equals(statusCombo.getSelectedItem()));
                    userDAO.update(u);
                    existing.setStudentCode(code); existing.setDateOfBirth(dob);
                    existing.setGender((String) genderCombo.getSelectedItem());
                    studentDAO.update(existing);
                }
                dlg.dispose(); loadData();
            } catch (Exception ex) { UIUtil.showError(dlg, ex.getMessage()); }
        });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.setContentPane(new JScrollPane(p));
        dlg.setVisible(true);
    }
}
