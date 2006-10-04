package ca.sqlpower.architect.layout;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;

import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TablePane;

public interface ArchitectLayoutInterface {

	/**
	 * Setup the layout algoritm.
	 * 
     * TODO change the Rectangle into a point.  This should indicate where to start
     * but the layout should determine dimensions 
     * 
	 * @param nodes  The list of entities to be placed on  
	 * @param preferedFrames The prefered number of animation frames, may be ignored by the layout algorithm
	 */
	public void setup(List<TablePane> nodes, List<Relationship> edges, Rectangle frame);
	
	public Dimension getNewArea(List<TablePane> nodes);
	
	public void setProperty(String key, Object value);
	
	public Object getProperty(String key);
	
	/**
	 * Interrupts the algorithm and puts play pen in a consistent state.
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

}
