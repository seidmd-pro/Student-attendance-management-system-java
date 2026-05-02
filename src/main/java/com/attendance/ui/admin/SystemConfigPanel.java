package com.attendance.ui.admin;

import com.attendance.service.EmailService;
import com.attendance.service.SystemConfigService;
import com.attendance.util.UIUtil;
import com.attendance.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class SystemConfigPanel extends JPanel {

    private final SystemConfigService configService = new SystemConfigService();
    private final EmailService        emailService  = new EmailService(configService);

    // Academic fields
    private JTextField thresholdField, semesterField, yearField, workingDaysField;

    // Email fields
    private JCheckBox  emailEnabledBox;
    private JTextField emailHostField, emailPortField, emailUserField, emailFromNameField;
    private JPasswordField emailPassField;
    private JCheckBox  emailTlsBox;
    private JLabel     emailStatusLabel;

    public SystemConfigPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = UIUtil.headerLabel("System Configuration");
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Scrollable content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // ---- Academic Settings card ----
        content.add(buildAcademicCard());
        content.add(Box.createVerticalStrut(20));

        // ---- Email Settings card ----
        content.add(buildEmailCard());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        loadConfig();
    }

    // ---- Academic card ----

    private JPanel buildAcademicCard() {
        JPanel card = UIUtil.card("Academic Settings");

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        thresholdField   = UIUtil.styledField(10);
        semesterField    = UIUtil.styledField(10);
        yearField        = UIUtil.styledField(10);
        workingDaysField = UIUtil.styledField(10);

        form.add(UIUtil.formRow("Attendance Threshold (%)", thresholdField));
        form.add(Box.createVerticalStrut(12));
        form.add(UIUtil.formRow("Current Semester",         semesterField));
        form.add(Box.createVerticalStrut(12));
        form.add(UIUtil.formRow("Academic Year",            yearField));
        form.add(Box.createVerticalStrut(12));
        form.add(UIUtil.formRow("Working Days/Week",        workingDaysField));
        form.add(Box.createVerticalStrut(20));

        JButton saveBtn = UIUtil.primaryButton("Save Academic Settings");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(saveBtn);
        saveBtn.addActionListener(e -> saveAcademic());

        card.add(form, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.add(card);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        return wrapper;
    }

    // ---- Email card ----

    private JPanel buildEmailCard() {
        JPanel card = UIUtil.card("Email Notification Settings");

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Enable toggle
        emailEnabledBox = new JCheckBox("Enable email notifications");
        emailEnabledBox.setFont(UIUtil.FONT_BOLD);
        emailEnabledBox.setOpaque(false);
        emailEnabledBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel(
            "<html><span style='color:gray'>When enabled, students receive email alerts for low attendance.<br>" +
            "For Gmail: use an App Password (myaccount.google.com/apppasswords).</span></html>");
        hint.setFont(UIUtil.FONT_SMALL);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setBorder(new EmptyBorder(0, 0, 12, 0));

        form.add(emailEnabledBox);
        form.add(Box.createVerticalStrut(6));
        form.add(hint);

        // SMTP fields
        emailHostField     = UIUtil.styledField(24);
        emailPortField     = UIUtil.styledField(6);
        emailUserField     = UIUtil.styledField(24);
        emailPassField     = UIUtil.styledPasswordField(24);
        emailFromNameField = UIUtil.styledField(24);
        emailTlsBox        = new JCheckBox("Use STARTTLS (recommended)");
        emailTlsBox.setOpaque(false);
        emailTlsBox.setFont(UIUtil.FONT_BODY);

        // Placeholders
        emailHostField.putClientProperty("JTextField.placeholderText", "smtp.gmail.com");
        emailPortField.putClientProperty("JTextField.placeholderText", "587");
        emailUserField.putClientProperty("JTextField.placeholderText", "your@email.com");
        emailFromNameField.putClientProperty("JTextField.placeholderText", "Attendance System");

        JPanel smtpGrid = new JPanel(new GridLayout(0, 2, 12, 10));
        smtpGrid.setOpaque(false);
        smtpGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        smtpGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));

        addFormRow(smtpGrid, "SMTP Host",        emailHostField);
        addFormRow(smtpGrid, "SMTP Port",        emailPortField);
        addFormRow(smtpGrid, "Username / Email", emailUserField);
        addFormRow(smtpGrid, "Password",         emailPassField);
        addFormRow(smtpGrid, "From Name",        emailFromNameField);

        form.add(smtpGrid);
        form.add(Box.createVerticalStrut(8));
        form.add(emailTlsBox);
        form.add(Box.createVerticalStrut(16));

        // Status label
        emailStatusLabel = new JLabel(" ");
        emailStatusLabel.setFont(UIUtil.FONT_SMALL);
        emailStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(emailStatusLabel);
        form.add(Box.createVerticalStrut(12));

        // Buttons
        JButton saveEmailBtn = UIUtil.primaryButton("Save Email Settings");
        JButton testBtn      = UIUtil.secondaryButton("📧 Send Test Email");
        saveEmailBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.add(saveEmailBtn);
        btnRow.add(testBtn);
        form.add(btnRow);

        saveEmailBtn.addActionListener(e -> saveEmail());
        testBtn.addActionListener(e -> sendTestEmail());

        card.add(form, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.add(card);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        return wrapper;
    }

    private void addFormRow(JPanel grid, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtil.FONT_BOLD);
        lbl.setForeground(UIUtil.TEXT_SECONDARY);
        grid.add(lbl);
        grid.add(field);
    }

    // ---- Load ----

    private void loadConfig() {
        try {
            Map<String, String> cfg = configService.getAll();

            // Academic
            thresholdField.setText(cfg.getOrDefault("attendance_threshold", "75"));
            semesterField.setText(cfg.getOrDefault("current_semester", "1"));
            yearField.setText(cfg.getOrDefault("academic_year", "2025-2026"));
            workingDaysField.setText(cfg.getOrDefault("working_days", "5"));

            // Email
            emailEnabledBox.setSelected("true".equalsIgnoreCase(cfg.getOrDefault("email_enabled", "false")));
            emailHostField.setText(cfg.getOrDefault("email_host", "smtp.gmail.com"));
            emailPortField.setText(cfg.getOrDefault("email_port", "587"));
            emailUserField.setText(cfg.getOrDefault("email_username", ""));
            emailFromNameField.setText(cfg.getOrDefault("email_from_name", "Attendance System"));
            emailTlsBox.setSelected(!"false".equalsIgnoreCase(cfg.getOrDefault("email_tls", "true")));
            // Don't pre-fill password for security — leave blank unless already set
            String savedPass = cfg.getOrDefault("email_password", "");
            if (!savedPass.isBlank()) emailPassField.setText(savedPass);

        } catch (Exception ex) { UIUtil.showError(this, ex.getMessage()); }
    }

    // ---- Save academic ----

    private void saveAcademic() {
        try {
            String threshold   = ValidationUtil.requireNonBlank(thresholdField.getText(), "Threshold");
            String semester    = ValidationUtil.requireNonBlank(semesterField.getText(), "Semester");
            String year        = ValidationUtil.requireNonBlank(yearField.getText(), "Academic Year");
            String workingDays = ValidationUtil.requireNonBlank(workingDaysField.getText(), "Working Days");
            double t = Double.parseDouble(threshold);
            if (t < 0 || t > 100) throw new IllegalArgumentException("Threshold must be 0-100.");
            int sem = Integer.parseInt(semester);
            if (sem < 1) throw new IllegalArgumentException("Semester must be a positive number.");
            int wd = Integer.parseInt(workingDays);
            if (wd < 1 || wd > 7) throw new IllegalArgumentException("Working Days must be between 1 and 7.");
            configService.set("attendance_threshold", threshold);
            configService.set("current_semester", semester);
            configService.set("academic_year", year);
            configService.set("working_days", workingDays);
            UIUtil.showSuccess(this, "Academic settings saved.");
        } catch (NumberFormatException ex) {
            UIUtil.showError(this, "Threshold, Semester, and Working Days must be valid numbers.");
        } catch (Exception ex) {
            UIUtil.showError(this, ex.getMessage());
        }
    }

    // ---- Save email ----

    private void saveEmail() {
        try {
            String host     = emailHostField.getText().trim();
            String port     = emailPortField.getText().trim();
            String username = emailUserField.getText().trim();
            String password = new String(emailPassField.getPassword());
            String fromName = emailFromNameField.getText().trim();
            boolean enabled = emailEnabledBox.isSelected();
            boolean tls     = emailTlsBox.isSelected();

            if (enabled) {
                if (host.isBlank())     throw new IllegalArgumentException("SMTP Host is required.");
                if (port.isBlank())     throw new IllegalArgumentException("SMTP Port is required.");
                if (username.isBlank()) throw new IllegalArgumentException("Username is required.");
                try { int p = Integer.parseInt(port); if (p < 1 || p > 65535) throw new Exception(); }
                catch (Exception e) { throw new IllegalArgumentException("Port must be a number 1-65535."); }
            }

            configService.set("email_enabled",   String.valueOf(enabled));
            configService.set("email_host",       host);
            configService.set("email_port",       port.isBlank() ? "587" : port);
            configService.set("email_username",   username);
            configService.set("email_from_name",  fromName.isBlank() ? "Attendance System" : fromName);
            configService.set("email_tls",        String.valueOf(tls));
            // Only update password if user typed something new
            if (!password.isBlank()) configService.set("email_password", password);

            emailStatusLabel.setText("✓ Email settings saved.");
            emailStatusLabel.setForeground(UIUtil.SUCCESS);
        } catch (Exception ex) {
            emailStatusLabel.setText("Error: " + ex.getMessage());
            emailStatusLabel.setForeground(UIUtil.DANGER);
        }
    }

    // ---- Test email ----

    private void sendTestEmail() {
        emailStatusLabel.setText("Sending test email...");
        emailStatusLabel.setForeground(UIUtil.TEXT_SECONDARY);

        SwingWorker<String, Void> w = new SwingWorker<>() {
            @Override protected String doInBackground() {
                // Use current field values (not necessarily saved yet)
                SystemConfigService tempConfig = new SystemConfigService() {
                    @Override public String get(String key, String def) {
                        return switch (key) {
                            case "email_enabled"   -> String.valueOf(emailEnabledBox.isSelected());
                            case "email_host"      -> emailHostField.getText().trim();
                            case "email_port"      -> emailPortField.getText().trim();
                            case "email_username"  -> emailUserField.getText().trim();
                            case "email_password"  -> new String(emailPassField.getPassword());
                            case "email_from_name" -> emailFromNameField.getText().trim();
                            case "email_tls"       -> String.valueOf(emailTlsBox.isSelected());
                            default                -> super.get(key, def);
                        };
                    }
                };
                return new EmailService(tempConfig).testConnection();
            }
            @Override protected void done() {
                try {
                    String error = get();
                    if (error == null) {
                        emailStatusLabel.setText("✓ Test email sent successfully! Check your inbox.");
                        emailStatusLabel.setForeground(UIUtil.SUCCESS);
                    } else {
                        emailStatusLabel.setText("✗ Failed: " + error);
                        emailStatusLabel.setForeground(UIUtil.DANGER);
                    }
                } catch (Exception ex) {
                    emailStatusLabel.setText("✗ Error: " + ex.getMessage());
                    emailStatusLabel.setForeground(UIUtil.DANGER);
                }
            }
        };
        w.execute();
    }
}
