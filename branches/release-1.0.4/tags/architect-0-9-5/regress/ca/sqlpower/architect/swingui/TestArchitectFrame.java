package ca.sqlpower.architect.swingui;

import junit.framework.TestCase;

public class TestArchitectFrame extends TestCase {
	
	private ArchitectFrame af;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		af = ArchitectFrame.getMainInstance();
	}
	
	public void testAutoLayoutAction() {
		assertNotNull(af.getAutoLayoutAction());
		assertSame(af.getProject().getPlayPen(), af.getAutoLayoutAction().getPlayPen());
		
		// FIXME: should check that the toolbar button of the action exists!
	}
}
