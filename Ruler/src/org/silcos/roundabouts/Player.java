package org.silcos.roundabouts;

public class Player {
	
	Board playBoard;
	Game gameInstance;
	
	Pebble[] pebbleSet;
	
	public Player(Board playBoard, Game gameInstance) {
		this.playBoard = playBoard;
		this.gameInstance = gameInstance;
		this.pebbleSet = new Pebble[Board.linearSize * 2];
		
		for(int pidx = 0; pidx < Board.linearSize * 2; pidx++) {
			pebbleSet[pidx] = new Pebble(this);
		}
	}

	public Pebble pebble(int id) {
		return (pebbleSet[id]);
	}
	
	public Game gameInstance() {
		return (gameInstance);
	}
	
	public static int pebbleSetSize() {
		return (Board.linearSize * 2);
	}
	
}
