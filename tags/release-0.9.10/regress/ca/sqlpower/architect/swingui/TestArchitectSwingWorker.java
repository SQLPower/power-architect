/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.swingui;

import junit.framework.TestCase;
import ca.sqlpower.swingui.SPSwingWorker;

public class TestArchitectSwingWorker extends TestCase {

	SPSwingWorker setCancelled;
	SPSwingWorker w2;
	SPSwingWorker w3;
	@Override
	protected void setUp() throws Exception {
		setCancelled = new SPSwingWorker(null){

			@Override
			public void cleanup() throws Exception {
			}

			@Override
			public void doStuff() throws Exception {
				this.setCancelled(true);
				
			}
			
		};
		w2 = new SPSwingWorker(null){

			@Override
			public void cleanup() throws Exception {
			}

			@Override
			public void doStuff() throws Exception {
			}
			
		};
		w3 = new SPSwingWorker(null){

			@Override
			public void cleanup() throws Exception {
			}

			@Override
			public void doStuff() throws Exception {
			}
			
		};
		super.setUp();
	}
	
}
