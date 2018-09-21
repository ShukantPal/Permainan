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
 * Self-adjusting button for Permainan that has two perpendicular lines in its
 * background to form the game's grid. It allows the user to drag-and-drop to
 * form his/her move.
 * 
 * @author Shukant Pal
 */
public class BoardInput extends Button {
	
	private static final Paint starterPlayerColor = Color.BLACK;
	private static final Paint otherPlayerColor = Color.RED;
	
	@SuppressWarnings("unused")
	private static final Insets pebblePos = new Insets(10, 10, 10, 10);
	
	private BackgroundFill pebbleFill;
	
	private int row;
	private int column;
	private Game gameInstance;
	
	public static boolean isPebbleData(String pebbleData) {
		return (pebbleData.startsWith("Permanin.move[") &&
				pebbleData.endsWith("]"));
	}
	
	public static int[] toCoordinates(String pebbleData) {
		int[] point = new int[2];
		
		int commaIndex = pebbleData.indexOf(',');
		point[0] = Integer.parseInt(
				pebbleData.substring(14, commaIndex));
		point[1] = Integer.parseInt(
				pebbleData.substring(commaIndex + 1, pebbleData.length() - 1));
		
		return (point);
	}
	
	/**
	 * Initiates the drag-and-drop wanted by the user, so that he/she can move
	 * a pebble on the board. The clip-board is attached with a string holding
	 * the coordinates of the source button-input with the format - <code>
	 * Permanin.move["row","column"]</code>
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
			if(e.getDragboard().hasString() && pebbleFill == null &&
					isPebbleData(e.getDragboard().getString())) {
				e.acceptTransferModes(TransferMode.ANY);
			}
			
			e.consume();
		}
		
	};
	
	private EventHandler<DragEvent> pieceAcceptor = new EventHandler<DragEvent>() {
		
		@Override
		public void handle(DragEvent e) {
			if(e.getDragboard().hasString()) {
				String pebbleData = e.getDragboard().getString();
				
				if(isPebbleData(pebbleData)) {
					int[] coordinates = toCoordinates(pebbleData);
					gameInstance.notifyInput(coordinates[0], coordinates[1], row, column);
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
	
	public int row() {
		return (row);
	}
	
	public int column() {
		return (column);
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
