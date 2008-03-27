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
package ca.sqlpower.architect.swingui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import ca.sqlpower.architect.SQLObject;

public class SQLObjectListTransferable implements Transferable, java.io.Serializable {
	public static final DataFlavor SQLOBJECT_ARRAY_FLAVOR = new DataFlavor
		(SQLObject[].class, "List of database objects");
	
	protected SQLObject[] data;
	
	public SQLObjectListTransferable(SQLObject[] data) {
		this.data = data;
	}
	
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { SQLOBJECT_ARRAY_FLAVOR };
	}
	
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor.equals(SQLOBJECT_ARRAY_FLAVOR));
	}
	
	public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException, IOException {
		if (flavor != SQLOBJECT_ARRAY_FLAVOR) {
			throw new IllegalArgumentException("Unsupported flavor "+flavor);
		}
		return data;
	}
}

