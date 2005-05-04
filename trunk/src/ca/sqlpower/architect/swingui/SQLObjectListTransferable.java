package ca.sqlpower.architect.swingui;

import java.awt.datatransfer.*;
import ca.sqlpower.architect.SQLObject;
import java.io.IOException;

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

