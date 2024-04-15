package de.werwolf2303.spie.gui;

import de.werwolf2303.spie.Initiator;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainGUI extends JPanel {
    JFrame main = new JFrame("Switch firmware profile image extractor (SPIE) - v1.0");

    JPanel controls;
    JButton extractbutton;
    JButton selectcolorbutton;
    ColorPreview colorpreview;
    JScrollPane imageholder;
    JPanel imagespanel;
    String selectedPathCache = null;

    ArrayList<ProfileImage> images = new ArrayList<>();

    public MainGUI() {
        setLayout(null);

        controls = new JPanel();
        controls.setBounds(0, 236, 420, 36);
        add(controls);
        controls.setLayout(null);

        extractbutton = new JButton("Export profile image");
        extractbutton.setBounds(0, 6, 240, 23);
        controls.add(extractbutton);

        selectcolorbutton = new JButton("Select color");
        selectcolorbutton.setBounds(247, 6, 137, 23);
        controls.add(selectcolorbutton);

        selectcolorbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color selected = JColorChooser.showDialog(main, "Choose profile background color", colorpreview.getColor());
                if(selected != null) {
                    colorpreview.setColor(selected);
                    for(ProfileImage image : images) {
                        image.setColored(selected);
                    }
                }
            }
        });

        colorpreview = new ColorPreview(24, 23);
        colorpreview.setBounds(384, 6, 24, 23);
        controls.add(colorpreview);

        imageholder = new JScrollPane();
        imageholder.setBounds(0, 0, 420, 238);
        add(imageholder);

        imagespanel = new JPanel();
        imagespanel.setBackground(Color.black);
        imagespanel.setLayout(null);

        extractbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                if(selectedPathCache != null) chooser.setCurrentDirectory(new File(selectedPathCache));
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                switch (chooser.showOpenDialog(null)) {
                    case JFileChooser.ERROR_OPTION:
                    case JFileChooser.CANCEL_OPTION:
                        System.exit(0);
                    case JFileChooser.APPROVE_OPTION:
                        File outputDirectory = chooser.getSelectedFile();
                        selectedPathCache = outputDirectory.getParentFile().getAbsolutePath();
                        int counter = 0;
                        for(ProfileImage image : images) {
                            if(image.mClicked) {
                                try {
                                    IOUtils.write(image.getColored(), Files.newOutputStream(new File(outputDirectory, "ProfileImage" + counter + ".png").toPath()));
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            counter++;
                        }
                        JOptionPane.showMessageDialog(main, "Profile images exported");
                }
            }
        });
    }

    public void launch() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return new File(f, "b4216df1ddc353c939ee5a983ee05ebb.nca").exists();
            }

            @Override
            public String getDescription() {
                return null;
            }
        });
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        switch (chooser.showOpenDialog(null)) {
            case JFileChooser.ERROR_OPTION:
            case JFileChooser.CANCEL_OPTION:
                System.exit(0);
            case JFileChooser.APPROVE_OPTION:
                JOptionPane.showMessageDialog(null, "The next step can take a while! Please be patient!");
                if (!new File(System.getProperty("java.io.tmpdir"), "spie-tool").mkdir()) {
                    System.err.println("Failed to create tmp directory");
                    System.exit(-1);
                }
                Initiator.outputDirectory = new File(System.getProperty("java.io.tmpdir"), "spie-tool");
                Initiator.firmwareDirectory = chooser.getSelectedFile();
                Initiator.startExtraction();
        }

        addProfileImages();

        main.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                main.dispose();
                System.exit(0);
            }
        });
        main.setPreferredSize(new Dimension(420, 300));
        main.setResizable(false);
        main.getContentPane().add(this);
        main.setVisible(true);
        main.pack();
    }


    private void addProfileImages() {
        int ycache = 6;
        int colcount = 0;
        int xcache = 6;
        for(File f : Objects.requireNonNull(new File(System.getProperty("java.io.tmpdir"), "spie-tool").listFiles(pathname -> pathname.getAbsolutePath().contains(".png")))) {
            if(colcount == 3) {
                xcache = 6;
                ycache+=137;
                colcount = 0;
            }
            ProfileImage image = new ProfileImage();
            try {
                image.setImage(Files.newInputStream(f.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            image.setBounds(xcache, ycache, 113, 113);
            images.add(image);
            imagespanel.add(image);
            xcache+=137;
            colcount+=1;
        }
        imagespanel.setPreferredSize(new Dimension(imagespanel.getWidth(),ycache+137));
        imageholder.setViewportView(imagespanel);
    }
}
