/*
 * Created on Aug 24, 2005
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

/**
 * SearchReplace is a GUI facility for searching for named items in the
 * SQlObject hierarchy, and optionally renaming them.
 *
 * @author fuerth
 * @version $Id$
 */
public class SearchReplace {
    private static final Logger logger = Logger.getLogger(SearchReplace.class);

    /**
     * The SearchResultsTableModel is an interface between a list of SQLObjects and the JTable component.
     *
     * @author fuerth
     * @version $Id$
     */
    private class SearchResultsTableModel implements TableModel {

        private List results;
        
        public SearchResultsTableModel(List results) {
            this.results = results;
        }

        public int getRowCount() {
            return results.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return "Type";
            } else if (columnIndex == 1) {
                return "Name";
            } else {
                return "Invalid column index: "+columnIndex;
            }
        }

        public Class getColumnClass(int columnIndex) {
            return String.class;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            SQLObject obj = (SQLObject) results.get(rowIndex);
            return columnIndex == 1 && (obj instanceof SQLTable || obj instanceof SQLColumn || obj instanceof SQLRelationship);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            SQLObject obj = (SQLObject) results.get(rowIndex);
            if (columnIndex == 0) {
                if (obj instanceof SQLColumn) {
                    return "Column of "+((SQLColumn) obj).getParentTable().getName();
                } else {
                    String className = obj.getClass().getName();
                    return className.substring(className.lastIndexOf('.') + 4);  // the +4 is to skip over ".SQL"
                }
            } else if (columnIndex == 1) {
                return obj.getName();
            } else {
                return "Invalid column: "+columnIndex;
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            SQLObject obj = (SQLObject) results.get(rowIndex);
            if (columnIndex == 1) {
                if (obj instanceof SQLTable) {
                    ((SQLTable) obj).setName((String) aValue);
                } else if (obj instanceof SQLColumn) {
                    ((SQLColumn) obj).setName((String) aValue);
                } else if (obj instanceof SQLRelationship) {
                    ((SQLRelationship) obj).setName((String) aValue);
                }
                fireEvent(new TableModelEvent(this, rowIndex, rowIndex, columnIndex, TableModelEvent.UPDATE));
            }
        }

        private ArrayList listeners = new ArrayList();
        
        public void addTableModelListener(TableModelListener l) {
            listeners.add(l);
        }

        public void removeTableModelListener(TableModelListener l) {
            listeners.remove(l);
        }

        private void fireEvent(TableModelEvent evt) {
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                ((TableModelListener) it.next()).tableChanged(evt);
            }
        }
    }
    private JRadioButton substringMatch;
    private JRadioButton exactMatch;
    private JRadioButton regexMatch;
    
    private JRadioButton tableSearch;
    private JRadioButton relationshipSearch;
    private JRadioButton columnSearch;
    private JRadioButton allSearch;
    
    private JTextField searchExpression;
    private JCheckBox caseInsensitive;

    public void showSearchDialog(final PlayPen pp) {
    	// XXX need to convert to an ArchitectPanel before switching
    	// this to use ArchitectPanelBuilder.
        final JDialog d = new JDialog((Frame) SwingUtilities.getAncestorOfClass(JFrame.class, pp), "Find");

        ButtonGroup matchType = new ButtonGroup();
        matchType.add(substringMatch = new JRadioButton("Substring"));
        matchType.add(exactMatch = new JRadioButton("Exact Match"));
        matchType.add(regexMatch = new JRadioButton("Regex"));
        JPanel matchTypePanel = new JPanel(new GridLayout(1,3));
        matchTypePanel.add(substringMatch);
        matchTypePanel.add(exactMatch);
        matchTypePanel.add(regexMatch);
        substringMatch.setSelected(true);
        
        ButtonGroup searchType = new ButtonGroup();
        searchType.add(tableSearch = new JRadioButton("Tables"));
        searchType.add(relationshipSearch = new JRadioButton("Relationships"));
        searchType.add(columnSearch = new JRadioButton("Columns"));
        searchType.add(allSearch = new JRadioButton("Anything"));
        JPanel searchTypePanel = new JPanel(new GridLayout(4,1));
        searchTypePanel.add(tableSearch);
        searchTypePanel.add(relationshipSearch);
        searchTypePanel.add(columnSearch);
        searchTypePanel.add(allSearch);
        allSearch.setSelected(true);
        
        caseInsensitive = new JCheckBox("Ignoring case");
        caseInsensitive.setSelected(true);
        
        searchExpression = new JTextField();
        
        JButton searchButton = new JButton("Search");
        // searchButton.setDefaultCapable(true);
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    showResults(d, pp);
                } catch (ArchitectException ex) {
                    ASUtils.showExceptionDialog("Encountered a problem during search", ex);
                }
            }
        });

        Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                d.setVisible(false);
                d.dispose();
            }
        };
        cancelAction.putValue(Action.NAME, ArchitectPanelBuilder.CANCEL_BUTTON_LABEL);
        JButton cancelButton = new JButton(cancelAction);
        
        ArchitectPanelBuilder.makeJDialogCancellable(d, cancelAction);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(searchButton);
        buttonPanel.add(cancelButton);

        JComponent cp = (JComponent) d.getContentPane();
        cp.setLayout(new FormLayout(10, 10));
        cp.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        cp.add(new JLabel("Look for"));
        cp.add(searchTypePanel);
        
        cp.add(new JLabel("Named"));
        cp.add(searchExpression);
        
        cp.add(new JLabel("Comparing by"));
        cp.add(matchTypePanel);
        
        cp.add(new JLabel(""));
        cp.add(caseInsensitive);

        cp.add(new JLabel(""));
        cp.add(buttonPanel);
        
        d.getRootPane().setDefaultButton(searchButton);

        d.pack();
        d.setLocationRelativeTo(pp);
        d.setVisible(true);
        searchExpression.requestFocus();
    }
    
    public void showResults(JDialog parent, final PlayPen pp) throws ArchitectException {
    	try {
	        final List results = doSearch(pp.getDatabase());
	        
	        // XXX This JDialog has three buttons so we cannot use
	        // ArchitectPanelBuilder to create it...
	        final JDialog d = new JDialog(parent, "Search Results");
	        final JTable t = new JTable(new SearchResultsTableModel(results));
	        
	        final JButton renameButton = new JButton("Rename Selected...");
	        renameButton.setEnabled(false);
	        renameButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	String newName;
	         	    newName = JOptionPane.showInputDialog(d, "Enter the new name");
	                TableModel m = t.getModel();
	                
	                int selectedRows[] = t.getSelectedRows();
	                for (int i = 0; i < selectedRows.length; i++) {
	                	//newName is null if the user press cancel
	                	if (newName!=null){
	                		// update using the table model because we want the results page to be notified of changes
	                		m.setValueAt(newName, selectedRows[i], 1);
	                	}
	                }
	            }
	        });
	        
	        final JButton gotoButton = new JButton("Show in Playpen");
	        gotoButton.setEnabled(false);
	        gotoButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                int row = t.getSelectedRow();
	                if (row >= 0) {
	                    SQLObject searchObj = (SQLObject) results.get(row);
	                    SQLTable searchTable = null;
	                    SQLColumn searchColumn = null;
	                    SQLRelationship searchRelationship = null;
	                    if (searchObj instanceof SQLColumn) {
	                        searchColumn = (SQLColumn) searchObj;
	                        searchTable = searchColumn.getParentTable();
	                    } else if (searchObj instanceof SQLTable) {
	                        searchTable = (SQLTable) searchObj;
	                    } else if (searchObj instanceof SQLRelationship) {
	                        searchRelationship = (SQLRelationship) searchObj;
	                    } else {
	                        JOptionPane.showMessageDialog(null, "Sorry, the selected search result is of an unknown type!");
	                        return;
	                    }
	                    
	                    if (searchTable != null) {
	                        TablePane tp = pp.findTablePane(searchTable);
	                        if (tp != null) {
	                            pp.selectNone();
	                            tp.setSelected(true);
	                            pp.scrollRectToVisible(tp.getBounds());
	                            
	                            if (searchColumn != null) {
	                            	try {
	                                tp.selectColumn(searchTable.getColumnIndex(searchColumn));
	                            	} catch (ArchitectException ex) {
	                            		logger.error("Failed to select column becuase getColumnIndex" +
	                            				" threw the following exception:", ex);
	                            		JOptionPane.showMessageDialog(
	                            				null, "Sorry, couldn't select the column you asked for." +
	                            						"\n\nMore information is available in the " +
	                            						"application log.");
	                            	}
	                            }
	                        }
	                    } else if (searchRelationship != null) {
	                        Relationship r = pp.findRelationship(searchRelationship);
	                        if (r != null) {
	                            pp.selectNone();
	                            r.setSelected(true);
	                            pp.scrollRectToVisible(r.getBounds());
	                        }
	                    }
	                }
	            }
	        });
	
	        Action closeAction = new CommonCloseAction(d);
	        JButton closeButton = new JButton(closeAction);
	        ArchitectPanelBuilder.makeJDialogCancellable(d, closeAction);
	        
	        ListSelectionListener buttonActivator = new ListSelectionListener() {
	            public void valueChanged(ListSelectionEvent e) {
	                renameButton.setEnabled(t.getSelectedRowCount() > 0);
	                gotoButton.setEnabled(t.getSelectedRowCount() == 1);
	            }
	        };
	        t.getSelectionModel().addListSelectionListener(buttonActivator);
	
	        JComponent cp = (JComponent) d.getContentPane();
	        cp.setLayout(new BorderLayout());
	        cp.add(new JScrollPane(t), BorderLayout.CENTER);
	        
	        Box buttonBox = new Box(BoxLayout.Y_AXIS);
	        buttonBox.add(renameButton);
	        buttonBox.add(gotoButton);
	        buttonBox.add(Box.createVerticalGlue());
	        buttonBox.add(closeButton);
	        cp.add(buttonBox, BorderLayout.EAST);
	        
	        d.pack();
	        d.setLocationRelativeTo(parent);
	        d.setVisible(true);
    	 }
        catch(PatternSyntaxException e){
        	
        	JOptionPane.showMessageDialog(null,e.getDescription(),"Regular Expression Error",
        			JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public List doSearch(SQLObject start) throws ArchitectException {
        List results = new ArrayList();
        String pat;
        if (substringMatch.isSelected() || exactMatch.isSelected()) {
            String p = searchExpression.getText();
            StringBuffer escapedPat = new StringBuffer();
            for (int i = 0; i < p.length(); i++) {
                if (! (Character.isLetterOrDigit(p.charAt(i)) || Character.isSpaceChar(p.charAt(i)))) {
                    escapedPat.append("\\");
                }
                escapedPat.append(p.charAt(i));
            }
            if (exactMatch.isSelected()) {
                pat = "^"+escapedPat+"$";
            } else {
                pat = ".*"+escapedPat+".*";
            }
        } else if (regexMatch.isSelected()) {
            pat = searchExpression.getText();
        } else {
            throw new IllegalStateException("Search mode is not substring, exact, or regex.  Don't know what to do.");
        }
        int patternFlags = 0;
        if (caseInsensitive.isSelected()) {
            patternFlags |= (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }
      
        Pattern searchPattern = Pattern.compile(pat, patternFlags);  
        return recursiveSearch(start, searchPattern, results);
    }

    private List recursiveSearch(SQLObject obj, Pattern searchPattern, List appendTo) throws ArchitectException {
        if (logger.isDebugEnabled()) logger.debug("Matching \""+obj.getName()+"\" against /"+searchPattern.pattern()+"/");
        if (searchPattern.matcher(obj.getName()).matches() && searchTypeMatches(obj)) {
            appendTo.add(obj);
        }
        List children = obj.getChildren();
        if (children != null) {
            Iterator it = children.iterator();
            while (it.hasNext()) {
                SQLObject so = (SQLObject) it.next();
                recursiveSearch(so, searchPattern, appendTo);
            }
        }
        return appendTo;
    }

    /**
     * Determines if the given object is of a type that the user has asked us to look for.
     * @param obj
     * @return
     */
    private boolean searchTypeMatches(SQLObject obj) {
        if (allSearch.isSelected() && !(obj instanceof SQLTable.Folder)) return true;
        if (tableSearch.isSelected() && obj instanceof SQLTable) return true;
        if (columnSearch.isSelected() && obj instanceof SQLColumn) return true;
        if (relationshipSearch.isSelected() && obj instanceof SQLRelationship) return true;
        return false;
    }

	public String getSearchExpressionText() {
		return searchExpression.getText();
	}

	public void setSearchExpression(String searchExpressionText) {
		searchExpression.setText(searchExpressionText);
	}
}
