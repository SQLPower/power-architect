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
package ca.sqlpower.architect;

/**
 * The SQLType class is a holder for a (name, typecode) pair.  The
 * type codes come from java.sql.Types, which themselves come from the
 * XOPEN specification.
 */
public class SQLType {

    /**
     * The official set of types.
     */
    private static final SQLType[] TYPES = {
        new SQLType("ARRAY", 2003),
        new SQLType("BIGINT", -5),
        new SQLType("BINARY", -2),
        new SQLType("BIT", -7),
        new SQLType("BLOB", 2004),
        new SQLType("BOOLEAN", 16),
        new SQLType("CHAR", 1),
        new SQLType("CLOB", 2005),
        new SQLType("DATE", 91),
        new SQLType("DECIMAL", 3),
        new SQLType("DISTINCT", 2001),
        new SQLType("DOUBLE", 8),
        new SQLType("FLOAT", 6),
        new SQLType("INTEGER", 4),
        new SQLType("JAVA_OBJECT", 2000),
        new SQLType("LONGVARBINARY", -4),
        new SQLType("LONGVARCHAR", -1),
        new SQLType("NULL", 0),
        new SQLType("NUMERIC", 2),
        new SQLType("OTHER", 1111),
        new SQLType("REAL", 7),
        new SQLType("REF", 2006),
        new SQLType("SMALLINT", 5),
        new SQLType("STRUCT", 2002),
        new SQLType("TIME", 92),
        new SQLType("TIMESTAMP", 93),
        new SQLType("TINYINT", -6),
        new SQLType("VARBINARY", -3),
        new SQLType("VARCHAR", 12)
    };
    
	/**
	 * The name of this type.
	 */
	private String name;

	/**
	 * The type code of this type.
	 */
	private int type;

	/**
	 * You won't normally need to use this.  See {@link #getTypes()}.
	 */ 
	public SQLType(String name, int type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Returns a shared, static array of all defined types.  Please
	 * don't modify the returned array!
	 */
	public static SQLType[] getTypes() {
		return TYPES;
	}

	public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}
	
	public String toString() {
		return name;
	}

	public static String getTypeName(int typecode) {
		SQLType type = getType(typecode);
		if (type == null) {
			return "Unknown Type "+typecode;
		} else {
			return type.getName();
		}
	}

	/**
	 * Returns a reference to the shared instance of SQLType that *has
	 * the requested type code.  If <code>typecode</code> is not a
	 * valid type code, returns null.
	 */
	public static SQLType getType(int typecode) {
		for (int i = 0; i < TYPES.length; i++) {
			if (TYPES[i].type == typecode) {
				return TYPES[i];
			}
		}
		return null;
	}
}
