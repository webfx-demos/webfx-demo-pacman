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

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.media.AudioClip;

import static de.amr.games.pacman.controller.GameState.INTRO;

/**
 * @author Armin Reichert
 */
public class GameActions {

	private static KeyCodeCombination just(KeyCode code) {
		return new KeyCodeCombination(code);
	}

	private static KeyCodeCombination alt(KeyCode code) {
		return new KeyCodeCombination(code, KeyCombination.ALT_DOWN);
	}

	private static KeyCodeCombination shift(KeyCode code) {
		return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN);
	}

	private static KeyCodeCombination altShift(KeyCode code) {
		return new KeyCodeCombination(code, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN);
	}

	public static final KeyCodeCombination HELP = just(KeyCode.H);
	public static final KeyCodeCombination AUTOPILOT = altShift(KeyCode.A);
	public static final KeyCodeCombination BOOT = just(KeyCode.Z); // TODO does not work with any combination?
	public static final KeyCodeCombination CHEAT_EAT_ALL = altShift(KeyCode.E);
	public static final KeyCodeCombination IMMUNITY = altShift(KeyCode.I);
	public static final KeyCodeCombination CHEAT_ADD_LIVES = altShift(KeyCode.L);
	public static final KeyCodeCombination CHEAT_NEXT_LEVEL = altShift(KeyCode.N);
	public static final KeyCodeCombination CHEAT_KILL_GHOSTS = altShift(KeyCode.X);
	public static final KeyCodeCombination TEST_LEVELS = altShift(KeyCode.T);
	public static final KeyCodeCombination PLAY_CUTSCENES = altShift(KeyCode.C);

	public static final KeyCodeCombination PAUSE = just(KeyCode.P);
	public static final KeyCodeCombination PAUSE_STEP = shift(KeyCode.P);
	public static final KeyCodeCombination SINGLE_STEP = just(KeyCode.SPACE);
	public static final KeyCodeCombination TEN_STEPS = shift(KeyCode.SPACE);
	public static final KeyCodeCombination SIMULATION_FASTER = altShift(KeyCode.F);
	public static final KeyCodeCombination SIMULATION_SLOWER = altShift(KeyCode.G);
	public static final KeyCodeCombination SIMULATION_NORMAL = altShift(KeyCode.DIGIT0);

	public static final KeyCodeCombination QUIT = just(KeyCode.Q);
	public static final KeyCodeCombination SELECT_VARIANT = just(KeyCode.V);

	public static final KeyCodeCombination START_GAME = just(KeyCode.DIGIT1);
	public static final KeyCodeCombination ADD_CREDIT = just(KeyCode.DIGIT5);

	private static GameUI ui;
	private static AudioClip currentVoiceMessage; //TODO move elsewhere

	public static void init(GameUI ui) {
		GameActions.ui = ui;
	}

	public static void showHelp() {
		ui.showHelp();
	}

	public static void playHelpVoiceMessageAfterSeconds(int seconds) {
		Ufx.afterSeconds(seconds, () -> playVoiceMessage(GameAssets.Sounds.VOICE_HELP)).play();
	}

	public static void playVoiceMessage(AudioClip voiceMessage) {
		if (currentVoiceMessage != null && currentVoiceMessage.isPlaying()) {
			return; // don't interrupt voice message still playing, maybe enqueue?
		}
		currentVoiceMessage = voiceMessage;
		currentVoiceMessage.play();
	}

	public static void stopVoiceMessage() {
		if (currentVoiceMessage != null) {
			currentVoiceMessage.stop();
		}
	}

	public static void showFlashMessage(String message, Object... args) {
		showFlashMessageSeconds(1, message, args);
	}

	public static void showFlashMessageSeconds(double seconds, String message, Object... args) {
		ui.flashMessageView().showMessage(message/* String.format(message, args) */, seconds);
	}

	public static void startGame() {
		if (ui.game().hasCredit()) {
			stopVoiceMessage();
			ui.gameController().startPlaying();
		}
	}

	public static void startCutscenesTest() {
		ui.gameController().startCutscenesTest();
		showFlashMessage("Cut scenes");
	}

	public static void restartIntro() {
		ui.currentGameScene().end();
		GameEvents.setSoundEventsEnabled(true);
		if (ui.game().isPlaying()) {
			ui.game().changeCredit(-1);
		}
		ui.gameController().restart(INTRO);
	}

	public static void reboot() {
		if (ui.currentGameScene() != null) {
			ui.currentGameScene().end();
		}
		//playHelpVoiceMessageAfterSeconds(4);
		ui.gameController().restart(GameState.BOOT);
	}

	public static void addCredit() {
		GameEvents.setSoundEventsEnabled(true);
		ui.gameController().addCredit();
	}

	public static void togglePaused() {
		Ufx.toggle(GameApp.simulationPausedPy);
		// TODO mute and unmute?
		if (GameApp.simulationPausedPy.get()) {
			GameAssets.Sounds.gameSounds(ui.game().variant()).stopAll();
		}
	}

	public static void oneSimulationStep() {
		if (GameApp.simulationPausedPy.get()) {
			ui.clock().executeSingleStep(true);
		}
	}

	public static void tenSimulationSteps() {
		if (GameApp.simulationPausedPy.get()) {
			ui.clock().executeSteps(10, true);
		}
	}

	public static void changeSimulationSpeed(int delta) {
		int newFramerate = ui.clock().targetFrameratePy.get() + delta;
		if (newFramerate > 0 && newFramerate < 120) {
			GameApp.simulationSpeedPy.set(newFramerate);
			showFlashMessageSeconds(0.75, newFramerate + "Hz");
		}
	}

	public static void resetSimulationSpeed() {
		GameApp.simulationSpeedPy.set(GameModel.FPS);
		showFlashMessageSeconds(0.75, GameApp.simulationSpeedPy.get() + "Hz");
	}

	public static void selectNextGameVariant() {
		var gameVariant = ui.game().variant().next();
		ui.gameController().selectGameVariant(gameVariant);
		playHelpVoiceMessageAfterSeconds(4);
	}

	public static void toggleAutopilot() {
		ui.gameController().toggleAutoControlled();
		var auto = ui.gameController().isAutoControlled();
		String message = GameAssets.Texts.message(auto ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		playVoiceMessage(auto ? GameAssets.Sounds.VOICE_AUTOPILOT_ON : GameAssets.Sounds.VOICE_AUTOPILOT_OFF);
	}

	public static void toggleImmunity() {
		ui.game().setImmune(!ui.game().isImmune());
		var immune = ui.game().isImmune();
		String message = GameAssets.Texts.message(immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		playVoiceMessage(immune ? GameAssets.Sounds.VOICE_IMMUNITY_ON : GameAssets.Sounds.VOICE_IMMUNITY_OFF);
	}

	public static void startLevelTestMode() {
		if (ui.gameController().state() == GameState.INTRO) {
			ui.gameController().restart(GameState.LEVEL_TEST);
			showFlashMessage("Level TEST MODE");
		}
	}

	public static void cheatAddLives(int numLives) {
		if (ui.game().isPlaying()) {
			ui.game().setLives(numLives + ui.game().lives());
			showFlashMessage(GameAssets.Texts.message("cheat_add_lives", ui.game().lives()));
		}
	}

	public static void cheatEatAllPellets() {
		if (ui.game().isPlaying()) {
			ui.gameController().cheatEatAllPellets();
		}
	}

	public static void cheatEnterNextLevel() {
		ui.gameController().cheatEnterNextLevel();
	}

	public static void cheatKillAllEatableGhosts() {
		ui.gameController().cheatKillAllEatableGhosts();
	}
}