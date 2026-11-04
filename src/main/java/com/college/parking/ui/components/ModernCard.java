package com.college.parking.ui.components;

import com.college.parking.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Modern card container panel with rounded corners and clean border.
 */
public class ModernCard extends JPanel {

    private int cornerRadius = 12;
    private Color backgroundColor = UITheme.BG_CARD;
    private Color borderColor = UITheme.BORDER_COLOR;

    public ModernCard() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }

    public ModernCard(LayoutManager layout) {
        setOpaque(false);
        setLayout(layout);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // Draw border
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
