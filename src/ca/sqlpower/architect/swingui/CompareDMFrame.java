package ca.sqlpower.architect.swingui;

import java.awt.Color;
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

public class CompareDMFrame extends JFrame{

	private static Logger logger = Logger.getLogger(CompareDMFrame.class);
	private JTextPane outputArea;
	private AbstractDocument outputText;

	private String title;
	private SQLDatabase target;
	private static JComponent panel;

			
	public CompareDMFrame(AbstractDocument outputText, String title, SQLDatabase target)
	{
		super();	
		setTitle("Data Model comparison");
		this.outputText = outputText;
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
				"pref, 3dlu, fill:pref:grow, 3dlu, 30dlu"); // rows
		
		PanelBuilder pb = new PanelBuilder(layout,new FormDebugPanel());
		CellConstraints cc = new CellConstraints();
		
		pb.add(new JLabel(title), cc.xy(2, 1));
		
		outputArea = new JTextPane();
		outputArea.setMargin(new Insets(6, 10, 4, 6));
		outputArea.setDocument(outputText);
		outputArea.setEditable(false);
		
		JScrollPane sp = new JScrollPane(outputArea);     
        
        pb.add(sp, cc.xy(2, 3));

	
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

		
		ButtonBarBuilder bbBuilder = new ButtonBarBuilder();
		JButton copyButton = new JButton(copy);
		copyButton.setText("Copy");
		bbBuilder.addGridded(copyButton);
		bbBuilder.addRelatedGap();
		bbBuilder.addGlue();
		
		JButton executeButton = new JButton(execute);
		executeButton.setText("Execute");
		bbBuilder.addGridded(executeButton);
		bbBuilder.addRelatedGap();
		bbBuilder.addGlue();
		if ( execute == null ) {
			execute.setEnabled(false);
		}
		
		JButton saveButton = new JButton(save);
		saveButton.setText("Save");
		bbBuilder.addGridded(saveButton);
		bbBuilder.addRelatedGap();
		bbBuilder.addGlue();
		
		JButton closeButton = new JButton(close);
		closeButton.setText("Close");
		bbBuilder.addGridded(closeButton);

		pb.add(bbBuilder.getPanel(), cc.xy(2, 5));
		return pb.getPanel();


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
        
        DefaultStyledDocument doc = new DefaultStyledDocument();
        doc.insertString(doc.getLength(),"line 1 - normal line"+newline, attrsMsg);
        doc.insertString(doc.getLength(),"line 2 - red line"+newline, attrsSource);
        doc.insertString(doc.getLength(),"line 3 - green line"+newline, attrsTarget);
        doc.insertString(doc.getLength(),"line 4 - black line"+newline, attrsSame);
        doc.insertString(doc.getLength(),"line 5 - normal line"+newline, attrsMsg);
        
        final JFrame f = new CompareDMFrame(doc,"compare test A to test B in english",new SQLDatabase());
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
