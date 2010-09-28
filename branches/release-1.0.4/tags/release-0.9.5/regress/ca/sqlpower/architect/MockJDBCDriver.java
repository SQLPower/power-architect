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
package ca.sqlpower.architect;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * A driver for the MockJDBCDatabase, which we can use for testing.
 * 
 * <p>The properties you pass to connect() are really important.  They define how the
 * resulting database "connection" will behave.
 * 
 * <dl>
 *  <dd>dbmd.*</dd>
 *   <dt>These properties define the return value of various DatabaseMetaData methods</dt>
 *  <dd>dbmd.catalogTerm</dd>
 *   <dt>The name this database uses for catalogs.  If not present, this
 *       database will not support catalogs.<dt>
 *  <dd>dbmd.schemaTerm</dd>
 *   <dt>The name this database uses for schemas.  If not present, this
 *       database will not support schemas. <dt>
 *  <dd>catalogs={list}</dd>
 *    <dt>A comma-separated list of catalogs this database claims to have<dt>
 *  <dd>schemas[.catalog]={list}</dd>
 *    <dt>A comma-separated list of schemas this database claims to have
 *        in catalog.  If the database doesn't have catalogs, leave out the ".catalog" part.
 *  <dd>tables[.catalog][.schema]={list}</dd>
 *    <dt>A comma-separated list of tables in the named schema.catalog.  Leave out the ".catalog"
 *        or ".schema" part if you've configured this database to be schemaless or catalogless.</dt>
 * </dl>
 * 
 * @author fuerth
 * @version $Id$
 */
public class MockJDBCDriver implements Driver {

	private static final Logger logger = Logger.getLogger(MockJDBCDriver.class);
	
	public Connection connect(String url, Properties info) throws SQLException {
		String params = url.substring("jdbc:mock:".length());
		String keyValuePairs[] = params.split("&");
		for (String keyvalue : Arrays.asList(keyValuePairs)) {
			String kv[] = keyvalue.split("=");
			logger.debug("Found URL property '"+kv[0]+"' = '"+kv[1]+"'");
			info.put(kv[0], kv[1]);
		}
		return new MockJDBCConnection(url, info);
	}

	public boolean acceptsURL(String url) throws SQLException {
		return url.startsWith("jdbc:mock");
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		return new DriverPropertyInfo[0];
	}

	public int getMajorVersion() {
		return 0;
	}

	public int getMinorVersion() {
		return 0;
	}

	public boolean jdbcCompliant() {
		return false;
	}

}
