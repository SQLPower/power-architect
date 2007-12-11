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

package ca.sqlpower.architect.profile;

import org.apache.log4j.Logger;

import ca.sqlpower.util.MonitorableImpl;

/**
 * Provides a template method for doProfile which alleviates the
 * need to write boilerplate code for every profile creator implementation.
 */
public abstract class AbstractTableProfileCreator implements TableProfileCreator {
    
    private static final Logger logger = Logger.getLogger(AbstractTableProfileCreator.class);
    
    /**
     * A generic template for populating a profile result.  Calls {@link #doProfileImpl()}
     * to perform the actual work of populating this profile result.
     * <p>
     * This method will fire a profileStarted event before calling the subclass's
     * doProfile, then fire a profileFinished event after doProfile exits (with
     * or without success) unless this profile population has been cancelled,
     * in which case it fires a profileCancelled event.
     */
    public final boolean doProfile(TableProfileResult tpr) {
        MonitorableImpl pm = (MonitorableImpl) tpr.getProgressMonitor();
        try {
            tpr.fireProfileStarted();
            pm.setMessage(tpr.getProfiledObject().getName());
            if (!pm.isCancelled()) {
                pm.setStarted(true);
                pm.setFinished(false);
                tpr.setCreateStartTime(System.currentTimeMillis());
                doProfileImpl(tpr);
            }
        } catch (Exception ex) {
            tpr.setException(ex);
            logger.error("Profile failed. Saving exception:", ex);
        } finally {
            tpr.setCreateEndTime(System.currentTimeMillis());
            pm.setProgress(pm.getProgress() + 1);
            pm.setFinished(true);
            if (pm.isCancelled()) {
                tpr.fireProfileCancelled();
            } else {
                tpr.fireProfileFinished();
            }
        }
        return !pm.isCancelled();
    }

    protected abstract boolean doProfileImpl(TableProfileResult tpr);

}
