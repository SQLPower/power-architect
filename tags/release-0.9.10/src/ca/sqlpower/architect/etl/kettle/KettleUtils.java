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
package ca.sqlpower.architect.etl.kettle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;

public class KettleUtils {

    private static final Logger logger = Logger.getLogger(KettleUtils.class);
    
    public static List<String> retrieveKettleConnectionTypes() {
        List<String> list = new ArrayList<String>();
        DatabaseInterface[] dbConnectionArray = DatabaseMeta.getDatabaseInterfaces();
        for (int i = 0; i < dbConnectionArray.length; i++) {
            list.add(dbConnectionArray[i].getDatabaseTypeDescLong());
        }
        return list;
    }
    
    /**
     * Creates a DatabaseMeta object based on the SPDataSource given to it.
     * This will return null if an error occurred and execution should stop. 
     * 
     * @param target The target datasource to create the DatabaseMeta upon,
     *               can not be null.
     */
    public static DatabaseMeta createDatabaseMeta(SPDataSource target) {
        DatabaseMeta databaseMeta;
        
        String databaseName = target.getName();
        String username = target.getUser();
        String password = target.getPass();
        SPDataSourceType targetType = target.getParentType();
        String connectionType = "";
        if (targetType.getKettleNames().size() > 0) {
            connectionType = targetType.getKettleNames().get(0);
        } else {
            throw new RuntimeException("Error: invalid target datasource (it contains no Kettle names)");
        }
        Map<String, String> map = targetType.retrieveURLParsing(target.getUrl());
        String hostname = map.get(KettleOptions.KETTLE_HOSTNAME);
        if (hostname == null) {
            hostname = target.get(KettleOptions.KETTLE_HOSTNAME_KEY);
        }
        String port = map.get(KettleOptions.KETTLE_PORT);
        if (port == null) {
            port = target.get(KettleOptions.KETTLE_PORT_KEY);
        }
        String database = map.get(KettleOptions.KETTLE_DATABASE);
        if (database == null) {
            database = target.get(KettleOptions.KETTLE_DATABASE_KEY);
        }
        
        databaseMeta = new DatabaseMeta(databaseName
                                              , connectionType
                                              , "Native"
                                              , hostname==null?"":hostname
                                              , database==null?"":database
                                              , port==null?"":port
                                              , username
                                              , password);
        

        return databaseMeta;
    }
}
