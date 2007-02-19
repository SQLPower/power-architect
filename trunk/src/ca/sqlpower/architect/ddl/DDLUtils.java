package ca.sqlpower.architect.ddl;

import java.util.Vector;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ASUtils.LabelValueBean;

/**
 * DDLUtils is a collection of utilities related to creating and
 * executing Data Definition Language (DDL) statements.
 */
public class DDLUtils {

	private static final Logger logger = Logger.getLogger(DDLUtils.class);

	/**
	 * DDLUtils is a container for static methods.  You can't make an instance of it.
	 */
	private DDLUtils() {
        // this never gets used
	}

    /**
     * Returns the appropriate dot-separated fully qualified name for the
     * given table, based on its Schema and Catalog ancestors, if any.
     * 
     * @param table The SQLTable instance to get the qualified name of
     * @return A string of the form:
     * <ul>
     *  <li><tt>catalog.schema.name</tt> if the table has a schema and catalog ancestor
     *  <li><tt>catalog.name</tt> if the table has a catalog ancestor but no schema
     *  <li><tt>schema.name</tt> if the table has a schema ancestor but no catalog
     *  <li><tt>name</tt> if the table has no schema or catalog ancestors
     * </ul>
     * @throws NullPointerException if table is null
     */
    public static String toQualifiedName(SQLTable table) {
        return toQualifiedName(
                table.getCatalogName(),
                table.getSchemaName(),
                table.getName());
    }
    
    /**
     * Formats the components of a fully qualified database object name
     * into the standard SQL "dot notation".
     *
     * @param catalog The catalog name of the object, or null if it has no catalog
     * @param schema The schema name of the object, or null if it has no schema
     * @param name The name of the object (null is not acceptable)
     * @return A dot-separated string of all the non-null arguments.
     */
    public static String toQualifiedName(String catalog, String schema, String name) {
        StringBuffer qualName = new StringBuffer();
        if (catalog != null && catalog.length() > 0 ) {
            qualName.append(catalog);
            qualName.append(".");
        }
        if (schema != null && schema.length() > 0) {
        	qualName.append(schema);
        	qualName.append(".");
        }

        qualName.append(name);
        logger.debug(String.format("%s.%s.%s -> %s", catalog, schema, name, qualName));
        return qualName.toString();
    }

    /**
     * Formats the components of a fully qualified database object name
     * into the standard SQL "dot notation", with quote.
     *
     * @param catalog The catalog name of the object, or null if it has no catalog
     * @param schema The schema name of the object, or null if it has no schema
     * @param name The name of the object (null is not acceptable)
     * @param openQuote openning quote
     * @param closeQuote closing quote
     * @return A dot-separated string of all the non-null arguments.
     */
    public static String toQualifiedName(String catalog, String schema, String name,
                                        String openQuote, String closeQuote) {

        String newCatalog = null;
        String newSchema = null;
        String newName = null;
        if (catalog != null && catalog.length() > 0 ) {
            if ( openQuote != null && openQuote.length() > 0 &&
                    closeQuote != null && closeQuote.length() > 0 ) {
                newCatalog = openQuote + catalog + closeQuote;
            }
            else if ( openQuote != null && openQuote.length() > 0 ) {
                newCatalog = openQuote + catalog;
            }
            else {
                newCatalog = catalog;
            }
        }
        if (schema != null && schema.length() > 0) {
            if ( openQuote != null && openQuote.length() > 0 &&
                    closeQuote != null && closeQuote.length() > 0 ) {
                newSchema = openQuote + schema + closeQuote;
            }
            else if ( openQuote != null && openQuote.length() > 0 ) {
                newSchema = openQuote + schema;
            }
            else {
                newSchema = schema;
            }
        }
        if ( openQuote != null && openQuote.length() > 0 &&
                closeQuote != null && closeQuote.length() > 0 ) {
            newName = openQuote + name + closeQuote;
        }
        else if ( openQuote != null && openQuote.length() > 0 ) {
            newName = openQuote + name;
        }
        else {
            newName = name;
        }
        return toQualifiedName(newCatalog,newSchema,newName);
    }

    public static Vector<LabelValueBean> getDDLTypes()
    {

    		Vector<LabelValueBean> dbTypeList = new Vector();
		dbTypeList.add(ASUtils.lvb("SQL 92", GenericDDLGenerator.class));
		dbTypeList.add(ASUtils.lvb("DB2", DB2DDLGenerator.class));
		dbTypeList.add(ASUtils.lvb("Oracle 8i/9i", OracleDDLGenerator.class));
		dbTypeList.add(ASUtils.lvb("PostgreSQL", PostgresDDLGenerator.class));
		dbTypeList.add(ASUtils.lvb("SQLServer 2000", SQLServerDDLGenerator.class));
        dbTypeList.add(ASUtils.lvb("MySql", MySqlDDLGenerator.class));
		return dbTypeList;
    }

    public static DDLGenerator createDDLGenerator(ArchitectDataSource ads)
                        throws InstantiationException, IllegalAccessException {

        Class generatorClass = (Class) ArchitectUtils.getDriverDDLGeneratorMap().get(
                                                                ads.getDriverClass());
        if (generatorClass == null) {
            return null;
        }
        return (DDLGenerator)generatorClass.newInstance();

    }

}
