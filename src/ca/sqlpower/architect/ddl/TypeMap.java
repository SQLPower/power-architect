/*
 * Created on Sep 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect.ddl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;

/**
 * @author jack
 *
 * TypeMap is a singleton which is used by SQLColumn to fix known
 * weird metadata that comes from JDBC drivers.  For example, database
 * with unlimited character fields report the precision as -1, which 
 * totally messes up forward engineering.
 * 
 * In the root directory of the Architect, there is a file called mappingrules.properties, 
 * which is a UNIX style whitespace delimited file that contains mapping
 * rules for these strange situations.  Rules are processed in the order 
 * in which they are found, so if two rules effect the same metadata
 * field, then the second one will win.
 * 
 * TODO: Eventually, these mapping rules should most likely be added to the 
 * User Settings to allow users to add their own custom rules.  
 * 
 * XXX: Once users are allowed to make their own rules, there may be 
 * some concurrency issues with the singleton class (i.e. concurrent 
 * modification exceptions on the Iterators which are used to grab
 * lists of rules for particular database/nativeType).  There is also some
 * question about how thread safe pattern.matcher() is.
 * 
 */
public class TypeMap {
    private static final Logger logger = Logger.getLogger(TypeMap.class);
   	TreeMap databases;
   	ArrayList EMPTY_LIST = new ArrayList(); 
   	Pattern SPACE_STRIPPER = Pattern.compile("[\\s]+");
   	
   	// singleton instance
   	protected static TypeMap mainInstance = new TypeMap();

	public static void main(String[] args) {
		TypeMap tm = TypeMap.getInstance();		
	}
	
	/*
	 * Warning: this code globally effects the behaviour of BeanUtils, 
	 * which the XML Digester relies heavily upon.  The TypeMap singleton
	 * is created right at the start of the app to ensure consistent
	 * behaviour.
	 */
	static {
		// No-args constructor gets the version that throws exceptions
		Converter myConverter = new IntegerConverter();
		ConvertUtils.register(myConverter, Integer.TYPE);    // Native type
		ConvertUtils.register(myConverter, Integer.class);   // Wrapper class		
	}
	
	/**
	 * @return singleton instance
	 */
	public static TypeMap getInstance() {
		return mainInstance;
	}
	
