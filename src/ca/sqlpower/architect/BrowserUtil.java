package ca.sqlpower.architect;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for dealing with Web Browsers.
 * For now uses operating system browser and knows about os.name;
 * when Java 6 becomes common, should delegate to java.awt.Desktop.
 */
public class BrowserUtil {

    final static String OS_NAME = System.getProperty("os.name");
    final static String OS_VER = System.getProperty("os.version");

    /** Launches the default browser to display a URL.
     * @throws IOException
     */
    public static void launch(String uri) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process p = null;
        if (OS_NAME.contains("Windows")) {
            p = runtime.exec("cmd /C \"start " + uri + "\"");
        } else if (OS_NAME.startsWith("Mac OS")) {
            p = runtime.exec("open " + uri);
        } else {
            // XXX check PATH for mozilla OR firefox? Opera????
            p = runtime.exec("firefox " + uri);
        }
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException("Problem waiting for browser", e);
        }
        final InputStream errorStream = p.getErrorStream();
        int c = -1;
        do {
            if (errorStream.available() > 0) {
                c = errorStream.read();
                System.out.print((char)c);
            }
        } while (c != -1);
    }
}
