package com.college.parking.ui;

import com.college.parking.model.ParkingActivityView;
import com.college.parking.model.SessionStatus;
import com.college.parking.model.VehicleType;
import com.college.parking.service.ParkingService;
import com.college.parking.ui.components.ModernButton;
import com.college.parking.ui.components.ModernCard;
import com.college.parking.ui.theme.UITheme;
import com.college.parking.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Screen displaying searchable and filterable parking session logs (historical & active).
 */
public class HistoryPanel extends JPanel {

    private final ParkingService parkingService;

    private JTextField txtSearch;
    private JComboBox<String> cbVehicleType;
    private JComboBox<String> cbStatus;
    private JTextField txtDate; // format: YYYY-MM-DD

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JLabel lblCount;

    public HistoryPanel() {
        this.parkingService = new ParkingService();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Top Filters Card
        ModernCard filterCard = new ModernCard(new BorderLayout(0, 10));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Parking History & Records");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        lblCount = new JLabel("Total Records: 0");
        lblCount.setFont(UITheme.FONT_BOLD);
        lblCount.setForeground(UITheme.PRIMARY_BLUE);

        titlePanel.add(lblTitle, BorderLayout.WEST);
        titlePanel.add(lblCount, BorderLayout.EAST);

        // Filter Controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        controlsPanel.setOpaque(false);

        txtSearch = new JTextField(14);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search Plate, Name, ID, Dept...");
        txtSearch.addActionListener(e -> searchHistory());

        cbVehicleType = new JComboBox<>(new String[]{"All Vehicles", "Car", "Bike", "Scooter"});
        cbVehicleType.addActionListener(e -> searchHistory());

        cbStatus = new JComboBox<>(new String[]{"All Statuses", "Active", "Completed"});
        cbStatus.addActionListener(e -> searchHistory());

        txtDate = new JTextField(8);
        txtDate.putClientProperty("JTextField.placeholderText", "YYYY-MM-DD");
        txtDate.addActionListener(e -> searchHistory());

        ModernButton btnSearch = new ModernButton("Filter", ModernButton.ButtonType.PRIMARY);
        btnSearch.addActionListener(e -> searchHistory());

        ModernButton btnReset = new ModernButton("Reset", ModernButton.ButtonType.SECONDARY);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbVehicleType.setSelectedIndex(0);
            cbStatus.setSelectedIndex(0);
            txtDate.setText("");
            searchHistory();
        });

        controlsPanel.add(new JLabel("Keywords:"));
        controlsPanel.add(txtSearch);
        controlsPanel.add(new JLabel("Type:"));
        controlsPanel.add(cbVehicleType);
        controlsPanel.add(new JLabel("Status:"));
        controlsPanel.add(cbStatus);
        controlsPanel.add(new JLabel("Date:"));
        controlsPanel.add(txtDate);
        controlsPanel.add(btnSearch);
        controlsPanel.add(btnReset);

        filterCard.add(titlePanel, BorderLayout.NORTH);
        filterCard.add(controlsPanel, BorderLayout.CENTER);

        add(filterCard, BorderLayout.NORTH);

        // 2. Table Card
        ModernCard tableCard = new ModernCard(new BorderLayout());

        String[] cols = {"Session ID", "Plate Number", "Owner Name", "College ID", "Department", "Type", "Slot", "Entry Time", "Exit Time", "Duration", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        UITheme.styleTable(historyTable);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        searchHistory();
    }

    public void searchHistory() {
        String query = txtSearch.getText().trim();
        String typeStr = (String) cbVehicleType.getSelectedItem();
        VehicleType vType = null;
        if (typeStr != null && !typeStr.equals("All Vehicles")) {
            vType = VehicleType.fromString(typeStr);
        }

        String statusStr = (String) cbStatus.getSelectedItem();
        SessionStatus sessionStatus = null;
        if (statusStr != null && !statusStr.equals("All Statuses")) {
            sessionStatus = SessionStatus.fromString(statusStr);
        }

        String dateStr = txtDate.getText().trim();

        final VehicleType finalVType = vType;
        final SessionStatus finalStatus = sessionStatus;

        SwingWorker<List<ParkingActivityView>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ParkingActivityView> doInBackground() throws Exception {
                return parkingService.searchHistory(query, finalVType, dateStr, finalStatus);
            }

            @Override
            protected void done() {
                try {
                    List<ParkingActivityView> list = get();
                    tableModel.setRowCount(0);
                    for (ParkingActivityView v : list) {
                        String duration = DateTimeUtil.formatDuration(v.getEntryTime(), v.getExitTime());
                        tableModel.addRow(new Object[]{
                            "#" + v.getSessionId(),
                            v.getRegistrationNumber(),
                            v.getOwnerName(),
                            v.getCollegeId(),
                            v.getDepartment(),
                            v.getVehicleType().getDisplayName(),
                            v.getSlotNumber(),
                            DateTimeUtil.format(v.getEntryTime()),
                            DateTimeUtil.format(v.getExitTime()),
                            duration,
                            v.getSessionStatus().getDisplayName()
                        });
                    }
                    lblCount.setText("Total Records: " + list.size());
                } catch (Exception e) {
                    System.err.println("Error searching history: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}
