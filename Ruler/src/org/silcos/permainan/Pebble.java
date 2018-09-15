package org.silcos.permainan;

public class Pebble {
	
	Player owner;
	
	public Pebble(Player owner) {
		this.owner = owner;
	}
	
	public Player owner() {
		return (owner);
	}
	
}
