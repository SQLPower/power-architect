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

import org.apache.log4j.Logger;

import ca.sqlpower.object.AbstractPoolingSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.sqlobject.SQLObject;

public class InsertionPointWatcher<T extends SQLObject> {

    private static final Logger logger = Logger.getLogger(InsertionPointWatcher.class);
    
    private final T objectUnderObservation;
    
    private int insertionPoint;
    
    private final Class<? extends SQLObject> childType;

    private final SQLObjectEventHandler eventHandler = new SQLObjectEventHandler();
    
    /**
     * @param objectUnderObservation
     * @param insertionPoint
     */
    public InsertionPointWatcher(final T objectUnderObservation, int insertionPoint) {
        this(objectUnderObservation, insertionPoint, SQLObject.class);
    }
    
    public InsertionPointWatcher(final T objectUnderObservation, int insertionPoint, Class<? extends SQLObject> childType) {
        super();
        this.objectUnderObservation = objectUnderObservation;
        this.insertionPoint = insertionPoint;
        this.childType = childType;
        objectUnderObservation.addSPListener(eventHandler);
    }

    public int getInsertionPoint() {
        return insertionPoint;
    }

    public T getObjectUnderObservation() {
        return objectUnderObservation;
    }
    
    private class SQLObjectEventHandler extends AbstractPoolingSPListener {

        @Override
        public void childAddedImpl(SPChildEvent e) {
            if (e.getChildType() == childType && e.getIndex() <= insertionPoint) {
                insertionPoint++;
            }
        }

        @Override
        public void childRemovedImpl(SPChildEvent e) {
            if (e.getChildType() == childType && e.getIndex() < insertionPoint) {
                insertionPoint--;
            }
        }

    }

    /**
     * Removes listener(s) installed in the SQLObject model. To prevent resource leaks,
     * you must be sure to call this method when you are finished with this instance of
     * InsertionPointWatcher.
     */
    public void dispose() {
        objectUnderObservation.removeSPListener(eventHandler);
    }
}
