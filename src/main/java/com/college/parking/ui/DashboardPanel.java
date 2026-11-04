package com.college.parking.ui;

import com.college.parking.model.DashboardStats;
import com.college.parking.model.ParkingActivityView;
import com.college.parking.model.ParkingSlot;
import com.college.parking.service.ParkingService;
import com.college.parking.ui.components.ModernButton;
import com.college.parking.ui.components.ModernCard;
import com.college.parking.ui.components.SlotCard;
import com.college.parking.ui.components.StatCard;
import com.college.parking.ui.theme.UITheme;
import com.college.parking.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Modern Dashboard Panel displaying:
 * - Summary metric cards (Total, Available, Occupied, Currently Parked)
 * - Visual parking bays layout with real-time slot cards
 * - Recent entry/exit activity table
 */
public class DashboardPanel extends JPanel {

    private final ParkingService parkingService;
    private final MainDashboardFrame parentFrame;

    // Stat Cards
    private StatCard cardTotal;
    private StatCard cardAvailable;
    private StatCard cardOccupied;
    private StatCard cardParked;

    // Visual Slots Panel
    private JPanel slotsGridPanel;

    // Recent Activity Table
    private JTable activityTable;
    private DefaultTableModel activityTableModel;

    private JLabel lblLoading;

    public DashboardPanel(MainDashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.parkingService = new ParkingService();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Top Section: Header & Action Buttons
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Parking Overview");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Live monitoring of college parking slots and recent activity");
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(lblSubtitle);

        JPanel actionsBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsBlock.setOpaque(false);

        ModernButton btnQuickEntry = new ModernButton("+ Park Vehicle", ModernButton.ButtonType.PRIMARY);
        btnQuickEntry.addActionListener(e -> parentFrame.navigateTo(MainDashboardFrame.PANEL_ENTRY_EXIT));

