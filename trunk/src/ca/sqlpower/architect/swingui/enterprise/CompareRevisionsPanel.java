/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.architect.swingui.enterprise;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;

import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.swingui.CompareDMFormatter;
import ca.sqlpower.diff.DiffChunk;
import ca.sqlpower.diff.DiffInfo;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class sets up a panel with two (identical) lists of revisions on the server,
 * and displays the differences between two selected revisions below them.
 */
public class CompareRevisionsPanel {
    
    private final ArchitectClientSideSession session;

    private final RevisionsTable revisionsTableLeft;
    private final RevisionsTable revisionsTableRight;
    private final JTextPane comparePane;
    
    private final JPanel panel;

    private final Action refreshAction = new AbstractAction("Refresh...") {
        public void actionPerformed(ActionEvent e) {
            revisionsTableLeft.refreshRevisionsList();
            revisionsTableRight.refreshRevisionsList();
            refreshPanel();
        }
    };

    private final Action compareAction = new AbstractAction("Compare...") {
        public void actionPerformed(ActionEvent e) {
            int oldRevisionNo = revisionsTableLeft.getSelectedRevisionNumber();
            int newRevisionNo = revisionsTableRight.getSelectedRevisionNumber();
            
            DefaultStyledDocument resultDoc = new DefaultStyledDocument();
            try {
                if (oldRevisionNo >= 0 && newRevisionNo >= 0) {
                    List<DiffChunk<DiffInfo>> diff = session.getComparisonDiffChunks(oldRevisionNo, newRevisionNo);      
                    if (diff.size() == 0) {
                        resultDoc.insertString(0, "Revisions are identical", null);
                    } else {
                        resultDoc = CompareDMFormatter.generateEnglishDescription(CompareDMFormatter.DIFF_STYLES, diff);
                    }
                } else {
                    JOptionPane.showMessageDialog(panel, "A revision must be selected from each table.");
                }
            } catch (Throwable t) {
                throw new RuntimeException("Error making comparison", t);
            }
            
            comparePane.setStyledDocument(resultDoc);
        }
    };

    public CompareRevisionsPanel(ArchitectClientSideSession session, Action closeAction) {

        this.session = session;               

        revisionsTableLeft = new RevisionsTable(this.session);
        revisionsTableRight = new RevisionsTable(this.session);      
                
        MouseListener tableListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                refreshPanel();
            }
        };
        
        revisionsTableLeft.addMouseListener(tableListener);
        revisionsTableRight.addMouseListener(tableListener);
        
        JScrollPane revisionsPaneLeft = new JScrollPane(revisionsTableLeft);                
        JScrollPane revisionsPaneRight = new JScrollPane(revisionsTableRight);
        
        revisionsPaneLeft.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        revisionsPaneRight.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        revisionsPaneLeft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        revisionsPaneRight.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);       
        
        comparePane = new JTextPane();
        comparePane.setEditable(false);
        comparePane.setMargin(new Insets(6, 10, 4, 6));
        JScrollPane sp = new JScrollPane(comparePane);
        sp.setPreferredSize(revisionsPaneLeft.getPreferredSize());

        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder revisionListsBuilder = new DefaultFormBuilder(
                new FormLayout("default:grow, 5dlu, default:grow", "pref, 2dlu, default:grow"));
        revisionListsBuilder.add(new JLabel("From revision..."), cc.xy(1, 1));
        revisionListsBuilder.add(new JLabel("To revision..."), cc.xy(3, 1));
        revisionListsBuilder.add(revisionsPaneLeft, cc.xy(1, 3));
        revisionListsBuilder.add(revisionsPaneRight, cc.xy(3, 3));

        DefaultFormBuilder buttonBarBuilder = new DefaultFormBuilder(new FormLayout("pref"));      
        buttonBarBuilder.append(new JButton(refreshAction));
        buttonBarBuilder.append(new JButton(compareAction));
        buttonBarBuilder.append(new JButton(closeAction));
        
        DefaultFormBuilder bottomBuilder = new DefaultFormBuilder(
                new FormLayout("default:grow, right:default", "default:grow"));
        bottomBuilder.add(sp, cc.xy(1, 1));
        bottomBuilder.add(buttonBarBuilder.getPanel(), cc.xy(2, 1));
        
        DefaultFormBuilder builder = new DefaultFormBuilder(
                new FormLayout("default:grow", "default:grow, 5dlu, default:grow"));
        builder.add(revisionListsBuilder.getPanel(), cc.xy(1, 1));
        builder.add(bottomBuilder.getPanel(), cc.xy(1, 3));
        
        builder.setDefaultDialogBorder();
        panel = builder.getPanel();
        panel.setPreferredSize(new Dimension(900, 650));
        refreshPanel();

    }
    
    private void refreshPanel() {
        compareAction.setEnabled(
                revisionsTableLeft.getSelectedRow() != -1 &&
                revisionsTableRight.getSelectedRow() != -1);
    }

    public JPanel getPanel() {
        return panel;
    }

}
