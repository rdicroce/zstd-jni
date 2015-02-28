package com.github.luben.zstd.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public enum Native {
    ;

    private static String osName() {
        return System.getProperty("os.name").toLowerCase().replace(' ', '_');
    }

    private static String osArch() {
        return System.getProperty("os.arch");
    }

    private static String libExtension() {
        if (osName().contains("os_x")) {
            return "dynlib";
        } else {
            return "so";
        }
    }

    private static String resourceName() {
        return "/" + osName() + "/" + osArch() + "/libzstd." + libExtension();
    }

    private static boolean loaded = false;

    public static synchronized boolean isLoaded() {
        return loaded;
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        String resourceName = resourceName();
        InputStream is = Native.class.getResourceAsStream(resourceName);
        if (is == null) {
            throw new UnsupportedOperationException("Unsupported OS/arch, cannot find " +
                    resourceName + ". Please try building from source.");
        }
        File tempLib;
        try {
            tempLib = File.createTempFile("libzstd", "." + libExtension());
            // copy to tempLib
            FileOutputStream out = new FileOutputStream(tempLib);
            try {
                byte[] buf = new byte[4096];
                while (true) {
                    int read = is.read(buf);
                    if (read == -1) {
                        break;
                    }
                    out.write(buf, 0, read);
                }
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    // ignore
                }
                System.load(tempLib.getAbsolutePath());
                loaded = true;
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                  // ignore
                }
                if (tempLib != null && tempLib.exists()) {
                    if (!loaded) {
                        tempLib.delete();
                    } else {
                        // try to delete on exit, does it work on Windows?
                        tempLib.deleteOnExit();
                    }
                }
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Cannot unpack libzstd");
        }
    }
}

