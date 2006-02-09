package ca.sqlpower.architect.layout;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TablePane;

public class FruchtermanReingoldForceLayout extends AbstractLayout {
	private static final Logger logger = Logger
			.getLogger(FruchtermanReingoldForceLayout.class);

	private ArrayList<TablePane> nodes;

	private ArrayList<Relationship> edges;

	/**
	 * The radius that is kept clear
	 */
	private double k;

	/**
	 * The maximum movement 
	 */
	private double temp;

	/**
	 * PlayPen Width
	 */
	private int w;

	/**
	 * PlayPen Height
	 */
	private int h;

	private PlayPen pp;
	
	/**
	 * Spacing multiplier
	 */
	private double c = 2;
	
	/**
	 *  The number of frames in a row that are considered stopped
	 */
	private int stoppedFrames;

	/**
	 * The amount of movement at the begining of the frame
	 * or away from the edge of the playpen
	 */
	private int  baseLineJitter;
	
	/**
	 * Used to be able to stop the algoithm prematurely
	 */
	private boolean overrideDone; 

	public void setup(List<TablePane> nodes, List<Relationship> edges) {
	
		this.nodes = new ArrayList<TablePane>(nodes);
		this.edges = new ArrayList<Relationship>(edges);

		
		baseLineJitter =10;
		temp = 100*nodes.size();
		
		stoppedFrames =0;
		overrideDone = false;

	
		this.getNewArea(pp);
		k = getEmptyRadius(pp);
	}

	/**
	 * get the magnitude of a point
	 * @param p
	 * @return the magnitude
	 */
	public double magnitude(Point p) {
		return p.distance(0, 0);
	}

	/**
	 * Checks and sees if the program stops
	 */
	public boolean isDone() {
		if (stoppedFrames > 5 || nodes.size() < 2 || overrideDone) {
			return true;
		}
		return false;
	}

	/**
	 * Performs the next step of the spring layout
	 */
	public void nextFrame() {
		
		HashMap<TablePane, Point> displacement;
		displacement = new HashMap<TablePane, Point>();
		
		for (TablePane tp : this.nodes) {
			displacement.put(tp, new Point((int)Math.round(Math.random()*baseLineJitter-baseLineJitter/2),+(int)Math.round(Math.random()*baseLineJitter-baseLineJitter/2)));
		}
		// Calculate repulsive forces
		if (nodes != null && !isDone()) {
			for (int ii = 0; ii < nodes.size(); ii++) {
				TablePane v = nodes.get(ii);
				Point disp = displacement.get(v);
				for (int jj = 0; jj < nodes.size(); jj++) {
					TablePane u = nodes.get(jj);
					if (u != v) {
						while (v.getLocation().equals(u.getLocation()))
						{
							v.setMovePathPoint((int)Math.round(Math.random()*5-3),(int)Math.round(Math.random()*5-3));
						}
						
						Point delta = new Point(v.getLocation().x
								- u.getLocation().x, v.getLocation().y
								- u.getLocation().y);

						
						if (delta.distance(0,0) < 5*k)
						{
							disp.translate((int) Math.round(delta.x
								/ magnitude(delta)
								* repulsiveForce(magnitude(delta), k)),
								(int) Math.round(delta.y / magnitude(delta)
										* repulsiveForce(magnitude(delta), k)));
						}
					

					}

				}

			}
			// calculate Attractive force

			for (Relationship e : edges) {
				TablePane v = e.getPkTable();
				TablePane u = e.getFkTable();
				Point delta = new Point(v.getLocation().x - u.getLocation().x,
						v.getLocation().y - u.getLocation().y);
				Point vDisp = displacement.get(v);
				Point uDisp = displacement.get(u);
				if(uDisp.equals(vDisp))
				{
					
					vDisp.translate((int)Math.random()+1,(int)Math.random()+1 );
				}
				
				vDisp.translate(-(int) Math.round(delta.x / magnitude(delta)
						* attractiveForce(delta, k)), -(int) Math
						.round(delta.y / magnitude(delta)
								* attractiveForce(delta, k)));

				uDisp.translate((int) Math.round(delta.x / magnitude(delta)
						* attractiveForce(delta, k)), (int) Math
						.round(delta.y / magnitude(delta)
								* attractiveForce(delta, k)));
				
			}

			Boolean done =true;
			// add the forces to the current location
			for (TablePane v : nodes) {
				Point pos = (Point) v.getLocation().clone();
				Point disp = displacement.get(v);
				pos.translate((int) Math.round(disp.x / magnitude(disp)
						* Math.min(magnitude(disp), getTemp())), (int) Math
						.round(disp.y / magnitude(disp)
								* Math.min(magnitude(disp), getTemp())));
				logger.debug("Unmodified move x:"+pos.x+", y:"+pos.y);
				pos.x = Math.min(getW()-(int)Math.round(Math.random()*baseLineJitter), Math.max(0+(int)Math.round(Math.random()*baseLineJitter), pos.x));
				pos.y = Math.min(getH()-(int)Math.round(Math.random()*baseLineJitter), Math.max(0+(int)Math.round(Math.random()*baseLineJitter), pos.y));
				logger.debug("Moving table "+v+" to position "+pos);
				if (pos.distance(v.getLocation())>2)
				{
					done = false;
				}
				v.setMovePathPoint(pos);
			}
			if (done)
			{
				stoppedFrames++;
			}
		}
		this.cool();
	}

