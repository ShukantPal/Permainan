package org.silcos.permainan;

import java.util.HashMap;

/**
 * Represents an change-event in a <tt>Board</tt> object that could be
 * of varying type.
 * 
 * @author Shukant Pal
 */
public class BoardChangeEvent {
	
	private BoardChangeType changeType;
	private HashMap<String, Object> userData;
	
	/**
	 * Constructs a change-event for a <tt>Board</tt> that represents
	 * a change in it.
	 * 
	 * @param changeType - the type of event for this board
	 */
	BoardChangeEvent(BoardChangeType changeType) {
		this.changeType = changeType;
		this.userData = new HashMap<String, Object>(2);
	}
	
	/**
	 * Returns the type of this event.
	 */
	public BoardChangeType changeType() {
		return (changeType);
	}

	/**
	 * Returns a user-data object named by the given key.
	 * 
	 * @param dataKey - the key identifying the object
	 * @return the user-data pointed to
	 */
	public Object getUserData(String dataKey) {
		return (userData.get(dataKey));
	}
	
	/**
	 * Returns a user-data object named by the given key
	 * 
	 * @param dataKey - the key identifying the object
	 * @param value - the user-data object
	 */
	public void setUserData(String dataKey, Object value) {
		userData.put(dataKey, value);
	}
	
	/**
	 * Instantiates a new <tt>BoardChangeEvent</tt> for placing a new
	 * pebble on the board at (row, column).
	 * 
	 * @param row - the row on which the pebble is placed
	 * @param column - the column on which the pebble is placed
	 * @return the <tt>BoardChangeEvent</tt> representing the placement
	 */
	static BoardChangeEvent newPebblePlacedEvent(
			Integer row, Integer column, Pebble pebble) {
		BoardChangeEvent e = new BoardChangeEvent(BoardChangeType.PLACE_PEBBLE);
		e.setUserData("targetRow", row);
		e.setUserData("targetColumn", column);
		e.setUserData("target", pebble);
		return (e);
	}
	
	/**
	 * Instantiates a new <tt>BoardChangeEvent</tt> for moving a pebble on
	 * the board from (srcRow, srcColumn) to (targetRow, targetColumn).
	 * 
	 * @param srcRow - the original row of the pebble
	 * @param srcColumn - the original column of the pebble
	 * @param targetRow - the destination row of the pebble
	 * @param targetColumn - the destination column of the pebble
	 * @return the <tt>BoardChangeEvent</tt> for moving the pebble
	 */
	static BoardChangeEvent newPebbleMovedEvent(Pebble pebble, Integer srcRow,
			Integer srcColumn, Integer targetRow, Integer targetColumn) {
		BoardChangeEvent e = new BoardChangeEvent(BoardChangeType.MOVE_PEBBLE);
		e.setUserData("sourceRow", srcRow);
		e.setUserData("sourceColumn", srcColumn);
		e.setUserData("targetRow", targetRow);
		e.setUserData("targetColumn", targetColumn);
		e.setUserData("target", pebble);
		return (e);
	}
	
}
