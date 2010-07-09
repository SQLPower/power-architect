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

package ca.sqlpower.architect.ddl;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.sqlpower.architect.ddl.DDLStatement.StatementType;
import ca.sqlpower.object.SPResolverRegistry;
import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.object.SPVariableResolver;
import ca.sqlpower.sqlobject.SQLCheckConstraint;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLEnumeration;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLType;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.sqlobject.SQLRelationship.Deferrability;
import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;
import ca.sqlpower.sqlobject.SQLTypePhysicalProperties.SQLTypeConstraint;

/**
 * Implements the quirks required for successful DDL generation that targets
 * H2 Database versions 1.0 and newer.
 */
public class H2DDLGenerator extends GenericDDLGenerator {
    
    public static final String GENERATOR_VERSION = "$Revision: 2933 $";
    
    public H2DDLGenerator() throws SQLException {
        super();
    }
    
    @Override
    public String getName() {
        return "H2 Database";
    }
 
    @Override
    public String getCatalogTerm() {
        return null;
    }

    @Override
    public String getSchemaTerm() {
        return "Schema";
    }

    @Override
    protected String getPlatformName() {
        return "H2 Database";
    }
    
    @Override
    public String columnType(SQLColumn c) {
        if (c.isAutoIncrement()) {
            return "IDENTITY";
        } else {
            return super.columnType(c);
        }
    }
    
    @Override
    public String getDeferrabilityClause(SQLRelationship r) {
        if (supportsDeferrabilityPolicy(r)) {
            return "";
        } else {
            throw new UnsupportedOperationException(getName() + " does not support " + 
                    r.getName() + "'s deferrability policy (" + r.getDeferrability() + ").");
        }
    }
    
    @Override
    public boolean supportsDeferrabilityPolicy(SQLRelationship r) {
        if (!Arrays.asList(Deferrability.values()).contains(r.getDeferrability())) {
            throw new IllegalArgumentException("Unknown deferrability policy: " + r.getDeferrability());
        } else {
            return r.getDeferrability() == Deferrability.NOT_DEFERRABLE;
        }
    }

