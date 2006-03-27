package regress.ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.swingui.ArchitectSwingWorker;
import junit.framework.TestCase;

public class TestArchitectSwingWorker extends TestCase {

	ArchitectSwingWorker setCancelled;
	ArchitectSwingWorker w2;
	ArchitectSwingWorker w3;
	@Override
	protected void setUp() throws Exception {
		setCancelled = new ArchitectSwingWorker(){

			@Override
			public void cleanup() throws Exception {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void doStuff() throws Exception {
				this.setCancelled(true);
				
			}
			
		};
		w2 = new ArchitectSwingWorker(){

			@Override
			public void cleanup() throws Exception {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void doStuff() throws Exception {
				// TODO Auto-generated method stub
				
			}
			
		};
		w3 = new ArchitectSwingWorker(){

			@Override
			public void cleanup() throws Exception {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void doStuff() throws Exception {
				// TODO Auto-generated method stub
				
			}
			
		};
		super.setUp();
	}
	
}
