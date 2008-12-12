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

import java.util.ArrayList;

/**
 * The StubSQLObject is a general-purpose SQLObject that you can use for testing
 * the Architect.  You might need to subclass it, or you might need to enhance
 * it directly.  Which is better is a judgement call!
 */
public class StubSQLObject extends SQLObject {

    /**
     * Keeps track of how many times populate() has been called.
     */
    private int populateCount = 0;
    
    public StubSQLObject() {
        children = new ArrayList<SQLObject>();
    }
    
    @Override
    public SQLObject getParent() {
        return null;
    }

    @Override
    protected void setParent(SQLObject parent) {
        // no op
    }

    @Override
    protected void populate() throws ArchitectException {
        populateCount++;
    }

    @Override
    public String getShortDisplayName() {
        return null;
    }

    @Override
    public boolean allowsChildren() {
        return true;
    }

    @Override
    public Class<? extends SQLObject> getChildType() {
        return null;
    }

    // ======= non-SQLObject methods below this line ==========
    
    public int getPopulateCount() {
        return populateCount;
    }
}