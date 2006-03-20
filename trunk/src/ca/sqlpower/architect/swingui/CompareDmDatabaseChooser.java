package ca.sqlpower.architect.swingui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.text.AbstractDocument;

import ca.sqlpower.architect.SQLDatabase;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class CompareDmDatabaseChooser extends JFrame {
	
	AbstractDocument doc;
	SQLDatabase db;
	public CompareDmDatabaseChooser(AbstractDocument doc){
		this.doc = doc;		
		getContentPane().add(buildFrame());
		this.pack();
		this.setVisible(true);
	}
	
	public JComponent buildFrame(){
		FormLayout layout = new FormLayout(
				"4dlu, pref:grow,6dlu, pref, 6dlu, pref, 4dlu",//columns
				"");
		CellConstraints cc= new CellConstraints();
		PanelBuilder pb  = new PanelBuilder(layout);
		JLabel executeIn = new JLabel("Execture in:");
		pb.add(executeIn, cc.xy(2,2));
		JComboBox connectionDropdown = new JComboBox();
		pb.add(connectionDropdown, cc.xy(2,4));
		JButton newButton = new JButton("New");
		pb.add(newButton, cc.xy(4,4));
		JButton executeButton = new JButton("Execute");
		pb.add(executeButton, cc.xy(4,6));
		return pb.getPanel();
	}

}
