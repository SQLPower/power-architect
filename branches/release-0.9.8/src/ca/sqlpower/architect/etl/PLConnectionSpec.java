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

import java.util.Properties;

/**
 * The PLConnectionSpec class is a container for POWER*LOADER ODBC
 * Connection information (normally retrieved from the PL.ini file).
 */
public class PLConnectionSpec {
	
	Properties props;
	
	public PLConnectionSpec() {
		props = new Properties();
	}

	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}

	// ----------------- accessors and mutators -------------------
	
	public String getLogical()  {
		return props.getProperty("Logical");
	}

	public String getDbType()  {
		return props.getProperty("Type");
	}

	public String getPlsOwner()  {
		return props.getProperty("PL Schema Owner");
	}

	public String getTNSName() {
		return props.getProperty("TNS Name");
	}

	public String getUid()  {
		return props.getProperty("UID");
	}

	public String getDSN() {
		return props.getProperty("DSN");
	}		

	public String getPwd()  {
	    return props.getProperty("PWD");
	}
}
