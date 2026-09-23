/*
 * Copyright (c) 2026, SQL Power Group Inc.
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
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLParser.ParsedColumn;
import ca.sqlpower.architect.ddl.DDLParser.ParsedForeignKey;
import ca.sqlpower.architect.ddl.DDLParser.ParsedTable;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * Turns the {@link ParsedTable}s produced by {@link DDLParser} into model
 * {@link SQLTable} objects, including columns, primary keys, unique
 * constraints and foreign key relationships.
 *
 * <p>Data type names are mapped to {@link java.sql.Types} constants for the
 * common SQL types; anything else becomes {@code OTHER} while keeping the
 * original type spelling as the source data type name.
 */
public class DDLTableBuilder {

    private static final Logger logger = Logger.getLogger(DDLTableBuilder.class);

    private static final Map<String, Integer> TYPE_MAP = new HashMap<String, Integer>();
    static {
        TYPE_MAP.put("INT", Types.INTEGER);
        TYPE_MAP.put("INTEGER", Types.INTEGER);
        TYPE_MAP.put("MEDIUMINT", Types.INTEGER);
        TYPE_MAP.put("SERIAL", Types.INTEGER);
        TYPE_MAP.put("BIGSERIAL", Types.BIGINT);
        TYPE_MAP.put("BIGINT", Types.BIGINT);
        TYPE_MAP.put("SMALLINT", Types.SMALLINT);
        TYPE_MAP.put("TINYINT", Types.TINYINT);
        TYPE_MAP.put("BIT", Types.BIT);
        TYPE_MAP.put("BOOL", Types.BOOLEAN);
        TYPE_MAP.put("BOOLEAN", Types.BOOLEAN);
        TYPE_MAP.put("DECIMAL", Types.DECIMAL);
        TYPE_MAP.put("NUMERIC", Types.NUMERIC);
        TYPE_MAP.put("NUMBER", Types.NUMERIC);
        TYPE_MAP.put("MONEY", Types.DECIMAL);
        TYPE_MAP.put("SMALLMONEY", Types.DECIMAL);
        TYPE_MAP.put("FLOAT", Types.FLOAT);
        TYPE_MAP.put("REAL", Types.REAL);
        TYPE_MAP.put("DOUBLE", Types.DOUBLE);
        TYPE_MAP.put("CHAR", Types.CHAR);
        TYPE_MAP.put("CHARACTER", Types.CHAR);
        TYPE_MAP.put("NCHAR", Types.NCHAR);
        TYPE_MAP.put("VARCHAR", Types.VARCHAR);
        TYPE_MAP.put("VARCHAR2", Types.VARCHAR);
        TYPE_MAP.put("NVARCHAR", Types.NVARCHAR);
        TYPE_MAP.put("NVARCHAR2", Types.NVARCHAR);
        TYPE_MAP.put("LONGVARCHAR", Types.LONGVARCHAR);
        TYPE_MAP.put("TEXT", Types.LONGVARCHAR);
        TYPE_MAP.put("CLOB", Types.CLOB);
        TYPE_MAP.put("NCLOB", Types.NCLOB);
        TYPE_MAP.put("BINARY", Types.BINARY);
        TYPE_MAP.put("VARBINARY", Types.VARBINARY);
        TYPE_MAP.put("BLOB", Types.BLOB);
        TYPE_MAP.put("BYTEA", Types.BINARY);
        TYPE_MAP.put("IMAGE", Types.LONGVARBINARY);
        TYPE_MAP.put("DATE", Types.DATE);
        TYPE_MAP.put("TIME", Types.TIME);
        TYPE_MAP.put("TIMESTAMP", Types.TIMESTAMP);
        TYPE_MAP.put("TIMESTAMPTZ", Types.TIMESTAMP);
        TYPE_MAP.put("DATETIME", Types.TIMESTAMP);
        TYPE_MAP.put("DATETIME2", Types.TIMESTAMP);
        TYPE_MAP.put("SMALLDATETIME", Types.TIMESTAMP);
        TYPE_MAP.put("UUID", Types.OTHER);
        TYPE_MAP.put("XML", Types.SQLXML);
    }

