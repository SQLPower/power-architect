/*
 * Created on Jul 30, 2007
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.profile;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ProfileTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for ca.sqlpower.architect.profile");
        //$JUnit-BEGIN$
        suite.addTestSuite(TestProfileManagerListeners.class);
        suite.addTestSuite(TestProfileCSV.class);
        suite.addTestSuite(TableProfileManagerTest.class);
        suite.addTestSuite(AbstractProfileResultTest.class);
        suite.addTestSuite(TestProfileManager.class);
        //$JUnit-END$
        return suite;
    }

}
