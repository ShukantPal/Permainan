package org.silcos.roundabouts;

import java.util.LinkedList;

/**
 * Holds the state of the Permainan game board.
 * 
 * <p>
 * Corners of this board are accessed using their indexes. They are
 * ordered as upper-left, upper-right, bottom-right, and bottom-left
 * and the indexes are 0, 1, 2, and 3 respectively.
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
	
	private Connector outerCircuits[];
	private Connector innerCircuits[];
	
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
	 * Returns the orientation with which the point at the given coordinates
	 * will connect with its external connector, if any exists. This is always
	 * perpendicular to the edge on which the point lies (<tt>UNDEFINED</tt>
	 * for internal points).
	 * 
	 * @param row - the row of the point's coordinates
	 * @param column - the column of the point's coordinates
	 * @return - the orientation with which the point will connect
	 */
	private static ConnectorOrientation loopOrientationAt(int row, int column) {
		if(row == 0) {
			return (ConnectorOrientation.UP);
		} else if(row == 5) {
			return (ConnectorOrientation.DOWN);
		} else if(column == 0) {
			return (ConnectorOrientation.LEFT);
		} else if(column == 5) {
			return (ConnectorOrientation.RIGHT);
		}
		
		return (ConnectorOrientation.UNDEFINED);
	}
	
	/**
	 * Associates the connector with the points at the coordinates fed
	 * into it, .i.e. points at (row0(), column0()) and
	 * (row1(), column1()) and also sets the connector orientations.
	 * 
	 * @param connector - the connector to link proper
	 */
	private void linkConnector(Connector connector) {
		grid[connector.row0()][connector.column0()]
				.setExternalConnector(connector);
		
		connector.setLink0(loopOrientationAt(
				connector.row0(), connector.column0()));
		
		grid[connector.row1()][connector.column1()]
				.setExternalConnector(connector);
		
		connector.setLink1(loopOrientationAt(
				connector.row1(), connector.column1()));
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
		
		innerCircuits = new Connector[] {
				new Connector(1, 0, 0, 1),
				new Connector(0, 4, 1, 5),
				new Connector(4, 5, 5, 4),
				new Connector(5, 1, 4, 0)
		};
		
		outerCircuits = new Connector[] {
				new Connector(2, 0, 0, 2),
				new Connector(0, 3, 2, 5),
				new Connector(3, 5, 5, 3),
				new Connector(5, 2, 3, 0)
		};
	}
	
	/**
	 * Returns the connector associated with the point at the given
	 * coordinates.
	 * 
	 * @param row - row of the point
	 * @param column - column of the point
	 */
	public Connector externalConnectorAt(int row, int column) {
		if(inBounds(row, column)) {
			return (grid[row][column].getExternalConnector());
		} else {
			return (null);
		}
	}
	
	/**
	 * Returns the inner-circuit connector for the given corner (given
	 * by index). It connects the points on the edges that are one
	 * unit away from the corner.
	 * 
	 * @param index - index of the corner for the inner-circuit
	 * @see Board
	 */
	public Connector innerCircuit(int index) {
		return (innerCircuits[index]);
	}
	
	/**
	 * Returns the outer-circuit connector for the given corner (given
	 * by index). It connects the points on the edges that are two
	 * units away from the corner.
	 * 
	 * @param index - index of the corner for the outer circuit
	 * @see Board
	 */
	public Connector outerCircuit(int index) {
		return (outerCircuits[index]);
	}
	
	/**
	 * Returns the pebble placed at the given coordinates, provided
	 * they are in-bounds; otherwise, null is returned, which cannot be
	 * distinguished from "empty".
	 * 
	 * @param row - the row of the point at which pebble is required
	 * @param column - the column of the point at which pebble is
	 * 				required
	 */
	public Pebble pebbleAt(int row, int column) {
		if(inBounds(row, column)) {
			return (grid[row][column].getHolder());
		} else {
			return (null);
		}
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
	 * <p>
	 * Enforces the move given by a player/user on this board, placing the
	 * target pebble from the <tt>(sourceRow, sourceColumn)</tt> source
	 * coordinates to the <tt>(targetRow, targetColumn)</tt> target
	 * coordinates. To do so, the source must be filled and the target must
	 * already be empty. In addition, a pebble may only move left, up, down,
	 * right, or diagonally.
	 * 
	 * <p>
	 * This adjacency rule is violated in the case of two points connected by
	 * an loop (via <tt>Connector</tt> objects). Any two points mutually
	 * connected through their <tt>externalConnector</tt> property can transfer
	 * pebbles.
	 * 
	 * @param sourceRow - the row of the original point holder
	 * @param sourceColumn - the column of the original point holder
	 * @param targetRow - the row of the destination point holder
	 * @param targetColumn - the column of the destination point holder
	 * @return whether the pebble was moved or not
	 */
	public boolean movePebble(int sourceRow, int sourceColumn,
			int targetRow, int targetColumn) {
		if(grid[sourceRow][sourceColumn].isEmpty()) {
			return (false);
		} else if(!grid[targetRow][targetColumn].isEmpty()
				&& grid[targetRow][targetColumn].getHolder().owner()
				== grid[sourceRow][sourceColumn].getHolder().owner()) {
			/*
			 * Here, we allow players to kill enemy pebbles that are adjacent
			 * to their pebbles. This is a violation of the "rules". But some
			 * front-ends may want this feature, and hence, it is the UI
			 * responsible for "not allowing" pieces to be killed by moving
			 * adjacently (without going through a loop).
			 */
			
			return (false);
		} else {
			Connector sourceTargetLoop = externalConnectorAt(
					sourceRow, sourceColumn);
			boolean loopExists = false;
			
			if(sourceTargetLoop != null) {
				if(sourceRow == sourceTargetLoop.row0()) {
					if(targetRow == sourceTargetLoop.row1())
						loopExists = true;
				} else {
					if(targetRow == sourceTargetLoop.row0()) {
						loopExists = true;
					}
				}
				
				if(loopExists)
					System.out.println("true loop");
			}
			
			if(!loopExists && (Math.abs(sourceRow - targetRow) > 1 ||
					Math.abs(sourceColumn - targetColumn) > 1)) {
				return (false);
			}
		}
		
		Pebble target = grid[sourceRow][sourceColumn].getHolder();
		Pebble victim = grid[targetRow][targetColumn].getHolder();
		
		grid[targetRow][targetColumn].setHolder(target);
		grid[sourceRow][sourceColumn].setHolder(null);
		
		if(victim == null)
			fireEvent(BoardChangeEvent.newPebbleMovedEvent(target,
					sourceRow, sourceColumn, targetRow, targetColumn));
		else
			fireEvent(BoardChangeEvent.newPebbleKilledEvent(target, victim,
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
	 * Returns whether the point at the given coordinates is on the edge
	 * of the board or not.
	 * 
	 * @param row - row of the point
	 * @param column - column of the point
	 */
	public static boolean isEdgePoint(int row, int column) {
		return (row == 0 || row == 5 || column == 0 || column == 5);
	}

	/**
	 * Returns whether the given point lies on an <b>edge</b> but is <b>
	 * not a corner</b> point.
	 * 
	 * @param row - row of the point
	 * @param column - column of the point.
	 */
	public static boolean isEdgeButNotCornerPoint(int row, int column) {
		if(row == 0 || row == 5) {
			if(column == 0 || column == 5) {
				return (true);
			} else {
				return (false);
			}
		} else {
			return (false);
		}
	}
	
	/**
	 * Returns the direction of the perpendicular on the edge at which the
	 * given non-corner edge-point lies. This is opposite to the orientation
	 * of the loop-connector for the given point w.r.t to it. If the given
	 * point is a corner, then results will be partial and if it isn't a
	 * edge-point, then <tt>UNDEFINED</tt> will be returned.
	 * 
	 * @param row - row of the non-corner edge point
	 * @param column - column of the non-corner edge point
	 */
	public static ConnectorOrientation inwardPerpendicular(int row, int column) {
		if(row == 0) {
			return (ConnectorOrientation.DOWN);
		} else if(row == 5) {
			return (ConnectorOrientation.UP);
		} else if(column == 0) {
			return (ConnectorOrientation.RIGHT);
		} else if(column == 5) {
			return (ConnectorOrientation.LEFT);
		} else {
			return (ConnectorOrientation.UNDEFINED);
		}
	}
	
	/**
	 * Returns the coordinates of the corner nearest to the given point
	 * in the grid. If the point given is out of bounds, then null is
	 * returned instead.
	 *
	 * @param row - the row of the point
	 * @param column - the column of the point
	 */
	public static int[] nearestCornerTo(int row, int column) {
		if(!inBounds(row, column))
			return (null);
		
		int[] cornerCoordinates = new int[2];
	
		if(row <= 2) {
			cornerCoordinates[0] = 0;
		} else {
			cornerCoordinates[0] = 5;
		}
		
		if(column <= 2) {
			cornerCoordinates[1] = 0;
		} else {
			cornerCoordinates[1] = 5;
		}
		
		return (cornerCoordinates);
	}
	
	/**
	 * Returns the integral distance between the given point and the nearest
	 * corner. This distance is equal to the sum of the (signed) differences
	 * between the row and column coordinates, e.g. distance b/w (0,0) and
	 * (1,1) is 2 but the distance b/w (1,1) and (0,0) is -2.
	 * 
	 * @param row - the row of the given point
	 * @param column - the column of the given point
	 */
	public static int nearestIntegralCornerDistance(int row, int column) {
		int[] nearestCorner = nearestCornerTo(row, column);
		
		return ((nearestCorner[0] - row) + (nearestCorner[1] - column));
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
	 * that can hold pebbles placed by both players. The external loop
	 * connectors also are linked proper (using <tt>linkConnector()</tt>). It
	 * is the responsibility of the <tt>Game</tt> controller object to
	 * initially place the pebbles properly.
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
		
		for(Connector innerCircuit : newBoard.innerCircuits) {
			newBoard.linkConnector(innerCircuit);
		}
		
		for(Connector outerCircuit : newBoard.outerCircuits) {
			newBoard.linkConnector(outerCircuit);
		}
		
		return (newBoard);
	}
	
}
