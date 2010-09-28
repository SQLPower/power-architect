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
package ca.sqlpower.architect.swingui.event;

import java.util.EventObject;

import ca.sqlpower.architect.swingui.Selectable;

public class SelectionEvent extends EventObject {

	public static final int SELECTION_EVENT = 1;
	public static final int DESELECTION_EVENT = 2;

    public static final int SINGLE_SELECT=4;
    public static final int CTRL_MULTISELECT=8;
    public static final int SHIFT_MULTISELECT=16;
    
    
	protected int eventType;
	private final int multiselectType;
    
	public SelectionEvent(Selectable source, int eventType, int multiselect) {
		super(source);
		this.eventType = eventType;
        this.multiselectType = multiselect;
	}

	public Selectable getSelectableSource() {
		return (Selectable) getSource();
	}
	
	public String toString() {
		return "[SelectionEvent: type="+(eventType==SELECTION_EVENT?"selection":eventType==DESELECTION_EVENT?"deselection":("unknown code "+eventType))+", source="+getSource()+"]";
	}

	/**
	 * Returns the event type (SELECTION_EVENT or DESELECTION_EVENT).
	 */
	public int getType() {
		return eventType;
	}

	/**
	 * returns the multiselect type (SINGLE_SELECT,CTRL_MULTISELECT,SHIFT_MULTISELECT)
	 */
    public int getMultiselectType() {
        return multiselectType;
    }  
}
