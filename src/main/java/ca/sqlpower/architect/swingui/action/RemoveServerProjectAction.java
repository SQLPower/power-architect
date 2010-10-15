package ca.sqlpower.architect.swingui.action;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.enterprise.client.ProjectLocation;

public class RemoveServerProjectAction extends AbstractAction {

	private final ProjectLocation location;
	private final ArchitectSession session;
	
	public RemoveServerProjectAction (ProjectLocation location, ArchitectSession session) {
		this.location = location;
		this.session = session;
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
			ArchitectClientSideSession.deleteServerWorkspace(location, session);
		} catch (Exception ex) {
			throw new RuntimeException("A problem has occured while deleting the a workspace", ex);
		}
	}
}
