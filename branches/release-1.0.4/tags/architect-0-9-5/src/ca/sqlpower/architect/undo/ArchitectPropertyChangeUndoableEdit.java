package ca.sqlpower.architect.undo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObjectEvent;

/**
 * Represents an undoable property change operation on a SQL Object.
 */
public class ArchitectPropertyChangeUndoableEdit extends AbstractUndoableEdit {
	private static final Logger logger = Logger.getLogger(ArchitectPropertyChangeUndoableEdit.class);

	private SQLObjectEvent event;
	private String toolTip;
	
	public ArchitectPropertyChangeUndoableEdit(SQLObjectEvent e) {
        if (e == null) throw new NullPointerException("Null event is not allowed");
		event = e;
		toolTip = createToolTip();
	}
	
	private String createToolTip() {
	    return "Set " + event.getPropertyName() + " to " + event.getNewValue();
    }
	
	@Override
	public void redo() throws CannotRedoException {
		try {
            event.getSQLSource().setMagicEnabled(false);
		    modifyProperty(event.getNewValue());
		} catch (IllegalAccessException e) {
			logger.error("Couldn't access setter for "+
					event.getPropertyName(), e);
			throw new CannotRedoException();
		} catch (InvocationTargetException e) {
			logger.error("Setter for "+event.getPropertyName()+
					" on "+event.getSource()+" threw exception", e);
			throw new CannotRedoException();
		} catch (IntrospectionException e) {
			logger.error("Couldn't introspect source object "+
					event.getSource(), e);
			throw new CannotRedoException();
		} finally {
            event.getSQLSource().setMagicEnabled(true);
        }
		super.redo();
	}
	
	@Override
	public void undo() throws CannotUndoException {
		try {
            event.getSQLSource().setMagicEnabled(false);
		    modifyProperty(event.getOldValue());
		} catch (IllegalAccessException e) {
			logger.error("Couldn't access setter for "+
					event.getPropertyName(), e);
			throw new CannotUndoException();
		} catch (InvocationTargetException e) {
			logger.error("Setter for "+event.getPropertyName()+
					" on "+event.getSource()+" threw exception", e);
			throw new CannotUndoException();
		} catch (IntrospectionException e) {
			logger.error("Couldn't introspect source object "+
					event.getSource(), e);
			throw new CannotUndoException();
		} finally {
            event.getSQLSource().setMagicEnabled(true);
        }
		super.undo();
	}
	
	private void modifyProperty(Object value) throws IntrospectionException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        // We did this using BeanUtils.copyProperty() before, but the error
        // messages were too vague.
        BeanInfo info = Introspector.getBeanInfo(event.getSource().getClass());

        PropertyDescriptor[] props = info.getPropertyDescriptors();
        for (PropertyDescriptor prop : Arrays.asList(props)) {
            if (prop.getName().equals(event.getPropertyName())) {
                Method writeMethod = prop.getWriteMethod();
                if (writeMethod != null) {
                    writeMethod.invoke(event.getSource(), new Object[] { value });
                }
            }
        }
    }
	
	@Override
	public String getPresentationName() {
		return toolTip;
	}
	
	@Override
	public String toString() {
		return event.getSource() + "."+event.getPropertyName()
        +" changed from ["+event.getOldValue()
        +"] to ["+ event.getNewValue() + "]";
	}
}
