/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import dev.webfx.kit.util.scene.DeviceSceneUtil;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;

/**
 * @author Armin Reichert
 */
public class GameApp extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	//@formatter:off
	public static final BooleanProperty simulationPausedPy = new SimpleBooleanProperty(false);
	public static final IntegerProperty simulationSpeedPy  = new SimpleIntegerProperty(60);
	//@formatter:on

	public static GameActions actions;

	private GameUI gameUI;

	@Override
	public void init() throws Exception {
		GameAssets.load();
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		var settings = new Settings(Collections.emptyMap()); // no command-line args used
		var gameController = new GameController(GameVariant.MS_PACMAN);
		gameUI = new GameUI(primaryStage, settings, gameController);
		GameApp.actions = new GameActions(gameUI);
		DeviceSceneUtil.onFontsAndImagesLoaded(() -> {} , GameAssets.Manager.getLoadedImages());
	}

	@Override
	public void stop() throws Exception {
		gameUI.clock().stop();
	}
}