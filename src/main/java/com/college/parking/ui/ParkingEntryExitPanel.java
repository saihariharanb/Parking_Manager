package com.college.parking.ui;

import com.college.parking.model.ParkingSession;
import com.college.parking.service.ParkingService;
import com.college.parking.ui.components.ModernButton;
import com.college.parking.ui.components.ModernCard;
import com.college.parking.ui.theme.UITheme;
import com.college.parking.util.DateTimeUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Screen handling Vehicle Entry (check-in with auto-slot assignment)
 * and Vehicle Exit (check-out, slot release, and duration calculation).
 */
public class ParkingEntryExitPanel extends JPanel {

    private final ParkingService parkingService;
    private final MainDashboardFrame parentFrame;

    // Entry Components
    private JTextField txtEntryRegNo;
    private JLabel lblEntryResult;
    private ModernButton btnProcessEntry;

    // Exit Components
    private JTextField txtExitRegNo;
    private JLabel lblExitDetails;
    private ModernButton btnCheckVehicle;
    private ModernButton btnProcessExit;

    public ParkingEntryExitPanel(MainDashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.parkingService = new ParkingService();
        initUI();
    }

    private void initUI() {
        setLayout(new GridLayout(1, 2, 24, 0));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // ==========================================
        // 1. VEHICLE ENTRY CARD (Left)
        // ==========================================
        ModernCard entryCard = new ModernCard(new BorderLayout(0, 16));

        // Header
        JPanel entryHeader = new JPanel();
        entryHeader.setOpaque(false);
        entryHeader.setLayout(new BoxLayout(entryHeader, BoxLayout.Y_AXIS));

        JLabel lblEntryTitle = new JLabel("Vehicle Check-In (Entry)");
        lblEntryTitle.setFont(UITheme.FONT_TITLE);
        lblEntryTitle.setForeground(UITheme.SUCCESS_GREEN);

        JLabel lblEntrySubtitle = new JLabel("Auto-assigns the first available parking slot to registered vehicles");
        lblEntrySubtitle.setFont(UITheme.FONT_SMALL);
        lblEntrySubtitle.setForeground(UITheme.TEXT_MUTED);

        entryHeader.add(lblEntryTitle);
        entryHeader.add(Box.createVerticalStrut(4));
        entryHeader.add(lblEntrySubtitle);

        entryCard.add(entryHeader, BorderLayout.NORTH);

        // Body
        JPanel entryBody = new JPanel();
        entryBody.setOpaque(false);
        entryBody.setLayout(new BoxLayout(entryBody, BoxLayout.Y_AXIS));

        JLabel lblPlatePrompt = new JLabel("Enter Vehicle Registration Number:");
        lblPlatePrompt.setFont(UITheme.FONT_BOLD);
        lblPlatePrompt.setForeground(UITheme.TEXT_DARK);

        txtEntryRegNo = new JTextField();
        txtEntryRegNo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtEntryRegNo.setPreferredSize(new Dimension(300, 42));
        txtEntryRegNo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtEntryRegNo.putClientProperty("JTextField.placeholderText", "e.g. KA-01-AB-1234");
        txtEntryRegNo.addActionListener(e -> processEntry());

        btnProcessEntry = new ModernButton("Assign Slot & Record Entry", ModernButton.ButtonType.SUCCESS);
        btnProcessEntry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnProcessEntry.addActionListener(e -> processEntry());

        // Result Banner Panel
        lblEntryResult = new JLabel("<html><i>Enter plate number and press Enter to record entry.</i></html>");
        lblEntryResult.setFont(UITheme.FONT_REGULAR);
        lblEntryResult.setForeground(UITheme.TEXT_MUTED);

        JPanel resultWrapper = new JPanel(new BorderLayout());
        resultWrapper.setBackground(new Color(241, 245, 249));
        resultWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        resultWrapper.add(lblEntryResult, BorderLayout.CENTER);

        entryBody.add(lblPlatePrompt);
        entryBody.add(Box.createVerticalStrut(8));
        entryBody.add(txtEntryRegNo);
        entryBody.add(Box.createVerticalStrut(14));
        entryBody.add(btnProcessEntry);
        entryBody.add(Box.createVerticalStrut(20));
        entryBody.add(resultWrapper);
        entryBody.add(Box.createVerticalGlue());

        entryCard.add(entryBody, BorderLayout.CENTER);

        // ==========================================
        // 2. VEHICLE EXIT CARD (Right)
        // ==========================================
        ModernCard exitCard = new ModernCard(new BorderLayout(0, 16));

        // Header
        JPanel exitHeader = new JPanel();
        exitHeader.setOpaque(false);
        exitHeader.setLayout(new BoxLayout(exitHeader, BoxLayout.Y_AXIS));

        JLabel lblExitTitle = new JLabel("Vehicle Check-Out (Exit)");
        lblExitTitle.setFont(UITheme.FONT_TITLE);
        lblExitTitle.setForeground(UITheme.DANGER_RED);

        JLabel lblExitSubtitle = new JLabel("Releases the parking slot and records departure duration");
        lblExitSubtitle.setFont(UITheme.FONT_SMALL);
        lblExitSubtitle.setForeground(UITheme.TEXT_MUTED);

        exitHeader.add(lblExitTitle);
        exitHeader.add(Box.createVerticalStrut(4));
        exitHeader.add(lblExitSubtitle);

        exitCard.add(exitHeader, BorderLayout.NORTH);

        // Body
        JPanel exitBody = new JPanel();
        exitBody.setOpaque(false);
        exitBody.setLayout(new BoxLayout(exitBody, BoxLayout.Y_AXIS));

        JLabel lblExitPrompt = new JLabel("Enter Parked Vehicle Plate Number:");
        lblExitPrompt.setFont(UITheme.FONT_BOLD);
        lblExitPrompt.setForeground(UITheme.TEXT_DARK);

        txtExitRegNo = new JTextField();
        txtExitRegNo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtExitRegNo.setPreferredSize(new Dimension(300, 42));
        txtExitRegNo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtExitRegNo.putClientProperty("JTextField.placeholderText", "e.g. KA-01-AB-1234");
        txtExitRegNo.addActionListener(e -> processExit());

        btnProcessExit = new ModernButton("Confirm Exit & Release Slot", ModernButton.ButtonType.DANGER);
        btnProcessExit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnProcessExit.addActionListener(e -> processExit());

        // Exit Result Banner
        lblExitDetails = new JLabel("<html><i>Enter parked vehicle registration to process departure.</i></html>");
        lblExitDetails.setFont(UITheme.FONT_REGULAR);
        lblExitDetails.setForeground(UITheme.TEXT_MUTED);

        JPanel exitDetailsWrapper = new JPanel(new BorderLayout());
        exitDetailsWrapper.setBackground(new Color(241, 245, 249));
        exitDetailsWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        exitDetailsWrapper.add(lblExitDetails, BorderLayout.CENTER);

        exitBody.add(lblExitPrompt);
        exitBody.add(Box.createVerticalStrut(8));
        exitBody.add(txtExitRegNo);
        exitBody.add(Box.createVerticalStrut(14));
        exitBody.add(btnProcessExit);
        exitBody.add(Box.createVerticalStrut(20));
        exitBody.add(exitDetailsWrapper);
        exitBody.add(Box.createVerticalGlue());

        exitCard.add(exitBody, BorderLayout.CENTER);

        add(entryCard);
        add(exitCard);
    }

