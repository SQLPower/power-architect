package ca.sqlpower.architect.swingui;

import java.awt.datatransfer.*;
import ca.sqlpower.architect.SQLObject;
import java.io.IOException;

public class SQLObjectListTransferable implements Transferable, java.io.Serializable {
	public static final DataFlavor flavor = new DataFlavor
		(SQLObject[].class, "List of database objects");
	
	protected SQLObject[] data;
	
	public SQLObjectListTransferable(SQLObject[] data) {
		this.data = data;
	}
	
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { flavor };
	}
	
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor.equals(this.flavor));
	}
	
	public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException, IOException {
		if (flavor != this.flavor) {
			throw new IllegalArgumentException("Unsupported flavor "+flavor);
		}
		return data;
	}
}

