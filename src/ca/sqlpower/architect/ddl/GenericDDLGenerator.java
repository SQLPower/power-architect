package ca.sqlpower.architect.ddl;

import ca.sqlpower.architect.*;
import java.sql.*;
import java.io.*;
import java.util.*;

public class GenericDDLGenerator {

	public static final String GENERATOR_VERSION = "$Revision$";

	/**
	 * This property says whether or not the user will allow us to
	 * connect to the target system in order to determine database
	 * meta-data.  This generic base class will fail if
	 * allowConnection == false.
	 */
	protected boolean allowConnection;

	protected File file;
	protected PrintWriter out;

	/**
	 * A mapping from JDBC type code (Integer values) to
	 * GenericTypeDescriptor objects which describe that data type.
	 */
	protected Map typeMap;

	/**
	 * This connection will be live and non-null (set up by writeDDL)
	 * if allowConnection is true.
	 */
	protected Connection con;

	public GenericDDLGenerator() {
		allowConnection = true;
	}

	public void writeDDL(SQLDatabase source) throws SQLException, IOException, ArchitectException {
		if (allowConnection) {
			con = source.getConnection();
		} else {
			con = null;
		}

		createTypeMap();

		out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		try {
			writeHeader();
			writeCreateDB(source);
			Iterator it = source.getChildren().iterator();
			while (it.hasNext()) {
				SQLTable t = (SQLTable) it.next();
				writeTable(t);
				writePrimaryKey(t);
			}
			it = source.getChildren().iterator();
			while (it.hasNext()) {
				SQLTable t = (SQLTable) it.next();
				writeExportedRelationships(t);
			}
		} finally {
			if (out != null) {
				out.close();
				out = null;
			}
		}
	}

	public void writeHeader() {
		out.println("-- Created by SQLPower Generic DDL Generator "+GENERATOR_VERSION+" --");
	}

	public void writeCreateDB(SQLDatabase db) {
		out.println("-- Would Create Database "+db.getName()+" here. --");
	}

	public void writeTable(SQLTable t) throws SQLException, IOException, ArchitectException {
		out.println("\nCREATE TABLE "+t.getName()+" (");
		boolean firstCol = true;
		Iterator it = t.getColumns().iterator();
		while (it.hasNext()) {
			SQLColumn c = (SQLColumn) it.next();
			GenericTypeDescriptor td = (GenericTypeDescriptor) typeMap.get(new Integer(c.getType()));
			if (td == null) {
				throw new UnsupportedOperationException
					("No type for "+c.getType()+" is available in the target database platform."
					 +"\nPlease choose a different type.");
			}
			if (!firstCol) out.println(",");
			out.print("                ");
			out.print(c.getName());
			out.print(" ");
			out.print(td.getName());
			if (td.getHasScale()) {
				out.print("("+c.getScale());
				if (td.getHasPrecision()) {
					out.print(","+c.getPrecision());
				}
				out.print(")");
			}

			if (c.isNullable()) {
				if (! td.isNullable()) {
					throw new UnsupportedOperationException
						("The data type "+td.getName()+" is not nullable on the target database platform.");
				}
				out.print(" NULL");
			} else {
				out.print(" NOT NULL");
			}

			// XXX: default values?

			firstCol = false;
		}
		out.println(")");
	}
	
	protected void writePrimaryKey(SQLTable t) throws ArchitectException {
		boolean firstCol = true;
		Iterator it = t.getColumns().iterator();
		while (it.hasNext()) {
			SQLColumn col = (SQLColumn) it.next();
			if (col.getPrimaryKeySeq() == null) break;
			if (firstCol) {
				out.println("ALTER TABLE "+t.getName()
							+" ADD CONSTRAINT "+t.getPrimaryKeyName()
							+" PRIMARY KEY (");
				firstCol = false;
			} else {
				out.println(",");
			}
			out.print("               ");
			out.print(col.getName());
		}
		out.println(")");
	}

	protected void writeExportedRelationships(SQLTable t) throws ArchitectException {
		Iterator it = t.getExportedKeys().iterator();
		while (it.hasNext()) {
			SQLRelationship rel = (SQLRelationship) it.next();
			out.println("ALTER TABLE "+rel.getFkTable().getName()
						+" ADD CONSTRAINT "+rel.getName()
						+" FORIEGN KEY (");
			StringBuffer pkCols = new StringBuffer();
			StringBuffer fkCols = new StringBuffer();
			boolean firstCol = true;
			Iterator mappings = rel.getChildren().iterator();
			while (mappings.hasNext()) {
				SQLRelationship.ColumnMapping cmap = (SQLRelationship.ColumnMapping) mappings.next();
				if (!firstCol) {
					pkCols.append(", ");
					fkCols.append(", ");
				}
				pkCols.append(cmap.getPkColumn().getName());
				fkCols.append(cmap.getFkColumn().getName());
				firstCol = false;
			}
			out.print(fkCols.toString());
			out.println(")");
			out.println("REFERENCES "+rel.getPkTable().getName()+" (");
			out.print(pkCols.toString());
			out.println(")");
		}
	}

	/**
	 * Creates and populates <code>typeMap</code> using
	 * DatabaseMetaData.  Subclasses for specific DB platforms will be
	 * able to override this implementation with one that uses a
	 * static, pre-defined type map.
	 */
	protected void createTypeMap() throws SQLException {
		if (con == null || !allowConnection) {
			throw new UnsupportedOperationException("Can't create a type map without DatabaseMetaData");
		}
		typeMap = new HashMap();
		DatabaseMetaData dbmd = con.getMetaData();
		ResultSet rs = dbmd.getTypeInfo();
		while (rs.next()) {
			GenericTypeDescriptor td = new GenericTypeDescriptor(rs);
			typeMap.put(new Integer(td.getDataType()), td);
		}
	}

	// ---------------------- accessors and mutators ----------------------
	
	/**
	 * Gets the value of allowConnection
	 *
	 * @return the value of allowConnection
	 */
	public boolean getAllowConnection()  {
		return this.allowConnection;
	}

	/**
	 * Sets the value of allowConnection
	 *
	 * @param argAllowConnection Value to assign to this.allowConnection
	 */
	public void setAllowConnection(boolean argAllowConnection) {
		this.allowConnection = argAllowConnection;
	}

	/**
	 * Gets the value of file
	 *
	 * @return the value of file
	 */
	public File getFile()  {
		return this.file;
	}

	/**
	 * Sets the value of file
	 *
	 * @param argFile Value to assign to this.file
	 */
	public void setFile(File argFile) {
		this.file = argFile;
	}

	/**
	 * Gets the value of out
	 *
	 * @return the value of out
	 */
	public PrintWriter getOut()  {
		return this.out;
	}

	/**
	 * Sets the value of out
	 *
	 * @param argOut Value to assign to this.out
	 */
	public void setOut(PrintWriter argOut) {
		this.out = argOut;
	}

	/**
	 * Gets the value of typeMap
	 *
	 * @return the value of typeMap
	 */
	public Map getTypeMap()  {
		return this.typeMap;
	}

	/**
	 * Sets the value of typeMap
	 *
	 * @param argTypeMap Value to assign to this.typeMap
	 */
	public void setTypeMap(Map argTypeMap) {
		this.typeMap = argTypeMap;
	}

	/**
	 * Gets the value of con
	 *
	 * @return the value of con
	 */
	public Connection getCon()  {
		return this.con;
	}

	/**
	 * Sets the value of con
	 *
	 * @param argCon Value to assign to this.con
	 */
	public void setCon(Connection argCon) {
		this.con = argCon;
	}

}
