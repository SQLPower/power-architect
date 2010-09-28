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

package ca.sqlpower.architect.profile;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.enterprise.ArchitectPersisterSuperConverter;
import ca.sqlpower.architect.enterprise.ArchitectSessionPersister;
import ca.sqlpower.dao.SPPersisterListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.MonitorableImpl;
import ca.sqlpower.util.SessionNotFoundException;

/**
 * Provides a template method for doProfile which alleviates the
 * need to write boilerplate code for every profile creator implementation.
 */
public abstract class AbstractTableProfileCreator implements TableProfileCreator {
    
    private static final Logger logger = Logger.getLogger(AbstractTableProfileCreator.class);
    
    /**
     * A generic template for populating a profile result.  Calls {@link #doProfileImpl()}
     * to perform the actual work of populating this profile result.
     * <p>
     * This method will fire a profileStarted event before calling the subclass's
     * doProfile, then fire a profileFinished event after doProfile exits (with
     * or without success) unless this profile population has been cancelled,
     * in which case it fires a profileCancelled event.
     */
    public final boolean doProfile(final TableProfileResult actualTPR) {
        final TableProfileResult tpr = new TableProfileResult(actualTPR);
        
        //Setting the profile result UUID to match which allows the
        //persister to update the actual tpr at the end.
        tpr.setUUID(actualTPR.getUUID());
        
        MonitorableImpl pm = (MonitorableImpl) tpr.getProgressMonitor();
        try {
            tpr.begin("Profiling");
            try {
                tpr.fireProfileStarted();
                pm.setMessage(tpr.getProfiledObject().getName());
                if (!pm.isCancelled()) {
                    pm.setStarted(true);
                    pm.setFinished(false);
                    tpr.setCreateStartTime(System.currentTimeMillis());
                    doProfileImpl(tpr);
                }
            } catch (Exception ex) {
                tpr.setException(ex);
                logger.error("Profile failed. Saving exception:", ex);
            } finally {
                tpr.setCreateEndTime(System.currentTimeMillis());
                pm.setProgress(pm.getProgress() + 1);
                pm.setFinished(true);
                // this somehow fixes a progress bar visibility issue
                pm.setStarted(false);
                if (pm.isCancelled()) {
                    tpr.fireProfileCancelled();
                } else {
                    tpr.fireProfileFinished();
                }
            }
            tpr.commit();
        } finally {
            Runnable runner = new Runnable() {
                public void run() {
                    //None of the profiling creates or saves any data source information so an
                    //empty data source is used for the converter. If the profiling stores
                    //data source information in the future we may need the data source collection
                    //in the project.
                    DataSourceCollection<SPDataSource> dsCollection = new PlDotIni();
                    
                    SPObject root = actualTPR.getWorkspaceContainer().getWorkspace();
                    ArchitectPersisterSuperConverter converter = 
                        new ArchitectPersisterSuperConverter(dsCollection, root);
                    ArchitectSessionPersister persister = 
                        new ArchitectSessionPersister("Profiling persister", root, converter);
                    persister.setWorkspaceContainer(actualTPR.getWorkspaceContainer());
                    SPPersisterListener eventCreator = new SPPersisterListener(persister, converter);
                    eventCreator.persistObject(tpr, 
                            actualTPR.getParent().getChildren(TableProfileResult.class).indexOf(actualTPR),
                            false);
                }
            };
            try {
                actualTPR.getRunnableDispatcher().runInForeground(runner);
            } catch (SessionNotFoundException e) {
                runner.run();
            }
        }
        return !pm.isCancelled();
    }

    /**
     * The meat of profiling the table. The {@link TableProfileResult} given
     * is not connected to the table so you do not need to worry about where the
     * events go to.
     * 
     * @param tpr
     *            The table to profile.
     */
    protected abstract boolean doProfileImpl(TableProfileResult tpr) throws Exception;

}
