package ca.sqlpower.architect;

import java.util.List;

public class SQLRelationship {
	
	/**
	 * A List of ColumnMapping objects that describe the relationship
	 * between the imported table (pkTable) and the importing table
	 * (fkTable).
	 *
	 * @see java.sql.DatabaseMetaData#getImportedKeys
	 */
	protected List mappings;

	protected int updateRule;
	protected int deleteRule;
	protected int deferrability;

	protected String fkName;
	protected String pkName;

	protected class ColumnMapping {
		protected SQLColumn pkColumn;
		protected SQLColumn fkColumn;

		
		/**
		 * Gets the value of pkColumn
		 *
		 * @return the value of pkColumn
		 */
		public SQLColumn getPkColumn()  {
			return this.pkColumn;
		}

		/**
		 * Sets the value of pkColumn
		 *
		 * @param argPkColumn Value to assign to this.pkColumn
		 */
		public void setPkColumn(SQLColumn argPkColumn) {
			this.pkColumn = argPkColumn;
		}

		/**
		 * Gets the value of fkColumn
		 *
		 * @return the value of fkColumn
		 */
		public SQLColumn getFkColumn()  {
			return this.fkColumn;
		}

		/**
		 * Sets the value of fkColumn
		 *
		 * @param argFkColumn Value to assign to this.fkColumn
		 */
		public void setFkColumn(SQLColumn argFkColumn) {
			this.fkColumn = argFkColumn;
		}

	}

	
	/**
	 * Gets the value of mappings
	 *
	 * @return the value of mappings
	 */
	public List getMappings()  {
		return this.mappings;
	}

	/**
	 * Sets the value of mappings
	 *
	 * @param argMappings Value to assign to this.mappings
	 */
	public void setMappings(List argMappings) {
		this.mappings = argMappings;
	}

	/**
	 * Gets the value of updateRule
	 *
	 * @return the value of updateRule
	 */
	public int getUpdateRule()  {
		return this.updateRule;
	}

	/**
	 * Sets the value of updateRule
	 *
	 * @param argUpdateRule Value to assign to this.updateRule
	 */
	public void setUpdateRule(int argUpdateRule) {
		this.updateRule = argUpdateRule;
	}

	/**
	 * Gets the value of deleteRule
	 *
	 * @return the value of deleteRule
	 */
	public int getDeleteRule()  {
		return this.deleteRule;
	}

	/**
	 * Sets the value of deleteRule
	 *
	 * @param argDeleteRule Value to assign to this.deleteRule
	 */
	public void setDeleteRule(int argDeleteRule) {
		this.deleteRule = argDeleteRule;
	}

	/**
	 * Gets the value of deferrability
	 *
	 * @return the value of deferrability
	 */
	public int getDeferrability()  {
		return this.deferrability;
	}

	/**
	 * Sets the value of deferrability
	 *
	 * @param argDeferrability Value to assign to this.deferrability
	 */
	public void setDeferrability(int argDeferrability) {
		this.deferrability = argDeferrability;
	}

	/**
	 * Gets the value of fkName
	 *
	 * @return the value of fkName
	 */
	public String getFkName()  {
		return this.fkName;
	}

	/**
	 * Sets the value of fkName
	 *
	 * @param argFkName Value to assign to this.fkName
	 */
	public void setFkName(String argFkName) {
		this.fkName = argFkName;
	}

	/**
	 * Gets the value of pkName
	 *
	 * @return the value of pkName
	 */
	public String getPkName()  {
		return this.pkName;
	}

	/**
	 * Sets the value of pkName
	 *
	 * @param argPkName Value to assign to this.pkName
	 */
	public void setPkName(String argPkName) {
		this.pkName = argPkName;
	}

}
