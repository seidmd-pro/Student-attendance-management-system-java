package com.attendance.ui.admin;

import com.attendance.model.User;
import com.attendance.service.AuthService;
import com.attendance.ui.auth.LoginFrame;
import com.attendance.ui.common.SidebarPanel;
import com.attendance.util.UIUtil;

import javax.swing.*;
import java.awt.*;

public class AdminFrame extends JFrame {

    private final JPanel contentPanel;
    private final CardLayout cardLayout;
    private final SidebarPanel sidebar;

    public AdminFrame(User user) {
        setTitle("Admin Dashboard — " + user.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        sidebar = new SidebarPanel("Attendance System", user.getFullName(), "ADMIN");
        sidebar.addSectionLabel("Main");
        JButton dashBtn  = sidebar.addMenu("Dashboard",    "dashboard");
        sidebar.addSectionLabel("Academic");
        JButton deptBtn  = sidebar.addMenu("Departments",  "departments");
        JButton courseBtn= sidebar.addMenu("Courses",      "courses");
        JButton classBtn = sidebar.addMenu("Classes",      "classes");
        JButton csBtn    = sidebar.addMenu("Assign Teachers","classsubjects");
        sidebar.addSectionLabel("People");
        JButton studBtn  = sidebar.addMenu("Students",    "students");
        JButton teachBtn = sidebar.addMenu("Teachers",    "teachers");
        JButton enrollBtn= sidebar.addMenu("Enrollments",  "enrollments");
        sidebar.addSectionLabel("Reports");
        JButton repBtn   = sidebar.addMenu("Attendance Reports","reports");
        sidebar.addSectionLabel("System");
        JButton cfgBtn   = sidebar.addMenu("Configuration", "config");
        JButton pwBtn    = sidebar.addMenu("Change Password","changepw");
        JButton logoutBtn= sidebar.addMenu("Logout",       "logout");

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIUtil.BG);

        contentPanel.add(new AdminDashboardPanel(user), "dashboard");
        contentPanel.add(new DepartmentPanel(),     "departments");
        contentPanel.add(new CoursePanel(),         "courses");
        contentPanel.add(new ClassPanel(),          "classes");
        contentPanel.add(new ClassSubjectPanel(),   "classsubjects");
        contentPanel.add(new StudentManagementPanel(), "students");
        contentPanel.add(new TeacherManagementPanel(), "teachers");
        contentPanel.add(new EnrollmentPanel(),     "enrollments");
        contentPanel.add(new AdminReportPanel(),    "reports");
        contentPanel.add(new SystemConfigPanel(),   "config");
        contentPanel.add(new com.attendance.ui.common.ChangePasswordPanel(user), "changepw");

        sidebar.setMenuListener(id -> {
            if ("logout".equals(id)) {
                if (UIUtil.confirm(this, "Are you sure you want to logout?")) {
                    AuthService.getInstance().logout();
                    dispose();
                    SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
                }
            } else {
                cardLayout.show(contentPanel, id);
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.add(sidebar, BorderLayout.WEST);
        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);

        sidebar.setActive(dashBtn);
        cardLayout.show(contentPanel, "dashboard");
    }
}
