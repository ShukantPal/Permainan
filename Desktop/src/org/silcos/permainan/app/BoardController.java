package org.silcos.permainan.app;

import java.io.IOException;
import java.util.Arrays;

import org.silcos.permainan.Board;
import org.silcos.permainan.BoardChangeEvent;
import org.silcos.permainan.Connector;
import org.silcos.permainan.ConnectorOrientation;
import org.silcos.permainan.Game;
import org.silcos.permainan.Pebble;
import org.silcos.permanin.control.BoardInput;

import javafx.animation.PathTransition;
import javafx.animation.PathTransition.OrientationType;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.VLineTo;
import javafx.util.Duration;

public final class BoardController {
	
	private Game gameInstance;
	
	@FXML
	private Pane userGrid;
	private BoardInput[][] inputGrid;
	
	private Path gridLoops[];
	
	@FXML
	private ScrollPane gameView;
	
	private volatile double pwidth;
	
	private int[] pointAlmostAt(double pixelRow, double pixelHeight) {
		int[] point = new int[2];
		
		point[0] = (int) ((pixelRow) / pwidth);
		point[1] = (int) ((pixelHeight) / pwidth);
		
		return (point);
	}
	
	/**
	 * <p>
	 * Creates an <tt>Arc</tt>, forming a path so that the user gets to see
	 * the movement of the pebble (-holding button) around the external
	 * loop (stored via <tt>Connector</tt>), emerging from the given edge
	 * point coordinates.
	 * 
	 * <p>
	 * All calculations are based on the fact that the loop is centered at
	 * the nearest corner for the given point.
	 * 
	 * @param row
	 * @param column
	 * @return
	 */
	private Arc pointLoopPath(int row, int column) {
		double arcCenter = (double) Board
				.nearestIntegralCornerDistance(row, column);
		double arcCenterX, arcCenterY;
		
		if(row == 0 || row == 5) {
			arcCenterX = (arcCenter + 0.5) * pwidth;
			arcCenterY = 0.5 * pwidth;
		} else {
			arcCenterX = 0.5 * pwidth;
			arcCenterY = (arcCenter + 0.5) * pwidth;
		}
		
		arcCenter = Math.abs(arcCenter);
		
		Connector boardLoop = gameInstance.externalConnectorAt(row, column);
		ConnectorOrientation startForm;
		double startAngle, arcLength = 270;
		
		if(boardLoop.row0() == row) { // if row is equal, implies column too
			startForm = boardLoop.link0();
		} else {
			startForm = boardLoop.link1();
		}
		
		switch(startForm) {
		case UP:
			if(arcCenterX < 0) {
				startAngle = 0;
			} else {
				startAngle = 180;
				arcLength *= -1;
			}
			break;
		case DOWN:
			if(arcCenterX < 0) {
				startAngle = 0;
				arcLength *= -1;
			} else {
				startAngle = 180;
			}
			break;
		case LEFT:
			if(arcCenterY > 0) {
				startAngle = 90;
			} else {
				startAngle = 270;
				arcLength *= -1;
			}
			break;
		case RIGHT:
			if(arcCenterY > 0) {
				startAngle = 90;
				arcLength *= -1;
			} else {
				startAngle = 270;
			}
			break;
		default:
			arcLength = 0;
			startAngle = 0;
			break;
		}
		
		return (new Arc(arcCenterX, arcCenterY + 10, arcCenter * pwidth,
				arcCenter * pwidth, startAngle, arcLength));
	}
	
	private EventHandler<DragEvent> pieceVerifier = new EventHandler<DragEvent>() {

		@SuppressWarnings("unused")
		@Override
		public void handle(DragEvent e) {
			if(e.getDragboard().hasString() &&
					BoardInput.isPebbleData(e.getDragboard().getString())) {
				int[] sourceInput = BoardInput.toCoordinates(
						e.getDragboard().getString());
				int[] destinationInput =
						new int[2];
				Connector targetLoop = gameInstance
						.externalConnectorAt(sourceInput[0], sourceInput[1]);
				
				if(sourceInput[0] == targetLoop.row0()) {
					destinationInput[0] = targetLoop.row1();
					destinationInput[1] = targetLoop.column1();
				} else {
					destinationInput[0] = targetLoop.row0();
					destinationInput[1] = targetLoop.column0();
				}
				
				Pebble tPebbleState = gameInstance.pebbleAt(
						destinationInput[0], destinationInput[1]);
				
				if(tPebbleState != null && 
						tPebbleState.owner() == gameInstance.pebbleAt(
						sourceInput[0], sourceInput[1]).owner()) {
					return;
				}
				
				int[] point = pointAlmostAt(e.getY(), e.getX());
				
				// TODO: Accept only if in correct orientation for target
				
				e.acceptTransferModes(TransferMode.ANY);
			}
			
			e.consume();
		}
		
	};
	
