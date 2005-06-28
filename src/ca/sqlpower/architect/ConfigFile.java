package ca.sqlpower.architect;

import java.io.*;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;
import org.xml.sax.SAXException;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.swingui.SwingUserSettings; // bad design
import ca.sqlpower.architect.etl.ETLUserSettings;
import ca.sqlpower.architect.ddl.DDLUserSettings;

import org.apache.log4j.Logger;
import org.apache.commons.digester.*;

public class ConfigFile {

	private static final Logger logger = Logger.getLogger(ConfigFile.class);

	public static final String DEFAULT_CONFIG_FILENAME = ".architect-prefs";
	
	/**
	 * The input or output file.
	 */
	protected File file;

	/**
	 * Where to write xml output to, or null if we're not in write
	 * mode.
	 */
	protected PrintWriter out;

	/**
	 * The current amount of indentation (xml nesting level) in the
	 * output file.
	 */
	protected int indent;
	
	/**
	 * The Digester will call setUserSettings() when it has finished
	 * parsing the config file.  The setter stores the new settings
	 * here.
	 */
	protected UserSettings userSettings;

	public ConfigFile(String filename) {
		file = new File(filename);
	}

	public static ConfigFile getDefaultInstance() {
		StringBuffer filename = new StringBuffer();
		filename.append(System.getProperty("user.home"));
		filename.append(System.getProperty("file.separator"));
		filename.append(DEFAULT_CONFIG_FILENAME);
		return new ConfigFile(filename.toString());
	}

	// -------------------- READING THE FILE --------------------------

	public UserSettings read() throws IOException {
		// use digester to read from file
		userSettings = new UserSettings();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			setupDigester().parse(in);
		} catch (SAXException ex) {
			logger.error("SAX Exception in config file parse!", ex);
		} catch (IOException ex) {
			logger.error("IO Exception in config file parse!", ex);
		} catch (Exception ex) {
			logger.error("General Exception in config file parse!", ex);
		}

