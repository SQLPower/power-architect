/*package ca.sqlpower.architect.swingui;

import java.awt.Font;
import java.awt.Insets;
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
import javax.swing.text.AbstractDocument;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.CompareDMFrame.CloseAction;
import ca.sqlpower.architect.swingui.CompareDMFrame.sourceCopyAction;
import ca.sqlpower.architect.swingui.CompareDMFrame.targetCopyAction;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FrameCompareDm extends JFrame{
	
	private static Logger logger = Logger.getLogger(FrameCompareDm.class);
	private AbstractDocument outputDoc;
	private String title;
	private static JComponent panel;
	private JTextPane outputArea;
	
	public FrameCompareDm (AbstractDocument outputDoc, String title){
		this.outputDoc = outputDoc;
		this.title = title;
	}
	
	public void mainFrame(){
		FormLayout layout = new FormLayout(
				"10dlu,min:grow, 10dlu, default", // columns
				"pref, 6dlu, fill:300dlu:grow, 6dlu,20dlu, 6dlu pref"); // rows
		JPanel p = logger.isDebugEnabled()  ? new FormDebugPanel(layout) : new JPanel(layout);
		PanelBuilder pb = new PanelBuilder(layout,p);		
		pb.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		Font titleFont = new Font("Arial", 1,16);
		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(titleFont);
		pb.add(titleLabel, cc.xyw(2, 1, 3,"c,c"));
		
		outputArea = new JTextPane();
		outputArea.setMargin(new Insets(6,10,4,6));
		outputArea.setDocument(outputDoc);
		outputArea.setEditable(false);
		outputArea.setAutoscrolls(true);
		JScrollPane outputSp = new JScrollPane(outputSp);
		
		
		
		pb.add(outputSp, cc.xy (2,3));
	
		Action Copy = new sourceCopyAction(outputDoc);
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
		ButtonBarBuilder builder = new ButtonBarBuilder();
		JButton copyButton = new JButton(Copy);
		copyButton.setText("Copy");
		builder.addGridded (copyButton);
		builder.addRelatedGap();
		builder.addGlue();
		
		JButton executeButton = new JButton(execute);
		executeButton.setText("Execute");
		builder.addGridded(execute);
		builder.addRelatedGap();
		builder.addGlue();
		if ( execute == null ) {
			execute.setEnabled(false);
		}
		JButton save = new JButton(save);
		save.setText("Save");
		builder.addGridded(save);
		builder.addRelatedGap();
		builder.addGlue();		
	
		
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
	

}
*/