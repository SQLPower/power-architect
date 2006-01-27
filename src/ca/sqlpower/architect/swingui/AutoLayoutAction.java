package ca.sqlpower.architect.swingui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLRelationship;

public class AutoLayoutAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(AutoLayoutAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;
	
	public AutoLayoutAction() {
		super("Auto Layout",
				  ASUtils.createIcon("AutoLayout",
									"Automatic Table Layout",
									ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Automatic Layout");
	}

	public void actionPerformed(ActionEvent evt) {
		try {
			Point p = new Point();
			Set<TablePane> alreadyDone = new HashSet<TablePane>();
			List<TablePane> tablePanes = (List<TablePane>) pp.getTablePanes();
			doRecursiveLayout(tablePanes, p, alreadyDone);
		} catch (ArchitectException ex) {
			logger.error("Error during auto-layout", ex);
			JOptionPane.showMessageDialog(null, "Error during auto-layout:\n\n"+ex.getMessage());
		}
	}
	
	private Point doRecursiveLayout(List<TablePane> tpList, Point startPoint, Set<TablePane> alreadyDone) throws ArchitectException {
		Rectangle b = new Rectangle();
		int x = startPoint.x;
		int y = startPoint.y;
		
		for (TablePane tp : tpList) {
			if (alreadyDone.contains(tp)) continue;
			
			// place this table
			tp.getBounds(b);
			tp.setBounds(x, y, b.width, b.height);
			
			alreadyDone.add(tp);

			List<TablePane> relatedTables = new ArrayList<TablePane>();
			for (SQLRelationship key : tp.getModel().getExportedKeys()) {
				TablePane relatedTable = pp.findTablePane(key.getFkTable());
				if (!alreadyDone.contains(relatedTable)) relatedTables.add(relatedTable);
			}
			for (SQLRelationship key : tp.getModel().getImportedKeys()) {
				TablePane relatedTable = pp.findTablePane(key.getPkTable());
				if (!alreadyDone.contains(relatedTable)) relatedTables.add(relatedTable);
			}

			// place the related tables to the right
			Point finishPoint = doRecursiveLayout(relatedTables, new Point(x + b.width + 60, y), alreadyDone);
			
			x = startPoint.x;
			y = Math.max(y + b.height + 10, finishPoint.y);
		}
		return new Point(x, y);
	}
	
	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
	
	public PlayPen getPlayPen() {
		return pp;
	}
}
