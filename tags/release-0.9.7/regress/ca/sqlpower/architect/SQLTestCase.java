/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import ca.sqlpower.ArchitectTestCase;
import ca.sqlpower.architect.SQLIndex.IndexType;
import ca.sqlpower.architect.undo.UndoManager;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.MockJDBCDriver;

/**
 * SQLTestCase is an abstract base class for test cases that require a
 * database connection.
 */
public abstract class SQLTestCase extends ArchitectTestCase {

	/**
	 * This is the SQLDatabase object.  It will be set up according to
	 * some system properties in the <code>setup()</code> method.
	 *
	 * @see #setup()
	 */
	SQLDatabase db;
    private static DataSourceCollection plini = new PlDotIni();
    
    Set<String>propertiesToIgnoreForUndo = new HashSet<String>();
    Set<String>propertiesToIgnoreForEventGeneration = new HashSet<String>();

	public SQLTestCase(String name) throws Exception {
		super(name);
	}
	
	/**
	 * Looks up and returns an SPDataSource that represents the testing
	 * database. Uses a PL.INI file located in the current working directory, 
	 * called "pl.regression.ini" and creates a connection to the database called
	 * "regression_test".
	 * 
	 * <p>FIXME: Need to parameterise this so that we can test each supported
	 * database platform!
	 * @throws ArchitectException 
	 */
	static SPDataSource getDataSource() throws IOException, ArchitectException {
		plini.read(new File("pl.regression.ini"));
		return plini.getDataSource("regression_test");
	}
	
	/**
	 * Sets up the instance variable <code>db</code> using the getDatabase() method.
	 */
	protected void setUp() throws Exception {
		db = new SQLDatabase(new SPDataSource(getDataSource()));
        assertNotNull(db.getDataSource().getParentType());
	}
	
	protected void tearDown() throws Exception {
		db.disconnect();
		db = null;
	}
	
	protected abstract SQLObject getSQLObjectUnderTest() throws ArchitectException;
	
	public void testAllSettersGenerateEvents()
	throws IllegalArgumentException, IllegalAccessException, 
	InvocationTargetException, NoSuchMethodException, ArchitectException {
		
		SQLObject so = getSQLObjectUnderTest();
		so.populate();
		
        propertiesToIgnoreForEventGeneration.add("referenceCount");
		propertiesToIgnoreForEventGeneration.add("populated");
		propertiesToIgnoreForEventGeneration.add("SQLObjectListeners");
		propertiesToIgnoreForEventGeneration.add("children");
		propertiesToIgnoreForEventGeneration.add("parent");
		propertiesToIgnoreForEventGeneration.add("parentDatabase");
		propertiesToIgnoreForEventGeneration.add("class");
		propertiesToIgnoreForEventGeneration.add("childCount");
		propertiesToIgnoreForEventGeneration.add("undoEventListeners");
		propertiesToIgnoreForEventGeneration.add("connection");
		propertiesToIgnoreForEventGeneration.add("typeMap");
		propertiesToIgnoreForEventGeneration.add("secondaryChangeMode");	
		propertiesToIgnoreForEventGeneration.add("zoomInAction");
		propertiesToIgnoreForEventGeneration.add("zoomOutAction");
        propertiesToIgnoreForEventGeneration.add("magicEnabled");
		
		if (so instanceof SQLDatabase) {
			// should be handled in the Datasource
			propertiesToIgnoreForEventGeneration.add("name");
		}
		
		CountingSQLObjectListener listener = new CountingSQLObjectListener();
		so.addSQLObjectListener(listener);

		List<PropertyDescriptor> settableProperties;
		
		settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(so.getClass()));
		