		return userSettings;
	}

	protected Digester setupDigester() {
		Digester d = new Digester();
		d.setValidating(false);
		d.push(this);
		d.addObjectCreate("architect-settings", UserSettings.class);

		// jdbc drivers
		d.addCallMethod("architect-settings/jdbc-jar-files/jar", "addDriverJarPath", 0);

		// db connections
		d.addObjectCreate("architect-settings/db-connections/dbcs", ArchitectDataSource.class);
		d.addSetProperties
			("architect-settings/db-connections/dbcs",
			 new String[] {"connection-name", "driver-class", "jdbc-url", "user-name",
						   "user-pass", "sequence-number", "single-login"},
			 new String[] {"displayName", "driverClass", "url", "user",
						   "pass", "seqNo", "singleLogin"});
		d.addCallMethod("architect-settings/db-connections/dbcs", "setName", 0); // argument is element body text
		d.addSetNext("architect-settings/db-connections/dbcs", "addConnection",
					 "ca.sqlpower.sql.ArchitectDataSource");

		// gui settings
		d.addObjectCreate("architect-settings/swing-gui-settings", SwingUserSettings.class);
		d.addCallMethod("architect-settings/swing-gui-settings/setting", "putSetting", 3);
		d.addCallParam("architect-settings/swing-gui-settings/setting", 0, "name");
		d.addCallParam("architect-settings/swing-gui-settings/setting", 1, "class");
		d.addCallParam("architect-settings/swing-gui-settings/setting", 2, "value");
		d.addSetNext("architect-settings/swing-gui-settings", "setSwingSettings",
					 "ca.sqlpower.architect.swingui.SwingUserSettings");

		// ETL settings
		d.addObjectCreate("architect-settings/etl-user-settings", ETLUserSettings.class);
		d.addCallMethod("architect-settings/etl-user-settings/setting", "putProperty", 2);
		d.addCallParam("architect-settings/etl-user-settings/setting", 0, "name");
		d.addCallParam("architect-settings/etl-user-settings/setting", 1, "value");
		d.addSetNext("architect-settings/etl-user-settings", "setETLUserSettings",
					 "ca.sqlpower.architect.etl.ETLUserSettings");

		// DDL settings
		d.addObjectCreate("architect-settings/ddl-user-settings", DDLUserSettings.class);
		d.addCallMethod("architect-settings/ddl-user-settings/setting", "putProperty", 2);
		d.addCallParam("architect-settings/ddl-user-settings/setting", 0, "name");
		d.addCallParam("architect-settings/ddl-user-settings/setting", 1, "value");
		d.addSetNext("architect-settings/ddl-user-settings", "setDDLUserSettings",
					 "ca.sqlpower.architect.ddl.DDLUserSettings");

		// Print settings
		d.addObjectCreate("architect-settings/print-user-settings", PrintUserSettings.class);
		d.addCallMethod("architect-settings/print-user-settings/setting", "putProperty", 2);
		d.addCallParam("architect-settings/print-user-settings/setting", 0, "name");
		d.addCallParam("architect-settings/print-user-settings/setting", 1, "value");
		d.addSetNext("architect-settings/print-user-settings", "setPrintUserSettings",
					 "ca.sqlpower.architect.PrintUserSettings");



		d.addSetNext("architect-settings", "setUserSettings",
					 "ca.sqlpower.architect.UserSettings");
		return d;
	}
	
	public void setUserSettings(UserSettings settings) {
		userSettings = settings;
	}

	// -------------------- WRITING THE FILE --------------------------

	public void write(ArchitectSession session) throws ArchitectException {
		UserSettings us = session.getUserSettings();
		try {
			out = new PrintWriter(new FileWriter(file));
			indent = 0;

			println("<?xml version=\"1.0\"?>");
			println("<architect-settings version=\"0.1\">");
			indent++;

			// generate XML directly from settings
			writeDriverJarPaths(session.getDriverJarList());
			writeDbConnections(us.getConnections());
			writeSwingSettings(us.getSwingSettings());
			writeETLUserSettings(us.getETLUserSettings());
			writeDDLUserSettings(us.getDDLUserSettings());
			writePrintUserSettings(us.getPrintUserSettings());

			indent--;
			println("</architect-settings>");
		} catch (IOException e) {
			throw new ArchitectException("Couldn't save settings", e);
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
			out = null;
		}
	}

	protected void writeDriverJarPaths(List driverJarList) throws IOException {
		println("<jdbc-jar-files>");
		indent++;
		Iterator it = driverJarList.iterator();
		while (it.hasNext()) {
			println("<jar>"+it.next()+"</jar>");
		}
		indent--;
		println("</jdbc-jar-files>");
	}

	protected void writeDbConnections(List dbConnections) throws IOException {
		println("<db-connections>");
		indent++;
		Iterator it = dbConnections.iterator();
		while (it.hasNext()) {
			ArchitectDataSource dbcs = (ArchitectDataSource) it.next();
			print("<dbcs");
			niprint(" connection-name=\""+escape(dbcs.getName())+"\"");
			niprint(" driver-class=\""+escape(dbcs.getDriverClass())+"\"");
			niprint(" jdbc-url=\""+escape(dbcs.getUrl())+"\"");
			niprint(" user-name=\""+escape(dbcs.getUser())+"\"");
			niprint(" user-pass=\""+escape(dbcs.getPass())+"\"");
			niprint(">");
			niprint(escape(dbcs.getDisplayName()));
			niprintln("</dbcs>");
		}
		indent--;
		println("</db-connections>");
	}

	protected void writeSwingSettings(SwingUserSettings sprefs) {
		println("<swing-gui-settings>");
		indent++;

		Iterator it = sprefs.getSettingNames().iterator();
		while (it.hasNext()) {
			String prefName = (String) it.next();
			Object pref = sprefs.getObject(prefName, "");
			println("<setting name=\""+escape(prefName)
					+"\" class=\""+escape(pref.getClass().getName())
					+"\" value=\""+escape(pref.toString())+"\" />");
		}
		
		indent--;
		println("</swing-gui-settings>");
	}

	protected void writeETLUserSettings(ETLUserSettings etlprefs) {
		println("<etl-user-settings>");
		indent++;

		Properties props = etlprefs.toPropList();
		Iterator it = props.keySet().iterator();
		while (it.hasNext()) {
			String prefName = (String) it.next();
			println("<setting name=\""+escape(prefName)
					+"\" value=\""+escape(props.getProperty(prefName))+"\" />");
		}
		
		indent--;
		println("</etl-user-settings>");
	}

	protected void writeDDLUserSettings(DDLUserSettings ddlprefs) {
		println("<ddl-user-settings>");
		indent++;

		Properties props = ddlprefs.toPropList();
		Iterator it = props.keySet().iterator();
		while (it.hasNext()) {
			String prefName = (String) it.next();
			println("<setting name=\""+escape(prefName)
					+"\" value=\""+escape(props.getProperty(prefName))+"\" />");
		}
		
		indent--;
		println("</ddl-user-settings>");
	}


	protected void writePrintUserSettings(PrintUserSettings printPrefs) {
		println("<print-user-settings>");
		indent++;

		Properties props = printPrefs.toPropList();
		Iterator it = props.keySet().iterator();
		while (it.hasNext()) {
			String prefName = (String) it.next();
			println("<setting name=\""+escape(prefName)
					+"\" value=\""+escape(props.getProperty(prefName))+"\" />");
		}
		
		indent--;
		println("</print-user-settings>");
	}


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
	 * Replaces double quotes, ampersands, and less-than signs with
	 * their character reference equivalents.  This makes the returned
	 * string be safe for use as an XML attribute value enclosed in
	 * double quotes.
	 */
	protected String escape(String src) {
		StringBuffer sb = new StringBuffer(src.length()+10);  //XXX: arbitrary amount of extra space
		char ch;
		for (int i = 0, n = src.length(); i < n; i++) {
			switch (ch = src.charAt(i)) {
			case '"':
				sb.append("&#x22;");
				break;
				
			case '&':
				sb.append("&#x26;");
				break;
				
			case '<':
				sb.append("&#x3C;");
				break;
				
			default:
				sb.append(ch);
				break;
			}
		}
		return sb.toString();
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
