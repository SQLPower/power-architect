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

package ca.sqlpower.architect.swingui.olap.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.PlayPen.CancelableListener;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.swingui.olap.OLAPPane;
import ca.sqlpower.swingui.CursorManager;

/**
 * Abstract action that makes it easy to implement new *usage-creating actions.
 */
public abstract class CreateUsageAction<P1 extends OLAPPane<?, ?>, P2 extends OLAPPane<?, ?>>
extends AbstractArchitectAction 
implements ActionListener, SelectionListener, CancelableListener {
    
    private static final Logger logger = Logger.getLogger(CreateUsageAction.class);
    
    /**
     * The playpen's CursorManager, which controls the type of the cursor
     */
    private CursorManager cursorManager;
    
    /**
     * One type of pane we are watching for the user to select in the playpen.
     */
    private final Class<P1> pane1Class;

    /**
     * One type of pane we are watching for the user to select in the playpen.
     */
    private final Class<P2> pane2Class;
    
    /**
     * OLAPPane containing the public resource which will be used by another
     * OLAPObject through an usage
     */
    private P1 pane1;
    
    /**
     * OLAPPane that will use the resource described above though an usage
     */
    private P2 pane2;

    /**
     * Controls the state of the usage creation process
     */
    protected boolean active;
    
    public CreateUsageAction(
            ArchitectSwingSession session,
            PlayPen pp,
            Class<P1> pane1Class,
            Class<P2> pane2Class,
            String name,
            Icon icon) {
        super(session, pp, "New " + name + "...", "Create a new " + name, icon);
        this.pane1Class = pane1Class;
        this.pane2Class = pane2Class;
        cursorManager = getPlaypen().getCursorManager();
    }

    public void actionPerformed(ActionEvent e) {
        // At start of creation process, register to selection and cancelable listener
        if (!active) {
            logger.debug(">>>>>> Adding to selectable listeners, preparing to start the creation.");
            getPlaypen().fireCancel();
            getPlaypen().selectNone();
            getPlaypen().addSelectionListener(this);
            getPlaypen().addCancelableListener(this);
            cursorManager.placeModeStarted();
            pane1 = null;
            pane2 = null;
            active = true;
        }
    }

    public void itemDeselected(SelectionEvent e) {
        logger.debug(">>>>>> Item deselected (ignoring).");
    }

    public void itemSelected(SelectionEvent e) {
        logger.debug(">>>>>> Item selected.");
        Selectable s = e.getSelectableSource();
        if (!s.isSelected()) {
            return;
        }

        if (pane1Class.isAssignableFrom(s.getClass()) && pane1 == null) {
            logger.debug(">>>>>> First selection");
            pane1 = pane1Class.cast(s);
            
        } else if (pane2Class.isAssignableFrom(s.getClass()) && pane2 == null) {
            logger.debug(">>>>>> Second selection");
            pane2 = pane2Class.cast(s);
            
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("The user clicked on an irrelevant component: "+s); //$NON-NLS-1$
                logger.debug(">>>>>> Wrong component selected, supposed to discard all data.");
            }
            reset();
        }
        
        if (pane2 != null && pane1 != null) {
            PlayPenContentPane cp = getPlaypen().getContentPane();
            try {
                cp.begin("Creating usage");
                createUsage(pane1, pane2);
                cp.commit();
            } catch (Throwable ex) {
                cp.rollback("Exception occurred while creating usage: " + ex.toString());
                throw new RuntimeException(ex);
            } finally {
                reset();
            }
        }
    }

    /**
     * Creates the usage this action is supposed to create, and attaches it
     * to the appropriate place(s) in the business model as well as adding it
     * and attaching it to the playpen.
     * 
     * @param pane1 The pane of type P1 that the user clicked
     * @param pane2 The pane of type P2 that the user clicked
     */
    protected abstract void createUsage(P1 pane1, P2 pane2);

    public void cancel() {
        reset();
    }

    /**
     * Cleans up and prepares this instance for another creation.
     */
    private void reset() {
        logger.debug(">>>>>> Resetting");
        pane1 = null;
        pane2 = null;
        cursorManager.placeModeFinished();
        active = false;
        getPlaypen().removeSelectionListener(this);
        getPlaypen().removeCancelableListener(this);
    }
}
