package ca.sqlpower.architect.swingui;

import java.util.*;
import ca.sqlpower.architect.*;
import java.io.*;
import java.beans.*;
import java.awt.Point;
import javax.swing.ProgressMonitor;
import org.apache.log4j.Logger;

public class SwingUIProject {
	private static final Logger logger = Logger.getLogger(SwingUIProject.class);

	// these are persistent properties
	protected String name;
	protected DBTree sourceDatabases;
	protected PlayPen playPen;
	protected File file;

	// these are only useful during a save or load
	protected PrintWriter out;
	protected int indent = 0;
	protected Map objectIdMap;
	protected ProgressMonitor pm;
	protected int progress = 0;

	public SwingUIProject(String name) throws ArchitectException {
		this.name = name;
		this.playPen = new PlayPen(new SQLDatabase());
		List initialDBList = new ArrayList();
		initialDBList.add(playPen.getDatabase());
		this.sourceDatabases = new DBTree(initialDBList);
	}

	// ------------- READING THE PROJECT FILE ---------------
	public void load() throws IOException, ArchitectException {
	}

	// ------------- WRITING THE PROJECT FILE ---------------
	public void save(ProgressMonitor pm) throws IOException, ArchitectException {
		out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		objectIdMap = new HashMap();
		indent = 0;
		this.pm = pm;
		pm.setMinimum(0);
		pm.setMaximum(countSourceTables(0, (SQLObject) sourceDatabases.getModel().getRoot())
					  + playPen.getComponentCount() * 2);
		progress = 0;
		pm.setProgress(progress);
		pm.setMillisToDecideToPopup(500);
		try {
			println("<?xml version=\"1.0\"?>");
			println("<architect-project version=\"0.1\">");
			indent++;
			println("<project-name>"+name+"</project-name>");
			saveSourceDatabases();
			saveTargetDatabase();
			saveLayout();
			indent--;
			println("</architect-project>");
		} finally {
			out.close();
			out = null;
			pm.close();
			pm = null;
		}
	}

	protected int countSourceTables(int count, SQLObject o) throws ArchitectException {
		if (o instanceof SQLTable) {
			return count + 1;
		} else {
			Iterator it = o.getChildren().iterator();
			while (it.hasNext()) {
				count += countSourceTables(count, (SQLObject) it.next());
			}
		}
		return count;
	}

	protected void saveSourceDatabases() throws IOException, ArchitectException {
		println("<source-databases>");
		indent++;
		SQLObject dbTreeRoot = (SQLObject) sourceDatabases.getModel().getRoot();
		Iterator it = dbTreeRoot.getChildren().iterator();
		while (it.hasNext()) {
			SQLObject o = (SQLObject) it.next();
			if (o != playPen.getDatabase()) {
				saveSQLObject(o);
			}
		}
		indent--;
		println("</source-databases>");
	}

	protected void saveTargetDatabase() throws IOException, ArchitectException {
		println("<target-database>");
		indent++;
		SQLDatabase db = (SQLDatabase) playPen.getDatabase();
		Iterator it = db.getChildren().iterator();
		while (it.hasNext()) {
			saveSQLObject((SQLObject) it.next());
		}
		indent--;
		println("</target-database>");
	}
	
	protected void saveLayout() throws IOException, ArchitectException {
		println("<layout-coordinates>");
		indent++;
		int n = playPen.getComponentCount();
		for (int i = 0; i < n; i++) {
			TablePane tp = (TablePane) playPen.getComponent(i);
			Point p = tp.getLocation();
			println("<position tableid=\""+objectIdMap.get(tp.getModel())+"\""
					+" x=\""+p.x+"\" y=\""+p.y+"\" />");
			pm.setProgress(++progress);
			pm.setNote(tp.getModel().getShortDisplayName());
		}
		indent--;
		println("</layout-coordinates>");
	}

