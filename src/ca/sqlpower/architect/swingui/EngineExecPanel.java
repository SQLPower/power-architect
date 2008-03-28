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
package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

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
