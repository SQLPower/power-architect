/* Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 2004-2006.
 * $Id$
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ca.sqlpower.architect.sqlrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.ArchitectException;

/** Class to run an SQL script, like psql(1), SQL*Plus, or similar programs.
 * Command line interface accepts options -c config [-f configFile] [scriptFile].
 * <p>Input language is: escape commands (begin with \ and MUST end with semi-colon), or
 * standard SQL statements which must also end with semi-colon);
 * <p>Escape sequences: 
 * <ul>
 * <li> \m (output-mode), takes character t for text,
 * h for html, s for sql, x for xml (not in this version)
 * (the SQL output is intended to be usable to re-insert the data into another identical table,
 * but this has not been extensively tested!).
 * <li> \o output-file, redirects output.
 * <li> \q quit the program
 * </ul>
 * <p>This class can also be used from within programs such as servlets, etc.;
 * see SQLRunnerGUI for an example of how to call.
 * <p>For example, this command and input:</pre>
 * SQLrunner -c testdb
 * \ms;
 * select * from person where person_key=4;
 * </pre>might produce this output:<pre>
 * Executing : <<select * from person where person_key=4>>
 *  insert into PERSON(PERSON_KEY,  FIRST_NAME, INITIAL, LAST_NAME, ... ) 
 * values (4, 'Ian', 'F', 'Darwin', ...);
 * </pre>
 * @author	Ian Darwin, http://www.darwinsys.com/
 */
public class SQLRunner {
	
	OutputMode outputMode = OutputMode.t;

	/** Database connection */
	private Connection conn;
	
	private DatabaseMetaData dbMeta;

	/** SQL Statement */
	private Statement statement;
	
	/** Where the output is going */
	private PrintWriter out;
	
	private ResultsDecorator currentDecorator;

	private ResultsDecorator textDecorator;

	private ResultsDecorator sqlDecorator;
	
	private ResultsDecorator htmlDecorator;
	
	private ResultsDecorator xmlDecorator;
	
	private boolean debug;

	/** DB2 is the only one I know of today that requires table names
	 * be given in upper case when getting table metadata
	 */
	private boolean upperCaseTableNames;
	
	private static Verbosity verbosity = Verbosity.QUIET;


	/** Construct a SQLRunner object */
	public SQLRunner(Connection c, String outputFileName, String outputModeName) throws IOException, SQLException {
		// set up the SQL input
		conn = c;

		dbMeta = conn.getMetaData();
		upperCaseTableNames = 
			dbMeta.getDatabaseProductName().indexOf("DB2") >= 0;
		String dbName = dbMeta.getDatabaseProductName();
		System.out.println("SQLRunner: Connected to " + dbName);
		statement = conn.createStatement();
		
		if (outputFileName == null) {
			out = new PrintWriter(System.out);
		} else {
			out = new PrintWriter(new FileWriter(outputFileName));
		}
		
		setOutputMode(outputModeName);
	}
	
	/** Set the output mode.
	 * @param outputMode Must be a value equal to one of the MODE_XXX values.
	 * @throws IllegalArgumentException if the mode is not valid.
	 */
	void setOutputMode(String outputModeName) {
		if (outputModeName == null || 
			outputModeName.length() == 0) { 
			System.err.println(
			"invalid mode: " + outputMode + "; must be t, h or s"); }
		
		outputMode = OutputMode.valueOf(outputModeName);
		setOutputMode(outputMode);
	}
	
	/** Assign the correct ResultsDecorator, creating them on the fly
	 * using lazy evaluation.
	 */
	void setOutputMode(OutputMode outputMode) {
		ResultsDecorator newDecorator = null;
		switch (outputMode) {
			case t:
				if (textDecorator == null) {
					textDecorator = new ResultsDecoratorText(out, verbosity);
				}
				newDecorator = textDecorator;
				break;
			case h:
				if (htmlDecorator == null) {
					htmlDecorator = new ResultsDecoratorHTML(out, verbosity);
				}
				newDecorator = htmlDecorator;
				break;
			case s:
				if (sqlDecorator == null) {
					sqlDecorator = new ResultsDecoratorSQL(out, verbosity);
				}
				newDecorator = sqlDecorator;
				break;
			case x:
				if (xmlDecorator == null) {
					xmlDecorator = new ResultsDecoratorXML(out, verbosity);
				}
				newDecorator = xmlDecorator;
				break;
			default:
				String values = OutputMode.values().toString();
				System.err.println("invalid mode: "
								+ outputMode + "; must be " + values);
		}
		if (currentDecorator != newDecorator) {
			currentDecorator = newDecorator;
			if (debug)
				System.out.println("Mode set to  " + outputMode);
		}
		currentDecorator.setWriter(out);
	}