	protected void saveSQLObject(SQLObject o) throws IOException, ArchitectException {
		String id = (String) objectIdMap.get(o);
		if (id != null) {
			println("<reference refid=\""+id+"\" />");
			return;
		}
		String type;
		
		Map propNames = new TreeMap();

		if (o instanceof SQLDatabase) {
			id = "DB"+objectIdMap.size();
			type = "database";
		} else if (o instanceof SQLCatalog) {
			id = "CAT"+objectIdMap.size();
			type = "catalog";
			propNames.put("catalogName", ((SQLCatalog) o).getCatalogName());
		} else if (o instanceof SQLSchema) {
			id = "SCH"+objectIdMap.size();
			type = "schema";
			propNames.put("schemaName", ((SQLSchema) o).getSchemaName());
		} else if (o instanceof SQLTable) {
			id = "TAB"+objectIdMap.size();
			type = "table";
			propNames.put("tableName", ((SQLTable) o).getTableName());
			propNames.put("remarks", ((SQLTable) o).getRemarks());
			propNames.put("objectType", ((SQLTable) o).getObjectType());
			propNames.put("primaryKeyName", ((SQLTable) o).getPrimaryKeyName());
			pm.setProgress(++progress);
			pm.setNote(o.getShortDisplayName());
		} else if (o instanceof SQLTable.Folder) {
			id = "FOL"+objectIdMap.size();
			type = "folder";
			propNames.put("name", ((SQLTable.Folder) o).getName());
		} else if (o instanceof SQLColumn) {
			id = "COL"+objectIdMap.size();
			type = "column";
			SQLColumn sourceCol = ((SQLColumn) o).getSourceColumn();
			if (sourceCol != null) {
				logger.debug("column "+o+" source is "+sourceCol+" (hash "+sourceCol.hashCode()
							 +"; id "+objectIdMap.get(sourceCol)
							 +", parent "+sourceCol.getParent()+" hash "+sourceCol.getParent().hashCode()
							 +", parent "+sourceCol.getParent().getParent()+" hash "+sourceCol.getParent().getParent().hashCode()
							 +", parent "+sourceCol.getParent().getParent().getParent()+" hash "+sourceCol.getParent().getParent().getParent().hashCode()
							 +")");
				propNames.put("sourceColumn", objectIdMap.get(sourceCol));
			}
			propNames.put("columnName", ((SQLColumn) o).getColumnName());
			propNames.put("type", new Integer(((SQLColumn) o).getType()));
			propNames.put("sourceDBTypeName", ((SQLColumn) o).getSourceDBTypeName());
			propNames.put("scale", new Integer(((SQLColumn) o).getScale()));
			propNames.put("precision", new Integer(((SQLColumn) o).getPrecision()));
			propNames.put("nullable", new Integer(((SQLColumn) o).getNullable()));
			propNames.put("remarks", ((SQLColumn) o).getRemarks());
			propNames.put("defaultValue", ((SQLColumn) o).getDefaultValue());
			propNames.put("primaryKeySeq", ((SQLColumn) o).getPrimaryKeySeq());
			propNames.put("autoIncrement", new Boolean(((SQLColumn) o).isAutoIncrement()));
		} else if (o instanceof SQLRelationship) {
			id = "REL"+objectIdMap.size();
			type = "relationship";
			propNames.put("pkTable", objectIdMap.get(((SQLRelationship) o).getPkTable()));
			propNames.put("fkTable", objectIdMap.get(((SQLRelationship) o).getFkTable()));
			propNames.put("updateRule", new Integer(((SQLRelationship) o).getUpdateRule()));
			propNames.put("deleteRule", new Integer(((SQLRelationship) o).getDeleteRule()));
			propNames.put("deferrability", new Integer(((SQLRelationship) o).getDeferrability()));
			propNames.put("fkName", ((SQLRelationship) o).getFkName());
			propNames.put("pkName", ((SQLRelationship) o).getPkName());
		} else if (o instanceof SQLRelationship.ColumnMapping) {
			id = "CMP"+objectIdMap.size();
			type = "column-mapping";
			propNames.put("pkTable", objectIdMap.get(((SQLRelationship.ColumnMapping) o).getPkColumn()));
			propNames.put("fkTable", objectIdMap.get(((SQLRelationship.ColumnMapping) o).getFkColumn()));
		} else {
			throw new UnsupportedOperationException("Woops, the SQLObject type "
													+o.getClass().getName()+" is not supported!");
		}

		objectIdMap.put(o, id);

		print("<"+type+" hashCode=\""+o.hashCode()+"\" id=\""+id+"\" ");
		Iterator props = propNames.keySet().iterator();
		while (props.hasNext()) {
			Object key = props.next();
			niprint(key+"=\""+propNames.get(key)+"\" ");
		}
		if (o.allowsChildren()) {
			niprintln(">");
			Iterator children = o.getChildren().iterator();
			indent++;
			while (children.hasNext()) {
				saveSQLObject((SQLObject) children.next());
			}
			indent--;
			println("</"+type+">");
		} else {
			niprintln("/>");
		}
	}

	// ----------------------- utility methods ------------------------

	protected void restoreTPPositions(PlayPen pp, Point[] points) {
		int n = pp.getComponentCount();
		for (int i = 0; i < n; i++) {
			pp.getComponent(i).setLocation(points[i]);
		}
	}

	// ------------------- accessors and mutators ---------------------
	
	/**
	 * Gets the value of name
	 *
	 * @return the value of name
	 */
	public String getName()  {
		return this.name;
	}

	/**
	 * Sets the value of name
	 *
	 * @param argName Value to assign to this.name
	 */
	public void setName(String argName) {
		this.name = argName;
	}

	/**
	 * Gets the value of sourceDatabases
	 *
	 * @return the value of sourceDatabases
	 */
	public DBTree getSourceDatabases()  {
		return this.sourceDatabases;
	}

	/**
	 * Sets the value of sourceDatabases
	 *
	 * @param argSourceDatabases Value to assign to this.sourceDatabases
	 */
	public void setSourceDatabases(DBTree argSourceDatabases) {
		this.sourceDatabases = argSourceDatabases;
	}

	/**
	 * Gets the value of targetDatabase
	 *
	 * @return the value of targetDatabase
	 */
	public SQLDatabase getTargetDatabase()  {
		return playPen.getDatabase();
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
	 * Gets the value of playPen
	 *
	 * @return the value of playPen
	 */
	public PlayPen getPlayPen()  {
		return this.playPen;
	}

	/**
	 * Sets the value of playPen
	 *
	 * @param argPlayPen Value to assign to this.playPen
	 */
	public void setPlayPen(PlayPen argPlayPen) {
		this.playPen = argPlayPen;
	}

	// ------------------- utility methods -------------------
	/**
	 * Prints to the output writer {@link #out} indentation spaces
	 * (according to {@link #indent}) followed by the given text.
	 */
	protected void print(String text) {
		for (int i = 0; i < indent; i++) {
			out.print(" ");
		}
		out.print(text);
	}

	/** 
	 * Prints <code>text</code> to the output writer {@link #out} (no
	 * indentation).
	 */
	protected void niprint(String text) {
		out.print(text);
	}

	/** 
	 * Prints <code>text</code> followed by newline to the output
	 * writer {@link #out} (no indentation).
	 */
	protected void niprintln(String text) {
		out.println(text);
	}

	/**
	 * Prints to the output writer {@link #out} indentation spaces
	 * (according to {@link #indent}) followed by the given text
	 * followed by a newline.
	 */
	protected void println(String text) {
		for (int i = 0; i < indent; i++) {
			out.print(" ");
		}
		out.println(text);
	}
}
