package org.silcos.permainan.app;

import org.silcos.permainan.Game;

import javafx.application.Application;
import javafx.stage.Stage;

public class PermainanApp extends Application {

	Stage gameStage;
	Game gameInstances[];
	
	@Override
	public void start(Stage gameStage) throws Exception {
		this.gameStage = gameStage;
		
		gameInstances = new Game[]{ Game.doubleUserGame() };
		
		gameStage.setScene(BoardController.newBoardScene(gameInstances[0]));
		gameStage.setHeight(600);
		gameStage.setWidth(600);
		gameStage.setTitle("Permanin/Surakurta Game");
		gameStage.show();
		
		gameInstances[0].placeAllPebbles();
	}

	public static void main(String[] args) {
		launch(args);
	}
	
}
