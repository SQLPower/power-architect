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
package ca.sqlpower.architect.qfa;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.ArchitectVersion;
/**
 * Creates Exception reports that are setup for the architect application 
 */
public class ArchitectExceptionReportFactory implements QFAFactory {
    private static final Logger logger = Logger.getLogger(ArchitectExceptionReportFactory.class);
    
    /**
     * The system property that controls which URL error reports go to.
     * The default (used when this property is not defined) is
     * the value of <tt>DEFAULT_REPORT_URL</tt>.
     */
    private static final String REPORT_URL_SYSTEM_PROP = "ca.sqlpower.architect.qfa.REPORT_URL";

    /**
     * The URL to post the error report to if the system property
     * that overrides it isn't defined.
     */
    private static final String DEFAULT_REPORT_URL = "http://bugs.sqlpower.ca/architect/postReport";
    
    public ExceptionReport createExceptionReport(Throwable exception) {
        ExceptionReport er = new ExceptionReport(
                exception, REPORT_URL_SYSTEM_PROP,
                DEFAULT_REPORT_URL, ArchitectVersion.APP_VERSION, 
                ArchitectUtils.getAppUptime());
        return er;
    }

}
