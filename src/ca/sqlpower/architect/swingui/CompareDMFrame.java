package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;

import com.jgoodies.forms.builder.ButtonBarBuilder;

public class CompareDMFrame extends JFrame{

	private static Logger logger = Logger.getLogger(CompareDMFrame.class);
	private AbstractDocument outputText;
	private String sourceName;
	private SQLObject source;
	private String targetName;
	private String outputType;
	private JLabel title;
	private JTextArea outputArea;
	private ButtonBarBuilder builder;	
			
	public CompareDMFrame(AbstractDocument outputDocument, SQLObject source, 
			String targetName, String outputType)
	{
		super();
		
		this.outputText = outputDocument;
		outputArea = new JTextArea(outputText);
		outputArea.setEditable(false);
		this.source = source;
		this.sourceName = source.getName();
		this.targetName = targetName;
		this.outputType = outputType;
	}
	
	public void mainFrame(){
				
		setLayout (new BorderLayout());
		builder = new ButtonBarBuilder();	
		Action copy = new CopyAction(outputText);
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
								
		title = new JLabel("Comparing " + sourceName + " to " +  
				targetName + " using " + outputType );
		add(title, BorderLayout.NORTH);
		add(outputArea,BorderLayout.CENTER);
		builder.add (new JButton(copy));
		builder.add (new JButton(execute));
		builder.add (new JButton(save));
		builder.add (new JButton (close));	
		add(builder.getPanel(),BorderLayout.SOUTH);
	}
	
	public class CopyAction extends AbstractAction{

		AbstractDocument doc;
		public CopyAction(AbstractDocument doc)
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
}
