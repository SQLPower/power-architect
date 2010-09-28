package regress;

import junit.framework.*;

public class ArchitectBusinessTestSuite extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(SaveLoadTest.class);
        return suite;
    }
}
