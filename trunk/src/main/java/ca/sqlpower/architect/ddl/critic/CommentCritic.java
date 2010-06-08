/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.architect.ddl.critic;

import ca.sqlpower.sqlobject.SQLColumn;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLTable;

public class CommentCritic implements Critic<SQLObject> {

    private final int maxTableCommentLength;
    private final int maxColumnCommentLength;
    private final String platformName;

    public CommentCritic(String platformName, int maxLengthTable, int maxLengthColumn) {
        this.platformName = platformName;
        this.maxColumnCommentLength = maxLengthColumn;
        this.maxTableCommentLength = maxLengthTable;
    }
    
    public List<Criticism<SQLObject>> criticize(final SQLObject so) {
        
        if (!(so instanceof SQLTable || so instanceof SQLColumn)) return Collections.emptyList();

        String remarks = null;
        int maxLength = 0;

        if (so instanceof SQLTable) {
            remarks = ((SQLTable)so).getRemarks();
            maxLength = maxTableCommentLength;
        } else if (so instanceof SQLColumn) {
            remarks = ((SQLColumn)so).getRemarks();
            maxLength = maxColumnCommentLength;
        }
        
        List<Criticism<SQLObject>> criticisms = new ArrayList<Criticism<SQLObject>>();
        if (remarks != null && remarks.length() > maxLength && maxLength > 0) {
            criticisms.add(new Criticism<SQLObject>(
                    so,
                    "Comment too long for " + platformName,
                    new QuickFix("Truncate comment to " + maxLength + " characters") {
                        public void apply() {
                            if (so instanceof SQLTable) {
                                SQLTable tbl = (SQLTable)so;
                                if (tbl.getRemarks() != null && tbl.getRemarks().length() > maxTableCommentLength) {
                                    tbl.setRemarks(tbl.getRemarks().substring(maxTableCommentLength));
                                }
                            } else if (so instanceof SQLColumn) {
                                SQLColumn col = (SQLColumn)so;
                                if (col.getRemarks() != null && col.getRemarks().length() > maxColumnCommentLength) {
                                    col.setRemarks(col.getRemarks().substring(maxColumnCommentLength));
                                }
                            }
                        }
                    }));
        }
        
        return criticisms;
    }
}
