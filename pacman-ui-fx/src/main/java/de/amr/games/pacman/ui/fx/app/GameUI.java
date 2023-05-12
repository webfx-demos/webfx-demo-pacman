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
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.Rendering2D;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneChoice;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import de.amr.games.pacman.ui.fx.util.FlashMessageView;
import de.amr.games.pacman.ui.fx.util.GameLoop;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static javafx.scene.layout.BackgroundSize.AUTO;

/**
 * User interface for Pac-Man and Ms. Pac-Man games.
 *
 * @author Armin Reichert
 */
public class GameUI extends GameLoop implements GameEventListener {

	private static final byte TILES_X = 28;
	private static final byte TILES_Y = 36;

	private static final byte INDEX_BOOT_SCENE = 0;
	private static final byte INDEX_INTRO_SCENE = 1;
	private static final byte INDEX_CREDIT_SCENE = 2;
	private static final byte INDEX_PLAY_SCENE = 3;

	private static final int LAYER_GAME_SCENE = 0;
	// LAYER_FLASH_MESSAGES = 1, LAYER_GREETING = 2;

	private final GameController gameController;
	private final Map<GameVariant, Rendering2D> renderers = new EnumMap<>(GameVariant.class);
	private final Map<GameVariant, List<GameSceneChoice>> scenes = new EnumMap<>(GameVariant.class);
	private final Stage stage;
	private final StackPane root = new StackPane();
	private final List<Node> layers = new ArrayList<>();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final ContextSensitiveHelp csHelp;
	private BorderPane greetingPane;
	private GameScene currentGameScene;

	public GameUI(final Stage stage, final Settings settings, GameController gameController,
			List<GameSceneChoice> msPacManScenes, List<GameSceneChoice> pacManScenes) {

		super(GameModel.FPS);

		checkNotNull(stage);
		checkNotNull(settings);

		this.stage = stage;
		this.gameController = gameController;

		var keyboardSteering = new KeyboardSteering(
			settings.keyMap.get(Direction.UP),
			settings.keyMap.get(Direction.DOWN),
			settings.keyMap.get(Direction.LEFT),
			settings.keyMap.get(Direction.RIGHT)
		);
		gameController.setManualPacSteering(keyboardSteering);

		csHelp = new ContextSensitiveHelp(gameController, AppRes.Texts.messageBundle);

		createGreetingPane();
		//TODO click on greeting text somehow didn't work in browser, so let user click anywhere
		greetingPane.setOnMouseClicked(e -> {
			layers.remove(greetingPane);
			rebuildMainSceneLayers();
			root.setBackground(ResourceManager.imageBackground(AppRes.Graphics.wallpaper));
			Actions.playHelpVoiceMessageAfterSeconds(4);
			gameController().restart(GameState.BOOT);
			start();
		});


		// renderers must be created before game scenes
		renderers.put(GameVariant.MS_PACMAN, new MsPacManGameRenderer());
		scenes.put(GameVariant.MS_PACMAN, msPacManScenes);

		renderers.put(GameVariant.PACMAN, new PacManGameRenderer());
		scenes.put(GameVariant.PACMAN, pacManScenes);

		var mainScene = createMainScene(TILES_X * TS * settings.zoom, TILES_Y * TS * settings.zoom);
		mainScene.addEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);
		stage.setScene(mainScene);

		GameEvents.addListener(this);
		initEnv();
		Actions.init(this);

		updateMainView();

		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();
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

	private void createGreetingPane() {
		var ds = new DropShadow();
		ds.setOffsetY(3.0f);
		ds.setColor(Color.color(0.2f, 0.2f, 0.2f));

		var text = new Text("Click to start!");
		text.setMouseTransparent(true);
		text.setEffect(ds);
		text.setCache(true);
		text.setFill(Color.YELLOW);
		text.setFont(AppRes.Fonts.font(AppRes.Fonts.arcade, 20));

		greetingPane = new BorderPane();
		greetingPane.setCenter(text);

		BorderPane.setAlignment(text, Pos.CENTER);
		text.setTranslateY(20);
	}

	private Scene createMainScene(float sizeX, float sizeY) {
		var scene = new Scene(root, sizeX, sizeY);
		scene.setOnKeyPressed(this::handleKeyPressed);
		scene.heightProperty().addListener((py, ov, nv) -> {
			if (currentGameScene != null) {
				currentGameScene.onParentSceneResize(scene);
			}
		});

		layers.add(new Label("")); // game scene layer
		layers.add(flashMessageView);
		layers.add(greetingPane);
		rebuildMainSceneLayers();

		var bgImage = new BackgroundImage(
			AppRes.Graphics.msPacManCabinet,
			BackgroundRepeat.NO_REPEAT,
			BackgroundRepeat.NO_REPEAT,
			BackgroundPosition.CENTER,
			new BackgroundSize(AUTO, AUTO, false, false, false, true));
		root.setBackground(new Background(bgImage));

		return scene;
	}

	private void rebuildMainSceneLayers() {
		root.getChildren().setAll(layers);
	}


	public void showHelp() {
		if (currentGameScene instanceof GameScene2D) {
			GameScene2D gameScene2d = (GameScene2D) currentGameScene;
			updateHelpContent();
			csHelp.show(gameScene2d.helpRoot(), Duration.seconds(2));
		}
	}

