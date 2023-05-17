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
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.util.Ufx;
import dev.webfx.kit.util.scene.DeviceSceneUtil;
import dev.webfx.platform.useragent.UserAgent;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.controller.GameState.INTRO;

/**
 * @author Armin Reichert
 */
public class GameApp extends Application {

	private static KeyCodeCombination just(KeyCode code) {
		return new KeyCodeCombination(code);
	}

	private static KeyCodeCombination altShift(KeyCode code) {
		return new KeyCodeCombination(code, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN);
	}

	// Game control
	public static final KeyCodeCombination KEY_HELP                = just(KeyCode.H);
	public static final KeyCodeCombination KEY_BOOT                = just(KeyCode.F3);
	public static final KeyCodeCombination KEY_QUIT                = just(KeyCode.Q);
	public static final KeyCodeCombination KEY_CHANGE_GAME_VARIANT = just(KeyCode.V);
	public static final KeyCodeCombination KEY_ADD_CREDIT          = just(KeyCode.DIGIT5);
	public static final KeyCodeCombination KEY_ADD_CREDIT_NUMPAD   = just(KeyCode.NUMPAD5);
	public static final KeyCodeCombination KEY_START_GAME          = just(KeyCode.DIGIT1);
	public static final KeyCodeCombination KEY_START_GAME_NUMPAD   = just(KeyCode.NUMPAD1);

	// Pac-Man control
	public static final KeyCodeCombination KEY_AUTOPILOT           = altShift(KeyCode.A);
	public static final KeyCodeCombination KEY_IMMUNITY            = altShift(KeyCode.I);

	// Cheats (only available while game is playing)
	public static final KeyCodeCombination KEY_CHEAT_EAT_ALL       = altShift(KeyCode.E);
	public static final KeyCodeCombination KEY_CHEAT_ADD_LIVES     = altShift(KeyCode.L);
	public static final KeyCodeCombination KEY_CHEAT_NEXT_LEVEL    = altShift(KeyCode.N);
	public static final KeyCodeCombination KEY_CHEAT_KILL_GHOSTS   = altShift(KeyCode.X);

	// Test modes (available from intro scenes)q
	public static final KeyCodeCombination KEY_TEST_LEVELS         = altShift(KeyCode.T);
	public static final KeyCodeCombination KEY_TEST_CUTSCENES      = altShift(KeyCode.C);

	// Game loop control keys
	public static final KeyCodeCombination KEY_PAUSE               = just(KeyCode.P);
	public static final KeyCodeCombination KEY_SINGLE_STEP         = just(KeyCode.SPACE);
	public static final KeyCodeCombination KEY_TEN_STEPS           = just(KeyCode.T);
	public static final KeyCodeCombination KEY_SIMULATION_FASTER   = altShift(KeyCode.F);
	public static final KeyCodeCombination KEY_SIMULATION_SLOWER   = altShift(KeyCode.S);
	public static final KeyCodeCombination KEY_SIMULATION_NORMAL   = altShift(KeyCode.DIGIT0);

	public static final BooleanProperty simulationPausedPy = new SimpleBooleanProperty(false);
	public static final IntegerProperty simulationSpeedPy  = new SimpleIntegerProperty(60);

	public static GameApp app;
	public static GameAssets assets;
	public static GameUI ui;

	private static Optional<GameVariant> getGameVariantFromQuery() {
		if (UserAgent.isBrowser()) {
			var query = dev.webfx.platform.windowlocation.WindowLocation.getQueryString();
			//var query = "x=1&y=2&game=mspacman&z=3";
			Map<String, String> parameterMap = Collections.emptyMap();
			if (query != null && !query.isEmpty()) {
				parameterMap = parseQueryParameters(query);
			}
			if (parameterMap.containsKey("game")) {
				var value = parameterMap.get("game");
				if ("pacman".equals(value)) {
					return Optional.of(GameVariant.PACMAN);
				} else if ("mspacman".equals(value)) {
					return Optional.of(GameVariant.MS_PACMAN);
				}
			}
		}
		return Optional.empty();
	}

