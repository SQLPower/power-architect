package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.IOException;
import java.util.Arrays;

import ca.sqlpower.architect.*;

public class PlayPen extends JPanel {

	protected PlayPenDropTargetListener dropTargetListener = new PlayPenDropTargetListener();

	public PlayPen() {
		//super((LayoutManager) null);
		//super(new FlowLayout());
		super(new PlayPenLayout());
		setName("Play Pen");
		setTransferHandler(new PlayPenTransferHandler());
		setMinimumSize(new Dimension(200,200));
		setOpaque(true);
		setBackground(Color.green);
		//getUI().getDropTarget().addDropTargetListener(dropTargetListener); XXX: impossible
	}
	
	public static class PlayPenTransferHandler extends TransferHandler {

		protected Transferable createTransferable(JComponent c) {
			// TODO: outbound transfers
			return null;
		}
    
		public int getSourceActions(JComponent c) {
			// TODO: outbound transfers (COPY)
			return NONE;
		}

		public boolean importData(JComponent c, Transferable t) {
			DataFlavor importFlavor = bestImportFlavor(c, t.getTransferDataFlavors());
			if (importFlavor != null) {
				try {
					Object someData = t.getTransferData(importFlavor);
					System.out.println("MyJTreeTransferHandler.importData: got object of type "+someData.getClass().getName());
					if (someData instanceof SQLTable) {
						SQLTable table = (SQLTable) someData;
						TablePane tp = new TablePane(table);
						c.add(tp);
						tp.setLocation(((PlayPen) c).dropTargetListener.currentLocation);
						System.out.println("Added "+table+" to playpen at "+tp.getLocation());
						tp.revalidate();
						return true;
					} else if (someData instanceof SQLColumn) {
						SQLColumn column = (SQLColumn) someData;
						JLabel colName = new JLabel(column.getColumnName());
						c.add(colName);
						colName.setLocation(30,30);
						colName.setSize(colName.getPreferredSize());
						System.out.println("Added "+column.getColumnName()+" to playpen (temporary, only for testing)");
						colName.revalidate();
						return true;
					}
				} catch (UnsupportedFlavorException ufe) {
					ufe.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return false;
		}
        
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


 				if (flavors[i].equals(SQLObjectTransferable.flavor)) {
					System.out.println("YES");
 					return flavors[i];
				}
 			}
			System.out.println("NO!");
 			return null;
		}
		
		public boolean canImport(JComponent c, DataFlavor[] flavors) {
			return bestImportFlavor(c, flavors) != null;
		} 
	}

	public static class PlayPenLayout implements LayoutManager {

		/**
		 * Does nothing.
		 */
		public void addLayoutComponent(String name, Component comp) {
		}

		/**
		 * Does nothing.
		 */
		public void removeLayoutComponent(Component comp) {
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
			return new Dimension(maxx - minx, maxy - miny);
		}

		/**
		 * Identical to {@link #preferredLayoutSize(Container)}.
		 */
		public Dimension minimumLayoutSize(Container parent) {
			return preferredLayoutSize(parent);
		}

		/**
		 * Does nothing!  Components will stay put.
		 */
		public void layoutContainer(Container parent) {
			
		}
	}
	
	/**
	 * Does nothing except track the current insertion point during
	 * dragOver notifications.  Note that this class cannot co-operate
	 * with a TransferHandler.  We will need to refactor the transfer
	 * handler into this listener and do away with the TransferHandler
	 * API completely.
	 */
	public static class PlayPenDropTargetListener implements DropTargetListener {
		public Point currentLocation = new Point(0,0);

		/**
		 * Called while a drag operation is ongoing, when the mouse
		 * pointer enters the operable part of the drop site for the
		 * DropTarget registered with this listener.
		 */
		public void dragEnter(DropTargetDragEvent dtde) {
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
			currentLocation.setLocation(dtde.getLocation());
		}
		
		/**
		 * Called when the drag operation has terminated with a drop on
		 * the operable part of the drop site for the DropTarget
		 * registered with this listener.
		 */
		public void drop(DropTargetDropEvent dtde) {
		}
		
		/**
		 * Called if the user has modified the current drop gesture.
		 */
		public void dropActionChanged(DropTargetDragEvent dtde) {
		}
	}
}
