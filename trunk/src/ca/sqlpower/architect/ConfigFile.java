package ca.sqlpower.architect;

import java.io.*;
import java.util.List;
import java.util.Iterator;
import org.xml.sax.SAXException;

import ca.sqlpower.sql.DBConnectionSpec;
import ca.sqlpower.architect.swingui.SwingUserSettings; // bad design

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

		// db connections
		d.addObjectCreate("architect-settings/db-connections/dbcs", DBConnectionSpec.class);
		d.addSetProperties
			("architect-settings/db-connections/dbcs",
			 new String[] {"connection-name", "driver-class", "jdbc-url", "user-name",
						   "user-pass", "sequence-number", "single-login"},
			 new String[] {"displayName", "driverClass", "url", "user",
						   "pass", "seqNo", "singleLogin"});
		d.addCallMethod("architect-settings/db-connections/dbcs", "setName", 0); // argument is element body text
		d.addSetNext("architect-settings/db-connections/dbcs", "addConnection",
					 "ca.sqlpower.sql.DBConnectionSpec");

		// gui settings
		d.addObjectCreate("architect-settings/swing-gui-settings", SwingUserSettings.class);
		d.addCallMethod("architect-settings/swing-gui-settings/setting", "putSetting", 3);
		d.addCallParam("architect-settings/swing-gui-settings/setting", 0, "name");
		d.addCallParam("architect-settings/swing-gui-settings/setting", 1, "class");
		d.addCallParam("architect-settings/swing-gui-settings/setting", 2, "value");
		d.addSetNext("architect-settings/swing-gui-settings", "setSwingSettings",
					 "ca.sqlpower.architect.swingui.SwingUserSettings");

		d.addSetNext("architect-settings", "setUserSettings",
					 "ca.sqlpower.architect.UserSettings");
		return d;
	}
	
	public void setUserSettings(UserSettings settings) {
		userSettings = settings;
	}

	// -------------------- WRITING THE FILE --------------------------

	public void write(UserSettings us) throws ArchitectException {
		try {
			out = new PrintWriter(new FileWriter(file));
			indent = 0;

			println("<?xml version=\"1.0\"?>");
			println("<architect-settings version=\"0.1\">");
			indent++;

			// generate XML directly from settings
			writeDbConnections(us.getConnections());
			writeSwingSettings(us.getSwingSettings());

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

	protected void writeDbConnections(List dbConnections) throws IOException {
		println("<db-connections>");
		indent++;
		Iterator it = dbConnections.iterator();
		while (it.hasNext()) {
			DBConnectionSpec dbcs = (DBConnectionSpec) it.next();
			print("<dbcs");
			niprint(" connection-name=\""+dbcs.getName()+"\"");
			niprint(" driver-class=\""+dbcs.getDriverClass()+"\"");
			niprint(" jdbc-url=\""+dbcs.getUrl()+"\"");
			niprint(" user-name=\""+dbcs.getUser()+"\"");
			niprint(" user-pass=\""+dbcs.getPass()+"\"");
			niprint(" sequence-number=\""+dbcs.getSeqNo()+"\"");
			niprint(" single-login=\""+dbcs.isSingleLogin()+"\"");
			niprint(">");
			niprint(dbcs.getDisplayName());
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
			println("<setting name=\""+prefName
					+"\" class=\""+pref.getClass().getName()
					+"\" value=\""+pref+"\" />");
		}
		
		indent--;
		println("</swing-gui-settings>");
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
