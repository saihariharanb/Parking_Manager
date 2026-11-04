package com.college.parking.ui;

import com.college.parking.service.AuthenticationService;
import com.college.parking.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application window with modern dark navy sidebar, responsive layout,
 * and seamless navigation across all modules.
 */
public class MainDashboardFrame extends JFrame {

    public static final String PANEL_DASHBOARD = "DASHBOARD";
    public static final String PANEL_REGISTRATION = "REGISTRATION";
    public static final String PANEL_ENTRY_EXIT = "ENTRY_EXIT";
    public static final String PANEL_SLOTS = "SLOTS";
    public static final String PANEL_HISTORY = "HISTORY";
    public static final String PANEL_REPORTS = "REPORTS";

    private JPanel contentCards;
    private CardLayout cardLayout;
    private JLabel lblPageTitle;
    private JLabel lblLiveClock;

    private DashboardPanel dashboardPanel;
    private VehicleRegistrationPanel registrationPanel;
    private ParkingEntryExitPanel entryExitPanel;
    private ParkingSlotPanel slotPanel;
    private HistoryPanel historyPanel;
    private SearchReportPanel reportPanel;

    private final Map<String, NavButton> navButtons = new HashMap<>();
    private String currentPanelKey = PANEL_DASHBOARD;

    public MainDashboardFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("College Parking Manager - Campus Vehicle Administration System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 820);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);

        // 1. Sidebar (Dark Navy #0F172A)
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // 2. Right Main Area
        JPanel rightArea = new JPanel(new BorderLayout());
        rightArea.setOpaque(false);

        // Top Header Bar
        JPanel topBar = createTopBar();
        rightArea.add(topBar, BorderLayout.NORTH);

        // CardLayout Container
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setOpaque(false);

        // Initialize Child Panels
        dashboardPanel = new DashboardPanel(this);
        registrationPanel = new VehicleRegistrationPanel(this);
        entryExitPanel = new ParkingEntryExitPanel(this);
        slotPanel = new ParkingSlotPanel(this);
        historyPanel = new HistoryPanel();
        reportPanel = new SearchReportPanel();

        contentCards.add(dashboardPanel, PANEL_DASHBOARD);
        contentCards.add(registrationPanel, PANEL_REGISTRATION);
        contentCards.add(entryExitPanel, PANEL_ENTRY_EXIT);
        contentCards.add(slotPanel, PANEL_SLOTS);
        contentCards.add(historyPanel, PANEL_HISTORY);
        contentCards.add(reportPanel, PANEL_REPORTS);

        rightArea.add(contentCards, BorderLayout.CENTER);

        root.add(rightArea, BorderLayout.CENTER);
        setContentPane(root);

        // Live Clock Timer
        Timer clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
        updateClock();

        // Highlight initial menu item
        navigateTo(PANEL_DASHBOARD);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UITheme.NAVY_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 0));

        // Brand Logo & Title
        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        JLabel lblBrand = new JLabel("PARKING MANAGER");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblBrand.setForeground(Color.WHITE);

        JLabel lblCollege = new JLabel("College Campus Edition");
        lblCollege.setFont(UITheme.FONT_SMALL);
        lblCollege.setForeground(new Color(148, 163, 184));

        brandPanel.add(lblBrand);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(lblCollege);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Items
        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        menuPanel.add(createNavItem("Dashboard", PANEL_DASHBOARD));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavItem("Registration", PANEL_REGISTRATION));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavItem("Vehicle Entry & Exit", PANEL_ENTRY_EXIT));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavItem("Parking Slots", PANEL_SLOTS));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavItem("Parking History", PANEL_HISTORY));
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(createNavItem("Analytics & Reports", PANEL_REPORTS));

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Footer User Profile & Logout
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setBackground(new Color(10, 15, 30));
        footer.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel userPanel = new JPanel();
        userPanel.setOpaque(false);
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));

        JLabel lblUser = new JLabel("Admin Operator");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel("System Admin");
        lblRole.setFont(UITheme.FONT_SMALL);
        lblRole.setForeground(UITheme.SUCCESS_GREEN);

        userPanel.add(lblUser);
        userPanel.add(lblRole);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UITheme.FONT_SMALL);
        btnLogout.setForeground(new Color(248, 113, 113));
        btnLogout.setBackground(new Color(30, 41, 59));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> logout());

        footer.add(userPanel, BorderLayout.WEST);
        footer.add(btnLogout, BorderLayout.EAST);

        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private NavButton createNavItem(String title, String key) {
        NavButton btn = new NavButton(title, key);
        btn.addActionListener(e -> navigateTo(key));
        navButtons.put(key, btn);
        return btn;
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)
        ));

        lblPageTitle = new JLabel("Dashboard Overview");
        lblPageTitle.setFont(UITheme.FONT_TITLE);
        lblPageTitle.setForeground(UITheme.TEXT_DARK);

        JPanel rightItems = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightItems.setOpaque(false);

        lblLiveClock = new JLabel();
        lblLiveClock.setFont(UITheme.FONT_REGULAR);
        lblLiveClock.setForeground(UITheme.TEXT_MUTED);

        rightItems.add(lblLiveClock);

        bar.add(lblPageTitle, BorderLayout.WEST);
        bar.add(rightItems, BorderLayout.EAST);

        return bar;
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy - hh:mm:ss a");
        lblLiveClock.setText(now.format(dtf));
    }

    public void navigateTo(String key) {
        currentPanelKey = key;
        cardLayout.show(contentCards, key);

        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(key));
        }

        switch (key) {
            case PANEL_DASHBOARD -> {
                lblPageTitle.setText("Dashboard Overview");
                dashboardPanel.loadData();
            }
            case PANEL_REGISTRATION -> {
                lblPageTitle.setText("Vehicle & Member Registration");
                registrationPanel.loadRegistrations();
            }
            case PANEL_ENTRY_EXIT -> lblPageTitle.setText("Vehicle Entry & Exit Operations");
            case PANEL_SLOTS -> {
                lblPageTitle.setText("Parking Slot Management");
                slotPanel.loadSlots();
            }
            case PANEL_HISTORY -> {
                lblPageTitle.setText("Parking History & Access Logs");
                historyPanel.searchHistory();
            }
            case PANEL_REPORTS -> {
                lblPageTitle.setText("Parking Analytics & Utilization Reports");
                reportPanel.generateReport();
            }
        }
    }

    public void refreshDashboard() {
        dashboardPanel.loadData();
    }

    private void logout() {
        boolean confirm = UITheme.showConfirm(this, "Are you sure you want to sign out?", "Confirm Sign Out");
        if (confirm) {
            new AuthenticationService().logout();
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        }
    }

    /**
     * Custom styled navigation button for sidebar with active/hover indicator.
     */
    private class NavButton extends JButton {
        private final String key;
        private boolean isActive = false;
        private boolean isHovered = false;

        public NavButton(String text, String key) {
            super(text);
            this.key = key;
            setFont(UITheme.FONT_BOLD);
            setForeground(new Color(203, 213, 225));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setPreferredSize(new Dimension(216, 40));
            setMargin(new Insets(8, 14, 8, 14));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            setForeground(active ? Color.WHITE : new Color(203, 213, 225));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isActive) {
                g2.setColor(UITheme.NAVY_SIDEBAR_ACTIVE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } else if (isHovered) {
                g2.setColor(UITheme.NAVY_SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
