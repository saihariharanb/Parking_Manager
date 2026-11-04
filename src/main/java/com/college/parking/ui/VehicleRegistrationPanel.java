package com.college.parking.ui;

import com.college.parking.model.UserType;
import com.college.parking.model.Vehicle;
import com.college.parking.model.VehicleType;
import com.college.parking.service.RegistrationService;
import com.college.parking.ui.components.ModernButton;
import com.college.parking.ui.components.ModernCard;
import com.college.parking.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Screen for registering, searching, updating, and deleting Student/Staff vehicles.
 */
public class VehicleRegistrationPanel extends JPanel {

    private final RegistrationService registrationService;
    private final MainDashboardFrame parentFrame;

    // Form Fields
    private JTextField txtFullName;
    private JTextField txtCollegeId;
    private JTextField txtDepartment;
    private JTextField txtPhone;
    private JTextField txtRegistrationNo;
    private JComboBox<UserType> cbUserType;
    private JComboBox<VehicleType> cbVehicleType;

    // Buttons
    private ModernButton btnSave;
    private ModernButton btnClear;
    private ModernButton btnDelete;

    // Search & Table
    private JTextField txtSearch;
    private JTable vehicleTable;
    private DefaultTableModel tableModel;

    // Currently selected vehicle for editing
    private Vehicle selectedVehicle = null;

