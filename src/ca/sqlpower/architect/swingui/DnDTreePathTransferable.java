package ca.sqlpower.architect.swingui;

import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.ArrayList;

public class DnDTreePathTransferable implements Transferable, java.io.Serializable {
	public static final DataFlavor flavor = new DataFlavor
		(ArrayList.class, "List of selected tree paths");
	
	protected ArrayList data;
	
	public DnDTreePathTransferable(ArrayList data) {
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

