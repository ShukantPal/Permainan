package org.silcos.roundabouts;

public class Pebble {
	
	Player owner;
	
	public Pebble(Player owner) {
		this.owner = owner;
	}
	
	public Player owner() {
		return (owner);
	}
	
}
