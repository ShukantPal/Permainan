package org.silcos.permainan;

import java.util.LinkedList;

/**
 * Holds the state of the Permanin game board.
 * 
 * @author Shukant Pal
 */
public class Board {

	public static final int linearSize = 6;
	public static final int arealSize = 36;
	
	public static final int INNER = 1;
	public static final int MIDDLE = 2;
	public static final int OUTER = 3;
	public static final int OUT_OF_BOUNDS = 1389;
	
	/**
	 * Holds the colors of each line at those indices in one
	 * array. To get the color of the <tt>nth</tt> line, use
	 * <tt>colorByLine[n]</tt>
	 */
	private static final int[] colorByLine = {
			OUTER,
			MIDDLE,
			INNER,
			INNER,
			MIDDLE,
			OUTER
	};
	
	private Point[][] grid;
	private LinkedList<BoardChangeListener> boardChangeListeners;
	
	/**
	 * Returns whether the given line is valid, and lies on this board
	 * or not.
	 * 
	 * @param line - index of the line starting from the origin
	 */
	private static boolean inBounds(int line) {
		return (line >= 0 && line < linearSize);
	}
	
	/**
	 * Returns whether the coordinates are in-bounds, and lie on this
	 * board or not.
	 * 
	 * @param row - row of the point
	 * @param column - column of the point
	 */
	private static boolean inBounds(int row, int column) {
		return (row >= 0 && row < linearSize &&
				column >= 0 && column < linearSize);
	}
	
	/**
	 * Invokes all the registered board-change event listeners using
	 * the given <tt>BoardChangeEvent</tt>.
	 * 
	 * @param e - the event to pass to all the listeners
	 */
	private void fireEvent(BoardChangeEvent e) {
		boardChangeListeners.forEach(
					(BoardChangeListener changeListener) -> {
						changeListener.handle(e);
					}
				);
	}
	
	private Board() {
		grid = new Point[linearSize][linearSize];
		boardChangeListeners = new LinkedList<BoardChangeListener>();
	}
	
	/**
	 * <p>
	 * Places a new pebble on this board, at the given coordinates. If a
	 * pebble was already placed at that point, it won't be replaced and
	 * no change will occur.
	 * 
	 * <p>
	 * To avoid duplicate errors, the caller must force the pebble to not
	 * be already placed on this board. 
	 * 
	 * @param pebble - the unplaced pebble
	 * @param row - row of the destination point
	 * @param column - column of the destination point
	 * @return whether the pebble was placed successfully
	 */
	public boolean placePebble(Pebble pebble, int row, int column) {
		if(inBounds(row, column) && grid[row][column].isEmpty()) {
			grid[row][column].setHolder(pebble);
			fireEvent(BoardChangeEvent.newPebblePlacedEvent(row, column, pebble));
			
			return (true);
		} else {
			return (false);
		}
	}
	
	/**
	 * Enforces the move given by a player/user on this board, placing the
	 * target pebble from the <tt>(sourceRow, sourceColumn)</tt> source
	 * coordinates to the <tt>(targetRow, targetColumn)</tt> target
	 * coordinates. To do so, the source must be filled and the target must
	 * already be empty.
	 * 
	 * @param sourceRow - the row of the original point holder
	 * @param sourceColumn - the column of the original point holder
	 * @param targetRow - the row of the destination point holder
	 * @param targetColumn - the column of the destination point holder
	 * @return whether the pebble was moved or not
	 */
	public boolean movePebble(int sourceRow, int sourceColumn,
			int targetRow, int targetColumn) {
		if(!grid[targetRow][targetColumn].isEmpty()
				|| grid[sourceRow][sourceColumn].isEmpty())
			return (false);

		Pebble target = grid[sourceRow][sourceColumn].getHolder();
		grid[targetRow][targetColumn].setHolder(target);
		grid[sourceRow][sourceColumn].setHolder(null);
		
		fireEvent(BoardChangeEvent.newPebbleMovedEvent(target,
				sourceRow, sourceColumn, targetRow, targetColumn));
		
		return (true);
	}
	
	/**
	 * Registers the given board-change listener, so that it is called
	 * whenever the board state changes.
	 * 
	 * @param changeListener - the board-change event listener
	 */
	public void addBoardChangeListener(BoardChangeListener changeListener) {
		boardChangeListeners.add(changeListener);
	}
	
	/**
	 * Removes the given board-change listener, so that it is not invoked
	 * whenever the board state changes.
	 * 
	 * @param changeListener - an already registered board-change event
	 * 			listener.
	 */
	public void removeBoardChangeListener(BoardChangeListener changeListener) {
		boardChangeListeners.remove(changeListener);
	}
	
	/**
	 * Returns the color of the line, <tt>lineIndex</tt> lines away
	 * from the origin (upper-left corner). If <tt>lineIndex</tt> is
	 * out of bounds, then <tt>OUT_OF_BOUNDS</tt> is returned, without
	 * throwing any exception.
	 * 
	 * @param lineIndex - the distance of the given line from the origin
	 * 			(upper-left corner).
	 * @return the color of the referred line
	 */
	public static int colorOf(int lineIndex) {
		if(inBounds(lineIndex)) {
			return (colorByLine[lineIndex]);
		} else {
			return (OUT_OF_BOUNDS);
		}
	}
	
	/**
	 * Returns the color coordinates of the given point on the board. If
	 * the any coordinate is out-of-bounds, <tt>OUT_OF_BOUNDS</tt> is filled
	 * instead at the color coordinate.
	 * 
	 * @param row - the row of the given point
	 * @param column - the column of the given point
	 * @return the colors of the row & column of the given point, which
	 * 			constitute its color coordinates
	 */
	public static int[] colorOf(int row, int column) {
		int[] colorSet = new int[2];
		colorSet[0] = colorOf(row);
		colorSet[1] = colorOf(column);
		
		return (colorSet);
	}
	
	/**
	 * Instantiates a new board & fills its grid with newly constructed points
	 * that can hold pebbles placed by both players. It is the responsibility
	 * of the <tt>Game</tt> controller object to initially place the pebbles
	 * properly.
	 * 
	 * @return the newly created, but filled, board
	 */
	public static Board filledInstance() {
		Board newBoard = new Board();
		
		for(int rowIdx = 0; rowIdx < linearSize; rowIdx++) {
			for(int colIdx = 0; colIdx < linearSize; colIdx++) {
				newBoard.grid[rowIdx][colIdx] = new Point();
			}
		}

		return (newBoard);
	}
	
}
