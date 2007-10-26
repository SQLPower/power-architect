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
package ca.sqlpower.architect.etl;

import ca.sqlpower.architect.AbstractUserSetting;

/**
 * The ETLUserSettings class stores project-independent ETL settings
 * that may differ from client machine to client machine, but not
 * project-to-project.
 *
 * @see ca.sqlpower.architect.CoreUserSettings
 * @see ca.sqlpower.architect.swingui.ArchitectSwingUserSettings
 */
public class ETLUserSettings extends AbstractUserSetting {

	// ------ PROPERTY LIST KEYS ------
	
	public static final String PROP_PL_ENGINE_PATH
		= "ETLUserSettings.PROP_PL_ENGINE_PATH";
	
	public static final String PROP_ETL_LOG_PATH
		= "ETLUserSettings.PROP_ETL_LOG_PATH";

	
	// ------ CONSTRUCTORS/FACTORIES ------

	/**
	 * Creates a user settings instance initialised to the default
	 * values.
	 */
	public ETLUserSettings() {
		super();
	}
}
