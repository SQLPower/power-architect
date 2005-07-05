/*
 * Created on Jul 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect.swingui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
 
public class ScrollablePopupMenu extends JComboBox {
   public ScrollablePopupMenu(ComboBoxModel aModel) {
	super(aModel);
	setup();
   }
   public ScrollablePopupMenu(final Object items[]) {
	super(items);
	setup();
   }
   public ScrollablePopupMenu(Vector items) {
	super(items);
	setup();
   }
   boolean keySelection=false;
   myBasicComboBoxUI myCBUI;
   private void setup() {
	myCBUI=new myBasicComboBoxUI();
	setUI(myCBUI);
	addKeyListener(new KeyAdapter() {
	   public void keyPressed(KeyEvent ke) {
		if (ke.getKeyCode()==10) {
		   keySelection=false;
		   fireActionEvent();
		   return;
		} else if (!myCBUI.getPopup().isVisible()) {
		   ke.consume();
		}
		keySelection=true;
	   }
	});
	setPreferredSize(new Dimension(0,0));
	setVisible(true);	
   }
   public void showPopup(int x, int y) {
	setLocation(x,y);
	myCBUI.getPopup().show();
   }
   public ComboPopup getPopup() {
	return myCBUI.getPopup();
   }
   public void setSelectedItem(Object anObject) {
	Object oldSelection = selectedItemReminder;
	if (oldSelection == null || !oldSelection.equals(anObject)) {
	   if (anObject != null && !isEditable()) {
		boolean found = false;
		for (int i = 0; i < dataModel.getSize(); i++) {
		   if (anObject.equals(dataModel.getElementAt(i))) {
			found = true;
			break;
		   }
		}
		if (!found) return;
	   }
	   dataModel.setSelectedItem(anObject);
	   if (selectedItemReminder != dataModel.getSelectedItem()) selectedItemChanged();
	}
   }
   public void contentsChanged(ListDataEvent e) {
	Object oldSelection = selectedItemReminder;
	Object newSelection = dataModel.getSelectedItem();
	if (oldSelection == null || !oldSelection.equals(newSelection)) selectedItemChanged();
	if (!keySelection) fireActionEvent();
   }
   class myBasicComboBoxUI extends BasicComboBoxUI {			// just to be able to get to the popup
	public ComboPopup getPopup() {
	   return popup;
	}
   }
}
