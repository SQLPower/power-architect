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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenLabel;
import ca.sqlpower.object.SPLabel;
import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.FontSelector;
import ca.sqlpower.swingui.LabelEditorPanel;

public class EditLabelAction extends AbstractArchitectAction {

    public class PlayPenLabelEditorPanel extends LabelEditorPanel {

        public PlayPenLabelEditorPanel(SPLabel label, boolean variables, boolean override) {
            super(label, variables, override);
        }

        @Override
        public SPVariableHelper getVariablesHelper() {
            return null;
        }

        @Override
        public FontSelector getFontSelector() {
            return null;
        }

        @Override
        public List<Color> getBackgroundColours() {
            return Collections.singletonList(Color.WHITE);
        }
    }

    public EditLabelAction(ArchitectFrame frame) {
        super(frame, "Label Properties...", "Label Properties");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        List<PlayPenComponent> selection = getPlaypen().getSelectedItems();
        if (selection.size() < 1) {
            JOptionPane.showMessageDialog(getPlaypen(), Messages.getString("EditLabelAction.noLabelsSelected")); //$NON-NLS-1$
        } else if (selection.size() > 1) {
            JOptionPane.showMessageDialog(getPlaypen(), Messages.getString("EditLabelAction.multipleItemsSelected")); //$NON-NLS-1$
        } else if (selection.get(0) instanceof PlayPenLabel) {
            PlayPenLabel label = (PlayPenLabel) selection.get(0);
            showDialog(label);
        } else {
            JOptionPane.showMessageDialog(getPlaypen(), Messages.getString("EditLabelAction.pleaseSelectLabel")); //$NON-NLS-1$
        }
    }
    
    private void showDialog(final PlayPenLabel label) {
        DataEntryPanel panel = new PlayPenLabelEditorPanel(label.getLabel(), false, false);
        JDialog editDialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
                panel, frame,
                Messages.getString("EditLabelAction.dialogTitle"), //$NON-NLS-1$ 
                DataEntryPanelBuilder.OK_BUTTON_LABEL);
        editDialog.pack();
        editDialog.setLocationRelativeTo(frame);
        editDialog.setVisible(true);
    }

}
