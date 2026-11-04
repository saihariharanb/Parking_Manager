package com.college.parking.ui;

import com.college.parking.config.DatabaseConfig;
import com.college.parking.service.AuthenticationService;
import com.college.parking.ui.components.ModernButton;
import com.college.parking.ui.components.ModernCard;
import com.college.parking.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Modern Administrator Login Frame.
 * Provides credentials authentication against MySQL database.
 */
public class LoginFrame extends JFrame {

    private final AuthenticationService authService;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblStatus;
    private ModernButton btnLogin;

    public LoginFrame() {
        this.authService = new AuthenticationService();
        initUI();
    }

    private void initUI() {
        setTitle("College Parking Manager - Admin Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_LIGHT);
        setLayout(new GridBagLayout());

        ModernCard card = new ModernCard();
        card.setPreferredSize(new Dimension(400, 440));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Header Title
        JLabel lblHeader = new JLabel("College Parking");
        lblHeader.setFont(UITheme.FONT_HEADER);
        lblHeader.setForeground(UITheme.NAVY_SIDEBAR);
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Smart Campus Parking Management System");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields Panel
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setOpaque(false);
        fieldsPanel.setLayout(new GridLayout(4, 1, 0, 8));
        fieldsPanel.setMaximumSize(new Dimension(340, 160));

        JLabel lblUser = new JLabel("Admin Username");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(UITheme.TEXT_DARK);

        txtUsername = new JTextField("admin");
        txtUsername.setFont(UITheme.FONT_REGULAR);
        txtUsername.setPreferredSize(new Dimension(320, 36));

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UITheme.FONT_BOLD);
        lblPass.setForeground(UITheme.TEXT_DARK);

        txtPassword = new JPasswordField("admin123");
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setPreferredSize(new Dimension(320, 36));

        fieldsPanel.add(lblUser);
        fieldsPanel.add(txtUsername);
        fieldsPanel.add(lblPass);
        fieldsPanel.add(txtPassword);

        // Demo Credentials Note
        JPanel notePanel = new JPanel();
        notePanel.setBackground(new Color(239, 246, 255));
        notePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        notePanel.setMaximumSize(new Dimension(340, 50));
        JLabel lblDemo = new JLabel("<html><b>Demo Admin:</b> username: <code>admin</code> | password: <code>admin123</code></html>");
        lblDemo.setFont(UITheme.FONT_SMALL);
        lblDemo.setForeground(new Color(30, 64, 175));
        notePanel.add(lblDemo);

        // Status / Message Label
        lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login Button
        btnLogin = new ModernButton("Sign In to Dashboard", ModernButton.ButtonType.PRIMARY);
        btnLogin.setMaximumSize(new Dimension(340, 42));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> performLogin());

        // Enter key triggers login
        getRootPane().setDefaultButton(btnLogin);

        // Assemble Card
        card.add(Box.createVerticalStrut(10));
        card.add(lblHeader);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(20));
        card.add(fieldsPanel);
        card.add(Box.createVerticalStrut(12));
        card.add(notePanel);
        card.add(Box.createVerticalStrut(10));
        card.add(lblStatus);
        card.add(Box.createVerticalStrut(10));
        card.add(btnLogin);
        card.add(Box.createVerticalGlue());

        add(card);

        // Check DB connection in background
        checkDatabaseConnectivity();
    }

    private void checkDatabaseConnectivity() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return DatabaseConfig.testConnection();
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (!ok) {
                        lblStatus.setText("⚠ MySQL not connected! Check application.properties");
                        lblStatus.setForeground(UITheme.DANGER_RED);
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please enter both username and password.");
            lblStatus.setForeground(UITheme.DANGER_RED);
            return;
        }

        btnLogin.setEnabled(false);
        lblStatus.setText("Authenticating with MySQL database...");
        lblStatus.setForeground(UITheme.TEXT_MUTED);

        // Use SwingWorker to avoid freezing UI
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    return authService.login(username, password);
                } catch (SQLException e) {
                    errorMessage = "Database Error: " + e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                try {
                    boolean success = get();
                    if (success) {
                        dispose(); // Close login
                        SwingUtilities.invokeLater(() -> {
                            MainDashboardFrame mainFrame = new MainDashboardFrame();
                            mainFrame.setVisible(true);
                        });
                    } else {
                        if (errorMessage != null) {
                            lblStatus.setText(errorMessage);
                        } else {
                            lblStatus.setText("Invalid username or password!");
                        }
                        lblStatus.setForeground(UITheme.DANGER_RED);
                    }
                } catch (Exception ex) {
                    lblStatus.setText("Unexpected error during login.");
                    lblStatus.setForeground(UITheme.DANGER_RED);
                }
            }
        };
        worker.execute();
    }
}
