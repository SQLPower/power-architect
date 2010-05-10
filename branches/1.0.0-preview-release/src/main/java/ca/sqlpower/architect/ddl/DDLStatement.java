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

import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.sqlobject.SQLObject;

/**
 * The DDLStatement class combines a high-level description of what a
 * certain Data Definition Language statement does, as well as the
 * text of that statement for a particular database.
 */
public class DDLStatement extends AbstractSPObject {

	public static class StatementType {

		public static final StatementType CREATE = new StatementType("CREATE");
		public static final StatementType DROP = new StatementType("DROP");
		public static final StatementType ALTER = new StatementType("ALTER");
		public static final StatementType ADD_PK = new StatementType("ADD_PK");
		public static final StatementType ADD_FK = new StatementType("ADD_FK");
		public static final StatementType MODIFY = new StatementType("MODIFY");
        public static final StatementType COMMENT = new StatementType("COMMENT");

        // NOT A DDL STATEMENT
        public static final StatementType SELECT = new StatementType("SELECT");
		public static final StatementType XMLTAG = new StatementType("XMLTAG");

		private String type;

		private StatementType(String type) {
			this.type = type;
		}

		public boolean equals(Object other) {
		    if (!(other instanceof StatementType)) {
		        return false;
		    }
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

	@Constructor
	public DDLStatement(
	        @ConstructorParameter(propertyName="object") SQLObject object,
	        @ConstructorParameter(propertyName="type") StatementType type,
	        @ConstructorParameter(propertyName="SQLText") String sqlText,
	        @ConstructorParameter(propertyName="sqlTerminator") String sqlTerminator,
	        @ConstructorParameter(propertyName="targetCatalog") String targetCatalog,
	        @ConstructorParameter(propertyName="targetSchema") String targetSchema) {
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
	@Accessor
	public SQLObject getObject()  {
		return this.object;
	}

	/**
	 * See {@link #object}.
	 *
	 * @param argObject Value to assign to this.object
	 */
	@Mutator
	public void setObject(SQLObject argObject) {
	    SQLObject oldObj = object;
		this.object = argObject;
		firePropertyChange("object", oldObj, argObject);
	}

	/**
	 * See {@link #type}.
	 *
	 * @return the value of type
	 */
	@Accessor
	public StatementType getType()  {
		return this.type;
	}

	/**
	 * See {@link #type}.
	 *
	 * @param argType Value to assign to this.type
	 */
	@Mutator
	public void setType(StatementType argType) {
	    StatementType oldType = type;
		this.type = argType;
		firePropertyChange("type", oldType, type);
	}

	/**
	 * See {@link #sqlText}.
	 *
	 * @return the value of sqlText
	 */
	@Accessor
	public String getSQLText()  {
		return this.sqlText;
	}

	/**
	 * See {@link #sqlText}.
	 *
	 * @param v Value to assign to this.sqlText
	 */
	@Mutator
	public void setSQLText(String v) {
	    String oldText = sqlText;
		this.sqlText = v;
		firePropertyChange("SQLText", oldText, v);
	}

	@Mutator
    public void setSqlTerminator(String sqlTerminator) {
	    String oldTerminator = this.sqlTerminator;
        this.sqlTerminator = sqlTerminator;
        firePropertyChange("sqlTerminator", oldTerminator, sqlTerminator);
    }

    @Accessor
    public String getSqlTerminator() {
        return sqlTerminator;
    }

    @Accessor
	public String getTargetCatalog() {
		return targetCatalog;
	}
    
    @Mutator
	public void setTargetCatalog(String targetCatalog) {
        String oldCatalog = this.targetCatalog;
		this.targetCatalog = targetCatalog;
		firePropertyChange("targetCatalog", oldCatalog, targetCatalog);
	}
	
	@Accessor
	public String getTargetSchema() {
		return targetSchema;
	}
	
	@Mutator
	public void setTargetSchema(String targetSchema) {
	    String oldSchema = this.targetSchema;
		this.targetSchema = targetSchema;
		firePropertyChange("targetSchema", oldSchema, targetSchema);
	}

    public String toString() {
        return getType()+" "+DDLUtils.toQualifiedName(getTargetCatalog(), getTargetSchema(), object.getName());
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }

    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        return 0;
    }

    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return Collections.emptyList();
    }

    public List<? extends SPObject> getChildren() {
        return Collections.emptyList();
    }

    public List<? extends SPObject> getDependencies() {
        // Assuming that the SQLObject field object is never modified/removed, as is the case currently...
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        
    }
}
