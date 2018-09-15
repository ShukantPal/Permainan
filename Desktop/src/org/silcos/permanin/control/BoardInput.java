package org.silcos.permanin.control;

import org.silcos.permainan.Game;
import org.silcos.permainan.Pebble;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Self-adjusting button for Permanin that has two perpendicular lines in its
 * background to form the game's grid. It allows the user to drag-and-drop to
 * form his/her move.
 * 
 * @author Shukant Pal
 */
public class BoardInput extends Button {
	
	private static final Paint starterPlayerColor = Color.RED;
	private static final Paint otherPlayerColor = Color.BLUE;
	
	private static final Insets pebblePos = new Insets(10, 10, 10, 10);
	
	private BackgroundFill pebbleFill;
	
	private int row;
	private int column;
	private Game gameInstance;
	
	/**
	 * Handles the event fired when the user initiates a drag on this
	 * board input, which in turn allows him/her to place a move.
	 */
	private EventHandler<MouseEvent> pieceDragDetector = new EventHandler<MouseEvent>() {
		
		@Override
		public void handle(MouseEvent e) {
			Dragboard pieceDragHolder = startDragAndDrop(TransferMode.ANY);
			
			ClipboardContent pieceClipper = new ClipboardContent();
			pieceClipper.putString("Permanin.move[" + row + "," + column + "]");
			
			pieceDragHolder.setContent(pieceClipper);
			e.consume();
		}
		
	};
	
	/**
	 * 
	 */
	private EventHandler<DragEvent> pieceVerifier = new EventHandler<DragEvent>() {

		@Override
		public void handle(DragEvent e) {
			if(e.getDragboard().hasString() && pebbleFill == null) {
				e.acceptTransferModes(TransferMode.ANY);
			}
			
			e.consume();
		}
		
	};
	
	private EventHandler<DragEvent> pieceAcceptor = new EventHandler<DragEvent>() {
		
		@Override
		public void handle(DragEvent e) {
			if(e.getDragboard().hasString()) {
				String pieceData = e.getDragboard().getString();
				
				if(pieceData.startsWith("Permanin.move[")
						&& pieceData.endsWith("]")) {
					int commaIndex = pieceData.indexOf(',');
					
					int srcRow = Integer.parseInt(
							pieceData.substring(14, commaIndex));
					int srcColumn = Integer.parseInt(
							pieceData.substring(commaIndex + 1, pieceData.length() - 1));

					gameInstance.notifyInput(srcRow, srcColumn, row, column);
				}
			}
	
			e.consume();
		}
		
	};
	
	/**
	 * Constructs a new board-input button for Permanin move input,
	 * that responds to changes in size, color profile, and focus.
	 * 
	 * @param gameInstance - the game controller to inform when the
	 * 						human-user provides a move
	 */
	public BoardInput(int row, int column, Game gameInstance) {
		super();
		this.row = row;
		this.column = column;
		this.gameInstance = gameInstance;
		
		setOnDragDetected(pieceDragDetector);
		setOnDragOver(pieceVerifier);
		setOnDragDropped(pieceAcceptor);
		setBackground(Background.EMPTY);
	}
	
	public void putPebble(Pebble pebble) {
		if(pebble == null) {
			pebbleFill = null;
		} else if(pebble.owner() == gameInstance.getStarterPlayer()) {
			pebbleFill = new BackgroundFill(starterPlayerColor,
					new CornerRadii(Math.max(0, getWidth() - 10)), pebblePos);
		} else {
			pebbleFill = new BackgroundFill(otherPlayerColor,
					new CornerRadii(Math.max(0, widthProperty().doubleValue() - 10)), pebblePos);
		}
		
		setBackground(new Background(pebbleFill));
	}
	
}