    private void processEntry() {
        String regNo = txtEntryRegNo.getText().trim();
        if (regNo.isEmpty()) {
            UITheme.showError(this, "Please enter a vehicle registration number.", "Missing Input");
            return;
        }

        btnProcessEntry.setEnabled(false);
        lblEntryResult.setText("<html><i>Assigning available slot...</i></html>");

        SwingWorker<ParkingService.EntryResult, Void> worker = new SwingWorker<>() {
            @Override
            protected ParkingService.EntryResult doInBackground() throws Exception {
                return parkingService.processVehicleEntry(regNo);
            }

            @Override
            protected void done() {
                btnProcessEntry.setEnabled(true);
                try {
                    ParkingService.EntryResult res = get();
                    String html = String.format(
                        "<html><div style='color: #065F46;'>" +
                        "<h3 style='margin:0;'>✓ Vehicle Entry Successful!</h3>" +
                        "<p style='margin:4px 0;'><b>Assigned Slot:</b> <span style='font-size:16px; color:#10B981;'><b>%s</b></span></p>" +
                        "<p style='margin:4px 0;'><b>Vehicle:</b> %s</p>" +
                        "<p style='margin:4px 0;'><b>Owner:</b> %s (%s - %s)</p>" +
                        "<p style='margin:4px 0;'><b>Entry Time:</b> %s</p>" +
                        "</div></html>",
                        res.getSlotNumber(),
                        res.getRegistrationNumber(),
                        res.getOwnerName(),
                        res.getCollegeId(),
                        res.getUserType().getDisplayName(),
                        DateTimeUtil.format(res.getEntryTime())
                    );
                    lblEntryResult.setText(html);
                    txtEntryRegNo.setText("");
                    parentFrame.refreshDashboard();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    lblEntryResult.setText("<html><div style='color: #991B1B;'><b>Entry Failed:</b><br>" + cause.getMessage() + "</div></html>");
                    UITheme.showError(ParkingEntryExitPanel.this, cause.getMessage(), "Entry Rejected");
                }
            }
        };
        worker.execute();
    }

