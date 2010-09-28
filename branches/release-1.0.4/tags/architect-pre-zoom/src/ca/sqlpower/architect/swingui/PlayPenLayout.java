package ca.sqlpower.architect.swingui;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import org.apache.log4j.Logger;

/**
 * Manages the layout of the PlayPen.
 */
public class PlayPenLayout implements LayoutManager2 {
	private static final Logger logger = Logger.getLogger(PlayPenLayout.class);

	/**
	 * This is the PlayPen that we are managing the layout for.
	 */
	PlayPen parent;
	
	public PlayPenLayout(PlayPen parent) {
		this.parent = parent;
	}
	
	/**
	 * Does nothing.  Use the Object-style constraints, not String.
	 *
	 * @throws UnsupportedOperationException if called.
	 */
	public void addLayoutComponent(String name, Component comp) {
		throw new UnsupportedOperationException("Use addLayoutComponent(Component,Object) instead");
	}
	
	/**
	 * Positions the new component near the given point.
	 *
	 * @param comp the component which has been added
	 * @param position A java.awt.Point, near which the object
	 * should be positioned.  It will not overlap existing
	 * components in this play pen.  If this argument is null, the
	 * layout manager will do nothing for this component addition.
	 */
	public void addLayoutComponent(Component comp,
								   Object position) {
		if (position == null) return;
		Point pos = (Point) position;
		comp.setSize(comp.getPreferredSize());
		int nh = comp.getHeight();
		int nw = comp.getWidth();
		logger.debug("new comp x="+pos.x+"; y="+pos.y+"; w="+nw+"; h="+nh);
		
		RangeList rl = new RangeList();
		Rectangle cbounds = null;
		for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
			Component c = parent.getComponent(i);
			if (c.isVisible() && c != comp) {
				cbounds = c.getBounds(cbounds);
				if ( ! ( (cbounds.y+cbounds.height < pos.y) 
						 || (pos.y + nh < cbounds.y)
						 )
					 ) {
					logger.debug("blocking "+c.getName());
					rl.blockOut(cbounds.x, cbounds.width);
				} else {
					logger.debug("IGNORING "+c.getName());
				}
			}
		}
		
		logger.debug("final range list: "+rl);
		logger.debug("rightGap = max("+rl.findGapToRight(pos.x, nw)+","+pos.x+")");
		
		int rightGap = Math.max(rl.findGapToRight(pos.x, nw), pos.x);
		int leftGap = rl.findGapToLeft(pos.x, nw);
		
		logger.debug("pos.x = "+pos.x+"; rightGap = "+rightGap+"; leftGap = "+leftGap);
		if (rightGap - pos.x <= pos.x - leftGap) {
			pos.x = rightGap;
		} else {
			pos.x = leftGap;
		}
		comp.setLocation(pos);
		
		if (pos.x < 0) {
			translateAllComponents(Math.abs(pos.x), 0, false);
		}
		
