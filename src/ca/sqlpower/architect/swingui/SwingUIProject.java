package ca.sqlpower.architect.swingui;

import java.util.*;
import ca.sqlpower.architect.*;
import java.io.*;

public class SwingUIProject {

	protected String name;
	protected List sourceDatabases;
	protected SQLDatabase targetDatabase;
	protected File file;

	public SwingUIProject(String name) {
		this.name = name;
		this.sourceDatabases = new ArrayList();
		this.targetDatabase = new SQLDatabase();
	}

	// ------------- READING THE PROJECT FILE ---------------

	// ------------- WRITING THE PROJECT FILE ---------------
	

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
	public List getSourceDatabases()  {
		return this.sourceDatabases;
	}

	/**
	 * Sets the value of sourceDatabases
	 *
	 * @param argSourceDatabases Value to assign to this.sourceDatabases
	 */
	public void setSourceDatabases(List argSourceDatabases) {
		this.sourceDatabases = argSourceDatabases;
	}

	/**
	 * Gets the value of targetDatabase
	 *
	 * @return the value of targetDatabase
	 */
	public SQLDatabase getTargetDatabase()  {
		return this.targetDatabase;
	}

	/**
	 * Sets the value of targetDatabase
	 *
	 * @param argTargetDatabase Value to assign to this.targetDatabase
	 */
	public void setTargetDatabase(SQLDatabase argTargetDatabase) {
		this.targetDatabase = argTargetDatabase;
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

}
