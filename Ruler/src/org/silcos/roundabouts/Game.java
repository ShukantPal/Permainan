package org.silcos.roundabouts;

/**
 * Controls the state and progression of the Permainan game, and allows
 * the <tt>Board</tt>, <tt>Player</tt> and UI objects to coordinate and
 * actually deliver the gaming experience on the screen.
 * 
 * @author Shukant Pal
 */
public class Game {

	/**
	 * Holds all the positions of the available-to-move pebbles and
	 * configurations of the external loops.
	 */
	private Board gameBoard;
	
	/**
	 * Player that starts the game by playing the first move.
	 */
	private Player starterPlayer;
	
	/**
	 * Player that moves after <tt>starterPlayer</tt> places the first
	 * move.
	 */
	private Player otherPlayer;

	/**
	 * Player who holds the current turn, and who solely has the right
	 * to move his/her pebbles on <tt>gameBoard</tt> via <tt>notifyInput
	 * </tt> and <tt>notifyLoopInput</tt>. This may also be done by the
	 * UI (for human players).
	 */
	private Player activePlayer;
	
	/**
	 * An adapter that performs the operations request on the screen to
	 * update game state for the human user.
	 */
	private UIAdapter visualAdapter;
	
	/**
	 * Holds whether any "long" move is in progress and is used to prevent
	 * concurrent moves.
	 */
	private volatile boolean moveLocked;

	/**
	 * Holds whether any "long" move is continuing via <tt>notifyLoopInput
	 * </tt> after an animation completes (to show the pebble going through
	 * the loop in the "long" move). This allows the move to occur even
	 * through <tt>moveLocked</tt> is set.
	 */
	private volatile boolean moveRepeating;
	
	/**
	 * The row from which the active pebble started its "long" move.
	 */
	private int startLongMoveRow;
	
	/**
	 * The column from which the active pebble started its "long" move.
	 */
	private int startLongMoveColumn;
	
	/**
	 * Number of times active pebble came on (startLongMoveRow,
	 * startLongMoveColumn), used to prevent infinite "long" moves.
	 */
	private int longMoveOverlapCount;
	
	/**
	 * Current row of the active pebble, which is undergoing a "long" move.
	 */
	private int longMoveRow;

	/**
	 * Current column of the active pebble, which is undergoing a "long"
	 * move.
	 */
	private int longMoveColumn;
	
	/**
	 * Whether the active pebble killed opponent pebbles. After capturing
	 * opponent pebbles, the active pebble may not land on an empty point.
	 */
	private boolean trippingOpponent;
	
	/**
	 * Current direction in which the active pebble is headed in a "long"
	 * move.
	 */
	private ConnectorOrientation longMoveDirection;
	
	private class LongMoveInvoker extends Thread {

		@Override
		public void run() {
			while(invokeLongMove()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if(!moveRepeating) {
				activePlayer = opponentOf(getActivePlayer());
				moveLocked = false;
			}
		}

	}
	
	private boolean isMoveLocked() {
		return (moveLocked);
	}
	
	/**
	 * Starts a "long" move that should initiate after the loop end.
	 * 
	 * @param row
	 * @param column
	 * @param direction
	 */
	private void startPath(int row, int column,
			ConnectorOrientation direction) {
		System.out.println("Start Path: " + row + ", " + column +
				" @" + direction.toString());
		
		if(gameBoard.pebbleAt(row, column) == null)
			return;
		
		if(!moveRepeating) {
			this.startLongMoveRow = row;
			this.startLongMoveColumn = column;
			this.longMoveOverlapCount = 0;
			this.trippingOpponent = false;
		} else {
			moveRepeating = false;
		}
		
		this.longMoveRow = row;
		this.longMoveColumn = column;
		this.longMoveDirection = direction;
		
		this.moveLocked = true;
		
		new LongMoveInvoker().start();
	}
	
