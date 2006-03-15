package ca.sqlpower.architect.swingui;

public class CompareDMSettings {
	
	public enum RadioButtonSelection { PROJECT, DATABASE, FILE; }
	public enum OutputFormat { SQL, ENGLISH; }

	public static class SourceOrTargetSettings {
		private RadioButtonSelection buttonSelection;
		private String connectName;
		private String connectURL;
		private String connectUserName;
		private String catalog;
		private String schema;
		private String fileName;
		
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
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
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
	}
	
	private SourceOrTargetSettings sourceSettings = new SourceOrTargetSettings();
	private SourceOrTargetSettings targetSettings = new SourceOrTargetSettings();
	
	public SourceOrTargetSettings getSourceSettings() {
		return sourceSettings;
	}
	public SourceOrTargetSettings getTargetSettings() {
		return targetSettings;
	}
	
	
}
