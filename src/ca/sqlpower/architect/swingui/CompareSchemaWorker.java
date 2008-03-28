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

import java.awt.Color;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.ddl.GenericTypeDescriptor;

public class CompareSchemaWorker implements Runnable {

	private static final Logger logger = Logger.getLogger(CompareSchemaWorker.class);

	private TreeSet<SQLTable> sourceTableList;

	private TreeSet<SQLTable> targetTableList;

	private AbstractDocument sourceDiff;

	private AbstractDocument targetDiff;

	private SimpleAttributeSet attrsDelete = null;

	private SimpleAttributeSet attrsAdd = null;

	private SimpleAttributeSet attrsDefault = null;
	
	private SimpleAttributeSet attrsModify = null;
	

	private enum printType {MISSING,ADDED,SAME, MODIFY}
	
	int jobSize;

	int progress;

	private boolean finished;


	private Map<Integer,GenericTypeDescriptor> sourceType;
	private Map<Integer,GenericTypeDescriptor> targetType;

	/**
	 * Use this constructor to compare the two tables in english
	 */
	public CompareSchemaWorker(TreeSet<SQLTable> sourceTableList,
			TreeSet<SQLTable> targetTableList, AbstractDocument sourceDiff,
			AbstractDocument targetDiff,Map<Integer,GenericTypeDescriptor> leftType, Map<Integer,GenericTypeDescriptor> rightType) {
		
		this(sourceTableList,targetTableList,leftType,rightType);
		this.sourceDiff = sourceDiff;
		this.targetDiff = targetDiff;
		
		

	}

	/**
	 *  Use this constructor to generate a sql script
	 */
	public CompareSchemaWorker(TreeSet<SQLTable> sourceTableSet, TreeSet<SQLTable> targetTableSet, AbstractDocument sqlDiff, Map<Integer, GenericTypeDescriptor> typeMap, Map<Integer, GenericTypeDescriptor> typeMap2, GenericDDLGenerator sqlDdlgen) {
		this(sourceTableSet,targetTableSet,typeMap,typeMap2);
	}

	/**
	 * The common constructor 
	 */
	private CompareSchemaWorker(TreeSet<SQLTable> sourceTableSet, TreeSet<SQLTable> targetTableSet,Map<Integer, GenericTypeDescriptor> typeMap, Map<Integer, GenericTypeDescriptor> typeMap2)
	{
		this.targetType = typeMap2;
		this.sourceType = typeMap;
		this.sourceTableList = sourceTableSet;
		this.targetTableList = targetTableSet;
		jobSize = targetTableList.size() + sourceTableList.size();
		progress = 0;
		finished = false;
		attrsDelete = new SimpleAttributeSet();
		attrsAdd = new SimpleAttributeSet();
		attrsDefault = new SimpleAttributeSet();
		attrsModify = new SimpleAttributeSet();

		StyleConstants.setForeground(attrsDelete, Color.red);

		StyleConstants.setForeground(attrsAdd, Color.green);
		StyleConstants.setForeground(attrsModify, Color.yellow);

		// StyleConstants.setFontFamily(attrsDefault, "Courier New");
		// StyleConstants.setFontSize(attrsDefault, 12);
		StyleConstants.setForeground(attrsDefault, Color.black);
	}
	public int getJobSize() {
		return jobSize;
	}

	public int getProgress() {
		return progress;
	}

	public boolean isFinished() {
		return finished;
	}

	public void run() {

		generateTableDiffs();
	}

