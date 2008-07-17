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

package ca.sqlpower.architect.ddl;

import java.sql.SQLException;

import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLRelationship.UpdateDeleteRule;

public class SQLServer2005DDLGenerator extends SQLServerDDLGenerator {

    public static final String GENERATOR_VERSION = "$Revision: 2099 $";

    public SQLServer2005DDLGenerator() throws SQLException {
        super();
    }

    @Override
    public void writeHeader() {
        println("-- Created by SQLPower SQLServer 2005 DDL Generator "+GENERATOR_VERSION+" --");
    }
    
    @Override
    public String getName() {
        return "Microsoft SQL Server 2005";
    }
    
    @Override
    public boolean supportsDeleteAction(SQLRelationship r) {
        return r.getDeleteRule() != UpdateDeleteRule.RESTRICT;
    }

    @Override
    public boolean supportsUpdateAction(SQLRelationship r) {
        return r.getUpdateRule() != UpdateDeleteRule.RESTRICT;
    }
}
