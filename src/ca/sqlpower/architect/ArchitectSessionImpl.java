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


import ca.sqlpower.architect.profile.TableProfileManager;

/**
 * The ArchitectSession class represents a single user's session with
 * the architect.  If using the Swing UI (currently this is the only
 * option, but that is subject to change), the ArchitectFrame has a
 * 1:1 relationship with an ArchitectSession.
 *
 * @version $Id$
 * @author fuerth
 */
public class ArchitectSessionImpl implements ArchitectSession {
    
    protected static ArchitectSession instance;
	protected CoreUserSettings userSettings;
    private TableProfileManager profileManager;

	public ArchitectSessionImpl() {
        profileManager = new TableProfileManager();
	}

	/**
	 * Gets the single ArchitectSession instance for this JVM.
	 *
	 * <p>Note: in the future, the ArchitectSession may no longer be a
	 * singleton (for example, if the Architect gets a servlet or RMI
	 * interface).  In that case, getInstance will necessarily change
	 * or disappear.
	 */
	public static synchronized ArchitectSession getInstance() {
		if (instance == null) {
			instance = new ArchitectSessionImpl();
		}
		return instance;
	}

	// --------------- accessors and mutators ------------------

	/* (non-Javadoc)
     * @see ca.sqlpower.architect.ArchitectSession#getUserSettings()
     */
	public CoreUserSettings getUserSettings()  {
		return this.userSettings;
	}

	/* (non-Javadoc)
     * @see ca.sqlpower.architect.ArchitectSession#setUserSettings(ca.sqlpower.architect.CoreUserSettings)
     */
	public void setUserSettings(CoreUserSettings argUserSettings) {
		this.userSettings = argUserSettings;
	}

    public TableProfileManager getProfileManager() {
        return profileManager;
    }
}
