package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;

import ca.sqlpower.architect.*;

public class PlayPen extends JPanel implements java.io.Serializable {

	/**
	 * Links this PlayPen with an instance of PlayPenDropListener so
	 * users can drop stuff on the playpen.
	 */
	protected DropTarget dt;
	
	public PlayPen() {
		super();
		setLayout(new PlayPenLayout(this));
		setName("Play Pen");
		setMinimumSize(new Dimension(200,200));
		setOpaque(true);
		dt = new DropTarget(this, new PlayPenDropListener());
	}
	
	public static class PlayPenLayout implements LayoutManager2 {

		/**
		 * This is the PlayPen that we are managing the layout for.
		 */
		PlayPen parent;

		public PlayPenLayout(PlayPen parent) {
			this.parent = parent;
		}

		/**
		 * Does nothing.  Use the Object-style constraints, not String.
		 */
		public void addLayoutComponent(String name, Component comp) {
			System.out.println("PlayPenLayout.addLayoutComponent(String,Component)");
		}

		/**
		 * Positions the new component near the given point.
		 *
		 * @param comp the component which has been added
		 * @param position A java.awt.Point, near which the object
		 * should be positioned.  It will not overlap existing
		 * components in this play pen.
		 */
		public void addLayoutComponent(Component comp,
									   Object position) {
			System.out.println("PlayPenLayout.addLayoutComponent(Component,Object)");
			Point pos = (Point) position;
			comp.setSize(comp.getPreferredSize());

			RangeList rl = new RangeList();
			Rectangle cbounds = null;
			for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
				Component c = parent.getComponent(i);
				if (c.isVisible() && c != comp) {
					cbounds = c.getBounds(cbounds);
					rl.blockOut(cbounds.x, cbounds.width);
				}
			}
			
			pos.x = Math.max(pos.x, rl.findGapToRight(pos.x, comp.getWidth()));
			comp.setLocation(pos);
		}

		/**
		 * Does nothing.
		 */
		public void removeLayoutComponent(Component comp) {
			System.out.println("PlayPenLayout.removeLayoutComponent");
		}

		/**
		 * Calculates the smallest rectangle that will completely
		 * enclose the visible components inside parent.
		 */
		public Dimension preferredLayoutSize(Container parent) {
			System.out.println("PlayPenLayout.preferredLayoutSize");
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
			System.out.println("PlayPenLayout.minimumLayoutSize");
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
			System.out.println("PlayPenLayout.layoutContainer");
		}

		protected static class RangeList {

			List blocks;

			public RangeList() {
				blocks = new LinkedList();

				// we need sentinel values because blockOut() requires non-empty list
				blocks.add(new Block(Integer.MAX_VALUE, 0));
			}

			public void blockOut(int start, int length) {
				Block block = new Block(start, length);
				System.out.println("blockOut "+block+": before "+blocks);
				ListIterator it = blocks.listIterator();
				while (it.hasNext()) {
					Block nextBlock = (Block) it.next();
					if (nextBlock.start > start) {
						it.previous();
						it.add(block);
						break;
					}
				}
				System.out.println("blockOut "+block+": after  "+blocks);
			}

