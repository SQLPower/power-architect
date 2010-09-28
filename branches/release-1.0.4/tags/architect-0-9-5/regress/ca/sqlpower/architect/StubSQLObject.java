package ca.sqlpower.architect;

/**
 * The StubSQLObject is a general-purpose SQLObject that you can use for testing
 * the Architect.  You might need to subclass it, or you might need to enhance
 * it directly.  Which is better is a judgement call!
 */
public class StubSQLObject extends SQLObject {

    /**
     * Keeps track of how many times populate() has been called.
     */
    private int populateCount = 0;
    
    @Override
    public SQLObject getParent() {
        return null;
    }

    @Override
    protected void setParent(SQLObject parent) {
        // no op
    }

    @Override
    protected void populate() throws ArchitectException {
        populateCount++;
    }

    @Override
    public String getShortDisplayName() {
        return null;
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    @Override
    public Class<? extends SQLObject> getChildType() {
        return null;
    }

    // ======= non-SQLObject methods below this line ==========
    
    public int getPopulateCount() {
        return populateCount;
    }
}