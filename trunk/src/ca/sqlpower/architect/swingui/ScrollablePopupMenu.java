/*
 * Copyright (c) 2007, SQL Power Group Inc.
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
/*
 * Created on Jul 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect.swingui;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataEvent;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
 
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
