package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLDatabase;


/**
 * When invoked, this action sets the given database's connection info to the supplied DataSource.
 */
public class SetDataSourceAction extends AbstractAction {
	
	private static final Logger logger = Logger.getLogger(SetDataSourceAction.class);

	protected ArchitectDataSource dbcs;
	protected SQLDatabase db;

	/**
	 * Creates an action that will set the DBCS properties of DB to those in DBCS when invoked.
	 * This is useful for a popup menu item, for example.
	 * 
	 * @param db The database to modify the connection properties of.
	 * @param dbcs The settings to set on db.
	 * @throws NullPointerException if db or dbcs are null. (Comes with free insult).
	 */
	public SetDataSourceAction(SQLDatabase db, ArchitectDataSource dbcs) {
		super(dbcs.getName());
		if (dbcs == null) throw new NullPointerException("Null DBCS is not allowed, doofus");
		this.dbcs = dbcs;
		if (db == null) throw new NullPointerException("Null db is not allowed, dimwad");
		this.db = db;
	}

	public void actionPerformed(ActionEvent e) {
		logger.debug("Setting data source of "+db+" to "+dbcs);
		
		// copy over the values from the selected DB.
		ArchitectDataSource tSpec = db.getDataSource();
		tSpec.setDisplayName(dbcs.getDisplayName());
		tSpec.setDriverClass(dbcs.getDriverClass());
		tSpec.setUrl(dbcs.getUrl());
		tSpec.setUser(dbcs.getUser());
		tSpec.setPass(dbcs.getPass());
		tSpec.setPlSchema(dbcs.getPlSchema());
		tSpec.setPlDbType(dbcs.getPlDbType());
		tSpec.setOdbcDsn(dbcs.getOdbcDsn());
	}
}