    public VehicleRegistrationPanel(MainDashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.registrationService = new RegistrationService();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 0));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Left Form Card (Width ~380)
        ModernCard formCard = new ModernCard(new BorderLayout(0, 16));
        formCard.setPreferredSize(new Dimension(380, 0));

        // Form Title
        JPanel formTitlePanel = new JPanel();
        formTitlePanel.setOpaque(false);
        formTitlePanel.setLayout(new BoxLayout(formTitlePanel, BoxLayout.Y_AXIS));

        JLabel lblFormTitle = new JLabel("Member Registration");
        lblFormTitle.setFont(UITheme.FONT_TITLE);
        lblFormTitle.setForeground(UITheme.TEXT_DARK);

        JLabel lblFormSubtitle = new JLabel("Register student or staff vehicle details");
        lblFormSubtitle.setFont(UITheme.FONT_SMALL);
        lblFormSubtitle.setForeground(UITheme.TEXT_MUTED);

        formTitlePanel.add(lblFormTitle);
        formTitlePanel.add(Box.createVerticalStrut(4));
        formTitlePanel.add(lblFormSubtitle);

        formCard.add(formTitlePanel, BorderLayout.NORTH);

        // Form Input Fields
        JPanel inputsPanel = new JPanel(new GridLayout(7, 2, 8, 12));
        inputsPanel.setOpaque(false);

        txtFullName = new JTextField();
        txtCollegeId = new JTextField();
        txtDepartment = new JTextField();
        txtPhone = new JTextField();
        txtRegistrationNo = new JTextField();
        cbUserType = new JComboBox<>(UserType.values());
        cbVehicleType = new JComboBox<>(VehicleType.values());

        inputsPanel.add(createFieldLabel("Full Name:"));
        inputsPanel.add(txtFullName);
        inputsPanel.add(createFieldLabel("College ID:"));
        inputsPanel.add(txtCollegeId);
        inputsPanel.add(createFieldLabel("Department:"));
        inputsPanel.add(txtDepartment);
        inputsPanel.add(createFieldLabel("Phone Number:"));
        inputsPanel.add(txtPhone);
        inputsPanel.add(createFieldLabel("User Type:"));
        inputsPanel.add(cbUserType);
        inputsPanel.add(createFieldLabel("Vehicle Plate No:"));
        inputsPanel.add(txtRegistrationNo);
        inputsPanel.add(createFieldLabel("Vehicle Type:"));
        inputsPanel.add(cbVehicleType);

        formCard.add(inputsPanel, BorderLayout.CENTER);

        // Form Action Buttons
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        buttonsPanel.setOpaque(false);

        btnSave = new ModernButton("Save", ModernButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> saveRegistration());

        btnClear = new ModernButton("Clear", ModernButton.ButtonType.SECONDARY);
        btnClear.addActionListener(e -> clearForm());

        btnDelete = new ModernButton("Delete", ModernButton.ButtonType.DANGER);
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> deleteSelectedRegistration());

        buttonsPanel.add(btnSave);
        buttonsPanel.add(btnClear);
        buttonsPanel.add(btnDelete);

        formCard.add(buttonsPanel, BorderLayout.SOUTH);

        add(formCard, BorderLayout.WEST);

        // 2. Right Table Card (Searchable List)
        ModernCard tableCard = new ModernCard(new BorderLayout(0, 14));

        // Search Bar & Header
        JPanel tableHeaderPanel = new JPanel(new BorderLayout(10, 0));
        tableHeaderPanel.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Registered Vehicles List");
        lblTableTitle.setFont(UITheme.FONT_TITLE);
        lblTableTitle.setForeground(UITheme.TEXT_DARK);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);

        txtSearch = new JTextField(16);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search name, ID, plate...");
        txtSearch.addActionListener(e -> searchRegistrations());

        ModernButton btnSearch = new ModernButton("Search", ModernButton.ButtonType.PRIMARY);
        btnSearch.addActionListener(e -> searchRegistrations());

        ModernButton btnResetSearch = new ModernButton("Reset", ModernButton.ButtonType.SECONDARY);
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            loadRegistrations();
        });

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnResetSearch);

        tableHeaderPanel.add(lblTableTitle, BorderLayout.WEST);
        tableHeaderPanel.add(searchPanel, BorderLayout.EAST);

        tableCard.add(tableHeaderPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Plate Number", "Owner Name", "College ID", "Department", "User Type", "Vehicle Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        vehicleTable = new JTable(tableModel);
        UITheme.styleTable(vehicleTable);

        // Selection Listener for editing
        vehicleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && vehicleTable.getSelectedRow() != -1) {
                onRowSelected(vehicleTable.getSelectedRow());
            }
        });

        JScrollPane scrollPane = new JScrollPane(vehicleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        loadRegistrations();
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_BOLD);
        label.setForeground(UITheme.TEXT_DARK);
        return label;
    }

    private void onRowSelected(int row) {
        int vehicleId = (int) tableModel.getValueAt(row, 0);
        SwingWorker<List<Vehicle>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Vehicle> doInBackground() throws Exception {
                return registrationService.getAllRegisteredVehicles();
            }

            @Override
            protected void done() {
                try {
                    List<Vehicle> list = get();
                    for (Vehicle v : list) {
                        if (v.getVehicleId() == vehicleId) {
                            selectedVehicle = v;
                            populateForm(v);
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void populateForm(Vehicle v) {
        if (v == null) return;
        txtRegistrationNo.setText(v.getRegistrationNumber());
        cbVehicleType.setSelectedItem(v.getVehicleType());

        if (v.getOwner() != null) {
            txtFullName.setText(v.getOwner().getFullName());
            txtCollegeId.setText(v.getOwner().getCollegeId());
            txtDepartment.setText(v.getOwner().getDepartment());
            txtPhone.setText(v.getOwner().getPhone());
            cbUserType.setSelectedItem(v.getOwner().getUserType());
        }

        btnSave.setText("Update");
        btnDelete.setEnabled(true);
    }

    private void clearForm() {
        selectedVehicle = null;
        txtFullName.setText("");
        txtCollegeId.setText("");
        txtDepartment.setText("");
        txtPhone.setText("");
        txtRegistrationNo.setText("");
        cbUserType.setSelectedIndex(0);
        cbVehicleType.setSelectedIndex(0);
        btnSave.setText("Save");
        btnDelete.setEnabled(false);
        vehicleTable.clearSelection();
    }

    private void saveRegistration() {
        String fullName = txtFullName.getText().trim();
        String collegeId = txtCollegeId.getText().trim();
        String dept = txtDepartment.getText().trim();
        String phone = txtPhone.getText().trim();
        UserType userType = (UserType) cbUserType.getSelectedItem();
        String regNo = txtRegistrationNo.getText().trim();
        VehicleType vehicleType = (VehicleType) cbVehicleType.getSelectedItem();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (selectedVehicle == null) {
                    // Create new
                    registrationService.registerNewVehicle(fullName, collegeId, dept, phone, userType, regNo, vehicleType);
                } else {
                    // Update existing
                    registrationService.updateRegistration(selectedVehicle.getVehicleId(), selectedVehicle.getUserId(),
                            fullName, collegeId, dept, phone, userType, regNo, vehicleType);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UITheme.showSuccess(VehicleRegistrationPanel.this, 
                        selectedVehicle == null ? "Registration saved successfully!" : "Registration updated successfully!",
                        "Success");
                    clearForm();
                    loadRegistrations();
                    parentFrame.refreshDashboard();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    UITheme.showError(VehicleRegistrationPanel.this, cause.getMessage(), "Registration Error");
                }
            }
        };
        worker.execute();
    }

    private void deleteSelectedRegistration() {
        if (selectedVehicle == null) return;

        boolean confirm = UITheme.showConfirm(this, 
                "Are you sure you want to delete registration for vehicle [" + selectedVehicle.getRegistrationNumber() + "]?",
                "Confirm Deletion");
        if (!confirm) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                registrationService.deleteRegistration(selectedVehicle.getVehicleId(), selectedVehicle.getUserId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UITheme.showSuccess(VehicleRegistrationPanel.this, "Vehicle registration deleted.", "Success");
                    clearForm();
                    loadRegistrations();
                    parentFrame.refreshDashboard();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    UITheme.showError(VehicleRegistrationPanel.this, cause.getMessage(), "Deletion Error");
                }
            }
        };
        worker.execute();
    }

    public void loadRegistrations() {
        SwingWorker<List<Vehicle>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Vehicle> doInBackground() throws Exception {
                return registrationService.getAllRegisteredVehicles();
            }

            @Override
            protected void done() {
                try {
                    updateTable(get());
                } catch (Exception e) {
                    System.err.println("Error loading registered vehicles: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void searchRegistrations() {
        String query = txtSearch.getText().trim();
        SwingWorker<List<Vehicle>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Vehicle> doInBackground() throws Exception {
                return registrationService.searchRegisteredVehicles(query);
            }

            @Override
            protected void done() {
                try {
                    updateTable(get());
                } catch (Exception e) {
                    System.err.println("Search error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateTable(List<Vehicle> list) {
        tableModel.setRowCount(0);
        for (Vehicle v : list) {
            String ownerName = v.getOwner() != null ? v.getOwner().getFullName() : "N/A";
            String collegeId = v.getOwner() != null ? v.getOwner().getCollegeId() : "N/A";
            String dept = v.getOwner() != null ? v.getOwner().getDepartment() : "N/A";
            String userType = v.getOwner() != null ? v.getOwner().getUserType().getDisplayName() : "N/A";

            tableModel.addRow(new Object[]{
                v.getVehicleId(),
                v.getRegistrationNumber(),
                ownerName,
                collegeId,
                dept,
                userType,
                v.getVehicleType().getDisplayName()
            });
        }
    }
}
