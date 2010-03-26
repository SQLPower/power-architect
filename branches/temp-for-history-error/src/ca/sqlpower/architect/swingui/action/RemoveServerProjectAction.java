package ca.sqlpower.architect.swingui.action;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.enterprise.ProjectLocation;

public class RemoveServerProjectAction extends AbstractAction {

	private final ProjectLocation location;
	
	public RemoveServerProjectAction (ProjectLocation location) {
		this.location = location;
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
			ArchitectClientSideSession.deleteServerWorkspace(location);
		} catch (Exception ex) {
			throw new RuntimeException("A problem has occured while deleting the a workspace", ex);
		}
	}
}
