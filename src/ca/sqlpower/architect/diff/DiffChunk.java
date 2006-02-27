package ca.sqlpower.architect.diff;

public class DiffChunk<T> {
	DiffType type;
	T data;
	
	/**
	 * @param data
	 * @param type
	 */
	public DiffChunk(T data, DiffType type) {
		super();
		this.data = data;
		this.type = type;
	}
} 