        ModernButton btnQuickExit = new ModernButton("Vehicle Exit", ModernButton.ButtonType.DANGER);
        btnQuickExit.addActionListener(e -> parentFrame.navigateTo(MainDashboardFrame.PANEL_ENTRY_EXIT));

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> loadData());

        actionsBlock.add(btnQuickEntry);
        actionsBlock.add(btnQuickExit);
        actionsBlock.add(btnRefresh);

        topHeader.add(titleBlock, BorderLayout.WEST);
        topHeader.add(actionsBlock, BorderLayout.EAST);

        // 2. Statistics Cards Grid
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(800, 110));

        cardTotal = new StatCard("TOTAL SLOTS", "0", "Capacity", UITheme.PRIMARY_BLUE);
        cardAvailable = new StatCard("AVAILABLE SLOTS", "0", "Open bays", UITheme.SUCCESS_GREEN);
        cardOccupied = new StatCard("OCCUPIED SLOTS", "0", "In use", UITheme.DANGER_RED);
        cardParked = new StatCard("ACTIVE VEHICLES", "0", "Currently parked", UITheme.WARNING_AMBER);

        statsPanel.add(cardTotal);
        statsPanel.add(cardAvailable);
        statsPanel.add(cardOccupied);
        statsPanel.add(cardParked);

        // Header + Stats Wrapper
        JPanel topWrapper = new JPanel(new BorderLayout(0, 16));
        topWrapper.setOpaque(false);
        topWrapper.add(topHeader, BorderLayout.NORTH);
        topWrapper.add(statsPanel, BorderLayout.CENTER);

        add(topWrapper, BorderLayout.NORTH);

        // 3. Main Center: Split between Visual Layout and Recent Activity
        JPanel centerContainer = new JPanel(new GridLayout(2, 1, 0, 16));
        centerContainer.setOpaque(false);

        // Visual Slots Card
        ModernCard slotsCard = new ModernCard(new BorderLayout(0, 12));
        JPanel slotsHeader = new JPanel(new BorderLayout());
        slotsHeader.setOpaque(false);

        JLabel lblSlotsTitle = new JLabel("Parking Bay Layout (Live)");
        lblSlotsTitle.setFont(UITheme.FONT_TITLE);
        lblSlotsTitle.setForeground(UITheme.TEXT_DARK);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem("Available", UITheme.SUCCESS_GREEN));
        legendPanel.add(createLegendItem("Occupied", UITheme.DANGER_RED));
        legendPanel.add(createLegendItem("Maintenance", UITheme.WARNING_AMBER));

        slotsHeader.add(lblSlotsTitle, BorderLayout.WEST);
        slotsHeader.add(legendPanel, BorderLayout.EAST);

        slotsGridPanel = new JPanel(new GridLayout(0, 5, 12, 12));
        slotsGridPanel.setOpaque(false);

        JScrollPane slotsScroll = new JScrollPane(slotsGridPanel);
        slotsScroll.setBorder(BorderFactory.createEmptyBorder());
        slotsScroll.setOpaque(false);
        slotsScroll.getViewport().setOpaque(false);
        slotsScroll.getVerticalScrollBar().setUnitIncrement(16);

        slotsCard.add(slotsHeader, BorderLayout.NORTH);
        slotsCard.add(slotsScroll, BorderLayout.CENTER);

        // Recent Activity Card
        ModernCard activityCard = new ModernCard(new BorderLayout(0, 10));
        JPanel activityHeader = new JPanel(new BorderLayout());
        activityHeader.setOpaque(false);

        JLabel lblActivityTitle = new JLabel("Recent Vehicle Movements");
        lblActivityTitle.setFont(UITheme.FONT_TITLE);
        lblActivityTitle.setForeground(UITheme.TEXT_DARK);

        ModernButton btnViewAll = new ModernButton("View Full History", ModernButton.ButtonType.SECONDARY);
        btnViewAll.addActionListener(e -> parentFrame.navigateTo(MainDashboardFrame.PANEL_HISTORY));

        activityHeader.add(lblActivityTitle, BorderLayout.WEST);
        activityHeader.add(btnViewAll, BorderLayout.EAST);

        String[] cols = {"Session ID", "Reg Number", "Owner Name", "Type", "Slot", "Entry Time", "Exit Time", "Status"};
        activityTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        activityTable = new JTable(activityTableModel);
        UITheme.styleTable(activityTable);

        JScrollPane tableScroll = new JScrollPane(activityTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        tableScroll.getViewport().setBackground(Color.WHITE);

        activityCard.add(activityHeader, BorderLayout.NORTH);
        activityCard.add(tableScroll, BorderLayout.CENTER);

        centerContainer.add(slotsCard);
        centerContainer.add(activityCard);

        add(centerContainer, BorderLayout.CENTER);

        // Initial Data Load
        loadData();
    }

    private JPanel createLegendItem(String label, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, 10, 10);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(10, 10));
        dot.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);

        p.add(dot);
        p.add(lbl);
        return p;
    }

    public void loadData() {
        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                DashboardStats stats = parkingService.getDashboardStats();
                List<ParkingSlot> slots = parkingService.getAllSlotsWithOccupants();
                List<ParkingActivityView> recent = parkingService.getRecentActivity(8);
                return new DashboardData(stats, slots, recent);
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    // 1. Update Stats
                    cardTotal.setValue(String.valueOf(data.stats.getTotalSlots()));
                    cardAvailable.setValue(String.valueOf(data.stats.getAvailableSlots()));
                    cardAvailable.setSubtitle(String.format("%.0f%% Available", 
                        data.stats.getTotalSlots() > 0 ? (double) data.stats.getAvailableSlots() / data.stats.getTotalSlots() * 100 : 0));
                    cardOccupied.setValue(String.valueOf(data.stats.getOccupiedSlots()));
                    cardOccupied.setSubtitle(String.format("%.0f%% Occupied", data.stats.getOccupancyPercentage()));
                    cardParked.setValue(String.valueOf(data.stats.getCurrentlyParkedCount()));

                    // 2. Update Visual Slots Grid
                    slotsGridPanel.removeAll();
                    for (ParkingSlot s : data.slots) {
                        SlotCard card = new SlotCard(s);
                        slotsGridPanel.add(card);
                    }
                    slotsGridPanel.revalidate();
                    slotsGridPanel.repaint();

                    // 3. Update Activity Table
                    activityTableModel.setRowCount(0);
                    for (ParkingActivityView a : data.recent) {
                        activityTableModel.addRow(new Object[]{
                            "#" + a.getSessionId(),
                            a.getRegistrationNumber(),
                            a.getOwnerName(),
                            a.getVehicleType().getDisplayName(),
                            a.getSlotNumber(),
                            DateTimeUtil.format(a.getEntryTime()),
                            DateTimeUtil.format(a.getExitTime()),
                            a.getSessionStatus().getDisplayName()
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error loading dashboard data: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private static class DashboardData {
        final DashboardStats stats;
        final List<ParkingSlot> slots;
        final List<ParkingActivityView> recent;

        DashboardData(DashboardStats stats, List<ParkingSlot> slots, List<ParkingActivityView> recent) {
            this.stats = stats;
            this.slots = slots;
            this.recent = recent;
        }
    }
}