	/**
     * populate the rules from mappingrules.properties
	 */
	protected TypeMap() {
		// make root node
		databases = new TreeMap();
		//XXX add header.
		// look for 7 groups on non-whitespace seperated by whitespace
		Pattern p = Pattern.compile("^([\\S]+)[\\s]+([\\S]+)[\\s]+([\\S]+)[\\s]+([\\S]+)[\\s]+([\\S]+)[\\s]+([\\S]+)[\\s]+([\\S]+)[\\s]*");
				
		try {
			String line = null;
			BufferedReader br = new BufferedReader(new FileReader("mappingrules.properties"));
			while ((line = br.readLine()) != null) {
				if (line.trim().length() == 0) {
					continue;
				}
				if (line.trim().length() > 0 && line.trim().substring(0,1).equals("#")) {
					continue;
				}
				Matcher m = p.matcher(line);
				m.find();
				if (m.groupCount() != 7) {
					logger.error("badly formatted line in mappingrules.properties:\n"+line);
				} else {
					
					MappingRule rule = new MappingRule();
					
					rule.setDatabase(m.group(1));
					rule.setNativeType(m.group(2));
					rule.setCompField(m.group(3));
					rule.setCompCondition(m.group(4));
					rule.setCompValue(m.group(5));
					rule.setModifyField(m.group(6));
					rule.setModifyValue(m.group(7));
					
					if (logger.isDebugEnabled()) {
						logger.debug("adding rule: " + rule);
					}
					
					addRule(rule);
										
				}
			}
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * calls to getRulesForNative type create new database and 
	 * native type entries on an as needed basis (it's called
	 * with createNew = true).
	 * 
	 * @param rule
	 * @return
	 */
	public boolean addRule (MappingRule rule) {
		return getRulesForNativeType(rule,true).add(rule);	
	}
	
	/**
	 * remove a mapping rule from the rules collection
	 * 
	 * @param rule
	 * @return true if we removed something, false if we didn't find it
	 */
	public boolean removeRule (MappingRule rule) {
		return getRulesForNativeType(rule,false).remove(rule);	
	}

	/**
	 * Get a list of rules for a native type.
	 * 
	 * @return arraylist of rules 
	 */
	protected List getRulesForNativeType(MappingRule rule, boolean createNew) {
		return getRulesForNativeType(rule.getDatabase(),
				                     rule.getNativeType(),
									 createNew);
	}
	
	/**
	 * 
	 * only create new database and native type entries when createNew is true.
	 * this prevents bogus map entries from being created when doing lookups.
	 *
	 * notice that a SQLColumn is not passed in, so the rules list is not
	 * cut down to size.  
	 * 
	 * @see getRules for the method which determines which rules are in effect
	 *  
	 * @param database
	 * @param nativeType
	 * @param createNew
	 * @return
	 */
	protected List getRulesForNativeType(String database, String nativeType, boolean createNew) {
		TreeMap nativeTypes = null;
		String tDatabase = translateDatabaseName(database);
		if (!databases.containsKey(tDatabase)) {
			if (createNew) {
				nativeTypes = new TreeMap();
				databases.put(tDatabase,nativeTypes);
			} else {
				return EMPTY_LIST; // empty list
			}
		} else {
			nativeTypes = (TreeMap) databases.get(tDatabase);
		}
		
		ArrayList mappingRules = null;
		if (!nativeTypes.containsKey(nativeType)) {
			if (createNew) {
				mappingRules = new ArrayList();
				nativeTypes.put(nativeType,mappingRules);
			} else {
				return EMPTY_LIST; // empty list
			}
		} else {
			mappingRules = (ArrayList) nativeTypes.get(nativeType);
		}
		return mappingRules;
	}

	/**
	 * Get the set of rules that apply for this SQLColumn.  Grab the whole list
	 * that applies for this database/nativeType and based on the contents of
	 * SQLColumn, return a subset of the ones that apply.
	 * 
	 * The rules are not processed here.  You can't process rules as you find
	 * them because you'll get side effects as you try to apply other rules.
	 * 
	 * @see applyRules for the code which actually processes the rules.   
	 * 
	 * @param col
	 * @return
	 */
	protected List getRules (SQLColumn col) {
		SQLDatabase database = col.getParentTable().getParentDatabase();
		List mappingRules = null; 
		List applicableRules = new ArrayList();
		if (database != null) {
			mappingRules = getRulesForNativeType(database.getDataSource().getPlDbType(),col.getSourceDataTypeName(),false);
		}
		if (mappingRules.size() > 0) {
			Iterator it = mappingRules.iterator();
			while (it.hasNext()) {
				MappingRule rule = (MappingRule) it.next();
				// check if this is an "any" rule
				if (rule.getCompCondition().equals("*")) {		
					applicableRules.add(rule);
					continue;
				}

				try {
					// see if the property exists in SQLColumn
					String propertyVal = BeanUtils.getProperty(col,rule.getCompField());
					
					// if we can cast things to integers, do an integer comparison, else, do
					// a string comparison...
					try {
						Integer iPropertyVal = new Integer(propertyVal);
						Integer iCompValue = new Integer(rule.getCompValue());
						if (satisfiesComparison(iPropertyVal,iCompValue,rule.getCompCondition())) {
							applicableRules.add(rule);
						}
					} catch (NumberFormatException nfe) {
						logger.debug("numeric conversion failed, reverting to lexical comparison");
						if (satisfiesComparison(propertyVal,rule.getCompValue(),rule.getCompCondition())) {
							applicableRules.add(rule);
						}
					}									
				} catch (NoSuchMethodException nsme) {
					logger.error("mappingrules.properties references an non-existent column from SQLColumn: " + rule.getCompField());
				} catch (IllegalAccessException iae) {
					logger.error("SQLColumn getter was not public: " + rule.getCompField());					
				} catch (InvocationTargetException ite) {					
					logger.error("SQLColumn getter threw an exception: " + rule.getCompField());					
				}
			}
		}
		return applicableRules;
	}
	
	/**
	 * convenience method for checking if a rule criteria is satisifed
	 * 
	 * @return boolean to tell if the rule is satisfied.  Works equally well with Strings 
	 * and Integer.
	 */
	protected boolean satisfiesComparison(Comparable c1, Comparable c2, String operator) {
		if (c1.compareTo(c2) == 0 && operator.equals("=") || 
			c1.compareTo(c2) > 0 && operator.equals(">") ||
			c1.compareTo(c2) < 0 && operator.equals("<")) {
				return true;
		}
		return false;
	}
	/**
	 * applicable rules are applied in the order in which they are found.
	 * 
	 * The applicability of a rule is based on the original state of the SQLColumn, not
	 * the transitional state as rules are applied.
	 * 
	 * @param col
	 * @return tell the caller if any rules were applied.  this could also be an integer...
	 */
	public boolean applyRules (SQLColumn col) {
		List mappingRules = getRules(col);
		Iterator it = mappingRules.iterator();
		while (it.hasNext()) {			 
			MappingRule rule = (MappingRule) it.next();
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("modifying SQLColumn, field=" + rule.getModifyField() + ", value=" + rule.getModifyValue());
				}
				BeanUtils.setProperty(col,rule.getModifyField(),rule.getModifyValue());
			} catch (ConversionException ce) {
				logger.error("Tried to set a numeric value with a non-numeric String: " + rule.getModifyField());
			} catch (IllegalAccessException iae) {
				logger.error("SQLColumn getter was not public: " + rule.getModifyField());					
			} catch (InvocationTargetException ite) {					
				logger.error("SQLColumn setter threw an exception: " + rule.getModifyField());					
			}
		}
		return (mappingRules.size() > 0);
	}
	/**
	 * 
	 * change the upper case with spaces style name from SQLDatabase
	 * into a lowercase name with no spaces.
	 * 
	 * the mappingrules.properties file cannot have spaces in the values (whitespace
	 * is the delimiter!)  
	 * 
	 * @param name
	 * @return the translated string 
	 */
	public String translateDatabaseName(String name) {		
		return SPACE_STRIPPER.matcher(name).replaceAll("").toLowerCase();
	}
	
