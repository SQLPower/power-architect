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
package prefs;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * A java.util.prefs.PreferencesFactory that lets us use the MemoryPreferences
 * so that tests will not affect (nor be affected by) preferences previously created
 * by the user running the tests.
 */
public class PreferencesFactory implements java.util.prefs.PreferencesFactory {

	public static final String PREFS_FACTORY_SYSTEM_PROPERTY = "java.util.prefs.PreferencesFactory";

	public static final String MY_CLASS_NAME = "prefs.PreferencesFactory";

	private static Logger logger = Logger.getLogger(PreferencesFactory.class);

	final static Map<String, Preferences> systemNodes = new HashMap<String, Preferences>();
	
	final Map<String, Preferences> userNodes = new HashMap<String, Preferences>();

	/**
	 * There is always only one System Root node
	 */
	final MemoryPreferences systemRoot = new MemoryPreferences(null, "");

	public Preferences systemRoot() {
		logger.debug("PreferencesFactory.systemRoot()");
		return systemRoot;
	}

	/**
	 * In this implementation there is only one UserRoot, because this
	 * implementation is only used for in-memory testing.
	 */
	final MemoryPreferences userRoot = new MemoryPreferences(null, "");
	
	public Preferences userRoot() {
		logger.debug("PreferencesFactory.userRoot()");
		return userRoot;
	}
	
}
