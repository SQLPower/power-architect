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

package ca.sqlpower.architect.swingui.query;

import java.sql.Connection;
import java.sql.Statement;

/**
 * This is a container that holds a connection, a statement that is currently executing
 * on the connection and a boolean that is true if the connection is not in auto commit
 * mode and has had statements executed on it but not committed. This is a collection
 * of elements to store in a map to prevent the need to create multiple maps.
 */
public class ConnectionAndStatementBean {
    
    /**
     * The statement stored in this class
     */
    private Connection con;
    
    /**
     * The statement currently executing on the connection. This will be null if no 
     * statements are currently running on the connection. Only one statement should be
     * run on the connection at a time.
     */
    private Statement currentStmt;
    
    /**
     * A boolean to track if the connection is not in auto commit mode and if there are
     * uncommitted statements executed on it.
     */
    private boolean connectionUncommitted;
    
    public ConnectionAndStatementBean(Connection con) {
        this.con = con;
        currentStmt = null;
        connectionUncommitted = false;
    }

    public Connection getConnection() {
        return con;
    }

    public synchronized void setConnection(Connection con) {
        this.con = con;
    }

    public Statement getCurrentStmt() {
        return currentStmt;
    }

    public synchronized void setCurrentStmt(Statement currentStmt) {
        this.currentStmt = currentStmt;
    }

    public boolean isConnectionUncommitted() {
        return connectionUncommitted;
    }

    public synchronized void setConnectionUncommitted(boolean connectionUncommitted) {
        this.connectionUncommitted = connectionUncommitted;
    }

}
