/*
 * Copyright (c) 2008, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;

public class ReverseRelationshipAction extends AbstractArchitectAction implements SelectionListener{
    
    private static final Logger logger = Logger.getLogger(ReverseRelationshipAction.class);
    
    private Relationship relationship;
    private ArchitectFrame af;
    
    public ReverseRelationshipAction(ArchitectSwingSession session) {
        super(session, Messages.getString("ReverseRelationshipAction.name"), Messages.getString("ReverseRelationshipAction.description"), "reverse"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        this.af = session.getArchitectFrame();
        setEnabled(true);
        
        playpen.addSelectionListener(this);
    }

    /**
     * First removes the existing relationship, and then insert a new relationship with 
     * primary key and foreign key swapped.
     */
    public void actionPerformed(ActionEvent e) {
        
        List selection = playpen.getSelectedItems();
        if (selection.size() < 1) {
            JOptionPane.showMessageDialog(playpen, Messages.getString("ReverseRelationshipAction.noRelationshipsSelected")); //$NON-NLS-1$
        } else if (selection.size() > 1) {
            JOptionPane.showMessageDialog(playpen, Messages.getString("ReverseRelationshipAction.multipleItemsSelected")); //$NON-NLS-1$
        } else if (selection.get(0) instanceof Relationship) {
            this.relationship = af.getPlayPen().getSelectedRelationShips().get(0);
            SQLTable fkTable = relationship.getFkTable().getModel();
            SQLTable pkTable = relationship.getPkTable().getModel();
            boolean identify = relationship.getModel().isIdentifying();
            
            try {
                playpen.startCompoundEdit("Reverse Relationship"); //$NON-NLS-1$
                
                SQLRelationship sr = relationship.getModel();
                sr.getPkTable().removeExportedKey(sr);
                SQLRelationship model = new SQLRelationship();
                // XXX: need to ensure uniqueness of setName(), but 
                // to_identifier should take care of this...            
                model.setName(pkTable.getName()+"_"+fkTable.getName()+"_fk");  //$NON-NLS-1$ //$NON-NLS-2$
                model.setIdentifying(identify);
                model.attachRelationship(fkTable,pkTable,true);

                Relationship r = new Relationship(playpen, model);
                playpen.addRelationship(r);
                r.revalidate();
            } catch (ArchitectException ex) {
                logger.error("Couldn't reverse relationship", ex); //$NON-NLS-1$
                ASUtils.showExceptionDialogNoReport(playpen, Messages.getString("ReverseRelationshipAction.couldNotReverseRelationship"), ex); //$NON-NLS-1$
            } finally {
                playpen.endCompoundEdit("Ending the reversal of a relationship"); //$NON-NLS-1$
            }
        } else {
            JOptionPane.showMessageDialog(playpen, Messages.getString("ReverseRelationshipAction.noRelationshipsSelected")); //$NON-NLS-1$
        }
        
       
    }

    public void itemDeselected(SelectionEvent e) {
        // TODO Auto-generated method stub
    }

    public void itemSelected(SelectionEvent e) {
        // TODO Auto-generated method stub
    }
}
