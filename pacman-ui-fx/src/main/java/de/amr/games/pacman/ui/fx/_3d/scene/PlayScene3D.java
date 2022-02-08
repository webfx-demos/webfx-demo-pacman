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
package de.amr.games.pacman.ui.fx._3d.scene;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.World.HTS;
import static de.amr.games.pacman.model.world.World.TS;
import static de.amr.games.pacman.ui.fx._3d.entity.Maze3D.NodeInfo.info;
import static de.amr.games.pacman.ui.fx.util.U.afterSeconds;
import static de.amr.games.pacman.ui.fx.util.U.pause;

import java.util.EnumMap;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._3d.entity.Bonus3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.LevelCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.LivesCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D;
import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import de.amr.games.pacman.ui.fx._3d.entity.ScoreNotReally3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.AbstractGameScene;
import de.amr.games.pacman.ui.fx.shell.FlashMessageView;
import de.amr.games.pacman.ui.fx.util.CoordinateSystem;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.beans.Observable;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D extends AbstractGameScene {

	protected final PacManModel3D model3D;
	protected final EnumMap<Perspective, CameraController<PlayScene3D>> cams = new EnumMap<>(Perspective.class);
	protected final Image floorImage = new Image(getClass().getResource("/common/escher-texture.jpg").toString());
	protected final AmbientLight ambientLight = new AmbientLight(Color.GHOSTWHITE);
	protected final CoordinateSystem coordSystem = new CoordinateSystem(1000);

	protected Maze3D maze3D;
	protected Pac3D player3D;
	protected Ghost3D[] ghosts3D;
	protected Bonus3D bonus3D;
	protected ScoreNotReally3D score3D;
	protected LevelCounter3D levelCounter3D;
	protected LivesCounter3D livesCounter3D;

	public PlayScene3D(PacManModel3D model3D) {
		this.model3D = model3D;
		coordSystem.visibleProperty().bind(Env.$axesVisible);
	}

	@Override
	public SubScene createSubScene(Scene parent) {
		if (fxSubScene == null) {
			fxSubScene = new SubScene(new Group(), parent.getWidth(), parent.getHeight(), true, SceneAntialiasing.BALANCED);
			fxSubScene.widthProperty().bind(parent.widthProperty());
			fxSubScene.heightProperty().bind(parent.heightProperty());
			fxSubScene.addEventHandler(KeyEvent.KEY_PRESSED, e -> camController().handle(e));
			PerspectiveCamera cam = new PerspectiveCamera(true);
			fxSubScene.setCamera(cam);
			cams.put(Perspective.CAM_FOLLOWING_PLAYER, new Cam_FollowingPlayer(cam));
			cams.put(Perspective.CAM_NEAR_PLAYER, new Cam_NearPlayer(cam));
			cams.put(Perspective.CAM_TOTAL, new Cam_Total(cam));
			log("Subscene for game scene '%s' created, width=%.0f, height=%.0f", getClass().getName(), fxSubScene.getWidth(),
					fxSubScene.getHeight());
		}
		return fxSubScene;
	}

	public CameraController<PlayScene3D> camController() {
		return cams.get(Env.$perspective.get());
	}

	@Override
	public void init() {
		final int width = game.world.numCols() * TS;
		final int height = game.world.numRows() * TS;

		maze3D = new Maze3D(width, height, floorImage);
		maze3D.$wallHeight.bind(Env.$mazeWallHeight);
		maze3D.$resolution.bind(Env.$mazeResolution);
		maze3D.$resolution.addListener($1 -> buildMaze3D(game.mazeNumber, false));
		buildMaze3D(game.mazeNumber, true);

		player3D = new Pac3D(game.player, model3D);
		ghosts3D = game.ghosts().map(ghost -> new Ghost3D(ghost, model3D, Env.r2D)).toArray(Ghost3D[]::new);
		bonus3D = new Bonus3D(Env.r2D);

		score3D = new ScoreNotReally3D(Env.r2D.getScoreFont());
		score3D.scoreValueOverwrite = gameController.attractMode ? "GAME OVER!" : null;

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.getTransforms().add(new Translate(TS, TS, -HTS));
		livesCounter3D.setVisible(!gameController.attractMode);

		levelCounter3D = new LevelCounter3D(game, Env.r2D, width - TS, TS);

		Group playground = new Group();
		playground.getChildren().addAll(maze3D, score3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		playground.getChildren().addAll(ghosts3D);
		playground.getTransforms().add(new Translate(-width / 2, -height / 2)); // center at origin

		fxSubScene.setRoot(new Group(ambientLight, playground, coordSystem));

		Env.sounds.setMuted(gameController.attractMode);
		Env.$perspective.addListener(this::onPerspectiveChange);
		onPerspectiveChange(null);
	}

	@Override
	public void end() {
		Env.sounds.setMuted(false);
		Env.$perspective.removeListener(this::onPerspectiveChange);
		super.end();
	}

	@Override
	public void update() {
		keepInSyncWith2DScene(); // TODO find a better solution
		maze3D.update(game);
		player3D.update();
		Stream.of(ghosts3D).forEach(Ghost3D::update);
		bonus3D.update(game.bonus);
		score3D.update(game.score, game.levelNumber, game.hiscorePoints, game.hiscoreLevel);
		livesCounter3D.update(game.player.lives);
		camController().update(this);
	}

	private void buildMaze3D(int mazeNumber, boolean withFood) {
		maze3D.buildStructure(game.world, Env.r2D.getMazeSideColor(mazeNumber), Env.r2D.getMazeTopColor(mazeNumber));
		if (withFood) {
			maze3D.buildFood(game.world, Env.r2D.getFoodColor(mazeNumber));
		}
		log("Built 3D maze (resolution=%d, wall height=%.2f)", maze3D.$resolution.get(), maze3D.$wallHeight.get());
	}

	private void onPerspectiveChange(Observable unused) {
		fxSubScene.setCamera(camController().cam()); // TODO why is this needed?
		camController().reset();
		if (score3D != null) {
			// TODO maybe there is some smarter way to keep the score in play sight
			score3D.rotationAxisProperty().bind(camController().cam().rotationAxisProperty());
			score3D.rotateProperty().bind(camController().cam().rotateProperty());
		}
	}

	/*
	 * Updates food visibility, animations and audio in case of switching between 2D and 3D scene
	 * 
	 * TODO: still incomplete
	 */
	private void keepInSyncWith2DScene() {
		maze3D.foodNodes().forEach(foodNode -> {
			foodNode.setVisible(!game.isFoodEaten(info(foodNode).tile));
		});
		maze3D.startEnergizerAnimationsIfNotRunning();
		AudioClip munching = Env.sounds.getClip(GameSounds.PACMAN_MUNCH);
		if (munching.isPlaying()) {
			if (game.player.starvingTicks > 10) {
				Env.sounds.stop(GameSounds.PACMAN_MUNCH);
			}
		}
	}

	@Override
	public boolean is3D() {
		return true;
	}

	@Override
	public void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
		if (e.scatterPhase > 0) {
			Env.sounds.stop(GameSounds.SIRENS.get(e.scatterPhase - 1));
		}
		GameSounds siren = GameSounds.SIRENS.get(e.scatterPhase);
		if (!Env.sounds.getClip(siren).isPlaying())
			Env.sounds.loop(siren, Animation.INDEFINITE);
	}

	@Override
	public void onPlayerGainsPower(GameEvent e) {
		Env.sounds.loop(GameSounds.PACMAN_POWER, Animation.INDEFINITE);
		Stream.of(ghosts3D) //
				.filter(ghost3D -> ghost3D.creature.is(GhostState.FRIGHTENED) || ghost3D.creature.is(GhostState.LOCKED))
				.forEach(Ghost3D::setFrightenedSkinColor);
	}

	@Override
	public void onPlayerLosingPower(GameEvent e) {
		Stream.of(ghosts3D) //
				.filter(ghost3D -> ghost3D.creature.is(GhostState.FRIGHTENED)) //
				.forEach(ghost3D -> ghost3D.playFlashingAnimation());
	}

	@Override
	public void onPlayerLostPower(GameEvent e) {
		Env.sounds.stop(GameSounds.PACMAN_POWER);
		Stream.of(ghosts3D).forEach(Ghost3D::setNormalSkinColor);
	}

	@Override
	public void onPlayerFoundFood(GameEvent e) {
		if (e.tile.isEmpty()) { // happens when using the "eat all pellets except energizers" cheat
			maze3D.foodNodes().filter(node -> !info(node).energizer).forEach(maze3D::hideFoodNode);
		} else {
			V2i tile = e.tile.get();
			maze3D.foodNodeAt(tile).ifPresent(maze3D::hideFoodNode);
			AudioClip munching = Env.sounds.getClip(GameSounds.PACMAN_MUNCH);
			if (!munching.isPlaying()) {
				Env.sounds.loop(GameSounds.PACMAN_MUNCH, Animation.INDEFINITE);
			}
		}
	}

	@Override
	public void onBonusActivated(GameEvent e) {
		bonus3D.showSymbol(game.bonus.symbol);
	}

	@Override
	public void onBonusEaten(GameEvent e) {
		bonus3D.showPoints(game.bonus.points);
		Env.sounds.play(GameSounds.BONUS_EATEN);
	}

	@Override
	public void onBonusExpired(GameEvent e) {
		bonus3D.setVisible(false);
	}

	@Override
	public void onExtraLife(GameEvent e) {
		FlashMessageView.showFlashMessage(1.5, Env.message("extra_life"));
		Env.sounds.play(GameSounds.EXTRA_LIFE);
	}

	@Override
	public void onGhostReturnsHome(GameEvent e) {
		Env.sounds.play(GameSounds.GHOST_RETURNING);
	}

	@Override
	public void onGhostEntersHouse(GameEvent e) {
		if (game.ghosts(GhostState.DEAD).count() == 0) {
			Env.sounds.stop(GameSounds.GHOST_RETURNING);
		}
	}

	@Override
	public void onGhostLeavingHouse(GameEvent e) {
		ghosts3D[e.ghost.get().id].setNormalSkinColor();
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {

		// enter READY
		if (e.newGameState == GameState.READY) {
			maze3D.reset();
			player3D.reset();
			Stream.of(ghosts3D).forEach(Ghost3D::reset);
			Env.sounds.stopAll();
			Env.sounds.setMuted(gameController.attractMode);
			if (!gameController.gameRunning) {
				Env.sounds.play(GameSounds.GAME_READY);
			}
		}

		// enter HUNTING
		else if (e.newGameState == GameState.HUNTING) {
			maze3D.playEnergizerAnimations();
		}

		// enter PACMAN_DYING
		else if (e.newGameState == GameState.PACMAN_DYING) {
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.setNormalSkinColor());
			Env.sounds.stopAll();
			Ghost killer = Stream.of(game.ghosts).filter(ghost -> ghost.tile().equals(game.player.tile())).findAny().get();
			new SequentialTransition( //
					afterSeconds(1, game::hideGhosts), //
					player3D.dyingAnimation(Env.r2D.getGhostColor(killer.id)), //
					afterSeconds(2, () -> gameController.stateTimer().expire()) //
			).play();
		}

		// enter GHOST_DYING
		else if (e.newGameState == GameState.GHOST_DYING) {
			Env.sounds.play(GameSounds.GHOST_EATEN);
		}

		// enter LEVEL_STARTING
		else if (e.newGameState == GameState.LEVEL_STARTING) {
			buildMaze3D(game.mazeNumber, true);
			levelCounter3D.update();
			var message = Env.message("level_starting", game.levelNumber);
			FlashMessageView.showFlashMessage(1, message);
			afterSeconds(3, () -> gameController.stateTimer().expire()).play();
		}

		// enter LEVEL_COMPLETE
		else if (e.newGameState == GameState.LEVEL_COMPLETE) {
			Env.sounds.stopAll();
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.setNormalSkinColor());
			var message = Env.LEVEL_COMPLETE_TALK.next() + "\n\n" + Env.message("level_complete", game.levelNumber);
			var animation = new SequentialTransition( //
					pause(1), //
					maze3D.flashingAnimation(game.numFlashes), //
					afterSeconds(1, () -> game.player.hide()), //
					afterSeconds(1, () -> FlashMessageView.showFlashMessage(2, message)) //
			);
			animation.setOnFinished(ae -> gameController.stateTimer().expire());
			animation.play();
		}

		// enter GAME_OVER
		else if (e.newGameState == GameState.GAME_OVER) {
			Env.sounds.stopAll();
			FlashMessageView.showFlashMessage(3, Env.GAME_OVER_TALK.next());
		}

		// exit HUNTING
		if (e.oldGameState == GameState.HUNTING && e.newGameState != GameState.GHOST_DYING) {
			maze3D.stopEnergizerAnimations();
			bonus3D.setVisible(false);
		}
	}
}