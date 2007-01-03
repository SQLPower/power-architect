package ca.sqlpower.architect;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class BrowserUtil {

    final static String OS_NAME = System.getProperty("os.name");
    final static String OS_VER = System.getProperty("os.version");

    public static void main(String[] args) throws IOException, URISyntaxException {
        System.out.println(OS_NAME + OS_VER);
        launch(new URI("http://www.sqlpower.ca/"));
    }

    /** Launches the default browser to display a URI.
     * @throws IOException */
    public static void launch(URI uri) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        if (OS_NAME.equals("windows")) {
            runtime.exec(uri.toString());
        } else if (OS_NAME.equals("macos")) {
            runtime.exec("open " + uri);
        } else {
            runtime.exec("mozilla " + uri);
        }
    }
}
