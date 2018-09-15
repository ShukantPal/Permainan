package org.silcos.permainan;

public class Point {
	
	Pebble holder;
	
	public Point() {
		this.holder = null;
	}
	
	public Point(Pebble holder) {
		this.holder = holder;
	}
	
	public boolean isEmpty() {
		return (holder == null);
	}
	
	public Pebble getHolder() {
		return (holder);
	}

	public void setHolder(Pebble holder) {
		this.holder = holder;
	}
	
}
