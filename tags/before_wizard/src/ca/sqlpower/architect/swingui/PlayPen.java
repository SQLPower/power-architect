package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashSet;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.*;
import ca.sqlpower.architect.ArchitectDataSource;


public class PlayPen extends JPanel
	implements java.io.Serializable, SQLObjectListener, SelectionListener, ContainerListener, Scrollable {

	private static Logger logger = Logger.getLogger(PlayPen.class);

	/**
	 * The ActionMap key for the action that deletes the selected
	 * object in this TablePane.
	 */
	public static final String KEY_DELETE_SELECTED
		= "ca.sqlpower.architect.swingui.PlayPen.KEY_DELETE_SELECTED";

	/**
	 * Links this PlayPen with an instance of PlayPenDropListener so
	 * users can drop stuff on the playpen.
	 */
	protected DropTarget dt;

	/**
	 * This database is the container of all the SQLObjects in this
	 * playpen.  Items added via the importTableCopy, addSchema, ... methods
	 * will be added into this database.
	 */
	protected SQLDatabase db;
	
	/**
	 * Maps table names (Strings) to Integers.  Useful for making up
	 * new table names if two tables of the same name are added to
	 * this playpen.
	 */
	protected HashSet tableNames;
	
	/**
	 * This is the shared popup menu that applies to right-clicks on
	 * any TablePane in the PlayPen.
	 */
	protected JPopupMenu tablePanePopup;

	protected JPopupMenu playPenPopup;

	/**
	 * This object receives all mouse and mouse motion events in the
	 * PlayPen.  It tries to dispatch them to the ppcomponents, and
	 * also handles playpen-specific behaviour like rubber band
	 * selection and popup menu triggering.
	 */
	protected PPMouseListener ppMouseListener;

	/**
	 * The RubberBand allows the user to select multiple ppcomponents
	 * by click-and-drag across a region.
	 */
	protected Rectangle rubberBand;

	/**
	 * This is the colour that the rubber band will be painted with.
	 */
	protected Color rubberBandColor = Color.black;

	/**
	 * The visual magnification factor for this playpen.
	 */
	protected double zoom;

	/**
	 * Contains the child components of this playpen.
	 */
	protected PlayPenContentPane contentPane;

	/**
	 * This action brings the selected TablePane or Relationship to
	 * the front/top of the component stack.
	 */
	protected Action bringToFrontAction;

	/**
	 * This action sends the selected TablePane or Relationship to
	 * the back/bottom of the component stack.
	 */
	protected Action sendToBackAction;

	/**
	 * This dialog box is for editting the PlayPen's DB Connection spec.
	 */
	protected JDialog dbcsDialog;

	/**
	 * A RenderingHints value of VALUE_ANTIALIAS_ON, VALUE_ANTIALIAS_OFF, or VALUE_ANTIALIAS_DEFAULT.
	 */
    private Object antialiasSetting = RenderingHints.VALUE_ANTIALIAS_DEFAULT;
        
	public PlayPen() {
		zoom = 1.0;
		setBackground(java.awt.Color.white);
		contentPane = new PlayPenContentPane(this);
		contentPane.addContainerListener(this);
		contentPane.setFont(getFont());
		contentPane.setForeground(getForeground());
		contentPane.setBackground(getBackground());
		setLayout(null);
		setName("Play Pen");
		setMinimumSize(new Dimension(1,1));
		dt = new DropTarget(this, new PlayPenDropListener());
		bringToFrontAction = new BringToFrontAction(this);
		sendToBackAction = new SendToBackAction(this);
		setupTablePanePopup();
		setupPlayPenPopup();
		setupKeyboardActions();
		ppMouseListener = new PPMouseListener();
		addMouseListener(ppMouseListener);
		addMouseMotionListener(ppMouseListener);
	}

	public PlayPen(SQLDatabase db) {
		this();
		setDatabase(db);
	}

	public SQLDatabase getDatabase() {
		return db;
	}

	public void setDatabase(SQLDatabase newdb) {
		if (newdb == null) throw new NullPointerException("db must be non-null");
		this.db = newdb;
		db.setIgnoreReset(true);
		if (db.getDataSource() == null) {
			ArchitectDataSource dbcs = new ArchitectDataSource();
			dbcs.setName("Target Database");
			dbcs.setDisplayName("Target Database");
			db.setDataSource(dbcs);
		}
		try {
			ArchitectUtils.listenToHierarchy(this, db);
		} catch (ArchitectException ex) {
			logger.error("Couldn't listen to database", ex);
		}
		tableNames = new HashSet();
	}

	/**
	 * This routine is called by the PlayPen constructor after it has
	 * set up all the Action instances.  It adds all the necessary
	 * items and action listeners to the TablePane popup menu.
     * 
     * Note: if an action is shared with the DBTree, make sure you 
     * set the action command in its parent menu item so that the
     * action can figure out what the source of the Action was.
	 */
	void setupTablePanePopup() {
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		tablePanePopup = new JPopupMenu();

		JMenuItem mi;

		mi = new JMenuItem();
		mi.setAction(af.editColumnAction);
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		tablePanePopup.add(mi);

		mi = new JMenuItem();
		mi.setAction(af.insertColumnAction);
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		tablePanePopup.add(mi);

		tablePanePopup.addSeparator();

		mi = new JMenuItem();
		mi.setAction(af.editTableAction);
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		tablePanePopup.add(mi);

		mi = new JMenuItem();
		mi.setAction(bringToFrontAction);
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		tablePanePopup.add(mi);

		mi = new JMenuItem();
		mi.setAction(sendToBackAction);
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		tablePanePopup.add(mi);
		
		tablePanePopup.addSeparator();

		mi = new JMenuItem();
		mi.setAction(af.deleteSelectedAction);
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		tablePanePopup.add(mi);

		if (logger.isDebugEnabled()) {
			tablePanePopup.addSeparator();
			mi = new JMenuItem("Show listeners");
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
			mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						List selection = getSelectedItems();
						if (selection.size() == 1) {
							TablePane tp = (TablePane) selection.get(0);
							JOptionPane.showMessageDialog(tp, new JScrollPane(new JList(new java.util.Vector(tp.getModel().getSQLObjectListeners()))));
						} else {
							JOptionPane.showMessageDialog(PlayPen.this, "You can only show listeners on one item at a time");
						}
					}
				});
			tablePanePopup.add(mi);

			mi = new JMenuItem("Show Selection List");
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
			mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						List selection = getSelectedItems();
						if (selection.size() == 1) {
							TablePane tp = (TablePane) selection.get(0);
							JOptionPane.showMessageDialog(tp, new JScrollPane(new JList(new java.util.Vector(tp.columnSelection))));
						} else {
							JOptionPane.showMessageDialog(PlayPen.this, "You can only show selected columns on one item at a time");
						}
					}
				});
			tablePanePopup.add(mi);
		}
	}
	
	void setupKeyboardActions() {
		ArchitectFrame af = ArchitectFrame.getMainInstance();

		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), KEY_DELETE_SELECTED);
		getActionMap().put(KEY_DELETE_SELECTED, af.deleteSelectedAction);
		if (af.deleteSelectedAction == null) logger.warn("af.deleteSelectedAction is null!");
	}

	// --------------------- Utility methods -----------------------

	/**
	 * Calls setChildPositionImpl(child, p.x, p.y).
	 */ 
	public void setChildPosition(JComponent child, Point p) {
		setChildPositionImpl(child, p.x, p.y);
	}

	/**
	 * Calls setChildPositionImpl(child, x, y).
	 */ 
	public void setChildPosition(JComponent child, int x, int y) {
		setChildPositionImpl(child, x, y);
	}
	
	/**
	 * Scales the given X and Y co-ords from the visible point (x,y)
	 * to the actual internal location, and sets child's position
	 * accordingly.
	 *
	 * @param child a component in this PlayPen's content pane.
	 * @param x the apparent visible X co-ordinate
	 * @param y the apparent visible Y co-ordinate
	 */
	protected void setChildPositionImpl(JComponent child, int x, int y) {
		child.setLocation((int) ((double) x / zoom), (int) ((double) y / zoom));
	}
	
	/**
	 * Modifies the given point p in model space to apparent position
	 * in screen space.
	 *
	 * @param p The point in model space (the space where the actual
	 * components of the content pane live).  THIS PARAMETER IS MODIFIED.
	 * @return The given point p, which has been modified.
	 */ 
	public Point zoomPoint(Point p) {
		p.x = (int) ((double) p.x * zoom);
		p.y = (int) ((double) p.y * zoom);
		return p;
	}

	/**
	 * Modifies the given point p from apparent position in screen
	 * space to model space.
	 *
	 * @param p The point in visible screen space (the space where
	 * mouse events are reported).  THIS PARAMETER IS MODIFIED.
	 * @return The given point p, which has been modified.
	 */ 
	public Point unzoomPoint(Point p) {
		p.x = (int) ((double) p.x / zoom);
		p.y = (int) ((double) p.y / zoom);
		return p;
	}

	/**
	 * Modifies the given rect p in model space to apparent position
	 * in screen space.
	 *
	 * @param r The rectangle in model space (the space where the actual
	 * components of the content pane live).  THIS PARAMETER IS MODIFIED.
	 * @return The given rect p, which has been modified.
	 */ 
	public Rectangle zoomRect(Rectangle r) {
		r.x = (int) ((double) r.x * zoom);
		r.y = (int) ((double) r.y * zoom);
		r.width = (int) ((double) r.width * zoom);
		r.height = (int) ((double) r.height * zoom);
		return r;
	}

	/**
	 * Modifies the given rect r from apparent position in screen
	 * space to model space.
	 *
	 * @param r The rectangle in visible screen space (the space where
	 * mouse events are reported).  THIS PARAMETER IS MODIFIED.
	 * @return The given rect p, which has been modified.
	 */ 
	public Rectangle unzoomRect(Rectangle r) {
		r.x = (int) ((double) r.x / zoom);
		r.y = (int) ((double) r.y / zoom);
		r.width = (int) ((double) r.width / zoom);
		r.height = (int) ((double) r.height / zoom);
		return r;
	}

	// --------------------- accessors and mutators ----------------------

	public void setZoom(double newZoom) {
		if (newZoom != zoom) {
			double oldZoom = zoom;
			zoom = newZoom;
			firePropertyChange("zoom", oldZoom, newZoom);
			revalidate();
			repaint();
		}
	}

	public double getZoom() {
		return zoom;
	}
	
	public void setRenderingAntialiased(boolean v) {
	    if (v) {
	        antialiasSetting = RenderingHints.VALUE_ANTIALIAS_ON;
	    } else {
	        antialiasSetting = RenderingHints.VALUE_ANTIALIAS_OFF;
	    }
	    repaint();
	}
	
	public boolean isRenderingAntialiased() {
	    return antialiasSetting == RenderingHints.VALUE_ANTIALIAS_ON;
	}

	// -------------------------- JComponent overrides ---------------------------

	/**
	 * Calculates the smallest rectangle that will completely
	 * enclose the visible components.  
	 *
	 * This is then compared to the viewport size, one dimension
	 * at a time.  To ensure the whole playpen is "live", always
	 * choose the larger number in each Dimension.
	 * 
	 * There is also a lower bound on how small the playpen can get.  The
	 * layout manager returns a preferred size of (100,100) when asked.
	 */
	public Dimension getPreferredSize() {
		Rectangle cbounds = null;
		//int minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = 0, maxy = 0;
		int minx = 0, miny = 0, maxx = 0, maxy = 0;
		for (int i = 0; i < contentPane.getComponentCount(); i++) {
			Component c = contentPane.getComponent(i);
			if (c.isVisible()) {
				cbounds = c.getBounds(cbounds);
				minx = Math.min(cbounds.x, minx);
				miny = Math.min(cbounds.y, miny);
				maxx = Math.max(cbounds.x + cbounds.width , maxx);
				maxy = Math.max(cbounds.y + cbounds.height, maxy);
			} 
		}
		
		Dimension userDim = new Dimension(maxx-minx,maxy-miny);
		Dimension usedSpace = new Dimension((int) ((double) Math.max(maxx - minx, getMinimumSize().width) * zoom),
				(int) ((double) Math.max(maxy - miny, getMinimumSize().height) * zoom));
		Dimension vpSize = getViewportSize();
		Dimension ppSize = null;
		
		// viewport seems to never come back as null, but protect anyways...
		if (vpSize != null) {
			ppSize = new Dimension(Math.max(usedSpace.width, vpSize.width),
					Math.max(usedSpace.height, vpSize.height));
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("minsize is: " + getMinimumSize());
			logger.debug("unzoomed userDim is: " + userDim);
			logger.debug("zoom="+zoom+",usedSpace size is " + usedSpace);
		}
		
		if (ppSize != null) { 
			logger.debug("preferred size is ppSize (viewport size was null): " + ppSize);
			return ppSize;
		} else {
			logger.debug("preferred size is usedSpace: " + usedSpace);
			return usedSpace;
		}
	}
	
	// get the size of the viewport that we are sitting in (return null if there isn't one);
	public Dimension getViewportSize() {
		Container c = SwingUtilities.getAncestorOfClass(JViewport.class, this);
		if (c != null) {
			JViewport jvp = (JViewport) c;
			logger.debug("viewport size is: " + jvp.getSize());
			return jvp.getSize();
		} else {
			logger.debug("viewport size is NULL");
			return null;
		}
	}
	


	public void paintComponent(Graphics g) {

		logger.debug("start of paintComponent, width="+getWidth()+",height="+getHeight());
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasSetting);

		if (logger.isDebugEnabled()) {
			Rectangle clip = g2.getClipBounds();
			if (clip != null) {
				g2.setColor(Color.green);
				clip.width--;
				clip.height--;
				g2.draw(clip);
				g2.setColor(getBackground());
				logger.debug("Clipping region: "+g2.getClip());
			} else {
				logger.debug("Null clipping region");
			}
		}

		Rectangle bounds = new Rectangle();
		AffineTransform backup = g2.getTransform();
		g2.scale(zoom, zoom);
		AffineTransform zoomedOrigin = g2.getTransform();

		// counting down so visual z-order matches click detection z-order
		for (int i = contentPane.getComponentCount() - 1; i >= 0; i--) {
			Component c = contentPane.getComponent(i);
			c.getBounds(bounds);
			if (c.isVisible() && g2.hitClip(bounds.x, bounds.y, bounds.width, bounds.height)) {
				if (logger.isDebugEnabled()) logger.debug("Painting visible component "+c);
				g2.translate(c.getLocation().x, c.getLocation().y);
				c.paint(g2);
				g2.setTransform(zoomedOrigin);
			} else {
				if (logger.isDebugEnabled()) logger.debug("SKIPPING "+c);
			}
		}

		if (rubberBand != null && !rubberBand.isEmpty()) {
			if (logger.isDebugEnabled()) logger.debug("painting rubber band "+rubberBand);
			g2.setColor(rubberBandColor);
			Composite backupComp = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
			g2.fillRect(rubberBand.x, rubberBand.y, rubberBand.width-1, rubberBand.height-1);
			g2.setComposite(backupComp);
			g2.drawRect(rubberBand.x, rubberBand.y, rubberBand.width-1, rubberBand.height-1);
		}

		g2.setTransform(backup);

		logger.debug("end of paintComponent, width="+getWidth()+",height="+getHeight());

	}

	/**
	 * Adds the given component to this PlayPen's content pane.  Does
	 * NOT add it to the Swing containment hierarchy. The playpen is a
	 * leaf in the hierarchy as far as swing is concerned.
	 *
	 * @param c The component to add.  The PlayPen only accepts
	 * Relationship and TablePane components.
	 * @param constraints The Point at which to add the component
	 * @param index ignored for now, but would normally specify the
	 * index of insertion for c in the child list.
	 */
	protected void addImpl(Component c, Object constraints, int index) {
		if (c instanceof Relationship) {
			contentPane.add(c, 0);
		} else if (c instanceof TablePane) {
			if (constraints instanceof Point) {
				contentPane.add(c, 0);
				c.setLocation((Point) constraints);
			} else {
				throw new IllegalArgumentException("Constraints must be a Point");
			}
		} else {
			throw new IllegalArgumentException("PlayPen can't contain components of type "
											   +c.getClass().getName());
		}
		Dimension size = c.getPreferredSize();
		c.setSize(size);
		logger.debug("Set size to "+size);
		c.setVisible(true);
		logger.debug("Final state looks like "+c);
	}

	/**
	 * Returns 0 because this PlayPen contains no Swing components
	 * directly.
	 *
	 * @deprecated Calling this method from Architect code is almost
	 * certainly a mistake, but it needs to exist for Swing to
	 * function correctly.  You probably want to use
	 * getPPComponentCount instead.
	 */
	public int getComponentCount() {
		return super.getComponentCount();
	}

	/**
	 * Throws IndexOutOfBoundsException becuase the PlayPen contains
	 * no Swing components directly.
	 *
	 * @deprecated Calling this method from Architect code is almost
	 * certainly a mistake, but it needs to exist for Swing to
	 * function correctly.  You probably want to use {@link
	 * #findTablePane}, {@link #getRelationships}, or {@link
	 * #getTablePanes} instead.
	 */
	public Component getComponent(int i) {
		return super.getComponent(i);
	}

	/**
	 * Delegates to the content pane.  XXX: doesn't actually cause tooltips to appear.
	 */
	public String getToolTipText(MouseEvent e) {
		return contentPane.getToolTipText(e);
	}

	// ------------------- Right-click popup menu for playpen -----------------------
	protected void setupPlayPenPopup() {
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		playPenPopup = new JPopupMenu();

 		JMenuItem mi = new JMenuItem();
		mi.setAction(chooseDBCSAction);
		playPenPopup.add(mi);

		mi = new JMenuItem();
		mi.setAction(af.createTableAction);
		playPenPopup.add(mi);

		if (logger.isDebugEnabled()) {
			playPenPopup.addSeparator();
			mi = new JMenuItem("Show Relationships");
			mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						JOptionPane.showMessageDialog(PlayPen.this, new JScrollPane(new JList(new java.util.Vector(getRelationships()))));
					}
				});
			playPenPopup.add(mi);
		}
	}

	public Action chooseDBCSAction = new AbstractAction("Target Database Properties") {
			public void actionPerformed(ActionEvent e) {
				showDbcsDialog();
			}
		};
	
	/**
	 * Pops up a dialog box that lets the user inspect and change the
	 * target db's connection spec.  Create from scratch every time
     * just in case the user changed the Target Database from the DBTree.
	 */
	public void showDbcsDialog() {
		final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
									  "Target Database Connection");
		JPanel cp = new JPanel(new BorderLayout(12,12));
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		final DBCSPanel dbcsPanel = new DBCSPanel();
		dbcsPanel.setDbcs(db.getDataSource());
		cp.add(dbcsPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					dbcsPanel.applyChanges();
					d.setVisible(false);
				}
			});
		buttonPanel.add(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					dbcsPanel.discardChanges();
					d.setVisible(false);
				}
			});
		buttonPanel.add(cancelButton);
		
		cp.add(buttonPanel, BorderLayout.SOUTH);
		
		d.setContentPane(cp);
		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		dbcsDialog = d;
		//
		dbcsDialog.setVisible(true);
	}

	/**
	 * Searches this PlayPen's children for a TablePane whose model is
	 * t.
	 *
	 * @return A reference to the TablePane that has t as a model, or
	 * null if no such TablePane is in the play pen.
	 */
	public TablePane findTablePane(SQLTable t) {
		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
			Component c = contentPane.getComponent(i);
			if (c instanceof TablePane 
				&& ((TablePane) c).getModel() == t) {
				return (TablePane) c;
			}
		}
		return null;
	}

	/**
	 * Returns a TablePane in this PlayPen whose name is <code>name</code>.
	 *
	 * <p>Warning: Unique names are not currently enforced in the
	 * PlayPen's database; results will be unpredictable if there is
	 * more than one table with the name you are searching for.
	 *
	 * <p>Implementation note: This method may benefit from a
	 * Map-based lookup rather than the current linear search
	 * algorithm.
	 *
	 * @return A reference to the TablePane whose model name is
	 * <code>name</code>, or <code>null</code> if no such TablePane is
	 * in the play pen.
	 */
	public TablePane findTablePaneByName(String name) {
		name = name.toLowerCase();
		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
			Component c = contentPane.getComponent(i);
			if (c instanceof TablePane 
				&& ((TablePane) c).getModel().getName().toLowerCase().equals(name)) {
				return (TablePane) c;
			}
		}
		return null;
	}

	/**
	 * Searches this PlayPen's children for a Relationship whose model is
	 * r.
	 *
	 * @return A reference to the Relationsip that has r as a model, or
	 * null if no such Relationship is in the play pen.
	 */
	public Relationship findRelationship(SQLRelationship r) {
		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
			Component c = contentPane.getComponent(i);
			if (c instanceof Relationship
				&& ((Relationship) c).getModel() == r) {
				return (Relationship) c;
			}
		}
		return null;
	}

	/**
	 * Returns a list of the Relationship gui components in this
	 * playpen.
	 */
	public List getRelationships() {
		LinkedList relationships = new LinkedList();
		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
			if (contentPane.getComponent(i) instanceof Relationship) {
				relationships.add(contentPane.getComponent(i));
			}
		}
		return relationships;
	}

	/**
	 * Returns a list of the TablePane components in this playpen.
	 */
	public List getTablePanes() {
		LinkedList tablePanes = new LinkedList();
		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
			if (contentPane.getComponent(i) instanceof TablePane) {
				tablePanes.add(contentPane.getComponent(i));
			}
		}
		return tablePanes;
	}

	/**
	 * Returns the number of components in this PlayPen's
	 * PlayPenContentPane.
	 */
	public int getPPComponentCount() {
		return contentPane.getComponentCount();
	}

	/**
	 * Adds a copy of the given source table to this playpen, using
	 * preferredLocation as the layout constraint.  Tries to avoid
	 * adding two tables with identical names.
	 *
	 * @return A reference to the newly-created TablePane.
	 * @see SQLTable#inherit
	 * @see PlayPenLayout#addLayoutComponent(Component,Object)
	 */
	public synchronized TablePane importTableCopy(SQLTable source, Point preferredLocation) throws ArchitectException {
		SQLTable newTable = SQLTable.getDerivedInstance(source, db); // adds newTable to db
		String key = source.getTableName().toLowerCase();
		
		// ensure tablename is unique
		if (logger.isDebugEnabled()) logger.debug("before add: " + tableNames);
		if (!tableNames.add(key)) {
			boolean done = false;			
			int newSuffix = 0;
			while (!done) {
				newSuffix++;
				done = tableNames.add(key+"_"+newSuffix);
			}
			newTable.setTableName(source.getTableName()+"_"+newSuffix);
		}
		if (logger.isDebugEnabled()) logger.debug("after add: " + tableNames);

		TablePane tp = new TablePane(newTable, this);
		
		logger.info("adding table "+newTable);
		add(tp, preferredLocation);
		tp.revalidate();
		
		// create exported relationships if the importing tables exist in pp
		Iterator sourceKeys = source.getExportedKeys().iterator();
		while (sourceKeys.hasNext()) {
		    Object next = sourceKeys.next();
		    if ( !(next instanceof SQLRelationship) ) continue;  // there could be SQLExceptionNodes here
			SQLRelationship r = (SQLRelationship) next;
			if (logger.isInfoEnabled()) {
				logger.info("Looking for fk table "+r.getFkTable().getName()+" in playpen");
			}
			TablePane fkTablePane = findTablePaneByName(r.getFkTable().getName());
			if (fkTablePane != null) {
				logger.info("FOUND IT!");
				SQLTable fkTable = fkTablePane.getModel();
				SQLRelationship newRel = new SQLRelationship();
				newRel.setName(r.getName());
				newRel.setIdentifying(true);
				newRel.setPkTable(newTable);
				newTable.addExportedKey(newRel);
				newRel.setFkTable(fkTable);
				fkTable.addImportedKey(newRel);
				add(new Relationship(this, newRel));

				Iterator mappings = r.getChildren().iterator();
				while (mappings.hasNext()) {
					SQLRelationship.ColumnMapping m
						= (SQLRelationship.ColumnMapping) mappings.next();
					SQLColumn pkCol = newTable.getColumnByName(m.getPkColumn().getName());
					SQLColumn fkCol = fkTable.getColumnByName(m.getFkColumn().getName());
					if (pkCol == null) {
						// this shouldn't happen
						throw new IllegalStateException("Couldn't fink pkCol "+m.getPkColumn().getName()+" in new table");
					}
					if (fkCol == null) {
						// this might reasonably happen (user deleted the column)
						continue;
					}
					SQLRelationship.ColumnMapping newMapping
						= new SQLRelationship.ColumnMapping();
					newMapping.setPkColumn(pkCol);
					newMapping.setFkColumn(fkCol);
					newRel.addChild(newMapping);
				}
			} else {
				logger.info("NOT FOUND");
			}
		}

		// create imported relationships if the exporting tables exist in pp
		sourceKeys = source.getImportedKeys().iterator();
		while (sourceKeys.hasNext()) {
			SQLRelationship r = (SQLRelationship) sourceKeys.next();
			if (logger.isDebugEnabled()) {
				logger.info("Looking for pk table "+r.getPkTable().getName()+" in playpen");
			}
			TablePane pkTablePane = findTablePaneByName(r.getPkTable().getName());
			if (pkTablePane != null) {
				logger.info("FOUND IT!");
				SQLTable pkTable = pkTablePane.getModel();
				SQLRelationship newRel = new SQLRelationship();
				newRel.setName(r.getName());
				newRel.setIdentifying(true);
				newRel.setPkTable(pkTable);
				pkTable.addExportedKey(newRel);
				newRel.setFkTable(newTable);
				newTable.addImportedKey(newRel);
				add(new Relationship(this, newRel));

				Iterator mappings = r.getChildren().iterator();
				while (mappings.hasNext()) {
					SQLRelationship.ColumnMapping m
						= (SQLRelationship.ColumnMapping) mappings.next();
					SQLColumn pkCol = pkTable.getColumnByName(m.getPkColumn().getName());
					SQLColumn fkCol = newTable.getColumnByName(m.getFkColumn().getName());
					if (fkCol == null) {
						// this shouldn't happen
						throw new IllegalStateException("Couldn't fink fkCol "+m.getPkColumn().getName()+" in new table");
					}
					if (pkCol == null) {
						// this might reasonably happen (user deleted the column)
						continue;
					}
					SQLRelationship.ColumnMapping newMapping
						= new SQLRelationship.ColumnMapping();
					newMapping.setPkColumn(pkCol);
					newMapping.setFkColumn(fkCol);
					newRel.addChild(newMapping);
				}
			} else {
				logger.info("NOT FOUND");
			}
		}

		return tp;
	}

	/**
	 * Calls {@link #importTableCopy} for each table contained in the given schema.
	 */

	/*
	public synchronized void addSchema(SQLSchema source, Point preferredLocation) throws ArchitectException {
		AddSchemaTask t = new AddSchemaTask(source, preferredLocation);
		new Thread(t, "Schema-Adder").start();
	}
	
	private class AddSchemaTask implements Runnable {
		SQLSchema source;
		Point preferredLocation;

		public AddSchemaTask(SQLSchema source, Point preferredLocation) {
			this.source = source;
			this.preferredLocation = new Point(preferredLocation);
		}

		public void run() {
			logger.info("AddSchemaTask starting on thread "+Thread.currentThread().getName());
			ProgressMonitor pm = null;
			try {
				pm = new ProgressMonitor
					(PlayPen.this,
					 "Copying schema "+source.getShortDisplayName(),
					 "...",
					 0,
					 source.getChildCount());
				int i = 0;
				Iterator it = source.getChildren().iterator();
				while (it.hasNext() && !pm.isCanceled()) {
					SQLTable sourceTable = (SQLTable) it.next();
					pm.setNote(ArchitectUtils.truncateString(sourceTable.getTableName()));
					TablePane tp = importTableCopy(sourceTable, preferredLocation);
					preferredLocation.x += tp.getPreferredSize().width + 5;
					pm.setProgress(i++);
				}
			} catch (ArchitectException e) {
				e.printStackTrace();
			} finally {
				if (pm != null) pm.close();
			}
			logger.info("AddSchemaTask done");
		}
	}
	*/
	
	
	/**
	 * Calls {@link #importTableCopy} for each table contained in the given schema.
	 */
	public synchronized void addObjects(List list, Point preferredLocation) throws ArchitectException {
		AddObjectsTask t = new AddObjectsTask(list, preferredLocation);
		ProgressMonitor pm
		 = new ProgressMonitor(null,
		                      "Copying objects from DBTree",
		                      "...",
		                      0,
			                  100);			
		new ProgressWatcher(pm, t);
		new Thread(t, "Objects-Adder").start();
	}
	
	private class AddObjectsTask implements Runnable, Monitorable {
		List paths;
		Point preferredLocation;
		
		boolean finished = false;
		boolean cancelled = false;
		String message = null;
		int progress = 0;
		Integer jobSize = null;
		
		public AddObjectsTask(List paths, Point preferredLocation) {
			this.paths = paths;
			this.preferredLocation = preferredLocation;
			finished = false;
		}
		
		public int getProgress() {
			return progress;
		}
		
		public Integer getJobSize() {
			return jobSize;
		}
		
		public boolean isFinished() {
			return finished;
		}
				
		public String getMessage() {
			return message;
		}

		/**
		 * @param cancelled The cancelled to set.
		 */
		public void setCancelled(boolean cancelled) {
			this.cancelled = cancelled;
		}
		
		public boolean isCancelled() {
			return this.cancelled;			
		}
	
		public void run () {
			logger.info("AddObjectsTask starting on thread "+Thread.currentThread().getName());

			try {
								
				int pmMax = 0;				
				Iterator pathIt = paths.iterator();
				DBTree dbtree = ArchitectFrame.getMainInstance().dbTree; // XXX: this is bad

				// first pass: figure out how much work we need to do...
				while (pathIt.hasNext()) {
					Object someData = dbtree.getNodeForDnDPath((int[]) pathIt.next());
					if (someData instanceof SQLObject) {
						pmMax += ArchitectUtils.countTablesSnapshot((SQLObject)someData);						
					}
				}				
				logger.error("the pmMax is: " + pmMax);
				jobSize = new Integer(pmMax);
				
				int i = 0;
				// reset iterator
				pathIt = paths.iterator();

				while (pathIt.hasNext() && !isCancelled()) {
					Object someData = dbtree.getNodeForDnDPath((int[]) pathIt.next());					
					if (someData instanceof SQLTable) {
						TablePane tp = importTableCopy((SQLTable) someData, preferredLocation);
						message = ArchitectUtils.truncateString(((SQLTable)someData).getTableName());
						preferredLocation.x += tp.getPreferredSize().width + 5;
						progress++;
					} else if (someData instanceof SQLSchema) {
						SQLSchema sourceSchema = (SQLSchema) someData;						
						Iterator it = sourceSchema.getChildren().iterator();
						while (it.hasNext() && !isCancelled()) {
							SQLTable sourceTable = (SQLTable) it.next();
							message = ArchitectUtils.truncateString(sourceTable.getTableName());
							TablePane tp = importTableCopy(sourceTable, preferredLocation);
							preferredLocation.x += tp.getPreferredSize().width + 5;
							progress++;
						}
					} else if (someData instanceof SQLCatalog) {
						SQLCatalog sourceCatalog = (SQLCatalog) someData;
						Iterator cit = sourceCatalog.getChildren().iterator();
						if (sourceCatalog.isSchemaContainer()) {
							while (cit.hasNext() && !isCancelled()) {
								SQLSchema sourceSchema = (SQLSchema) cit.next();						
								Iterator it = sourceSchema.getChildren().iterator();
								while (it.hasNext() && !isCancelled()) {
									SQLTable sourceTable = (SQLTable) it.next();									
									message = ArchitectUtils.truncateString(sourceTable.getTableName());
									TablePane tp = importTableCopy(sourceTable, preferredLocation);
									preferredLocation.x += tp.getPreferredSize().width + 5;
									progress++;
								}
							}
						} else {
							while (cit.hasNext() && !isCancelled()) {
								SQLTable sourceTable = (SQLTable) cit.next();
								message = ArchitectUtils.truncateString(sourceTable.getTableName());
								TablePane tp = importTableCopy(sourceTable, preferredLocation);
								preferredLocation.x += tp.getPreferredSize().width + 5;
								progress++;
							}
						}
					} else if (someData instanceof SQLColumn) {
						SQLColumn column = (SQLColumn) someData;
						JLabel colName = new JLabel(column.getColumnName());
						colName.setSize(colName.getPreferredSize());
						add(colName, preferredLocation);
						logger.debug("Added "+column.getColumnName()+" to playpen (temporary, only for testing)");
						colName.revalidate();
					} else {
						logger.error("Unknown object dropped in PlayPen: "+someData);
					}				
				}				
			} catch (ArchitectException e) {
				e.printStackTrace();
			} finally {
				finished = true;
			}
			logger.info("AddObjectsTask done");
		}
	}

	/**
	 * Adds the given table pane to the playpen, and adds its model to
	 * the database.  The new table will follow the mouse until the
	 * user clicks.
	 */
	public void addFloating(TablePane tp) {
		db.addChild(tp.getModel());
		tp.setVisible(false);
		add(tp, new Point(0,0));
		new FloatingTableListener(this, tp, zoomPoint(new Point(tp.getSize().width/2,0)));
	}

	// -------------------- SQLOBJECT EVENT SUPPORT ---------------------

	/**
	 * Listens for property changes in the model (tables
	 * added).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbChildrenInserted(SQLObjectEvent e) {
		logger.debug("SQLObject children got inserted: "+e);
		boolean fireEvent = false;
		SQLObject o = e.getSQLSource();
		SQLObject[] c = e.getChildren();
		for (int i = 0; i < c.length; i++) {
			try {
				ArchitectUtils.listenToHierarchy(this, c[i]);
			} catch (ArchitectException ex) {
				logger.error("Couldn't listen to added object", ex);
			}
			if (c[i] instanceof SQLTable
				|| c[i] instanceof SQLRelationship) {
				fireEvent = true;
			}
		}
		
		if (fireEvent) {
			firePropertyChange("model.children", null, null);
			revalidate();
		}
	}

	/**
	 * Listens for property changes in the model (columns
	 * removed).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbChildrenRemoved(SQLObjectEvent e) {
		logger.debug("SQLObject children got removed: "+e);
		boolean fireEvent = false;
		SQLObject o = e.getSQLSource();
		SQLObject[] c = e.getChildren();
		for (int i = 0; i < c.length; i++) {
			try {
				ArchitectUtils.unlistenToHierarchy(this, c[i]);
			} catch (ArchitectException ex) {
				logger.error("Couldn't unlisten to removed object", ex);
			}

			if (c[i] instanceof SQLTable) {
				for (int j = 0; j < contentPane.getComponentCount(); j++) {
					if (contentPane.getComponent(j) instanceof TablePane) {
						TablePane tp = (TablePane) contentPane.getComponent(j);
						if (tp.getModel() == c[i]) {
							contentPane.remove(j);
							fireEvent = true;
						}
					}
				}
			} else if (c[i] instanceof SQLRelationship) {
				for (int j = 0; j < contentPane.getComponentCount(); j++) {
					if (contentPane.getComponent(j) instanceof Relationship) {
						Relationship r = (Relationship) contentPane.getComponent(j);
						if (r.getModel() == c[i]) {
						    r.setSelected(false);
							contentPane.remove(j);
							fireEvent = true;
						}
					}
				}
			}
		}

		if (fireEvent) {
			firePropertyChange("model.children", null, null);
			repaint();
		}
	}

	/**
	 * Listens for property changes in the model (table
	 * properties modified).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 */
	public void dbObjectChanged(SQLObjectEvent e) {
		firePropertyChange("model."+e.getPropertyName(), null, null);
		revalidate();
	}

	/**
	 * Listens for property changes in the model (significant
	 * structure change).  If this change affects the appearance of
	 * this widget, we will notify all change listeners (the UI
	 * delegate) with a ChangeEvent.
	 *
	 * <p>NOTE: This is not currently implemented.
	 */
	public void dbStructureChanged(SQLObjectEvent e) {
		throw new UnsupportedOperationException
			("FIXME: we have to make sure we're listening to the right objects now!");
		//firePropertyChange("model.children", null, null);
		//revalidate();
	}

	// --------------- SELECTION METHODS ----------------

	/**
	 * Deselects all selectable items in the PlayPen.
	 */
	public void selectNone() {
 		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
 			if (contentPane.getComponent(i) instanceof Selectable) {
 				Selectable s = (Selectable) contentPane.getComponent(i);
 				s.setSelected(false);
 			}
 		}
	}

	/**
	 * Returns a read-only view of the set of selected children in the PlayPen.
	 */
	public List getSelectedItems() {
		// It would be possible to speed this up by maintaining a
		// cache of which children are selected, but the need would
		// have to be demonstrated first.
		ArrayList selected = new ArrayList();
 		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
 			if (contentPane.getComponent(i) instanceof Selectable) {
				Selectable s = (Selectable) contentPane.getComponent(i);
				if (s.isSelected()) {
					selected.add(s);
				}
			}
		}			
		return Collections.unmodifiableList(selected);
	}

	/**
	 * Returns a read-only view of the set of selected table in the PlayPen.
	 */
	public List getSelectedTables() {
		ArrayList selected = new ArrayList();
 		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
 			if (contentPane.getComponent(i) instanceof TablePane) {
				TablePane tp = (TablePane) contentPane.getComponent(i);
				if (tp.isSelected()) {
					selected.add(tp);
				}
			}
		}			
		return Collections.unmodifiableList(selected);
	}


	/**
	 * Returns a read-only view of the set of selected relationships in the PlayPen.
	 */
	public List getSelectedRelationShips() {
		ArrayList selected = new ArrayList();
 		for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
 			if (contentPane.getComponent(i) instanceof Relationship) {
 				Relationship r = (Relationship) contentPane.getComponent(i);
 				if (r.isSelected()) {
					selected.add(r);
				}
			}
		}			
		return Collections.unmodifiableList(selected);
	}

	
	// --------------------------- CONTAINER LISTENER -------------------------

	/**
	 * Unregisters this PlayPen as a SelectionListener if the
	 * removed component is Selectable.
	 */
	public void componentRemoved(ContainerEvent e) {
		if (e.getChild() instanceof Selectable) {
			((Selectable) e.getChild()).removeSelectionListener(this);
		}
	}

	/**
	 * Registers this PlayPen as a SelectionListener if the added
	 * component is Selectable.
	 */
	public void componentAdded(ContainerEvent e) {
		((JComponent) e.getChild()).revalidate();
		if (e.getChild() instanceof Selectable) {
			((Selectable) e.getChild()).addSelectionListener(this);
		}
	}

	// ---------------------- SELECTION LISTENER ------------------------
	
	/**
	 * Forwards the selection event <code>e</code> to all PlayPen
	 * selection listeners.
	 */
	public void itemSelected(SelectionEvent e) {
		fireSelectionEvent(e);
	}

	/**
	 * Forwards the selection event <code>e</code> to all PlayPen
	 * selection listeners.
	 */
	public void itemDeselected(SelectionEvent e) {
		fireSelectionEvent(e);
	}

	// --------------------- SELECTION EVENT SUPPORT ---------------------

	protected LinkedList selectionListeners = new LinkedList();

	public void addSelectionListener(SelectionListener l) {
		selectionListeners.add(l);
	}

	public void removeSelectionListener(SelectionListener l) {
		selectionListeners.remove(l);
	}
	
	protected void fireSelectionEvent(SelectionEvent e) {
		Iterator it = selectionListeners.iterator();
		if (e.getType() == SelectionEvent.SELECTION_EVENT) {
			while (it.hasNext()) {
				((SelectionListener) it.next()).itemSelected(e);
			}
		} else if (e.getType() == SelectionEvent.DESELECTION_EVENT) {
			while (it.hasNext()) {
				((SelectionListener) it.next()).itemDeselected(e);
			}
		} else {
			throw new IllegalStateException("Unknown selection event type "+e.getType());
		}
	}

	// ------------------------------------- INNER CLASSES ----------------------------

	/**
	 * Tracks incoming objects and adds successfully dropped objects
	 * at the current mouse position.  Also retargets drops to the
	 * TablePanes when necessary.
	 */
	public static class PlayPenDropListener implements DropTargetListener {

		/**
		 * When the user moves over a table pane, its drop target's
		 * dragEnter method will be called, and this variable will
		 * reference it.  When the user moves off of a table pane, its
		 * dragExit method will be called, and this variable will
		 * reference null (or a different table pane).
		 */
		protected TablePane tpTarget;

		/**
		 * Called while a drag operation is ongoing, when the mouse
		 * pointer enters the operable part of the drop site for the
		 * DropTarget registered with this listener.
		 */
		public void dragEnter(DropTargetDragEvent dtde) {
			dragOver(dtde);
		}
		
		/**
		 * Called while a drag operation is ongoing, when the mouse
		 * pointer has exited the operable part of the drop site for the
		 * DropTarget registered with this listener.
		 */
		public void dragExit(DropTargetEvent dte) {
            // nothing needs to be put back
		}
		
		/**
		 * Called when a drag operation is ongoing, while the mouse
		 * pointer is still over the operable part of the drop site for
		 * the DropTarget registered with this listener.
		 */
		public void dragOver(DropTargetDragEvent dtde) {
			PlayPen pp = (PlayPen) dtde.getDropTargetContext().getComponent();
			Point sp = pp.unzoomPoint(new Point(dtde.getLocation()));
			Component ppc = pp.contentPane.getComponentAt(sp);
			TablePane tp = ppc != null && ppc instanceof TablePane ? (TablePane) ppc : null;
			if (tp != tpTarget) {
				if (tpTarget != null) {
					tpTarget.getDropTarget().dragExit(dtde);
				}
				tpTarget = tp;
				if (tpTarget != null) {
					tpTarget.getDropTarget().dragEnter(dtde);
				}
			}
			if (tpTarget != null) {
				tpTarget.getDropTarget().dragOver(dtde);
			} else {
				dtde.acceptDrag(DnDConstants.ACTION_COPY);
			}
		}
		
		/**
		 * Processes the drop action on the PlayPen (the DropTarget)
		 * or current target TablePane if there is one.
		 */
		public void drop(DropTargetDropEvent dtde) {
			if (tpTarget != null) {
				tpTarget.getDropTarget().drop(dtde);
				return;
			}

			Transferable t = dtde.getTransferable();
			PlayPen c = (PlayPen) dtde.getDropTargetContext().getComponent();
			DataFlavor importFlavor = bestImportFlavor(c, t.getTransferDataFlavors());
			if (importFlavor == null) {
				dtde.rejectDrop();
			} else {
				try {
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					ArrayList paths = (ArrayList) t.getTransferData(importFlavor);
					Point dropLoc = c.unzoomPoint(new Point(dtde.getLocation()));									
					c.addObjects(paths, dropLoc);					
					dtde.dropComplete(true);
				} catch (UnsupportedFlavorException ufe) {
					logger.error(ufe);
					dtde.rejectDrop();
				} catch (IOException ioe) {
					logger.error(ioe);
					dtde.rejectDrop();
				} catch (InvalidDnDOperationException ex) {
					logger.error(ex);
					dtde.rejectDrop();
				} catch (ArchitectException ex) {
					logger.error(ex);
					dtde.rejectDrop();
				}
			}
		}
		
		
		
		/**
		 * Called if the user has modified the current drop gesture.
		 */
		public void dropActionChanged(DropTargetDragEvent dtde) {
            // we don't care
		}

		/**
		 * Chooses the best import flavour from the flavors array for
		 * importing into c.  The current implementation actually just
		 * chooses the first acceptable flavour.
		 *
		 * @return The first acceptable DataFlavor in the flavors
		 * list, or null if no acceptable flavours are present.
		 */
		public DataFlavor bestImportFlavor(JComponent c, DataFlavor[] flavors) {
			DataFlavor best = null;
			logger.debug("PlayPenTransferHandler: can I import "+Arrays.asList(flavors));
 			for (int i = 0; i < flavors.length; i++) {
				String cls = flavors[i].getDefaultRepresentationClassAsString();
				logger.debug("representation class = "+cls);
				logger.debug("mime type = "+flavors[i].getMimeType());
				logger.debug("type = "+flavors[i].getPrimaryType());
				logger.debug("subtype = "+flavors[i].getSubType());
				logger.debug("class = "+flavors[i].getParameter("class"));
				logger.debug("isSerializedObject = "+flavors[i].isFlavorSerializedObjectType());
				logger.debug("isInputStream = "+flavors[i].isRepresentationClassInputStream());
				logger.debug("isRemoteObject = "+flavors[i].isFlavorRemoteObjectType());
				logger.debug("isLocalObject = "+flavors[i].getMimeType().equals(DataFlavor.javaJVMLocalObjectMimeType));


 				if (flavors[i].equals(DnDTreePathTransferable.TREEPATH_ARRAYLIST_FLAVOR)) {
					logger.debug("YES");
					best = flavors[i];
				} else {
					logger.debug("NO!");
				}
 			}
 			return best;
		}

		/**
		 * This is set up this way because this DropTargetListener was
		 * derived from a TransferHandler.  It works, so no sense in
		 * changing it.
		 */
		public boolean canImport(JComponent c, DataFlavor[] flavors) {
			return bestImportFlavor(c, flavors) != null;
		} 
	}

	/**
	 * The PPMouseListener class receives all mouse and mouse motion
	 * events in the PlayPen.  It tries to dispatch them to the
	 * ppcomponents, and also handles playpen-specific behaviour like
	 * rubber band selection and popup menu triggering.
	 */
	protected class PPMouseListener implements MouseListener, MouseMotionListener {

		/**
		 * This state is required by the mouseMoved method for
		 * resizing the rubber band in response to user input.
		 */
		protected Point rubberBandOrigin;

		// ------------------- MOUSE LISTENER INTERFACE ------------------
		
		public void mouseEntered(MouseEvent evt) {
			retargetToContentPane(evt);
		}
		
		public void mouseExited(MouseEvent evt) {
			retargetToContentPane(evt);
		}
		
		public void mouseClicked(MouseEvent evt) {
			if (!retargetToContentPane(evt)) {
				maybeShowPopup(evt);
			}
		}
		
		public void mousePressed(MouseEvent evt) {
			requestFocus();

			// FIXME: DND is currently busted.  Need to do something in here to figure out 
            // if we're clicking on something that was already selected!  



			if (!retargetToContentPane(evt)) {
				maybeShowPopup(evt);
				if ((evt.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
					selectNone();
					rubberBandOrigin = unzoomPoint(new Point(evt.getPoint()));
					rubberBand = new Rectangle(rubberBandOrigin.x, rubberBandOrigin.y, 0, 0);
				}
			}
		}
		
		public void mouseReleased(MouseEvent evt) {
			if (rubberBand != null) {
				if (evt.getButton() == MouseEvent.BUTTON1) {
					Rectangle dirtyRegion = new Rectangle(rubberBand);
					
					// grow dirty region by 10% because when it's an exact match, poop gets left behind
					dirtyRegion.width += (int) (dirtyRegion.width * 0.1);
					dirtyRegion.height += (int) (dirtyRegion.height * 0.1);

					rubberBandOrigin = null;
					rubberBand = null;
					repaint(zoomRect(dirtyRegion));
				}
			} else if (!retargetToContentPane(evt)) {
				//((PlayPen) evt.getSource()).selectNone();
				maybeShowPopup(evt);
			}
		}
		
		// ---------------- MOUSEMOTION LISTENER INTERFACE -----------------
		public void mouseDragged(MouseEvent evt) {
			mouseMoved(evt);
		}
		
		public void mouseMoved(MouseEvent evt) {
			if (rubberBand != null) {
				// repaint old region in case of shrinkage
				Rectangle dirtyRegion = zoomRect(new Rectangle(rubberBand));

				Point p = unzoomPoint(new Point(evt.getPoint()));
				rubberBand.setBounds(rubberBandOrigin.x, rubberBandOrigin.y, 0, 0);
				rubberBand.add(p);

				// update selected items
				Rectangle temp = new Rectangle();  // avoids multiple allocations in getBounds
				for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
					Component c = contentPane.getComponent(i);
					if (c instanceof Relationship) {
					    // relationship is non-rectangular so we can't use getBounds for intersection testing
					    ((Relationship) c).setSelected(((Relationship) c).intersects(rubberBand));
					} else if (c instanceof Selectable) {
						((Selectable) c).setSelected(rubberBand.intersects(c.getBounds(temp)));
					}
				}

				// Add the new rubberband to the dirty region and grow
				// it in case the line is thick due to extreme zoom
				dirtyRegion.add(zoomRect(new Rectangle(rubberBand)));
				dirtyRegion.x -= 3;
				dirtyRegion.y -= 3;
				dirtyRegion.width += 6;
				dirtyRegion.height += 6;
				repaint(dirtyRegion);
			} else {
				retargetToContentPane(evt);
			}
		}
		
		/**
		 * Handles retargetting of mouse events.
		 */
		public boolean retargetToContentPane(MouseEvent evt) {
			PlayPen pp = (PlayPen) evt.getSource();
			return pp.contentPane.delegateEvent(evt);
		}
		
		/**
		 * Shows the playpen popup menu if appropriate.
		 */
		public boolean maybeShowPopup(MouseEvent evt) {
			PlayPen pp = (PlayPen) evt.getSource();
			if (evt.isPopupTrigger()) {
				//pp.selectNone();
				pp.playPenPopup.show(pp, evt.getX(), evt.getY());
				return true;
			} else {
				return false;
			}
		}
	}

	// ---------- Floating Table Listener ------------
	
	/**
	 * Listens to mouse motion and moves the given component so it
	 * follows the mouse.  When the user lifts the mouse button, it
	 * stops moving the component, and unregisters itself as a
	 * listener.
	 */
	public static class FloatingTableListener extends MouseInputAdapter {
		PlayPen pp;
		TablePane tp;
		Point handle;

		public FloatingTableListener(PlayPen pp, TablePane tp, Point handle) {
			this.pp = pp;
			this.tp = tp;
			this.handle = handle;
			pp.addMouseMotionListener(this);
			pp.addMouseListener(this); // the click that ends this operation
			pp.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}

		public void mouseMoved(MouseEvent e) {
			mouseDragged(e);
		}

		public void mouseDragged(MouseEvent e) {
			tp.setVisible(true);
			Point p = new Point(e.getPoint().x - handle.x, e.getPoint().y - handle.y);
			pp.setChildPosition(tp, p);
			pp.repaint(); // FIXME: should use a contentPane approach. it would automatically want to redraw
		}

		/**
		 * Anchors the tablepane and disposes this listener instance.
		 */
		public void mouseReleased(MouseEvent e) {
			cleanup();
		}

		protected void cleanup() {
			pp.setCursor(null);
			pp.removeMouseMotionListener(this);
			pp.removeMouseListener(this);
			pp.revalidate();
		}
	}

	// -------------- Bring to Front / Send To Back ------------------
	public static class BringToFrontAction extends AbstractAction {

		protected PlayPen pp;

		public BringToFrontAction(PlayPen pp) {
			super("Bring to Front");
			this.pp = pp;
		}

		public void actionPerformed(ActionEvent e) {
			List items = pp.getSelectedItems();
			Iterator it = items.iterator();
			while (it.hasNext()) {
				PlayPenComponent c = (PlayPenComponent) it.next();
				pp.contentPane.remove(c);
				pp.contentPane.add(c, 0);
			}
			pp.repaint();
		}
	}

	public static class SendToBackAction extends AbstractAction {

		protected PlayPen pp;

		public SendToBackAction(PlayPen pp) {
			super("Send to Back");
			this.pp = pp;
		}

		public void actionPerformed(ActionEvent e) {
			List items = pp.getSelectedItems();
			Iterator it = items.iterator();
			while (it.hasNext()) {
				PlayPenComponent c = (PlayPenComponent) it.next();
				pp.contentPane.remove(c);
				pp.contentPane.add(c, pp.contentPane.getComponentCount());
			}
			pp.repaint();
		}
	}

	// --- Scrollable Methods --- //
 	public Dimension getPreferredScrollableViewportSize() {
		// return getPreferredSize();
		return new Dimension(800,600);
	}

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL) {
			return visibleRect.width;
		} else { // SwingConstants.VERTICAL
			return visibleRect.height;
		}
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

    public boolean getScrollableTracksViewportWidth() {
		return false;
	}
	
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL) {
			return visibleRect.width/5;
		} else { // SwingConstants.VERTICAL
			return visibleRect.height/5;
		}
	}

    /**
     * @return The font render context at the current zoom setting.
     */
    public FontRenderContext getFontRenderContext() {
        Graphics2D g2 = (Graphics2D) getGraphics();
        FontRenderContext frc = null;
        if (g2 != null) {
            g2.scale(zoom, zoom);
            frc = g2.getFontRenderContext();
            g2.dispose();
        }
        if (logger.isDebugEnabled()) logger.debug("Returning frc="+frc);
        return frc;
    }

    /**
     * Selects the playpen component that represents the given SQLObject,
     * and scrolls the viewport to ensure the component is visible on-screen.
     * If the given SQL Object isn't in the playpen, this method has no effect.
     * 
     * @param selection Either a SQLTable or SQLRelationship object. 
     */
    public void selectAndShow(SQLObject selection) {
        if (selection instanceof SQLTable) {
            TablePane tp = findTablePane((SQLTable) selection);
            if (tp != null) {
                selectNone();
                tp.setSelected(true);
                Rectangle scrollTo = tp.getBounds();
                zoomRect(scrollTo);
                scrollRectToVisible(scrollTo);
            }
        } else if (selection instanceof SQLRelationship) {
            Relationship r = findRelationship((SQLRelationship) selection);
            if (r != null) {
                selectNone();
                r.setSelected(true);
                Rectangle scrollTo = r.getBounds();
                zoomRect(scrollTo);
                scrollRectToVisible(scrollTo);
            }
        }

    }
}
