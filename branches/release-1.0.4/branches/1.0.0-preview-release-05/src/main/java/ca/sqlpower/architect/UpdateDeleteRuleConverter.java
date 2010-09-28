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

import org.apache.commons.beanutils.Converter;

import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;

/**
 * Converts between String and {@link UpdateDeleteRule} values. Supports
 * converting an Integer code value (such as 3), a String representation of an
 * integer code value (such as "3"), or a string representation of a
 * UpdateDeleteRule constant name (such as "CASCADE").
 */
public class UpdateDeleteRuleConverter implements Converter {

    public Object convert(@SuppressWarnings("unchecked") Class targetType, Object value) {
        if (value == null) {
            return null;
        }
        
        if (targetType == UpdateDeleteRule.class) {
            if ( ! (value instanceof Integer)) {
                try {
                    value = Integer.valueOf(value.toString());
                } catch (NumberFormatException ex) {
                    return UpdateDeleteRule.valueOf(value.toString());
                }
            }
            return UpdateDeleteRule.ruleForCode(((Integer) value).intValue());
        } else {
            throw new IllegalArgumentException("Cannot perform conversion to target type " + targetType);
        }
    }
    
}
