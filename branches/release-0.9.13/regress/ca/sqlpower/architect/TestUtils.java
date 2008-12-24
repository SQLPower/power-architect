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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;

import ca.sqlpower.testutil.NewValueMaker;

/**
 * Container class for utility methods useful to testing.
 */
public class TestUtils {

    /**
     * Sets all the settable properties on the given target object
     * which are not in the given ignore set.
     * 
     * @param target The object to change the properties of
     * @param propertiesToIgnore The properties of target not to modify or read
     * @return A Map describing the new values of all the non-ignored, readable 
     * properties in target.
     */
    public static Map<String,Object> setAllInterestingProperties(Object target,
    		Set<String> propertiesToIgnore) throws Exception {
    	
    	PropertyDescriptor props[] = PropertyUtils.getPropertyDescriptors(target);
    	for (int i = 0; i < props.length; i++) {
    		Object oldVal = null;
    		if (PropertyUtils.isReadable(target, props[i].getName()) &&
    				props[i].getReadMethod() != null &&
    				!propertiesToIgnore.contains(props[i].getName())) {
    			oldVal = PropertyUtils.getProperty(target, props[i].getName());
    		}
    		if (PropertyUtils.isWriteable(target, props[i].getName()) &&
    				props[i].getWriteMethod() != null &&
    				!propertiesToIgnore.contains(props[i].getName())) {
    		
    		    NewValueMaker valueMaker = new ArchitectValueMaker();
    		    Object newVal = valueMaker.makeNewValue(props[i].getPropertyType(), oldVal, props[i].getName());

    		    System.out.println("Changing property \""+props[i].getName()+"\" to \""+newVal+"\"");
                PropertyUtils.setProperty(target, props[i].getName(), newVal);

    		}
    	}
    	
    	// read them all back at the end in case there were dependencies between properties
    	return TestUtils.getAllInterestingProperties(target, propertiesToIgnore);
    }

    /**
     * Gets all the settable properties on the given target object
     * which are not in the given ignore set, and stuffs them into a Map.
     * 
     * @param target The object to change the properties of
     * @param propertiesToIgnore The properties of target not to modify or read
     * @return The aforementioned stuffed map
     */
    public static Map<String, Object> getAllInterestingProperties(Object target, Set<String> propertiesToIgnore) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	Map<String,Object> newDescription = new HashMap<String,Object>();
    	PropertyDescriptor[] props = PropertyUtils.getPropertyDescriptors(target);
    	for (int i = 0; i < props.length; i++) {
    		if (PropertyUtils.isReadable(target, props[i].getName()) &&
    				props[i].getReadMethod() != null &&
    				!propertiesToIgnore.contains(props[i].getName())) {
    			newDescription.put(props[i].getName(),
    					PropertyUtils.getProperty(target, props[i].getName()));
    		}
    	}
    	return newDescription;
    }

}
