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
import javafx.scene.media.AudioClip;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.lib.Globals.RND;

/**
 * @author Armin Reichert
 */
public class Actions {

	private static GameUI ui;
	private static AudioClip currentVoiceMessage; //TODO move elsewhere

	public static void init(GameUI ui) {
		Actions.ui = ui;
	}

	public static void showHelp() {
		ui.showHelp();
	}

	public static void playHelpVoiceMessageAfterSeconds(int seconds) {
		Ufx.afterSeconds(seconds, () -> playVoiceMessage(AppRes.Sounds.VOICE_HELP)).play();
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
		Ufx.toggle(Env.simulationPausedPy);
		// TODO mute and unmute?
		if (Env.simulationPausedPy.get()) {
			AppRes.Sounds.gameSounds(ui.game().variant()).stopAll();
		}
	}

	public static void oneSimulationStep() {
		if (Env.simulationPausedPy.get()) {
			ui.executeSingleStep(true);
		}
	}

	public static void tenSimulationSteps() {
		if (Env.simulationPausedPy.get()) {
			ui.executeSteps(10, true);
		}
	}

	public static void changeSimulationSpeed(int delta) {
		int newFramerate = ui.targetFrameratePy.get() + delta;
		if (newFramerate > 0 && newFramerate < 120) {
			Env.simulationSpeedPy.set(newFramerate);
			showFlashMessageSeconds(0.75, newFramerate + "Hz");
		}
	}

	public static void resetSimulationSpeed() {
		Env.simulationSpeedPy.set(GameModel.FPS);
		showFlashMessageSeconds(0.75, Env.simulationSpeedPy.get() + "Hz");
	}

	public static void selectNextGameVariant() {
		var gameVariant = ui.game().variant().next();
		ui.gameController().selectGameVariant(gameVariant);
		playHelpVoiceMessageAfterSeconds(4);
	}

	public static void toggleAutopilot() {
		ui.gameController().toggleAutoControlled();
		var auto = ui.gameController().isAutoControlled();
		String message = AppRes.Texts.message(auto ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		playVoiceMessage(auto ? AppRes.Sounds.VOICE_AUTOPILOT_ON : AppRes.Sounds.VOICE_AUTOPILOT_OFF);
		ui.updateHelpContent();
	}

	public static void toggleImmunity() {
		ui.game().setImmune(!ui.game().isImmune());
		var immune = ui.game().isImmune();
		String message = AppRes.Texts.message(immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		playVoiceMessage(immune ? AppRes.Sounds.VOICE_IMMUNITY_ON : AppRes.Sounds.VOICE_IMMUNITY_OFF);
		ui.updateHelpContent();
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
			showFlashMessage(AppRes.Texts.message("cheat_add_lives", ui.game().lives()));
		}
	}

	public static void cheatEatAllPellets() {
		if (ui.game().isPlaying()) {
			ui.gameController().cheatEatAllPellets();
			if (RND.nextDouble() < 0.1) {
				showFlashMessage(AppRes.Texts.pickCheatingMessage());
			}
		}
	}

	public static void cheatEnterNextLevel() {
		ui.gameController().cheatEnterNextLevel();
	}

	public static void cheatKillAllEatableGhosts() {
		ui.gameController().cheatKillAllEatableGhosts();
		if (RND.nextDouble() < 0.1) {
			showFlashMessage(AppRes.Texts.pickCheatingMessage());
		}
	}
}