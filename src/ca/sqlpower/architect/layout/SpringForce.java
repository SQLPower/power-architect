package ca.sqlpower.architect.layout;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TablePane;

public class SpringForce extends AbstractLayout {

	private static Logger logger = Logger.getLogger(SpringForce.class);
	public final static int THRESHOLD=5;
	private ArrayList<TablePane> tablePanes;
	private ArrayList<Relationship> relations;
	private ArrayList<HashMap<TablePane,Boolean>> adjacentNodes;
	private boolean movement;
	private int fallOffDistance;
	private double friction;
	
	public SpringForce() {
		super();	
	}
	
	public void setup(List<TablePane> nodes,List<Relationship> edges) {
		tablePanes = new ArrayList<TablePane>(nodes);

		relations = new ArrayList<Relationship>(edges);
		adjacentNodes = new ArrayList<HashMap<TablePane,Boolean>>(tablePanes.size());
		movement = false;
		if (this.getProperty("fallOffDistance") instanceof Integer)
		{
			fallOffDistance = ((Integer) this.getProperty("fallOffDistance")).intValue();
		}
		else
		{
			fallOffDistance = 1500;
		}
		if (this.getProperty("friction") instanceof Integer)
		{
			friction = ((Double) this.getProperty("friction")).doubleValue();
		}
		else
		{
			friction = 1;
		}
		// create the adjacency list
		for(TablePane table :tablePanes)
		{
			adjacentNodes.add(new HashMap<TablePane,Boolean>());
		}
		// TODO improve from order (E*N)
		for (Relationship rel: relations)
		{
			for(int ii = 0; ii <tablePanes.size(); ii++)
			{
				if (rel.getPkTable() == tablePanes.get(ii))
				{
					adjacentNodes.get(ii).put(rel.getFkTable(),true);
				}
				if (rel.getFkTable() == tablePanes.get(ii))
				{
					adjacentNodes.get(ii).put(rel.getPkTable(),true);
				}
			}
			
		}
		
	}

	public boolean isDone() {
	
		logger.debug(movement);
	
		return movement;
	}

	public void nextFrame() {
	
	
		friction = friction-0.001;
		ArrayList<Point> paneMotion = new ArrayList<Point>();
		movement = true;
		// calculate motion
		for (int ii =0;ii < tablePanes.size();ii++)
		{
			paneMotion.add(ii,calculateForce(tablePanes.get(ii),ii));
			
			
		}
		//apply motion
		for (int ii =0;ii < tablePanes.size();ii++)
		{
			Point newPosition = tablePanes.get(ii).getLocation();
			
			newPosition.translate((int)Math.round(paneMotion.get(ii).x*friction),(int)Math.round(paneMotion.get(ii).y*friction));

			if (newPosition.x < 0)
			{
				newPosition.x = 0;
			}
			if (newPosition.x > 500)
			{
				newPosition.x = 500;
			}
			if (newPosition.y < 0)
			{
				newPosition.y = 0;
			}
			if (newPosition.y > 500)
			{
				newPosition.y = 500;
			}
			if (newPosition.distance(tablePanes.get(ii).getLocation()) >THRESHOLD)
			{	
				movement=false;
			}
			
			tablePanes.get(ii).setMovePathPoint(newPosition);
			
		}

	}
	
	private double getAngle(Point point1, Point point2)
	{
		double angle;
		if (0!= point2.x-point1.x)
		{
			
			 angle = Math.atan((point2.y -point1.y)/(point2.x-point1.x));
			 if (point2.y>point1.y)
			 {
				 angle +=Math.PI;
			 }
			 if (point2.y == point1.y)
			 {
				 if (point2.x < point1.x)
				 {
					 angle+= Math.PI;
				 }
			 }
			return angle;
		}
		else
		{
			if (point2.y>point1.y)
			{
				return 3/2*Math.PI;
			}
			else if (point2.y< point1.y)
			{
				return Math.PI/2;
			}
			else
			{
				// if on the origin move both off!!!
				if ( point1.x == point2.x)
				{
					return Math.PI/4;
				}
				return Math.random()*2*Math.PI;
			}
				
		}
			
	}
	