	/**
	 * Continues to the next step in a "long" move, and moves the active
	 * pebble forward. A loop may also be triggered in this step.
	 */
	private boolean invokeLongMove() {
		boolean moveSuccess = false;
		Pebble oldPebble;
		int newLongMoveRow = longMoveRow, newLongMoveColumn = longMoveColumn;
		
		System.out.println("long-move incro");
		
		if(longMoveRow == startLongMoveRow &&
				longMoveColumn == startLongMoveColumn) {
			++(longMoveOverlapCount);
		}
		
		if(longMoveOverlapCount == 2)
			return (false);// prevent infinite "long" moves
		
		switch(longMoveDirection) {
		case UP:
			newLongMoveRow -= 1;
			break;
		case DOWN:
			newLongMoveRow += 1;
			break;
		case LEFT:
			newLongMoveColumn -= 1;
			break;
		case RIGHT:
			newLongMoveColumn += 1;
			break;
		default:
			oldPebble = null;
			break;
		}
		
		if(newLongMoveRow < 0 || newLongMoveRow > 5 ||
				newLongMoveColumn < 0 || newLongMoveColumn > 5) {
			return (loopLongMove());
		}
		
		oldPebble = pebbleAt(newLongMoveRow, newLongMoveColumn);
		
		if(oldPebble != null) {
			trippingOpponent = true;
		} else if(trippingOpponent) {
			return (false);
		}
		
		moveSuccess = gameBoard.movePebble(longMoveRow, longMoveColumn,
				newLongMoveRow, newLongMoveColumn);
		longMoveRow = newLongMoveRow;
		longMoveColumn = newLongMoveColumn;
		
		return (moveSuccess);
	}
	
	/**
	 * Performs the next step in the long move, when a loop is required,
	 * and the direction is to be changed.
	 *  
	 * @return - whether to continue the "long" move after this step
	 */
	private boolean loopLongMove() {
		Connector loop = externalConnectorAt(longMoveRow, longMoveColumn);
		
		if(loop == null)
			throw new RuntimeException("No connector found at: (" + longMoveRow
					+ ", " + longMoveColumn + ")");
		
		if(loop.row0() == longMoveRow) {
			if(pebbleAt(loop.row1(), loop.column1()) != null) {
				return (false);
			}
		} else if(pebbleAt(loop.row0(), loop.column0()) != null) {
			return (false);
		}

		moveRepeating = true;
		visualAdapter.invokeLoopAnimation(longMoveRow, longMoveColumn);
		return (false);
	}
	
	/**
	 * Constructs a new <tt>Game</tt> object with a filled board, but
	 * without any registered players. The players should be registered
	 * using <tt>setStarterPlayer</tt> and <tt>setOtherPlayer</tt>.
	 */
	private Game() {
		this.gameBoard = Board.filledInstance();
		this.starterPlayer = null;
		this.otherPlayer = null;
		this.activePlayer = null;
		this.moveLocked = false;
		this.moveRepeating = false;
	}
	
	/**
	 * Returns the value of <tt>gameBoard.externalConnecterAt</tt> for the
	 * given arguments.
	 */
	public Connector externalConnectorAt(int row, int column) {
		return (gameBoard.externalConnectorAt(row, column));
	}
	
	/**
	 * Returns the value of <tt>gameBoard.innerCircuit</tt> for the given
	 * arguments.
	 */
	public Connector innerCircuit(int index) {
		return (gameBoard.innerCircuit(index));
	}
	
	/**
	 * Returns the value of <tt>gameBoard.outerCircuit</tt> for the given
	 * arguments.
	 */
	public Connector outerCircuit(int index) {
		return (gameBoard.outerCircuit(index));
	}
	
	/**
	 * Returns the pebble holding the point at (row, column) 
	 */
	public Pebble pebbleAt(int row, int column) {
		return (gameBoard.pebbleAt(row, column));
	}
	
	/**
	 * Returns the value of the property <tt>starterPlayer</tt>
	 */
	public Player getStarterPlayer() {
		return (starterPlayer);
	}
	
	/**
	 * Returns the value of the property <tt>otherPlayer</tt>
	 */
	public Player getOtherPlayer() {
		return (otherPlayer);
	}
	