	public void updateHelpContent() {
		if (currentGameScene instanceof GameScene2D) {
			GameScene2D gameScene2d = (GameScene2D) currentGameScene;
			var help = csHelp.current();
			if (help.isEmpty()) {
				gameScene2d.helpRoot().getChildren().clear();
			} else {
				gameScene2d.helpRoot().getChildren().setAll(help.get());
			}
		}
	}

	private void updateMainView() {
		switch (gameController.game().variant()) {
		case MS_PACMAN: {
			stage.setTitle("Ms. Pac-Man (WebFX)");
			break;
		}
		case PACMAN: {
			stage.setTitle("Pac-Man (WebFX)");
			break;
		}
		default:
			throw new IllegalGameVariantException(gameController.game().variant());
		}
	}

	private void handleKeyPressed(KeyEvent keyEvent) {
		Keyboard.accept(keyEvent);
		handleKeyboardInput();
		Keyboard.clearState();
	}

	private void initEnv() {
		Env.mainSceneBgColorPy.addListener((py, oldVal, newVal) -> updateMainView());
		Env.simulationPausedPy.addListener((py, oldVal, newVal) -> updateMainView());
		pausedPy.bind(Env.simulationPausedPy);
		targetFrameratePy.bind(Env.simulationSpeedPy);
		measuredPy.bind(Env.simulationTimeMeasuredPy);
	}

	private GameSceneChoice sceneSelectionMatchingCurrentGameState() {
		var game = gameController.game();
		var gameState = gameController.state();
		int index;
		switch (gameState) {
		case BOOT:
			index = INDEX_BOOT_SCENE;
			break;
		case CREDIT:
			index = INDEX_CREDIT_SCENE;
			break;
		case INTRO:
			index = INDEX_INTRO_SCENE;
			break;
		case GAME_OVER:
		case GHOST_DYING:
		case HUNTING:
		case LEVEL_COMPLETE:
		case LEVEL_TEST:
		case CHANGING_TO_NEXT_LEVEL:
		case PACMAN_DYING:
		case READY:
			index = INDEX_PLAY_SCENE;
			break;
		case INTERMISSION:
			index = INDEX_PLAY_SCENE + game.level().orElseThrow(IllegalStateException::new).intermissionNumber;
			break;
		case INTERMISSION_TEST:
			index = INDEX_PLAY_SCENE + game.intermissionTestNumber;
			break;
		default:
			throw new IllegalArgumentException("Unknown game state: %s"/* .formatted(gameState) */);
		}
		return scenes.get(game.variant()).get(index);
	}

	public void updateGameScene(boolean reload) {
		var matching = sceneSelectionMatchingCurrentGameState();
		var nextGameScene = matching.scene2D();
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found for game state " + gameController.state());
		}
		if (reload || nextGameScene != currentGameScene) {
			changeGameScene(nextGameScene);
		}
		updateMainView();
	}

	private void changeGameScene(GameScene nextGameScene) {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		var renderer = renderers.get(gameController.game().variant());
		nextGameScene.context().setRendering2D(renderer);
		nextGameScene.init();
		layers.set(LAYER_GAME_SCENE, nextGameScene.root());
		rebuildMainSceneLayers();
		nextGameScene.onEmbedIntoParentScene(mainScene());
		currentGameScene = nextGameScene;
	}

	private void handleKeyboardInput() {
		if (Keyboard.pressed(Keys.HELP)) {
			showHelp();
		} else if (Keyboard.pressed(Keys.AUTOPILOT)) {
			Actions.toggleAutopilot();
		} else if (Keyboard.pressed(Keys.BOOT)) {
			Actions.reboot(); //TODO this does not work. Why?
		} else if (Keyboard.pressed(Keys.IMMUNITY)) {
			Actions.toggleImmunity();
		} else if (Keyboard.pressed(Keys.PAUSE)) {
			Actions.togglePaused();
		} else if (Keyboard.pressed(Keys.PAUSE_STEP) || Keyboard.pressed(Keys.SINGLE_STEP)) {
			Actions.oneSimulationStep();
		} else if (Keyboard.pressed(Keys.TEN_STEPS)) {
			Actions.tenSimulationSteps();
		} else if (Keyboard.pressed(Keys.SIMULATION_FASTER)) {
			Actions.changeSimulationSpeed(5);
		} else if (Keyboard.pressed(Keys.SIMULATION_SLOWER)) {
			Actions.changeSimulationSpeed(-5);
		} else if (Keyboard.pressed(Keys.SIMULATION_NORMAL)) {
			Actions.resetSimulationSpeed();
		} else if (Keyboard.pressed(Keys.QUIT)) {
			Actions.restartIntro();
		} else if (Keyboard.pressed(Keys.TEST_LEVELS)) {
			Actions.startLevelTestMode();
		}
		currentGameScene.handleKeyboardInput();
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
		var sounds = AppRes.Sounds.gameSounds(event.game.variant());
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
		default: {
			// ignore
		}
		}
	}

	public GameController gameController() {
		return gameController;
	}

	public GameModel game() {
		return gameController.game();
	}

	public Scene mainScene() {
		return stage.getScene();
	}

	public GameScene currentGameScene() {
		return currentGameScene;
	}

	public FlashMessageView flashMessageView() {
		return flashMessageView;
	}

	public ContextSensitiveHelp csHelp() {
		return csHelp;
	}
}