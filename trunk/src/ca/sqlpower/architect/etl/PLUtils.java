/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