    private void processExit() {
        String regNo = txtExitRegNo.getText().trim();
        if (regNo.isEmpty()) {
            UITheme.showError(this, "Please enter a parked vehicle registration number.", "Missing Input");
            return;
        }

        boolean confirm = UITheme.showConfirm(this,
                "Process exit for vehicle [" + regNo.toUpperCase() + "] and release its parking slot?",
                "Confirm Exit");
        if (!confirm) return;

        btnProcessExit.setEnabled(false);
        lblExitDetails.setText("<html><i>Releasing slot and recording exit time...</i></html>");

        SwingWorker<ParkingService.ExitResult, Void> worker = new SwingWorker<>() {
            @Override
            protected ParkingService.ExitResult doInBackground() throws Exception {
                return parkingService.processVehicleExit(regNo);
            }

            @Override
            protected void done() {
                btnProcessExit.setEnabled(true);
                try {
                    ParkingService.ExitResult res = get();
                    String html = String.format(
                        "<html><div style='color: #1E3A8A;'>" +
                        "<h3 style='margin:0;'>✓ Vehicle Exit Completed!</h3>" +
                        "<p style='margin:4px 0;'><b>Freed Slot:</b> <span style='font-size:16px; color:#2563EB;'><b>%s</b></span></p>" +
                        "<p style='margin:4px 0;'><b>Vehicle:</b> %s</p>" +
                        "<p style='margin:4px 0;'><b>Owner:</b> %s</p>" +
                        "<p style='margin:4px 0;'><b>Parked Duration:</b> %s</p>" +
                        "<p style='margin:4px 0;'><b>Exit Time:</b> %s</p>" +
                        "</div></html>",
                        res.getSlotNumber(),
                        res.getRegistrationNumber(),
                        res.getOwnerName(),
                        res.getDurationFormatted(),
                        DateTimeUtil.format(res.getExitTime())
                    );
                    lblExitDetails.setText(html);
                    txtExitRegNo.setText("");
                    parentFrame.refreshDashboard();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    lblExitDetails.setText("<html><div style='color: #991B1B;'><b>Exit Failed:</b><br>" + cause.getMessage() + "</div></html>");
                    UITheme.showError(ParkingEntryExitPanel.this, cause.getMessage(), "Exit Error");
                }
            }
        };
        worker.execute();
    }
}
