package com.college.parking.ui;

import com.college.parking.model.ParkingSlot;
import com.college.parking.model.SlotStatus;
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
 * Screen for managing physical parking slots:
 * - Viewing all slots and their statuses
 * - Adding new parking slots
 * - Toggling slot maintenance status safely
 */
public class ParkingSlotPanel extends JPanel {

    private final ParkingService parkingService;
    private final MainDashboardFrame parentFrame;

    private JTable slotTable;
    private DefaultTableModel tableModel;
    private JTextField txtNewSlotNumber;
    private JComboBox<String> cbFilter;

    public ParkingSlotPanel(MainDashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.parkingService = new ParkingService();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Top Action Card
        ModernCard topCard = new ModernCard(new BorderLayout(16, 0));
        topCard.setPreferredSize(new Dimension(0, 80));

        // Left: Add Slot
        JPanel addSlotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        addSlotPanel.setOpaque(false);

        JLabel lblAdd = new JLabel("Add New Slot:");
        lblAdd.setFont(UITheme.FONT_BOLD);
        lblAdd.setForeground(UITheme.TEXT_DARK);

        txtNewSlotNumber = new JTextField(8);
        txtNewSlotNumber.setFont(UITheme.FONT_BOLD);
        txtNewSlotNumber.putClientProperty("JTextField.placeholderText", "e.g. C01");

        ModernButton btnAdd = new ModernButton("+ Add Slot", ModernButton.ButtonType.PRIMARY);
        btnAdd.addActionListener(e -> addNewSlot());

        addSlotPanel.add(lblAdd);
        addSlotPanel.add(txtNewSlotNumber);
        addSlotPanel.add(btnAdd);

        // Right: Status Actions & Filter
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        rightActions.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(UITheme.FONT_BOLD);

        cbFilter = new JComboBox<>(new String[]{"All Slots", "Available", "Occupied", "Maintenance"});
        cbFilter.addActionListener(e -> loadSlots());

        ModernButton btnMaintenance = new ModernButton("Toggle Maintenance", ModernButton.ButtonType.SECONDARY);
        btnMaintenance.addActionListener(e -> toggleMaintenance());

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> loadSlots());

        rightActions.add(lblFilter);
        rightActions.add(cbFilter);
        rightActions.add(btnMaintenance);
        rightActions.add(btnRefresh);

        topCard.add(addSlotPanel, BorderLayout.WEST);
        topCard.add(rightActions, BorderLayout.EAST);

        add(topCard, BorderLayout.NORTH);

        // 2. Table Card
        ModernCard tableCard = new ModernCard(new BorderLayout(0, 12));

        JLabel lblTableTitle = new JLabel("Parking Slots Inventory");
        lblTableTitle.setFont(UITheme.FONT_TITLE);
        lblTableTitle.setForeground(UITheme.TEXT_DARK);

        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"Slot ID", "Slot Number", "Status", "Current Vehicle", "Parked Since"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        slotTable = new JTable(tableModel);
        UITheme.styleTable(slotTable);

        JScrollPane scrollPane = new JScrollPane(slotTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        loadSlots();
    }

    private void addNewSlot() {
        String slotNo = txtNewSlotNumber.getText().trim();
        if (slotNo.isEmpty()) {
            UITheme.showError(this, "Please enter a slot number (e.g. C01).", "Invalid Input");
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                parkingService.addNewSlot(slotNo);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UITheme.showSuccess(ParkingSlotPanel.this, "Slot [" + slotNo.toUpperCase() + "] added successfully!", "Success");
                    txtNewSlotNumber.setText("");
                    loadSlots();
                    parentFrame.refreshDashboard();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    UITheme.showError(ParkingSlotPanel.this, cause.getMessage(), "Error Adding Slot");
                }
            }
        };
        worker.execute();
    }

    private void toggleMaintenance() {
        int row = slotTable.getSelectedRow();
        if (row == -1) {
            UITheme.showError(this, "Please select a slot from the table to toggle its maintenance status.", "No Slot Selected");
            return;
        }

        int slotId = (int) tableModel.getValueAt(row, 0);
        String slotNum = (String) tableModel.getValueAt(row, 1);
        String statusStr = (String) tableModel.getValueAt(row, 2);

        SlotStatus currentStatus = SlotStatus.fromString(statusStr);
        if (currentStatus == SlotStatus.OCCUPIED) {
            UITheme.showError(this, "Slot [" + slotNum + "] is currently OCCUPIED! You cannot put an occupied slot into maintenance until the vehicle exits.", "Slot Occupied");
            return;
        }

        SlotStatus newStatus = (currentStatus == SlotStatus.MAINTENANCE) ? SlotStatus.AVAILABLE : SlotStatus.MAINTENANCE;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                parkingService.updateSlotStatusSafely(slotId, newStatus);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UITheme.showSuccess(ParkingSlotPanel.this, 
                        "Slot [" + slotNum + "] status changed to " + newStatus.getDisplayName() + ".", "Status Updated");
                    loadSlots();
                    parentFrame.refreshDashboard();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    UITheme.showError(ParkingSlotPanel.this, cause.getMessage(), "Status Update Failed");
                }
            }
        };
        worker.execute();
    }

    public void loadSlots() {
        String filter = (String) cbFilter.getSelectedItem();

        SwingWorker<List<ParkingSlot>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ParkingSlot> doInBackground() throws Exception {
                return parkingService.getAllSlotsWithOccupants();
            }

            @Override
            protected void done() {
                try {
                    List<ParkingSlot> list = get();
                    tableModel.setRowCount(0);
                    for (ParkingSlot s : list) {
                        if (filter != null && !filter.equals("All Slots")) {
                            if (!s.getStatus().getDisplayName().equalsIgnoreCase(filter)) {
                                continue;
                            }
                        }

                        tableModel.addRow(new Object[]{
                            s.getSlotId(),
                            s.getSlotNumber(),
                            s.getStatus().getDisplayName(),
                            s.getCurrentVehicleReg() != null ? s.getCurrentVehicleReg() : "-",
                            DateTimeUtil.format(s.getOccupiedSince())
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error loading slots: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}
