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
package ca.sqlpower.architect.profile.event;

import java.util.EventListener;

/**
 * Interface for receiving notifications of changes to the collection
 * of profiles in a ProfileManager.
 */
public interface ProfileChangeListener extends EventListener {

    /** One or Many profiles were added. */
    public void profilesAdded(ProfileChangeEvent e);

    /** One or Many profiles were removed. */
    public void profilesRemoved(ProfileChangeEvent e);

    /** The list changed in some major way; listeners should re-fetch it. */
    public void profileListChanged(ProfileChangeEvent event);

}
