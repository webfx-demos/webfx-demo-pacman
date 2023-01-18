/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.ui.fx.shell;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.Actions;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.PacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneManager;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import de.amr.games.pacman.ui.fx.util.GameLoop;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.KeyboardSteering;
import de.amr.games.pacman.ui.fx.util.Modifier;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * JavaFX UI for Pac-Man and Ms. Pac-Man game.
 * <p>
 * The play scene is available in 2D and 3D. All others scenes are 2D only.
 * 
 * @author Armin Reichert
 */
public class GameUI implements GameEventListener {

	private static final Logger LOGGER = LogManager.getFormatterLogger();
	private static final Image APP_ICON_PACMAN = Ufx.image("icons/pacman.png");
	private static final Image APP_ICON_MSPACMAN = Ufx.image("icons/mspacman.png");

	private GameController gameController;
	private Stage stage;
	private final GameLoop gameLoop = new GameLoop(GameModel.FPS);
	private final GameSceneManager sceneManager = new GameSceneManager();
	private Scene mainScene;
	private Group gameSceneParent;
	private BorderPane overlayPane;
	private Dashboard dashboard;
	private FlashMessageView flashMessageView;
	private PiPView pipView;
	private KeyboardSteering kbSteering;

	private GameScene currentGameScene;

	public GameUI(GameController gameController, Stage primaryStage, float zoom, boolean fullScreen) {
		this.gameController = Objects.requireNonNull(gameController);
		this.stage = Objects.requireNonNull(primaryStage);
		this.kbSteering = new KeyboardSteering(KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT);

		gameController.setManualPacSteering(kbSteering);

		Keyboard.addHandler(this::onKeyPressed);
		GameEvents.addListener(this);
		Actions.setUI(this);

		Env.drawModePy.addListener((property, oldVal, newVal) -> updateMainSceneBackground());
		Env.bgColorPy.addListener((property, oldVal, newVal) -> updateMainSceneBackground());
		Env.pausedPy.addListener((property, oldVal, newVal) -> updateStageTitle());

		createMainScene(zoom);
		configureStage(fullScreen);
		configureGameLoop();
	}

	public void start() {
		gameController.boot();
		stage.show();
		playGreetingVoice();
		gameLoop().start();
	}

	public void playGreetingVoice() {
		Ufx.pause(0.5, Actions::playHelpVoiceMessage).play();
	}

	private void configureStage(boolean fullScreen) {
		stage.setFullScreen(fullScreen);
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.setOnCloseRequest(e -> {
			gameLoop.stop();
			LOGGER.info("Game loop stopped. Application closed.");
		});
		stage.setScene(mainScene);
		stage.centerOnScreen();
	}

	private void createMainScene(float zoom) {
		if (zoom <= 0) {
			throw new IllegalArgumentException("Zoom value must be positive, but is " + zoom);
		}

		gameSceneParent = new Group(); // single child is current game scenes' JavaFX subscene
		flashMessageView = new FlashMessageView();
		overlayPane = new BorderPane();
		dashboard = new Dashboard(this);
		pipView = new PiPView();
		overlayPane.setLeft(dashboard);
		overlayPane.setRight(new VBox(pipView));

		var root = new StackPane();
		root.getChildren().addAll(gameSceneParent, flashMessageView, overlayPane);

		var size = ArcadeWorld.SIZE_PX.toFloatVec().scaled(zoom);
		mainScene = new Scene(root, size.x(), size.y());

		mainScene.setOnKeyPressed(Keyboard::processEvent);
		mainScene.addEventHandler(KeyEvent.KEY_PRESSED, kbSteering::onKeyPressed);
		mainScene.heightProperty().addListener((heightPy, oldHeight, newHeight) -> {
			if (currentGameScene instanceof GameScene2D scene2D) {
				scene2D.resizeToHeight(newHeight.floatValue());
			}
		});
	}

	private void updateMainSceneBackground() {
		var bgColor = Env.drawModePy.get() == DrawMode.LINE ? Color.BLACK : Env.bgColorPy.get();
		var sceneRoot = (Region) mainScene.getRoot();
		sceneRoot.setBackground(Ufx.colorBackground(bgColor));
	}

	private void configureGameLoop() {
		gameLoop.setUpdateTask(() -> {
			gameController.update();
			currentGameScene.onTick();
			Keyboard.clear();
		});
		gameLoop.setRenderTask(() -> {
			flashMessageView.update();
			dashboard.update();
			pipView.update();
			pipView.setVisible(Env.pipEnabledPy.get() && sceneManager.isPlayScene(currentGameScene));
		});
		gameLoop.pausedPy.bind(Env.pausedPy);
		gameLoop.targetFrameratePy.bind(Env.targetFrameratePy);
		gameLoop.measuredPy.bind(Env.timeMeasuredPy);
	}

