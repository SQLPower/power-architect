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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;

public class SQLObjectTransferable implements Transferable, java.io.Serializable {
	private static final Logger logger = Logger.getLogger(SQLObjectTransferable.class);

	public static final DataFlavor SQLOBJECT_FLAVOR = new DataFlavor
		(SQLObject.class, "Database objects");
	
	protected SQLObject data;
	
	public SQLObjectTransferable(SQLObject data) {
		this.data = data;
	}
	
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { SQLOBJECT_FLAVOR };
	}
	
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor.equals(SQLOBJECT_FLAVOR));
	}
	
	public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException, IOException {
		if (flavor != SQLOBJECT_FLAVOR) {
			throw new IllegalArgumentException("Unsupported flavor "+flavor);
		}
		logger.debug("getTransferData returns '"+data.getName()+"'"+data.getClass().getName()+"@"+data.hashCode());
		return data;
	}
}

