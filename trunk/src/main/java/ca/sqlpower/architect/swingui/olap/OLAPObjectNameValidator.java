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

package ca.sqlpower.architect.swingui.olap;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

/**
 * A simple validator for OLAPObjects that checks for invalid names, relies on
 * {@link OLAPObject#getName()} for name comparisons.
 * 
 */
public class OLAPObjectNameValidator implements Validator {
    
    private final OLAPObject parent;
    private final OLAPObject obj;
    
    /**
     * Indicates whether name can be null. If true, empty string will also be
     * treated as null.
     */
    private final boolean allowNull;
    
    public OLAPObjectNameValidator(OLAPObject parent, OLAPObject obj, boolean allowNull) {
        this.parent = parent;
        this.obj = obj;
        this.allowNull = allowNull;
    }

    public ValidateResult validate(Object contents) {
        String value = (String) contents;

        if (value == null || value.length() == 0) {
            if (allowNull) {
                if (obj.getName() != null && !OLAPUtil.isNameUnique(parent, obj.getClass(), null)) {
                    return ValidateResult.createValidateResult(Status.FAIL, "Name already exists.");
                }
            } else {
                return ValidateResult.createValidateResult(Status.FAIL, "A name is required.");
            }
        } else if (!value.equalsIgnoreCase(obj.getName()) && !OLAPUtil.isNameUnique(parent, obj.getClass(), value)) {
            return ValidateResult.createValidateResult(Status.FAIL, "Name already exists.");
        }

        return ValidateResult.createValidateResult(Status.OK, "");
    }

}
