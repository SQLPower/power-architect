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

package ca.sqlpower.architect.swingui.query;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.DnDTreePathTransferable;
import ca.sqlpower.swingui.query.SQLQueryUIComponents;

/**
 * This QueryDropListener Listens to the SQLObjects being dragged onto the
 * QueryTextArea from a DBTree. When an object is dragged onto the text area
 * the object's name will be entered at the caret position.
 */
public class QueryDropListener implements DropTargetListener {
    
    private static Logger logger = Logger.getLogger(SQLQueryUIComponents.class);
    private DBTree dbTree;
    private final JTextArea queryArea;
    
    public QueryDropListener(DBTree dbtree, JTextArea textArea) {
        this.queryArea = textArea;
        this.dbTree = dbtree;
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        logger.debug("we are in dragEnter");
        
    }

    public void dragExit(DropTargetEvent dte) {
        logger.debug("we are in dragExit");
        
    }

    public void dragOver(DropTargetDragEvent dtde) {
        logger.debug("we are in dragOver");
        
    }
    
    public void drop(DropTargetDropEvent dtde) {
        DataFlavor[] flavours = dtde.getTransferable().getTransferDataFlavors();
        DataFlavor bestFlavour = null;
        for (int i = 0; i < flavours.length; i++) {
            if (flavours[i] != null) {
                bestFlavour = flavours[i];
                break;
            }
        }
        try {
            ArrayList paths = (ArrayList) dtde.getTransferable().getTransferData(bestFlavour);
            Iterator it = paths.iterator();
            while(it.hasNext()) {
                Object oo = DnDTreePathTransferable.getNodeForDnDPath((SQLObject) dbTree.getModel().getRoot(), (int[])it.next());
                if (oo instanceof SQLTable) {
                    SQLTable table = ((SQLTable) oo);
                    StringBuffer buffer = new StringBuffer();
                    if (!table.getCatalogName().equals("")) {
                        buffer.append(table.getCatalogName());
                        buffer.append(".");
                    }
                    if(!table.getSchemaName().equals("")) {
                        buffer.append(table.getSchemaName());
                        buffer.append(".");
                    }
                    buffer.append(table.getPhysicalName());
                    queryArea.insert(buffer.toString(), queryArea.getCaretPosition());
                } else if (oo instanceof SQLObject) {
                    queryArea.insert(((SQLObject) oo).getPhysicalName(), queryArea.getCaretPosition());
                } else {
                    logger.error("Unknown object dropped in PlayPen: "+oo);
                }
            }
            dtde.dropComplete(true);
        } catch (UnsupportedFlavorException e) {
            logger.error(e);
            dtde.rejectDrop();
        } catch (IOException e) {
            logger.error(e);
            dtde.rejectDrop();
        } catch (ArchitectException e) {
            logger.error(e);
            dtde.rejectDrop();
        }
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
        logger.debug("we are in dropActionChange");
        
    }
    
}
