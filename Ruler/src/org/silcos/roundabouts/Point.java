package org.silcos.roundabouts;

/**
 * <p>
 * Represents a location on the grid on which players can place a pebble. All
 * points on the game board are stored in an 2-D array, and are identified by
 * their (row, column) coordinates. Points can be surrounded by other points
 * in all four directions (and hence, are called internal points) or can be
 * on the edge (and hence, are called edge points).
 * 
 * <p>
 * Certain edge points are connected to other-edge points through structures
 * that look visually like loops. There are eight such loops on the Permainan
 * board, and these points expose those <tt>Connector</tt> loops through the
 * <tt>externalConnector</tt> property.
 * 
 * @author Shukant Pal
 */
public class Point {

	private Connector externalConnector;
	private Pebble holder;
	
	/**
	 * Constructs a <tt>Point</tt> with no pebble holding it, and with no
	 * external connector.
	 */
	public Point() {
		this.externalConnector = null;
		this.holder = null;
	}
	
	/**
	 * Constructs a <tt>Point</tt> with the given pebble holding it, but with
	 * no external connector.
	 * 
	 * @param holder - the pebble holding the newly created <tt>Point</tt>
	 */
	public Point(Pebble holder) {
		this.holder = holder;
	}
	
	/**
	 * Returns whether this point is held by a pebble or not.
	 */
	public boolean isEmpty() {
		return (holder == null);
	}
	
	/**
	 * Returns the external connector associated with this point; null, if no
	 * such connector exists.
	 */
	public Connector getExternalConnector() {
		return (externalConnector);
	}
	
	/**
	 * Associates the given connector with this point, so that it can be
	 * directly accessed using <tt>getExternalConnector</tt>. If a connector
	 * is already associated, then this method will silently fail.
	 * 
	 * @param externalConnector - the connector to associate with this point
	 */
	public void setExternalConnector(Connector externalConnector) {
		if(this.externalConnector == null)
			this.externalConnector = externalConnector;
	}
	
	/**
	 * Returns the pebble holding this point, i.e. placed at this point.
	 */
	public Pebble getHolder() {
		return (holder);
	}

	/**
	 * Places the given pebble at this point, assuming no other point is held
	 * by this pebble.
	 * 
	 * @param holder - the pebble to place here
	 */
	public void setHolder(Pebble holder) {
		this.holder = holder;
	}
	
}
