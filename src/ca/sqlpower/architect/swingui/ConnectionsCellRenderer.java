package ca.sqlpower.architect.swingui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import ca.sqlpower.architect.ArchitectDataSource;

public class ConnectionsCellRenderer extends JLabel implements ListCellRenderer {
	
    protected static Border noFocusBorder;

    /**
     * Constructs a default renderer object for an item
     * in a list.
     */
    public ConnectionsCellRenderer() {
	super();
        if (noFocusBorder == null) {
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }
	setOpaque(true);
	setBorder(noFocusBorder);
    }
	
    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus)
    {
        setComponentOrientation(list.getComponentOrientation());
    	if (isSelected) {
    	    setBackground(list.getSelectionBackground());
    	    setForeground(list.getSelectionForeground());
    	}
    	else {
    	    setBackground(list.getBackground());
    	    setForeground(list.getForeground());
    	}
    	if (value != null) {
    		ArchitectDataSource dataSource = (ArchitectDataSource) value;
    		setText(dataSource.get(ArchitectDataSource.PL_LOGICAL));
    	} else {
    		setText(null);
    	}

    	setEnabled(list.isEnabled());
    	setFont(list.getFont());
    	setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

        return this;        
    }
    
    public boolean isOpaque() { 
    	Color back = getBackground();
    	Component p = getParent(); 
    	if (p != null) { 
    	    p = p.getParent(); 
    	}
    	// p should now be the JList. 
    	boolean colorMatch = (back != null) && (p != null) && 
    	    back.equals(p.getBackground()) && 
    			p.isOpaque();
    	return !colorMatch && super.isOpaque(); 
    }
    
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    	// Strings get interned...
    	if (propertyName=="text") {
    	    super.firePropertyChange(propertyName, oldValue, newValue);
        }
	}
}