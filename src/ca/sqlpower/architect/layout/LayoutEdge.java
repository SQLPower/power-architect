package ca.sqlpower.architect.layout;

public interface LayoutEdge {

    /**
     * Returns the node whose outbound edge list includes this edge.
     */
    LayoutNode getTailNode();

    /**
     * Returns the node whose inbound edge list includes this edge.
     */
    LayoutNode getHeadNode();

}
