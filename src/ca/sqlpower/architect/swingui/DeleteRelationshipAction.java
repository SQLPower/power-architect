package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;

public class DeleteRelationshipAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(DeleteRelationshipAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;
	
	public DeleteRelationshipAction() {
		super("Delete Relationship");
	}

	public void actionPerformed(ActionEvent evt) {
		Selectable invoker = pp.getSelection();
		if (invoker instanceof Relationship) {
			Relationship r = (Relationship) invoker;
			SQLRelationship sr = r.getModel();
			sr.getPkTable().removeExportedKey(sr);
			sr.getFkTable().removeImportedKey(sr);
		} else {
			JOptionPane.showMessageDialog((JComponent) invoker,
										  "The selected item type is not recognised");
		}
	}

	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
}
