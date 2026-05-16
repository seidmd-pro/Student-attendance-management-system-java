package com.attendance.ui.auth;

import com.attendance.model.User;
import com.attendance.service.AuthService;
import com.attendance.ui.admin.AdminFrame;
import com.attendance.ui.student.StudentFrame;
import com.attendance.ui.teacher.TeacherFrame;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Login page — centered glass-card on a soft indigo→light-blue gradient background.
 * Layout follows the 2024-2025 dashboard prompt:
 *   • Gradient background (indigo → soft blue)
 *   • Centered white card with 12px rounded corners and subtle shadow
 *   • App icon + title at top of card
 *   • "Welcome back" heading + subtitle
 *   • Email + Password fields with focus glow
 *   • Feature bullet list (left column)
 *   • Sign In button (full-width, indigo)
 *   • Default credentials info box
 *   • "Contact administrator" footer note
 */
public class LoginFrame extends JFrame {

    private final JTextField     emailField;
    private final JPasswordField passwordField;
    private final JCheckBox      rememberMe;
    private final JButton        loginBtn;
    private final JLabel         statusLabel;
    private final AuthService    auth = AuthService.getInstance();

    // Design tokens
    private static final Color INDIGO_600  = new Color(79,  70,  229);
    private static final Color INDIGO_700  = new Color(67,  56,  202);
    private static final Color INDIGO_100  = new Color(224, 231, 255);
    private static final Color BG_FROM     = new Color(238, 242, 255); // very light indigo
    private static final Color BG_TO       = new Color(219, 234, 254); // light blue
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color BORDER      = new Color(226, 232, 240);
    private static final Color TEXT_DARK   = new Color(15,  23,  42);
    private static final Color TEXT_MUTED  = new Color(100, 116, 139);
    private static final Color TEXT_LIGHT  = new Color(148, 163, 184);
    private static final Color DANGER      = new Color(239, 68,  68);
    private static final Color CRED_BG     = new Color(248, 250, 252);

