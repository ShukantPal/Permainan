package org.silcos.permainan;

import java.util.HashMap;

/**
 * <p>
 * Holds data about "special" connections between two points on the board.
 * that are not spatially adjacent. These are manifested as loops on the
 * Permainan board, usually displayed using <tt>Path</tt>.
 * 
 * <p>
 * Users can register objects to hold with specific connectors, and can
 * be notified on activation of a "connector".
 * 
 * @author Shukant Pal
 */
public class Connector {

	private int row0, column0;
	private ConnectorOrientation link0;
	
	private int row1, column1;
	private ConnectorOrientation link1;
	
	private HashMap<String, Object> userProperties;
	
	/**
	 * Constructs a new <tt>Connector</tt> linking two coordinates on an
	 * unknown board. Also sets the connector orientations to <tt>UNDEFINED
	 * </tt>.
	 * 
	 * @param row0 - row of first coordinate
	 * @param column0 - column of first coordinate
	 * @param row1 - row of second coordinate
	 * @param column1 - column of second coordinate
	 */
	public Connector(int row0, int column0, 
			int row1, int column1) {
		this.row0 = row0;
		this.column0 = column0;
		this.link0 = ConnectorOrientation.UNDEFINED;
		
		this.row1 = row1;
		this.column1 = column1;
		this.link1 = ConnectorOrientation.UNDEFINED;
		
		this.userProperties = new HashMap<String, Object>();
	}
	
	/**
	 * Returns the column of the first point linked with this connector.
	 */
	public int column0() {
		return (column0);
	}
	
	/**
	 * Returns the column of the second point linked with this connector.
	 */
	public int column1() {
		return (column1);
	}
	
	/**
	 * Returns the connector-orientation w.r.t the first point.
	 */
	public ConnectorOrientation link0() {
		return (link0);
	}
	
	/**
	 * Returns the connector-orientation w.r.t the second point.
	 */
	public ConnectorOrientation link1() {
		return (link1);
	}
	
	/**
	 * Returns the row of the first point linked with this connector.
	 */
	public int row0() {
		return (row0);
	}
	
	/**
	 * Returns the row of the second point linked with this connector.
	 */
	public int row1() {
		return (row1);
	}
	
	public Object getUserProperty(String key) {
		return (userProperties.get(key));
	}
	
	public void setUserProperty(String key, Object value) {
		userProperties.put(key, value);
	}
	
	/**
	 * Sets the connector-orientation for this connector w.r.t to the
	 * first point. It cannot be set again, after calling it once.
	 * 
	 * @param link0 - connector orientation w.r.t first point
	 */
	public void setLink0(ConnectorOrientation link0) {
		if(this.link0 == ConnectorOrientation.UNDEFINED)
			this.link0 = link0;
	}
	
	/**
	 * Sets the connector-orientation for this connector w.r.t to the
	 * second point. It cannot be set again, after calling it once.
	 * 
	 * @param link1 - connector orientation w.r.t second point
	 */
	public void setLink1(ConnectorOrientation link1) {
		if(this.link1 == ConnectorOrientation.UNDEFINED)
			this.link1 = link1;
	}
	
	public boolean activableWith0(int row, int column, int adjust) {
		row -= adjust;
		column -= adjust;
		
		switch(link0) {
		case UP:
			return ((row < row0) && (column == column0));
		case LEFT:
			return ((row == row0) && (column < column0));
		case DOWN:
			return ((row > row0) && (column == column0));
		case RIGHT:
			return ((row == row0) && (column > column0));
		default:
			return (false);
		}
	}
	
	public boolean activableWith1(int row, int column, int adjust) {
		row -= adjust;
		column -= adjust;
		
		switch(link1) {
		case UP:
			return ((row < row1) && (column == column1));
		case LEFT:
			return ((row == row1) && (column < column1));
		case DOWN:
			return ((row > row1) && (column == column1));
		case RIGHT:
			return ((row == row1) && (column > column1));
		default:
			return (false);
		}
	}

	/**
	 * Places a move on the given <tt>Board</tt> to move the pebble from point0
	 * to point1 (or vice-versa based on <tt>forwardDirection</tt>).
	 * 
	 * @param targetBoard - the board on which to place a move
	 * @param forwardDirection - whether the source should be point0 or point1
	 */
	public void moveThroughConnector(
			Board targetBoard, boolean forwardDirection) {
		if(forwardDirection) {
			targetBoard.movePebble(row0, column0, row1, column1);
		} else {
			targetBoard.movePebble(row1, column1, row0, column0);
		}
	}
	
}
