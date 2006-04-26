package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import ca.sqlpower.architect.layout.ArchitectLayoutInterface;

public class LayoutAnimator implements ActionListener {

	private PlayPen pp;
	private Timer timer;
	private ArchitectLayoutInterface layout;

	public LayoutAnimator(PlayPen pp, Timer timer, ArchitectLayoutInterface layout) {
		this.pp = pp;
		this.timer = timer;
		this.layout = layout;
		pp.startCompoundEdit("Auto Layout");
	}
	
	public void actionPerformed(ActionEvent e) {
		if (layout.isDone()) {
			timer.stop();
			layout.done();
			pp.endCompoundEdit("Layout animation finished");
		} else {
			layout.nextFrame();
			pp.revalidate();
		}
	}

	public Timer getTimer() {
		return timer;
	}

	public ArchitectLayoutInterface getLayout() {
		return layout;
	}
}
