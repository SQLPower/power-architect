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

package ca.sqlpower.architect.undo;

import ca.sqlpower.architect.swingui.ArchitectSwingProject;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.undo.NotifyingUndoManager;
import ca.sqlpower.sqlobject.undo.SQLObjectUndoManager;
import ca.sqlpower.util.SQLPowerUtils;

public class ArchitectUndoManager extends SQLObjectUndoManager implements NotifyingUndoManager {

    /**
     * Creates a new UndoManager and attaches it to the given PlayPen's
     * component and SQL Object model events.
     * 
     * @param playPen
     *            The play pen to track undo/redo history for.
     * @throws SQLObjectException
     *             If the manager fails to listen to all objects in the play
     *             pen's database hierarchy.
     */
    public ArchitectUndoManager(PlayPen playPen) {
        super(playPen.getSession().getTargetDatabase());
        SQLObjectRoot rootObject = playPen.getSession().getRootObject();
        final SQLObjectUndoableEventAdapter undoListener = new SQLObjectUndoableEventAdapter(false);
        rootObject.addSPListener(undoListener);
        undoListener.attachToObject(rootObject);
        final ArchitectSwingProject workspace = playPen.getSession().getWorkspace();
        playPen.getContentPane().addSPListener(eventAdapter);
        eventAdapter.attachToObject(playPen.getContentPane());
        SQLPowerUtils.listenToHierarchy(workspace.getSnapshotCollection(), eventAdapter);
        workspace.addSPListener(new AbstractSPListener() {

            /**
             * The current play pen content pane. This is assigned from the
             * ArchitectProject to ensure if the way the content pane is defined
             * in the project is changed this will be changed as well.
             */
            private PlayPenContentPane lastPPCP = 
                workspace.getPlayPenContentPane();
            
            @Override
            public void childAdded(SPChildEvent e) {
                if (e.getChild() != null && e.getChild().equals(workspace.getPlayPenContentPane())) {
                    lastPPCP = workspace.getPlayPenContentPane();
                    lastPPCP.addSPListener(eventAdapter);
                }
            }
            
            @Override
            public void childRemoved(SPChildEvent e) {
                if (e.getChild().equals(lastPPCP)) {
                    lastPPCP.removeSPListener(eventAdapter);
                }
            }
        });
        if (playPen != null) {
            playPen.addUndoEventListener(eventAdapter);
        }
    }

    public ArchitectUndoManager(SQLObject sqlObjectRoot) throws SQLObjectException {
        super(sqlObjectRoot);
    }
    
}
