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

import java.util.Arrays;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSequence;

/**
 * A DDLWarning for invalid name of a SQLSequence that can be 
 * fixed by calling setName() on one of the involved objects.
 * It has a side effect of setting the given SQLColumn's 
 * autoIncrementSequenceName as well.
 */
public class InvalidSeqNameDDLWarning extends AbstractDDLWarning {

    protected String whatQuickFixShouldCallIt;
    private final SQLColumn col;

    public InvalidSeqNameDDLWarning(String message,
            SQLSequence seq, SQLColumn col,
            String quickFixMesssage,
            String whatQuickFixShouldCallIt)
    {
        super(Arrays.asList(new SQLObject[]{seq}),
                message, true, quickFixMesssage,
                seq, "name");
        this.col = col;
        this.whatQuickFixShouldCallIt = whatQuickFixShouldCallIt;
    }

    public boolean quickFix() {
        // XXX need differentiator for setName() vs setPhysicalName()
        whichObjectQuickFixFixes.setName(whatQuickFixShouldCallIt);
        col.setAutoIncrementSequenceName(whatQuickFixShouldCallIt);
        return true;
    }
}
