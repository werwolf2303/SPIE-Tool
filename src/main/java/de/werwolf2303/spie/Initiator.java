package de.werwolf2303.spie;


import de.werwolf2303.spie.gui.MainGUI;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Objects;

public class Initiator {
    public static File outputDirectory;
    public static File firmwareDirectory;

    public static void main(String[] args) {
        //args = new String[] {"--console", "'Firmware 18.0.0'", "ProfileImages"};

        new SystemDetect();


        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                deleteDirectory(new File(outputDirectory, "0"));
                try {
                    for(String s : IOUtils.readLines(new FileReader(new File(outputDirectory, ".index")))) {
                        new File(outputDirectory, s).delete();
                    }
                    new File(outputDirectory, ".index").delete();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                new File(outputDirectory, "nstool").delete();
            }
        }));

        if(SystemDetect.osType == SystemDetect.OSType.win) {
            System.out.println("Windows is currently not supported");
            System.exit(-1);
        }

        try {
            if (!args[0].equals("--console")) System.err.println("Expected firmware folder and output folder\n\nspie.jar --console 'Firmware 18.0.0' ProfileImages");
            if (!args[1].isEmpty()) {
                if (!args[2].isEmpty()) {
                    firmwareDirectory = new File(args[1].replaceAll("'", "").replaceAll("\"", ""));
                    outputDirectory = new File(args[2].replaceAll("'", "").replaceAll("\"", ""));
                    startExtraction();
                }
            }
        }catch (ArrayIndexOutOfBoundsException e) {
            new MainGUI().launch();
        }
    }

    public static void startExtraction() {
        try {
            IOUtils.write(IOUtils.toByteArray(FileBuilder.getNSTool()), Files.newOutputStream(new File(outputDirectory, "nstool").toPath()));
            if(!new File(outputDirectory, "nstool").setExecutable(true)) {
                System.out.println("nstool maybe not executable");
            }
        }catch (NullPointerException e) {
            System.err.println("NSTool not compatible for Sys:" + SystemDetect.osType.name() + " Arch:" + SystemDetect.archType.name());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //b4216df1ddc353c939ee5a983ee05ebb => NCA Archive with profile images

        for(File f : Objects.requireNonNull(firmwareDirectory.listFiles(pathname -> pathname.getAbsolutePath().toLowerCase().endsWith(".nca")))) {
            if(f.getName().toLowerCase().contains("b4216df1ddc353c939ee5a983ee05ebb")) {
                try {
                    executeInSys(new String[]{"sh", "-c", "\"" + new File(outputDirectory, "nstool").getAbsolutePath() + "\" -x \"" + outputDirectory.getAbsolutePath() + "/\" \"" + f.getAbsolutePath() + "\""});
                }catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        try {
            for(Object[] objects : FileBuilder.getCSSPIE()) {
                String source = (String) objects[0];
                String filename = (String) objects[1];
                IOUtils.write(IOUtils.toByteArray(Objects.requireNonNull(Initiator.class.getResourceAsStream(source))), Files.newOutputStream(new File(outputDirectory, filename).toPath()));
                if(!new File(outputDirectory, filename).setExecutable(true)) {
                    System.out.println("CSSPIE maybe not executable");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for(File f : Objects.requireNonNull(new File(outputDirectory, "0" + File.separator + "chara").listFiles(pathname -> pathname.getAbsolutePath().toLowerCase().endsWith(".szs")))) {
            try {
                System.out.println("Extracting profile image: " + f.getName());
                executeInSys(new String[]{"sh", "-c", "\"" + new File(outputDirectory, "SwitchProfileImageExtractor").getAbsolutePath() + "\" \"" + f.getAbsolutePath() + "\" \"" + outputDirectory.getAbsolutePath() + "\""});
            }catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void deleteDirectory(File directoryToBeDeleted) {
        for(File file : Objects.requireNonNull(directoryToBeDeleted.listFiles())) {
            if(file.isDirectory()) deleteDirectory(file);
            file.delete();
        }
        directoryToBeDeleted.delete();
    }

    private static void executeInSys(String[] command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.inheritIO().start().waitFor();
    }
}
