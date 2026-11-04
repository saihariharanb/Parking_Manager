package com.college.parking.ui.theme;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * UITheme defines the color palette, typography, borders, and styling helpers
 * across the College Parking Manager application.
 */
public class UITheme {

    // Color Palette
    public static final Color BG_LIGHT = new Color(248, 250, 252);         // #F8FAFC
    public static final Color BG_CARD = Color.WHITE;                       // #FFFFFF
    public static final Color NAVY_SIDEBAR = new Color(15, 23, 42);        // #0F172A
    public static final Color NAVY_SIDEBAR_HOVER = new Color(30, 41, 59);  // #1E293B
    public static final Color NAVY_SIDEBAR_ACTIVE = new Color(37, 99, 235);// #2563EB
    public static final Color PRIMARY_BLUE = new Color(37, 99, 235);       // #2563EB
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);      // #1D4ED8
    public static final Color SUCCESS_GREEN = new Color(16, 185, 129);     // #10B981
    public static final Color SUCCESS_LIGHT = new Color(236, 253, 245);    // #ECFDF5
    public static final Color DANGER_RED = new Color(239, 68, 68);         // #EF4444
    public static final Color DANGER_LIGHT = new Color(254, 242, 242);     // #FEF2F2
    public static final Color WARNING_AMBER = new Color(245, 158, 11);     // #F59E0B
    public static final Color WARNING_LIGHT = new Color(254, 243, 199);    // #FEF3C7
    public static final Color TEXT_DARK = new Color(15, 23, 42);           // #0F172A
    public static final Color TEXT_MUTED = new Color(100, 116, 139);       // #64748B
    public static final Color BORDER_COLOR = new Color(226, 232, 240);     // #E2E8F0

    // Typography
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_METRIC = new Font("Segoe UI", Font.BOLD, 30);

    /**
     * Initializes FlatLaf Light Look & Feel with system antialiasing.
     */
    public static void initializeTheme() {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("CheckBox.arc", 6);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("Table.selectionBackground", new Color(238, 242, 255));
            UIManager.put("Table.selectionForeground", TEXT_DARK);
            UIManager.put("Table.gridColor", BORDER_COLOR);
            UIManager.put("TableHeader.separatorColor", BORDER_COLOR);
        } catch (Exception e) {
            System.err.println("Could not load FlatLaf, falling back to system L&F: " + e.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }
    }

    /**
     * Creates a rounded subtle border for cards.
     */
    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        );
    }

    /**
     * Styles a JTable for modern appearance.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }
    }

    public static void showSuccess(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
}
