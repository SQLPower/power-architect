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
import java.util.regex.Pattern;

import ca.sqlpower.architect.ddl.critic.Critic;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings;
import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.architect.ddl.critic.CriticFix;
import ca.sqlpower.architect.ddl.critic.CriticFix.FixType;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLIndex.Column;
import ca.sqlpower.sqlobject.SQLRelationship.ColumnMapping;

/**
 * Criticizes the physical name of all SQLObjects based on the parameters given
 * to the constructor.
 */
public class PhysicalNameCritic extends CriticAndSettings {
    
    private final Pattern legalNamePattern;
    private final int maxNameLength;
    private final String platformName;

    /**
     * If the {@link SQLObject}s criticized do not match the pattern or is
     * longer than the given max length criticisms will be created to warn the
     * user.
     * 
     * @param platformName
     *            The name of the platform we are criticizing. See
     *            {@link Critic.StarterPlatformTypes} for examples.
     * @param legalNamePattern
     *            A {@link Pattern} that the names must match to prevent errors.
     * @param maxNameLength
     *            Names longer than this value in character count will be marked
     *            as an error.
     */
    @Constructor
    public PhysicalNameCritic(
            @ConstructorParameter(propertyName="platformName") String platformName, 
            @ConstructorParameter(propertyName="legalNamePattern") Pattern legalNamePattern, 
            @ConstructorParameter(propertyName="maxNameLength") int maxNameLength) {
        super(platformName, Messages.getString("PhysicalNameCritic.name"));
        this.platformName = platformName;
        this.legalNamePattern = legalNamePattern;
        this.maxNameLength = maxNameLength;
        
    }
    
    public List<Criticism> criticize(final Object subject) {
        if (!(subject instanceof SQLObject)) return Collections.emptyList();
        
        //Column mappings and the columns in SQLIndex do not get written in
        //a DDL statement so we can ignore criticizing them.
        if (subject instanceof ColumnMapping || subject instanceof Column) return Collections.emptyList();
        
        final SQLObject so = (SQLObject) subject;
        final String physName = so.getPhysicalName();

        List<Criticism> criticisms = new ArrayList<Criticism>();
        
        if (physName == null || physName.trim().length() == 0){
            criticisms.add(new Criticism(
                    so,
                    "No physical name for " + so.getName(),
                    this,
                    new CriticFix("Copy logical name to physical name", FixType.QUICK_FIX) {
                        public void apply() {
                            so.setPhysicalName(so.getName());
                        }
                    }));
			return criticisms;
		}

        if (physName.length() > getMaxNameLength()) {
            criticisms.add(new Criticism(
                    so,
                    "Physical name too long for " + getPlatformName(),
                    this,
                    new CriticFix("Truncate name to " + so.getPhysicalName().substring(0, getMaxNameLength()), FixType.QUICK_FIX) {
                        public void apply() {
                            if (so.getPhysicalName() != null && so.getPhysicalName().length() > getMaxNameLength()) {
                                so.setPhysicalName(so.getPhysicalName().substring(0, getMaxNameLength()));
                            }
                        }
                    }));
        }
		
        if (!getLegalNamePattern().matcher(physName).matches()) {
            final String newLogicalName = correctPhysicalName(so, physName);
            criticisms.add(new Criticism(
                    so,
                    "Physical name not legal for " + so.getPhysicalName(),
                    this,
                    new CriticFix("Replace the physical name with " + newLogicalName, FixType.QUICK_FIX) {
                        @Override
                        public void apply() {
                            so.setPhysicalName(newLogicalName);
                        }
                    }
                    ));
        }
        
        return criticisms;
    }

    /**
     * This method will be given the subject object being criticized and its
     * physical name that does not match the pattern for valid physical names and it
     * will return a valid physical name for the object that passes the legal
     * name pattern.
     */
    public String correctPhysicalName(Object subject, String existingName) {
        StringBuffer buffer = new StringBuffer(existingName.length());
        for (int i = 0; i < existingName.length(); i++) {
            if (existingName.charAt(i) == ' ') {
                buffer.append('_');
            } else if (getLegalNamePattern().matcher(Character.toString(existingName.charAt(i))).matches()) {
                buffer.append(existingName.charAt(i));
            } else if (i == 0) {
                if (subject instanceof SQLTable) {
                    buffer.append("Table_" + existingName.charAt(i));
                } else if (subject instanceof SQLColumn) {
                    buffer.append("Column_" + existingName.charAt(i));
                } else if (subject instanceof SQLIndex) {
                    buffer.append("Index_" + existingName.charAt(i));
                } else {
                    buffer.append("X_" + existingName.charAt(i));
                }
            }
        }
        return buffer.toString();
    }

    @Accessor
    public Pattern getLegalNamePattern() {
        return legalNamePattern;
    }

    @Accessor
    public int getMaxNameLength() {
        return maxNameLength;
    }

    @Accessor
    public String getPlatformName() {
        return platformName;
    }
    
}
