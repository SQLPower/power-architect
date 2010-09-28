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

package ca.sqlpower.architect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * The DepthFirstSearch class performs a depth-first search on a
 * SQLDatabase, treating it as a graph where the SQLTable objects
 * are the vertices and SQLRelationships are the edges that connect
 * them.
 *
 * @author fuerth
 * @version $Id$
 */
public class DepthFirstSearch {
    
    private static final Logger logger = Logger.getLogger(DepthFirstSearch.class);
    
    /**
     * Maps individual SQLTable objects (the vertices of our graph)
     * to their associated VertexInfo instances.  VertexInfo objects
     * store information about the DFS execution which will be of interest
     * to users of the class. 
     */
    private Map<SQLTable, VertexInfo> vertexInfo;
    
    /**
     * Tracks the current visit time.  This variable is only useful
     * during the execution of the search.
     */
    private int visitTime;
    
    /**
     * Keeps track of the order the DFS finished with each of the vertices
     * in the graph.  The last vertex finished is at the head of the list.
     * This list constitutes a topological sort of the graph.
     * <p>
     * This is declared as a LinkedList so we can use the special addFirst()
     * method of LinkedList.
     */
    private LinkedList<SQLTable> finishOrder;

    /**
     * The VertexInfo class contains visit information related to the DFS
     * algorithm's discovery of a vertex in the graph.  It is capable of
     * classifying a vertex as "white," "grey," and "black" depending on
     * when it was started and finished by the DFS.
     */
    private class VertexInfo {
        
        /**
         * A serial number assigned to this vertex when it is first discovered
         * by the DFS.
         */
        private int discoveryTime;
        
        /**
         * A serial number assigned to this vertex when the DFS leaves it.
         */
        private int finishTime;
        
        /**
         * The vertex that the DFS was at when it discovered this vertex.
         * In SQLTable terms, the predecessor is a pkTable which exports its
         * key to this table.
         */
        private SQLTable predecessor;
        
        /**
         * Returns true iff this vertex has not been started (discovered) yet.
         */
        public boolean isWhite() {
            return (discoveryTime == 0 && finishTime == 0);
        }
        
        /**
         * Returns true iff this vertex has been started but not
         * finished.
         */
        public boolean isGrey() {
            return (discoveryTime != 0 && finishTime == 0);
        }

        /**
         * Returns true iff this vertex has been started and finished.
         */
        public boolean isBlack() {
            return finishTime != 0;
        }
        
        /**
         * See {@link #discoveryTime}.
         */
        public int getDiscoveryTime() {
            return discoveryTime;
        }
        /**
         * See {@link #discoveryTime}.
         */
        public void setDiscoveryTime(int discoveryTime) {
            this.discoveryTime = discoveryTime;
        }
        /**
         * See {@link #finishTime}.
         */
        public int getFinishTime() {
            return finishTime;
        }
        /**
         * See {@link #finishTime}.
         */
        public void setFinishTime(int finishTime) {
            this.finishTime = finishTime;
        }
        /**
         * See {@link #predecessor}.
         */
        public SQLTable getPredecessor() {
            return predecessor;
        }
        /**
         * See {@link #predecessor}.
         */
        public void setPredecessor(SQLTable predecessor) {
            this.predecessor = predecessor;
        }
    }
    
    public DepthFirstSearch(SQLDatabase db) throws ArchitectException {
        List<SQLTable> tables = new ArrayList<SQLTable>();
        ArchitectUtils.findDescendentsByClass(db, SQLTable.class, tables);
        performSearch(tables);
    }
    
    public DepthFirstSearch(List<SQLTable> tables) throws ArchitectException {
        performSearch(tables);
    }

    /**
     * Performs a depth-first search on the given list of SQLTable objects,
     * which are taken to be connected by their exported keys.
     * 
     * <p>This is an implementation of the DFS algorithm in section 23.3 of 
     * "Introduction to Algorithms" by Cormen et al (ISBN 0-07-013143-0).
     * 
     * @param tables
     * @throws ArchitectException
     */
    private void performSearch(List<SQLTable> tables) throws ArchitectException {
        if (logger.isDebugEnabled()) {
            logger.debug("Performing Search on: " + tables);
        }
        vertexInfo = new HashMap();
        finishOrder = new LinkedList();
        for (SQLTable u : tables) {
            vertexInfo.put(u, new VertexInfo());
        }
        visitTime = 0;
        for (SQLTable u : tables) {
            VertexInfo vi = vertexInfo.get(u);
            if (vi.isWhite()) visit(u);
        }
    }

    /**
     * The recursive subroutine of performSearch.  Explores the connected
     * subgraph at u, colouring nodes as they are encountered. 
     * 
     * <p>This is an implementation of the DFS-VISIT routine in section
     * 23.3 of "Introduction to Algorithms" by Cormen et al (ISBN 
     * 0-07-013143-0).
     *
     * @param u
     * @throws ArchitectException
     */
    private void visit(SQLTable u) throws ArchitectException {
        VertexInfo vi = vertexInfo.get(u);
        vi.setDiscoveryTime(++visitTime);
        for (SQLRelationship r : u.getExportedKeys()) {
            SQLTable v = r.getFkTable();
            VertexInfo vi2 = vertexInfo.get(v);
            if (vi2 == null) {
                logger.debug("Skipping vertex " + v + " because it is not in the set of tables to search");
            } else {
                if (vi2.isWhite()) {
                    vi2.setPredecessor(u);
                    visit(v);
                }
            }
        }
        vi.setFinishTime(++visitTime);
        finishOrder.addFirst(u);
    }

    
    
    /**
     * Gives back the order in which the vertices of these graphs were finished (coloured black) by the DFS.
     * This list will be a topological sort of the graph.
     * 
     * <p>See {@link #finishOrder}.
     */
    public List<SQLTable> getFinishOrder() {
        return finishOrder;
    }
}
