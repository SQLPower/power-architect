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
package ca.sqlpower.architect.profile;

import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;

/**
 * These are the profile manager's settings.
 * <p>
 * We may want to store this in prefs in the future as we would want to store
 * the settings in a per-user type of fashion similar to compareDM and ddl
 * settings.
 */
public class ProfileSettings extends AbstractSPObject {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static List<Class<? extends SPObject>> allowedChildTypes = Collections.emptyList();
    
    private boolean findingMin = true;

    private boolean findingMax = true;

    private boolean findingAvg = true;

    private boolean findingMinLength = true;

    private boolean findingMaxLength = true;

    private boolean findingAvgLength = true;

    private boolean findingDistinctCount = true;

    private boolean findingNullCount = true;

    private boolean findingTopTen = true;

    private int topNCount = 10;

    public ProfileSettings() {
        setName("Profile Settings");
    }
    
    @Accessor
    public boolean isFindingAvg() {
        return findingAvg;
    }

    @Mutator
    public void setFindingAvg(boolean findingAvg) {
        boolean oldAvg = this.findingAvg;
        this.findingAvg = findingAvg;
        firePropertyChange("findingAvg", oldAvg, findingAvg);
    }

    @Accessor
    public boolean isFindingAvgLength() {
        return findingAvgLength;
    }

    @Mutator
    public void setFindingAvgLength(boolean findingAvgLength) {
        boolean oldAvg = this.findingAvgLength;
        this.findingAvgLength = findingAvgLength;
        firePropertyChange("findingAvgLength", oldAvg, findingAvgLength);
    }

    @Accessor
    public boolean isFindingDistinctCount() {
        return findingDistinctCount;
    }

    @Mutator
    public void setFindingDistinctCount(boolean findingDistinctCount) {
        boolean oldCount = this.findingDistinctCount;
        this.findingDistinctCount = findingDistinctCount;
        firePropertyChange("findingDistinctCount", oldCount, findingDistinctCount);
    }

    @Accessor
    public boolean isFindingMax() {
        return findingMax;
    }

    @Mutator
    public void setFindingMax(boolean findingMax) {
        boolean oldMax = this.findingMax;
        this.findingMax = findingMax;
        firePropertyChange("findingMax", oldMax, findingMax);
    }

    @Accessor
    public boolean isFindingMaxLength() {
        return findingMaxLength;
    }

    @Mutator
    public void setFindingMaxLength(boolean findingMaxLength) {
        boolean oldMax = this.findingMaxLength;
        this.findingMaxLength = findingMaxLength;
        firePropertyChange("findingMaxLength", oldMax, findingMaxLength);
    }

    @Accessor
    public boolean isFindingMin() {
        return findingMin;
    }

    @Mutator
    public void setFindingMin(boolean findingMin) {
        boolean oldMin = this.findingMin;
        this.findingMin = findingMin;
        firePropertyChange("findingMin", oldMin, findingMin);
    }

    @Accessor
    public boolean isFindingMinLength() {
        return findingMinLength;
    }

    @Mutator
    public void setFindingMinLength(boolean findingMinLength) {
        boolean oldMin = this.findingMinLength;
        this.findingMinLength = findingMinLength;
        firePropertyChange("findingMinLenth", oldMin, findingMinLength);
    }

    @Accessor
    public boolean isFindingNullCount() {
        return findingNullCount;
    }

    @Mutator
    public void setFindingNullCount(boolean findingNullCount) {
        boolean oldCount = this.findingNullCount;
        this.findingNullCount = findingNullCount;
        firePropertyChange("findingNullCount", oldCount, findingNullCount);
    }

    @Accessor
    public boolean isFindingTopTen() {
        return findingTopTen;
    }

    @Mutator
    public void setFindingTopTen(boolean findingTopTen) {
        boolean oldTopTen = this.findingTopTen;
        this.findingTopTen = findingTopTen;
        firePropertyChange("findingTopTen", oldTopTen, findingTopTen);
    }

    @Accessor
    public int getTopNCount() {
        return topNCount;
    }

    @Mutator
    public void setTopNCount(int topNCount) {
        int oldCount = this.topNCount;
        this.topNCount = topNCount;
        firePropertyChange("topNCount", oldCount, topNCount);
    }
    
    @Transient @Mutator
    public void setTopNCount(String topNCount) {
        setTopNCount(Integer.valueOf(topNCount));
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }

    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        return 0;
    }

    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return Collections.emptyList();
    }

    @NonProperty
    public List<? extends SPObject> getChildren() {
        return Collections.emptyList();
    }

    @NonBound
    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        //no-op
    }
}
