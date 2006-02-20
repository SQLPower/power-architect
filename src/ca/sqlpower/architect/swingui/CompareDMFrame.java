package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.sql.SQLData;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;

import com.jgoodies.forms.builder.ButtonBarBuilder;

public class CompareDMFrame extends JFrame{

	private static Logger logger = Logger.getLogger(CompareDMFrame.class);
	private AbstractDocument outputText;
	private String sourceName;
	private JPanel panel;
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
		outputArea = new JTextArea("test");	
		outputArea.setEditable(false);
		this.source = source;
		this.sourceName = source.getName();
		this.targetName = targetName;
		this.outputType = outputType;
		mainFrame();
	}
	
	public void mainFrame() {
		
		panel = new JPanel();
		panel.setLayout (new BorderLayout());
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
		panel.add(title, BorderLayout.NORTH);
		JScrollPane sp = new JScrollPane();
		sp.add(outputArea);
		panel.add(sp,BorderLayout.CENTER);

/*		builder.add (new JButton(copy));		
		builder.add (new JButton(execute));
		builder.add (new JButton(save));
		builder.add (new JButton (close));*/	
		panel.add(builder.getPanel(),BorderLayout.SOUTH);
		add(panel);
		pack();
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
	
	/**
	 * Just for testing the form layout without running the whole Architect.
	 * 
	 * <p>The frame it makes is EXIT_ON_CLOSE, so you should never use this
	 * in a real app.
	 */
	public static void main(String[] args) {
		final JFrame f = new JFrame("Testing compare dm panel");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				f.add(new CompareDMFrame( new DefaultStyledDocument(), new SQLDatabase(), "target", "SQL" ).getPanel());
				f.pack();
				f.setVisible(true);
			};
		});
	}

	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}
	
}