    /**
     * Builds the model objects for the given parsed tables.
     */
    public List<SQLTable> buildTables(List<ParsedTable> parsedTables) throws SQLObjectException {
        List<SQLTable> result = new ArrayList<SQLTable>();
        Map<String, SQLTable> byName = new HashMap<String, SQLTable>();

        for (ParsedTable parsed : parsedTables) {
            SQLTable table = new SQLTable();
            table.initFolders(true);
            table.setName(parsed.name);
            table.setPhysicalName(parsed.name);

            for (ParsedColumn parsedCol : parsed.columns) {
                String baseType = baseTypeName(parsedCol.typeName);
                int[] params = typeParameters(parsedCol.typeName);
                Integer sqlType = TYPE_MAP.get(baseType.toUpperCase(Locale.ROOT));
                int typeCode = sqlType != null ? sqlType.intValue() : Types.OTHER;

                SQLColumn col = new SQLColumn(table, parsedCol.name, typeCode,
                        params[0], params[1]);
                col.setSourceDataTypeName(parsedCol.typeName.isEmpty() ? baseType : parsedCol.typeName);
                col.setNullable(parsedCol.nullable
                        ? DatabaseMetaData.columnNullable
                        : DatabaseMetaData.columnNoNulls);
                if (parsedCol.defaultValue != null && !parsedCol.defaultValue.isEmpty()) {
                    col.setDefaultValue(parsedCol.defaultValue);
                }
                table.addColumn(col);
            }

            // Primary key
            for (String pkName : parsed.primaryKeyColumns) {
                SQLColumn pk = table.getColumnByName(pkName);
                if (pk != null) {
                    table.addToPK(pk);
                }
            }

            result.add(table);
            byName.put(parsed.name.toLowerCase(Locale.ROOT), table);
        }

        // Foreign keys (second pass, once all tables exist)
        for (ParsedTable parsed : parsedTables) {
            SQLTable fkTable = byName.get(parsed.name.toLowerCase(Locale.ROOT));
            for (ParsedForeignKey fk : parsed.foreignKeys) {
                createRelationship(byName, fkTable, fk);
            }
        }

        return result;
    }

    private void createRelationship(Map<String, SQLTable> byName, SQLTable fkTable,
            ParsedForeignKey fk) {
        if (fk.referencedTable == null) return;
        SQLTable pkTable = byName.get(fk.referencedTable.toLowerCase(Locale.ROOT));
        if (pkTable == null || fkTable == null) {
            logger.debug("Skipping foreign key referencing unknown table " + fk.referencedTable);
            return;
        }
        try {
            SQLRelationship rel = new SQLRelationship();
            int count = Math.min(fk.columns.size(), fk.referencedColumns.size());
            if (count == 0) {
                // No explicit referenced columns: use the referenced table's PK.
                for (int i = 0; i < fk.columns.size() && i < pkTable.getPkSize(); i++) {
                    SQLColumn fkCol = fkTable.getColumnByName(fk.columns.get(i));
                    SQLColumn pkCol = pkTable.getColumn(i);
                    if (fkCol != null) rel.addMapping(pkCol, fkCol);
                }
            } else {
                for (int i = 0; i < count; i++) {
                    SQLColumn fkCol = fkTable.getColumnByName(fk.columns.get(i));
                    SQLColumn pkCol = pkTable.getColumnByName(fk.referencedColumns.get(i));
                    if (fkCol != null && pkCol != null) rel.addMapping(pkCol, fkCol);
                }
            }
            if (!rel.getChildren(SQLRelationship.ColumnMapping.class).isEmpty()) {
                fkTable.addChild(rel);
            }
        } catch (Exception e) {
            logger.warn("Could not create foreign key on " + fkTable.getName()
                    + " referencing " + fk.referencedTable, e);
        }
    }

    private static String baseTypeName(String typeName) {
        if (typeName == null) return "";
        int paren = typeName.indexOf('(');
        String base = paren >= 0 ? typeName.substring(0, paren) : typeName;
        return base.trim();
    }

    /**
     * @return {@code [precision, scale]}; -1 means "not specified".
     */
    private static int[] typeParameters(String typeName) {
        int precision = -1;
        int scale = -1;
        if (typeName != null) {
            int open = typeName.indexOf('(');
            int close = typeName.indexOf(')', open + 1);
            if (open >= 0 && close > open) {
                String params = typeName.substring(open + 1, close).trim();
                String[] parts = params.split(",");
                try {
                    if (parts.length > 0 && !parts[0].trim().isEmpty()) {
                        precision = Integer.parseInt(parts[0].trim());
                    }
                    if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                        scale = Integer.parseInt(parts[1].trim());
                    }
                } catch (NumberFormatException e) {
                    // e.g. CHAR(10 CHAR) - leave the parameters unspecified
                    precision = -1;
                    scale = -1;
                }
            }
        }
        return new int[] { precision, scale };
    }
}