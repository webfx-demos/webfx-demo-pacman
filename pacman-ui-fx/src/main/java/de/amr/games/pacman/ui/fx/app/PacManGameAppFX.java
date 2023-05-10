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
import de.amr.games.pacman.ui.fx.scene.GameSceneChoice;
import de.amr.games.pacman.ui.fx.scene2d.*;
import dev.webfx.kit.util.scene.DeviceSceneUtil;
import javafx.application.Application;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * This is the entry point of the Pac-Man and Ms. Pac-Man games.
 * 
 * <p>
 * The application is structured according to the MVC (model-view-controller) design pattern. The model layer consists
 * of the two game models <code> PacManGame</code> and <code> MsPacManGame</code>. The controller is a finite-state
 * machine which is triggered 60 times per second by the game loop. The user interface listens to game events sent from
 * the controller/model layer. The model and controller layers are decoupled from the user interface. This allows to
 * attach different user interfaces without having to change the controller or model.
 * 
 * <p>
 * As a proof of concept I implemented also a (simpler) Swing user interface, see repository
 * <a href="https://github.com/armin-reichert/pacman-ui-swing">Pac-Man Swing UI</a>.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppFX extends Application {

	private static List<GameSceneChoice> createPacManScenes(GameController gc) {
		return dev.webfx.platform.util.collection.Collections.listOf(
		//@formatter:off
			new GameSceneChoice(new BootScene(gc)),
			new GameSceneChoice(new PacManIntroScene(gc)),
			new GameSceneChoice(new PacManCreditScene(gc)),
			new GameSceneChoice(new PlayScene2D(gc)),
			new GameSceneChoice(new PacManCutscene1(gc)), 
			new GameSceneChoice(new PacManCutscene2(gc)),
			new GameSceneChoice(new PacManCutscene3(gc))
		//@formatter:on
		);
	}

	private static List<GameSceneChoice> createMsPacManScenes(GameController gc) {
		return dev.webfx.platform.util.collection.Collections.listOf(
		//@formatter:off
			new GameSceneChoice(new BootScene(gc)),
			new GameSceneChoice(new MsPacManIntroScene(gc)), 
			new GameSceneChoice(new MsPacManCreditScene(gc)),
			new GameSceneChoice(new PlayScene2D(gc)),
			new GameSceneChoice(new MsPacManIntermissionScene1(gc)), 
			new GameSceneChoice(new MsPacManIntermissionScene2(gc)),
			new GameSceneChoice(new MsPacManIntermissionScene3(gc))
		//@formatter:on
		);
	}

	public static void main(String[] args) {
		launch(args);
	}

	private GameUI gameUI;

	@Override
	public void init() throws Exception {
		AppRes.load();
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		var settings = new Settings(Collections.emptyMap()); // no settings used in this application
		var gameController = new GameController(GameVariant.MS_PACMAN);
		gameUI = new GameUI(primaryStage, settings, gameController, createMsPacManScenes(gameController),
				createPacManScenes(gameController));
		DeviceSceneUtil.onFontsAndImagesLoaded(() -> {} , AppRes.Manager.getLoadedImages());
	}

	@Override
	public void stop() throws Exception {
		gameUI.stop();
		Logger.info("Game stopped");
	}
}