	/**
	 * <p>
	 * Accepts a piece move on the <tt>userGrid</tt> pane, that requires an
	 * animation through a loop. On verifying the request, it instantiates
	 * a <tt>PathTransition</tt> for the pebble-holding <tt>ButtonInput</tt>
	 * control, and sets it to go through the loop.
	 * 
	 * <p>
	 * <tt>gameInstance</tt> is also notified about the looping movement and
	 * once the pebble completes the circular loop, the button is placed back
	 * to its original position, but the destination gets hold of the <tt>
	 * Pebble</tt> object being actually moved (in the game). <tt>gameInstance
	 * </tt> may continue the animation by moving the pebble horizontally and
	 * vertically (<tt>BoardController</tt> will be notified through the
	 * <tt>BoardChangeListener</tt> interface).
	 */
	private EventHandler<DragEvent> pieceAcceptor = new EventHandler<DragEvent>() {
		
		@Override
		public void handle(DragEvent e) {
			if(e.getDragboard().hasString()) {
				String pebbleData = e.getDragboard().getString();
				int[] pebbleCoordinates = BoardInput.toCoordinates(pebbleData);
								
				Connector loopData = gameInstance.externalConnectorAt(
						pebbleCoordinates[0], pebbleCoordinates[1]);
				
				if(loopData != null) {
					PathTransition loopTransition = new PathTransition();
					Arc cloop = pointLoopPath(pebbleCoordinates[0], pebbleCoordinates[1]);
					
					loopTransition.setDuration(Duration.millis(500));
					loopTransition.setPath(cloop);
					loopTransition.setNode(inputGrid[pebbleCoordinates[0]][pebbleCoordinates[1]]);
					loopTransition.setCycleCount(1);
					loopTransition.setOrientation(OrientationType.NONE);

					loopTransition.setOnFinished(new EventHandler<ActionEvent>(){
						@Override
						public void handle(ActionEvent e) {
							if(e.getSource() instanceof PathTransition) {
								PathTransition src = (PathTransition) e.getSource();
								BoardInput node = (BoardInput) src.getNode();
								
								loopTransition.stop();
								
								node.relocate(25 + 5 + (node.column() + 1.5) * pwidth, 
										25 + 5 + (node.row() + 1.5) * pwidth);
								node.setTranslateX(0);
								node.setTranslateY(0);
								node.rotationAxisProperty().setValue(Point3D.ZERO);
								
								gameInstance.notifyLoopInput(node.row(), node.column());
								e.consume();
							}
						}
					});
					
					loopTransition.play();
				} else {
					System.err.println("Warning *:* Connector not found");
				}
			}
	
			e.consume();
		}
	};
	
	private BoardController(Game gameInstance) {
		this.gameInstance = gameInstance;
		this.inputGrid = new BoardInput[6][6];
		this.gridLoops = new Path[3];
		
		gameInstance.addBoardChangeListener(
					(BoardChangeEvent e) -> {
						switch(e.changeType()) {
						case PLACE_PEBBLE:
							int row = (Integer) e.getUserData("targetRow");
							int column = (Integer) e.getUserData("targetColumn");
							inputGrid[row][column].putPebble(
									(Pebble) e.getUserData("target"));
							break;
						case MOVE_PEBBLE:
						case CAPTURE_PEBBLE:
							int srcRow = (Integer) e.getUserData("sourceRow");
							int srcColumn = (Integer) e.getUserData("sourceColumn");
							int tarRow = (Integer) e.getUserData("targetRow");
							int tarColumn = (Integer) e.getUserData("targetColumn");
							Pebble peb = (Pebble) e.getUserData("target");
							
							inputGrid[srcRow][srcColumn].putPebble(null);
							inputGrid[tarRow][tarColumn].putPebble(peb);
							
						default:
							break;
						}
					}
				);
	}
	
