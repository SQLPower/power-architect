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

import java.util.List;

import ca.sqlpower.architect.SQLObject;

/**
 * A DDLWarning object encapsulates the details of a single warning
 * message issued by a DDL generator.
 */
public interface DDLWarning {

	/**
	 * Get the message associated with this warning, e.g., a string
     * like "Primary Key Name is already in use"
	 */
	public String getMessage();

	/**
	 * The subject(s) of this warning.  For instance, if there is a
	 * duplicate table names, the SQLTable objects with the duplicate
	 * names will be the "involved objects".
	 */
	public List<? extends SQLObject> getInvolvedObjects();

	/** Return true if the user has repaired or quickfixed the problem */
	public boolean isFixed();

    public void setFixed(boolean fixed);

    /** Tell whether the user can "quick fix" this problem */
    public boolean isQuickFixable();

    /** If isQuickFixable(), then this gives the message about what
     * will be done.
     */
    public String getQuickFixMessage();

    /** If isQuickFixable(), then this applies the quick fix */
    public boolean quickFix();

    /**
     * Returns the name of the Beans property of the involved object(s) that
     * can be modified to fix the problem.  For example, if the warning is
     * about a duplicate or illegal name, this method would return "name".
     * If the warning is about an illegal type, this method would return "type".
     * If the warning does not pertain to a problem that can be fixed by
     * fiddling with a particular property value, this method will return null.
     */
    public String getQuickFixPropertyName();
}
