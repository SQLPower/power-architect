/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
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