		for (PropertyDescriptor property : settableProperties) {
			Object oldVal;
			if (propertiesToIgnoreForEventGeneration.contains(property.getName())) continue;
			
			try {
				oldVal = PropertyUtils.getSimpleProperty(so, property.getName());
				// check for a setter
				if (property.getWriteMethod() == null)
				{
					continue;
				}
				
			} catch (NoSuchMethodException e) {
				System.out.println("Skipping non-settable property "+property.getName()+" on "+so.getClass().getName());
				continue;
			}
			Object newVal;  // don't init here so compiler can warn if the following code doesn't always give it a value
			if (property.getPropertyType() == Integer.TYPE ||property.getPropertyType() == Integer.class ) {
				newVal = ((Integer)oldVal)+1;
			} else if (property.getPropertyType() == String.class) {
				// make sure it's unique
				newVal ="new " + oldVal;
				
			} else if (property.getPropertyType() == Boolean.TYPE){
				newVal = new Boolean(! ((Boolean) oldVal).booleanValue());
			} else if (property.getPropertyType() == SQLCatalog.class) {
				newVal = new SQLCatalog(new SQLDatabase(),"This is a new catalog");
			} else if (property.getPropertyType() == SPDataSource.class) {
				newVal = new SPDataSource();
				((SPDataSource)newVal).setName("test");
				((SPDataSource)newVal).setDisplayName("test");
				((SPDataSource)newVal).setUser("a");
				((SPDataSource)newVal).setPass("b");
				((SPDataSource)newVal).getParentType().setJdbcDriver(MockJDBCDriver.class.getName());
				((SPDataSource)newVal).setUrl("jdbc:mock:tables=tab1");
			} else if (property.getPropertyType() == SQLTable.class) {
				newVal = new SQLTable();
            } else if ( property.getPropertyType() == SQLColumn.class){
                newVal = new SQLColumn();
			} else if ( property.getPropertyType() == IndexType.class){
                newVal = IndexType.STATISTIC;
            } else if ( property.getPropertyType() == SQLIndex.class){
                newVal = new SQLIndex();
            } else {
				throw new RuntimeException("This test case lacks a value for "+
						property.getName()+
						" (type "+property.getPropertyType().getName()+") from "+so.getClass()+" on property"+property.getDisplayName());
			}
			
			int oldChangeCount = listener.getChangedCount();
			
			try {
				BeanUtils.copyProperty(so, property.getName(), newVal);
				
				// some setters fire multiple events (they change more than one property)
				assertTrue("Event for set "+property.getName()+" on "+so.getClass().getName()+" didn't fire!",
						listener.getChangedCount() > oldChangeCount);
				if (listener.getChangedCount() == oldChangeCount + 1) {
					assertEquals("Property name mismatch for "+property.getName()+ " in "+so.getClass(),
							property.getName(),
							listener.getLastEvent().getPropertyName());
					assertEquals("New value for "+property.getName()+" was wrong",
							newVal,
							listener.getLastEvent().getNewValue());
				}
			} catch (InvocationTargetException e) {
				System.out.println("(non-fatal) Failed to write property '"+property.getName()+" to type "+so.getClass().getName());
			}
		}
	}

	
	
	/**
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws ArchitectException 
	 */
	public void testAllSettersAreUndoable() 
	throws IllegalArgumentException, IllegalAccessException, 
	InvocationTargetException, NoSuchMethodException, ArchitectException {
		
		SQLObject so = getSQLObjectUnderTest();
        propertiesToIgnoreForUndo.add("referenceCount");
		propertiesToIgnoreForUndo.add("populated");
		propertiesToIgnoreForUndo.add("SQLObjectListeners");
		propertiesToIgnoreForUndo.add("children");
		propertiesToIgnoreForUndo.add("parent");
		propertiesToIgnoreForUndo.add("parentDatabase");
		propertiesToIgnoreForUndo.add("class");
		propertiesToIgnoreForUndo.add("childCount");
		propertiesToIgnoreForUndo.add("undoEventListeners");
		propertiesToIgnoreForUndo.add("connection");
		propertiesToIgnoreForUndo.add("typeMap");
		propertiesToIgnoreForUndo.add("secondaryChangeMode");		
		propertiesToIgnoreForUndo.add("zoomInAction");
		propertiesToIgnoreForUndo.add("zoomOutAction");
        propertiesToIgnoreForUndo.add("magicEnabled");
        propertiesToIgnoreForUndo.add("deleteRule");
        propertiesToIgnoreForUndo.add("updateRule");

		if(so instanceof SQLDatabase)
		{
			// should be handled in the Datasource
			propertiesToIgnoreForUndo.add("name");
		}
		UndoManager undoManager= new UndoManager(so);
		List<PropertyDescriptor> settableProperties;
		settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(so.getClass()));
		if(so instanceof SQLDatabase)
		{
			// should be handled in the Datasource
			settableProperties.remove("name");
		}
		for (PropertyDescriptor property : settableProperties) {
			Object oldVal;
			if (propertiesToIgnoreForUndo.contains(property.getName())) continue;
			
			try {
				oldVal = PropertyUtils.getSimpleProperty(so, property.getName());
				if (property.getWriteMethod() == null)
				{
					continue;
				}
			} catch (NoSuchMethodException e) {
				System.out.println("Skipping non-settable property "+property.getName()+" on "+so.getClass().getName());
				continue;
			}
			Object newVal;  // don't init here so compiler can warn if the following code doesn't always give it a value
			if (property.getPropertyType() == Integer.TYPE  || property.getPropertyType() == Integer.class) {
				newVal = ((Integer)oldVal)+1;
			} else if (property.getPropertyType() == String.class) {
				// make sure it's unique
				newVal ="new " + oldVal;
				
			} else if (property.getPropertyType() == Boolean.TYPE){
				newVal = new Boolean(! ((Boolean) oldVal).booleanValue());
			} else if (property.getPropertyType() == SQLCatalog.class) {
				newVal = new SQLCatalog(new SQLDatabase(),"This is a new catalog");
			} else if (property.getPropertyType() == SPDataSource.class) {
				newVal = new SPDataSource();
				((SPDataSource)newVal).setName("test");
				((SPDataSource)newVal).setDisplayName("test");
				((SPDataSource)newVal).setUser("a");
				((SPDataSource)newVal).setPass("b");
				((SPDataSource)newVal).getParentType().setJdbcDriver(MockJDBCDriver.class.getName());
				((SPDataSource)newVal).setUrl("jdbc:mock:tables=tab1,tab2");
			} else if (property.getPropertyType() == SQLTable.class) {
				newVal = new SQLTable();
            } else if (property.getPropertyType() == SQLColumn.class) {
                newVal = new SQLColumn();
            } else if (property.getPropertyType() == SQLIndex.class) {
                newVal = new SQLIndex();
			} else if ( property.getPropertyType() == IndexType.class){
                newVal = IndexType.STATISTIC;
    
            } else {
				throw new RuntimeException("This test case lacks a value for "+
						property.getName()+
						" (type "+property.getPropertyType().getName()+") from "+so.getClass());
			}
			
			int oldChangeCount = undoManager.getUndoableEditCount();
			
			try {
				BeanUtils.copyProperty(so, property.getName(), newVal);
				
				// some setters fire multiple events (they change more than one property)  but only register one as an undo
				assertEquals("Event for set "+property.getName()+" on "+so.getClass().getName() +
                        " added multiple ("+undoManager.printUndoVector()+") undos!",
						oldChangeCount+1,undoManager.getUndoableEditCount());
				
			} catch (InvocationTargetException e) {
				System.out.println("(non-fatal) Failed to write property '"+property.getName()+" to type "+so.getClass().getName());
			}
		}
	}
    
    /**
     * The child list should never be null for any SQL Object, even if
     * that object's type is childless.
     */
    public void testChildrenNotNull() throws ArchitectException {
        assertNotNull(getSQLObjectUnderTest().getChildren());
    }

    public SQLDatabase getDb() {
        return new SQLDatabase(new SPDataSource(db.getDataSource()));
    }

}