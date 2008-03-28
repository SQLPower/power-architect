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

import java.util.Collections;

/**
 * A SQLObject that represents a sequence in a database. This is not yet a
 * full-fledged SQLObject, because it does not have a place in the overall
 * SQLObject tree, and it is not saved with the project. Its main purpose at
 * this point is to exist temporarily during the DDL generation process. One
 * day, the SQLSequence may be worked into the overall tree; however it is a
 * feature only supported by a small number of platforms (only Oracle and
 * PostgreSQL as far as we know) so it is not a high priority.
 * <p>
 * See {@link SQLColumn#getAutoIncrementSequenceName()} for more details on
 * how a sequence might come to exist.
 */
public class SQLSequence extends SQLObject {

    /**
     * Creates a new SQLSequence with the given name.
     */
    public SQLSequence(String name) {
        setPopulated(true);
        setName(name);
        children = Collections.emptyList();
    }
    
    /**
     * Returns false.
     */
    @Override
    public boolean allowsChildren() {
        return false;
    }

    /**
     * Returns null because this type of object doesn't allow children.
     */
    @Override
    public Class<? extends SQLObject> getChildType() {
        return null;
    }

    /**
     * Returns null because this type of object doesn't have a parent.
     */
    @Override
    public SQLObject getParent() {
        return null;
    }
    
    /**
     * Does nothing because this type of object doesn't have a parent.
     */
    @Override
    protected void setParent(SQLObject parent) {
        // no-op
    }

    /**
     * Returns the name of this SQLSequence.
     */
    @Override
    public String getShortDisplayName() {
        return getName();
    }

    /**
     * Does nothing because this type of object is not reverse-engineered.
     */
    @Override
    protected void populate() throws ArchitectException {
        // no op
    }
    
}
