package ca.sqlpower.architect.swingui;

public class UnknownDatabaseTypeException extends ca.sqlpower.architect.ArchitectException {
	public UnknownDatabaseTypeException(String type) {
		super(type);
	}
}
