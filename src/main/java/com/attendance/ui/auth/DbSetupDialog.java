package com.attendance.ui.auth;

import com.attendance.config.DatabaseConfig;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DbSetupDialog extends JDialog {

    private boolean connected = false;

    public DbSetupDialog(Frame owner) {
        super(owner, "Database Configuration", true);
        setSize(480, 300);
        setLocationRelativeTo(owner);
        setResizable(false);

        JTextField urlField  = UIUtil.styledField(28);
        JTextField userField = UIUtil.styledField(28);
        JPasswordField passField = UIUtil.styledPasswordField(28);
        urlField.setText(DatabaseConfig.getUrl());
        userField.setText(DatabaseConfig.getUsername());

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24, 28, 24, 28));
        p.setBackground(Color.WHITE);

        JLabel title = new JLabel("Configure Database Connection");
        title.setFont(UIUtil.FONT_HEADER);
        title.setForeground(UIUtil.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel("Make sure MySQL is running and accessible.");
        hint.setFont(UIUtil.FONT_SMALL);
        hint.setForeground(UIUtil.TEXT_SECONDARY);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(hint);
        p.add(Box.createVerticalStrut(16));
        p.add(UIUtil.formRow("JDBC URL", urlField));
        p.add(Box.createVerticalStrut(10));
        p.add(UIUtil.formRow("Username", userField));
        p.add(Box.createVerticalStrut(10));
        p.add(UIUtil.formRow("Password", passField));
        p.add(Box.createVerticalStrut(16));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false);
        JButton testBtn    = UIUtil.secondaryButton("Test Connection");
        JButton connectBtn = UIUtil.primaryButton("Connect & Continue");
        btns.add(testBtn);
        btns.add(connectBtn);
        p.add(btns);

        testBtn.addActionListener(e -> {
            DatabaseConfig.updateConfig(urlField.getText().trim(),
                userField.getText().trim(), new String(passField.getPassword()));
            if (DatabaseConfig.testConnection())
                UIUtil.showSuccess(this, "Connection successful!");
            else
                UIUtil.showError(this, "Connection failed. Check your settings.");
        });

        connectBtn.addActionListener(e -> {
            DatabaseConfig.updateConfig(urlField.getText().trim(),
                userField.getText().trim(), new String(passField.getPassword()));
            if (DatabaseConfig.testConnection()) {
                connected = true;
                dispose();
            } else {
                UIUtil.showError(this, "Cannot connect. Please check settings.");
            }
        });

        setContentPane(p);
    }

    public boolean isConnected() { return connected; }
}
