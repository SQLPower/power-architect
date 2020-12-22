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
 */
package ca.sqlpower.architect.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.dao.PersisterUtils;
import ca.sqlpower.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.diff.DiffChunk;
import ca.sqlpower.diff.DiffType;
import ca.sqlpower.diff.PropertyChange;
import ca.sqlpower.object.SPObjectUUIDComparator;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.util.Monitorable;

public class CompareSQL implements Monitorable {

    private static final char LEFT_RIGHT_ARROW = '\u2194';
    private static final char LEFT_ARROW = '\u2190';
    private static final char RIGHT_ARROW = '\u2192';

    private static final Logger logger = Logger.getLogger(CompareSQL.class);

	/**
	 * A comparator that compares SQL Objects by name.
	 */
	final static Comparator<SQLObject> nameComparator = new SQLObjectComparator();

	/**
	 * A comparator that detects equality by comparing the UUID
	 */
    final static Comparator<SQLObject> uuidComparator = new SPObjectUUIDComparator<SQLObject>();

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
	private Integer jobSize;

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
     * Flags whether or not this comparison has been cancelled.
     */
    private boolean cancelled;

	/**
	 * The results we are working on (this will be returned by generateTableDiffs()).
	 */
	List<DiffChunk<SQLObject>> results;

	/**
	 * The table we're working on right now (for the progress monitor).
	 */
	private String progressMessage;

	/**
	 * A switch to indicate whether indices with be compared.
	 */
	private boolean compareIndex;

	/**
	 * Flags whether or not the comparison has started.
	 */
    private boolean started;
    private boolean suppressSimilarities;

	private boolean useUUID;

	public CompareSQL(
			Collection<SQLTable> sourceTables,
			Collection<SQLTable> targetTables, boolean suppressSimilarities) throws ArchitectDiffException {
		this(sourceTables, targetTables, suppressSimilarities, false);
	}

	/**
	 * @throws ArchitectDiffException When the source or target table collections
	 * contain tables that have the same name as each other.
	 */
	public CompareSQL(
			Collection<SQLTable> sourceTables,
			Collection<SQLTable> targetTables,
			boolean suppressSimilarities, boolean useUUID) throws ArchitectDiffException {

		boolean sourceValid = true;
		boolean targetValid = true;
		this.useUUID = useUUID;
		this.sourceTableSet = new TreeSet<SQLTable>(getObjectComparator());
		this.sourceTableSet.addAll(sourceTables);
		this.targetTableSet = new TreeSet<SQLTable>(getObjectComparator());
		this.targetTableSet.addAll(targetTables);
		this.suppressSimilarities = suppressSimilarities;


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
		setJobSize(targetTableSet.size()*2 + sourceTableSet.size()*2);
		setFinished(false);
		setStarted(false);
	}
	
	public List<DiffChunk<SQLObject>> generateTableDiffs() throws SQLObjectException {
	return generateTableDiffs(null);
	}
	
