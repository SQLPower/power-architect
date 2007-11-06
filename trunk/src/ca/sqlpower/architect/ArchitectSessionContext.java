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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.prefs.Preferences;

/**
 * The ArchitectSessionContext interface specifies a set of
 * properties and methods for creating new Architect Sessions.
 * Additionally, the session context is the gateway to information
 * that is specific to the current user's environment (as opposed
 * to information that is attached to specific projects, which is
 * stored in the session).
 */
public interface ArchitectSessionContext {

    /**
     * The URL where there is more information about finding and configuring
     * JDBC drivers.
     */
    static final String DRIVERS_URL = "http://www.sqlpower.ca/forum/posts/list/401.page";
    
    /**
     * Creates a new session within this parent context. 
     * @throws ArchitectException 
     */
    public abstract ArchitectSession createSession() throws ArchitectException;
    
    /**
     * Creates a new session by loading the Architect XML project description
     * from the given input stream.
     * 
     * @param in The input stream to read the XML data from
     * @return The new session
     */
    public abstract ArchitectSession createSession(InputStream in) throws ArchitectException, IOException;

    /**
     * Returns the user preferences node associated with this context.
     */
    public abstract Preferences getPrefs();

    /**
     * Gets the user settings for this session 
     */
    public abstract CoreUserSettings getUserSettings();

    /**
     * Returns a collection containing all the sessions from this context. 
     */
    public Collection<ArchitectSession> getSessions();
}
