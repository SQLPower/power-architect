package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;
import java.sql.*;

public class InsertColumnAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(InsertColumnAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;
	
	public InsertColumnAction(PlayPen pp) {
		super("Insert Column");
		this.pp = pp;
	}

	public void actionPerformed(ActionEvent evt) {
		Selectable invoker = pp.getSelection();
		if (invoker instanceof TablePane) {
			TablePane tp = (TablePane) invoker;
			int idx = tp.getSelectedColumnIndex();
			try {
				if (idx < 0) idx = tp.getModel().getChildCount();
			} catch (ArchitectException e) {
				idx = 0;
			}
			tp.getModel().addChild(idx, new SQLColumn(tp.getModel(),
													  "new column",
													  Types.INTEGER,
													  "Integer",
													  10,
													  0,
													  DatabaseMetaData.columnNullable,
													  null,
													  null,
													  null,
													  false));
		} else {
			JOptionPane.showMessageDialog((JComponent) invoker,
										  "The selected item type is not recognised");
		}
	}
}
