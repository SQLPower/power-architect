/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.ddl.critic.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings;
import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.architect.ddl.critic.CriticFix;
import ca.sqlpower.architect.ddl.critic.CriticFix.FixType;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * This critic will create a warning for objects that have the same name if they
 * are of conflicting types. This means some objects, like columns in different
 * tables, can have the same name but a sequence cannot have the same name as a
 * table because they are at the same level in a database.
 */
public class DuplicateNameCritic extends CriticAndSettings {

    /**
     * Stores all of the top level target database objects by name for the
     * current run of the critics. The definition of top level is any object
     * that is in the target database that would be at the same level in a
     * database when written out to a DDL script. This includes tables,
     * relationships, indices, and sequences.
     * <p>
     * Note: sequences are represented by columns in this map as there is no
     * sequence object.
     */
    private Multimap<String, SQLObject> topLevelPhysicalNameMap = ArrayListMultimap.create();

    /**
     * Maps each parent table to the columns in the table that we have
     * critiqued. This way we can find if there are columns with matching column
     * names.
     */
    private Multimap<SQLTable, SQLColumn> columnPhysicalNameMap = ArrayListMultimap.create();
    
    public DuplicateNameCritic() {
        super(StarterPlatformTypes.GENERIC.getName(), Messages.getString("DuplicateNameCritic.name"));
    }

    @Override
    public void start() {
        super.start();
        topLevelPhysicalNameMap.clear();
        columnPhysicalNameMap.clear();
    }
    
    @Override
    public void end() {
        super.end();
        topLevelPhysicalNameMap.clear();
        columnPhysicalNameMap.clear();
    }
    
    public List<Criticism> criticize(Object subject) {
        if (!(subject instanceof SQLObject)) return Collections.emptyList();

        List<Criticism> criticisms = new ArrayList<Criticism>();
        if (subject instanceof SQLColumn) {
            final SQLColumn col = (SQLColumn) subject;
            SQLTable parent = col.getParent();
            if (col.getPhysicalName() == null) return criticisms;

            int count = 0;
            for (SQLColumn otherCol : columnPhysicalNameMap.get(parent)) {
                if (col.getPhysicalName().equals(otherCol.getPhysicalName())) {
                    count++;
                }
            }
            if (count > 0) {
                final String newPhysicalName = col.getPhysicalName() + "_" + count;
                criticisms.add(new Criticism(subject, 
                        "Duplicate physical name \"" + col.getPhysicalName() + "\"", this, 
                        new CriticFix("Replace physical name " + col.getPhysicalName() + " with " + newPhysicalName, 
                                FixType.QUICK_FIX) {
                            @Override
                            public void apply() {
                                col.setPhysicalName(newPhysicalName);
                            }
                        }));
            }
            columnPhysicalNameMap.put(parent, col);
        }
        if (subject instanceof SQLTable || subject instanceof SQLRelationship || 
                subject instanceof SQLIndex || subject instanceof SQLColumn) {
            final SQLObject obj = (SQLObject) subject;
            String physicalName = obj.getPhysicalName();
            if (obj instanceof SQLColumn) {
                physicalName = ((SQLColumn) obj).getAutoIncrementSequenceName();
            }
            final Collection<SQLObject> sameNameObjects = topLevelPhysicalNameMap.get(physicalName);
            if (!sameNameObjects.isEmpty()) {
                final String newPhysicalName = physicalName + "_" + sameNameObjects.size();
                SQLObject duplicate = sameNameObjects.iterator().next();
                criticisms.add(new Criticism(subject, 
                        "Duplicate physical name \"" + physicalName + 
                            "\". There is a " + ArchitectUtils.convertClassToString(duplicate.getClass())+ " in " + 
                            duplicate.getParent().getName() + " with this name already.", this, 
                        new CriticFix("Replace physical name " + obj.getPhysicalName() + " with " + newPhysicalName, 
                                FixType.QUICK_FIX) {
                            @Override
                            public void apply() {
                                if (obj instanceof SQLColumn) {
                                    ((SQLColumn) obj).setAutoIncrementSequenceName(newPhysicalName);
                                } else {
                                    obj.setPhysicalName(newPhysicalName);
                                }
                            }
                }));
            }
            topLevelPhysicalNameMap.put(physicalName, obj);
        }
        return criticisms;
    }

}
