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

package ca.sqlpower.architect.swingui.query.action;

import java.awt.Component;

import javax.swing.AbstractAction;

/**
 * This is a small extension of {@link AbstractAction} that takes a
 * parent {@link Component} for popping up dialogs.
 */
public abstract class AbstractSQLQueryAction extends AbstractAction {
    
    public final Component parent;
    
    public AbstractSQLQueryAction(Component c) {
        super();
        this.parent = c;
    }
    
    public AbstractSQLQueryAction(Component c, String name) {
        super(name);
        parent = c;
    }

}
