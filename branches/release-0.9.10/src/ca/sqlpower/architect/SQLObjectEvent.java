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

import java.util.EventObject;

public class SQLObjectEvent extends EventObject implements java.io.Serializable {
	
	protected int[] changedIndices;
	protected SQLObject[] children;
	protected String propertyName;
	protected Object oldValue;
	protected Object newValue;
	
	/**
	 * Use this constructor for DBChildrenInserted and
	 * DBChildrenRemoved type events.  <code>propertyName</code> will be set to the
	 * string "children".
	 *
	 * @param source The SQLObject that changed.
	 * @param changedIndices The indices of the children that were
	 * added or removed.  The indices must be in ascending order.
	 * @param children The actual SQLObject instances that were added
	 * or removed to/from source.
	 */
	public SQLObjectEvent(SQLObject source, int[] changedIndices, SQLObject[] children) {
		super(source);
		this.changedIndices = changedIndices;
		this.children = children;
		this.propertyName = "children";
	}
	
	/**
	 * Use this constructor for DBObjectChanged type events.
	 * <code>changedIndices</code> and <code>children</code> will be null.
	 *
	 * @param source The SQLObject that changed
	 * @param propertyName The name of the property on source that changed.
	 */
	public SQLObjectEvent(SQLObject source, String propertyName, Object oldValue, Object newValue) {
		super(source);
		this.propertyName = propertyName;
		this.changedIndices = null;
		this.children = null;
		this.oldValue = oldValue;
		this.newValue= newValue;		
	}

	/**
	 * Use this constructor for DBObjectChanged type events.
	 * <code>changedIndices</code> and <code>children</code> will be null.
	 *
	 * @param source The SQLObject that changed
	 * @param propertyName The name of the property on source that changed.
	 */
	public SQLObjectEvent(SQLObject source, String propertyName) {
		super(source);
		this.propertyName = propertyName;
		this.changedIndices = null;
		this.children = null;
	}
	
	public SQLObject getSQLSource() {
		return (SQLObject) source;
	}

	
	/**
	 * Gets the value of changedIndices
	 *
	 * @return the value of changedIndices
	 */
	public int[] getChangedIndices()  {
		return this.changedIndices;
	}

	/**
	 * Sets the value of changedIndices
	 *
	 * @param argChangedIndices Value to assign to this.changedIndices
	 */
	public void setChangedIndices(int[] argChangedIndices) {
		this.changedIndices = argChangedIndices;
	}

	/**
	 * Gets the value of children
	 *
	 * @return the value of children
	 */
	public SQLObject[] getChildren()  {
		return this.children;
	}

	/**
	 * Sets the value of children
	 *
	 * @param argChildren Value to assign to this.children
	 */
	public void setChildren(SQLObject[] argChildren) {
		this.children = argChildren;
	}

	
	/**
	 * Gets the value of propertyName
	 *
	 * @return the value of propertyName
	 */
	public String getPropertyName()  {
		return this.propertyName;
	}

	/**
	 * Sets the value of propertyName
	 *
	 * @param argPropertyName Value to assign to this.propertyName
	 */
	public void setPropertyName(String argPropertyName) {
		this.propertyName = argPropertyName;
	}

	public Object getNewValue() {
		return newValue;
	}

	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SQLObjectEvent[");
        sb.append("source=").append(source);
        if (propertyName != null) {
            sb.append(" propertyName=").append(propertyName);
            sb.append(" oldValue=").append(oldValue);
            sb.append(" newValue=").append(newValue);
        }
        if (changedIndices != null) {
            sb.append(" changed indices=");
            for (int i = 0; i < changedIndices.length; i++) {
                if (i != 0) sb.append(" ");
                sb.append(changedIndices[i]);
            }
        }
        if (children != null) {
            sb.append(" children=");
            for (int i = 0; i < children.length; i++) {
                if (i != 0) sb.append(" ");
                sb.append(children[i] == null ? "null" : children[i].getName());
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
