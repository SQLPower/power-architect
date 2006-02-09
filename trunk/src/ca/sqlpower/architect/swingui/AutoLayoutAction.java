package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.layout.ArchitectLayoutInterface;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;

public class AutoLayoutAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(AutoLayoutAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	private PlayPen pp;
	
	private boolean animationEnabled = true;

	private ArchitectLayoutInterface layout; 

	private int framesPerSecond = 25;

	public AutoLayoutAction() {
		super("Auto Layout",
				  ASUtils.createIcon("AutoLayout",
									"Automatic Table Layout",
									ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Automatic Layout");
	}

	public void actionPerformed(ActionEvent evt) {
		
		
		if (layout != null)
		{
			layout.setPlayPen(pp);
			List<TablePane> tablePanes = pp.getTablePanes();
			for(TablePane tp:tablePanes){
				tp.firePlayPenComponentMoveStart(tp.getLocation());
			}
			List<Relationship> relationships = pp.getRelationships();
			logger.debug("About to do layout. tablePanes="+tablePanes);
			logger.debug("About to do layout. relationships="+relationships);
			layout.setup(tablePanes,relationships);
			doAnimation(layout, tablePanes);
		}
		
	}
	
	private void doAnimation(ArchitectLayoutInterface layout, List<TablePane> tables) {
		
		if (!animationEnabled) {
			layout.done();
		} else {
		
			final Timer timer = new Timer( (int) (1.0 / ((double) framesPerSecond) * 1000.0), null);
			LayoutAnimator animator = new LayoutAnimator();
			animator.setLayout(layout);
			animator.setTimer(timer);
			animator.setPlayPen(pp);
			animator.setTablePanes(tables);
			timer.addActionListener(animator);
			timer.start();
		}
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

	public ArchitectLayoutInterface getLayout() {
		return layout;
	}

	public void setLayout(ArchitectLayoutInterface layout) {
		this.layout = layout;
	}
}
