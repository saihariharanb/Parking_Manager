package com.college.parking.ui.components;

import com.college.parking.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern styled JButton with rounded corners and hover feedback.
 */
public class ModernButton extends JButton {

    public enum ButtonType {
        PRIMARY(UITheme.PRIMARY_BLUE, UITheme.PRIMARY_HOVER, Color.WHITE),
        SUCCESS(UITheme.SUCCESS_GREEN, new Color(5, 150, 105), Color.WHITE),
        DANGER(UITheme.DANGER_RED, new Color(220, 38, 38), Color.WHITE),
        SECONDARY(new Color(241, 245, 249), new Color(226, 232, 240), UITheme.TEXT_DARK);

        final Color normal;
        final Color hover;
        final Color text;

        ButtonType(Color normal, Color hover, Color text) {
            this.normal = normal;
            this.hover = hover;
            this.text = text;
        }
    }

    private final ButtonType type;
    private boolean isHovered = false;
    private int cornerRadius = 10;

    public ModernButton(String text, ButtonType type) {
        super(text);
        this.type = type;
        init();
    }

    public ModernButton(String text) {
        this(text, ButtonType.PRIMARY);
    }

    private void init() {
        setFont(UITheme.FONT_BOLD);
        setForeground(type.text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(10, 18, 10, 18));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!isEnabled()) {
            g2.setColor(new Color(203, 213, 225));
        } else if (isHovered) {
            g2.setColor(type.hover);
        } else {
            g2.setColor(type.normal);
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Optional subtle border for secondary
        if (type == ButtonType.SECONDARY) {
            g2.setColor(UITheme.BORDER_COLOR);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