	/**
	 * Calculate the attractive force
	 * @param distance The distance between two nodes
	 * @param emptyRadius  The radius we want to keep clear
	 * @return the force between the two nodes
	 */
	protected double attractiveForce(Point distance, double emptyRadius) {
		double force;
		force = Math.abs( 2*distance.x *distance.x) / emptyRadius;
		force +=Math.abs(2*(distance.y * distance.y)/emptyRadius);

		return force;
	}

	/**
	 * Calculate the Repulsive force
	 * @param distance The distance between two nodes
	 * @param emptyRadius  The radius we want to keep clear
	 * @return the force between the two nodes
	 */
	protected double repulsiveForce(double distance, double emptyRadius) {
		double force;
		force = ( emptyRadius * emptyRadius ) / distance;

		return force;
	}

	/**
	 * The clear radius between two nodes
	 * @param pp a playen
	 * @return
	 */
	public double getEmptyRadius(PlayPen pp) {

		if (pp != null) {
			List<TablePane> tps;
			int radius =0;

			tps = pp.getTablePanes();
			for (TablePane tp : tps) {
				Rectangle b = tp.getBounds();
				radius=Math.max(b.height,radius);
				radius=Math.max(b.width,radius);
			}

			return radius*c;
		}
		return 1;
	}

	private Dimension getNewArea(PlayPen pp) {
		Dimension d = new Dimension();
		int radius =0;
		
		List<TablePane> tps;
		tps = pp.getTablePanes();
		for (TablePane tp : tps) {
			Rectangle b = tp.getBounds();
			radius= Math.max(b.height,radius);
			radius= Math.max(b.width,radius);
		}
		
		d.setSize(c*radius*Math.sqrt(tps.size()),c*radius*Math.sqrt(tps.size()));
		w = d.width;
		h = d.height;
		return d;
	}

	public void done() {
		overrideDone = true;
	}

	/**
	 * Cool the temperature
	 *
	 */
	public void cool() {	
		temp = temp/1.1;
	}

	
	public ArrayList<Relationship> getEdges() {
		return edges;
	}

	public double getK() {
		return k;
	}

	public ArrayList<TablePane> getNodes() {
		return nodes;
	}

	public double getTemp() {
		return temp;
	}

	public int getH() {
		return h;
	}

	public int getW() {
		return w;
	}

	public void setPlayPen(PlayPen pp) {
		this.pp = pp;

	}

}
