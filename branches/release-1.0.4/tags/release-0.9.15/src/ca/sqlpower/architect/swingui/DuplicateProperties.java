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

package ca.sqlpower.architect.swingui;

import ca.sqlpower.sqlobject.SQLTable.TransferStyles;

/**
 * These properties distinguish the difference between
 * different ways of creating objects in a play pen.
 * These objects relate to actions including but not limited
 * to: copy and paste, cut and paste, and drag and drop.
 * The {@link ASUtils} class has the method for creating 
 * one of these property objects depending on current settings.
 */
public class DuplicateProperties {
    
    /**
     * If true then the source should be deleted on a move
     * or move-like option. Cut will of course delete the 
     * object if it is possible due to the definition of
     * its action.
     */
    private final boolean deleteSourceByDefault;
    
    /**
     * This is the method of transfer of objects by default. This
     * can be any style allowed by the SQLObjects.
     */
    private TransferStyles defaultTransferStyle;
    
    /**
     * If true the object can be reverse engineered even if 
     * it is not the default option. Reverse engineering
     * is not allowed in certain places due to it being
     * ridiculous to implement or not making logical sense.
     */
    private final boolean canReverseEngineer;
    
    /**
     * If true the object can be copied even if it is not
     * the default option. Copying is not allowed in certain
     * places when reverse engineering should be done.
     */
    private final boolean canCopy;

    /**
     * This tracks if the column source of the objects being duplicated
     * should keep the same source as the object's source being copied from.
     * Sometimes when copying an object the source cannot be preserved
     * even if the source object has a source and the root source will
     * have to be removed. The source from reverse engineering is always
     * the column that is being copied.
     * <p>
     * NOTE: This property only matters if you are copying or doing a 
     * copy-like operation.
     */
    private final boolean preserveColumnSource;
    
    public DuplicateProperties(boolean canReverseEngineer, TransferStyles defaultTransferStyle, boolean deleteSourceByDefault, boolean preserveColumnSource, boolean canCopy) {
        this.canReverseEngineer = canReverseEngineer;
        this.canCopy = canCopy;
        this.setDefaultTransferStyle(defaultTransferStyle);
        this.deleteSourceByDefault = deleteSourceByDefault;
        this.preserveColumnSource = preserveColumnSource;
    }
    
    public boolean isDeleteSourceByDefault() {
        return deleteSourceByDefault;
    }

    public TransferStyles getDefaultTransferStyle() {
        return defaultTransferStyle;
    }

    public boolean isCanReverseEngineer() {
        return canReverseEngineer;
    }

    public boolean isPreserveColumnSource() {
        return preserveColumnSource;
    }

    public void setDefaultTransferStyle(TransferStyles defaultTransferStyle) {
        this.defaultTransferStyle = defaultTransferStyle;
    }

    public boolean isCanCopy() {
        return canCopy;
    }
}
