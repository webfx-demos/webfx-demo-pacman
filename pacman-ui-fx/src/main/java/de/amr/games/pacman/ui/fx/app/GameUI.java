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
import de.amr.games.pacman.event.*;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameRenderer;
import de.amr.games.pacman.ui.fx.scene.GameSceneConfiguration;
import de.amr.games.pacman.ui.fx.scene2d.*;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import de.amr.games.pacman.ui.fx.util.FlashMessageView;
import de.amr.games.pacman.ui.fx.util.GameClock;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * User interface for Pac-Man and Ms. Pac-Man.
 *
 * @author Armin Reichert
 */
public class GameUI implements GameEventListener {

	public static final byte TILES_X = 28;
	public static final byte TILES_Y = 36;

	private static final int LAYER_GAME_SCENE = 0;

	private final GameClock clock;
	private final GameController gameController;
	private final Map<GameVariant, GameSceneConfiguration> sceneConfig = new EnumMap<>(GameVariant.class);
	private final Stage stage;
	private final StackPane root = new StackPane();
	private final List<Node> layers = new ArrayList<>();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final GameHelp gameSceneHelp;
	private GreetingPane greetingPane;
	private GameScene2D currentGameScene;
	private AudioClip currentVoiceMessage;

	public GameUI(Stage stage, Settings settings, GameController gameController) {
		checkNotNull(stage);
		checkNotNull(settings);
		checkNotNull(gameController);

		this.stage = stage;
		this.gameController = gameController;

		this.clock = new GameClock(GameModel.FPS) {
			{
				pausedPy.bind(GameApp.simulationPausedPy);
				targetFrameratePy.bind(GameApp.simulationSpeedPy);
			}

			@Override
			public void doUpdate() {
				gameController.update();
				currentGameScene.update();
			}

			@Override
			public void doRender() {
				flashMessageView.update();
				currentGameScene.render();
			}
		};

		var keyboardSteering = new KeyboardSteering(
			settings.keyMap.get(Direction.UP),
			settings.keyMap.get(Direction.DOWN),
			settings.keyMap.get(Direction.LEFT),
			settings.keyMap.get(Direction.RIGHT)
		);
		gameController.setManualPacSteering(keyboardSteering);

		gameSceneHelp = new GameHelp(gameController, GameApp.assets.messageBundle);
		gameSceneHelp.setFont(GameApp.assets.helpFont);

		sceneConfig.put(GameVariant.MS_PACMAN,
				new GameSceneConfiguration(new MsPacManGameRenderer(),
						new BootScene(gameController),
						new MsPacManIntroScene(gameController),
						new MsPacManCreditScene(gameController),
						new PlayScene2D(gameController),
						new MsPacManIntermissionScene1(gameController),
						new MsPacManIntermissionScene2(gameController),
						new MsPacManIntermissionScene3(gameController)));

		sceneConfig.put(GameVariant.PACMAN,
				new GameSceneConfiguration(new PacManGameRenderer(),
						new BootScene(gameController),
						new PacManIntroScene(gameController),
						new PacManCreditScene(gameController),
						new PlayScene2D(gameController),
						new PacManCutscene1(gameController),
						new PacManCutscene2(gameController),
						new PacManCutscene3(gameController)));

		root.setBackground(ResourceManager.colorBackground(Color.BLACK));
		var mainScene = createMainScene(TILES_X * TS * settings.zoom, TILES_Y * TS * settings.zoom);
		mainScene.addEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);

		stage.setScene(mainScene);
		stage.setTitle(stageTitle());
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();

		GameEvents.addListener(this);

