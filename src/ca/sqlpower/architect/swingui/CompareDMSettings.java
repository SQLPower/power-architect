package ca.sqlpower.architect.swingui;

public class CompareDMSettings {
	
	public enum RadioButtonSelection { PROJECT, DATABASE, FILE; }
	public enum OutputFormat { SQL, ENGLISH; }
	
	private OutputFormat outputFormat;
	private String sqlScriptFormat;
	private boolean saveFlag = false;	//Checks if the user has been on compareDM
	
	public static class SourceOrTargetSettings {
		private RadioButtonSelection buttonSelection;
		private String connectName;
		private String connectURL;
		private String connectUserName;
		private String catalog;
		private String schema;
		private String filePath;

		public RadioButtonSelection getButtonSelection() {
			return buttonSelection;
		}
		public void setButtonSelection(RadioButtonSelection buttonSelection) {
			this.buttonSelection = buttonSelection;
		}
		public String getCatalog() {
			return catalog;
		}
		public void setCatalog(String catalog) {
			this.catalog = catalog;
		}
		public String getConnectName() {
			return connectName;
		}
		public void setConnectName(String connectName) {
			this.connectName = connectName;
		}
		public String getFilePath() {
			return filePath;
		}
		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}
		public String getSchema() {
			return schema;
		}
		public void setSchema(String schema) {
			this.schema = schema;
		}
		public String getConnectURL() {
			return connectURL;
		}
		public String getConnectUserName() {
			return connectUserName;
		}
		public void setConnectUserName(String connectUserName) {
			this.connectUserName = connectUserName;
		}
		
		public String getRadioButtonSelectionAsString() {
			return buttonSelection.toString();
		}

		public void setRadioButtonSelectionAsString(String v) {
			buttonSelection = RadioButtonSelection.valueOf(v);
		}
		
	}
	
	private SourceOrTargetSettings sourceSettings = new SourceOrTargetSettings();
	private SourceOrTargetSettings targetSettings = new SourceOrTargetSettings();
	
	public SourceOrTargetSettings getSourceSettings() {
		return sourceSettings;
	}
	public SourceOrTargetSettings getTargetSettings() {
		return targetSettings;
	}
	public String getSqlScriptFormat() {
		return sqlScriptFormat;
	}
	public void setSqlScriptFormat(String scriptFormat) {
		sqlScriptFormat = scriptFormat;
	}
	
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}
	public void setOutputFormat(OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}
	
	public String getOutputFormatAsString() {
		return outputFormat.toString();
	}

	public void setOutputFormatAsString(String v) {
		outputFormat = OutputFormat.valueOf(v);
	}
	public boolean getSaveFlag() {
		return saveFlag;
	}
	public void setSaveFlag(boolean saveFlag) {
		this.saveFlag = saveFlag;
	}
}
