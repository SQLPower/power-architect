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
	
	public EngineExecPanel(Process pr) {
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
		add(output, BorderLayout.CENTER);

		InputStream pis = new BufferedInputStream(proc.getInputStream());
		InputStream pes = new BufferedInputStream(proc.getErrorStream());
		Thread iss = new Thread(new StreamSink(pis));
		Thread ess = new Thread(new StreamSink(pes));
		iss.start();
		ess.start();

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
