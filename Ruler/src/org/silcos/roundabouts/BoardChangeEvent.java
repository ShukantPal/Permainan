package org.silcos.roundabouts;

import java.util.HashMap;

/**
 * Represents an change-event in a <tt>Board</tt> object that could be
 * of varying type - <tt>PLACE_PEBBLE</tt>, <tt>MOVE_PEBBLE</tt>,
 * and <tt>CAPTURE_PEBBLE</tt>. Additional information is stored as
 * a set of <tt>String-Object</tt> pairs, which can be accessed using
 * the <tt>userData</tt> getter and setter methods.
 * 
 * These pairs have the following definition:
 * <p>
 * <table border="1">
 * 	<tr>
 * 		<th>String</th>
 * 		<th>Object</th>
 * 		<th>Type</th>
 * 	</tr>
 * 	<tr>
 * 		<td>"sourceRow"</td>
 * 		<td><tt>int</tt> holding original row of pebble</td>
 * 		<td><tt>MOVE_PEBBLE</tt>, <tt>CAPTURE_PEBBLE</tt></td>
 * 	</tr>
 * 	<tr>
 * 		<td>"sourceColumn"</td>
 * 		<td><tt>int</tt> holding original column of pebble</td>
 * 		<td><tt>MOVE_PEBBLE</tt>, <tt>CAPTURE_PEBBLE</tt></td>
 * 	</tr>
 * 	<tr>
 * 		<td>"targetRow"</td>
 * 		<td><tt>int</tt> holding destination row of pebble</td>
 * 		<td><tt>PLACE_PEBBLE</tt>, <tt>MOVE_PEBBLE</tt>, <tt>CAPTURE_PEBBLE</tt></td>
 * 	</tr>
 * 	<tr>
 * 		<td>"targetColumn"</td>
 * 		<td><tt>int</tt> holding destination column of pebble</td>
 * 		<td><tt>PLACE_PEBBLE</tt>, <tt>MOVE_PEBBLE</tt>, <tt>CAPTURE_PEBBLE</tt></td>
 * 	</tr>
 * 	<tr>
 * 		<td>"target"</td>
 * 		<td><tt>Pebble</tt> being placed or moved</td>
 * 		<td><tt>PLACE_PEBBLE</tt>, <tt>MOVE_PEBBLE</tt>, <tt>CAPTURE_PEBBLE</tt></td>
 * 	</tr>
 * 	<tr>
 * 		<td>"victim"</td>
 * 		<td><tt>Pebble</tt> being removed from the board (captured)</td>
 * 		<td><tt>CAPTURE_PEBBLE</tt></td> 
 * 	</tr>
 * </table>
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
	
	/**
	 * Instantiates a new <tt>BoardChangeEvent</tt> for moving, the target,
	 * and capturing the victim, from (srcRow, srcColumn) to
	 * (targetRow, targetColumn).
	 * 
	 * @param source - the pebble being moved
	 * @param victim - the pebble captured in the process
	 * @param srcRow - the original row of the source pebble
	 * @param srcColumn - the original column of the source pebble
	 * @param targetRow - the destination row of the source
	 * @param targetColumn - the destination column of the source
	 * @return the <tt>BoardChangeEvent</tt> for moving and capturing
	 */
	static BoardChangeEvent newPebbleKilledEvent(Pebble source, Pebble victim,
			Integer srcRow, Integer srcColumn, Integer targetRow,
			Integer targetColumn) {
		BoardChangeEvent e = new BoardChangeEvent(BoardChangeType.CAPTURE_PEBBLE);
		e.setUserData("sourceRow", srcRow);
		e.setUserData("sourceColumn", srcColumn);
		e.setUserData("targetRow", targetRow);
		e.setUserData("targetColumn", targetColumn);
		e.setUserData("target", source);
		e.setUserData("victim", victim);
		return (e);
	}
	
}
