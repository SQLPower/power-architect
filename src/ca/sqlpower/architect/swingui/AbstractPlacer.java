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

package ca.sqlpower.architect.swingui;

import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.swingui.PlayPen.CancelableListener;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPSUtils;

/**
 * A generic class for placing arbitrary playpen components in the
 * playpen when the user releases the mouse button.
 */
public abstract class AbstractPlacer extends MouseAdapter implements CancelableListener {

    private static final Logger logger = Logger.getLogger(AbstractPlacer.class);

    protected final PlayPen playpen;
    
    /**
     * Creates a new table placer for the given table pane. Once constructed,
     * you have to activate this instance by calling {@link #dirtyup()}.
     * 
     * @param tp The new tablepane to add when the user clicks the mouse
     */
    protected AbstractPlacer(PlayPen playpen) {
        this.playpen = playpen;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        cleanup();
        Point p = e.getPoint();
        playpen.unzoomPoint(p);
        logger.debug("Placing item at: " + p); //$NON-NLS-1$
        try {
            DataEntryPanel editPanel = place(p);
            Window owner = SwingUtilities.getWindowAncestor(playpen);
            JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                    editPanel, owner,
                    getEditDialogTitle(),
                    Messages.getString("PlayPen.okOption")); //$NON-NLS-1$
            
            d.pack();
            d.setLocationRelativeTo(owner);
            d.setVisible(true);
        } catch (ArchitectException ex) {
            logger.error("Failed to add item to play pen:", ex); //$NON-NLS-1$
            SPSUtils.showExceptionDialogNoReport(
                    playpen.getSession().getArchitectFrame(),
                    "Add item to play pen failed",
                    ex);
            return;
        }
    }

    /**
     * Returns the title for the edit dialog that pops up after place()
     * has been called, if place() returns a data entry panel. If place()
     * never returns a panel, it's ok for this method to return null.
     */
    protected abstract String getEditDialogTitle();

    /**
     * Performs the specific placement operation based on the mouse click.
     * This should add both to the PlayPen itself as well as the business
     * model.
     * 
     * @param p
     *            The point to place at, in logical playpen coordinates.
     * @return The data entry panel to show. This it convenient if you want to
     *         pop up an editor for the newly-placed component. If you don't
     *         want an editor to pop up, simply return null.
     */
    public abstract DataEntryPanel place(Point p) throws ArchitectException;

    /**
     * Implements {@link CancelableListener} by canceling this place operation.
     */
    public void cancel() {
        cleanup();
    }

    /**
     * Activates this TablePlacer by attaching it to the play pen. This
     * table placer will clean itself up when the mouse button is pressed
     * and released, or when the user cancels on the playpen, whichever
     * comes first.
     */
    public void dirtyup() {
        playpen.getCursorManager().placeModeStarted();
        playpen.addMouseListener(this);
        playpen.addCancelableListener(this);
    }

    /**
     * Deactivates this TablePlacer by detaching it from the play pen. This
     * gets called either when the mouse button is pressed and released, or
     * when the user cancels on the playpen, whichever comes first.
     */
    private void cleanup() {
        playpen.removeCancelableListener(this);
        playpen.removeMouseListener(this);
        playpen.getCursorManager().placeModeFinished();
    }
    
}
