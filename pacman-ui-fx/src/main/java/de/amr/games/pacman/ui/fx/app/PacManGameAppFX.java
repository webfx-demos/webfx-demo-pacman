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
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;

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

	//@formatter:off
	public static final ObjectProperty<Color> mainSceneBgColorPy       = new SimpleObjectProperty<>(Color.gray(0.2));
	public static final BooleanProperty       showDebugInfoPy          = new SimpleBooleanProperty(false);
	public static final BooleanProperty       simulationPausedPy       = new SimpleBooleanProperty(false);
	public static final IntegerProperty       simulationStepsPy        = new SimpleIntegerProperty(1);
	public static final IntegerProperty       simulationSpeedPy        = new SimpleIntegerProperty(60);
	public static final BooleanProperty       simulationTimeMeasuredPy = new SimpleBooleanProperty(false);
	//@formatter:on


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
		gameUI = new GameUI(primaryStage, settings, gameController);
		DeviceSceneUtil.onFontsAndImagesLoaded(() -> {} , AppRes.Manager.getLoadedImages());
	}

	@Override
	public void stop() throws Exception {
		gameUI.stop();
	}
}