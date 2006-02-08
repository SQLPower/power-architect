package regress.ca.sqlpower.architect;

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
