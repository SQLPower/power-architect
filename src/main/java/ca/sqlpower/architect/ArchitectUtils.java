/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Types;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.ExceptionReport;

/**
 * Collection of static utility methods for Architect.
 */
public class ArchitectUtils {

	public static final Logger logger = Logger.getLogger(ArchitectUtils.class);

	/**
	 * This class is just a container for utility routines; you do not
	 * need to instantiate it.
	 */
	private ArchitectUtils() {
		// never gets called
	}

    /**
     * Performs startup tasks for the architect system.  You should call
     * this when starting the Architect.
     */
    public static void startup() {
        ExceptionReport.init();
    }

	/**
	 * Sets up the log4j logging framework.
	 */
	public static void configureLog4j() {
		String configName = "log4j.properties";
		URL config = ArchitectUtils.class.getClassLoader().getResource(configName);
		if (config == null) {
			// It is probably not a good idea to change this to use log4j logging...
			System.err.println("Warning: Couldn't find log4j config resource '"+configName+"'");
		} else {
			org.apache.log4j.PropertyConfigurator.configure(config);
			logger.info("Log4J configured successfully");
		}
	}

	/**
	 * Does a generic object comparison where one or both of the objects could
	 * be null.  If both objects are null, they are considered equal; if only
	 * one is null, they are not equal; otherwise they are compared using
	 * <code>o1.equals(o2)</code>.
	 */
	public static boolean areEqual(Object o1, Object o2) {
		if (o1 == o2) return true;
		else if (o1 == null || o2 == null) return false;
		else return o1.equals(o2);
	}

	/**
	 * Chop long strings down to size for display purposes
	 *
	 * @param s the input string
	 * @return the truncated string
	 */
	public static String truncateString (String s) {
		if (s == null || s.length() < 28) {
			return s;
		} else {
			return s.substring(27) + "...";
		}
	}

	/**
     * Double quotes input string for CSV if needed:
     * string contains ", \n, \r, ','
     */
    public static String quoteCSV(Object val) {
        if ( val == null ) {
            return "";
        } else if ( val instanceof String ) {
            return quoteCSVStr((String) val);
        }
        return val.toString();
    }
    public static String quoteCSVStr(String val) {
        CharSequence doubleQuote = "\"".subSequence(0,"\"".length());
        CharSequence doubleDoubleQuote = "\"\"".subSequence(0,"\"\"".length());
        CharSequence newLine = "\n".subSequence(0,"\n".length());
        CharSequence cr = "\r".subSequence(0,"\r".length());
        CharSequence comma = ",".subSequence(0,",".length());

        if ( val.contains(doubleQuote) ) {
            StringBuffer sb = new StringBuffer(doubleQuote);
            sb.append(val.replace(doubleQuote,doubleDoubleQuote));
            sb.append(doubleQuote);
            return sb.toString();
        } else if ( val.contains(newLine) ||
                val.contains(cr) ||
                val.contains(comma) ) {
            StringBuffer sb = new StringBuffer(doubleQuote);
            sb.append(val);
            sb.append(doubleQuote);
            return sb.toString();
        }
        return val;
    }

    /**
     * Checks if the definitions of two columns are materially different.
     * Some data types (for example, DECIMAL and NUMERIC) are essentially
     * the same.  Also, the precision and scale values on DATE columns are
     * not of much consequence, but different databases report different
     * values.
     * 
     * @param targetColumn One of the columns to compare. Must not be null.
     * @param sourceColumn One of the columns to compare. Must not be null.
     * @return True iff the source and target columns are materially different
     * (as in, they are unlikely to be able to hold the same set of data as
     * each other)
     */
    public static boolean columnsDiffer(SQLColumn targetColumn, SQLColumn sourceColumn) {

        // eliminate meaningless type differences
        int targetType = compressType(targetColumn.getType());
        int sourceType = compressType(sourceColumn.getType());

        int targetPrecision = targetColumn.getPrecision();
        int sourcePrecision = sourceColumn.getPrecision();
        
        int targetScale = targetColumn.getScale();
        int sourceScale = sourceColumn.getScale();

		if (!needsScale(targetType)) {
			targetScale = 0;
		}
		if (!needsPrecision(targetType)) {
			targetPrecision = 0;
		}

		if (!needsScale(sourceType)) {
			sourceScale = 0;
		}
		if (!needsPrecision(sourceType)) {
			sourcePrecision = 0;
		}

        return (sourceType != targetType)
            || (targetPrecision != sourcePrecision)
            || (targetScale != sourceScale)
            || (targetColumn.getNullable() != sourceColumn.getNullable());
    }

	public static boolean needsScale(int type) {
		return (type == Types.DECIMAL ||
		        type == Types.NUMERIC);
	}

	public static boolean needsPrecision(int type) {
		final int NCHAR = -15; // Java6/JDBC 4.0
		final int NVARCHAR = -9; // Java6/JDBC 4.0
		return (type == Types.DECIMAL ||
		        type == Types.NUMERIC ||
				type == Types.VARCHAR ||
				type == Types.CHAR ||
				type == NCHAR ||
				type == NVARCHAR);
	}

