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

package ca.sqlpower.architect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import ca.sqlpower.architect.swingui.ArchitectSwingSessionContextImpl;
import ca.sqlpower.enterprise.client.SPServerInfoManager;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SpecificDataSourceCollection;
import ca.sqlpower.sqlobject.SQLObjectException;

public class TestingArchitectSessionContext implements ArchitectSessionContext {

    private final DataSourceCollection<JDBCDataSource> plDotIni;
    private Preferences prefs = Preferences.userNodeForPackage(ArchitectSwingSessionContextImpl.class);

    public TestingArchitectSessionContext() throws IOException {
        PlDotIni delegate = new PlDotIni();
        delegate.read(new File("pl.regression.ini"));
        plDotIni = new SpecificDataSourceCollection<JDBCDataSource>(delegate, JDBCDataSource.class);
    }
    
    public ArchitectSession createSession() throws SQLObjectException {
        return new ArchitectSessionImpl(this, "Test");
    }

    public ArchitectSession createSession(InputStream in) throws SQLObjectException, IOException {
        ArchitectSession s = createSession();
        s.getProjectLoader().load(in, getPlDotIni());
        return s;
    }

    public List<JDBCDataSource> getConnections() {
        return Collections.emptyList();
    }

    public DataSourceCollection<JDBCDataSource> getPlDotIni() {
        return plDotIni ;
    }

    public String getPlDotIniPath() {
        return "";
    }

    public Preferences getPrefs() {
        return prefs;
    }

    public Collection<ArchitectSession> getSessions() {
        return Collections.emptyList();
    }

    public void setPlDotIniPath(String plDotIniPath) {
        // do nothing!
    }

    public SPServerInfoManager getServerManager() {
        // TODO Auto-generated method stub
        return null;
    }

}
