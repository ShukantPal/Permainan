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
	
	public Player getStarterPlayer() {
		return (starterPlayer);
	}
	
	public Player getOtherPlayer() {
		return (otherPlayer);
	}
	
	public void setStarterPlayer(Player starterPlayer) {
		if(this.starterPlayer != null) {
			System.err.println("Warning : Game player change attempt while already set (starter)");
			return;
		}
		
		this.starterPlayer = starterPlayer;
	}
	
	public void setOtherPlayer(Player otherPlayer) {
		if(this.otherPlayer != null) {
			System.err.println("Warning : Game player change attempt while already set (other)");
			return;
		}
		
		this.otherPlayer = otherPlayer;
	}
	
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
	
	public void notifyInput(int sourceRow, int sourceColumn,
			int targetRow, int targetColumn) {
		gameBoard.movePebble(sourceRow, sourceColumn, targetRow, targetColumn);
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