	// public visible such that Actions class can call it
	public void updateGameScene(boolean forcedReload) {
		int dim = Env.threeDScenesPy.get() ? 3 : 2;
		var gameScene = sceneManager.selectGameScene(gameController, dim, currentGameScene, forcedReload);
		if (gameScene != currentGameScene) {
			setGameScene(gameScene);
			gameController.setSounds(Env.SOUND_DISABLED ? GameSounds.NO_SOUNDS : sounds());
			pipView.init(gameScene.ctx());
		}
	}

	private void updateStageTitle() {
		var pausedText = Env.pausedPy.get() ? " (paused)" : "";
		switch (gameController.game().variant()) {
		case MS_PACMAN -> {
			stage.setTitle("Ms. Pac-Man" + pausedText);
			stage.getIcons().setAll(APP_ICON_MSPACMAN);
		}
		case PACMAN -> {
			stage.setTitle("Pac-Man" + pausedText);
			stage.getIcons().setAll(APP_ICON_PACMAN);
		}
		default -> throw new IllegalStateException();
		}
	}

	private Rendering2D renderer() {
		return switch (gameController.game().variant()) {
		case MS_PACMAN -> MsPacManGameRenderer.THE_ONE_AND_ONLY;
		case PACMAN -> PacManGameRenderer.THE_ONE_AND_ONLY;
		default -> throw new IllegalStateException();
		};
	}

	private GameSounds sounds() {
		return switch (gameController.game().variant()) {
		case MS_PACMAN -> GameSounds.MS_PACMAN_SOUNDS;
		case PACMAN -> GameSounds.PACMAN_SOUNDS;
		default -> throw new IllegalStateException();
		};
	}

	private void setGameScene(GameScene gameScene) {
		currentGameScene = gameScene;
		gameSceneParent.getChildren().setAll(gameScene.fxSubScene());
		gameScene.embedInto(stage.getScene());
		updateMainSceneBackground();
		updateStageTitle();
		LOGGER.trace("Game scene is now %s", gameScene);
	}

	@Override
	public void onGameEvent(GameEvent event) {
		GameEventListener.super.onGameEvent(event);
		currentGameScene.onGameEvent(event);
		LOGGER.trace("Game UI received game event %s", event);
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
		gameController.game().level().ifPresent(level -> {
			var r = renderer();
			level.pac().setAnimations(r.createPacAnimations(level.pac()));
			level.ghosts().forEach(ghost -> ghost.setAnimations(r.createGhostAnimations(ghost)));
			if (level.world() instanceof ArcadeWorld arcadeWorld) {
				arcadeWorld.setFlashingAnimation(r.createMazeFlashingAnimation());
			}
		});
		updateGameScene(true);
	}

	private void onKeyPressed() {
		if (Keyboard.pressed(Modifier.ALT, KeyCode.A)) {
			Actions.toggleAutopilot();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.B)) {
			Actions.reboot();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.D)) {
			Env.toggle(Env.showDebugInfoPy);
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.I)) {
			Actions.toggleImmunity();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.M)) {
			Actions.toggleSoundMuted();
		} else if (Keyboard.pressed(KeyCode.P)) {
			Actions.togglePaused();
		} else if (Keyboard.pressed(Modifier.SHIFT, KeyCode.P) || Keyboard.pressed(KeyCode.SPACE)) {
			Actions.oneSimulationStep();
		} else if (Keyboard.pressed(Modifier.SHIFT, KeyCode.SPACE)) {
			Actions.tenSimulationSteps();
		} else if (Keyboard.pressed(KeyCode.Q)) {
			Actions.restartIntro();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.T)) {
			Actions.toggleLevelTestMode();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.DIGIT3)) {
			Actions.toggleUse3DScene();
		} else if (Keyboard.pressed(KeyCode.F1)) {
			Actions.toggleDashboardVisible();
		} else if (Keyboard.pressed(KeyCode.F2)) {
			Actions.togglePipViewVisible();
		} else if (Keyboard.pressed(KeyCode.F3)) {
			Actions.reboot();
		} else if (Keyboard.pressed(KeyCode.F11)) {
			stage.setFullScreen(true);
		}
		currentGameScene.onKeyPressed();
	}

	public GameController gameController() {
		return gameController;
	}

	public Scene mainScene() {
		return stage.getScene();
	}

	public GameSceneManager sceneManager() {
		return sceneManager;
	}

	public GameLoop gameLoop() {
		return gameLoop;
	}

	public GameScene currentGameScene() {
		return currentGameScene;
	}

	public FlashMessageView flashMessageView() {
		return flashMessageView;
	}

	public Dashboard dashboard() {
		return dashboard;
	}
}