			public int findGapToRight(int start, int length) {
				Iterator it = blocks.iterator();
				while (it.hasNext()) {
					Block block = (Block) it.next();

					if ( (start + length) < block.start ) {
						// current gap fits at right-hand side.. done!
						return start;
					} else {
						start = block.start + block.length;
					}

				}
				return start;
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
	
	/**
	 * Tracks incoming objects and adds successfully dropped objects
	 * at the current mouse position.
	 */
	public static class PlayPenDropListener implements DropTargetListener {

		/**
		 * Called while a drag operation is ongoing, when the mouse
		 * pointer enters the operable part of the drop site for the
		 * DropTarget registered with this listener.
		 */
		public void dragEnter(DropTargetDragEvent dtde) {
			dragOver(dtde);
		}
		
		/**
		 * Called while a drag operation is ongoing, when the mouse
		 * pointer has exited the operable part of the drop site for the
		 * DropTarget registered with this listener.
		 */
		public void dragExit(DropTargetEvent dte) {
		}
		
		/**
		 * Called when a drag operation is ongoing, while the mouse
		 * pointer is still over the operable part of the drop site for
		 * the DropTarget registered with this listener.
		 */
		public void dragOver(DropTargetDragEvent dtde) {
			System.out.println("PlayPenDropTarget.dragOver()");
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		}
		
		/**
		 * Called when the drag operation has terminated with a drop on
		 * the operable part of the drop site for the DropTarget
		 * registered with this listener.
		 */
		public void drop(DropTargetDropEvent dtde) {
			Transferable t = dtde.getTransferable();
			JComponent c = (JComponent) dtde.getDropTargetContext().getComponent();
			DataFlavor importFlavor = bestImportFlavor(c, t.getTransferDataFlavors());
			if (importFlavor == null) {
				dtde.rejectDrop();
			} else {
				try {
					Object someData = t.getTransferData(importFlavor);
					System.out.println("MyJTreeTransferHandler.importData: got object of type "+someData.getClass().getName());
					if (someData instanceof SQLTable) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						SQLTable table = (SQLTable) someData;
						TablePane tp = new TablePane(table);
						c.add(tp, dtde.getLocation());

						tp.revalidate();
						dtde.dropComplete(true);
						return;
					} else if (someData instanceof SQLColumn) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						SQLColumn column = (SQLColumn) someData;
						JLabel colName = new JLabel(column.getColumnName());
						colName.setSize(colName.getPreferredSize());
						c.add(colName, dtde.getLocation());
						System.out.println("Added "+column.getColumnName()+" to playpen (temporary, only for testing)");
						colName.revalidate();
						dtde.dropComplete(true);
						return;
					} else if (someData instanceof SQLObject[]) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						SQLObject[] objects = (SQLObject[]) someData;
						for (int i = 0; i < objects.length; i++) {
							if (objects[i] instanceof SQLTable) {
								TablePane tp = new TablePane((SQLTable) objects[i]);
								c.add(tp, dtde.getLocation());
								tp.revalidate();
							}
						}
						dtde.dropComplete(true);
						return;
					} else {
						dtde.rejectDrop();
					}
				} catch (UnsupportedFlavorException ufe) {
					ufe.printStackTrace();
					dtde.rejectDrop();
				} catch (IOException ioe) {
					ioe.printStackTrace();
					dtde.rejectDrop();
				} catch (InvalidDnDOperationException ex) {
					ex.printStackTrace();
					dtde.rejectDrop();
				}
			}
		}
		
		/**
		 * Called if the user has modified the current drop gesture.
		 */
		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

		/**
		 * Chooses the best import flavour from the flavors array for
		 * importing into c.  The current implementation actually just
		 * chooses the first acceptable flavour.
		 *
		 * @return The first acceptable DataFlavor in the flavors
		 * list, or null if no acceptable flavours are present.
		 */
		public DataFlavor bestImportFlavor(JComponent c, DataFlavor[] flavors) {
			System.out.println("PlayPenTransferHandler: can I import "+Arrays.asList(flavors));
 			for (int i = 0; i < flavors.length; i++) {
				String cls = flavors[i].getDefaultRepresentationClassAsString();
				System.out.println("representation class = "+cls);
				System.out.println("mime type = "+flavors[i].getMimeType());
				System.out.println("type = "+flavors[i].getPrimaryType());
				System.out.println("subtype = "+flavors[i].getSubType());
				System.out.println("class = "+flavors[i].getParameter("class"));
				System.out.println("isSerializedObject = "+flavors[i].isFlavorSerializedObjectType());
				System.out.println("isInputStream = "+flavors[i].isRepresentationClassInputStream());
				System.out.println("isRemoteObject = "+flavors[i].isFlavorRemoteObjectType());
				System.out.println("isLocalObject = "+flavors[i].getMimeType().equals(DataFlavor.javaJVMLocalObjectMimeType));


 				if (flavors[i].equals(SQLObjectTransferable.flavor)
					|| flavors[i].equals(SQLObjectListTransferable.flavor)) {
					System.out.println("YES");
 					return flavors[i];
				}
 			}
			System.out.println("NO!");
 			return null;
		}

		/**
		 * This is set up this way because this DropTargetListener was
		 * derived from a TransferHandler.  It works, so no sense in
		 * changing it.
		 */
		public boolean canImport(JComponent c, DataFlavor[] flavors) {
			return bestImportFlavor(c, flavors) != null;
		} 
	}
}
