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

import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.undo.NotifyingUndoManager;
import ca.sqlpower.sqlobject.undo.SQLObjectUndoManager;

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
    public ArchitectUndoManager(PlayPen playPen) throws SQLObjectException {
        super(playPen.getSession().getTargetDatabase());
        playPen.getSession().getRootObject().addSQLObjectListener(new SQLObjectUndoableEventAdapter(false));
        if (playPen != null) {
            playPen.addUndoEventListener(eventAdapter);
        }
    }

    public ArchitectUndoManager(SQLObject sqlObjectRoot) throws SQLObjectException {
        super(sqlObjectRoot);
    }
    
}
