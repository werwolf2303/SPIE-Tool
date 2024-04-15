package de.werwolf2303.spie;

public class SystemDetect {
    public enum OSType {
        win,
        mac,
        linux
    }

    public enum ArchType {
        X86,
        X64,
        ARM64
    }

    public static OSType osType = null;
    public static ArchType archType = null;

    public SystemDetect() {
        if(System.getProperty("os.name").toLowerCase().contains("mac")) osType = OSType.mac;
        if(System.getProperty("os.name").toLowerCase().contains("win")) osType = OSType.win;
        //Falling back to linux
        if(osType == null) osType = OSType.linux;

        if(System.getProperty("os.arch").equalsIgnoreCase("x86")) archType = ArchType.X86;
        if(System.getProperty("os.arch").equalsIgnoreCase("x86_64")) archType = ArchType.X64;
        if(System.getProperty("os.arch").equalsIgnoreCase("amd64")) archType = ArchType.X64;
        if(System.getProperty("os.arch").equalsIgnoreCase("arm64")) archType = ArchType.ARM64;
        //Falling back to 64bit
        if(archType == null) archType = ArchType.X64;
    }
}
