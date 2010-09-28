/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.architect;

import java.io.Serializable;

/**
 * Event object associated with the SQLObject pre-event system. Presently,
 * this event class does not support property change type events; this
 * will be added in the future if the need arises.
 */
public class SQLObjectPreEvent implements Serializable {
    
    private final SQLObject source;
    private final int[] changeIndices;
    private final SQLObject[] children;
    private boolean vetoed;
    
    /**
     * Use this constructor for DBChildrenInserted and
     * DBChildrenRemoved type events.  <code>propertyName</code> will be set to the
     * string "children".
     *
     * @param source The SQLObject that may undergo a change.
     * @param changeIndices The indices of the children that might be
     * added or removed.  The indices must be in ascending order.
     * @param children The actual SQLObject instances that might be added
     * or removed to/from the source object.
     */
    public SQLObjectPreEvent(SQLObject source, int[] changeIndices, SQLObject[] children) {
        this.source = source;
        this.changeIndices = changeIndices;
        this.children = children;
    }

    /**
     * Returns the child indices that may be added/removed. Do not modify
     * the returned array.
     */
    public int[] getChangeIndices() {
        return changeIndices;
    }

    /**
     * Returns the child objects that may be added/removed. Do not modify
     * the returned array.
     */
    public SQLObject[] getChildren() {
        return children;
    }

    /**
     * Returns the source object of this event. In the case of an add/remove
     * notification, this is the parent object that is imminently gaining or
     * losing children.
     */
    public SQLObject getSource() {
        return source;
    }

    /**
     * Sets the vetoed state of this event to true. This means that, once all
     * pre-event listeners have been notified of the impending change, the
     * source object will abort the operation it was about to carry out.  If none
     * of the listeners veto this event, it the source object should proceed
     * with the action (which will cause a corresponding SQLObject event to be
     * fired announcing that the change has actually taken place).
     */
    public void veto() {
        vetoed = true;
    }
    
    /**
     * Returns whether or not this event has been vetoed yet. In general, it is
     * only useful for the source SQLObject (the one firing the event) to check
     * this, and only then after all listeners have been notified. The reason
     * is, listeners have no way of knowing how many other listeners there are
     * for this event, or what order those listeners will be notified in. This
     * means that if a listener checks the vetoed state of the event, it is
     * likely to get a false negative response.
     */
    public boolean isVetoed() {
        return vetoed;
    }
}