	public List<DiffChunk<SQLObject>> generateTableDiffs(ArchitectSession session) throws SQLObjectException {
		setStarted(true);
		setFinished(false);
	    try {
			Iterator<SQLTable> sourceIter = sourceTableSet.iterator();
			Iterator<SQLTable> targetIter = targetTableSet.iterator();
			SQLTable targetTable;
			SQLTable sourceTable;
			boolean sourceContinue;
			boolean targetContinue;

			//Checks if both lists of tables contain any tables at all, if they do
			//the iterator is initialized for the list
			if (sourceIter.hasNext()) {

				sourceContinue = true;
				sourceTable = sourceIter.next();
				sourceTable.populate();
			} else {
				sourceContinue = false;
				sourceTable = null;
			}

			if (targetIter.hasNext()) {
				targetContinue = true;
				targetTable = targetIter.next();
				targetTable.populate();
			} else {
				targetContinue = false;
				targetTable = null;
			}

			// Will loop until one or both the list reaches its last table
			while (sourceContinue && targetContinue && !isCancelled()) {
			    logger.debug("Generating table diffs for " + sourceTable.getName());
			    if (session != null) {
			        ArchitectUtils.setUpstreamTypesInTable(sourceTable, session);
			        ArchitectUtils.setUpstreamTypesInTable(targetTable, session);
			    }
			    DiffChunk<SQLObject> chunk = null;
			    int compareResult = getObjectComparator().compare(sourceTable, targetTable);

				// bring the source table up to the same level as the target
				if (compareResult < 0) {
					chunk = new DiffChunk<SQLObject>(sourceTable, DiffType.LEFTONLY);
					incProgress(1, sourceTable, targetTable);
					//results.addAll(generateColumnDiffs(sourceTable, null));
					if (sourceIter.hasNext()) {
						sourceTable = (SQLTable) sourceIter.next();
						sourceTable.populate();
					} else {
						sourceContinue = false;

					}
				}

				// bring the target table up to the same level as the source
				if (compareResult > 0) {
					chunk = new DiffChunk<SQLObject>(targetTable, DiffType.RIGHTONLY);
					incProgress(1, sourceTable, targetTable);
					// now don't do the columns it's already handled
					//results.addAll(generateColumnDiffs(null, targetTable));
					if (targetIter.hasNext()) {
						targetTable = (SQLTable) targetIter.next();
						targetTable.populate();
					} else {
						targetContinue = false;
					}
				}

				if (compareResult == 0) {
                    List<PropertyChange> changes = generatePropertyChanges(sourceTable, targetTable);
					if (changes.size() > 0) {
						if (nameComparator.compare(sourceTable, targetTable) != 0) {
							chunk = new DiffChunk<SQLObject>(targetTable, DiffType.NAME_CHANGED);
							chunk.setOriginalData(sourceTable);
						} else if (!StringUtils.equals(sourceTable.getRemarks(), targetTable.getRemarks())) {
                            // If the remarks are the same, then don't generate SQL script
                            chunk = new DiffChunk<SQLObject>(sourceTable, DiffType.SQL_MODIFIED);
                        } else {
                            // If the remarks were changed, generate SQL script for that
                            chunk = new DiffChunk<SQLObject>(sourceTable, DiffType.MODIFIED);
                        }
                        for (PropertyChange change : changes) {
                            chunk.addPropertyChange(change);
                        }
                    } else {
                        chunk = new DiffChunk<SQLObject>(sourceTable, DiffType.SAME);
                    }
                    incProgress(1, sourceTable, targetTable);
                    List<DiffChunk<SQLObject>> columns = generateColumnDiffs(sourceTable, targetTable);
                    if (!(chunk.getType() == DiffType.SAME && suppressSimilarities) || columns.size() > 0) {
                        results.add(chunk);
                        results.addAll(columns);
                    }
					if (!targetIter.hasNext() && !sourceIter.hasNext())
					{
						targetContinue = false;
						sourceContinue = false;
					}
					if (targetIter.hasNext()) {
						targetTable = (SQLTable) targetIter.next();
						targetTable.populate();
					} else {
						targetContinue = false;
					}

					if (sourceIter.hasNext()) {
						sourceTable = (SQLTable) sourceIter.next();
						sourceTable.populate();
					}

					else {
						sourceContinue = false;
					}
				} else {
				    // If we are adding column diffs, the table diff must be in the list first.
				    // If we are not generating column diffs, we have waited until now to add the chunk.
				    results.add(chunk);
				}

			}
			// If any tables in the sourceList still exist, the changes are added
			while (sourceContinue && !isCancelled()) {
			    if (session != null) {
			        ArchitectUtils.setUpstreamTypesInTable(sourceTable, session);
			    }
				results.add(new DiffChunk<SQLObject>(sourceTable, DiffType.LEFTONLY));
				incProgress(1, sourceTable, null);
				//results.addAll(generateColumnDiffs(sourceTable, null));
				if (sourceIter.hasNext()) {
					sourceTable = (SQLTable) sourceIter.next();
					sourceTable.populate();
				} else {
					sourceContinue = false;
				}
			}

			//If any remaining tables in the targetList still exist, they are now being added
			while (targetContinue && !isCancelled()) {
			    if (session != null) {
			        ArchitectUtils.setUpstreamTypesInTable(targetTable, session);
			    }
				results.add(new DiffChunk<SQLObject>(targetTable, DiffType.RIGHTONLY));
				incProgress(1, null, targetTable);

				//results.addAll(generateColumnDiffs(null, targetTable));
				if (targetIter.hasNext()) {
					targetTable = (SQLTable) targetIter.next();
					targetTable.populate();
				} else {
					targetContinue = false;
				}
			}
			results.addAll(generateRelationshipDiffs(sourceTableSet, targetTableSet));

			if (compareIndex) {
			    results.addAll(generateIndexDiffs(sourceTableSet, targetTableSet));
			}
		} finally {
			setJobSize(null);
			setFinished(true);
		}
		return results;
	}

