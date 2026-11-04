package com.college.parking.ui.components;

import com.college.parking.model.ParkingSlot;
import com.college.parking.model.SlotStatus;
import com.college.parking.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Visual card representing an individual parking slot in the parking bay layout.
 * Shows Slot ID, Status indicator (green=available, red=occupied), and Vehicle Reg if occupied.
 */
public class SlotCard extends JPanel {

    private final ParkingSlot slot;
    private final JLabel lblSlotId;
    private final JLabel lblStatus;
    private final JLabel lblVehicle;

    public SlotCard(ParkingSlot slot) {
        this.slot = slot;
        setOpaque(false);
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        setPreferredSize(new Dimension(135, 95));
        setMinimumSize(new Dimension(120, 85));

        // Header: Slot ID & Status Pill
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        lblSlotId = new JLabel(slot.getSlotNumber());
        lblSlotId.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSlotId.setForeground(UITheme.TEXT_DARK);

        lblStatus = new JLabel(slot.getStatus().getDisplayName());
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        updateStatusLabelStyle();

        topPanel.add(lblSlotId, BorderLayout.WEST);
        topPanel.add(lblStatus, BorderLayout.EAST);

        // Center: Vehicle registration or Available placeholder
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        String regText = (slot.isOccupied() && slot.getCurrentVehicleReg() != null)
                ? slot.getCurrentVehicleReg()
                : (slot.getStatus() == SlotStatus.AVAILABLE ? "Available" : "Under Service");

        lblVehicle = new JLabel(regText);
        lblVehicle.setFont(new Font("Segoe UI", slot.isOccupied() ? Font.BOLD : Font.PLAIN, 12));
        lblVehicle.setForeground(slot.isOccupied() ? UITheme.DANGER_RED : UITheme.TEXT_MUTED);

        centerPanel.add(lblVehicle, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void updateStatusLabelStyle() {
        if (slot.getStatus() == SlotStatus.AVAILABLE) {
            lblStatus.setForeground(UITheme.SUCCESS_GREEN);
        } else if (slot.getStatus() == SlotStatus.OCCUPIED) {
            lblStatus.setForeground(UITheme.DANGER_RED);
        } else {
            lblStatus.setForeground(UITheme.WARNING_AMBER);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Card background
        Color bg = UITheme.BG_CARD;
        Color border;

        if (slot.getStatus() == SlotStatus.AVAILABLE) {
            border = UITheme.SUCCESS_GREEN;
            bg = UITheme.SUCCESS_LIGHT;
        } else if (slot.getStatus() == SlotStatus.OCCUPIED) {
            border = UITheme.DANGER_RED;
            bg = UITheme.DANGER_LIGHT;
        } else {
            border = UITheme.WARNING_AMBER;
            bg = UITheme.WARNING_LIGHT;
        }

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

        // Left accent indicator bar
        g2.setColor(border);
        g2.fillRoundRect(0, 0, 5, getHeight() - 1, 4, 4);

        // Border outline
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

        g2.dispose();
        super.paintComponent(g);
    }

    public ParkingSlot getSlot() {
        return slot;
    }
}
