package ca.sqlpower.architect;

public class LockedColumnException extends ArchitectException {
	
	private SQLRelationship lockingRelationship;
	
	public LockedColumnException(SQLRelationship lockingRelationship) {
		super("Locked column belongs to relationship "+lockingRelationship);
		this.lockingRelationship = lockingRelationship;
	}

	public SQLRelationship getLockingRelationship() {
		return lockingRelationship;
	}
}
