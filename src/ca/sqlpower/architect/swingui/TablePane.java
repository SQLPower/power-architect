package ca.sqlpower.architect.swingui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import ca.sqlpower.architect.*;

public class TablePane extends JComponent implements SQLObjectListener, java.io.Serializable {

	/**
	 * How many pixels should be left between the surrounding box and
	 * the column name labels.
	 */
	protected Insets margin = new Insets(1,1,1,1);

	static {
		UIManager.put(TablePaneUI.UI_CLASS_ID, "ca.sqlpower.architect.swingui.BasicTablePaneUI");
	}

	private SQLTable model;

	public TablePane(SQLTable m) {
		setModel(m);
		setMinimumSize(new Dimension(100,200));
		setPreferredSize(new Dimension(100,200));
		updateUI();
	}

	public void setUI(TablePaneUI ui) {super.setUI(ui);}

    public void updateUI() {
		setUI((TablePaneUI)UIManager.getUI(this));
		invalidate();
    }

    public String getUIClassID() {
        return TablePaneUI.UI_CLASS_ID;
    }

	// -------------------- sqlobject event support ---------------------

	/**
	 * Listens for property changes in the model (columns
	 * added).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbChildrenInserted(SQLObjectEvent e) {
		firePropertyChange("model.children", null, null);
		revalidate();
	}

	/**
	 * Listens for property changes in the model (columns
	 * removed).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbChildrenRemoved(SQLObjectEvent e) {
		firePropertyChange("model.children", null, null);
		revalidate();
	}

	/**
	 * Listens for property changes in the model (columns
	 * properties modified).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbObjectChanged(SQLObjectEvent e) {
		firePropertyChange("model."+e.getPropertyName(), null, null);
		revalidate();
	}

	/**
	 * Listens for property changes in the model (significant
	 * structure change).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbStructureChanged(SQLObjectEvent e) {
		firePropertyChange("model.children", null, null);
		revalidate();
	}

	// ----------------------- accessors and mutators --------------------------
	
	/**
	 * Gets the value of model
	 *
	 * @return the value of model
	 */
	public SQLTable getModel()  {
		return this.model;
	}

	/**
	 * Sets the value of model, removing this TablePane as a listener
	 * on the old model and installing it as a listener to the new
	 * model.
	 *
	 * @param argModel Value to assign to this.model
	 */
	public void setModel(SQLTable m) {
		SQLTable old = model;
        if (old != null) {
			old.removeSQLObjectListener(this);
		}

        if (m == null) {
			throw new IllegalArgumentException("model may not be null");
		} else {
            model = m;
		}
		model.addSQLObjectListener(this);

        firePropertyChange("model", old, model);
	}

	/**
	 * Gets the value of margin
	 *
	 * @return the value of margin
	 */
	public Insets getMargin()  {
		return this.margin;
	}

	/**
	 * Sets the value of margin
	 *
	 * @param argMargin Value to assign to this.margin
	 */
	public void setMargin(Insets argMargin) {
		Insets old = margin;
		this.margin = (Insets) argMargin.clone();
		firePropertyChange("margin", old, margin);
		revalidate();
	}
}
