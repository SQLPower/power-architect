package ca.sqlpower.architect;

public class LockedColumnException extends ArchitectException {

	private SQLRelationship lockingRelationship;
    private SQLColumn col;

	public LockedColumnException(SQLRelationship lockingRelationship, SQLColumn col) {
		super("Locked column belongs to relationship "+lockingRelationship);
		this.lockingRelationship = lockingRelationship;
        this.col = col;
	}

	public SQLRelationship getLockingRelationship() {
		return lockingRelationship;
	}

    public SQLColumn getCol() {
        return col;
    }

    public void setCol(SQLColumn col) {
        this.col = col;
    }
}
