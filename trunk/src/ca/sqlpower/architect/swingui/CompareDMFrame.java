package ca.sqlpower.architect.swingui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils.FileExtensionFilter;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class CompareDMFrame extends JFrame {

	private static Logger logger = Logger.getLogger(CompareDMFrame.class);
	private JTextPane leftOutputArea;
	private JTextPane rightOutputArea;
	
	private AbstractDocument sourceOutputText;
	private AbstractDocument targetOutputText;

	private String title;
	private JComponent panel;
			
	public CompareDMFrame(AbstractDocument sourceOutputText, AbstractDocument targetOutputText, 
						String leftDBName, String rightDBName)
	{
		super();	
		
		setTitle("Data Model comparison");
		this.sourceOutputText = sourceOutputText;
		this.targetOutputText = targetOutputText;
		this.title = "Comparing " + leftDBName+ " to "
		+ rightDBName + " using English";	
		panel = mainFrame();
		getContentPane().add(panel);
		
		SimpleAttributeSet att = new SimpleAttributeSet();
		
		StyleConstants.setForeground(att, Color.black);
		StyleConstants.setFontSize(att,leftOutputArea.getFont().getSize() * 2);

		try {
			sourceOutputText.insertString(0,leftDBName + "\n\n",att);
			targetOutputText.insertString(0,rightDBName + "\n\n",att);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
	
	}
	
	public JComponent mainFrame() {		
		
		FormLayout layout = new FormLayout(
				"4dlu,fill:min(150dlu;default):grow, 6dlu, fill:min(150dlu;default):grow, 4dlu", // columns
				" min(300dlu;default), 6dlu,  min(300dlu;default), 3dlu, fill:min(300dlu;default):grow, 3dlu, 20dlu,6dlu,20dlu"); // rows
		

		CellConstraints cc = new CellConstraints();
		Font titleFont = new Font("Ariel",1,16);
		
		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(titleFont);
		
		leftOutputArea = new JTextPane();
		leftOutputArea.setMargin(new Insets(6, 10, 4, 6));
		leftOutputArea.setDocument(sourceOutputText);
		leftOutputArea.setEditable(false);
		JPanel comparePanel =  new JPanel(new GridLayout(1,2));
		JScrollPane sp = new JScrollPane(comparePanel);
		comparePanel.add(leftOutputArea);
		Action sourceCopy = new sourceCopyAction(sourceOutputText);
	
		Action sourceSave = new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				new SaveDocument(CompareDMFrame.this,
						sourceOutputText,
						(FileExtensionFilter) ASUtils.TEXT_FILE_FILTER );
			}
		};
		CloseAction close = new CloseAction();
		close.setFrame(this);
		
		ButtonBarBuilder sourcebbBuilder = new ButtonBarBuilder();
		JButton copySource = new JButton(sourceCopy);
		copySource.setText("Copy");
		sourcebbBuilder.addGridded (copySource);
		sourcebbBuilder.addRelatedGap();
		sourcebbBuilder.addGlue();
		

			
		
		JButton sourceSaveButton = new JButton(sourceSave);
		sourceSaveButton.setText("Save");
		sourcebbBuilder.addGridded(sourceSaveButton);
		sourcebbBuilder.addRelatedGap();
		sourcebbBuilder.addGlue();		
		ButtonBarBuilder closeBar = new ButtonBarBuilder(); 
		JButton closeButton = new JButton(close);
		closeButton.setText("Close");
		closeBar.addGridded(closeButton);
		PanelBuilder pb;



		
		layout.setColumnGroups(new int [][] { {2,4}}); 
		JPanel p = logger.isDebugEnabled()  ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);			
		pb.setDefaultDialogBorder();	
		pb.add(titleLabel, cc.xy(2, 1));			
		pb.add(sp, cc.xy(2, 3));
		pb.add(sourcebbBuilder.getPanel(), cc.xy(2, 5, "c,c"));
		pb.add(closeBar.getPanel(), cc.xy(2,7, "r,c"));
		
		pb = new PanelBuilder(layout,p);
		pb.setDefaultDialogBorder();		
		
		rightOutputArea = new JTextPane();
		rightOutputArea.setMargin(new Insets(6, 10, 4, 6));
		rightOutputArea.setDocument(targetOutputText);
		rightOutputArea.setEditable(false);
		comparePanel.add(rightOutputArea);
		Action targetCopy = new targetCopyAction(targetOutputText);
		//Sets the target Buttons
		ButtonBarBuilder targetbbBuilder = new ButtonBarBuilder();
		JButton copyTarget = new JButton(targetCopy);
		copyTarget.setText("Copy");
		targetbbBuilder.addGridded (copyTarget);
		targetbbBuilder.addRelatedGap();
		targetbbBuilder.addGlue();					
		
		
		Action targetSaveAction = new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				new SaveDocument(CompareDMFrame.this,
						targetOutputText,
						(FileExtensionFilter) ASUtils.TEXT_FILE_FILTER );
			}
		};
		

		JButton targetSave = new JButton(targetSaveAction);
		targetSave.setText("Save");
		targetbbBuilder.addGridded(targetSave);
		targetbbBuilder.addRelatedGap();
		targetbbBuilder.addGlue();

		
		pb.add(titleLabel, cc.xyw(2, 1, 3,"c,c"));
		pb.add(new JLabel("Source"), cc.xy(2,3));
		pb.add(new JLabel("Target"), cc.xy(4,3));
		pb.add(sp, cc.xyw(2, 5,3));
		pb.add(sourcebbBuilder.getPanel(), cc.xy(2, 7, "l,c"));
		pb.add(targetbbBuilder.getPanel(), cc.xy(4, 7, "r,c"));
		pb.add(closeBar.getPanel(), cc.xy(4,9, "r,c"));
	
		return pb.getPanel();
	}
	
	public class sourceCopyAction extends AbstractAction{

		AbstractDocument doc;
		public sourceCopyAction(AbstractDocument doc)
		{
			this.doc = doc;
			
		}
		
		public void actionPerformed(ActionEvent e) {
			
			try {
				StringSelection selection = new StringSelection(doc.getText(0,doc.getLength()));
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection,selection);
			} catch (BadLocationException e1) {
				logger.debug("Unable to get the text for copying"+ e1);
			}
			
		}			
	}
	
	public class targetCopyAction extends AbstractAction{

		AbstractDocument doc;
		public targetCopyAction(AbstractDocument doc)
		{
			this.doc = doc;
		}
		
		public void actionPerformed(ActionEvent e) {
			
			try {
				StringSelection selection = new StringSelection(doc.getText(0,doc.getLength()));
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection,selection);
			} catch (BadLocationException e1) {
				logger.debug("Unable to get the text for copying"+ e1);
			}
			
		}			
	}

	public class CloseAction extends AbstractAction {	
		JFrame localframe;
		
		public void setFrame(JFrame frame){
			localframe = frame;				
		}
		public void actionPerformed(ActionEvent e) {
			localframe.setVisible(false);
		}						
	}
	
	

	public JPanel getPanel() {
		return (JPanel) panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}
	
	@Override
	public void pack() {
		super.pack();
		Dimension d =Toolkit.getDefaultToolkit().getScreenSize();
		logger.debug("Before change: Window width ="+getWidth() + " screen width ="+d.width);
		if (getWidth() > d.width - getX()) {
			setSize(d.width-getX(),getHeight());
		}
		logger.debug("Before change: Window height ="+getHeight() + " screen height ="+d.height);
		if (getHeight() > d.height-getY()) {
			setSize(getWidth(),d.height-getY());
		}
	}
	
	
}
