/*
 * Created on Jul 25, 2007
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.swingui;

import java.io.IOException;

import ca.sqlpower.architect.ArchitectException;
import junit.framework.TestCase;

public class ArchitectSwingSessionImplTest extends TestCase {

    /**
     * A special stub context that can create a real ArchitectSwingSessionImpl,
     * which is what we are testing.
     */
    class StubContext extends TestingArchitectSwingSessionContext {

        public StubContext() throws IOException {
            super();
        }

        @Override
        public ArchitectSwingSession createSession(boolean showGUI) throws ArchitectException {
            return new ArchitectSwingSessionImpl(this, "testing");
        }
    }
    
    /**
     * Closing a session which has not had its frame created used to fail with
     * NPE. This is the regression test for that.
     */
    public void testCloseSessionWithoutGUI() throws Exception {
        ArchitectSwingSessionContext context = new StubContext();
        ArchitectSwingSession session = context.createSession(false);
        session.close();
    }
}
