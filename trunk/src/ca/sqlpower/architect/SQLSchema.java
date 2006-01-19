package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A SQLSchema is a container for SQLTables.  If it is in the
 * containment hierarchy for a given RDBMS, it will be directly above
 * SQLTables.  Its parent could be either a SQLDatabase or a SQLCatalog.
 */
public class SQLSchema extends SQLObject {
	protected SQLObject parent;
	protected String schemaName;
	protected String nativeTerm;

	public SQLSchema() {
		this(null, null);
	}

	public SQLSchema(SQLObject parent, String name) {
		this.parent = parent;
		this.schemaName = name;
		this.children = new LinkedList();
		this.nativeTerm = "schema";
	}

	public SQLTable getTableByName(String tableName) throws ArchitectException {
		populate();
		
		Iterator childit = children.iterator();
		while (childit.hasNext()) {
			SQLTable child = (SQLTable) childit.next();
			if (child.getTableName().equalsIgnoreCase(tableName)) {
				return child;
			}
		}
		return null;
	}

	public String toString() {
		return getShortDisplayName();
	}

	public boolean isParentTypeDatabase() {
		return (parent instanceof SQLDatabase);
	}

	// ---------------------- SQLObject support ------------------------

	public SQLObject getParent() {
		return parent;
	}

	protected void setParent(SQLObject newParent) {
		parent = newParent;
	}

	public String getName() {
		return getSchemaName();
	}

	public String getShortDisplayName() {
		return schemaName;
	}
	
	public boolean allowsChildren() {
		return true;
	}

	public void populate() throws ArchitectException {
		if (populated) return;
		
		int oldSize = children.size();
		
		SQLObject databaseParent;
		SQLObject tmp = parent;
		if ( tmp instanceof SQLDatabase )	databaseParent = tmp;
		else {
			while ( true ) {
				databaseParent = tmp.getParent();
				if ( databaseParent == null ) {
					databaseParent = tmp;
					break;
				}
				if ( databaseParent instanceof SQLDatabase )	break;
				else	tmp = databaseParent;
			}
		}
	
		
		synchronized (databaseParent) {
			
			ResultSet rs = null;
					
			try {

				Connection con = ((SQLDatabase)databaseParent).getConnection();
				DatabaseMetaData dbmd = con.getMetaData();
				
				tmp = parent;
				if ( tmp instanceof SQLDatabase ) {
					rs = dbmd.getTables(null,
										schemaName,
										"%",
										new String[] {"TABLE", "VIEW"});
				}
				else if ( tmp instanceof SQLCatalog ) {
					rs = dbmd.getTables(tmp.getName(),
										schemaName,
										"%",
										new String[] {"TABLE", "VIEW"});
				}
				
				while ( rs!=null && rs.next()) {
					children.add(new SQLTable(this,
											  rs.getString(3),
											  rs.getString(5),
											  rs.getString(4) ));
				}
			} catch (SQLException e) {
				throw new ArchitectException("schema.populate.fail", e);
			} finally {
				populated = true;
				int newSize = children.size();
				if (newSize > oldSize) {
					int[] changedIndices = new int[newSize - oldSize];
					for (int i = 0, n = newSize - oldSize; i < n; i++) {
						changedIndices[i] = oldSize + i;
					}
					fireDbChildrenInserted(changedIndices, children.subList(oldSize, newSize));
				}
				try {
					if ( rs != null )	rs.close();
				} catch (SQLException e2) {
					throw new ArchitectException("schema.rs.close.fail", e2);
				}
			}
		}
	}


	// ----------------- accessors and mutators -------------------

	/**
	 * Gets the value of schemaName
	 *
	 * @return the value of schemaName
	 */
	public String getSchemaName()  {
		return this.schemaName;
	}

	/**
	 * Sets the value of schemaName
	 *
	 * @param argSchemaName Value to assign to this.schemaName
	 */
	public void setSchemaName(String argSchemaName) {
		this.schemaName = argSchemaName;
	}

	
	/**
	 * Gets the value of nativeTerm
	 *
	 * @return the value of nativeTerm
	 */
	public String getNativeTerm()  {
		return this.nativeTerm;
	}

	/**
	 * Sets the value of nativeTerm to a lowercase version of argNativeTerm.
	 *
	 * @param argNativeTerm Value to assign to this.nativeTerm
	 */
	public void setNativeTerm(String argNativeTerm) {
		if (argNativeTerm != null) argNativeTerm = argNativeTerm.toLowerCase();
		this.nativeTerm = argNativeTerm;
	}

}
