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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import ca.sqlpower.architect.swingui.ArchitectSwingSessionContextImpl;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;

public class TestingArchitectSessionContext implements ArchitectSessionContext {

    private DataSourceCollection emptyPlDotIni = new PlDotIni();
    private Preferences prefs = Preferences.userNodeForPackage(ArchitectSwingSessionContextImpl.class);

    public ArchitectSession createSession() throws ArchitectException {
        return new ArchitectSessionImpl(this, "Test");
    }

    public ArchitectSession createSession(InputStream in) throws ArchitectException, IOException {
        ArchitectSession s = createSession();
        s.getProject().load(in, getPlDotIni());
        return s;
    }

    public List<SPDataSource> getConnections() {
        return Collections.emptyList();
    }

    public DataSourceCollection getPlDotIni() {
        return emptyPlDotIni ;
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

}
