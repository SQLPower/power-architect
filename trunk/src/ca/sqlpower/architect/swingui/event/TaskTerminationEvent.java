/*
 * Created on Aug 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect.swingui.event;

import ca.sqlpower.architect.swingui.ProgressWatcher;

/**
 * @author jack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TaskTerminationEvent {
	private ProgressWatcher source;

	public TaskTerminationEvent(ProgressWatcher source) {
		this.source = source;
	}
	
	public ProgressWatcher getSource() {
		return source; 
	}
}
