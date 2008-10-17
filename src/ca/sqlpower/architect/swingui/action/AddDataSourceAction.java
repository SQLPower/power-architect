package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.sql.SPDataSource;

/**
 * When invoked, this action adds the data source that was given in the
 * constructor to the DBTree's model.  There is normally one
 * AddDataSourceAction associated with each item in the "Set Connection"
 * menu.
 */
public class AddDataSourceAction extends AbstractAction {
    
	/**
     * The tree to add the data source to. 
     */
    private final DBTree tree;
    
    /**
     * The data source to add to the tree.
     */
    private SPDataSource ds;

	public AddDataSourceAction(DBTree tree, SPDataSource ds) {
		super(ds.getName());
        this.tree = tree;
		this.ds = ds;
	}

	public void actionPerformed(ActionEvent e) {
		tree.addSourceConnection(ds);
	}

}