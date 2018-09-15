package org.silcos.permainan.app;

import java.io.IOException;
import java.util.Arrays;

import org.silcos.permainan.Board;
import org.silcos.permainan.BoardChangeEvent;
import org.silcos.permainan.Game;
import org.silcos.permainan.Pebble;
import org.silcos.permanin.control.BoardInput;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.VLineTo;

public final class BoardController {
	
	private Game gameInstance;
	
	@FXML
	private Pane userGrid;
	private BoardInput[][] inputGrid;
	
	private Path gridLoops[];
	
	@FXML
	private ScrollPane gameView;
	
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
		
		userGrid.getChildren().removeAll(gridLoops);
		
		Color c[]= { Color.GREEN, Color.AQUA, Color.YELLOW	};
		
		for(int lidx = 0; lidx < gridLoops.length; lidx++) {
			gridLoops[lidx] = newGridLoop((2 - lidx) * inputWidth, (2 * lidx + 1) * inputWidth, c[lidx]);
			gridLoops[lidx].relocate(25 + inputWidth * lidx, 25 + inputWidth * lidx);
		}
		
		userGrid.getChildren().addAll(0, Arrays.asList(gridLoops));
		
		for(int row = 0; row < Board.linearSize; row++) {
			for(int column = 0; column < Board.linearSize; column++) {
				BoardInput inputElement = inputGrid[row][column];
				
				inputElement.setPrefSize(inputWidth, inputWidth);
				inputElement.relocate(25 + inputWidth * (column + 1.5), 25 + inputWidth * (row + 1.5));
			}
		}
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
		gridLoops[1] = newGridLoop(50, 150, Color.AQUA);
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
		
		return (new Scene(root));
	}

}