    private List<DiffChunk<SQLObject>> generateRelationshipDiffs(
			Collection<SQLTable> sourceTables, Collection<SQLTable> targetTables) throws SQLObjectException {
		SQLRelationshipComparator relComparator = new SQLRelationshipComparator(useUUID);
		Set<SQLRelationship> sourceRels = new TreeSet<SQLRelationship>(relComparator);
		Set<SQLRelationship> targetRels = new TreeSet<SQLRelationship>(relComparator);
		

		for (SQLTable t : sourceTables) {
			incProgress(1, t, null);
			sourceRels.addAll(SQLRelationship.getExportedKeys(t.getImportedKeys()));
		}

		for (SQLTable t : targetTables) {
			incProgress(1, null, t);
			targetRels.addAll(SQLRelationship.getExportedKeys(t.getImportedKeys()));
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

			int compareResult = relComparator.compare(sourceRel, targetRel);

			// bring the source table up to the same level as the target
			if (compareResult < 0) {
				diffs.add(new DiffChunk<SQLObject>(sourceRel, DiffType.LEFTONLY));
				if (sourceIter.hasNext()) {
					sourceRel = sourceIter.next();
				} else {
					sourceContinue = false;
				}
			}

			// bring the target table up to the same level as the source
			if (compareResult > 0) {
				diffs.add(new DiffChunk<SQLObject>(targetRel, DiffType.RIGHTONLY));
				// now do the mappings
				if (targetIter.hasNext()) {
					targetRel = targetIter.next();
				} else {
					targetContinue = false;
				}
			}

			if (compareResult == 0) {
			    List<PropertyChange> changes = generatePropertyChanges(sourceRel, targetRel);
			    if (changes.size() > 0) {

					DiffChunk<SQLObject> chunk = null;
					if (nameComparator.compare(sourceRel, targetRel) != 0) {
						chunk = new DiffChunk<SQLObject>(targetRel, DiffType.NAME_CHANGED);
						chunk.setOriginalData(sourceRel);
					} else {
						chunk = new DiffChunk<SQLObject>(sourceRel, DiffType.MODIFIED);
					}
			        for (PropertyChange change : changes) {
			           chunk.addPropertyChange(change);
			        }
			        diffs.add(chunk);
			    } else {
			        if (!suppressSimilarities) {
			            diffs.add(new DiffChunk<SQLObject>(sourceRel, DiffType.SAME));
			        }
			    }

				// now do the columns
				if (!targetIter.hasNext() && !sourceIter.hasNext())
				{
					targetContinue = false;
					sourceContinue = false;
				}
				if (targetIter.hasNext()) {
					targetRel = targetIter.next();
				} else {
					targetContinue = false;
				}

				if (sourceIter.hasNext()) {
					sourceRel = sourceIter.next();
				} else {
					sourceContinue = false;
				}
			}

		}
		// If any tables in the sourceList still exist, the changes are added
		while (sourceContinue) {
			diffs.add(new DiffChunk<SQLObject>(sourceRel, DiffType.LEFTONLY));
			if (sourceIter.hasNext()) {
				sourceRel = sourceIter.next();
			} else {
				sourceContinue = false;
			}
		}

		//If any remaining tables in the targetList still exist, they are now being added
		while (targetContinue) {
			diffs.add(new DiffChunk<SQLObject>(targetRel, DiffType.RIGHTONLY));
			if (targetIter.hasNext()) {
				targetRel = targetIter.next();
			} else {
				targetContinue = false;
			}
		}
		return diffs;
	}
	

