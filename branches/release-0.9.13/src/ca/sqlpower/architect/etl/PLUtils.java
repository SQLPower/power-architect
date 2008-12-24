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
package ca.sqlpower.architect.etl;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.SPDataSource;

/**
 * PLUtils is a collection of utility methods for interfacing with the Power*Loader.
 */
public class PLUtils {

	private static final Logger logger = Logger.getLogger(PLUtils.class);

	public static final String CONNECTION_TYPE_ORACLE = "ORACLE";
	public static final String CONNECTION_TYPE_SQLSERVER = "SQL SERVER";
	public static final String CONNECTION_TYPE_POSTGRES = "POSTGRES";
	public static final String CONNECTION_TYPE_ACCESS = "ACCESS";
	public static final String CONNECTION_TYPE_DB2 = "DB2";
	
	/**
	 * Maximum length (in characters) of a PL identifier (transaction
	 * name, job name, etc).
	 */
	public static final int MAX_PLID_LENGTH = 80;


	/**
	 * Store the last time we loaded PL.INI from disk
	 */
	private static java.util.Date plLastReadTimestamp = new Date(0);

	/** PLUtils is a non-instantiable class. */
	private PLUtils() {
        // this method doesn't get called
    }

	/**
	 * Mangles the given string into a valid PL identifier (no spaces,
	 * at most 80 characters long, all uppercase).
	 */
	public static String toPLIdentifier(String text) {
		if (text.length() > MAX_PLID_LENGTH) text = text.substring(0, MAX_PLID_LENGTH);
		StringBuffer plid = new StringBuffer(text.toUpperCase());
		for (int i = 0, n = plid.length(); i < n; i++) {
			if (Character.isWhitespace(plid.charAt(i))) {
				plid.setCharAt(i, '_');
			}
		}
		return plid.toString();
	}

	public static boolean plDotIniHasChanged(String plDotIniPath) {
		File inputFile = new File(plDotIniPath);
		boolean retVal = false;
		// logger.debug("last mod=" + new Date(inputFile.lastModified()) + ", currTimestamp=" + plLastReadTimestamp);
		if (inputFile.lastModified() > plLastReadTimestamp.getTime()) {
			retVal = true;
		}
		inputFile = null; // is this necessary?
		return retVal;
	}
	
	public static String getEngineExecutableName(SPDataSource dataSource) throws UnknownDatabaseTypeException {
		String type = dataSource.get(SPDataSource.PL_TYPE);
		if (type == null) {
			throw new UnknownDatabaseTypeException("<unspecified>");
		} else if (type.equalsIgnoreCase(CONNECTION_TYPE_SQLSERVER)
				   || type.equalsIgnoreCase(CONNECTION_TYPE_ACCESS)
				   || type.equalsIgnoreCase(CONNECTION_TYPE_DB2)
                   || type.equalsIgnoreCase(CONNECTION_TYPE_POSTGRES)) {
			return "PowerLoader_odbc.exe";
		} else if (type.equalsIgnoreCase(CONNECTION_TYPE_ORACLE)) {
			return "PowerLoader_oracle.exe";
		} else {
			throw new UnknownDatabaseTypeException(type);
		}
	}

	/**
	 * Returns the correct argument for USER= when running the PL engine.
	 *
	 * Use the same rules as engine executable name to decide what 
	 * kind of connection string to return.
	 */
	public static String getEngineConnectString(SPDataSource dataSource) throws UnknownDatabaseTypeException {
		logger.debug("get engine connect String PWD: " + dataSource.get(SPDataSource.PL_PWD));
		String type = dataSource.get(SPDataSource.PL_TYPE);
		if (type == null) {
			throw new UnknownDatabaseTypeException("<unspecified>");
		}
		if (type.equalsIgnoreCase(CONNECTION_TYPE_SQLSERVER) 
		    || type.equalsIgnoreCase(CONNECTION_TYPE_ACCESS)
		    || type.equalsIgnoreCase(CONNECTION_TYPE_DB2)
		    || type.equalsIgnoreCase(CONNECTION_TYPE_POSTGRES)) {
			return dataSource.get(SPDataSource.PL_UID)+"/"
				      +dataSource.get(SPDataSource.PL_PWD)+"@"
					  +dataSource.get(SPDataSource.PL_LOGICAL);
		} else if (type.equalsIgnoreCase(CONNECTION_TYPE_ORACLE)) {
				return dataSource.get(SPDataSource.PL_UID)+"/"
				      +dataSource.get(SPDataSource.PL_PWD)+"@"
					  +dataSource.get(SPDataSource.PL_TNS);
		} else {
			throw new UnknownDatabaseTypeException(type);
		}
	}
	
	
}
