package de.amr.games.pacman.ui.fx.scenes.common._2d;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.GhostReturningHomeEvent;
import de.amr.games.pacman.controller.event.PacManFoundFoodEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

/**
 * Controls sound and animation for 2D play scenes in reaction to game events.
 * 
 * @author Armin Reichert
 */
public class PlayScene2DAnimationController {

	public final SoundManager sounds;
	private final PlayScene2D<?> playScene;
	private PacManGameController gameController;
	private SequentialTransition levelCompleteAnimation;

	public PlayScene2DAnimationController(PlayScene2D<?> playScene, SoundManager sounds) {
		this.playScene = playScene;
		this.sounds = sounds;
	}

	public void setGameController(PacManGameController gameController) {
		this.gameController = gameController;
	}

	private AbstractGameModel game() {
		return gameController.game();
	}
	
	public void init() {
		levelCompleteAnimation = new SequentialTransition(playScene.maze2D.getFlashingAnimation(),
				new PauseTransition(Duration.seconds(1)));
		levelCompleteAnimation.setDelay(Duration.seconds(2));
		levelCompleteAnimation.setOnFinished(e -> gameController.stateTimer().forceExpiration());
	}

	public void update() {
		if (gameController.isAttractMode()) {
			return;
		}
		sounds.setMuted(false);

		if (gameController.state == PacManGameState.HUNTING) {
			AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
			if (munching.isPlaying()) {
				if (game().player.starvingTicks > 10) {
					sounds.stop(PacManGameSound.PACMAN_MUNCH);
					log("Munching sound clip %s stopped", munching);
				}
			}
		}
	}

	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
		sounds.setMuted(gameController.isAttractMode());

		// enter READY
		if (newState == PacManGameState.READY) {
			sounds.stopAll();
			if (!gameController.isAttractMode() && !gameController.isGameRunning()) {
				gameController.stateTimer().resetSeconds(4.5);
				sounds.play(PacManGameSound.GAME_READY);
			} else {
				gameController.stateTimer().resetSeconds(2);
			}
		}

		// enter HUNTING
		if (newState == PacManGameState.HUNTING) {
			playScene.maze2D.getEnergizerBlinking().restart();
			playScene.player2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
			playScene.ghosts2D.forEach(ghost2D -> ghost2D.getKickingAnimations().values().forEach(TimedSequence::restart));
		}

		// enter PACMAN_DYING
		if (newState == PacManGameState.PACMAN_DYING) {
			playScene.maze2D.getEnergizerBlinking().reset();
			playScene.ghosts2D.forEach(ghost2D -> ghost2D.getKickingAnimations().values().forEach(TimedSequence::reset));
			playScene.player2D.getDyingAnimation().restart();
			sounds.stopAll();
			sounds.play(PacManGameSound.PACMAN_DEATH);
		}

		// enter GHOST_DYING
		if (newState == PacManGameState.GHOST_DYING) {
			game().player.visible = false;
			sounds.play(PacManGameSound.GHOST_EATEN);
		}

		// exit GHOST_DYING
		if (oldState == PacManGameState.GHOST_DYING) {
			game().player.visible = true;
		}

		// enter LEVEL_COMPLETE
		if (newState == PacManGameState.LEVEL_COMPLETE) {
			game().ghosts().forEach(ghost -> ghost.visible = false);
			gameController.stateTimer().reset();
			levelCompleteAnimation.play();
			sounds.stopAll();
		}

		// enter GAME_OVER
		if (newState == PacManGameState.GAME_OVER) {
			playScene.maze2D.getEnergizerBlinking().reset();
			playScene.ghosts2D.forEach(ghost2D -> ghost2D.getKickingAnimations().values().forEach(TimedSequence::restart));
			sounds.stopAll();
		}
	}

	public void onGameEvent(PacManGameEvent gameEvent) {
		if (gameController.isAttractMode()) {
			return;
		}
		sounds.setMuted(false);

		if (gameEvent instanceof ScatterPhaseStartedEvent) {
			ScatterPhaseStartedEvent e = (ScatterPhaseStartedEvent) gameEvent;
			if (e.scatterPhase > 0) {
				sounds.stop(PacManGameSound.SIRENS.get(e.scatterPhase - 1));
			}
			PacManGameSound siren = PacManGameSound.SIRENS.get(e.scatterPhase);
			if (!sounds.getClip(siren).isPlaying())
				sounds.loop(siren, Integer.MAX_VALUE);
		}

		else if (gameEvent instanceof PacManLostPowerEvent) {
			sounds.stop(PacManGameSound.PACMAN_POWER);
		}

		else if (gameEvent instanceof PacManGainsPowerEvent) {
			sounds.loop(PacManGameSound.PACMAN_POWER, Integer.MAX_VALUE);
		}

		else if (gameEvent instanceof PacManFoundFoodEvent) {
			AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
			if (!munching.isPlaying()) {
				sounds.loop(PacManGameSound.PACMAN_MUNCH, Integer.MAX_VALUE);
				Logging.log("Munching sound clip %s started", munching);
			}
		}

		else if (gameEvent instanceof BonusEatenEvent) {
			sounds.play(PacManGameSound.BONUS_EATEN);
		}

		else if (gameEvent instanceof ExtraLifeEvent) {
			sounds.play(PacManGameSound.EXTRA_LIFE);
		}

		else if (gameEvent instanceof GhostReturningHomeEvent) {
			sounds.play(PacManGameSound.GHOST_RETURNING_HOME);
		}
	}
}