package ca.sqlpower.architect.layout;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;

/**
 * The ArchitectLayout interface is a generic interface to a collection of
 * graph layout algorithms.  Its features include optional animation of the layout,
 * and setting an overall maximum boundary for the laid-out graph.
 * <p>
 * Example for using this interface:
 * <pre>
 *   ArchitectLayout layout = new MyFavouriteLayout();
 *   layout.setup(myNodes, myEdges, myMaximumBounds);
 *   while (!layout.isDone()) {
 *     layout.nextFrame();
 *     myComponent.paint();
 *   }
 * </pre>
 *
 * @author matt
 */
public interface ArchitectLayout {

	/**
	 * Sets up the layout algoritm.  You have to call this before attempting
     * to use an ArchitectLayout instance to perform a layout.
	 * 
     * TODO change the Rectangle into a point.  This should indicate where to start
     * but the layout should determine dimensions 
     * 
	 * @param nodes  The list of entities to be placed on  
	 * @param preferedFrames The prefered number of animation frames, may be ignored by the layout algorithm
	 */
	public void setup(Collection<? extends LayoutNode> nodes, Collection<? extends LayoutEdge> edges, Rectangle frame);
	
	public Dimension getNewArea(List<? extends LayoutNode> nodes);
	
	/**
	 * Interrupts the algorithm and node bounds in a consistent state.
	 */
	public void done();
	
	/**
	 * Returns true iff the layout is finished.
	 * 
	 * @return true if the layout algorithm is finished with the layout, false otherwise
	 */
	public boolean isDone();
	
	/**
	 * Updates all the node locations to correspond with the positions they should
     * have in the next frame.
	 */
	public void nextFrame();

}
