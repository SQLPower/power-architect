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
package ca.sqlpower.architect.etl.kettle;

import org.apache.log4j.Logger;

/**
 * A container for Kettle-related options.
 */
public class KettleOptions {

    private static final Logger logger = Logger.getLogger(KettleOptions.class);
    
    /**
     * The key to use in an SPDataSource for the repository login
     * name.
     */
    public static final String KETTLE_REPOS_LOGIN_KEY = "ca.sqlpower.architect.etl.kettle.repos.login";

    /**
     * The key to use in an SPDataSource for the repository password.
     */
    public static final String KETTLE_REPOS_PASSWORD_KEY = "ca.sqlpower.architect.etl.kettle.repos.password";
    
    /**
     * The key to use in an SPDataSource for the connection type.
     */
    public static final String KETTLE_CONNECTION_TYPE_KEY = "ca.sqlpower.architect.etl.kettle.connectionType";
    
    /**
     * The string of the url parameter that defines the database name
     */
    public static final String KETTLE_DATABASE = "Database";
    
    /**
     * The key to use in an SPDataSource for the database name.
     */
    public static final String KETTLE_DATABASE_KEY = "ca.sqlpower.architect.etl.kettle.database";

    /**
     * The string of the url parameter that defines the port
     */
    public static final String KETTLE_PORT = "Port";

    /**
     * The key to use in an SPDataSource for the port value.
     */
    public static final String KETTLE_PORT_KEY = "ca.sqlpower.architect.etl.kettle.port";
    
    /**
     * The string of the url parameter that defines the host name
     */
    public static final String KETTLE_HOSTNAME = "Hostname";

    /**
     * The key to use in an SPDataSource for the host name.
     */
    public static final String KETTLE_HOSTNAME_KEY = "ca.sqlpower.architect.etl.kettle.hostname";

}
