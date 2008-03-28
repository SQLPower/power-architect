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
package ca.sqlpower.architect.ddl;

import ca.sqlpower.architect.SQLObject;

/**
 * The DDLStatement class combines a high-level description of what a
 * certain Data Definition Language statement does, as well as the
 * text of that statement for a particular database.
 */
public class DDLStatement {

	public static class StatementType {

		public static final StatementType CREATE = new StatementType("CREATE");
		public static final StatementType DROP = new StatementType("DROP");
		public static final StatementType ALTER = new StatementType("ALTER");
		public static final StatementType ADD_PK = new StatementType("ADD_PK");
		public static final StatementType ADD_FK = new StatementType("ADD_FK");
		public static final StatementType MODIFY = new StatementType("MODIFY");

        // NOT A DDL STATEMENT
        public static final StatementType SELECT = new StatementType("SELECT");

		private String type;

		private StatementType(String type) {
			this.type = type;
		}

		public boolean equals(Object other) {
			return this.type.equals(((StatementType) other).type);
		}

		@Override
		public int hashCode() {
			return type.hashCode();
		}

        public String toString() {
            return type;
        }
	}

	private String targetCatalog;
	private String targetSchema;
	private SQLObject object;
	private StatementType type;
	private String sqlText;
	private String sqlTerminator;

	public DDLStatement(
	        SQLObject object,
	        StatementType type,
	        String sqlText,
	        String sqlTerminator,
	        String targetCatalog,
	        String targetSchema) {
	    this.object = object;
	    this.type = type;
	    this.sqlText = sqlText;
	    this.sqlTerminator = sqlTerminator;
	    this.targetCatalog = targetCatalog;
	    this.targetSchema = targetSchema;
	}

	// ------------------------ Accessors and Mutators -------------------------

	/**
	 * See {@link #object}.
	 *
	 * @return the value of object
	 */
	public SQLObject getObject()  {
		return this.object;
	}

	/**
	 * See {@link #object}.
	 *
	 * @param argObject Value to assign to this.object
	 */
	public void setObject(SQLObject argObject) {
		this.object = argObject;
	}

	/**
	 * See {@link #type}.
	 *
	 * @return the value of type
	 */
	public StatementType getType()  {
		return this.type;
	}

	/**
	 * See {@link #type}.
	 *
	 * @param argType Value to assign to this.type
	 */
	public void setType(StatementType argType) {
		this.type = argType;
	}

	/**
	 * See {@link #sqlText}.
	 *
	 * @return the value of sqlText
	 */
	public String getSQLText()  {
		return this.sqlText;
	}

	/**
	 * See {@link #sqlText}.
	 *
	 * @param v Value to assign to this.sqlText
	 */
	public void setSQLText(String v) {
		this.sqlText = v;
	}

    public void setSqlTerminator(String sqlTerminator) {
        this.sqlTerminator = sqlTerminator;
    }

    public String getSqlTerminator() {
        return sqlTerminator;
    }

	public String getTargetCatalog() {
		return targetCatalog;
	}
	public void setTargetCatalog(String targetCatalog) {
		this.targetCatalog = targetCatalog;
	}
	public String getTargetSchema() {
		return targetSchema;
	}
	public void setTargetSchema(String targetSchema) {
		this.targetSchema = targetSchema;
	}

    public String toString() {
        return getType()+" "+DDLUtils.toQualifiedName(getTargetCatalog(), getTargetSchema(), object.getName());
    }
}
