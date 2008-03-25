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

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.swingui.CompareDMPanel.SourceOrTargetStuff;

public class CompareDMSettings {
	
	public enum DatastoreType { PROJECT, DATABASE, FILE; }
	public enum OutputFormat { SQL, ENGLISH; }

    /**
     * The DDL Generator class the user has chosen.  This option
     * only applies if the user chooses the SQL Script output
     * format (as opposed to English descriptions).
     */
    private Class<? extends DDLGenerator> ddlGenerator;
    
	private OutputFormat outputFormat;
    private boolean suppressSimilarities;
    private SourceOrTargetStuff targetStuff;
    private SourceOrTargetStuff sourceStuff;
    
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
    
    public void setSuppressSimilarities (boolean b) {
        suppressSimilarities = b;
    }
    
    public boolean getSuppressSimilarities () {
        return suppressSimilarities;
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
    
    public Class<? extends DDLGenerator> getDdlGenerator() {
        return ddlGenerator;
    }
    
    public void setDdlGenerator(Class<? extends DDLGenerator> ddlGenerator) {
        this.ddlGenerator = ddlGenerator;
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
