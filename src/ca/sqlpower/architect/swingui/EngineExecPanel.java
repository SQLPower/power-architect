package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class EngineExecPanel extends JPanel {
	
	protected Process proc;
	protected JTextArea output;
	protected Thread iss;
	protected Thread ess;
	private Font font;
	boolean autoMoveInd;
	
	protected Action abortAction;


	public Action getAbortAction() {
		return abortAction;
	}

	public EngineExecPanel(String header, Process pr) {
		super(new BorderLayout());
		proc = pr;
		autoMoveInd = true;

		abortAction = new AbstractAction("Abort") {
			public void actionPerformed(ActionEvent e) {
				proc.destroy();
				if ( output != null && output.isEnabled() ) {
					output.append("Aborted ...");
					output.setEnabled(false);
				}
			}
		};
		abortAction.setEnabled(true);
		
		font = new Font("Courier New", Font.PLAIN, 12 );

		output = new JTextArea(25, 120);
		output.append(header);
		output.append("\n\n");
		output.setFont(font);
		add(new JScrollPane(output), BorderLayout.CENTER);

		InputStream pis = new BufferedInputStream(proc.getInputStream());
		InputStream pes = new BufferedInputStream(proc.getErrorStream());
		
		iss = new Thread(new StreamSink(pis));
		ess = new Thread(new StreamSink(pes));
		
		iss.setPriority(Thread.MIN_PRIORITY);
		ess.setPriority(Thread.MAX_PRIORITY);
		
		iss.start();
		ess.start();
		
		Runnable buttonEnabler = new Runnable() {
			public void run() {
				waitForProcessCompletion();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						abortAction.setEnabled(false);
					}
				});
			}
		};
		new Thread(buttonEnabler).start();
	}

	/**
	 * Returns only when the process's stdout and stderr streams have
	 * both been closed (and therefore, no more output will be
	 * appended to the textarea).  You do not need to call this method
	 * if you don't want to do something special when the engine is
	 * finished.
	 */
	public void waitForProcessCompletion() {
		try {
			iss.join();
			ess.join();
		} catch (InterruptedException ex) {
			System.out.println("Interrupted while waiting for engine");
		}
		output.append("Execution halted");
	}

	class StreamSink implements Runnable {

		protected InputStream is;

		public StreamSink(InputStream is) {
			this.is = is;
		}

		public void run() {
			int ch;
			StringBuffer msg = new StringBuffer();
			try {
				while ( (ch = is.read()) >= 0) {
					msg.append(String.valueOf((char) ch));
					if ( ch == '\n' ) {
						output.append(msg.toString());
						output.setCaretPosition(output.getText().length());
						msg.delete(0,msg.length());
					}
				}
				if ( msg.length() > 0 ) {
					output.append(msg.toString());
					output.setCaretPosition(output.getText().length());
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
