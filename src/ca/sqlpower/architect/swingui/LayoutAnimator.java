package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Timer;

import ca.sqlpower.architect.layout.ArchitectLayoutInterface;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;

public class LayoutAnimator implements ActionListener {

	private ArchitectLayoutInterface layout;
	private Timer timer;

	private List<TablePane> tables;
	private PlayPen pp;
	
	public ArchitectLayoutInterface getLayout() {
		return layout;
	}


	public void setLayout(ArchitectLayoutInterface layout) {
		this.layout = layout;
	}


	public void actionPerformed(ActionEvent e) {
		if (pp == null)
		{
			pp = ArchitectFrame.getMainInstance().playpen;
		}
		if (layout.isDone() )
		{
			timer.stop();
			layout.done();
			for (TablePane tp: tables)
			{
				tp.firePlayPenComponentMoveEnd(tp.getLocation());
			}
		}
		else
		{
			
			layout.nextFrame();
			pp.revalidate();
			
		}
	}


	public Timer getTimer() {
		return timer;
	}

	public void setPlayPen(PlayPen playPen)
	{
		pp = playPen;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}
	public void setTablePanes(List <TablePane> tablePanes)
	{
		tables = tablePanes;
	}
}