	/**
     * Creates a List of DiffChunks that describe the differences between the
     * indices of the given tables.
     * 
     *
     * @param sourceTables The "left side" for the comparison.  If null, then all indices
     * in the target table will be considered obsolete.
     * @param targetTables The "right side" for the comparison.  If null, then all indices
     * in the source table will be considered new.
	 * @throws SQLObjectException If the getIndices() methods of the source or target
     * tables run into trouble.
     * @return the list of DiffChunk
     */
	private List<DiffChunk<SQLObject>> generateIndexDiffs(
	        Collection<SQLTable> sourceTables, Collection<SQLTable> targetTables) throws SQLObjectException {
	    SQLIndexComparator indComparator = new SQLIndexComparator(useUUID);
	    Set<SQLIndex> sourceInds = new TreeSet<SQLIndex>(indComparator);
	    Set<SQLIndex> targetInds = new TreeSet<SQLIndex>(indComparator);

	    for (SQLTable t : sourceTables) {
	        incProgress(1, t, null);
	        if (t.getIndices() != null){
	            sourceInds.addAll(t.getIndices());
	        }
	    }

	    for (SQLTable t : targetTables) {
	        incProgress(1, null, t);
	        if (t.getIndices() != null){
	            targetInds.addAll(t.getIndices());
	        }
	    }

	    logger.debug("Source indices: "+sourceInds);
	    logger.debug("Target indices: "+targetInds);

	    List<DiffChunk<SQLObject>> diffs = new ArrayList<DiffChunk<SQLObject>>();

	    Iterator<SQLIndex> sourceIter = sourceInds.iterator();
	    Iterator<SQLIndex> targetIter = targetInds.iterator();
	    SQLIndex targetInd;
	    SQLIndex sourceInd;
	    boolean sourceContinue;
	    boolean targetContinue;

	    //Checks if both lists of tables contain any tables at all, if they do
	    //the iterator is initialized for the list
	    if (sourceIter.hasNext()) {
	        sourceContinue = true;
	        sourceInd = sourceIter.next();
	    } else {
	        sourceContinue = false;
	        sourceInd = null;
	    }

	    if (targetIter.hasNext()) {
	        targetContinue = true;
	        targetInd = targetIter.next();
	    } else {
	        targetContinue = false;
	        targetInd = null;
	    }


	    // Will loop until one or both of the lists reaches its last table
	    while (sourceContinue && targetContinue) {

			int compareResult = indComparator.compare(sourceInd, targetInd);
	        // bring the source table up to the same level as the target
	        if (compareResult < 0) {
	            diffs.add(new DiffChunk<SQLObject>(sourceInd, DiffType.LEFTONLY));
	            if (sourceIter.hasNext()) {
	                sourceInd = sourceIter.next();
	            } else {
	                sourceContinue = false;
	            }
	        }

	        // bring the target table up to the same level as the source
	        if (compareResult > 0) {
	            diffs.add(new DiffChunk<SQLObject>(targetInd, DiffType.RIGHTONLY));
	            // now do the mappings
	            if (targetIter.hasNext()) {
	                targetInd = targetIter.next();
	            } else {
	                targetContinue = false;
	            }
	        }

	        if (compareResult == 0) {
	            List<PropertyChange> changes = generatePropertyChanges(sourceInd, targetInd);

	            if (changes.size() > 0) {
					DiffChunk<SQLObject> chunk = null;
					if (nameComparator.compare(sourceInd, targetInd) != 0) {
						chunk = new DiffChunk<SQLObject>(targetInd, DiffType.NAME_CHANGED);
						chunk.setOriginalData(sourceInd);
					} else {
						chunk = new DiffChunk<SQLObject>(sourceInd, DiffType.MODIFIED);
					}
	                for (PropertyChange change : changes) {
	                    chunk.addPropertyChange(change);
	                }
	                diffs.add(chunk);
	            } else {
	                if (!suppressSimilarities) {
	                    diffs.add(new DiffChunk<SQLObject>(sourceInd, DiffType.SAME));
	                }
	            }
	            // now do the columns
	            if (!targetIter.hasNext() && !sourceIter.hasNext()) {
	                targetContinue = false;
	                sourceContinue = false;
	            }
	            if (targetIter.hasNext()) {
	                targetInd = targetIter.next();
	            } else {
	            }

	            if (sourceIter.hasNext()) {
	                sourceInd = sourceIter.next();
	            } else {
	                sourceContinue = false;
	            }
	        }

	    }
	    // If any tables in the sourceList still exist, the changes are added
	    while (sourceContinue) {
	        diffs.add(new DiffChunk<SQLObject>(sourceInd, DiffType.LEFTONLY));
	        if (sourceIter.hasNext()) {
	            sourceInd = sourceIter.next();
	        } else {
	            sourceContinue = false;
	        }
	    }

	    //If any remaining tables in the targetList still exist, they are now being added
	    while (targetContinue) {
	        diffs.add(new DiffChunk<SQLObject>(targetInd, DiffType.RIGHTONLY));
	        if (targetIter.hasNext()) {
	            targetInd = targetIter.next();
	        } else {
	            targetContinue = false;
	        }
	    }
	    return diffs;
	}

