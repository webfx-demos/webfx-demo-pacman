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
package de.amr.games.pacman.ui.fx._2d.scene.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;
import static de.amr.games.pacman.ui.fx.util.Animations.pause;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.TickTimerEvent;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx._2d.entity.common.Bonus2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LivesCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Maze2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.fx.util.Animations;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends AbstractGameScene2D {

	private Maze2D maze2D;
	private LivesCounter2D livesCounter2D;
	private Player2D player2D;
	private List<Ghost2D> ghosts2D;
	private Bonus2D bonus2D;

	public PlayScene2D(int tilesX, int tilesY, Rendering2D rendering, SoundManager sounds) {
		super(tilesX, tilesY, rendering, sounds);
	}

	@Override
	public void init(PacManGameController gameController) {
		super.init(gameController);

		maze2D = new Maze2D(0, t(3), game, rendering);
		livesCounter2D = new LivesCounter2D(t(2), t(34), game, rendering);
		player2D = new Player2D(game.player, rendering);
		player2D.dyingAnimation.delay(120).onStart(game::hideGhosts);
		ghosts2D = game.ghosts().map(ghost -> new Ghost2D(ghost, rendering)).collect(Collectors.toList());
		bonus2D = new Bonus2D(game.bonus, rendering);
		game.player.powerTimer.addEventListener(this::handleGhostsFlashing);
	}

	@Override
	public void end() {
		game.player.powerTimer.removeEventListener(this::handleGhostsFlashing);
	}

	@Override
	public void doUpdate() {
		sounds.setMuted(gameController.isAttractMode());
		if (gameController.currentStateID == PacManGameState.HUNTING) {
			// ensure animations are running when switching between 2D and 3D
			if (!player2D.munchingAnimations.get(game.player.dir()).isRunning()) {
				player2D.munchingAnimations.values().forEach(TimedSequence::restart);
			}
			if (!maze2D.getEnergizerAnimation().isRunning()) {
				maze2D.getEnergizerAnimation().restart();
			}
			AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
			if (munching.isPlaying()) {
				if (game.player.starvingTicks > 10) {
					sounds.stop(PacManGameSound.PACMAN_MUNCH);
					log("Munching sound stopped");
				}
			}
		}
	}

	@Override
	public void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
		if (e.scatterPhase > 0) {
			sounds.stop(PacManGameSound.SIRENS.get(e.scatterPhase - 1));
		}
		PacManGameSound siren = PacManGameSound.SIRENS.get(e.scatterPhase);
		if (!sounds.getClip(siren).isPlaying()) {
			sounds.loop(siren, Integer.MAX_VALUE);
		}
	}

	@Override
	public void onPlayerLostPower(PacManGameEvent e) {
		sounds.stop(PacManGameSound.PACMAN_POWER);
	}

	@Override
	public void onPlayerGainsPower(PacManGameEvent e) {
		game.ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
			Ghost2D ghost2D = ghosts2D.get(ghost.id);
			ghost2D.flashingAnimation.reset();
			ghost2D.frightenedAnimation.restart();
		});
		sounds.loop(PacManGameSound.PACMAN_POWER, Integer.MAX_VALUE);
	}

	@Override
	public void onPlayerFoundFood(PacManGameEvent e) {
		AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
		if (!munching.isPlaying()) {
			sounds.loop(PacManGameSound.PACMAN_MUNCH, Integer.MAX_VALUE);
			log("Munching sound started");
		}
	}

	@Override
	public void onBonusActivated(PacManGameEvent e) {
		bonus2D.startAnimation();
	}

	@Override
	public void onBonusEaten(PacManGameEvent e) {
		bonus2D.stopAnimation();
		sounds.play(PacManGameSound.BONUS_EATEN);
	}

	@Override
	public void onExtraLife(PacManGameEvent e) {
		sounds.play(PacManGameSound.EXTRA_LIFE);
	}

	@Override
	public void onGhostReturnsHome(PacManGameEvent e) {
		sounds.play(PacManGameSound.GHOST_RETURNING);
	}

	@Override
	public void onGhostEntersHouse(PacManGameEvent e) {
		if (game.ghosts(GhostState.DEAD).count() == 0) {
			sounds.stop(PacManGameSound.GHOST_RETURNING);
		}
	}

	@Override
	public void onPacManGameStateChange(PacManGameStateChangeEvent e) {

		// enter READY
		if (e.newGameState == PacManGameState.READY) {
			sounds.stopAll();
			maze2D.getEnergizerAnimation().reset();
			player2D.reset();
			if (!gameController.isAttractMode() && !gameController.isGameRunning()) {
				sounds.setMuted(false);
				sounds.play(PacManGameSound.GAME_READY);
			}
		}

		// enter HUNTING
		else if (e.newGameState == PacManGameState.HUNTING) {
			maze2D.getEnergizerAnimation().restart();
			player2D.munchingAnimations.values().forEach(TimedSequence::restart);
			ghosts2D.forEach(ghost2D -> ghost2D.kickingAnimations.values().forEach(TimedSequence::restart));
		}

		// enter PACMAN_DYING
		else if (e.newGameState == PacManGameState.PACMAN_DYING) {
			sounds.stopAll();
			ghosts2D.forEach(ghost2D -> ghost2D.kickingAnimations.values().forEach(TimedSequence::reset));
			player2D.dyingAnimation.restart();
			Animations.afterSeconds(2, () -> sounds.play(PacManGameSound.PACMAN_DEATH)).play();
			gameController.stateTimer().resetSeconds(4);
			gameController.stateTimer().start();
		}

		// enter GHOST_DYING
		else if (e.newGameState == PacManGameState.GHOST_DYING) {
			game.player.hide();
			sounds.play(PacManGameSound.GHOST_EATEN);
		}

		// enter LEVEL_COMPLETE
		else if (e.newGameState == PacManGameState.LEVEL_COMPLETE) {
			sounds.stopAll();
			player2D.reset();
			game.hideGhosts();
			gameController.stateTimer().reset();
			maze2D.getEnergizerAnimation().reset(); // energizers might still exist if "next level" cheat has been used

			Animation levelCompleteAnimation = new SequentialTransition(pause(2), maze2D.getFlashingAnimation(), pause(1));
			levelCompleteAnimation.setOnFinished(event -> gameController.stateTimer().expire());
			levelCompleteAnimation.play();
		}

		// enter LEVEL_STARTING
		else if (e.newGameState == PacManGameState.LEVEL_STARTING) {
			maze2D.setGame(game);
			gameController.stateTimer().reset(TickTimer.sec_to_ticks(1));
			gameController.stateTimer().start();
		}

		// enter GAME_OVER
		else if (e.newGameState == PacManGameState.GAME_OVER) {
			maze2D.getEnergizerAnimation().reset();
			ghosts2D.forEach(ghost2D -> ghost2D.kickingAnimations.values().forEach(TimedSequence::restart));
			sounds.stopAll();
		}

		// exit GHOST_DYING
		if (e.oldGameState == PacManGameState.GHOST_DYING) {
			game.player.show();
		}
	}

	// TODO simplify
	public void handleGhostsFlashing(TickTimerEvent e) {
		if (e.type == TickTimerEvent.Type.HALF_EXPIRED) {
			game.ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
				Ghost2D ghost2D = ghosts2D.get(ghost.id);
				TimedSequence<?> flashing = ghost2D.flashingAnimation;
				long frameTime = e.ticks / (game.numFlashes * flashing.numFrames());
				flashing.frameDuration(frameTime).repetitions(game.numFlashes).restart();
			});
		}
	}

	@Override
	public void doRender() {
		if (gameController.isAttractMode()) {
			score2D.showPoints = false;
		} else {
			score2D.showPoints = true;
			livesCounter2D.render(gc);
			renderLevelCounter();
		}
		renderGameState();
		game.ghosts(GhostState.LOCKED)
				.forEach(ghost -> ghosts2D.get(ghost.id).setLooksFrightened(game.player.powerTimer.isRunning()));
		Stream.concat(Stream.of(score2D, highScore2D, maze2D, bonus2D, player2D), ghosts2D.stream())
				.forEach(r -> r.render(gc));
	}

	private void renderGameState() {
		PacManGameState state = gameController.isAttractMode() ? PacManGameState.GAME_OVER : gameController.currentStateID;
		if (state == PacManGameState.GAME_OVER) {
			gc.setFont(rendering.getScoreFont());
			gc.setFill(Color.RED);
			gc.fillText("GAME", t(9), t(21));
			gc.fillText("OVER", t(15), t(21));
		} else if (state == PacManGameState.READY) {
			gc.setFont(rendering.getScoreFont());
			gc.setFill(Color.YELLOW);
			gc.fillText("READY!", t(11), t(21));
		}
	}
}