	/**
	 * Returns the value of the read-only property <tt>activePlayer</tt>
	 */
	public Player getActivePlayer() {
		return (activePlayer);
	}
	
	/**
	 * Returns the value of the property <tt>visualAdapter</tt>
	 */
	public UIAdapter getVisualAdapter() {
		return (visualAdapter);
	}

	/**
	 * Returns the player playing against the given player
	 * <tt>onPlayer</tt>
	 *
	 * @param onPlayer
	 */
	public Player opponentOf(Player onPlayer) {
		if(onPlayer == starterPlayer)
			return (getOtherPlayer());
		else
			return (getStarterPlayer());
	}
	
	/**
	 * Registers the starter player (that plays the first move) to this game,
	 * so that it can be notified. Once registered, a player cannot be changed.
	 * 
	 * @param starterPlayer - <tt>Player</tt> that will start the game
	 */
	public void setStarterPlayer(Player starterPlayer) {
		if(this.starterPlayer != null) {
			System.err.println("Warning : Game player change attempt while already set (starter)");
			return;
		}
		
		this.activePlayer = starterPlayer;
		this.starterPlayer = starterPlayer;
	}
	
	/**
	 * Registers the other player (that doesn't start the game), to this game,
	 * so that it can be notified. Once registered, a player cannot be changed.
	 * 
	 * @param otherPlayer - <tt>Player</tt> that will not start the game
	 */
	public void setOtherPlayer(Player otherPlayer) {
		if(this.otherPlayer != null) {
			System.err.println("Warning : Game player change attempt while already set (other)");
			return;
		}
		
		this.otherPlayer = otherPlayer;
	}
	
	/**
	 * Sets the value of the property <tt>visualAdapter</tt>
	 */
	public void setVisualAdapter(UIAdapter visualAdapter) {
		this.visualAdapter = visualAdapter;
	}
	
	/**
	 * Initializes the game board by placing all the pebbles initially
	 * owned by the two players (that should be registered before calling
	 * this method), on the board at their respective positions.
	 */
	public void placeAllPebbles() {
		int pebbleCount;
		
		pebbleCount = 0;
		for(int row = 0; row < 2; row++) {
			for(int column = 0; column < Board.linearSize; column++) {
				gameBoard.placePebble(starterPlayer.pebble(pebbleCount), row, column);
				++(pebbleCount);
			}
		}
		
		pebbleCount = 0;
		for(int row = Board.linearSize - 2; row < Board.linearSize; row++) {
			for(int column = 0; column < Board.linearSize; column++) {
				gameBoard.placePebble(otherPlayer.pebble(pebbleCount), row, column);
				++(pebbleCount);
			}
		}
	}
	
	/**
	 * <p>
	 * Acknowledges the input given by the user to move the pebble from
	 * (sourceRow, sourceColumn) to (targetRow, targetColumn). If the given
	 * input forms a valid move, then the pebble is actually moved, and a
	 * <tt>BoardChangeEvent</tt> is fired of type <tt>MOVE_PEBBLE</tt>. This
	 * will also cause the <tt>activePlayer</tt> property to change to the
	 * opponent. Any input given while another pebble in being "long" moved
	 * will not be acknowledged.
	 * 
	 * <p>
	 * The front-end should not inform any other module about the change,
	 * as it will be notified by the board, if it is registered as an
	 * <tt>BoardChangeListener</tt>.
	 * 
	 * @param sourceRow - the row of the pebble to be moved
	 * @param sourceColumn - the column of the pebble to be moved
	 * @param targetRow - destination row for the pebble
	 * @param targetColumn - destination column for the pebble
	 * @see org.silcos.permanin.Board.movePebble
	 */
	public synchronized void notifyInput(int sourceRow, int sourceColumn,
			int targetRow, int targetColumn) {
		if(isMoveLocked())
			return;
		
		Player nextActivePlayer = opponentOf(
				gameBoard.pebbleAt(sourceRow, sourceColumn).owner());
		
		if(nextActivePlayer == activePlayer)
			return;// opponent cannot move nah
		
		if(gameBoard.movePebble(sourceRow, sourceColumn,
				targetRow, targetColumn)) {
			activePlayer = nextActivePlayer;
		}
	}
	
