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

package ca.sqlpower.architect.swingui.critic;

import ca.sqlpower.architect.swingui.PlayPenComponent;


/**
 * With critics and other future enhancements we want to be able to mark
 * existing model objects to make visible notes to the user. This interface
 * allows the content pane to collect and display these object types.
 */
public abstract class ModelBadge extends PlayPenComponent {

    protected ModelBadge(String name) {
        super(name);
    }

    /**
     * Returns the subject that this badge is marking. 
     */
    public abstract Object getSubject();
    
}
