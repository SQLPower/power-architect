package ca.sqlpower.architect.swingui;

import java.awt.Color;
import java.awt.Font;
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
import javax.swing.JTextArea;
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
import com.jgoodies.forms.layout.CellConstraints.Alignment;

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

			
	public CompareDMFrame(AbstractDocument sourceOutputText, AbstractDocument targetOutputText, 
						String title, SQLDatabase target)
	{
		super();	
		setTitle("Data Model comparison");
		this.sourceOutputText = sourceOutputText;
		this.targetOutputText = targetOutputText;
		this.title = title;
		this.target = target;
		panel = mainFrame();
		getContentPane().add(panel);
		this.pack();
		this.setVisible(true);
	}
	
	public JComponent mainFrame() {
		
		
		FormLayout layout = new FormLayout(
				"4dlu,fill:pref:grow, 6dlu, min:grow, 4dlu, default", // columns
				"pref, 6dlu, pref, 3dlu, fill:pref:grow, 3dlu, 20dlu,6dlu,20dlu"); // rows
		
		PanelBuilder pb = new PanelBuilder(layout,new FormDebugPanel());
		pb.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		Font titleFont = new Font("Arial", 1,16);
		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(titleFont);
		pb.add(titleLabel, cc.xyw(2, 1, 3,"c,c"));
		pb.add(new JLabel("Source name: SourceTest"), cc.xy(2,3));
		pb.add(new JLabel("Target name: TargetTest"), cc.xy(4,3));
		
		leftOutputArea = new JTextPane();
		leftOutputArea.setMargin(new Insets(6, 10, 4, 6));
		leftOutputArea.setDocument(sourceOutputText);
		leftOutputArea.setEditable(false);
		
		
		
		JScrollPane sp = new JScrollPane(leftOutputArea);     
        
        pb.add(sp, cc.xy(2, 5));
        
        rightOutputArea = new JTextPane();
		rightOutputArea.setMargin(new Insets(6, 10, 4, 6));
		rightOutputArea.setDocument(sourceOutputText);
		rightOutputArea.setEditable(false);
		
		JScrollPane sp1 = new JScrollPane(rightOutputArea);
		
		pb.add(sp1, cc.xy (4,5));
	
		Action sourceCopy = new sourceCopyAction(sourceOutputText);
		Action execute = new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				//TODO: Implement execute function
			}			
		};

		Action save = new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				//TODO: Implement Save function
			}			
		};
		CloseAction close = new CloseAction();
		close.setFrame(this);

		//Sets the Source Buttons
		ButtonBarBuilder sourcebbBuilder = new ButtonBarBuilder();
		JButton copySource = new JButton(sourceCopy);
		copySource.setText("Copy");
		sourcebbBuilder.addGridded (copySource);
		sourcebbBuilder.addRelatedGap();
		sourcebbBuilder.addGlue();
		
		JButton sourceExecute = new JButton(execute);
		sourceExecute.setText("Execute");
		sourcebbBuilder.addGridded(sourceExecute);
		sourcebbBuilder.addRelatedGap();
		sourcebbBuilder.addGlue();
		if ( execute == null ) {
			execute.setEnabled(false);
		}
		JButton sourceSave = new JButton(save);
		sourceSave.setText("Save");
		sourcebbBuilder.addGridded(sourceSave);
		sourcebbBuilder.addRelatedGap();
		sourcebbBuilder.addGlue();		
	
		
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
		if ( execute == null ) {
			execute.setEnabled(false);
		}
		JButton targetSave = new JButton(save);
		targetSave.setText("Save");
		targetbbBuilder.addGridded(targetSave);
		targetbbBuilder.addRelatedGap();
		targetbbBuilder.addGlue();
		
		ButtonBarBuilder closeBar = new ButtonBarBuilder(); 
		JButton closeButton = new JButton(close);
		closeButton.setText("Close");
		closeBar.addGridded(closeButton);

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
        sourceDoc.insertString(sourceDoc.getLength(),"line 1 - normal line"+newline, attrsMsg);
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
        
        final JFrame f = new CompareDMFrame(sourceDoc, targetDoc,
        		"compare test A to test B in english",new SQLDatabase());
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
