package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import ca.sqlpower.architect.*;
import java.util.*;
import java.sql.DatabaseMetaData;
import org.apache.log4j.Logger;

public class RelationshipEditPanel extends JPanel
	implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(RelationshipEditPanel.class);

	protected JTextField relationshipName;
	protected JComboBox  relationshipPkTable;

	protected void cleanup() {
	
	
	}

	// ------------------ ARCHITECT PANEL INTERFACE ---------------------
	
	/**
	 * Does nothing because this version of ColumnEditPanel works
	 * directly on the live data.
	 */
	public void applyChanges() {
		cleanup();
	}

	/**
	 * Does nothing because this version of ColumnEditPanel works
	 * directly on the live data.
	 *
	 * <p>XXX: in architect version 2, this will undo the changes to
	 * the model.
	 */
	public void discardChanges() {
		cleanup();
	}
}
