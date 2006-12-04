package ca.sqlpower.architect.swingui;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

public class ASUtilsTest extends TestCase {

    /** Trivial test, just make sure our class name appears */
    public void testStackTrace() {
        String answer = getPrintOut(new Exception("Boo!"));
        assertTrue(answer.indexOf(this.getClass().getName()) >= 0);
    }

    final String FAKE_CLASS_NAME = "my.test";

    /** Make sure we get the correct number of elements truncated */
    public void testStackTraceArithmetic() {
        final int STACK_DEPTH = 16;
        assertTrue(STACK_DEPTH >= 1+ASUtils.MAX_JRE_ELEMENTS);
        Throwable t = fakeUpException(STACK_DEPTH);

        String answer = getPrintOut(t);
        assertTrue(answer.indexOf(FAKE_CLASS_NAME) >= 0);
        int expectedLength = STACK_DEPTH - ASUtils.MAX_JRE_ELEMENTS;
        System.out.println("ANSER=" + answer);
        System.out.println("EXPECT=" + expectedLength);
        assertTrue(answer.indexOf(expectedLength + " more") >= 0);
    }


    /** Border case : make sure the "more" does not appear if exactly 10 */
    public void testStackTraceBoundary() {
        Throwable t = fakeUpException(10);
        String answer = getPrintOut(t);
        assertFalse(answer.indexOf("more...") >= 0);
    }


    // Private Methods...

    private String getPrintOut(Throwable t) {
        StringWriter sWriter = new StringWriter();
        PrintWriter pout = new PrintWriter(sWriter);
        ASUtils.printStackTrace(t, pout);
        pout.close();
        return sWriter.toString();
    }

    private Throwable fakeUpException(int howMany) {
        StackTraceElement data[] = new StackTraceElement[howMany];
        data[0] = new StackTraceElement(FAKE_CLASS_NAME, "foo", "mytest.java", 123);
        for (int i = 1; i < howMany; i++) {
            data[i] = new StackTraceElement("java.lang.Fake", "method" + i, "yourtest.java", i);
        }
        Throwable t = new RuntimeException();
        t.setStackTrace(data);
        return t;
    }

    public void testClassNameStuff() {
        assertEquals("String", ASUtils.niceClassName(""));
        assertEquals("Object", ASUtils.niceClassName(new Object()));
    }
}
