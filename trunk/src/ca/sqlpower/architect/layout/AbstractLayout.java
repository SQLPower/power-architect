package ca.sqlpower.architect.layout;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;

import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TablePane;

public abstract class AbstractLayout implements ArchitectLayoutInterface {

	protected Rectangle frame;
	
	public void setup(List<TablePane> nodes, List<Relationship> edges, Rectangle rect) {
		frame = rect;
		
	}
	
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
	
	public Dimension getNewArea(List<TablePane> nodes) {
		Dimension d = new Dimension();
		int radius =0;
		
		for (TablePane tp : nodes) {
			Rectangle b = tp.getBounds();
			radius= Math.max(b.height,radius);
			radius= Math.max(b.width,radius);
		}
		
		d.setSize(radius*2*Math.sqrt(nodes.size()),radius*2*Math.sqrt(nodes.size()));
		return d;
	}

	
	
	
}
