package org.silcos.permainan;

import java.util.EventListener;

public interface BoardChangeListener extends EventListener {
	
	void handle(BoardChangeEvent e);
	
}