		parent.scrollRectToVisible(comp.getBounds());
	}
	
	/**
	 * Translates all components left and down by the specified
	 * amounts.
	 *
	 * @param scrollToCompensate if true, this method tries to
	 * make it appear that the components didn't move by scrolling
	 * the viewport by the same amount as the components were
	 * translated.  If false, no scrolling is attempted.
	 */
	protected void translateAllComponents(int xdist, int ydist, boolean scrollToCompensate) {
		synchronized (parent) {
			Rectangle visibleArea = null;
			if (scrollToCompensate) {
				parent.getVisibleRect();
			}
			
			Point p = new Point();
			for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
				JComponent c = (JComponent) parent.getComponent(i);
				p = c.getLocation(p);
				p.x += xdist;
				p.y += ydist;
				c.setLocation(p);
			}
			
			if (scrollToCompensate) {
				visibleArea.x += xdist;
				visibleArea.y += ydist;
				parent.scrollRectToVisible(visibleArea);
			}
		}
	}
	
	/**
	 * Does nothing.
	 */
	public void removeLayoutComponent(Component comp) {
		logger.debug("PlayPenLayout.removeLayoutComponent");
	}
	
	/**
	 * Calculates the smallest rectangle that will completely
	 * enclose the visible components inside parent.
	 */
	public Dimension preferredLayoutSize(Container parent) {
		Rectangle cbounds = null;
		//int minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = 0, maxy = 0;
		int minx = 0, miny = 0, maxx = 0, maxy = 0;
		for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
			Component c = parent.getComponent(i);
			if (c.isVisible()) {
				cbounds = c.getBounds(cbounds);
				minx = Math.min(cbounds.x, minx);
				miny = Math.min(cbounds.y, miny);
				maxx = Math.max(cbounds.x + cbounds.width , maxx);
				maxy = Math.max(cbounds.y + cbounds.height, maxy);
			}
		}
		
		Dimension min = parent.getMinimumSize();
		return new Dimension(Math.max(maxx - minx, min.width),
							 Math.max(maxy - miny, min.height));
	}
	
	/**
	 * Identical to {@link #preferredLayoutSize(Container)}.
	 */
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}
	
	/**
	 * Identical to {@link #preferredLayoutSize(Container)}.
	 */
	public Dimension maximumLayoutSize(Container target) {
		return preferredLayoutSize(target);
	}
	
	public float getLayoutAlignmentX(Container target) {
		return 0.5f;
	}
	
	public float getLayoutAlignmentY(Container target) {
		return 0.5f;
	}
	
	/**
	 * Discards cached layout information.  Currently this is a no-op.
	 */
	public void invalidateLayout(Container target) {
		return;
	}
	
	/**
	 * Does nothing!  Components will stay put.
	 */
	public void layoutContainer(Container parent) {
		logger.debug("PlayPenLayout.layoutContainer");
	}
	
	protected static class RangeList {
		
		LinkedList blocks;
		
		public RangeList() {
			blocks = new LinkedList();
			
			// blockOut needs non-empty list with something at far right side
			blocks.add(new Block(Integer.MAX_VALUE, 0));
		}
		
		public void blockOut(int start, int length) {
			Block block = new Block(start, length);
			//logger.debug("blockOut "+block+": before "+blocks);
			ListIterator it = blocks.listIterator();
			while (it.hasNext()) {
				Block nextBlock = (Block) it.next();
				if (nextBlock.start > start) {
					it.previous();
					it.add(block);
					break;
				}
			}
			//logger.debug("blockOut "+block+": after  "+blocks);
		}
		
		public int findGapToRight(int start, int length) {
			int origStart = start;
			Iterator it = blocks.iterator();
			while (it.hasNext()) {
				Block block = (Block) it.next();
				
				if ( (start + length) < block.start ) {
					// current gap fits at right-hand side.. done!
					if (start < origStart) {
						throw new IllegalStateException("Start < origStart!");
					}
					return start;
				} else {
					// increase start past this block if applicable
					start = Math.max(block.start + block.length, start);
				}
				
			}
			return start;
		}
		
		public int findGapToLeft(int start, int length) {
			int closestLeftGap = Integer.MIN_VALUE;
			int prevBlockEnd = Integer.MIN_VALUE;
			Iterator it = blocks.iterator();
			while (it.hasNext()) {
				Block block = (Block) it.next();
				if ( (prevBlockEnd < block.start - length)
					 && (block.start - length <= start) ) {
					closestLeftGap = block.start - length;
				}
				if ( block.start > start ) {
					// we have reached a block to the right of start
					break;
				}
				prevBlockEnd = block.start + block.length;
			}
			
			// if we're still at one of the sentinel values, return the mouse location
			if (closestLeftGap == Integer.MIN_VALUE) {
				return start;
			} else {
				// otherwise, the answer is correct
				return closestLeftGap;
			}
		}
		
		public String toString() {
			return blocks.toString();
		}
		
		protected static class Block {
			public int start;
			public int length;
			public Block(int start, int length) {
				this.start = start;
				this.length = length;
			}
			public String toString() {
				return "("+start+","+length+")";
			}
		}
	}
}
