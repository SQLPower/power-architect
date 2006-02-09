package ca.sqlpower.architect.layout;

import java.util.List;

import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TablePane;

public interface ArchitectLayoutInterface {

	/**
	 * Setup the layout algoritm.
	 * 
	 * @param nodes  The list of entities to be placed on  
	 * @param preferedFrames The prefered number of animation frames, may be ignored by the layout algorithm
	 */
	public void setup(List<TablePane> nodes, List<Relationship> edges);
	
	public void setProperty(String key, Object value);
	
	public Object getProperty(String key);
	
	/**
	 * Interupt the algorithm and put in a consistant state
	 *
	 */
	public void done();
	
	/**
	 * Check and see if the layout is finished
	 * 
	 * @return true if the layout algorithm is finished with the layout, false other wise
	 */
	public boolean isDone();
	
	/**
	 * Draw the next frame
	 * 
	 */
	public void nextFrame();
	
	public void setPlayPen(PlayPen pp);
	
}