	/**
	 * Process an escape, like "\ms;" for mode=sql.
	 * @throws ArchitectException 
	 */
	private void doEscape(String str) throws IOException, SQLException, ArchitectException  {
		String rest = null;
		if (str.length() > 2) {
			rest = str.substring(2);
		}
		
		if (str.startsWith("\\d")) {	// Display
			if (rest == null){
				throw new ArchitectException("\\d needs display arg");
			}
			display(rest);
		} else if (str.startsWith("\\m")) {	// MODE
			if (rest == null){
				throw new ArchitectException("\\m needs output mode arg");
			}
			setOutputMode(rest);
		} else if (str.startsWith("\\o")){
			if (rest == null){
				throw new ArchitectException("\\o needs output file arg");
			}
			setOutputFile(rest);
		} else {
			throw new ArchitectException("Unknown escape: " + str);
		}		
	}

	/**
	 * Display - generate output for \dt and similar escapes
	 * @param rest - what to display - the argument with the \d stripped off
	 * XXX: Move more formatting to ResultsDecorator: listTables(rs), listColumns(rs)
	 * @throws ArchitectException 
	 */
	private void display(String rest) throws IOException, SQLException, ArchitectException {
		if (rest.equals("t")) {
			// Display list of tables
			List<String> userTables = getUserTables(conn);
			for (String name : userTables) {
				textDecorator.println(name);
			}
		} else if (rest.startsWith("t")) {
			// Display one table. Some DatabaseMetaData implementations
			// don't do ignorecase so, for now, convert to UPPER CASE.
			String tableName = rest.substring(1).trim();
			if (upperCaseTableNames) tableName = tableName.toUpperCase();
			System.out.println("# Display table " + tableName);
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getColumns(null, null, tableName, "%");
			while (rs.next()) {
				textDecorator.println(rs.getString(4));
			}
		} else
			throw new ArchitectException("\\d"  + rest + " invalid");
	}
	
	/**
	 * Return the names of the user tables in the given Connection
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static List<String> getUserTables(Connection conn) throws SQLException {
		ArrayList<String> result = new ArrayList<String>();
		DatabaseMetaData md = conn.getMetaData();
		ResultSet rs = md.getTables(null, null, "%", null);
		while (rs.next()) {
			String catName = rs.getString(2);
			if (!catName.startsWith("SYS"))
				result.add(rs.getString(3));
		}
		return result;
	}

	/** Set the output to the given filename.
	 * @param fileName
	 */
	public void setOutputFile(String fileName) throws IOException {
		if (fileName == null) {
			/* Set the output file back to System.out */
			out = new PrintWriter(System.out, true);
		} else {
			File file = new File(fileName);
			setOutputFile(new PrintWriter(new FileWriter(file), true));
			System.out.println("Output set to " + file.getCanonicalPath());
		}
	}

	/** Set the output to the given Writer
	 * @param writer
	 */
	public void setOutputFile(PrintWriter writer) {
		out = writer;
	}

	/** Run one Statement, and format results as per Update or Query.
	 * Called from runScript or from user code.
	 * @throws ArchitectException 
	 */
	public void runStatement(final String rawString) throws IOException, SQLException, ArchitectException {
		
		final String inString = rawString.trim();
		
		if (verbosity != Verbosity.QUIET) {
			out.println("Executing : <<" + inString.trim() + ">>");		
			out.flush();
		}
		
		if (inString.startsWith("\\")) {
			doEscape(inString);
			return;
		}

		boolean hasResultSet = statement.execute(inString);			// Rrringggg! Rrriinngggg! 
		
		if (!hasResultSet) {
			currentDecorator.printRowCount(statement.getUpdateCount());
		} else {
			ResultSet rs = statement.getResultSet();
			int n = currentDecorator.write(rs);
			currentDecorator.printRowCount(n);
			currentDecorator.flush();
		}
	}
	
	/** Extract one statement from the given Reader.
	 * Ignore comments and null lines.
	 * @return The SQL statement, up to but not including the ';' character.
	 * May be null if not statement found.
	 */
	public static String getStatement(BufferedReader is)
	throws IOException {
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = is.readLine()) != null) {
			if (verbosity == Verbosity.DEBUG) {
				System.out.println("SQLRunner.getStatement(): LINE " + line);
			}
			if (line == null || line.length() == 0) {
				continue;
			}
			line = line.trim();
			if (line.startsWith("#") || line.startsWith("--")) {
				continue;
			}
			if (line.startsWith("\\")) {
				if (sb.length() == 0) {
					return line;
				}
				throw new IllegalArgumentException("Escape command found inside statement");
			}
			sb.append(line);
			int nb = sb.length();
			if (nb > 0 && sb.charAt(nb-1) == ';') {
				if (nb == 1) {
					return "";
				}
				sb.setLength(nb-1);
				return sb.toString();
			}
			// Add a space in case the sql is generated by a tool
			// that doesn't remember to add spaces (hopefully this won't
			// break tools that output newlines inside quoted strings!).
			sb.append(' ');
		}
		return null;
	}

	public void close() throws SQLException {
		statement.close();
		conn.close();
		out.flush();
		out.close();
	}

	public static Verbosity getVerbosity() {
		return verbosity;
	}

	public static void setVerbosity(Verbosity verbosity) {
		SQLRunner.verbosity = verbosity;
	}
}
