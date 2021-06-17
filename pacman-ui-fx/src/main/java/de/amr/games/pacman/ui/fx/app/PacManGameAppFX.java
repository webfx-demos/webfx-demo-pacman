/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import static de.amr.games.pacman.lib.Logging.log;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop.GameLoopTask;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The Pac-Man / Ms. Pac-Man game running in a JavaFX UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppFX extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	private static final List<String> PARAMETER_NAMES = Arrays.asList("-height", "-mspacman", "-pacman");

	private double height = 576;
	private GameVariant gameVariant = GameVariant.PACMAN;
	private PacManGameController gameController;

	private void parseParameters(List<String> params) {
		int i = -1;
		while (++i < params.size()) {
			if ("-pacman".equals(params.get(i))) {
				gameVariant = GameVariant.PACMAN;
				continue;
			}
			if ("-mspacman".equals(params.get(i))) {
				gameVariant = GameVariant.MS_PACMAN;
				continue;
			}
			if ("-height".equals(params.get(i))) {
				++i;
				if (i == params.size() || PARAMETER_NAMES.contains(params.get(i))) {
					log("!!! Error parsing parameters: missing height value.");
					break;
				}
				try {
					height = Double.parseDouble(params.get(i));
				} catch (NumberFormatException x) {
					log("!!! Error parsing parameters: '%s' is no legal height value.", params.get(i));
				}
				continue;
			}
			log("!!! Error parsing parameters: Found garbage '%s'", params.get(i));
		}
	}

	@Override
	public void init() throws Exception {
		parseParameters(getParameters().getRaw());
		gameController = new PacManGameController();
		gameController.selectGameVariant(gameVariant);
	}

	@Override
	public void start(Stage stage) throws IOException {
		PacManGameUI ui = new PacManGameUI_JavaFX(stage, gameController, height);
		gameController.setUI(ui);

		GameLoop gameLoop = new GameLoop( //
				new GameLoopTask("Controller Step", gameController::step), //
				new GameLoopTask("UI Update", ui::update));
		Env.$totalTicks.bind(gameLoop.$totalTicks);
		Env.$fps.bind(gameLoop.$fps);
		gameLoop.start();
	}
}