    /**
     * Checks if the given column types materially differ. Some data 
     * types (for example, DECIMAL and NUMERIC) are essentially the same.
     * 
     * @param t1 One of the column types to compare
     * @param t2 One of the column types to compare.
     * @return True iff the given column types are materially different
     */
    public static boolean columnTypesDiffer(int t1, int t2) {
        int sourceType = compressType(t1);
        int targetType = compressType(t2);
        return sourceType != targetType;
    }
    
    /**
     * Compresses all the different kinds of essentially identical types
     * into an arbitrarily chosen one of them.  For instance, NUMERIC
     * and DECIMAL both compress to NUMERIC.
     * 
     * @param type
     * @return
     */
    private static int compressType(int type) {
        if (type == Types.DECIMAL) {
            return Types.NUMERIC;
        } else {
            return type;
        }
    }

    /**
     * This will check if the path of the PlDotIni file is valid. If it is not,
     * it will display a message asking the user to either browse for the file
     * or create a new file.
     * 
     * @param plDotIniPath
     *            The path defined to be the location of the pl.ini file.
     * @return The valid pl.ini path. This may be different from the one given
     *         if the user changed it.
     */
    public static String checkForValidPlDotIni(String plDotIniPath, String projectName) throws SQLObjectException {
        while (!isPlDotIniPathValid(plDotIniPath)) {
            String message;
            String[] options = new String[] {"Browse", "Create"};
            if (plDotIniPath == null) {
                message = "location is not set";
            } else if (new File(plDotIniPath).isFile()) {
                message = "file \n\n\""+plDotIniPath+"\"\n\n could not be read";
            } else {
                message = "file \n\n\""+plDotIniPath+"\"\n\n does not exist";
            }
            
            final int choice;
            Boolean isHeadless = Boolean.valueOf(
                    System.getProperty("ca.sqlpower.headless", "false"));
            if (isHeadless) {
                choice = 1;
            } else {
                choice = JOptionPane.showOptionDialog(null,   // blocking wait
                        "The " + projectName + " keeps its list of database connections" +
                        "\nin a file called PL.INI.  Your PL.INI "+message+"." +
                        "\n\nYou can browse for an existing PL.INI file on your system" +
                        "\nor allow the " + projectName + " to create a new one in your home directory." +
                        "\n\nHint: If you are a Power*Loader Suite user, you should browse for" +
                        "\nan existing PL.INI in your Power*Loader installation directory.",
                        "Missing PL.INI", 0, JOptionPane.INFORMATION_MESSAGE, null, options, null);
            }
            
            final File newPlIniFile;
            if (choice == JOptionPane.CLOSED_OPTION) {
                throw new SQLObjectException("Can't start without a pl.ini file");
            } else if (choice == 0) {
                
                // Don't use recent files menu for default dir here.. we're looking for PL.INI
                JFileChooser fc = new JFileChooser();
                
                fc.setFileFilter(SPSUtils.INI_FILE_FILTER);
                fc.setDialogTitle("Locate your PL.INI file");
                int fcChoice = fc.showOpenDialog(null);       // blocking wait
                if (fcChoice == JFileChooser.APPROVE_OPTION) {
                    newPlIniFile = fc.getSelectedFile();
                } else {
                    newPlIniFile = null;
                }
            } else if (choice == 1) {
                newPlIniFile = new File(System.getProperty("user.home"), "pl.ini");
            } else
                throw new SQLObjectException("Unexpected return from JOptionPane.showOptionDialog to get pl.ini");

            if (newPlIniFile != null) try {
                newPlIniFile.createNewFile();
                return newPlIniFile.getPath();
            } catch (IOException e1) {
                logger.error("Caught IO exception while creating empty PL.INI at \""
                        +newPlIniFile.getPath()+"\"", e1);
                ASUtils.showExceptionDialogNoReport("Failed to create file \""+newPlIniFile.getPath()+"\".", e1);
            }
        }
        return plDotIniPath;
    }
    
    /**
     * This is a helper method for checkForValidPlDotIni().
     */
    private static boolean isPlDotIniPathValid(String plDotIniPath) {
        logger.debug("Checking pl.ini path: "+plDotIniPath);
        String path = plDotIniPath;
        if (path == null) {
            return false;
        } else {
            File f = new File(path);
            return (f.canRead() && f.isFile());
        }
    }
    
    /**
     * A simple method that converts classes to a nicer human-readable
     * name.
     */
    public static String convertClassToString(Class<?> c) {
        if (SQLTable.class.equals(c)) {
            return "Table";
        } else if (SQLColumn.class.equals(c)) {
            return "Column";
        } else if (SQLRelationship.class.equals(c)) {
            return "Relationship";
        } else if (SQLIndex.class.equals(c)) {
            return "Index";
        } else if (JDBCDataSourceType.class.equals(c)) {
            return "Data Source Type";
        } else {
            return c.getSimpleName();
        }
    }
}