	private double tableOffSet(TablePane table1, TablePane table2)
	{
		double length;
		Point centre1 = new Point (table1.getX()+(table1.getWidth()/2),
								table1.getY()+(table1.getHeight()/2));
		
		Point centre2 = new Point (table2.getX()+(table2.getWidth()/2),
					table2.getY()+(table2.getHeight()/2));
		
		length = Math.sqrt((centre1.x-table1.getX())^2 +(centre1.y - table1.getY())^2);
		length += Math.sqrt((centre2.x-table2.getX())^2 +(centre2.y - table2.getY())^2);
		
		length =length+50;
		return length*2;
	}
	
	private Point calculateForce(TablePane tp,int adjacencyListIndex)
	{
		Point forceVector = new Point();
		for(TablePane otherTP:tablePanes)
		{
			Point individualVector;
			if(tp!=otherTP)
			{
				Point centre1 = new Point (tp.getX()+(tp.getWidth()/2),
						tp.getY()+(tp.getHeight()/2));
				
				Point centre2 = new Point (otherTP.getX()+(otherTP.getWidth()/2),
						otherTP.getY()+(otherTP.getHeight()/2));
				
				if (centre1.equals(centre2))
				{
					
					centre2.translate(10,10);
					tp.getLocation().translate(10,10);
				}
				if(adjacentNodes.get(adjacencyListIndex).containsKey(otherTP))
				{
					double zeroPoint = tableOffSet(tp, otherTP);
					individualVector =calculateAdjacentForce(centre1,centre2,zeroPoint);
				}
				else
				{
					
					individualVector =calculateNonAdjacentForce(centre1,centre2,tableOffSet(tp, otherTP));
				}
				forceVector.translate(individualVector.x,individualVector.y);
			}
			
		}
		Point originPull = calculateOriginPullFoce();
		forceVector.translate(originPull.x, originPull.y);
		logger.debug("TablePane "+tp+" moves "+forceVector + " from " + tp.getLocation());
		return forceVector;
	}
	
	/**
	 * 
	 * @param point1
	 * @param point2
	 * @param zeroPoint the point where the forces are 0
	 * @return
	 */
	
	private Point calculateOriginPullFoce()
	{
		
		return new Point();//(-THRESHOLD*8,-THRESHOLD*8);
	}
	
	private Point calculateAdjacentForce(Point point1,Point point2,double zeroPoint)
	{
		int multiplier =1;
		if (this.properties.get("forceMultiplier") instanceof Integer)
		{
			multiplier =((Integer) this.properties.get("forceMultiplier")).intValue();
		}
		double distance = multiplier *zeroPoint* Math.log(point1.distance(point2));
		double angle = getAngle (point1,point2);
		Point returnValue;
		
		returnValue = new Point((int)Math.round(distance*Math.cos(angle)),(int)Math.round(distance*Math.sin(angle)));

		return returnValue;
	}
	private Point calculateNonAdjacentForce(Point point1,Point point2, double forcePoint)
	{
	
		int multiplier =1;
		if (this.properties.get("forceMultiplier") instanceof Integer)
		{
			multiplier =((Integer) this.properties.get("forceMultiplier")).intValue();
		}
			
		if (point1.distance(point2) < fallOffDistance)
		{
		
			// At the distance of 2 the desired length of a relation balance the origin force
			double distance = multiplier*THRESHOLD*10*forcePoint/point1.distance(point2);
			double angle = getAngle (point1,point2);
			Point returnValue;
			
			int projectX =(int) Math.round(distance*Math.cos(angle));
			int projectY =(int) Math.round(distance*Math.sin(angle));
			returnValue = new Point(projectX,projectY);

			// sanity clause
			if (point1.x > point2.x)
			{
				if (projectX < 0 )
				{
					logger.debug( "Oh no pushing the wrong way ("+projectX+") projectX sabotaged!");
				}
				
			}
			if (point1.x < point2.x)
			{
				if (projectX > 0 )
				{
					logger.debug( "Oh no pushing the wrong way ("+projectX+") projectX sabotaged!");
				}
			}
			
//			 sanity clause
			if (point1.y > point2.y)
			{
				if (projectY < 0 )
				{
					logger.debug( "Oh no pushing the wrong way ("+projectY+") projectY sabotaged!");
				}
				
			}
			if (point1.y < point2.y)
			{
				if (projectY > 0 )
				{
					logger.debug( "Oh no pushing the wrong way ("+projectY+") projectY sabotaged! ");
					
				}
			}
			
			return returnValue;
		}
		else
		{
			return new Point();
		}
		
	}

	public void done() {
		movement = false;
	}

	public void setPlayPen(PlayPen pp) {
		// TODO Auto-generated method stub
		
	}			
	
}
