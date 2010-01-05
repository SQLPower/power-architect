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

import ca.sqlpower.architect.etl.kettle.KettleRepositoryDirectoryChooser;
import ca.sqlpower.architect.etl.kettle.RootRepositoryDirectoryChooser;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.testutil.GenericNewValueMaker;

public class ArchitectValueMaker extends GenericNewValueMaker {
    
    public ArchitectValueMaker(SPObject root) {
        super(root);
    }

    public Object makeNewValue(Class<?> valueType, Object oldVal, String propName) {
        Object newVal;  // don't init here so compiler can warn if the following code doesn't always give it a value

        if (valueType == SQLColumn.class) {
            newVal = new SQLColumn();
            ((SQLColumn) newVal).setName("testing!");
        } else if (valueType == SQLIndex.class) {
            newVal = new SQLIndex();
            ((SQLIndex) newVal).setName("a new index");
        } else if (valueType == KettleRepositoryDirectoryChooser.class) {
            newVal = new RootRepositoryDirectoryChooser();
        } else if (valueType.isAssignableFrom(Throwable.class)) {
            newVal = new SQLObjectException("Test Exception");
        } else {
            newVal = super.makeNewValue(valueType, oldVal, propName);
        }

        return newVal;
    }
}
