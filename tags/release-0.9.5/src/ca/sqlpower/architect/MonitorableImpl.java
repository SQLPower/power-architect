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
package ca.sqlpower.architect;

/**
 * The simplest possible Monitorable implementation. It simply starts with
 * reasonable defaults, which you can modify as much as you want. The getter
 * methods simply return the values you gave to the setter methods.
 * 
 * <p>
 * In order to guarantee reliable communication between threads using this
 * object, all methods are declared as synchronized.
 * 
 * <p>
 * This class will not be the best choice of Monitorable in many cases, but it
 * is certainly useful in cases where the work being carried out is shared
 * between several classes. They can pass an instance of this class between
 * them, and the ProgressWatcher will still have a single Monitorable to poll
 * for progress.
 * 
 * <p>The "Reasonable" defaults are:
 * <ul>
 *  <li>progress = 0
 *  <li>jobSize = null (means not yet determined)
 *  <li>message = null
 *  <li>started = false
 *  <li>cancelled = false
 *  <li>finished = false
 * </ul>
 */
public class MonitorableImpl implements Monitorable {

    private int progress = 0;
    private Integer jobSize = null;
    private String message = null;
    private boolean started = false;
    private boolean cancelled = false;
    private boolean finished = false;
    
    public synchronized boolean isCancelled() {
        return cancelled;
    }
    
    public synchronized void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public synchronized Integer getJobSize() {
        return jobSize;
    }
    
    public synchronized void setJobSize(Integer jobSize) {
        this.jobSize = jobSize;
    }
    
    public synchronized String getMessage() {
        return message;
    }
    
    public synchronized void setMessage(String message) {
        this.message = message;
    }
    
    public synchronized int getProgress() {
        return progress;
    }
    
    public synchronized void setProgress(int progress) {
        this.progress = progress;
    }
    
    public synchronized boolean hasStarted() {
        return started;
    }
    
    public synchronized void setStarted(boolean started) {
        this.started = started;
    }
    
    public synchronized boolean isFinished() {
        return finished;
    }
    
    public synchronized void setFinished(boolean finished) {
        this.finished = finished;
    }

    public synchronized void incrementProgress() {
        this.progress++;
    }
    
    public String toString() {
        return String.format("Job size: %4d " +
                                 "Progress: %4d " +
                                 "Started: %b " +
                                 "Finished: %b " +
                                 "Cancelled: %b " +
                                 "Message: %s ",
                                 jobSize, 
                                 progress, 
                                 started, 
                                 finished, 
                                 cancelled, 
                                 message);
    }
}
