/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.swingui.olap;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.PlayPenCoordinate;

public class DnDOLAPTransferable implements Transferable, java.io.Serializable {
    
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DnDOLAPTransferable.class);

    public static final DataFlavor PP_COORDINATE_FLAVOR = new DataFlavor
		(PlayPenCoordinate.class, "List of selected PlayPen coordinates");

	private final List<List<Integer>> coords;

    private final String userVisibleName;
	
    /**
     * Creates a transferable for the given selection.
     * <p>
     * This has the same disadvantages described in {@link DnDTreePathTransferable}.
     *
     * @param node The SQLObject you want the path to
     */
    public DnDOLAPTransferable(PlayPen pp, List<PlayPenCoordinate<? extends OLAPObject,? extends OLAPObject>> selectedCoordinates) {
        PlayPenContentPane contentPane = pp.getContentPane();
        StringBuilder name = new StringBuilder();
        coords = new ArrayList<List<Integer>>();
        for (int i = 0; i < contentPane.getChildren().size(); i++) {
            PlayPenComponent ppc = contentPane.getChildren().get(i);
            // XXX n*m is bad -- use a Map<Pane,PlayPenCoordinate>
            for (PlayPenCoordinate<?, ?> ppco : selectedCoordinates) {
                OLAPPane<?, ?> pane = ppco.getPane();
                PaneSection<?> section = ppco.getSection();
                if (pane == ppc) {
                    List<Integer> c = new ArrayList<Integer>();
                    c.add(i);
                    c.add(pane.getSections().indexOf(section));
                    c.add(ppco.getIndex());
                    
                    coords.add(c);
                    
                    if (name.length() != 0) {
                        name.append("\n");
                    }
                    if (ppco.getItem() == null) {
                        name.append(section.getTitle());
                    } else {
                        name.append(OLAPUtil.nameFor(ppco.getItem()));
                    }
                }
            }
        }
        userVisibleName = name.toString();
    }
	
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { PP_COORDINATE_FLAVOR };
	}
	
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(PP_COORDINATE_FLAVOR);
	}
	
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor != PP_COORDINATE_FLAVOR) {
            throw new IllegalArgumentException("Unsupported flavor " + flavor);
        }
        return coords;
    }
	
	/**
	 * Gets back the objects given to the transferable constructor, subject to a
	 * few limitations:
	 * <ul>
	 *  <marquee scrolldelay="10" scrollamount="1" >
	 *  <li>pp must be the same playpen passed in to the constructor
	 *  </marquee>
	 *  <marquee  scrolldelay="10" scrollamount="2">
	 *  <li>pp's content pane must not have been added to or removed
	 *      from since the constructor was invoked
	 *  </marquee>
     *  <marquee  scrolldelay="10" scrollamount="2" direction="right">
	 *  <li>none of the panes represented in the selection must have
	 *      had any items or sections added or removed.
     *  </marquee>
	 * </ul>
	 */
	public static List<PlayPenCoordinate<? extends OLAPObject,? extends OLAPObject>> resolve(PlayPen pp, List<List<Integer>> coords) {
	    List<PlayPenCoordinate<? extends OLAPObject,? extends OLAPObject>> items =
	        new ArrayList<PlayPenCoordinate<? extends OLAPObject,? extends OLAPObject>>();
	    for (List<Integer> coord : coords) {
	        int paneIndex = coord.get(0);
	        int sectIndex = coord.get(1);
	        int itemIndex = coord.get(2);
	        OLAPPane<OLAPObject,OLAPObject> ppc = (OLAPPane<OLAPObject,OLAPObject>)
	                pp.getContentPane().getChildren().get(paneIndex);
	        PaneSection<? extends OLAPObject> s = ppc.getSections().get(sectIndex);
	        OLAPObject item;
	        if (itemIndex >= 0) {
	            item = s.getItems().get(itemIndex);
	        } else {
	            item = null;
	        }
	        PlayPenCoordinate<OLAPObject,OLAPObject> ppco =
	            new PlayPenCoordinate<OLAPObject,OLAPObject>(ppc, s, itemIndex, item);
	        items.add(ppco);
	    }
	    return items;
	}

	@Override
	public String toString() {
	    return userVisibleName;
	}
}

