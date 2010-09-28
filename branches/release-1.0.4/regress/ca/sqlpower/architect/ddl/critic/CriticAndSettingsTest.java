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

package ca.sqlpower.architect.ddl.critic;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import ca.sqlpower.architect.util.ArchitectNewValueMaker;
import ca.sqlpower.object.PersistedSPObjectTest;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.NewValueMaker;

public class CriticAndSettingsTest extends PersistedSPObjectTest {

    private SPObject critic;

    public CriticAndSettingsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        critic = (SPObject) createNewValueMaker(getRootObject(), getPLIni()).
            makeNewValue(CriticAndSettings.class, null, "Object under test");
    }
    
    @Override
    public SPObject getSPObjectUnderTest() {
        return critic;
    }

    @Override
    public NewValueMaker createNewValueMaker(SPObject root, 
            DataSourceCollection<SPDataSource> dsCollection) {
        return new ArchitectNewValueMaker(root, dsCollection);
    }

    @Override
    protected Class<? extends SPObject> getChildClassType() {
        return null;
    }
    
    /**
     * A different implementation of the allowed child types since this is an abstract class.
     */
    @Override
    public void testAllowedChildTypesField() throws Exception {
        Class<? extends SPObject> classUnderTest = CriticAndSettings.class;
        Field childOrderField;
        try {
            childOrderField = classUnderTest.getDeclaredField("allowedChildTypes");
        } catch (NoSuchFieldException ex) {
            fail("Persistent " + classUnderTest + " must have a static final field called allowedChildTypes");
            throw new AssertionError(); // NOTREACHED
        }
        
        assertEquals("The allowedChildTypes field must be final",
                true, Modifier.isFinal(childOrderField.getModifiers()));

        assertEquals("The allowedChildTypes field must be static",
                true, Modifier.isStatic(childOrderField.getModifiers()));

        // Note: in the future, we will change this to require that the field is private
        assertEquals("The allowedChildTypes field must be public",
                true, Modifier.isPublic(childOrderField.getModifiers()));
        
        List<Class<? extends SPObject>> allowedChildTypes =
            (List<Class<? extends SPObject>>) childOrderField.get(null);
        assertTrue(allowedChildTypes.isEmpty());
    }
}
