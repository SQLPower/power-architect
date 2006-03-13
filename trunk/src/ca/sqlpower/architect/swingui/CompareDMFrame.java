package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.swingui.ASUtils.FileExtensionFilter;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class CompareDMFrame extends JFrame{

	private static Logger logger = Logger.getLogger(CompareDMFrame.class);
	private JTextPane leftOutputArea;
	private JTextPane rightOutputArea;
	
	private AbstractDocument sourceOutputText;
	private AbstractDocument targetOutputText;

	private String title;
	private SQLDatabase target;
	private static JComponent panel;
	private String sourceName;
	private String targetName;
	private boolean debugLayout;
	private boolean isSQLScript;

			
	public CompareDMFrame(AbstractDocument sourceOutputText, AbstractDocument targetOutputText, 
						String title, SQLDatabase target, boolean isSQLScript)
	{
		super();	
		
		setTitle("Data Model comparison");
		this.sourceOutputText = sourceOutputText;
		this.targetOutputText = targetOutputText;
		this.title = title;
		this.target = target;
		this.isSQLScript = isSQLScript;
		panel = mainFrame();
		getContentPane().add(panel);
		this.pack();
		this.setVisible(true);
	}
	
	public JComponent mainFrame() {		
		
		FormLayout englisLayout = new FormLayout(
				"4dlu,min:grow, 6dlu, min:grow, 4dlu, default", // columns
				"pref, 6dlu, pref, 3dlu, fill:300dlu:grow, 3dlu, 20dlu,6dlu,20dlu"); // rows
		
		FormLayout sqlLayout = new FormLayout(
				"4dlu, min:grow, 4dlu", //columns
				"pref, 6dlu, fill:300dlu:grow,6dlu, 20dlu, 6dlu, 20dlu"); //rows		
		
		CellConstraints cc = new CellConstraints();
		Font titleFont = new Font("Arial", 1,16);
		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(titleFont);
		
		leftOutputArea = new JTextPane();
		leftOutputArea.setMargin(new Insets(6, 10, 4, 6));
		leftOutputArea.setDocument(sourceOutputText);
		leftOutputArea.setEditable(false);
		leftOutputArea.setAutoscrolls(true);
		JScrollPane sp = new JScrollPane(leftOutputArea);
		
		Action sourceCopy = new sourceCopyAction(sourceOutputText);
		Action execute = new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				//TODO: Implement execute function
			}			
		};

		Action sourceSave = new AbstractAction(){
			public void actionPerformed(ActionEvent e) { 
				saveDocument(sourceOutputText);
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
		
		if (isSQLScript){
			JButton sourceExecute = new JButton(execute);
			sourceExecute.setText("Execute");
			sourcebbBuilder.addGridded(sourceExecute);		
			sourcebbBuilder.addRelatedGap();
			sourcebbBuilder.addGlue();
			if ( execute == null ) {
				execute.setEnabled(false);
			}
		}
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



		if (isSQLScript){
			JPanel p = logger.isDebugEnabled()  ? new FormDebugPanel(sqlLayout) : new JPanel(sqlLayout);
			pb = new PanelBuilder(sqlLayout, p);			
			pb.setDefaultDialogBorder();	
			pb.add(titleLabel, cc.xy(2, 1));			
	        pb.add(sp, cc.xy(2, 3));
	    	pb.add(sourcebbBuilder.getPanel(), cc.xy(2, 5, "c,c"));
			pb.add(closeBar.getPanel(), cc.xy(2,7, "r,c"));
			
		}
		else{
		englisLayout.setColumnGroups(new int [][] { {2,4}}); 
		JPanel p = logger.isDebugEnabled()  ? new FormDebugPanel(englisLayout) : new JPanel(englisLayout);
		pb = new PanelBuilder(englisLayout,p);
		pb.setDefaultDialogBorder();		
		
		rightOutputArea = new JTextPane();
		rightOutputArea.setMargin(new Insets(6, 10, 4, 6));
		rightOutputArea.setDocument(targetOutputText);
		rightOutputArea.setEditable(false);
		JScrollPane sp1 = new JScrollPane(rightOutputArea);
		Action targetCopy = new targetCopyAction(targetOutputText);
		Action targetexecute = new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				//TODO: Implement execute function
			}			
		};
		//Sets the target Buttons
		ButtonBarBuilder targetbbBuilder = new ButtonBarBuilder();
		JButton copyTarget = new JButton(targetCopy);
		copyTarget.setText("Copy");
		targetbbBuilder.addGridded (copyTarget);
		targetbbBuilder.addRelatedGap();
		targetbbBuilder.addGlue();					
		
		
		Action targetSaveAction = new AbstractAction(){
			public void actionPerformed(ActionEvent e) {			
				saveDocument(targetOutputText);
			}
		};
		
		if ( execute == null ) {
			execute.setEnabled(false);
		}
		JButton targetSave = new JButton(targetSaveAction);
		targetSave.setText("Save");
		targetbbBuilder.addGridded(targetSave);
		targetbbBuilder.addRelatedGap();
		targetbbBuilder.addGlue();

		
		pb.add(titleLabel, cc.xyw(2, 1, 3,"c,c"));
		pb.add(new JLabel("Source"), cc.xy(2,3));
		pb.add(new JLabel("Target"), cc.xy(4,3));
        pb.add(sp, cc.xy(2, 5));
    	pb.add(sp1, cc.xy (4,5));
    	pb.add(sourcebbBuilder.getPanel(), cc.xy(2, 7, "l,c"));
		pb.add(targetbbBuilder.getPanel(), cc.xy(4, 7, "r,c"));
		pb.add(closeBar.getPanel(), cc.xy(4,9, "r,c"));
		//rightOutputArea.setAutoscrolls(true);
	
		//Sets the Source Buttons

	}

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
	
	
	protected void saveDocument ( AbstractDocument doc ) {
		JFileChooser saveChooser = new JFileChooser();
		FileExtensionFilter fef;
		if (isSQLScript){
			fef = (FileExtensionFilter) ASUtils.SQL_FILE_FILTER;								
		} else{
			fef=(FileExtensionFilter) ASUtils.TEXT_FILE_FILTER;
		}
			saveChooser.setFileFilter(fef);			
		int returnVal = saveChooser.showSaveDialog(CompareDMFrame.this);
		
		while (true) {
			if (returnVal == JFileChooser.CANCEL_OPTION)
				break;
			else if (returnVal == JFileChooser.APPROVE_OPTION) {
				
				File file = saveChooser.getSelectedFile();				
				String fileName = file.getPath();
				String fileExt = ASUtils.FileExtensionFilter.getExtension(file);
				if ( fileExt.length() == 0 ) {
					file = new File(fileName + "." + fef.getFilterExtension(new Integer(0)));
				}
				if ( file.exists() && 
						( JOptionPane.showOptionDialog(CompareDMFrame.this, 
							"Are your sure you want to overwrite this file?",
							"Confirm Overwrite", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,null,null,null) == JOptionPane.NO_OPTION ) )
					{
						returnVal = saveChooser.showSaveDialog(CompareDMFrame.this);
					}
				else {
					writeDocument(doc,file);
					break;
				}
			}
		}
	}
	protected void writeDocument (AbstractDocument doc, File file) {
		try {						
			StringReader sr = new StringReader(doc.getText(0,sourceOutputText.getLength()));
			BufferedReader br = new BufferedReader(sr);
			PrintWriter out = new PrintWriter(file);
			String s;
			while ( (s = br.readLine()) != null ) {
				out.println(s);
			}
			out.close();
		} catch (IOException e1) {
			ASUtils.showExceptionDialog("Save file Error!", e1);
		} catch (BadLocationException e1) {
			ASUtils.showExceptionDialog("Open file Error!", e1);
		} 
	}	
}
