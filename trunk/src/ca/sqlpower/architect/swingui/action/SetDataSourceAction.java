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
package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLDatabase;


/**
 * When invoked, this action copies one database's connection info
 * into the supplied DataSource.
 */
public class SetDataSourceAction extends AbstractAction {
	
	private static final Logger logger = Logger.getLogger(SetDataSourceAction.class);

    /**
     * The source of the database connection parameters.
     */
	private final ArchitectDataSource dbcs;
    
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
	public SetDataSourceAction(SQLDatabase db, ArchitectDataSource dbcs) {
		super(dbcs.getName());
		if (dbcs == null) throw new NullPointerException("Null DBCS is not allowed, doofus");
		this.dbcs = dbcs;
		if (db == null) throw new NullPointerException("Null db is not allowed, dimwad");
		this.db = db;
	}

	public void actionPerformed(ActionEvent e) {
		logger.debug("Setting data source of "+db+" to "+dbcs);
        ArchitectDataSource tSpec = db.getDataSource();
        tSpec.copyFrom(dbcs);
	}
}