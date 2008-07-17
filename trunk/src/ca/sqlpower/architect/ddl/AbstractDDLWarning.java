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

public abstract class AbstractDDLWarning implements DDLWarning {

    protected List<? extends SQLObject> involvedObjects;
    protected String message;
    protected boolean fixed;
    protected boolean isQuickFixable;
    protected String quickFixMesssage;
    protected SQLObject whichObjectQuickFixFixes;
    
    /**
     * The name of the Beans property of the object(s) involved in this warning
     * that can be modified to fix the problem. For example, if the warning is
     * about a duplicate or illegal name, this would be "name". If the warning
     * is about an illegal type, it would be "type". If the warning does not
     * pertain to a problem that can be fixed by fiddling with a particular
     * property value, this will be null.
     */
    protected String quickFixPropertyName;
    
    public AbstractDDLWarning(
            List<? extends SQLObject> involvedObjects,
            String message,
            boolean isQuickFixable,
            String quickFixMesssage,
            SQLObject whichObjectQuickFixFixes,
            String quickFixPropertyName) {
        super();
        for (SQLObject so : involvedObjects) {
            if (so == null) {
                throw new NullPointerException("None of the objects in the involvedObjects list can be null!");
            }
        }
        this.involvedObjects = involvedObjects;
        this.message = message;
        this.isQuickFixable = isQuickFixable;
        this.quickFixMesssage = quickFixMesssage;
        this.whichObjectQuickFixFixes = whichObjectQuickFixFixes;
        this.quickFixPropertyName = quickFixPropertyName;
    }


    public List<? extends SQLObject> getInvolvedObjects() {
        return involvedObjects;
    }

    public String getMessage() {
        return message;
    }

    public String getQuickFixMessage() {
        return quickFixMesssage;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isQuickFixable() {
        return isQuickFixable;
    }

    /** Dummy version for subclasses that are not quickfixable */
    public boolean quickFix() {
        throw new IllegalStateException("Called generic version of quickFix");
    }

    /**
     * Returns the value of {@link #quickFixPropertyName}.  Subclasses that support QuickFix
     * should initialize that value to the appropriate property name.
     */
    public String getQuickFixPropertyName() {
        return quickFixPropertyName;
    }
}
