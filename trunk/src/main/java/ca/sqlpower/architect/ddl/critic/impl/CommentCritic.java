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

package ca.sqlpower.architect.ddl.critic.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ddl.critic.CriticAndSettings;
import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.architect.ddl.critic.CriticFix;
import ca.sqlpower.architect.ddl.critic.CriticFix.FixType;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLTable;

public class CommentCritic extends CriticAndSettings {

    private final int maxTableCommentLength;
    private final int maxColumnCommentLength;
    private final String platformName;

    @Constructor
    public CommentCritic(
            @ConstructorParameter(propertyName="platformName") String platformName, 
            @ConstructorParameter(propertyName="maxTableCommentLength") int maxLengthTable, 
            @ConstructorParameter(propertyName="maxColumnCommentLength")int maxLengthColumn) {
        super(platformName, Messages.getString("CommentCritic.name"));
        this.platformName = platformName;
        this.maxColumnCommentLength = maxLengthColumn;
        this.maxTableCommentLength = maxLengthTable;
    }
    
    public List<Criticism> criticize(final Object so) {
        
        if (!(so instanceof SQLTable || so instanceof SQLColumn)) return Collections.emptyList();

        String remarks = null;
        int maxLength = 0;

        if (so instanceof SQLTable) {
            remarks = ((SQLTable)so).getRemarks();
            maxLength = getMaxTableCommentLength();
        } else if (so instanceof SQLColumn) {
            remarks = ((SQLColumn)so).getRemarks();
            maxLength = getMaxColumnCommentLength();
        }
        
        List<Criticism> criticisms = new ArrayList<Criticism>();
        if (remarks != null && remarks.length() > maxLength && maxLength > 0) {
            criticisms.add(new Criticism(
                    so,
                    "Comment too long for " + getPlatformName(),
                    this,
                    new CriticFix("Truncate comment to " + maxLength + " characters", FixType.QUICK_FIX) {
                        public void apply() {
                            if (so instanceof SQLTable) {
                                SQLTable tbl = (SQLTable)so;
                                if (tbl.getRemarks() != null && tbl.getRemarks().length() > getMaxTableCommentLength()) {
                                    tbl.setRemarks(tbl.getRemarks().substring(getMaxTableCommentLength()));
                                }
                            } else if (so instanceof SQLColumn) {
                                SQLColumn col = (SQLColumn)so;
                                if (col.getRemarks() != null && col.getRemarks().length() > getMaxColumnCommentLength()) {
                                    col.setRemarks(col.getRemarks().substring(getMaxColumnCommentLength()));
                                }
                            }
                        }
                    }));
        }
        
        return criticisms;
    }

    @Accessor
    public int getMaxTableCommentLength() {
        return maxTableCommentLength;
    }

    @Accessor
    public int getMaxColumnCommentLength() {
        return maxColumnCommentLength;
    }

    @Accessor
    public String getPlatformName() {
        return platformName;
    }
    
}
