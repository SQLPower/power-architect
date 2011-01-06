/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.sqlpower.architect.swingui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.architect.ddl.LiquibaseDDLGenerator;
import ca.sqlpower.architect.ddl.LiquibaseSettings;
import ca.sqlpower.swingui.DataEntryPanel;

/**
 *
 * @author Thomas Kellerer
 */
public class LiquibaseOptionsPanel
  implements DataEntryPanel {

	private JPanel panel;
	private LiquibaseDDLGenerator ddlGenerator;

	private JCheckBox useChangeSets;
	private JTextField authorField;
	private JLabel authorLabel;
	private JCheckBox generateId;
	private JCheckBox useAddPKTagForSingleColumns;
	private JSpinner startId;
	private JLabel startValueLabel;

	public LiquibaseOptionsPanel() {
		setup();
	}

	public void setGenerator(LiquibaseDDLGenerator generator) {
		ddlGenerator = generator;
	}

	protected int getIdStart() {
		Integer start = (Integer)startId.getValue();
		return start == null ? 0 : start.intValue();
	}
	
	protected boolean getUseChangeSets() {
		return useChangeSets.isSelected();
	}

	protected String getAuthor() {
		return authorField.getText();
	}

	protected boolean getGenerateId() {
		return generateId.isSelected();
	}

	private void setup() {
		panel = new JPanel(new MigLayout());
		useChangeSets = new JCheckBox(Messages.getString("LiquibaseOptionsPanel.useChangeSet")); //$NON-NLS-1$
		useAddPKTagForSingleColumns = new JCheckBox(Messages.getString("LiquibaseOptionsPanel.alwaysUseAddPK")); //$NON-NLS-1$
		generateId = new JCheckBox(Messages.getString("LiquibaseOptionsPanel.generateID")); //$NON-NLS-1$
		authorLabel = new JLabel(Messages.getString("LiquibaseOptionsPanel.authorName")); //$NON-NLS-1$
		authorField = new JTextField(20);
		startValueLabel = new JLabel(Messages.getString("LiquibaseOptionsPanel.idStart")); //$NON-NLS-1$
		SpinnerNumberModel model = new SpinnerNumberModel(1,1,9999,1);
		startId = new JSpinner(model);
		panel.add(useAddPKTagForSingleColumns, "wrap");
		panel.add(useChangeSets);
		panel.add(authorLabel);
		panel.add(authorField, "wrap");
		panel.add(generateId);
		panel.add(startValueLabel);
		panel.add(startId);

	}

	public void restoreSettings(LiquibaseSettings settings) {
		if (settings == null) return;
		String author = settings.getAuthor();
		authorField.setText(author == null ? "" : author);
		useChangeSets.setSelected(settings.getUseSeparateChangeSets());
		generateId.setSelected(settings.getGenerateId());
		int start = settings.getIdStart();
		if (start > 0) {
			startId.setValue(Integer.valueOf(start));
		}
		useAddPKTagForSingleColumns.setSelected(settings.getUseAddPKTagForSingleColumns());
	}
	
	public LiquibaseSettings getLiquibaseSettings() {
		LiquibaseSettings settings = new LiquibaseSettings();
		settings.setAuthor(getAuthor());
		settings.setGenerateId(getGenerateId());
		settings.setUseSeparateChangeSets(getUseChangeSets());
		settings.setIdStart(getIdStart());
		settings.setUseAddPKTagForSingleColumns(useAddPKTagForSingleColumns.isSelected());
		return settings;
	}
	
	public boolean applyChanges() {
		ddlGenerator.setUseSeparateChangeSets(getUseChangeSets());
		ddlGenerator.setAuthor(getAuthor());
		ddlGenerator.setGenerateId(getGenerateId());
		ddlGenerator.setIdStart(getIdStart());
		ddlGenerator.setUseAddPKTagForSingleColumns(useAddPKTagForSingleColumns.isSelected());
		return true;
	}

	public void discardChanges() {

	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return true;
	}

}
