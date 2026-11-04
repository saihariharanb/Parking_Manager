package com.college.parking.ui.components;

import com.college.parking.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Modern dashboard metric card showing key statistical indicators.
 */
public class StatCard extends ModernCard {

    private final JLabel lblValue;
    private final JLabel lblTitle;
    private final JLabel lblSubtitle;
    private final Color accentColor;

    public StatCard(String title, String initialValue, String subtitle, Color accentColor) {
        super(new BorderLayout(8, 8));
        this.accentColor = accentColor;

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_BOLD);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        lblValue = new JLabel(initialValue);
        lblValue.setFont(UITheme.FONT_METRIC);
        lblValue.setForeground(UITheme.TEXT_DARK);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(accentColor);

        contentPanel.add(lblTitle);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(lblValue);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(lblSubtitle);

        add(contentPanel, BorderLayout.CENTER);
        setPreferredSize(new Dimension(220, 115));
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw top accent bar
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(accentColor);
        g2.fillRoundRect(14, 2, 40, 4, 3, 3);
        g2.dispose();
    }
}
