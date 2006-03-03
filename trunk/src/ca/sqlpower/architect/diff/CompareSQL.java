package ca.sqlpower.architect.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.GenericTypeDescriptor;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.Monitorable;
import ca.sqlpower.architect.swingui.SwingUIProject;

public class CompareSQL implements Monitorable {

	private static final Logger logger = Logger.getLogger(CompareSQL.class);

	/**
	 * A comparator that compares SQL Objects by name.
	 */
	final static Comparator<SQLObject> comparator = new SQLObjectComparator();

	/**
	 * The source tables that this compare object will use when asked
	 * to generate diffs.
	 */
	private TreeSet<SQLTable> sourceTableSet;

	/**
	 * The target tables that this compare object will use when asked
	 * to generate diffs.
	 */
	private TreeSet<SQLTable> targetTableSet;

	/**
	 * The amount of work that needs to be done (for the progress monitor).
	 */
	private int jobSize;

	/**
	 * The amount of work that has been done (for the progress monitor).
	 */
	private int progress;

	/**
	 * A flag to indicate that we're done (in case the process runs into
	 * an exception, progress may not equal jobSize by the time this is set
	 * true).
	 */
	private boolean finished;

	/**
	 * The results we are working on (this will be returned by generateTableDiffs()).
	 */
	List<DiffChunk<SQLObject>> results;

	/**
	 * The table we're working on right now (for the progress monitor).
	 */
	private String currentTableName;

	/**
	 * The common constructor 
	 * @throws ArchitectDiffException When the source or target table collections
	 * contain tables that have the same name as each other.
	 */
	public CompareSQL(
			Collection<SQLTable> sourceTables,
			Collection<SQLTable> targetTables) throws ArchitectDiffException {
		
		boolean sourceValid = true;
		boolean targetValid = true;
		this.sourceTableSet = new TreeSet<SQLTable>(comparator);
		this.sourceTableSet.addAll(sourceTables);
		this.targetTableSet = new TreeSet<SQLTable>(comparator);
		this.targetTableSet.addAll(targetTables);
		
		
		
		if (sourceTableSet.size() != sourceTables.size()){
			sourceValid = false;
		}
		if (targetTableSet.size() != targetTables.size()){
			targetValid = false;
		}
		if (!sourceValid || !targetValid){
			String error;
			if (!sourceValid && !targetValid) {
				error = "Your source and target both have tables with duplicate names.";
			} else if (!sourceValid){
				error = "Your source has tables with the same name as each other.";
			} else {
				error = "Your target has tables with the same name as each other.";
			}
			throw new ArchitectDiffException(error);
		}

		
		
			
		results = new ArrayList<DiffChunk<SQLObject>>();
		setProgress(0);
		setJobSize(targetTableSet.size() + sourceTableSet.size());
		setFinished(false);
	}
	
