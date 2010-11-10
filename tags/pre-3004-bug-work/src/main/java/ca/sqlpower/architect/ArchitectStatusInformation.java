/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect;

import ca.sqlpower.util.MonitorableImpl;

/**
 * Used to update a status message/label/image/text/other that is user visible
 * but does not interrupt their work.
 */
public interface ArchitectStatusInformation {

    /**
     * Creates and returns an object that can be used to monitor the progress of
     * one type of action. If you want to monitor a second type of action call
     * this method again to get a new monitor for the new action. This will
     * prevent the progress bars from moving backwards. When the monitor
     * isFinished method returns true or the progress has reached or passed the
     * job size the monitor will be removed.
     */
    public MonitorableImpl createProgressMonitor();
    
}
