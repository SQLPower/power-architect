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

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;

public class ReverseRelationshipAction extends AbstractArchitectAction {
    
    private static final Logger logger = Logger.getLogger(ReverseRelationshipAction.class);
    
    
    public ReverseRelationshipAction(ArchitectFrame frame) {
        super(frame, Messages.getString("ReverseRelationshipAction.name"), //$NON-NLS-1$
                Messages.getString("ReverseRelationshipAction.description"), "reverse"); //$NON-NLS-1$ //$NON-NLS-2$
        putValue(ACTION_COMMAND_KEY, PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
    }

    /**
     * First removes the existing relationship, and then insert a new relationship with 
     * primary key and foreign key swapped.
     */
    public void actionPerformed(ActionEvent e) {
        List<PlayPenComponent> selection = getPlaypen().getSelectedItems();
        if (selection.size() != 1) {
            JOptionPane.showMessageDialog(getPlaypen(), Messages.getString("ReverseRelationshipAction.multipleSelected")); //$NON-NLS-1$
        } else if (selection.get(0) instanceof Relationship) {
            reverseRelationship((Relationship) selection.get(0));
        } else {
            JOptionPane.showMessageDialog(getPlaypen(), Messages.getString("ReverseRelationshipAction.noRelationshipsSelected")); //$NON-NLS-1$
        }
    }        

    public void reverseRelationship(Relationship relationship) {
        SQLTable fkTable = relationship.getFkTable().getModel();
        SQLTable pkTable = relationship.getPkTable().getModel();
        boolean identify = relationship.getModel().isIdentifying();
        
        try {
            getPlaypen().startCompoundEdit("Reverse Relationship"); //$NON-NLS-1$
            
            SQLRelationship sr = relationship.getModel();
            sr.getPkTable().removeExportedKey(sr);
            SQLRelationship model = new SQLRelationship();  
            model.setName(pkTable.getName()+"_"+fkTable.getName()+"_fk"); //$NON-NLS-1$ //$NON-NLS-2$
            model.setIdentifying(identify);
            
            // swaps the fkTable and pkTable around, resulting in reversing the relationship
            model.attachRelationship(fkTable,pkTable,true);

            model.setDeferrability(sr.getDeferrability());
            // since the relationship is already reversed, then you can setFkCardinality
            // to whatever the fkCardinality was before, rather than the pkCardinality
            model.setFkCardinality(sr.getFkCardinality());
            model.setPkCardinality(sr.getPkCardinality());
            
            Relationship r = new Relationship(model, getPlaypen().getContentPane());
            getPlaypen().addRelationship(r);
            r.revalidate();
        } catch (SQLObjectException ex) {
            logger.error("Couldn't reverse relationship", ex); //$NON-NLS-1$
            ASUtils.showExceptionDialogNoReport(getPlaypen(),
                    Messages.getString("ReverseRelationshipAction.couldNotReverseRelationship"), ex); //$NON-NLS-1$
        } finally {
            getPlaypen().endCompoundEdit("Reverse Relationship"); //$NON-NLS-1$
        }
    }
}
