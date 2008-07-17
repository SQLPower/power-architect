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
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;

import ca.sqlpower.architect.etl.kettle.KettleRepositoryDirectoryChooser;
import ca.sqlpower.architect.etl.kettle.RootRepositoryDirectoryChooser;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;

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
    			
    			// XXX: factor this (and the same thing in SQLTestCase) 
    			//      out into a changeValue() method in some util class.
    			
    			Object newVal;  // don't init here so compiler can warn if the following code doesn't always give it a value
    			if (props[i].getPropertyType() == Integer.TYPE) {
    				newVal = ((Integer)oldVal)+1;
    			} else if (props[i].getPropertyType() == Integer.class) {
    				if (oldVal == null) {
    					newVal = new Integer(1);
    				} else {
    					newVal = new Integer((Integer)oldVal+1);
    				}
    			} else if (props[i].getPropertyType() == String.class) {
    				// make sure it's unique
    				newVal ="new " + oldVal;
    			} else if (props[i].getPropertyType() == Boolean.TYPE){
    				newVal = new Boolean(! ((Boolean) oldVal).booleanValue());
    			} else if (props[i].getPropertyType() == SQLColumn.class) {
    				newVal = new SQLColumn();
    				((SQLColumn) newVal).setName("testing!");
                } else if (props[i].getPropertyType() == SQLIndex.class) {
                    newVal = new SQLIndex();
                    ((SQLIndex) newVal).setName("a new index");
                } else if (props[i].getPropertyType() == File.class) {
                    newVal = new File("temp" + System.currentTimeMillis());
                } else if (props[i].getPropertyType() == UserPrompter.class) {
                    newVal = new AlwaysOKUserPrompter();
                } else if (props[i].getPropertyType() == KettleRepositoryDirectoryChooser.class) {
                    newVal = new RootRepositoryDirectoryChooser();
                } else if (props[i].getPropertyType() == SPDataSource.class) {
                    newVal = new SPDataSource(new PlDotIni());
                    ((SPDataSource)newVal).setName("Testing data source");
    			} else {
    				throw new RuntimeException("This test case lacks a value for "+
    						props[i].getName()+
    						" (type "+props[i].getPropertyType().getName()+")");
    			}
    
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
