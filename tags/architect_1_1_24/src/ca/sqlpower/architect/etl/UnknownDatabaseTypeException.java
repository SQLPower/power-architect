package ca.sqlpower.architect.etl;

public class UnknownDatabaseTypeException extends ca.sqlpower.architect.ArchitectException {
	public UnknownDatabaseTypeException(String type) {
		super(type);
	}
}
