package org.silcos.permainan;

public class Game {

	private Board gameBoard;
	private Player starterPlayer;
	private Player otherPlayer;
	
	private Game() {
		this.gameBoard = Board.filledInstance();
		this.starterPlayer = null;
		this.otherPlayer = null;
	}
	
	public Connector externalConnectorAt(int row, int column) {
		return (gameBoard.externalConnectorAt(row, column));
	}
	
	public Connector innerCircuit(int index) {
		return (gameBoard.innerCircuit(index));
	}
	
	public Connector outerCircuit(int index) {
		return (gameBoard.outerCircuit(index));
	}
	
	public Pebble pebbleAt(int row, int column) {
		return (gameBoard.pebbleAt(row, column));
	}
	
	public Player getStarterPlayer() {
		return (starterPlayer);
	}
	
	public Player getOtherPlayer() {
		return (otherPlayer);
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
	 * <tt>BoardChangeEvent</tt> is fired of type <tt>MOVE_PEBBLE</tt>.
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
	public void notifyInput(int sourceRow, int sourceColumn,
			int targetRow, int targetColumn) {
		gameBoard.movePebble(sourceRow, sourceColumn, targetRow, targetColumn);
	}
	
	/**
	 * <p>
	 * Acknowledges the input given by the user to move the pebble at
	 * (sourceRow, sourceColumn) through the external connector associated
	 * with it, to the stored destination.
	 * 
	 * @param sourceRow
	 * @param sourceColumn
	 */
	public void notifyLoopInput(int sourceRow, int sourceColumn) {
		Connector loop = gameBoard.externalConnectorAt(sourceRow, sourceColumn);
		
		if(loop.row0() == sourceRow)
			loop.moveThroughConnector(gameBoard, true);
		else
			loop.moveThroughConnector(gameBoard, false);
	}
	
	public void addBoardChangeListener(BoardChangeListener changeListener) {
		gameBoard.addBoardChangeListener(changeListener);
	}
	
	public void removeBoardChangeListener(BoardChangeListener changeListener) {
		gameBoard.removeBoardChangeListener(changeListener);
	}
	
	public static Game doubleUserGame() {
		Game game = new Game();
		game.setStarterPlayer(new HumanPlayer(game.gameBoard, game));
		game.setOtherPlayer(new HumanPlayer(game.gameBoard, game));
		
		return (game);
	}
	
}
