package ca.sqlpower.architect.swingui;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

public class ASUtilsTest extends TestCase {

    /** Trivial test, just make sure our class name appears */
    public void testStackTrace() {
        StringWriter sWriter = new StringWriter();
        PrintWriter pout = new PrintWriter(sWriter);
        ASUtils.printStackTrace(new Exception("Boo!"), pout);
        pout.close();
        String answer = sWriter.toString();
        assertTrue(answer.indexOf(this.getClass().getName()) >= 0);
    }
}
