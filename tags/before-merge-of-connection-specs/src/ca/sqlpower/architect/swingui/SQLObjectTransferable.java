package ca.sqlpower.architect.swingui;

import java.awt.datatransfer.*;
import ca.sqlpower.architect.SQLObject;
import java.io.IOException;
import org.apache.log4j.Logger;

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

