/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.architect.enterprise;

import java.util.List;

import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.UserDefinedSQLType;

/**
 * This converter contains specific conversions for Architect objects. This will
 * still convert types in the base class.
 */
public class ArchitectPersisterSuperConverter extends SessionPersisterSuperConverter {

    public ArchitectPersisterSuperConverter(DataSourceCollection<? extends SPDataSource> dsCollection, SPObject root) {
        super(dsCollection, root);
    }
    
    @Override
    public Object convertToBasicType(Object convertFrom, Object... additionalInfo) {
        return super.convertToBasicType(convertFrom, additionalInfo);
    }
    
    @Override
    public Object convertToComplexType(Object o, Class<? extends Object> type) {
        if (o == null) {
            return null;
            
        } else if (SPObject.class.isAssignableFrom(type)) {
            SPObject foundObject = spObjectConverter.convertToComplexType((String) o);
            if (foundObject == null && dsCollection != null) {
                List<UserDefinedSQLType> listOfSqlTypes = dsCollection.getSQLTypes();
                for (UserDefinedSQLType sqlType : listOfSqlTypes) {
                    if (sqlType != null && sqlType.getUUID().equals((String) o)) {
                        return sqlType;
                    }
                }
            }
            return foundObject;
        } else {
            return super.convertToComplexType(o, type);
        }
    }

}
