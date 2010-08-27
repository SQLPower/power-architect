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
package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.etl.ExportCSV;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;

public class ExportCSVAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(ExportCSVAction.class);
    
    private final ArchitectFrame frame;
    
    public ExportCSVAction(ArchitectFrame frame) {
        super(Messages.getString("ExportCSVAction.name")); //$NON-NLS-1$
        this.frame = frame;
    }
    
    public void actionPerformed(ActionEvent e) {
        FileWriter output = null;
        try {
            if (frame.getCurrentSession() == null) return;
            ExportCSV export = new ExportCSV(frame.getCurrentSession().getTargetDatabase().getTables());

            File file = null;

            JFileChooser fileDialog = new JFileChooser();
            fileDialog.setSelectedFile(new File("map.csv")); //$NON-NLS-1$

            if (fileDialog.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION){
                file = fileDialog.getSelectedFile();
            } else {
                return;
            }

            output = new FileWriter(file);
            output.write(export.getCSVMapping());
            output.flush();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        } catch (SQLObjectException e1) {
            throw new SQLObjectRuntimeException(e1);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e1) {
                    logger.error("IO Error", e1); //$NON-NLS-1$
                }
            }
        }
    }
}