	/**
	 * Creates a List of DiffChunks that describe the differences between the
	 * columns of the given tables.
	 *
	 * @param sourceTable The "left side" for the comparison.  If null, then all columns
	 * in the target table will be considered obsolete.
	 * @param targetTable The "right side" for the comparison.  If null, then all columns
	 * in the source table will be considered new.
	 * @throws SQLObjectException If the getColumns() methods of the source or target
	 * tables run into trouble.
	 */
	private List<DiffChunk<SQLObject>> generateColumnDiffs(
			SQLTable sourceTable,
			SQLTable targetTable) throws SQLObjectException {
		TreeSet<SQLColumn> sourceColumnList;
		TreeSet<SQLColumn> targetColumnList;
		Iterator<SQLColumn> sourceColIter;
		Iterator<SQLColumn> targetColIter;
		SQLColumn sourceColumn;
		SQLColumn targetColumn;
		boolean sourceColContinue;
		boolean targetColContinue;
		boolean keyChangeFlag = false;

		sourceColumnList = new TreeSet<SQLColumn>(getObjectComparator());
		targetColumnList = new TreeSet<SQLColumn>(getObjectComparator());
		sourceColContinue = false;
		targetColContinue = false;
		sourceColIter = null;
		targetColIter = null;
		sourceColumn = null;
		targetColumn = null;

		// We store the diffs in here, then return this list
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

			int compareResult = getObjectComparator().compare(sourceColumn, targetColumn);
			// Comparing Columns
			if (compareResult < 0) {
				diffs.add(new DiffChunk<SQLObject>(sourceColumn,
						DiffType.LEFTONLY));
				logger.debug("The source column is " + sourceColumn);
				if (sourceColumn.isPrimaryKey()) {
                    keyChangeFlag = true;
                }
				if (sourceColIter.hasNext()) {
					sourceColumn = sourceColIter.next();
				} else {
					sourceColContinue = false;
					

				}

			}
			// Comparing Columns
			if (compareResult > 0) {
				diffs.add(new DiffChunk<SQLObject>(targetColumn,
						DiffType.RIGHTONLY));
				logger.debug("The target column is " + targetColumn);
				if (targetColumn.isPrimaryKey()) {
                    keyChangeFlag = true;
				}
				if (targetColIter.hasNext()) {
					targetColumn = targetColIter.next();
				} else {
					targetColContinue = false;
					

				}
			}

			// Comparing Columns
			if (compareResult == 0) {

				if (targetColumn.isPrimaryKey() != sourceColumn.isPrimaryKey()){
				    keyChangeFlag = true;
					//diffs.add(new DiffChunk<SQLObject>(targetColumn, DiffType.KEY_CHANGED));
				}

				List<PropertyChange> changes = generatePropertyChanges(sourceColumn, targetColumn);
				if (changes.size() > 0) {
					DiffChunk<SQLObject> chunk = null;

					if (nameComparator.compare(sourceColumn, targetColumn) != 0) {
						chunk = new DiffChunk<SQLObject>(targetColumn, DiffType.NAME_CHANGED);
						chunk.setOriginalData(sourceColumn);
					} else if (ArchitectUtils.columnsDiffer(targetColumn, sourceColumn)) {
						// Make sure the changes are worthy of a SQL script generation:
	                    if (logger.isDebugEnabled()) {
	                        logger.debug("Column " + sourceColumn.getName() + " differs!");
	                        logger.debug(String.format("  Type:      %10d %10d", targetColumn.getType(), sourceColumn.getType()));
	                        logger.debug(String.format("  Precision: %10d %10d", targetColumn.getPrecision(), sourceColumn.getPrecision()));
	                        logger.debug(String.format("  Scale:     %10d %10d", targetColumn.getScale(), sourceColumn.getScale()));
	                        logger.debug(String.format("  Nullable:  %10d %10d", targetColumn.getNullable(), sourceColumn.getNullable()));
	                    }
	                    chunk = new DiffChunk<SQLObject>(targetColumn, DiffType.SQL_MODIFIED);
	                } else {
						chunk = new DiffChunk<SQLObject>(targetColumn, DiffType.MODIFIED);
					}

				    for (PropertyChange change : changes) {
				        chunk.addPropertyChange(change);
				    }
	                diffs.add(chunk);
	                
				} else {
				    if (!suppressSimilarities) {
				        diffs.add(new DiffChunk<SQLObject>(sourceColumn, DiffType.SAME));
				    }
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

		if (keyChangeFlag) {
		    if (sourceTable.getPkSize() > 0) {
		        diffs.add(new DiffChunk<SQLObject>(sourceTable, DiffType.DROP_KEY));
		    }
		    diffs.add(new DiffChunk<SQLObject>(targetTable, DiffType.KEY_CHANGED));
		}
		return diffs;
	}
	

	/**
     * This method gets two lists of interesting properties, and compares them against each other
     * to generate a list of PropertyChange.
     */
	private List<PropertyChange> generatePropertyChanges(SQLObject sourceObject, SQLObject targetObject)
	throws SQLObjectException {
	    

	    List<PropertyChange> changes = new ArrayList<PropertyChange>();
	    

	    try {
	        SessionPersisterSuperConverter converter = new SessionPersisterSuperConverter(null, sourceObject);
	        Map<String, Object> sourceProperties = PersisterUtils.getInterestingProperties(sourceObject, converter);
	        converter = new SessionPersisterSuperConverter(null, targetObject);
	        Map<String, Object> targetProperties = PersisterUtils.getInterestingProperties(targetObject, converter);
	        

	        Iterator<String> i = sourceProperties.keySet().iterator();
	        while (i.hasNext()) {
	            String propertyName = i.next();
	            String oldValue = String.valueOf(sourceProperties.get(propertyName));
	            String newValue = String.valueOf(targetProperties.get(propertyName));
	            if (oldValue.equals("")) oldValue = "null";
	            if (newValue.equals("")) newValue = "null";
	            if (!oldValue.equals(newValue)) {
	                logger.debug(propertyName + "differs");
	                changes.add(new PropertyChange(propertyName, oldValue, newValue));
	            }
	        }
	    } catch (Exception e) {
	        throw new SQLObjectException("Error generating property diffs", e);
	    }
	    

	    return changes;
	}

	private Comparator<SQLObject> getObjectComparator() {
		return useUUID ? uuidComparator : nameComparator;
	}

    // ------------------ Monitorable Interface --------------------

    public synchronized Integer getJobSize() {
		return jobSize;
	}

	public synchronized int getProgress() {
		return progress;
	}

	public synchronized boolean hasStarted() {
		return started;
	}

	private synchronized void setStarted(boolean started) {
	    this.started = started;
	}

	public synchronized boolean isFinished() {
		return finished;
	}

	public synchronized String getMessage() {
		return progressMessage;
	}

    public synchronized void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public synchronized boolean isCancelled() {
        return cancelled;
    }
	

	private synchronized void setJobSize(Integer jobSize) {
		this.jobSize = jobSize;
	}
	

	private synchronized void setProgress(int progress) {
		this.progress = progress;
	}

	/**
	 * Increments the progress by the given amount and changes the message
	 * to reflect the current objects being compared.
	 * 
	 *
	 * @param amount The number of units to increment progress by.
	 * @param lhs The current left-hand object of comparison. Can be null.
	 * @param rhs The current right-hand object of comparison. Can be null.
	 */
	private synchronized void incProgress(int amount, SQLObject lhs, SQLObject rhs) {
	    if (lhs != null && rhs != null) {
	        progressMessage = lhs + " " + LEFT_RIGHT_ARROW + " " + rhs;
	    } else if (lhs != null) {
	        progressMessage = LEFT_ARROW + " " + lhs.getName();
	    } else if (rhs != null) {
	        progressMessage = RIGHT_ARROW + " " + rhs.getName();
	    } else {
	        progressMessage = null;
	    }
		this.progress += amount;
	}
	

	public synchronized void setFinished(boolean finished) {
		this.finished = finished;
	}

	/**
     * Indicates whether table comparisons will include indices comparisons.
     */
    public boolean isCompareIndices() {
        return compareIndex;
    }

    /**
     * Sets the switch that determines whether table comparisons will include
     * indices comparisons.
     */
    public void setCompareIndices(boolean compareIndices) {
        this.compareIndex = compareIndices;
    }
}