    @Override
    protected void createTypeMap() throws SQLException {
        typeMap = new HashMap<Integer, GenericTypeDescriptor>();

        typeMap.put(Integer.valueOf(Types.BIGINT), new GenericTypeDescriptor("BIGINT", Types.BIGINT, 1000, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.BINARY), new GenericTypeDescriptor("BINARY", Types.BINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.BIT), new GenericTypeDescriptor("BIT", Types.BIT, 1, null, null, DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.BLOB), new GenericTypeDescriptor("LONGVARBINARY", Types.BLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.BOOLEAN), new GenericTypeDescriptor("BOOLEAN", Types.BOOLEAN, 1, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.CHAR), new GenericTypeDescriptor("CHAR", Types.CHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, true, false));
        typeMap.put(Integer.valueOf(Types.CLOB), new GenericTypeDescriptor("LONGVARCHAR", Types.CLOB, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.DATE), new GenericTypeDescriptor("DATE", Types.DATE, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.DECIMAL), new GenericTypeDescriptor("DECIMAL", Types.DECIMAL, 1000, null, null, DatabaseMetaData.columnNullable, true, true));
        typeMap.put(Integer.valueOf(Types.DOUBLE), new GenericTypeDescriptor("DOUBLE", Types.DOUBLE, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.FLOAT), new GenericTypeDescriptor("FLOAT", Types.FLOAT, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.INTEGER), new GenericTypeDescriptor("INTEGER", Types.INTEGER, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.LONGVARBINARY), new GenericTypeDescriptor("LONGVARBINARY", Types.LONGVARBINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.LONGVARCHAR), new GenericTypeDescriptor("LONGVARCHAR", Types.LONGVARCHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.NUMERIC), new GenericTypeDescriptor("NUMERIC", Types.NUMERIC, 1000, null, null, DatabaseMetaData.columnNullable, true, true));
        typeMap.put(Integer.valueOf(Types.REAL), new GenericTypeDescriptor("REAL", Types.REAL, 38, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.SMALLINT), new GenericTypeDescriptor("SMALLINT", Types.SMALLINT, 16, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.TIME), new GenericTypeDescriptor("TIME", Types.TIME, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.TIMESTAMP), new GenericTypeDescriptor("TIMESTAMP", Types.TIMESTAMP, 0, "'", "'", DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.TINYINT), new GenericTypeDescriptor("TINYINT", Types.TINYINT, 16, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.VARBINARY), new GenericTypeDescriptor("VARBINARY", Types.VARBINARY, 4000000000L, null, null, DatabaseMetaData.columnNullable, false, false));
        typeMap.put(Integer.valueOf(Types.VARCHAR), new GenericTypeDescriptor("VARCHAR", Types.VARCHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(SQLType.NVARCHAR), new GenericTypeDescriptor("NVARCHAR", SQLType.NVARCHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(SQLType.NCHAR), new GenericTypeDescriptor("NCHAR", SQLType.NCHAR, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, true, false));
		typeMap.put(Integer.valueOf(SQLType.NCLOB), new GenericTypeDescriptor("NCLOB", SQLType.NCLOB, 4000000000L, "'", "'", DatabaseMetaData.columnNullable, true, false));
    }
    
    @Override
    public boolean supportsUpdateAction(SQLRelationship r) {
        return r.getUpdateRule() != UpdateDeleteRule.RESTRICT;
    }
    
    @Override
    public String getUpdateActionClause(SQLRelationship r) {
        if (r.getUpdateRule() == UpdateDeleteRule.RESTRICT) {
            throw new IllegalArgumentException("Unsupported update action: " + r.getUpdateRule());
        } else {
            return super.getUpdateActionClause(r);
        }
    }
    
    @Override
    public boolean supportsDeleteAction(SQLRelationship r) {
        return r.getDeleteRule() != UpdateDeleteRule.RESTRICT;
    }
    
    @Override
    public String getDeleteActionClause(SQLRelationship r) {
        if (r.getDeleteRule() == UpdateDeleteRule.RESTRICT) {
            throw new IllegalArgumentException("Unsupported update action: " + r.getDeleteRule());
        } else {
            return super.getDeleteActionClause(r);
        }
    }
    
    @Override
    public String toString() {
        return "SQL Power H2 DDL Generator " + GENERATOR_VERSION;
    }

	/**
	 * Generate the SQL to rename a column.
	 * 
	 * @param oldTable
	 * @param newTable
	 */
	public void renameColumn(SQLColumn oldCol, SQLColumn newCol) {
		Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();
		print("\nALTER TABLE ");
		print(toQualifiedName(oldCol.getParent()));
		print(" ALTER COLUMN ");
		print(createPhysicalName(colNameMap, oldCol));
        print(" RENAME TO ");
		print(createPhysicalName(colNameMap, newCol));
		endStatement(StatementType.ALTER, newCol);
    }

    /**
     * Overridden to generate add table statement to have domain/type level
     * check constraints be added on the table level. The reason why it is being
     * generated on the table level is because H2 does not allow check
     * constraints on the column level.
     * 
     * Since check constraints from multiple objects are being combined into the
     * table level, we must ensure that there are no name conflicts by
     * prepending tags to identify which SQLObject type and physical name the
     * check constraint is actually supposed to be applied on. e.g.
     * col_<column-name>_<constraint-name> or table_<table-name>_<constraint-name>.
     * 
     * This is especially important since actual table level check constraints
     * will be added in the future.
     */
    @Override
    public void addTable(SQLTable t) throws SQLException, SQLObjectException {
           Map<String, SQLObject> colNameMap = new HashMap<String, SQLObject>();  // for detecting duplicate column names
            // generate a new physical name if necessary
            createPhysicalName(topLevelNames,t); // also adds generated physical name to the map
            print("\nCREATE TABLE ");
            print( toQualifiedName(t) );
            println(" (");
            boolean firstCol = true;
            
            List<SQLColumn> columns = t.getColumns();
            
            for (SQLColumn c : columns) {
                if (!firstCol) println(",");
                print("                ");

                print(columnDefinition(c,colNameMap));

                firstCol = false;
            }

            SQLIndex pk = t.getPrimaryKeyIndex();
            if (pk.getChildCount() > 0) {
                print(",\n");
                print("                ");
                writePKConstraintClause(pk);
            }
            
            for (SQLColumn c : columns) {
                UserDefinedSQLType type = c.getUserDefinedSQLType();
                List<SQLCheckConstraint> checkConstraints;
                SQLTypeConstraint constraintType = type.getConstraintType(getPlatformName());
                if (constraintType == null) {
                    constraintType = type.getDefaultPhysicalProperties().getConstraintType();
                    checkConstraints = type.getDefaultPhysicalProperties().getCheckConstraints();
                } else {
                    checkConstraints = type.getCheckConstraints(getPlatformName());
                }
                
                if (constraintType == SQLTypeConstraint.CHECK) {
                    print(",\n");
                    print(columnCheckConstraint(c, checkConstraints));
                }
            }

            print("\n)");
            endStatement(StatementType.CREATE, t);
            addComment(t, true);
    }
	
    /**
     * Overridden because check constraints can only be added to the table
     * level. Each constraint clause is delimited by a comma.
     */
    @Override
    protected String columnCheckConstraint(SQLColumn c, List<SQLCheckConstraint> checkConstraints) {
        if (!supportsCheckConstraint() || 
                c == null || 
                checkConstraints == null || 
                checkConstraints.isEmpty()) {
            return "";
        }
        
        SPVariableResolver resolver = c.getVariableResolver();
        SPVariableHelper helper = new SPVariableHelper(c);
        SPResolverRegistry.register(c, resolver);
        
        StringBuilder sb = new StringBuilder();
        for (SQLCheckConstraint constraint : checkConstraints) {
            if (sb.length() > 0) {
                sb.append(",\n");
            }
            sb.append("                ");
            sb.append(String.format("CONSTRAINT %s CHECK (%s)",
                    constraint.getName(),
                    helper.substitute(constraint.getConstraint())));
        }
        
        SPResolverRegistry.deregister(c, resolver);
        
        return sb.toString();
    }

    /**
     * H2 does not allow check constraints on the column level. Instead, we must
     * add them on the table level.
     * 
     * @see #addTable(SQLTable)
     */
    @Override
    protected String columnDefinition(SQLColumn c, Map<String, SQLObject> colNameMap) {
        StringBuffer def = new StringBuffer();

        // Column name
        def.append(createPhysicalName(colNameMap, c));

        def.append(" ");
        def.append(columnType(c));

        UserDefinedSQLType type = c.getUserDefinedSQLType();
        String defaultValue = type.getDefaultValue(getPlatformName());
        if ( defaultValue != null && !defaultValue.equals("")) {
            def.append(" ");
            def.append("DEFAULT ");
            def.append(defaultValue);
        }

        def.append(columnNullability(c));
        
        List<SQLEnumeration> enumerations;
        SQLTypeConstraint constraintType = type.getConstraintType(getPlatformName());
        if (constraintType == null) {
            constraintType = type.getDefaultPhysicalProperties().getConstraintType();
            enumerations = type.getDefaultPhysicalProperties().getChildrenWithoutPopulating(SQLEnumeration.class);
        } else {
            enumerations = type.getEnumerations(getPlatformName());
        }
        
        // Add enumeration.
        if (constraintType == SQLTypeConstraint.ENUM) {
            String columnEnumeration = columnEnumeration(c, enumerations);
            if (columnEnumeration != null && columnEnumeration.length() > 0) {
                def.append(" " + columnEnumeration);
            }
        }

        return def.toString();
    }

}
