package com.attendance.ui.admin;

import com.attendance.dao.UserDAO;
import com.attendance.model.User;
import com.attendance.service.AuthService;
import com.attendance.util.UIUtil;
import com.attendance.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PasswordResetDialog extends JDialog {

    private final UserDAO userDAO = new UserDAO();
    private final AuthService auth = AuthService.getInstance();

    public PasswordResetDialog(Window owner, User user) {
        super(owner, "Reset Password — " + user.getFullName(), ModalityType.APPLICATION_MODAL);
        setSize(400, 260);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPasswordField newPassField    = UIUtil.styledPasswordField(20);
        JPasswordField confirmPassField= UIUtil.styledPasswordField(20);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        p.setBackground(Color.WHITE);

        JLabel info = new JLabel("<html>Reset password for: <b>" + user.getFullName() + "</b><br>"
            + "<span style='color:gray'>" + user.getEmail() + "</span></html>");
        info.setFont(UIUtil.FONT_BODY);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(info);
        p.add(Box.createVerticalStrut(16));
        p.add(UIUtil.formRow("New Password", newPassField));
        p.add(Box.createVerticalStrut(10));
        p.add(UIUtil.formRow("Confirm Password", confirmPassField));
        p.add(Box.createVerticalStrut(16));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false);
        JButton resetBtn  = UIUtil.primaryButton("Reset Password");
        JButton cancelBtn = UIUtil.secondaryButton("Cancel");
        btns.add(cancelBtn);
        btns.add(resetBtn);
        p.add(btns);

        resetBtn.addActionListener(e -> {
            try {
                String newPass = new String(newPassField.getPassword());
                String confirm = new String(confirmPassField.getPassword());
                if (!ValidationUtil.isStrongPassword(newPass))
                    throw new IllegalArgumentException("Password must be at least 6 characters.");
                if (!newPass.equals(confirm))
                    throw new IllegalArgumentException("Passwords do not match.");
                userDAO.updatePassword(user.getId(), auth.hashPassword(newPass));
                UIUtil.showSuccess(this, "Password reset successfully.");
                dispose();
            } catch (Exception ex) {
                UIUtil.showError(this, ex.getMessage());
            }
        });
        cancelBtn.addActionListener(e -> dispose());
        setContentPane(p);
    }
}