	/**
	 * <p>
	 * Acknowledges the input given by the user to move the pebble at
	 * (sourceRow, sourceColumn) through the external connector associated
	 * with it, to the stored destination.
	 * 
	 * <p>
	 * <b>activePlayer</b> will remain the same until the "long" move
	 * associated with this loop is over, which depends on the number of
	 * points to cross. On finishing the long move, the <tt>activePlayer</tt>
	 * property will switch to the opponent.
	 * 
	 * <p>
	 * Note that the front-end UI must handle moving the pebble through the
	 * loop. It will not be separately notified for making an animation. <b>
	 * The destination given must be free also, otherwise the application
	 * will break here.</b>
	 * 
	 * @param sourceRow
	 * @param sourceColumn
	 */
	public synchronized void notifyLoopInput(int sourceRow, int sourceColumn) {
		if(isMoveLocked() && !moveRepeating)
			return;
		
		if(!moveRepeating) {
			Player nextActivePlayer = opponentOf(
					gameBoard.pebbleAt(sourceRow, sourceColumn).owner());
		
			if(nextActivePlayer == activePlayer)
				return;// opponent cannot move nah
		}

		Connector loop = gameBoard.externalConnectorAt(sourceRow, sourceColumn);
		
		if(loop == null)
			return;
		
		boolean c0;
		if(loop.row0() == sourceRow) {
			loop.moveThroughConnector(gameBoard, true);
			c0 = true;
		} else {
			loop.moveThroughConnector(gameBoard, false);
			c0 = false;
		}
		
		if(c0 == false)
			startPath(loop.row0(), loop.column0(),
					Board.inwardPerpendicular(loop.row0(), loop.column0()));
		else
			startPath(loop.row1(), loop.column1(),
					Board.inwardPerpendicular(loop.row1(), loop.column1()));
	}
	
	/**
	 * Adds the given listener to be notified whenever the state of
	 * the <tt>gameBoard</tt> changes.
	 * 
	 * @param changeListener - board-change listener
	 */
	public void addBoardChangeListener(BoardChangeListener changeListener) {
		gameBoard.addBoardChangeListener(changeListener);
	}
	
	/**
	 * Removes the given listener from being notified whenever the state
	 * of the <tt>gameBoard</tt> changes, if it was added before.
	 * 
	 * @param changeListener - registered board-change listener to remove
	 */
	public void removeBoardChangeListener(BoardChangeListener changeListener) {
		gameBoard.removeBoardChangeListener(changeListener);
	}
	
	/**
	 * Returns whether the long move starting from (startRow, startColumn)
	 * will capture an opponent pebble or not. It practically simulates the
	 * "long" moves without actually modifying the board.
	 * 
	 * @param startRow
	 * @param startColumn
	 * @return
	 */
	public boolean isLongMoveCapturable(int startRow, int startColumn) {
		Player activePebbleOwner = pebbleAt(startRow, startColumn).owner();
		
		int curRow = startRow, curColumn = startColumn;
	//	ConnectorOrientation curDir =
	//			Board.inwardPerpendicular(curRow, curColumn);
//		boolean curOpponentTripping = false;
		
		while(true) {
			// TODO: Finish this
			
			if(pebbleAt(curRow, curColumn).owner() == activePebbleOwner) {
				break;
			}
		}
		
		return (false);
	}
	
	/**
	 * Factory for <tt>Game</tt> objects that are controlled by two
	 * <tt>HumanPlayer</tt> objects.
	 */
	public static Game doubleUserGame() {
		Game game = new Game();
		game.setStarterPlayer(new HumanPlayer(game.gameBoard, game));
		game.setOtherPlayer(new HumanPlayer(game.gameBoard, game));
		
		return (game);
	}
	
}
