package de.werwolf2303.spie.gui;

import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import javax.sound.sampled.Line;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileImage extends JPanel {
    byte[] image = null;
    boolean mEntered = false;
    boolean mExited = false;
    boolean mClicked = false;
    Color backgroundColor = Color.white;

    public ProfileImage() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(mClicked) {
                    mClicked = false;
                } else {
                    mClicked = true;
                }
                repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                mEntered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                mEntered = false;
                repaint();
            }
        });
    }

    public void setImage(InputStream stream) throws IOException {
        this.image = IOUtils.toByteArray(stream);
    }

    public void setColored(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.repaint();
    }

    public byte[] getColored() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
        BufferedImage bi = new BufferedImage(bufferedImage.getWidth(),bufferedImage.getHeight(),BufferedImage.TYPE_INT_RGB);
        for (int x=0;x<bufferedImage.getWidth();x++){
            for (int y=0;y<bufferedImage.getHeight();y++){
                int rgba = bufferedImage.getRGB(x,y);
                boolean isTrans = (rgba & 0xff000000) == 0;
                if (isTrans){
                    bi.setRGB(x,y, (this.backgroundColor.getRGB()&0x00ffffff));
                } else {
                    bi.setRGB(x,y,rgba);
                }
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", os);
        return os.toByteArray();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(mClicked) {
            g.setColor(Color.cyan);
            g.fillRect(0, 0, getWidth(), getHeight());
        }else{
            if(mEntered) {
                g.setColor(Color.lightGray);
                g.fillRect(0, 0, getWidth(), getHeight());
            }else {
                g.setColor(Color.darkGray);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
        if(image != null) {
            g.setColor(this.backgroundColor);
            g.fillRect(5, 5, getWidth() - 5, getHeight() - 5);
            try {
                g.drawImage(ImageIO.read(new ByteArrayInputStream(image)).getScaledInstance(getWidth() - 5, getHeight() - 5, Image.SCALE_SMOOTH), 5, 5, getWidth() - 5, getHeight() - 5, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
