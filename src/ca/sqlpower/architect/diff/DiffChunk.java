package ca.sqlpower.architect.diff;

public class DiffChunk<T> {
	private DiffType type;
	private T data;
	
	/**
	 * @param data
	 * @param type
	 */
	public DiffChunk(T data, DiffType type) {
		super();
		this.data = data;
		this.type = type;
	}

	public T getData() {
		return data;
	}

	public DiffType getType() {
		return type;

	}
	@Override
	public String toString() {
		
		return super.toString() + "(" +type+")["+data+"]";
	}
}
