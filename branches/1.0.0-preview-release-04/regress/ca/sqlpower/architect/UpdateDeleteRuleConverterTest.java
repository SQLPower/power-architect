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

import junit.framework.TestCase;

import org.apache.commons.beanutils.Converter;

import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;

/**
 * Test class for the BeanUtils converter for the updateRule/DeleteRule
 * type converter.
 */
public class UpdateDeleteRuleConverterTest extends TestCase {

    Converter converter;
    
    @Override
    protected void setUp() throws Exception {
        converter = new UpdateDeleteRuleConverter();
    }
    
    public void testFromInt() throws Exception {
        Object r = converter.convert(UpdateDeleteRule.class, new Integer(3));
        assertSame(UpdateDeleteRule.ruleForCode(3), r);
    }

    public void testFromIntString() throws Exception {
        Object r = converter.convert(UpdateDeleteRule.class, "3");
        assertSame(UpdateDeleteRule.ruleForCode(3), r);
    }

    public void testFromName() throws Exception {
        Object r = converter.convert(UpdateDeleteRule.class, "NO_ACTION");
        assertSame(UpdateDeleteRule.NO_ACTION, r);
    }
}