	protected void generateTableDiffs() {
		try {

			Iterator sourceIter = sourceTableList.iterator();
			Iterator targetIter = targetTableList.iterator();
			SQLTable targetTable;
			SQLTable sourceTable;
			boolean sourceContinue;
			boolean targetContinue;

			//Checks if both lists of tables contain any tables at all, if they do
			//the iterator is initialized for the list
			if (sourceIter.hasNext()) {

				sourceContinue = true;
				sourceTable = (SQLTable) sourceIter.next();
			} else {
				sourceContinue = false;
				sourceTable = null;
			}

			if (targetIter.hasNext()) {
				targetContinue = true;
				targetTable = (SQLTable) targetIter.next();
			} else {
				targetContinue = false;
				targetTable = null;
			}

			//Will loop until one or both the list reaches its last table
			while (sourceContinue && targetContinue) {
				SQLObjectCompare comparator = new SQLObjectCompare();
				// bring the source table up to the same level as the target
				while (comparator.compare(sourceTable, targetTable) < 0) {
					if (targetDiff != null) {
						targetDiff.insertString(targetDiff.getLength(), 
								printTable (sourceTable, printType.MISSING), attrsAdd);
					}
					if (sourceDiff != null) {
						sourceDiff.insertString(sourceDiff.getLength(),
								printTable (sourceTable, printType.ADDED), attrsDelete);
					}					
					generateColumnDiffs(sourceTable, null);
					if (sourceIter.hasNext()) {
						sourceTable = (SQLTable) sourceIter.next();
					} else {
						sourceContinue = false;
						break;
					}
				}

				// bring the target table up to the same level as the source
				while (comparator.compare(sourceTable, targetTable) > 0) {
					if (targetDiff != null) {
						targetDiff.insertString(targetDiff.getLength(),
								printTable (targetTable, printType.ADDED), attrsDelete);
					}
					if (sourceDiff != null) {
						sourceDiff.insertString(sourceDiff.getLength(),
								printTable (targetTable, printType.MISSING),attrsAdd);
					}
					// now do the columns
					generateColumnDiffs(null, targetTable);
					if (targetIter.hasNext()) {
						targetTable = (SQLTable) targetIter.next();
					} else {
						targetContinue = false;
						break;
					}
				}

				while (comparator.compare(sourceTable, targetTable) == 0) {
					if (targetDiff != null) {
						targetDiff.insertString(targetDiff.getLength(),
								printTable (targetTable, printType.SAME),attrsDefault);
					}
					if (sourceDiff != null) {
						sourceDiff.insertString(sourceDiff.getLength(), 
								printTable(targetTable, printType.SAME), attrsDefault);
					}

					// now do the columns
					generateColumnDiffs(sourceTable, targetTable);
					if (targetIter.hasNext()) {
						targetTable = (SQLTable) targetIter.next();
					} else {
						targetContinue = false;
						break;
					}

					if (sourceIter.hasNext()) {
						sourceTable = (SQLTable) sourceIter.next();
					}

					else {
						sourceContinue = false;
						break;
					}
				}

			}
			//If any tables in the sourceList still exist, the changes are added
			while (sourceContinue) {
				if (targetDiff != null) {

					targetDiff.insertString(targetDiff.getLength(),
							"Missing table: " + sourceTable.getName() + "\n",
							attrsAdd);
				}
				if (sourceDiff != null) {
					sourceDiff.insertString(sourceDiff.getLength(), "Extra table: "
							+ sourceTable.getName() + "\n", attrsDelete);
				}
				generateColumnDiffs(sourceTable, null);
				if (sourceIter.hasNext()) {
					sourceTable = (SQLTable) sourceIter.next();
				} else {
					sourceContinue = false;
				}
			}
			
			//If any remaining tables in the targetList still exist, they are now being added
			while (targetContinue) {

				if (targetDiff != null) {

					targetDiff.insertString(targetDiff.getLength(),
							"Extra table: " + targetTable.getName() + "\n",
							attrsDelete);
				}
				if (sourceDiff != null) {
					sourceDiff.insertString(sourceDiff.getLength(),
							"Missing table: " + targetTable.getName() + "\n",
							attrsAdd);
				}
				generateColumnDiffs(null, targetTable);
				if (targetIter.hasNext()) {
					targetTable = (SQLTable) targetIter.next();
				} else {
					targetContinue = false;
				}
			}

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			finished = true;
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			}
		});
	}

	
	
	public void generateColumnDiffs(SQLTable sourceTable, SQLTable targetTable) {
		TreeSet<SQLColumn> sourceColumnList;
		TreeSet<SQLColumn> targetColumnList;
		Iterator sourceColIter;
		Iterator targetColIter;
		SQLColumn sourceColumn;
		SQLColumn targetColumn;
		boolean sourceColContinue;
		boolean targetColContinue;

		sourceColumnList = new TreeSet<SQLColumn>(new SQLObjectCompare());
		targetColumnList = new TreeSet<SQLColumn>(new SQLObjectCompare());
		sourceColContinue = false;
		targetColContinue = false;
		sourceColIter = null;
		targetColIter = null;
		sourceColumn = null;
		targetColumn = null;


		try {
			if (sourceTable != null) {
				for (SQLColumn col : sourceTable.getColumns()) {
					sourceColumnList.add(col);
				}
			}
			if (targetTable != null) {
				for (SQLColumn col : targetTable.getColumns()) {
					targetColumnList.add(col);
				}
			}
			if (sourceColumnList.size() == 0) {
				sourceColumnList = null;
				sourceColContinue = false;
			} else {

				sourceColIter = sourceColumnList.iterator();
				sourceColumn = (SQLColumn) sourceColIter.next();
				sourceColContinue = true;
			}

			if (targetColumnList.size() == 0) {
				targetColumnList = null;
				targetColContinue = false;
			} else {

				targetColIter = targetColumnList.iterator();
				targetColumn = (SQLColumn) targetColIter.next();
				targetColContinue = true;
			}

			while (sourceColContinue && targetColContinue) {
				SQLObjectCompare colComparator = new SQLObjectCompare();

				// Comparing Columns
				while (colComparator.compare(sourceColumn, targetColumn) < 0) {
					if (targetDiff != null) {
						targetDiff.insertString(targetDiff.getLength(),
								printColumn (printType.MISSING, sourceColumn, null, sourceType),attrsAdd);										
					}
					if (sourceDiff != null) {
						sourceDiff.insertString(sourceDiff.getLength(),
								printColumn (printType.ADDED, sourceColumn, null, sourceType), attrsDelete);
					}
					if (sourceColIter.hasNext()) {
						sourceColumn = (SQLColumn) sourceColIter.next();
					} else {
						sourceColContinue = false;
						break;
					}

				}
				// Comparing Columns
				while (colComparator.compare(sourceColumn, targetColumn) > 0) {
					if (targetDiff != null) {
						targetDiff.insertString(targetDiff.getLength(),
								printColumn (printType.ADDED, targetColumn,null, targetType), attrsDelete);
					}
					if (sourceDiff != null) {
						sourceDiff.insertString(sourceDiff.getLength(),
								printColumn (printType.MISSING, targetColumn,null, targetType),attrsAdd);

					}
					if (targetColIter.hasNext()) {
						targetColumn = (SQLColumn) targetColIter.next();
					} else {
						targetColContinue = false;
						break;
					}
				}

				// Comparing Columns
				while (colComparator.compare(sourceColumn, targetColumn) == 0) {
					GenericTypeDescriptor td = targetType.get(targetColumn.getType());
					
					if (targetColumn.getType() != sourceColumn.getType() || 
							(td.getHasPrecision() && targetColumn.getPrecision() != sourceColumn.getPrecision()) 
							|| (td.getHasScale() && targetColumn.getScale() != sourceColumn.getScale()))
					{
					
						if (targetDiff != null) {
							targetDiff.insertString(targetDiff.getLength(),
									printColumn (printType.MODIFY, targetColumn,sourceColumn, targetType), attrsModify);
						}
						if (sourceDiff != null) {
							sourceDiff.insertString(sourceDiff.getLength(),
									printColumn (printType.MODIFY, sourceColumn,targetColumn, sourceType), attrsModify);
						}
					}
					else {
						if (targetDiff != null) {
							targetDiff.insertString(targetDiff.getLength(),
									printColumn (printType.SAME, targetColumn,null, targetType), attrsDefault);
						}
						if (sourceDiff != null) {
							sourceDiff.insertString(sourceDiff.getLength(),
									printColumn (printType.SAME, targetColumn,null, targetType), attrsDefault);
						}
					}
					if (targetColIter.hasNext()) {
						targetColumn = (SQLColumn) targetColIter.next();
					} else {
						targetColContinue = false;
						
					}

					if (sourceColIter.hasNext()) {
						sourceColumn = (SQLColumn) sourceColIter.next();
					} else {
						sourceColContinue = false;
						
					}
					if(!sourceColContinue ||!targetColContinue)
					{
						break;
					}
				}
			}
			while (sourceColContinue) {
				if (targetDiff != null) {
					targetDiff.insertString(targetDiff.getLength(),
							printColumn (printType.MISSING, sourceColumn, null, sourceType), attrsAdd);
				}
				if (sourceDiff != null) {
					sourceDiff.insertString(sourceDiff.getLength(),
							printColumn (printType.ADDED, sourceColumn, null, sourceType),attrsDelete);
				}
				if (sourceColIter.hasNext()) {
					sourceColumn = (SQLColumn) sourceColIter.next();
				} else {
					sourceColContinue = false;
				}
			}
			
			while (targetColContinue) {
				if (targetDiff != null) {
					targetDiff.insertString(targetDiff.getLength(),
							printColumn (printType.ADDED, targetColumn,null, targetType), attrsDelete);
				}
				if (sourceDiff != null) {
					sourceDiff.insertString(sourceDiff.getLength(),
							printColumn(printType.MISSING, targetColumn,null, targetType), attrsAdd);
				}
				if (targetColIter.hasNext()) {
					targetColumn = (SQLColumn) targetColIter.next();
				} else {
					targetColContinue = false;
				}
			}
			
		} catch (ArchitectException e) {
			logger.debug("Architect exception in compareSchemaWorker", e);
		} catch (BadLocationException e) {

			logger.debug("Wrong document size", e);
		}

	}
	
	private String printTable (SQLTable table, printType type){
		StringBuffer text = new StringBuffer();
		
		if (type == printType.MISSING){
			text.append("Missing table: ");
		}
		
		else if (type == printType.ADDED){
			text.append("Extra table: ");
		}
		
		else{
			text.append("Same table: ");			
		}
		
		text.append (table.getName() + "\n");
		return text.toString();
	}

	private String printColumn (printType type, SQLColumn originalColumn, SQLColumn modifyTo, Map<Integer, GenericTypeDescriptor> typeMap){
		StringBuffer text = new StringBuffer() ;
		GenericTypeDescriptor td = typeMap.get(originalColumn.getType());
		if (type == printType.MODIFY  && td != null)
		{
			GenericTypeDescriptor modifyTd = typeMap.get(modifyTo.getType());
			text.append("\tModify column "+originalColumn.getName() +" from type: "+td.getName());
			if (td.getHasPrecision()){
				text.append("("+ originalColumn.getPrecision() );		
				if (td.getHasScale()){
					text.append("," + originalColumn.getScale());
				}
				text.append(")");
			}
			text.append(" to type: "+modifyTd.getName());
			if (modifyTd.getHasPrecision()){
				text.append("("+ modifyTo.getPrecision() );		
				if (modifyTd.getHasScale()){
					text.append("," + modifyTo.getScale());
				}
				text.append(")");
			}
			text.append("\n");
			
			
			
		}else if (type == printType.MISSING){
			text.append("\tMissing column: ");
		}
		
		else if (type == printType.ADDED){
			text.append("\tExtra column: ");
		}
		
		else{
			text.append("\tSame column: ");			
		}
		
		if (td!=null &&type != printType.MODIFY)
		{
			text.append (originalColumn.getName() + ": " + td.getName());
			
			
			if (td.getHasPrecision()){
				text.append("("+ originalColumn.getPrecision() );		
				if (td.getHasScale()){
					text.append("," + originalColumn.getScale());
				}
				text.append(")");
			}
			text.append("\n");
		}
		return text.toString();
	}
	
	public AbstractDocument getLeftDiff() {
		return sourceDiff;
	}

	public void setLeftDiff(AbstractDocument leftDiff) {
		this.sourceDiff = leftDiff;
	}

	public AbstractDocument getTargetDiff() {
		return targetDiff;
	}

	public void setTargetDiff(AbstractDocument rightDiff) {
		this.targetDiff = rightDiff;
	}

}
