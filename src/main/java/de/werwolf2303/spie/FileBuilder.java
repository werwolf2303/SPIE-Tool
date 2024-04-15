package de.werwolf2303.spie;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class FileBuilder {
    public static InputStream getNSTool() throws NullPointerException {
        String osType = SystemDetect.osType.name();
        String architecture = SystemDetect.archType.name();
        InputStream stream = Initiator.class.getResourceAsStream("/nstool/" + osType + architecture + "/nstool");
        assert(stream!=null);
        return stream;
    }

    public static ArrayList<Object[]> getCSSPIE() throws NullPointerException, IOException {
        ArrayList<Object[]> files = new ArrayList<>();
        String osType = SystemDetect.osType.name();
        String architecture = SystemDetect.archType.name();
        files.add(new Object[]{"/csspie/" + osType + architecture + "/.index", ".index"});
        for(String s : IOUtils.readLines(Objects.requireNonNull(Initiator.class.getResourceAsStream("/csspie/" + osType + architecture + "/.index")))) {
            files.add(new Object[] {"/csspie/" + osType + architecture + "/" + s, s});
        }
        return files;
    }
}
