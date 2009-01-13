/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

import org.apache.log4j.Logger;

/**
 * This hierarchy listener will add and remove itself to children added to and
 * removed from the object it is listening to. This class will not automatically
 * listen to the hierarchy of the object it is initially placed on,
 * {@link ArchitectUtils#listenToHierarchy(SQLObjectListener, SQLObject)} can be
 * used to do the initial connecting of this listener to a hierarchy.
 * <p>
 * This class is meant to be extended by listeners that wish to listen to the
 * hierarchy of SQLObjects.
 */
public class SQLObjectHierarchyListener implements SQLObjectListener {
    private static final Logger logger = Logger.getLogger(SQLObjectHierarchyListener.class);

    public void dbChildrenInserted(SQLObjectEvent e) {
        try {
            SQLObject[] newEventSources = e.getChildren();
            for (int i = 0; i < newEventSources.length; i++) {
                ArchitectUtils.listenToHierarchy(this, newEventSources[i]);
                logger.debug("Adding listener to " + newEventSources[i] + " of type " + newEventSources[i].getClass());
            }
        } catch (ArchitectException ex) {
            logger.error("Error listening to added object", ex); //$NON-NLS-1$
        }
    }

    public void dbChildrenRemoved(SQLObjectEvent e) {
        try {
            SQLObject[] oldEventSources = e.getChildren();
            for (int i = 0; i < oldEventSources.length; i++) {
                ArchitectUtils.unlistenToHierarchy(this, oldEventSources[i]);
                logger.debug("Removing listener from " + oldEventSources[i]);
            }
        } catch (ArchitectException ex) {
            logger.error("Error unlistening to removed object", ex); //$NON-NLS-1$
        }
    }

    public void dbObjectChanged(SQLObjectEvent e) {
        // Object changes do not affect hierarchy.
    }

}
