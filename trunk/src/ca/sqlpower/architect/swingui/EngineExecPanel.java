package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class EngineExecPanel extends JPanel {
	
	protected Process proc;
	protected JTextArea output;
	protected JButton abortButton;
	protected Thread iss;
	protected Thread ess;

	public EngineExecPanel(String header, Process pr) {
		super(new BorderLayout());
		proc = pr;

		abortButton = new JButton("Abort");
		abortButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					proc.destroy();
				}
			});
		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
		p.add(abortButton);
		add(p, BorderLayout.SOUTH);

		output = new JTextArea(25, 80);
		output.append(header);
		add(new JScrollPane(output), BorderLayout.CENTER);

		InputStream pis = new BufferedInputStream(proc.getInputStream());
		InputStream pes = new BufferedInputStream(proc.getErrorStream());
		iss = new Thread(new StreamSink(pis));
		ess = new Thread(new StreamSink(pes));
		iss.start();
		ess.start();
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
		abortButton.setEnabled(false);
		output.append("Execution halted");
	}

	class StreamSink implements Runnable {

		protected InputStream is;

		public StreamSink(InputStream is) {
			this.is = is;
		}

		public void run() {
			int ch;
			try {
				while ( (ch = is.read()) >= 0) {
					output.append(String.valueOf((char) ch));
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
