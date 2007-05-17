package ca.sqlpower.validation.swingui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import ca.sqlpower.validation.RegExValidator;

import junit.framework.TestCase;

public class TestFormValidationHandler extends TestCase {

    private FormValidationHandler handler;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        handler = new FormValidationHandler(new StatusComponent());
    }

    protected class Counter {
        int count = 0;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
    public void testEventSupport() throws BadLocationException {
        Integer count = new Integer(0);
        RegExValidator val = new RegExValidator("\\d+");
        final JTextField textField = new JTextField();
        final Counter counter = new Counter();

        final PropertyChangeListener listener = new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt) {
                counter.setCount(counter.getCount()+1);
            }};
        handler.addPropertyChangeListener(listener);
        handler.addValidateObject(textField,val);

        // TextField firs 2 events for setText: clearing then inserting,
        // when the file needs clear or insert new string
        assertEquals("There should be no events", 0, counter.getCount());

        // no need for clear
        textField.setText("10");
        assertEquals("event counter should be 1", 1, counter.getCount());

        // clear and insert
        textField.setText("20");
        assertEquals("event counter should be 3", 3, counter.getCount());

        // just clear
        textField.setText("");
        assertEquals("event counter should be 4", 4, counter.getCount());

        // just insert
        textField.setText("a");
        assertEquals("event counter should be 5", 5, counter.getCount());

        handler.removePropertyChangeListener(listener);
        textField.getDocument().insertString(textField.getDocument().getLength(),"aa",null);
        assertEquals("event counter should be remain 5", 5, counter.getCount());
    }
}