	// Quick-and-dirty implementation
	public static Map<String, String> parseQueryParameters(String query) {
		var map = new HashMap<String, String>();
		String[] params = query.split("&");
		for (var p : params) {
			String[] keyValue = p.split("=", 2);
			String key = "";
			String value = "";
			if (keyValue.length >= 1) {
				//TODO URLDecoder not available in GWT?
				// key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
				key = keyValue[0];
			}
			if (keyValue.length >= 2) {
				//TODO URLDecoder not available in GWT?
				// value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
				value = keyValue[1];
			}
			if (!key.isEmpty()) {
				map.put(key, value);
			}
		}
		return map;
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() {
		app = this;
		assets = new GameAssets();
	}

	@Override
	public void start(Stage primaryStage) {
		var settings = new Settings(Collections.emptyMap()); // no command-line args used
		var gameVariant = getGameVariantFromQuery().orElse(GameVariant.MS_PACMAN);
		var gameController = new GameController(gameVariant);
		ui = new GameUI(primaryStage, settings, gameController);
		DeviceSceneUtil.onFontsAndImagesLoaded(() -> {} , GameAssets.Manager.getLoadedImages());
	}

	@Override
	public void stop() {
		ui.clock().stop();
	}

	// --- Actions ---

	public void showHelp() {
		ui.showHelp();
	}

	public void playHelpVoiceMessageAfterSeconds(int seconds) {
		Ufx.afterSeconds(seconds, () -> ui.playVoiceMessage(assets.voiceHelp)).play();
	}

	public void showFlashMessage(String message, Object... args) {
		showFlashMessageSeconds(1, message, args);
	}

	public void showFlashMessageSeconds(double seconds, String message, Object... args) {
		ui.flashMessageView().showMessage(message/* String.format(message, args) */, seconds);
	}

	public void startGame() {
		if (ui.game().hasCredit()) {
			ui.stopVoiceMessage();
			ui.gameController().startPlaying();
		}
	}

	public void startCutscenesTest() {
		ui.gameController().startCutscenesTest();
		showFlashMessage("Cut scenes");
	}

	public void restartIntro() {
		ui.currentGameScene().end();
		GameEvents.setSoundEventsEnabled(true);
		if (ui.game().isPlaying()) {
			ui.game().changeCredit(-1);
		}
		ui.gameController().restart(INTRO);
	}

	public void reboot() {
		if (ui.currentGameScene() != null) {
			ui.currentGameScene().end();
		}
		//playHelpVoiceMessageAfterSeconds(4);
		ui.gameController().restart(GameState.BOOT);
	}

	public void addCredit() {
		GameEvents.setSoundEventsEnabled(true);
		ui.gameController().addCredit();
	}

	public void togglePaused() {
		Ufx.toggle(simulationPausedPy);
		// TODO mute and unmute?
		if (simulationPausedPy.get()) {
			assets.gameSounds.get(ui.game().variant()).stopAll();
		}
	}

	public void oneSimulationStep() {
		if (simulationPausedPy.get()) {
			ui.clock().executeSingleStep(true);
		}
	}

	public void tenSimulationSteps() {
		if (simulationPausedPy.get()) {
			ui.clock().executeSteps(10, true);
		}
	}

	public void changeSimulationSpeed(int delta) {
		int newFramerate = ui.clock().targetFrameratePy.get() + delta;
		if (newFramerate > 0 && newFramerate < 120) {
			simulationSpeedPy.set(newFramerate);
			showFlashMessageSeconds(0.75, newFramerate + "Hz");
		}
	}

	public void resetSimulationSpeed() {
		simulationSpeedPy.set(GameModel.FPS);
		showFlashMessageSeconds(0.75, simulationSpeedPy.get() + "Hz");
	}

	public void selectNextGameVariant() {
		var gameVariant = ui.game().variant().next();
		ui.gameController().selectGameVariant(gameVariant);
		playHelpVoiceMessageAfterSeconds(4);
	}

	public void toggleAutopilot() {
		ui.gameController().toggleAutoControlled();
		var autoPilotOn = ui.gameController().isAutoControlled();
		String message = assets.message(autoPilotOn ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		ui.playVoiceMessage(autoPilotOn ? assets.voiceAutopilotOn : assets.voiceAutoPilotOff);
	}

	public void toggleImmunity() {
		ui.game().setImmune(!ui.game().isImmune());
		var immune = ui.game().isImmune();
		String message = assets.message(immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		ui.playVoiceMessage(immune ? assets.voiceImmunityOn : assets.voiceImmunityOff);
	}

	public void startLevelTestMode() {
		if (ui.gameController().state() == GameState.INTRO) {
			ui.gameController().restart(GameState.LEVEL_TEST);
			showFlashMessage("Level TEST MODE");
		}
	}

	public void cheatAddLives(int numLives) {
		if (ui.game().isPlaying()) {
			ui.game().setLives(numLives + ui.game().lives());
			showFlashMessage(assets.message("cheat_add_lives", ui.game().lives()));
		}
	}

	public void cheatEatAllPellets() {
		if (ui.game().isPlaying()) {
			ui.gameController().cheatEatAllPellets();
		}
	}

	public void cheatEnterNextLevel() {
		ui.gameController().cheatEnterNextLevel();
	}

	public void cheatKillAllEatableGhosts() {
		ui.gameController().cheatKillAllEatableGhosts();
	}
}