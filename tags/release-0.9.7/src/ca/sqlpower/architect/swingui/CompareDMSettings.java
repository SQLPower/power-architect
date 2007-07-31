/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
