/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;

/**
 * 
 * Used to filter SQLObjects from an import operation. It was originally made to determine
 * if a copy and paste operation between different sessions would result
 * in losing ETL lineage, and notifies the user giving them the option
 * to continue the operation or not. However, it is hopefully designed in such
 * a way that other filters could be easily added or implemented.
 * 
 */

class ImportSafetyChecker {
    
    /**
     * The session having objects imported into it.
     */
    private final ArchitectSwingSession targetSession;
    
    /**
     * Set true by the user prompt if the whole import is to be cancelled (everything will be filtered).
     */
    private boolean isCancelled = false;
    
    /**
     * Used to prompt the user when ETL lineage will be lost as a result of copying across sessions.
     */
    UserPrompter loseLineage; 

    ImportSafetyChecker(ArchitectSwingSession session) {
        
        this.targetSession = session;
        
        loseLineage = targetSession.createUserPrompter(
                "One or more columns in {0} have ETL lineage from their source session. " +
                "\nThis lineage will not be able to be copied along with them. " +
                "\nCopy {0} anyway, and lose ETL lineage of one or more columns?", 
                UserPromptType.BOOLEAN, UserPromptOptions.OK_NOTOK_CANCEL, 
                UserPromptResponse.NOT_OK, UserPromptResponse.NOT_OK, "Yes", "No", "Cancel"); 
        
    }
    
    List<SQLObject> filterImportedItems(Collection<SQLObject> items) {
        List<SQLObject> acceptedItems = new ArrayList<SQLObject>();
        
        for (SQLObject item : items) {
            
            if (accept(item)) {
                acceptedItems.add(item);
            }
            
            if (isCancelled) {
                return Collections.emptyList();
            }
            
        }
        
        return acceptedItems;
    }

    /**
     * Checks if this item should not be filtered out from the import operation.
     */
    private boolean accept(SQLObject item) { 
        
        SPObject parent = item;              
        
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }        
        
        // Accept the item if it is in the same project/session, since ETL lineage won't be a problem.
        if (targetSession.getWorkspace() == parent) {            
            return true;
        }
        if (!visit(item)) {               
            UserPromptResponse response = loseLineage.promptUser(item.getPhysicalName());            
            if (response == UserPromptResponse.CANCEL) {
                isCancelled = true;
            }
            return (response == UserPromptResponse.OK);
        } else {
            return true;
        }
    }
    
    /**
     * Checks if this item and its entire subtree pass the filter.
     */
    public boolean visit(SQLObject item) {
                
        boolean subtreeAccepted = true;
        
        if (item instanceof SQLColumn) {            
            return sourceDatabaseAccessible((SQLColumn) item, targetSession);
        }
        
        for (SQLObject child : item.getChildren()) {
            if (!(subtreeAccepted &= visit(child))) {
                break;
            }
        }        
        return subtreeAccepted;
    }

    /**
     * Checks if the given sourceColumn's source database is accessible from the targetSession.
     * If not, ETL lineage is impossible to preserve.
     */
    private boolean sourceDatabaseAccessible(SQLColumn sourceColumn, ArchitectSwingSession targetSession) {        
                    
        SQLDatabase sourceSourceDatabase;
        
        // The source column could be its own source if is is being
        // copied directly from a database, not a play pen.
        if (sourceColumn.getSourceColumn() == null) {
            sourceColumn.setSourceColumn(sourceColumn);
        }
        sourceSourceDatabase = SQLPowerUtils.getAncestor(sourceColumn.getSourceColumn(), SQLDatabase.class);

        if (targetSession.getDBTree().getDuplicateDbcs(
                sourceSourceDatabase.getDataSource()) == null) {
            return false;         
        } else {
            return true;            
        }
           
    }
}
