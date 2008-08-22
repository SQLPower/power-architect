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
import ca.sqlpower.architect.swingui.DnDTreePathTransferable;
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
        PlayPenContentPane ppcp = pp.getContentPane();
        StringBuilder name = new StringBuilder();
        coords = new ArrayList<List<Integer>>();
        for (int i = 0; i < ppcp.getComponentCount(); i++) {
            PlayPenComponent ppc = ppcp.getComponent(i);
            // XXX n*m is bad -- use a Map<Pane,PlayPenCoordinate>
            for (PlayPenCoordinate<?, ?> ppco : selectedCoordinates) {
                OLAPPane<?, ?> pane = ppco.getPane();
                PaneSection<?> section = ppco.getSection();
                if (pane == ppc) {
                    List<Integer> c = new ArrayList<Integer>();
                    c.add(i);
                    c.add(pane.getSections().indexOf(section));
                    c.add(ppco.getIndex());
                    
                    if (name.length() != 0) {
                        name.append("\n");
                    }
                    if (ppco.getItem() == null) {
                        name.append(section.getTitle());
                    } else {
                        name.append(ppco.getItem().getName());
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
	
	public List<PlayPenCoordinate<?, ?>> resolve(PlayPen pp) {
	    return null; // TODO
	}

	@Override
	public String toString() {
	    return userVisibleName;
	}
}

