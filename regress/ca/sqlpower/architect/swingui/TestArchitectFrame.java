package ca.sqlpower.architect.swingui;

import junit.framework.TestCase;

public class TestArchitectFrame extends TestCase {
	
	private ArchitectFrame af;
    private ArchitectSwingSession session;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
		session = context.createSession();
        af = session.getArchitectFrame();
	}
	
	public void testAutoLayoutAction() {
		assertNotNull(af.getAutoLayoutAction());
		assertSame(session.getPlayPen(), af.getAutoLayoutAction().getPlayPen());
		
		// FIXME: should check that the toolbar button of the action exists!
	}
}
