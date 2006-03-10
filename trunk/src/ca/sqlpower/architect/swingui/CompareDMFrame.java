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
				JFileChooser saveChooser = new JFileChooser();				
				int returnVal = saveChooser.showSaveDialog(CompareDMFrame.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					final File file = saveChooser.getSelectedFile();
					try {
						StringReader sr = new StringReader(sourceOutputText.getText(0,sourceOutputText.getLength()));
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
		
		
		JButton targetExecute = new JButton(execute);
		targetExecute.setText("Execute");
		targetbbBuilder.addGridded(targetExecute);
		targetbbBuilder.addRelatedGap();
		targetbbBuilder.addGlue();
		
		
		Action targetSaveAction = new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				JFileChooser saveChooser = new JFileChooser();				
				int returnVal = saveChooser.showSaveDialog(CompareDMFrame.this);								
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					/*if (saveChooser.getSelectedFile().exists()){
						System.out.println("This should pop up overwrite message");
					}*/
					final File file = saveChooser.getSelectedFile();					
					try {
						StringReader sr = new StringReader(targetOutputText.getText(0,targetOutputText.getLength()));
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
	
	final private static String newline="\n";
	/**
	 * Just for testing the form layout without running the whole Architect.
	 * 
	 * <p>The frame it makes is EXIT_ON_CLOSE, so you should never use this
	 * in a real app.
	 * @throws BadLocationException 
	 */
	public static void main(String[] args) throws BadLocationException {

		try {
            UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");
        } catch (Exception e) {
            // Likely PlasticXP is not in the class path; ignore.
        }
        
        SimpleAttributeSet attrsSource = new SimpleAttributeSet();
		SimpleAttributeSet attrsTarget = new SimpleAttributeSet();
		SimpleAttributeSet attrsSame = new SimpleAttributeSet();
		SimpleAttributeSet attrsMsg = new SimpleAttributeSet();

        StyleConstants.setFontFamily(attrsSource, "Courier New");
        StyleConstants.setFontSize(attrsSource, 12);
        StyleConstants.setForeground(attrsSource, Color.red);

        StyleConstants.setFontFamily(attrsTarget, "Courier New");
        StyleConstants.setFontSize(attrsTarget, 12);
        StyleConstants.setForeground(attrsTarget, Color.green);

        StyleConstants.setFontFamily(attrsSame, "Courier New");
        StyleConstants.setFontSize(attrsSame, 12);
        StyleConstants.setForeground(attrsSame, Color.black);
        
        StyleConstants.setFontFamily(attrsMsg, "Courier New");
        StyleConstants.setFontSize(attrsMsg, 12);
        StyleConstants.setForeground(attrsMsg, Color.orange);
        
        DefaultStyledDocument sourceDoc = new DefaultStyledDocument();
        sourceDoc.insertString(sourceDoc.getLength(),"line 1 - normal line"+newline,attrsMsg);
        sourceDoc.insertString(sourceDoc.getLength(),"line 2 - red line"+newline, attrsSource);
        sourceDoc.insertString(sourceDoc.getLength(),"line 3 - green line"+newline, attrsTarget);
        sourceDoc.insertString(sourceDoc.getLength(),"line 4 - black line"+newline, attrsSame);
        sourceDoc.insertString(sourceDoc.getLength(),"line 5 - normal line"+newline, attrsMsg);
        
        DefaultStyledDocument targetDoc = new DefaultStyledDocument();
        targetDoc.insertString(targetDoc.getLength(),"line 1 - normal line"+newline, attrsMsg);
        targetDoc.insertString(targetDoc.getLength(),"line 2 - red line"+newline, attrsSource);
        targetDoc.insertString(targetDoc.getLength(),"line 3 - green line"+newline, attrsTarget);
        targetDoc.insertString(targetDoc.getLength(),"line 4 - black line"+newline, attrsSame);
        targetDoc.insertString(targetDoc.getLength(),"line 5 - normal line"+newline, attrsMsg);
        targetDoc.insertString(targetDoc.getLength(),"line 6 - this is a really really really reallly really long line so long that I do not quite know what's going on, ha!", attrsMsg);
        final JFrame f = new CompareDMFrame(sourceDoc, targetDoc,
        		"compare test A to test B in english",new SQLDatabase(), true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(panel);
        
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
	
				f.pack();
				f.setVisible(true);
			};
		});
	}

	public JPanel getPanel() {
		return (JPanel) panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}
	
}
