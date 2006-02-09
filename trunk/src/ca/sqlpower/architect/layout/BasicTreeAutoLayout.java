package ca.sqlpower.architect.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.TablePane;

public class BasicTreeAutoLayout extends AbstractLayout {
	private static final Logger logger = Logger.getLogger(BasicTreeAutoLayout.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	private PlayPen pp;
	
	private boolean animationEnabled = true;

	/**
	 * The number of frames to try for in the repositioning animation.
	 */
	private int numFramesInAnim = 50;

	/**
	 * The number of frames per second to render.
	 */
	private int framesPerSecond = 25;
	
	private HashMap<TablePane,Point> newLocations;
	private Map<TablePane,Point> origLocations;

	private int frame =0;
	public BasicTreeAutoLayout() {

	}

	private Point doRecursiveLayout(List<TablePane> tpList, Point startPoint, Map<TablePane,Point> alreadyDone) throws ArchitectException {
		Rectangle b = new Rectangle();
		int x = startPoint.x;
		int y = startPoint.y;
		
		for (TablePane tp : tpList) {
			if (alreadyDone.containsKey(tp)) continue;
			
			// place this table
			tp.getBounds(b);
			Point newLoc = new Point(x, y);
			alreadyDone.put(tp, newLoc);

			List<TablePane> relatedTables = new ArrayList<TablePane>();
			for (SQLRelationship key : tp.getModel().getExportedKeys()) {
				TablePane relatedTable = pp.findTablePane(key.getFkTable());
				if (!alreadyDone.containsKey(relatedTable)) relatedTables.add(relatedTable);
			}
			for (SQLRelationship key : tp.getModel().getImportedKeys()) {
				TablePane relatedTable = pp.findTablePane(key.getPkTable());
				if (!alreadyDone.containsKey(relatedTable)) relatedTables.add(relatedTable);
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

	public boolean isAnimationEnabled() {
		return animationEnabled;
	}

	public void setAnimationEnabled(boolean animationEnabled) {
		this.animationEnabled = animationEnabled;
	}

	public void setup(List<TablePane> nodes, List<Relationship> edges) {
		origLocations = new HashMap<TablePane,Point>();
		
			for (TablePane tp : nodes) {
				origLocations.put(tp, tp.getLocation());
			}
			Point p = new Point();
			newLocations = new HashMap<TablePane,Point>();
			List<TablePane> tablePanes = nodes;
			try {
				doRecursiveLayout(tablePanes, p, newLocations);
				if (logger.isDebugEnabled()) {
					for (Map.Entry<TablePane, Point> entry : newLocations.entrySet()) {
						TablePane tp = entry.getKey();
						Point newLoc = entry.getValue();
						Point oldLoc = origLocations.get(tp);
						
						logger.debug("Table "+tp.getModel().getName()+": old="+oldLoc.x+","+oldLoc.y+"; new="+newLoc.x+","+newLoc.y);
					}
				}
		
			} catch (ArchitectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}

	public void done() {
		for (Map.Entry<TablePane, Point> entry : newLocations.entrySet()) {
			entry.getKey().setMovePathPoint(entry.getValue().x, entry.getValue().y);
		}
		for (Map.Entry<TablePane, Point> entry : newLocations.entrySet()) {
			entry.getKey().setMoving(false);
		}
		frame = numFramesInAnim;
	}

	public boolean isDone() {
		// TODO Auto-generated method stub
		return frame >= numFramesInAnim;
	}

	public void nextFrame() {
		frame++;
		if (frame >= numFramesInAnim){
			for (Map.Entry<TablePane, Point> entry : newLocations.entrySet()) {
				entry.getKey().setMoving(false);
			}
		}
		double progress = ((double) frame) / ((double) numFramesInAnim);
		logger.debug(progress);
		for (Map.Entry<TablePane, Point> entry : newLocations.entrySet()) {
			TablePane tp = entry.getKey();
			Point newLoc = entry.getValue();
			Point oldLoc = origLocations.get(tp);
			
			int x = (int) (oldLoc.x + (double) (newLoc.x - oldLoc.x) * progress);
			int y = (int) (oldLoc.y + (double) (newLoc.y - oldLoc.y) * progress);
			
			tp.setMovePathPoint(x, y);
		}
		pp.repaint();
		
	}
}
