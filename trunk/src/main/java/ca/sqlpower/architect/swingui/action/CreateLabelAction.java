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

package ca.sqlpower.architect.swingui.action;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;

import ca.sqlpower.architect.swingui.AbstractPlacer;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenLabel;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.object.SPLabel;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.LabelEditorPanel;

public class CreateLabelAction extends AbstractArchitectAction {

    public CreateLabelAction(ArchitectFrame frame) {
        super(frame, "New Label...", "New Label", "label");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PlayPen playpen = getPlaypen();
        playpen.fireCancel();

        SPLabel spLabel = new ArchitectLabel();
        spLabel.setName("label");
        PlayPenLabel ppLabel = new PlayPenLabel(spLabel, "label"); 
        LabelPlacer labelPlacer = new LabelPlacer(playpen, ppLabel);
        labelPlacer.dirtyup();
    }
    
    public static final class ArchitectLabel extends SPLabel {
        @Override
        public Font getFont() {
            return null;
        }

        @Override
        public void setFont(Font font) {
            // no-op, Architect does not persist font properties yet.
        }
    }

    private class LabelPlacer extends AbstractPlacer {

        private final PlayPenLabel label;

        LabelPlacer(PlayPen pp, PlayPenLabel label) {
            super(pp);
            this.label = label;
        }
        
        @Override
        protected String getEditDialogTitle() {
            return "Label Properties";
        }

        @Override
        public DataEntryPanel place(final Point p) throws SQLObjectException {
            label.setLocation(p);
            LabelEditorPanel editPanel = frame.getEditLabelAction().new PlayPenLabelEditorPanel(label.getLabel(), false, true) {
                @Override
                public boolean applyChanges() {
                    ArchitectSwingSession session = getSession();
                    try {       
                        session.getWorkspace().begin("Creating a PlayPenLabel");
                        if (super.applyChanges()) {
                            playpen.selectNone();
                            playpen.addLabel(label, p);
                            label.setSelected(true, SelectionEvent.SINGLE_SELECT);
                            session.getWorkspace().commit();
                            return true;
                        } else {
                            session.getWorkspace().rollback("Error creating label");
                            return false;
                        }
                    } catch (Throwable t) {
                        session.getWorkspace().rollback("Error creating label");
                        throw new RuntimeException(t);
                    }
                }
            };

            return editPanel;
        }
    }
    
}