    public LoginFrame() {
        setTitle("Student Attendance System — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        // ── Gradient background panel ─────────────────────────────────────────
        JPanel bg = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, BG_FROM, getWidth(), getHeight(), BG_TO);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bg.setOpaque(true);

        // ── Card — white, rounded 12px, shadow ────────────────────────────────
        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow layers
                for (int i = 6; i >= 1; i--) {
                    g2.setColor(new Color(0, 0, 0, 6));
                    g2.fillRoundRect(i, i + 2, getWidth() - i, getHeight() - i, 14, 14);
                }
                // White card
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 14, 14);
                // Border
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 7, getHeight() - 7, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(780, 480));
        card.setBorder(new EmptyBorder(0, 0, 6, 6)); // shadow offset

        // ── Left column: branding + features ─────────────────────────────────
        JPanel left = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Indigo gradient left panel with rounded left corners
                GradientPaint gp = new GradientPaint(
                    0, 0, INDIGO_700, 0, getHeight(), new Color(109, 40, 217));
                g2.setPaint(gp);
                // Fill with rounded left corners only
                g2.fillRoundRect(0, 0, getWidth() + 14, getHeight(), 14, 14);
                // Decorative circles
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillOval(-40, -40, 200, 200);
                g2.fillOval(getWidth() - 80, getHeight() - 100, 180, 180);
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fillOval(20, getHeight() / 2 - 40, 140, 140);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(300, 0));
        left.setBorder(new EmptyBorder(40, 32, 40, 32));

        // App icon — load attendance icon from resources
        JLabel iconCircle = new JLabel("", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        // Load attendance icon
        try {
            java.net.URL iconUrl = getClass().getClassLoader()
                .getResource("icons/icons8-attendance-48.png");
            if (iconUrl != null) {
                ImageIcon raw = new ImageIcon(iconUrl);
                Image scaled = raw.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                iconCircle.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception ignored) {}
        iconCircle.setPreferredSize(new Dimension(80, 80));
        iconCircle.setMaximumSize(new Dimension(80, 80));
        iconCircle.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconCircle.setOpaque(false);

        JLabel appName = new JLabel("Attendance System", SwingConstants.CENTER);
        appName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Track · Manage · Report", SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tagline.setForeground(new Color(196, 181, 253));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Feature bullets
        JPanel features = new JPanel();
        features.setLayout(new BoxLayout(features, BoxLayout.Y_AXIS));
        features.setOpaque(false);
        features.setAlignmentX(Component.CENTER_ALIGNMENT);
        features.setBorder(new EmptyBorder(24, 0, 0, 0));

        for (String f : new String[]{
            "✓  Real-time attendance tracking",
            "✓  Multi-role access control",
            "✓  Reports & analytics"}) {
            JLabel fl = new JLabel(f);
            fl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            fl.setForeground(new Color(196, 181, 253));
            fl.setAlignmentX(Component.LEFT_ALIGNMENT);
            features.add(fl);
            features.add(Box.createVerticalStrut(8));
        }

        // Credentials box
        JPanel credBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        credBox.setLayout(new BoxLayout(credBox, BoxLayout.Y_AXIS));
        credBox.setOpaque(false);
        credBox.setBorder(new EmptyBorder(12, 14, 12, 14));
        credBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        credBox.setMaximumSize(new Dimension(240, 70));

        JLabel credTitle = new JLabel("Default Admin Credentials");
        credTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        credTitle.setForeground(new Color(221, 214, 254));
        credTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel credVal = new JLabel("admin@attendance.com / admin123");
        credVal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        credVal.setForeground(new Color(196, 181, 253));
        credVal.setAlignmentX(Component.CENTER_ALIGNMENT);

        credBox.add(credTitle);
        credBox.add(Box.createVerticalStrut(4));
        credBox.add(credVal);

        left.add(Box.createVerticalGlue());
        left.add(iconCircle);
        left.add(Box.createVerticalStrut(12));
        left.add(appName);
        left.add(Box.createVerticalStrut(4));
        left.add(tagline);
        left.add(features);
        left.add(Box.createVerticalStrut(24));
        left.add(credBox);
        left.add(Box.createVerticalGlue());

        // ── Right column: login form ──────────────────────────────────────────
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(340, 400));

        // Title
        JLabel titleLbl = new JLabel("Welcome back");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLbl.setForeground(TEXT_DARK);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel("Sign in to your account to continue");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setForeground(TEXT_MUTED);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Email field
        emailField = styledTextField(20);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password + show/hide
        passwordField = styledPasswordField(20);
        JButton toggleBtn = new JButton();
        toggleBtn.setBackground(Color.WHITE);
        toggleBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 1, BORDER),
            new EmptyBorder(0, 8, 0, 8)));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.setPreferredSize(new Dimension(42, 42));
        // Eye icon
        ImageIcon eyeIcon = UIUtil.loadIcon("icons8-eye-64.png", 18);
        if (eyeIcon != null) {
            toggleBtn.setIcon(eyeIcon);
            toggleBtn.setToolTipText("Show / Hide password");
        } else {
            toggleBtn.setText("Show");
            toggleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            toggleBtn.setForeground(INDIGO_600);
        }
        toggleBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == (char) 0) {
                passwordField.setEchoChar('•');
            } else {
                passwordField.setEchoChar((char) 0);
            }
        });

        JPanel pwRow = new JPanel(new BorderLayout(0, 0));
        pwRow.setOpaque(false);
        pwRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        pwRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pwRow.add(passwordField, BorderLayout.CENTER);
        pwRow.add(toggleBtn,     BorderLayout.EAST);

        // Remember me
        rememberMe = new JCheckBox("Remember me");
        rememberMe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMe.setOpaque(false);
        rememberMe.setForeground(TEXT_MUTED);
        rememberMe.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Sign In button — full width, indigo, rounded
        loginBtn = new JButton("Sign In →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(INDIGO_600);
        loginBtn.setOpaque(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setBorder(new EmptyBorder(12, 0, 12, 0));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { loginBtn.setBackground(INDIGO_700); }
            @Override public void mouseExited(MouseEvent e)  { loginBtn.setBackground(INDIGO_600); }
        });

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Contact note
        JLabel contactNote = new JLabel("Contact administrator for account creation");
        contactNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        contactNote.setForeground(TEXT_LIGHT);
        contactNote.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Assemble form
        form.add(titleLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(subLbl);
        form.add(Box.createVerticalStrut(24));
        form.add(fieldLabel("Email Address"));
        form.add(Box.createVerticalStrut(5));
        form.add(emailField);
        form.add(Box.createVerticalStrut(14));
        form.add(fieldLabel("Password"));
        form.add(Box.createVerticalStrut(5));
        form.add(pwRow);
        form.add(Box.createVerticalStrut(10));
        form.add(rememberMe);
        form.add(Box.createVerticalStrut(18));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(8));
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(10));
        form.add(contactNote);

        right.add(form);

        // ── Assemble card ─────────────────────────────────────────────────────
        card.add(left,  BorderLayout.WEST);
        card.add(right, BorderLayout.CENTER);

        bg.add(card);
        setContentPane(bg);

        // ── Pre-fill remembered email ─────────────────────────────────────────
        String remembered = auth.loadRememberedEmail();
        if (!remembered.isEmpty()) {
            emailField.setText(remembered);
            rememberMe.setSelected(true);
            passwordField.requestFocusInWindow();
        }

        // ── Wire actions ──────────────────────────────────────────────────────
        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        emailField.addActionListener(e -> passwordField.requestFocusInWindow());
    }

    // ── Styled text field with focus glow ─────────────────────────────────────
    private JTextField styledTextField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        f.setBackground(Color.WHITE);
        addFocusGlow(f);
        return f;
    }

    private JPasswordField styledPasswordField(int cols) {
        JPasswordField f = new JPasswordField(cols);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 0, BORDER),
            new EmptyBorder(8, 10, 8, 10)));
        f.setBackground(Color.WHITE);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(2, 2, 2, 0, INDIGO_600),
                    new EmptyBorder(7, 9, 7, 9)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 1, 1, 0, BORDER),
                    new EmptyBorder(8, 10, 8, 10)));
            }
        });
        return f;
    }

    private void addFocusGlow(JTextField f) {
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(INDIGO_600, 2, true),
                    new EmptyBorder(7, 9, 7, 9)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1, true),
                    new EmptyBorder(8, 10, 8, 10)));
            }
        });
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ── Login logic (unchanged) ───────────────────────────────────────────────
    private void doLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("⚠  Please enter email and password.");
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in...");
        statusLabel.setText(" ");

        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() throws Exception {
                return auth.login(email, password);
            }
            @Override protected void done() {
                loginBtn.setEnabled(true);
                loginBtn.setText("Sign In →");
                try {
                    User user = get();
                    if (rememberMe.isSelected()) auth.saveRememberMe(email);
                    else auth.clearRememberMe();
                    openDashboard(user);
                } catch (Exception ex) {
                    String msg = ex.getCause() != null
                        ? ex.getCause().getMessage() : ex.getMessage();
                    statusLabel.setText("⚠  " + (msg != null ? msg : "Login failed."));
                }
            }
        }.execute();
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
