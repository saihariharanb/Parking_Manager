package com.college.parking.ui;

import com.college.parking.model.DashboardStats;
import com.college.parking.service.ParkingService;
import com.college.parking.ui.components.ModernButton;
import com.college.parking.ui.components.ModernCard;
import com.college.parking.ui.components.StatCard;
import com.college.parking.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

/**
 * Screen presenting analytical reports, aggregate metrics, and usage breakdowns.
 */
public class SearchReportPanel extends JPanel {

    private final ParkingService parkingService;

    // Stat Cards
    private StatCard cardTotalSessions;
    private StatCard cardTodaySessions;
    private StatCard cardActiveSessions;
    private StatCard cardOccupancy;

    // Tables
    private JTable vehicleTypeTable;
    private DefaultTableModel vehicleTypeModel;

    private JTable slotUsageTable;
    private DefaultTableModel slotUsageModel;

    public SearchReportPanel() {
        this.parkingService = new ParkingService();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Top Section Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Parking Analytics & Reports");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Aggregate statistics, usage trends, and slot turnover metrics");
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(lblSubtitle);

        ModernButton btnRefresh = new ModernButton("Refresh Reports", ModernButton.ButtonType.PRIMARY);
        btnRefresh.addActionListener(e -> generateReport());

        topHeader.add(titleBlock, BorderLayout.WEST);
        topHeader.add(btnRefresh, BorderLayout.EAST);

        // 2. Metrics Cards Row
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(800, 110));

        cardTotalSessions = new StatCard("TOTAL SESSIONS", "0", "All time entries", UITheme.PRIMARY_BLUE);
        cardTodaySessions = new StatCard("TODAY'S ACTIVITY", "0", "Entries today", UITheme.SUCCESS_GREEN);
        cardActiveSessions = new StatCard("CURRENTLY PARKED", "0", "Vehicles inside", UITheme.WARNING_AMBER);
        cardOccupancy = new StatCard("OCCUPANCY RATE", "0%", "Bay utilization", UITheme.DANGER_RED);

        statsPanel.add(cardTotalSessions);
        statsPanel.add(cardTodaySessions);
        statsPanel.add(cardActiveSessions);
        statsPanel.add(cardOccupancy);

        JPanel topContainer = new JPanel(new BorderLayout(0, 16));
        topContainer.setOpaque(false);
        topContainer.add(topHeader, BorderLayout.NORTH);
        topContainer.add(statsPanel, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);

        // 3. Center Section: 2 Breakdown Cards
        JPanel tablesWrapper = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesWrapper.setOpaque(false);

        // Left Table: Vehicle Type Breakdown
        ModernCard vTypeCard = new ModernCard(new BorderLayout(0, 12));
        JLabel lblVType = new JLabel("Distribution by Vehicle Type");
        lblVType.setFont(UITheme.FONT_TITLE);
        lblVType.setForeground(UITheme.TEXT_DARK);
        vTypeCard.add(lblVType, BorderLayout.NORTH);

        String[] vCols = {"Vehicle Category", "Total Sessions Recorded"};
        vehicleTypeModel = new DefaultTableModel(vCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        vehicleTypeTable = new JTable(vehicleTypeModel);
        UITheme.styleTable(vehicleTypeTable);

        JScrollPane vScroll = new JScrollPane(vehicleTypeTable);
        vScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        vScroll.getViewport().setBackground(Color.WHITE);
        vTypeCard.add(vScroll, BorderLayout.CENTER);

        // Right Table: Top Utilized Slots
        ModernCard slotCard = new ModernCard(new BorderLayout(0, 12));
        JLabel lblSlot = new JLabel("Most Utilized Parking Slots");
        lblSlot.setFont(UITheme.FONT_TITLE);
        lblSlot.setForeground(UITheme.TEXT_DARK);
        slotCard.add(lblSlot, BorderLayout.NORTH);

        String[] sCols = {"Parking Bay / Slot", "Turnover Count"};
        slotUsageModel = new DefaultTableModel(sCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        slotUsageTable = new JTable(slotUsageModel);
        UITheme.styleTable(slotUsageTable);

        JScrollPane sScroll = new JScrollPane(slotUsageTable);
        sScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        sScroll.getViewport().setBackground(Color.WHITE);
        slotCard.add(sScroll, BorderLayout.CENTER);

        tablesWrapper.add(vTypeCard);
        tablesWrapper.add(slotCard);

        add(tablesWrapper, BorderLayout.CENTER);

        generateReport();
    }

    public void generateReport() {
        SwingWorker<ReportData, Void> worker = new SwingWorker<>() {
            @Override
            protected ReportData doInBackground() throws Exception {
                DashboardStats stats = parkingService.getDashboardStats();
                Map<String, Integer> vTypes = parkingService.getVehicleTypeBreakdown();
                Map<String, Integer> slotUsage = parkingService.getMostUtilizedSlots(10);
                return new ReportData(stats, vTypes, slotUsage);
            }

            @Override
            protected void done() {
                try {
                    ReportData data = get();

                    cardTotalSessions.setValue(String.valueOf(data.stats.getTotalSessionsCount()));
                    cardTodaySessions.setValue(String.valueOf(data.stats.getTodaySessionsCount()));
                    cardActiveSessions.setValue(String.valueOf(data.stats.getCurrentlyParkedCount()));
                    cardOccupancy.setValue(String.format("%.1f%%", data.stats.getOccupancyPercentage()));

                    vehicleTypeModel.setRowCount(0);
                    for (Map.Entry<String, Integer> entry : data.vTypes.entrySet()) {
                        vehicleTypeModel.addRow(new Object[]{entry.getKey(), entry.getValue() + " vehicles"});
                    }

                    slotUsageModel.setRowCount(0);
                    for (Map.Entry<String, Integer> entry : data.slotUsage.entrySet()) {
                        slotUsageModel.addRow(new Object[]{entry.getKey(), entry.getValue() + " uses"});
                    }

                } catch (Exception e) {
                    System.err.println("Error generating report: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private static class ReportData {
        final DashboardStats stats;
        final Map<String, Integer> vTypes;
        final Map<String, Integer> slotUsage;

        ReportData(DashboardStats stats, Map<String, Integer> vTypes, Map<String, Integer> slotUsage) {
            this.stats = stats;
            this.vTypes = vTypes;
            this.slotUsage = slotUsage;
        }
    }
}
