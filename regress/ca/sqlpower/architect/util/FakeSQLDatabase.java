/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

/**
 * 
 */
package ca.sqlpower.architect.util;

import java.sql.SQLException;
import java.util.Properties;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.testutil.MockJDBCConnection;
import ca.sqlpower.testutil.MockJDBCDriver;

/**
 * A SQLDatabase instance that uses a MockJDBCConnection. This class takes care
 * of the fancy footwork required to get around the problem that connections are
 * normally pooled, and there is no way to get the real connection object back
 * from the pool.
 */
public class FakeSQLDatabase extends SQLDatabase {
    
    private final MockJDBCConnection con;
    private final SPDataSource ds;

    public FakeSQLDatabase(String url) throws SQLException {
        super((SPDataSource) null);
        MockJDBCDriver driver = new MockJDBCDriver();
        con = (MockJDBCConnection) driver.connect(url, new Properties());
        ds = new SPDataSource((DataSourceCollection)null) {
            
            @Override
            public String getName() {
                return "mock database";
            }
            
            @Override
            public SPDataSourceType getParentType() {
                return new SPDataSourceType() {
                    @Override
                    public boolean getSupportsUpdateableResultSets() {
                        return true;
                    }
                };
            }
        };
    }
    
    @Override
    public MockJDBCConnection getConnection() {
        return con;
    }
    
    @Override
    public SPDataSource getDataSource() {
    	return ds;
    }
    
    @Override
    public void setDataSource(SPDataSource argDataSource) {
        // do nothing
    }
}