		boolean showGreeting = true; // UserAgent.isBrowser();
		if (showGreeting) {
			var greetingImage = game().variant() == GameVariant.MS_PACMAN
				? GameApp.assets.greetingMsPacMan
				: GameApp.assets.greetingPacMan;
			greetingPane = new GreetingPane(greetingImage, this::closeGreetingAndStart);
			layers.add(greetingPane);
			rebuildMainSceneLayers();
		} else {
			startUI();
		}
	}

	private void closeGreetingAndStart() {
		layers.remove(greetingPane);
		rebuildMainSceneLayers();
		root.setBackground(ResourceManager.imageBackground(GameApp.assets.wallpaper));
		GameApp.app.playHelpVoiceMessageAfterSeconds(4);
		gameController().restart(GameState.BOOT);
		startUI();
	}

	public void startUI() {
		GameApp.app.playHelpVoiceMessageAfterSeconds(4);
		gameController().restart(GameState.BOOT);
		clock.start();
	}

	private Scene createMainScene(float sizeX, float sizeY) {
		var scene = new Scene(root, sizeX, sizeY);
		scene.setOnKeyPressed(this::handleKeyPressed);
		scene.heightProperty().addListener((py, ov, nv) -> {
			if (currentGameScene != null) {
				currentGameScene.onParentResize(root);
			}
		});

		layers.add(new Label("")); // game scene layer
		layers.add(flashMessageView);
		rebuildMainSceneLayers();

		return scene;
	}

	private void rebuildMainSceneLayers() {
		root.getChildren().setAll(layers);
	}

	public void showHelp() {
		gameSceneHelp.show(currentGameScene, Duration.seconds(2));
	}

	private String stageTitle() {
		return game().variant() == GameVariant.MS_PACMAN ? "Ms. Pac-Man (WebFX)" : "Pac-Man (WebFX)";
	}

	private void handleKeyPressed(KeyEvent keyEvent) {
		Keyboard.accept(keyEvent);
		handleKeyboardInput();
		Keyboard.clearState();
	}

	private GameScene2D sceneMatchingCurrentGameState() {
		var gameState = gameController.state();
		var scenes = sceneConfig.get(game().variant());
		switch (gameState) {
		case BOOT:
			return scenes.bootScene();
		case CREDIT:
			return scenes.creditScene();
		case INTRO:
			return scenes.introScene();
		case GAME_OVER:
		case GHOST_DYING:
		case HUNTING:
		case LEVEL_COMPLETE:
		case LEVEL_TEST:
		case CHANGING_TO_NEXT_LEVEL:
		case PACMAN_DYING:
		case READY:
			return scenes.playScene();
		case INTERMISSION:
			GameLevel level =  game().level().orElseThrow(IllegalStateException::new);
			return scenes.cutScene(level.intermissionNumber);
		case INTERMISSION_TEST:
			return scenes.cutScene(game().intermissionTestNumber);
		default:
			throw new IllegalArgumentException("Unknown game state: " + gameState);
		}
	}

	public void updateGameScene(boolean reload) {
		var scenes = sceneConfig.get(game().variant());
		var gameScene = sceneMatchingCurrentGameState();
		if (gameScene == null) {
			throw new IllegalStateException("No game scene found for game state " + gameController.state());
		}
		if (reload || gameScene != currentGameScene) {
			changeGameScene(gameScene);
		}
		stage.setTitle(stageTitle());
		gameScene.helpButton().setVisible(gameScene != scenes.bootScene());
	}

	private void changeGameScene(GameScene2D nextGameScene) {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		var scenes = sceneConfig.get(game().variant());
		nextGameScene.context().setRendering2D(scenes.renderer());
		nextGameScene.init();
		layers.set(LAYER_GAME_SCENE, nextGameScene.root());
		rebuildMainSceneLayers();
		nextGameScene.onEmbedIntoParent(root);
		gameSceneHelp.clear(nextGameScene);
		currentGameScene = nextGameScene;
	}

	private void handleKeyboardInput() {
		if (Keyboard.pressed(GameApp.HELP)) {
			showHelp();
		} else if (Keyboard.pressed(GameApp.AUTOPILOT)) {
			GameApp.app.toggleAutopilot();
		} else if (Keyboard.pressed(GameApp.BOOT)) {
			GameApp.app.reboot();
		} else if (Keyboard.pressed(GameApp.IMMUNITY)) {
			GameApp.app.toggleImmunity();
		} else if (Keyboard.pressed(GameApp.PAUSE)) {
			GameApp.app.togglePaused();
		} else if (Keyboard.pressed(GameApp.SINGLE_STEP)) {
			GameApp.app.oneSimulationStep();
		} else if (Keyboard.pressed(GameApp.TEN_STEPS)) {
			GameApp.app.tenSimulationSteps();
		} else if (Keyboard.pressed(GameApp.SIMULATION_FASTER)) {
			GameApp.app.changeSimulationSpeed(5);
		} else if (Keyboard.pressed(GameApp.SIMULATION_SLOWER)) {
			GameApp.app.changeSimulationSpeed(-5);
		} else if (Keyboard.pressed(GameApp.SIMULATION_NORMAL)) {
			GameApp.app.resetSimulationSpeed();
		} else if (Keyboard.pressed(GameApp.QUIT)) {
			GameApp.app.restartIntro();
		} else if (currentGameScene != null) {
			currentGameScene.handleKeyboardInput();
		}
	}

	@Override
	public void onGameEvent(GameEvent e) {
		GameEventListener.super.onGameEvent(e);
		if (currentGameScene != null) {
			currentGameScene.onGameEvent(e);
		}
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		updateGameScene(false);
	}

	@Override
	public void onUnspecifiedChange(GameEvent e) {
		updateGameScene(true);
	}

	@Override
	public void onLevelStarting(GameEvent e) {
		e.game.level().ifPresent(level -> {
			var r = currentGameScene.context().rendering2D();
			level.pac().setAnimations(r.createPacAnimations(level.pac()));
			level.ghosts().forEach(ghost -> ghost.setAnimations(r.createGhostAnimations(ghost)));
			level.world().setAnimations(r.createWorldAnimations(level.world()));
			Logger.trace("Created creature and world animations for level #{}", level.number());
		});
		updateGameScene(true);
	}

	@Override
	public void onSoundEvent(SoundEvent event) {
		var sounds = GameApp.assets.gameSounds.get(event.game.variant());
		switch (event.id) {
		case GameModel.SE_BONUS_EATEN:
			sounds.play(AudioClipID.BONUS_EATEN);
			break;
		case GameModel.SE_CREDIT_ADDED:
			sounds.play(AudioClipID.CREDIT);
			break;
		case GameModel.SE_EXTRA_LIFE:
			sounds.play(AudioClipID.EXTRA_LIFE);
			break;
		case GameModel.SE_GHOST_EATEN:
			sounds.play(AudioClipID.GHOST_EATEN);
			break;
		case GameModel.SE_HUNTING_PHASE_STARTED_0:
			sounds.ensureSirenStarted(0);
			break;
		case GameModel.SE_HUNTING_PHASE_STARTED_2:
			sounds.ensureSirenStarted(1);
			break;
		case GameModel.SE_HUNTING_PHASE_STARTED_4:
			sounds.ensureSirenStarted(2);
			break;
		case GameModel.SE_HUNTING_PHASE_STARTED_6:
			sounds.ensureSirenStarted(3);
			break;
		case GameModel.SE_READY_TO_PLAY:
			sounds.play(AudioClipID.GAME_READY);
			break;
		case GameModel.SE_PACMAN_DEATH:
			sounds.play(AudioClipID.PACMAN_DEATH);
			break;
		// TODO this does not sound as in the original game
		case GameModel.SE_PACMAN_FOUND_FOOD:
			sounds.ensureLoop(AudioClipID.PACMAN_MUNCH, AudioClip.INDEFINITE);
			break;
		case GameModel.SE_PACMAN_POWER_ENDS: {
			sounds.stop(AudioClipID.PACMAN_POWER);
			event.game.level().ifPresent(level -> sounds.ensureSirenStarted(level.huntingPhase() / 2));
			break;
		}
		case GameModel.SE_PACMAN_POWER_STARTS: {
			sounds.stopSirens();
			sounds.stop(AudioClipID.PACMAN_POWER);
			sounds.loop(AudioClipID.PACMAN_POWER, AudioClip.INDEFINITE);
			break;
		}
		case GameModel.SE_START_INTERMISSION_1: {
			switch (event.game.variant()) {
			case MS_PACMAN:
				sounds.play(AudioClipID.INTERMISSION_1);
				break;
			case PACMAN:
				sounds.loop(AudioClipID.INTERMISSION_1, 2);
				break;
			default:
				throw new IllegalGameVariantException(event.game.variant());
			}
			break;
		}
		case GameModel.SE_START_INTERMISSION_2: {
			switch (event.game.variant()) {
			case MS_PACMAN:
				sounds.play(AudioClipID.INTERMISSION_2);
				break;
			case PACMAN:
				sounds.play(AudioClipID.INTERMISSION_1);
				break;
			default:
				throw new IllegalGameVariantException(event.game.variant());
			}
			break;
		}
		case GameModel.SE_START_INTERMISSION_3: {
			switch (event.game.variant()) {
			case MS_PACMAN:
				sounds.play(AudioClipID.INTERMISSION_3);
				break;
			case PACMAN:
				sounds.loop(AudioClipID.INTERMISSION_1, 2);
				break;
			default:
				throw new IllegalGameVariantException(event.game.variant());
			}
			break;
		}
		case GameModel.SE_STOP_ALL_SOUNDS:
			sounds.stopAll();
			break;
		default:
			break;
		}
	}

	public void playVoiceMessage(AudioClip voiceMessage) {
		if (currentVoiceMessage != null && currentVoiceMessage.isPlaying()) {
			return; // don't interrupt voice message still playing, maybe enqueue?
		}
		currentVoiceMessage = voiceMessage;
		currentVoiceMessage.play();
	}

	public void stopVoiceMessage() {
		if (currentVoiceMessage != null) {
			currentVoiceMessage.stop();
		}
	}


	public GameClock clock() {
		return clock;
	}

	public GameController gameController() {
		return gameController;
	}

	public GameModel game() {
		return gameController.game();
	}

	public GameScene2D currentGameScene() {
		return currentGameScene;
	}

	public FlashMessageView flashMessageView() {
		return flashMessageView;
	}
}