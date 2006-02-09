package ca.sqlpower.architect.layout;

import java.util.HashMap;
import java.util.List;

import ca.sqlpower.architect.swingui.TablePane;

public abstract class AbstractLayout implements ArchitectLayoutInterface {

	HashMap<String,Object> properties;
	
	protected AbstractLayout()
	{
		properties = new HashMap<String,Object>();
	}
	
	public void setProperty(String key, Object value) {
		properties.put(key,value);

	}

	public Object getProperty(String key) {
		return properties.get(key);
	}

	
	
}
