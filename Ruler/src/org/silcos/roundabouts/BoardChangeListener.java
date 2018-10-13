package org.silcos.roundabouts;

import java.util.EventListener;

public interface BoardChangeListener extends EventListener {
	
	void handle(BoardChangeEvent e);
	
}