	/**
	 * 
	 * @author jack
	 * 
	 * Bean for holding rules after they are parsed in from mappingrules.properties.
	 *
	 */
	public class MappingRule {
		String database;
		String nativeType;
		String compField;
		String compCondition;
		String compValue;
		String modifyField;
		String modifyValue;
						
		/**
		 * @return Returns the compCondition.
		 */
		public String getCompCondition() {
			return compCondition;
		}
		/**
		 * @param compCondition The compCondition to set.
		 */
		public void setCompCondition(String compCondition) {
			this.compCondition = compCondition;
		}
		/**
		 * @return Returns the compField.
		 */
		public String getCompField() {
			return compField;
		}
		/**
		 * @param compField The compField to set.
		 */
		public void setCompField(String compField) {
			this.compField = compField;
		}
		/**
		 * @return Returns the compValue.
		 */
		public String getCompValue() {
			return compValue;
		}
		/**
		 * @param compValue The compValue to set.
		 */
		public void setCompValue(String compValue) {
			this.compValue = compValue;
		}
		/**
		 * @return Returns the database.
		 */
		public String getDatabase() {
			return database;
		}
		/**
		 * @param database The database to set.
		 */
		public void setDatabase(String database) {
			this.database = database;
		}
		/**
		 * @return Returns the modifyField.
		 */
		public String getModifyField() {
			return modifyField;
		}
		/**
		 * @param modifyField The modifyField to set.
		 */
		public void setModifyField(String modifyField) {
			this.modifyField = modifyField;
		}
		/**
		 * @return Returns the modifyValue.
		 */
		public String getModifyValue() {
			return modifyValue;
		}
		/**
		 * @param modifyValue The modifyValue to set.
		 */
		public void setModifyValue(String modifyValue) {
			this.modifyValue = modifyValue;
		}
		/**
		 * @return Returns the nativeType.
		 */
		public String getNativeType() {
			return nativeType;
		}
		/**
		 * @param nativeType The nativeType to set.
		 */
		public void setNativeType(String nativeType) {
			this.nativeType = nativeType;
		}

		/**
		 * Convenience method for looking at the contents of this bean.
		 */
		public String toString () {
			StringBuffer sb = new StringBuffer();
			sb.append("database="+getDatabase());
			sb.append(",nativeType=" + getNativeType());
			sb.append(",compField=" + getCompField());
			sb.append(",compCondition=" + getCompCondition());
			sb.append(",compValue=" + getCompValue());
			sb.append(",modifyField=" + getModifyField());
			sb.append(",modifyValue=" + getModifyValue());			
			return sb.toString();
		}
	}		
	

}


