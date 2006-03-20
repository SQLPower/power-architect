/*
 * Created on Jun 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;


/**
 * @author jack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WizardDialog extends JDialog {
	
	JPanel top;
	JPanel customPanel;
	
	JButton nextButton;
	JButton backButton;
	JButton cancelButton;
	
	ArchitectWizard wizard;
	
	/**
	 * @return Returns the progressBar.
	 */
	public JProgressBar getProgressBar() {
		return progressBar;
	}
	/**
	 * @return Returns the progressLabel.
	 */
	public JLabel getProgressLabel() {
		return progressLabel;
	}
	final JProgressBar progressBar;
	final JLabel progressLabel;
	
	final JDialog d;

	public WizardDialog(Frame frame, ArchitectWizard wizard) {
		super(frame);
		d = this;

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(progressBar.getPreferredSize());
		progressBar.setVisible(false);		

		progressLabel = new JLabel("Starting...");
		progressLabel.setPreferredSize(progressBar.getPreferredSize());
		progressLabel.setVisible(false);

		this.wizard = wizard;
		setupDialog();
		
	}
	
	private void setupDialog() {
		top = new JPanel(new BorderLayout());
		top.setPreferredSize(new Dimension(600,400));
		customPanel = new JPanel(new GridLayout(1,1));
		top.add(customPanel,BorderLayout.CENTER);
				
		JPanel buttonPanel = new JPanel(new BorderLayout());
		JPanel bpRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bpLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(bpRight,BorderLayout.EAST);
		buttonPanel.add(bpLeft,BorderLayout.WEST);
	
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				WizardPanel wp = getWizard().getCurrent();
				wp.discardChanges();
				setVisible(false);
			}
		});
		bpLeft.add(cancelButton);		

		backButton = new JButton("< Back");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setWizardPanel(getWizard().getPrevious());
				refreshButtons();
			}
		});
		bpRight.add(backButton);		
		
		nextButton = new JButton("Next >");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				WizardPanel wp = getWizard().getCurrent();
				if (wp.applyChanges()) {
					if (getWizard().isOnLastPanel()){
						
						setVisible(false);
					} else {
						if (getWizard().isOnExecutePanel()) {
					
							nextButton.setEnabled(false);
							getWizard().execute(d);
							nextButton.setEnabled(true);
						}
						setWizardPanel(getWizard().getNext());
						refreshButtons();
					}
				}
			}
		});
		bpRight.add(nextButton);
	
	
		JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		progressPanel.add(progressBar);
		progressPanel.add(progressLabel);

		// not sure what borderlayout will do here...
		JPanel bottomPanel = new JPanel(new GridLayout(2,1));
		bottomPanel.add(progressPanel);
		bottomPanel.add(buttonPanel);
		top.add(bottomPanel,BorderLayout.SOUTH);
		setContentPane(top);
		
		// set dialog to point to first panel
		setWizardPanel(getWizard().getCurrent());
		refreshButtons();
	}
	
	public void setWizardPanel(WizardPanel panel) {
		customPanel.removeAll();
		customPanel.add(panel.getPanel());		
		customPanel.revalidate();
		customPanel.repaint();
		setTitle(panel.getTitle());
	}
		
	private void refreshButtons() {
		backButton.setVisible(true);
		if (getWizard().isOnLastPanel()) {
			nextButton.setText("Close");
			backButton.setVisible(false);
			cancelButton.setVisible(false);
		} else if ( getWizard().isOnExecutePanel() ) {
			nextButton.setText("Execute");
		} else { 
			nextButton.setText("Next >");
		}
		
		if (getWizard().isOnFirstPanel()) {
			backButton.setVisible(false);
		}
	}

	/**
	 * users of this Dialog are responsible for supplying
	 * the wizard class.
	 * 
	 * @return
	 */
	public ArchitectWizard getWizard() {
		return wizard;
	}
	
	public static void refreshTargetConnections (JComboBox targetConnectionsBox,
									ListCellRenderer dataSourceRenderer) {
		
		
		
		Vector connections = new Vector();
		connections.add(null);
		Iterator it = ArchitectFrame.getMainInstance().getUserSettings().getConnections().iterator();
		while (it.hasNext()) {
			connections.add(it.next());
		}
		Object selectedConnection = targetConnectionsBox.getSelectedItem();
		targetConnectionsBox.setModel(new DefaultComboBoxModel(connections));
		if (selectedConnection != null) {
			targetConnectionsBox.setSelectedItem(selectedConnection);
		}
		targetConnectionsBox.setRenderer(dataSourceRenderer);
	}		
}
