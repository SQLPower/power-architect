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
 * A simple validator for OLAPObjects that checks for empty and unique names.
 *
 */
public class OLAPObjectNameValidator implements Validator {
    
    private final OLAPObject oo;
    
    /**
     * Indicates whether to check if the name is empty or null; true to check,
     * false for otherwise.
     */
    private final boolean checkEmptyName;
    
    public OLAPObjectNameValidator(OLAPObject oo, boolean checkEmptyName) {
        this.oo = oo;
        this.checkEmptyName = checkEmptyName;
    }

    public ValidateResult validate(Object contents) {
        String value = (String) contents;
        if (checkEmptyName && (value == null || value.trim().length() == 0)) {
            return ValidateResult.createValidateResult(Status.FAIL, 
                    "A name is required.");
        } else if (OLAPUtil.isNameUnique(oo, value)) {
            return ValidateResult.createValidateResult(Status.OK, "");
        } else {
            return ValidateResult.createValidateResult(Status.FAIL, "Name already exists.");
        }
    }

}
