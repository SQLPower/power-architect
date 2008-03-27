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