	public List<DiffChunk<SQLObject>> generateTableDiffs() throws ArchitectException {
		try {
			Iterator sourceIter = sourceTableSet.iterator();
			Iterator targetIter = targetTableSet.iterator();
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
					results.addAll(generateColumnDiffs(sourceTable, null));
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
					results.addAll(generateColumnDiffs(null, targetTable));
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
					results.addAll(generateColumnDiffs(sourceTable, targetTable));
					if (!targetIter.hasNext() && !sourceIter.hasNext())
					{
						targetContinue = false;
						sourceContinue = false;
						break;
					}
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
				results.addAll(generateColumnDiffs(sourceTable, null));
				if (sourceIter.hasNext()) {
					sourceTable = (SQLTable) sourceIter.next();
				} else {
					sourceContinue = false;
				}
			}
			
			//If any remaining tables in the targetList still exist, they are now being added
			while (targetContinue) {

				results.add(new DiffChunk<SQLObject>(targetTable, DiffType.RIGHTONLY));
				results.addAll(generateColumnDiffs(null, targetTable));
				if (targetIter.hasNext()) {
					targetTable = (SQLTable) targetIter.next();
				} else {
					targetContinue = false;
				}
			}

			results.addAll(generateRelationshipDiffs(sourceTableSet, targetTableSet));

		} finally {
			setFinished(true);
		}
		return results;
	}
	
	private List<DiffChunk<SQLObject>> generateRelationshipDiffs(
			Collection<SQLTable> sourceTables, Collection<SQLTable> targetTables) throws ArchitectException {
		
		//XXX: This is using an incorrect comparator, it only checks by relationship
		//names and does not get into a deeper level of checking the mappings.
		Set<SQLRelationship> sourceRels = new TreeSet<SQLRelationship>(comparator);
		Set<SQLRelationship> targetRels = new TreeSet<SQLRelationship>(comparator);
		
		for (SQLTable t : sourceTables) {
			sourceRels.addAll(t.getImportedKeys());
		}
		
		for (SQLTable t : targetTables) {
			targetRels.addAll(t.getImportedKeys());
		}

		logger.debug("Source relationships: "+sourceRels);
		logger.debug("Target relationships: "+targetRels);

		List<DiffChunk<SQLObject>> diffs = new ArrayList<DiffChunk<SQLObject>>();
		
		Iterator<SQLRelationship> sourceIter = sourceRels.iterator();
		Iterator<SQLRelationship> targetIter = targetRels.iterator();
		SQLRelationship targetRel;
		SQLRelationship sourceRel;
		boolean sourceContinue;
		boolean targetContinue;

		//Checks if both lists of tables contain any tables at all, if they do
		//the iterator is initialized for the list
		if (sourceIter.hasNext()) {

			sourceContinue = true;
			sourceRel = sourceIter.next();
		} else {
			sourceContinue = false;
			sourceRel = null;
		}

		if (targetIter.hasNext()) {
			targetContinue = true;
			targetRel = targetIter.next();
		} else {
			targetContinue = false;
			targetRel = null;
		}
		

		// Will loop until one or both of the lists reaches its last table
		while (sourceContinue && targetContinue) {
			// bring the source table up to the same level as the target
			while (comparator.compare(sourceRel, targetRel) < 0) {
				diffs.add(new DiffChunk<SQLObject>(sourceRel, DiffType.LEFTONLY));
				diffs.addAll(generateMappingDiffs(sourceRel, null));
				if (sourceIter.hasNext()) {
					sourceRel = sourceIter.next();
				} else {
					sourceContinue = false;
					break;
				}
			}

			// bring the target table up to the same level as the source
			while (comparator.compare(sourceRel, targetRel) > 0) {
				diffs.add(new DiffChunk<SQLObject>(targetRel, DiffType.RIGHTONLY));
				// now do the mappings
				diffs.addAll(generateMappingDiffs(null, targetRel));
				if (targetIter.hasNext()) {
					targetRel = targetIter.next();
				} else {
					targetContinue = false;
					break;
				}
			}

			while (comparator.compare(sourceRel, targetRel) == 0) {
				diffs.add(new DiffChunk<SQLObject>(sourceRel, DiffType.SAME));

				// now do the columns
				diffs.addAll(generateMappingDiffs(sourceRel, targetRel));
				if (!targetIter.hasNext() && !sourceIter.hasNext())
				{
					targetContinue = false;
					sourceContinue = false;
					break;
				}
				if (targetIter.hasNext()) {
					targetRel = targetIter.next();
				} else {
					targetContinue = false;
					break;
				}

				if (sourceIter.hasNext()) {
					sourceRel = sourceIter.next();
				}

				else {
					sourceContinue = false;
					break;
				}
			}

		}
		// If any tables in the sourceList still exist, the changes are added
		while (sourceContinue) {
			diffs.add(new DiffChunk<SQLObject>(sourceRel, DiffType.LEFTONLY));
			diffs.addAll(generateMappingDiffs(sourceRel, null));
			if (sourceIter.hasNext()) {
				sourceRel = sourceIter.next();
			} else {
				sourceContinue = false;
			}
		}
		
		//If any remaining tables in the targetList still exist, they are now being added
		while (targetContinue) {			
			diffs.add(new DiffChunk<SQLObject>(targetRel, DiffType.RIGHTONLY));
			diffs.addAll(generateMappingDiffs(null, targetRel));
			if (targetIter.hasNext()) {
				targetRel = targetIter.next();
			} else {
				targetContinue = false;
			}
		}	
		return diffs;
	}

	private List<DiffChunk<SQLObject>> generateMappingDiffs(
			SQLRelationship sourceRel,
			SQLRelationship targetRel) {
		
		return Collections.EMPTY_LIST;  // TODO: create a real diff list
	}

	/**
	 * Creates a List of DiffChunks that describe the differences between the
	 * columns of the given tables.
	 * 
	 * @param sourceTable The "left side" for the comparison.  If null, then all columns
	 * in the target table will be considered obsolete.
	 * @param targetTable The "right side" for the comparison.  If null, then all columns
	 * in the source table will be considered new.
	 * @throws ArchitectException If the getColumns() methods of the source or target
	 * tables run into trouble.
	 */
	private List<DiffChunk<SQLObject>> generateColumnDiffs(
			SQLTable sourceTable,
			SQLTable targetTable) throws ArchitectException {
		TreeSet<SQLColumn> sourceColumnList;
		TreeSet<SQLColumn> targetColumnList;
		Iterator<SQLColumn> sourceColIter;
		Iterator<SQLColumn> targetColIter;
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

		// We store the diffs in here, then return this listS
		List<DiffChunk<SQLObject>> diffs = new ArrayList<DiffChunk<SQLObject>>();

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
			sourceColumn = sourceColIter.next();
			sourceColContinue = true;
		}

		if (targetColumnList.size() == 0) {
			targetColumnList = null;
			targetColContinue = false;
		} else {

			targetColIter = targetColumnList.iterator();
			targetColumn = targetColIter.next();
			targetColContinue = true;
		}

		while (sourceColContinue && targetColContinue) {

			// Comparing Columns
			while (comparator.compare(sourceColumn, targetColumn) < 0) {
				diffs.add(new DiffChunk<SQLObject>(sourceColumn,
						DiffType.LEFTONLY));
				if (sourceColIter.hasNext()) {
					sourceColumn = sourceColIter.next();
				} else {
					sourceColContinue = false;
					break;
				}

			}
			// Comparing Columns
			while (comparator.compare(sourceColumn, targetColumn) > 0) {
				diffs.add(new DiffChunk<SQLObject>(targetColumn,
						DiffType.RIGHTONLY));
				if (targetColIter.hasNext()) {
					targetColumn = targetColIter.next();
				} else {
					targetColContinue = false;
					break;
				}
			}

			// Comparing Columns
			while (comparator.compare(sourceColumn, targetColumn) == 0) {
				
				if (targetColumn.getType() != sourceColumn.getType()
					|| (targetColumn.getPrecision() != sourceColumn.getPrecision())
					|| (targetColumn.getScale() != sourceColumn.getScale())
					|| (targetColumn.getNullable() != sourceColumn.getNullable())
					) {

					diffs.add(new DiffChunk<SQLObject>(targetColumn, DiffType.MODIFIED));
				} else {
					diffs.add(new DiffChunk<SQLObject>(sourceColumn, DiffType.SAME));
				}
				if (targetColIter.hasNext()) {
					targetColumn = targetColIter.next();
				} else {
					targetColContinue = false;
				}

				if (sourceColIter.hasNext()) {
					sourceColumn = sourceColIter.next();
				} else {
					sourceColContinue = false;
				}
				if (!sourceColContinue || !targetColContinue) {
					break;
				}
			}
		}
		while (sourceColContinue) {
			diffs.add(new DiffChunk<SQLObject>(sourceColumn, DiffType.LEFTONLY));
			if (sourceColIter.hasNext()) {
				sourceColumn = sourceColIter.next();
			} else {
				sourceColContinue = false;
			}
		}

		while (targetColContinue) {
			diffs.add(new DiffChunk<SQLObject>(targetColumn, DiffType.RIGHTONLY));
			if (targetColIter.hasNext()) {
				targetColumn = targetColIter.next();
			} else {
				targetColContinue = false;
			}
		}

		return diffs;
	}
	
	
	// ------------------ Monitorable Interface --------------------

	public synchronized Integer getJobSize() {
		return jobSize;
	}

	public synchronized int getProgress() {
		return progress;
	}

	public synchronized boolean hasStarted() throws ArchitectException {
		return true;
	}

	public synchronized boolean isFinished() throws ArchitectException {
		return finished;
	}

	public synchronized String getMessage() {
		return currentTableName;
	}

	public synchronized void setCancelled(boolean cancelled) {
		cancelled = true;
	}
	
	private synchronized void setJobSize(int jobSize) {
		this.jobSize = jobSize;
	}
	
	private synchronized void setProgress(int progress) {
		this.progress = progress;
	}

	public synchronized void setFinished(boolean finished) {
		this.finished = finished;
	}
	

}