	private void adjustInputs(double width, double height) {
		double inputWidth = (width - 50) / 10;// width / 6;
		pwidth = inputWidth;
		
		userGrid.getChildren().removeAll(gridLoops);
		
		Color c[]= { Color.GREEN, Color.DEEPSKYBLUE, Color.YELLOW	};
		
		for(int lidx = 0; lidx < gridLoops.length; lidx++) {
			gridLoops[lidx] = newGridLoop((2 - lidx) * inputWidth, (2 * lidx + 1) * inputWidth, c[lidx]);
			gridLoops[lidx].relocate(25 + inputWidth * lidx, 25 + inputWidth * lidx);
		}
		
		userGrid.getChildren().addAll(0, Arrays.asList(gridLoops));
		
		for(int row = 0; row < Board.linearSize; row++) {
			for(int column = 0; column < Board.linearSize; column++) {
				BoardInput inputElement = inputGrid[row][column];
				
				inputElement.setPrefSize(inputWidth, inputWidth);
				inputElement.relocate(25 + 5 + inputWidth * (column + 1.5),
						25 + 5 + inputWidth * (row + 1.5));
			}
		}
		
		System.out.println("adjusted inputs : " + inputWidth);
	}
	
	private Path newGridLoop(double loopRadius, double middleGap, Color stroke) {
		final double lineLength = 2 * loopRadius + middleGap;
		
		Path loop = new Path(
				new MoveTo(loopRadius, 2 * loopRadius),
				new ArcTo(loopRadius, loopRadius, 0, loopRadius, -loopRadius, true, true),
				new VLineTo(lineLength),
				new ArcTo(loopRadius, loopRadius, 0, -loopRadius, -loopRadius, true, true),
				new HLineTo(lineLength),
				new ArcTo(loopRadius, loopRadius, 0, -loopRadius, loopRadius, true, true),
				new VLineTo(-lineLength),
				new ArcTo(loopRadius, loopRadius, 0, loopRadius, loopRadius, true, true),
				new ClosePath()
			);
		
		loop.setStroke(stroke);
		loop.setStrokeWidth(10);
		
		loop.getElements().forEach(
					(PathElement element) -> {
						if(!(element instanceof MoveTo || element instanceof ClosePath))
							element.setAbsolute(false);
					}
				);
		
		return (loop);
	}
	
	/**
	 * Initializes the game-grid with all the controls required to play
	 * with it.
	 */
	private void fillUserGrid() {
		gridLoops[0] = newGridLoop(100, 50, Color.GREEN);
		gridLoops[1] = newGridLoop(50, 150, Color.BLUE);
		gridLoops[2] = newGridLoop(0, 250, Color.YELLOW);
		
		gridLoops[1].relocate(50, 50);
		gridLoops[2].relocate(100, 100);
		
		for(Path loop : gridLoops) {
			userGrid.getChildren().add(loop);
		}
		
		for(int row = 0; row < Board.linearSize; row++) {
			for(int column = 0; column < Board.linearSize; column++) {
				BoardInput gameInput = new BoardInput(row, column, gameInstance);
				inputGrid[row][column] = gameInput;
				gameInput.setMaxWidth(Double.MAX_VALUE);
				gameInput.setMaxHeight(Double.MAX_VALUE);
				gameInput.setPrefWidth(50);
				gameInput.setPrefHeight(50);
				
				gameInput.relocate(100+50 * (double) column,100+ 50 * (double) row);
				userGrid.getChildren().add(gameInput);
			}
		}
		
		pwidth = 50;
		
		gameView.widthProperty().addListener(
					(ObservableValue<? extends Number> num, Number org, Number finalWidth) -> {
						adjustInputs(finalWidth.doubleValue(),
								userGrid.heightProperty().doubleValue());
					}
				);
	}
	
	/**
	 * Instantiates a new and setup <tt>BoardController</tt> attached with
	 * the root layout of the board-game window. A scene holding the layout
	 * is returned, and can be displayed in a stage.
	 */
	public static Scene newBoardScene(Game gameInstance) {
		BoardController ctlObj = new BoardController(gameInstance);
		FXMLLoader guiReader = new FXMLLoader();
		guiReader.setLocation(BoardController.class.getResource("BoardWindow.fxml"));
		guiReader.setController(ctlObj);

		BorderPane root;
		
		try {
			root = (BorderPane) guiReader.load();
		} catch(IOException e) {
			throw new RuntimeException("The application could not load the window"
					+ " due to build-time issues!");
		}
		
		ctlObj.fillUserGrid();
		ctlObj.userGrid.setOnDragOver(ctlObj.pieceVerifier);
		ctlObj.userGrid.setOnDragDropped(ctlObj.pieceAcceptor);
		
		return (new Scene(root));
	}

}
