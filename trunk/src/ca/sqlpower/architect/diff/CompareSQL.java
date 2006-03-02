package ca.sqlpower.architect.diff;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.GenericTypeDescriptor;
import ca.sqlpower.architect.swingui.SwingUIProject;

public class CompareSQL {

	private static final Logger logger = Logger.getLogger(CompareSQL.class);

	private TreeSet<SQLTable> sourceTableList;

	private TreeSet<SQLTable> targetTableList;

	private int jobSize;

	private int progress;

	private boolean done;

	final static Comparator<SQLObject> comparator = new SQLObjectComparator();

	private Map<Integer,GenericTypeDescriptor> targetType;
	
	List<DiffChunk<SQLObject>> results;

	/**
	 * The common constructor 
	 */

		
	public CompareSQL(TreeSet<SQLTable> sourceTableSet, TreeSet<SQLTable> targetTableSet)
	{
		this.sourceTableList = sourceTableSet;
		this.targetTableList = targetTableSet;
		progress = 0;
		jobSize = targetTableList.size() + sourceTableList.size();
	}
	
	public int getJobSize() {
		return jobSize;
	}

	public int getProgress() {
		return progress;
	}

	public boolean isDone() {
		return done;
	}

	public List<DiffChunk<SQLObject>> generateTableDiffs() {
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
			

			// Will loop until one or both the list reaches its last table
			while (sourceContinue && targetContinue) {
				// bring the source table up to the same level as the target
				while (comparator.compare(sourceTable, targetTable) < 0) {
					results.add(new DiffChunk<SQLObject>(sourceTable, DiffType.LEFTONLY));
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
					results.add(new DiffChunk<SQLObject>(targetTable, DiffType.RIGHTONLY));
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
					results.add(new DiffChunk<SQLObject>(sourceTable, DiffType.SAME));

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
			// If any tables in the sourceList still exist, the changes are added
			while (sourceContinue) {
				results.add(new DiffChunk<SQLObject>(sourceTable, DiffType.LEFTONLY));
				generateColumnDiffs(sourceTable, null);
				if (sourceIter.hasNext()) {
					sourceTable = (SQLTable) sourceIter.next();
				} else {
					sourceContinue = false;
				}
			}
			
			//If any remaining tables in the targetList still exist, they are now being added
			while (targetContinue) {

				results.add(new DiffChunk<SQLObject>(targetTable, DiffType.RIGHTONLY));
				generateColumnDiffs(null, targetTable);
				if (targetIter.hasNext()) {
					targetTable = (SQLTable) targetIter.next();
				} else {
					targetContinue = false;
				}
			}


		} finally {
			done = true;
		}
		return results;
	}
	
	private void generateColumnDiffs(SQLTable sourceTable, SQLTable targetTable) {
		TreeSet<SQLColumn> sourceColumnList;
		TreeSet<SQLColumn> targetColumnList;
		Iterator sourceColIter;
		Iterator targetColIter;
		SQLColumn sourceColumn;
		SQLColumn targetColumn;
		boolean sourceColContinue;
		boolean targetColContinue;

		sourceColumnList = new TreeSet<SQLColumn>(comparator);
		targetColumnList = new TreeSet<SQLColumn>(comparator);
		sourceColContinue = false;
		targetColContinue = false;
		sourceColIter = null;
		targetColIter = null;
		sourceColumn = null;
		targetColumn = null;


		try {
			if (sourceTable != null) {
				sourceColumnList.addAll(sourceTable.getColumns());
			}
			if (targetTable != null) {
				targetColumnList.addAll(targetTable.getColumns());
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

				// Comparing Columns
				while (comparator.compare(sourceColumn, targetColumn) < 0) {
					results.add(new DiffChunk<SQLObject>(sourceColumn, DiffType.LEFTONLY));
					if (sourceColIter.hasNext()) {
						sourceColumn = (SQLColumn) sourceColIter.next();
					} else {
						sourceColContinue = false;
						break;
					}

				}
				// Comparing Columns
				while (comparator.compare(sourceColumn, targetColumn) > 0) {
					results.add(new DiffChunk<SQLObject>(targetColumn, DiffType.RIGHTONLY));
					if (targetColIter.hasNext()) {
						targetColumn = (SQLColumn) targetColIter.next();
					} else {
						targetColContinue = false;
						break;
					}
				}

				// Comparing Columns
				while (comparator.compare(sourceColumn, targetColumn) == 0) {
					GenericTypeDescriptor td = targetType.get(targetColumn.getType());
					
					if (targetColumn.getType() != sourceColumn.getType() || 
							(td.getHasPrecision() && targetColumn.getPrecision() != sourceColumn.getPrecision()) 
							|| (td.getHasScale() && targetColumn.getScale() != sourceColumn.getScale()))
					{
					
						results.add(new DiffChunk<SQLObject>(sourceColumn, DiffType.MODIFIED));
						results.add(new DiffChunk<SQLObject>(targetColumn, DiffType.MODIFIED));
					}
					else {
						results.add(new DiffChunk<SQLObject>(sourceColumn, DiffType.SAME));
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
				results.add(new DiffChunk<SQLObject>(sourceColumn, DiffType.LEFTONLY));
				if (sourceColIter.hasNext()) {
					sourceColumn = (SQLColumn) sourceColIter.next();
				} else {
					sourceColContinue = false;
				}
			}
			
			while (targetColContinue) {
				results.add(new DiffChunk<SQLObject>(targetColumn, DiffType.RIGHTONLY));
				if (targetColIter.hasNext()) {
					targetColumn = (SQLColumn) targetColIter.next();
				} else {
					targetColContinue = false;
				}
			}
			
		} catch (ArchitectException e) {
			logger.debug("Architect exception in compareSchemaWorker", e);

		}

	}
	



}
