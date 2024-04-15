package de.werwolf2303.spie.gui;

import javax.swing.*;
import java.awt.*;

public class ColorPreview extends JPanel {
    Color color = Color.white;

    public void setColor(Color color) {
        this.color = color;
        repaint();
    }

    public ColorPreview(int width, int height) {
        setSize(width, height);
    }

    public Color getColor() {
        return this.color;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(color);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
