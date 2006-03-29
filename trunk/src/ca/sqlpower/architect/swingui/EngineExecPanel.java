package ca.sqlpower.architect.swingui;

import javax.swing.*;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class EngineExecPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(EngineExecPanel.class);
	
	protected Process proc;
	protected JScrollPane jsp;
	protected JTextArea output;
	protected JScrollBar jsb;
	protected Thread iss;
	protected Thread ess;
	private Font font;
	boolean scrollBarLock;
	
	protected Action abortAction;
	protected Action scrollBarLockAction;


	public Action getScrollBarLockAction() {
		return scrollBarLockAction;
	}
	
	public Action getAbortAction() {
		return abortAction;
	}

	public EngineExecPanel(String header, Process pr) {
		super(new BorderLayout());
		proc = pr;
		scrollBarLock = false;

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
		
		scrollBarLockAction = new AbstractAction("Scroll Lock") {
			public void actionPerformed(ActionEvent e) {
				JCheckBox cb = (JCheckBox)e.getSource();
				scrollBarLock = cb.isSelected();
			}
		};

		font = new Font("Courier New", Font.PLAIN, 12 );

		output = new JTextArea(25, 120);
		output.append(header);
		output.append("\n\n");
		output.setFont(font);
		
		jsp = new JScrollPane(output);
		jsb = jsp.getVerticalScrollBar();

 
		add(jsp, BorderLayout.CENTER);

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
			logger.error("Interrupted while waiting for engine", ex);
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
						if ( !scrollBarLock && ( jsb == null || !jsb.getValueIsAdjusting()) )
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
