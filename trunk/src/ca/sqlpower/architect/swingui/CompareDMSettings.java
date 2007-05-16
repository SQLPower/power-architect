package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.swingui.CompareDMPanel.SourceOrTargetStuff;

public class CompareDMSettings {
	
	public enum DatastoreType { PROJECT, DATABASE, FILE; }
	public enum OutputFormat { SQL, ENGLISH; }
	
	private OutputFormat outputFormat;
	private String sqlScriptFormat;
    private boolean showNoChanges;
    private SourceOrTargetStuff targetStuff;
    private SourceOrTargetStuff sourceStuff;
    private Object sqlScriptFormatValue;
    
    /**
     * This flag should be set to true after the user has potentially modified
     * the CompareDM settings (for example, because the user has pressed the OK
     * button on the CompareDM dialog).
     */
	private boolean saveFlag = false;
	
	public static class SourceOrTargetSettings {
		private DatastoreType datastoreType;
		private String connectName;
		private String connectURL;
		private String connectUserName;
        private String catalogName;
        private String schemaName;
		private Object catalog;
		private Object schema;
		private String filePath;

		public DatastoreType getDatastoreType() {
			return datastoreType;
		}
		public void setDatastoreType(DatastoreType v) {
			this.datastoreType = v;
		}
		public String getCatalog() {
			return catalogName;
		}
        public void setCatalogObject(Object catalog) {
            this.catalog = catalog;
        }
        public Object getCatalogObject() {
            return catalog;
        }
		public void setCatalog(String catalog) {
			this.catalogName = catalog;
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
			return schemaName;
		}
		public void setSchema(String schema) {
			this.schemaName = schema;
		}
        public Object getSchemaObject() {
            return schema;
        }
        public void setSchemaObject(Object schema) {
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
		
		public String getDatastoreTypeAsString() {
			return datastoreType.toString();
		}

		public void setDatastoreTypeAsString(String v) {
			datastoreType = DatastoreType.valueOf(v);
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
    
    public void setShowNoChanges (boolean b) {
        showNoChanges = b;
    }
    
    public boolean getShowNoChanges () {
        return showNoChanges;
    }
    
    public void setTargetStuff(SourceOrTargetStuff target) {
        targetStuff = target;
    }
    
    public SourceOrTargetStuff getTargetStuff() {
        return targetStuff;
    }
    
    public void setSourceStuff(SourceOrTargetStuff source) {
        sourceStuff = source;
    }
    
    public SourceOrTargetStuff getSourceStuff() {
        return sourceStuff;
    }
    
    public void setSqlScriptFormatValue (Object o) {
        sqlScriptFormatValue = o;
    }
    
    public Object getSqlScriptFormatValue() {
        return sqlScriptFormatValue;
    }
    
    /**
     * If the user never uses compareDM function, the saving process
     * would fail since some of the return values of saving compareDM
     * settings would be null.  Therefore the saveFlag is used as an
     * indicator to tell if the user went into compareDM or not.
     */
	public boolean getSaveFlag() {
		return saveFlag;
	}
    
	public void setSaveFlag(boolean saveFlag) {
		this.saveFlag = saveFlag;
	}
}
