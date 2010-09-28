package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import ca.sqlpower.architect.layout.ArchitectLayout;

public class LayoutAnimator implements ActionListener {

	private PlayPen pp;
	private Timer timer;
	private ArchitectLayout layout;
    private boolean animationEnabled = true;
    private int framesPerSecond = 15;


    public LayoutAnimator(PlayPen pp, ArchitectLayout layout) {
		this.pp = pp;
		this.layout = layout;
	}
	
    public void startAnimation() {
        pp.startCompoundEdit("Auto Layout");
        if (!animationEnabled) {
            layout.done();
        } else {
            timer = new Timer( (int) (1.0 / ((double) framesPerSecond) * 1000.0), null);
            timer.addActionListener(this);
            timer.start();
        }
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
    
	public ArchitectLayout getLayout() {
		return layout;
	}
    
	public boolean isAnimationEnabled() {
	    return animationEnabled;
	}
	
	public void setAnimationEnabled(boolean animationEnabled) {
	    this.animationEnabled = animationEnabled;
	}
	
	public int getFramesPerSecond() {
	    return framesPerSecond;
	}
	
	public void setFramesPerSecond(int framesPerSecond) {
	    this.framesPerSecond = framesPerSecond;
	}
}
