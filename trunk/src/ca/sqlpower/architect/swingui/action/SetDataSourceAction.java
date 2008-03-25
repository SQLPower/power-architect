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
package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.sql.SPDataSource;


/**
 * When invoked, this action copies one database's connection info
 * into the supplied DataSource.
 */
public class SetDataSourceAction extends AbstractAction {
	
	private static final Logger logger = Logger.getLogger(SetDataSourceAction.class);

    /**
     * The source of the database connection parameters.
     */
	private final SPDataSource dbcs;
    
    /**
     * The target for the database connection parameters.
     */
	private final SQLDatabase db;

	/**
	 * Creates an action that will set the DBCS properties of DB to those in DBCS when invoked.
	 * This is useful for a popup menu item, for example.
	 * 
	 * @param db The database to modify the connection properties of.
	 * @param dbcs The settings to set on db.
	 * @throws NullPointerException if db or dbcs are null. (Comes with free insult).
	 */
	public SetDataSourceAction(SQLDatabase db, SPDataSource dbcs) {
		super(dbcs.getName());
		if (dbcs == null) throw new NullPointerException("Null DBCS is not allowed, doofus");
		this.dbcs = dbcs;
		if (db == null) throw new NullPointerException("Null db is not allowed, dimwad");
		this.db = db;
	}

	public void actionPerformed(ActionEvent e) {
		logger.debug("Setting data source of "+db+" to "+dbcs);
        SPDataSource tSpec = db.getDataSource();
        tSpec.copyFrom(dbcs);
	}
}