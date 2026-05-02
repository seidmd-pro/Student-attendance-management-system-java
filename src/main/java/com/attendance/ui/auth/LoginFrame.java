package com.attendance.ui.auth;

import com.attendance.service.AuthService;
import com.attendance.model.User;
import com.attendance.ui.admin.AdminFrame;
import com.attendance.ui.teacher.TeacherFrame;
import com.attendance.ui.student.StudentFrame;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JCheckBox rememberMe;
    private final JButton loginBtn;
    private final JLabel statusLabel;
    private final AuthService auth = AuthService.getInstance();

    public LoginFrame() {
        setTitle("Student Attendance System — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtil.BG);

        // ---- Left branding ----
        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(UIUtil.PRIMARY_DARK);
        left.setPreferredSize(new Dimension(360, 560));

        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);

        JLabel icon = new JLabel("🎓");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("Attendance System");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Track. Manage. Report.");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagline.setForeground(new Color(180, 190, 255));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Default credentials hint on left panel
        JPanel credBox = new JPanel();
        credBox.setLayout(new BoxLayout(credBox, BoxLayout.Y_AXIS));
        credBox.setOpaque(false);
        credBox.setBorder(new EmptyBorder(20, 20, 0, 20));

        JLabel credTitle = new JLabel("Default Admin");
        credTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        credTitle.setForeground(new Color(150, 160, 220));
        credTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel credEmail = new JLabel("admin@attendance.com");
        credEmail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        credEmail.setForeground(new Color(150, 160, 220));
        credEmail.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel credPass = new JLabel("Password: admin123");
        credPass.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        credPass.setForeground(new Color(150, 160, 220));
        credPass.setAlignmentX(Component.CENTER_ALIGNMENT);

        credBox.add(credTitle);
        credBox.add(Box.createVerticalStrut(2));
        credBox.add(credEmail);
        credBox.add(credPass);

        brandBox.add(icon);
        brandBox.add(Box.createVerticalStrut(12));
        brandBox.add(appName);
        brandBox.add(Box.createVerticalStrut(6));
        brandBox.add(tagline);
        brandBox.add(credBox);
        left.add(brandBox);

        // ---- Right login form ----
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(340, 380));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(UIUtil.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(UIUtil.FONT_BODY);
        sub.setForeground(UIUtil.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Email
        emailField = UIUtil.styledField(20);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password + show/hide toggle
        passwordField = UIUtil.styledPasswordField(20);

        JButton toggleBtn = new JButton("Show");
        toggleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        toggleBtn.setForeground(UIUtil.PRIMARY);
        toggleBtn.setBackground(Color.WHITE);
        toggleBtn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.setPreferredSize(new Dimension(55, 38));
        toggleBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == (char) 0) {
                passwordField.setEchoChar('•');
                toggleBtn.setText("Show");
            } else {
                passwordField.setEchoChar((char) 0);
                toggleBtn.setText("Hide");
            }
        });

        JPanel pwPanel = new JPanel(new BorderLayout(0, 0));
        pwPanel.setOpaque(false);
        pwPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pwPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pwPanel.add(passwordField, BorderLayout.CENTER);
        pwPanel.add(toggleBtn, BorderLayout.EAST);

        rememberMe = new JCheckBox("Remember me");
        rememberMe.setFont(UIUtil.FONT_BODY);
        rememberMe.setOpaque(false);
        rememberMe.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginBtn = UIUtil.primaryButton("Sign In");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIUtil.FONT_SMALL);
        statusLabel.setForeground(UIUtil.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Contact admin note
        JLabel contactNote = new JLabel("Contact administrator for account creation");
        contactNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        contactNote.setForeground(UIUtil.TEXT_SECONDARY);
        contactNote.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(28));
        form.add(fieldLabel("Email Address"));
        form.add(Box.createVerticalStrut(4));
        form.add(emailField);
        form.add(Box.createVerticalStrut(14));
        form.add(fieldLabel("Password"));
        form.add(Box.createVerticalStrut(4));
        form.add(pwPanel);
        form.add(Box.createVerticalStrut(10));
        form.add(rememberMe);
        form.add(Box.createVerticalStrut(18));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(8));
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(10));
        form.add(contactNote);

        right.add(form);
        root.add(left, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);

        // Pre-fill remembered email
        String remembered = auth.loadRememberedEmail();
        if (!remembered.isEmpty()) {
            emailField.setText(remembered);
            rememberMe.setSelected(true);
            passwordField.requestFocusInWindow();
        }

        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        emailField.addActionListener(e -> passwordField.requestFocusInWindow());
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIUtil.FONT_SMALL);
        lbl.setForeground(UIUtil.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter email and password.");
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in...");
        statusLabel.setText(" ");

        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return auth.login(email, password);
            }
            @Override
            protected void done() {
                loginBtn.setEnabled(true);
                loginBtn.setText("Sign In");
                try {
                    User user = get();
                    if (rememberMe.isSelected()) auth.saveRememberMe(email);
                    else auth.clearRememberMe();
                    openDashboard(user);
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    statusLabel.setText(msg != null ? msg : "Login failed.");
                }
            }
        };
        worker.execute();
    }

    private void openDashboard(User user) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            JFrame frame;
            switch (user.getRole()) {
                case "ADMIN":   frame = new AdminFrame(user);   break;
                case "TEACHER": frame = new TeacherFrame(user); break;
                default:        frame = new StudentFrame(user); break;
            }
            frame.setVisible(true);
        });
    }
}
