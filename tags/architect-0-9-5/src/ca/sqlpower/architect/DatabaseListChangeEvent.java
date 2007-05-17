package ca.sqlpower.architect;

import java.util.EventObject;

public class DatabaseListChangeEvent extends EventObject implements java.io.Serializable {
	
	int listIndex;
	ArchitectDataSource dbcs;
	
	/**
	 * @param dbcs
	 * @param index
	 */
	public DatabaseListChangeEvent(Object source, int index, ArchitectDataSource dbcs) {
		super(source);
		this.dbcs = dbcs;
		listIndex = index;
	}

	public int getListIndex() {
		return listIndex;
	}

}
