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
import de.amr.games.pacman.controller.common.Steering;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventAdapter;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneManager;
import de.amr.games.pacman.ui.fx.shell.info.Dashboard;
import de.amr.games.pacman.ui.fx.util.GameLoop;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.KeyboardSteering;
import de.amr.games.pacman.ui.fx.util.Modifier;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Group;
import javafx.scene.Parent;
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
 * JavaFX UI for Pac-Man / Ms. Pac-Man game.
 * <p>
 * The play scene is available in 2D and 3D. The intro scenes and intermission scenes are all 2D.
 * 
 * @author Armin Reichert
 */
public class GameUI implements GameEventAdapter {

	private static final Logger LOGGER = LogManager.getFormatterLogger();
	private static final Image APP_ICON = Ufx.image("icons/pacman.png");

	private final GameLoop gameLoop = new GameLoop(60);
	private final GameSceneManager sceneManager = new GameSceneManager();
	private final GameController gameController;
	private final Stage stage;
	/** Game scene placeholder, single child will be the game scene's FX subscene. */
	private final Group gameSceneParent = new Group();
	private Dashboard dashboard;
	private FlashMessageView flashMessageView;
	private PiPView pipView;

	private Steering currentSteering;
	private GameScene currentGameScene;

	// In MAME, window is about 4% smaller than the 28x36 aspect ratio. Why?
	public GameUI(GameController gameController, Stage stage, double width, double height) {
		this.gameController = gameController;
		this.stage = stage;
		var mainScene = new Scene(createSceneContent(), width, height);
		mainScene.setOnKeyPressed(Keyboard::processEvent);
		Keyboard.addHandler(this::onKeyPressed);
		GameEvents.addEventListener(this);
		Actions.setUI(this);
		Actions.playHelpMessageAfterSeconds(0.5);
		initGameLoop();
		stage.setScene(mainScene);
		updateGameScene(true);
		stage.setOnCloseRequest(e -> gameLoop.stop());
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.setTitle("Pac-Man / Ms. Pac-Man");
		stage.getIcons().add(APP_ICON);
		stage.centerOnScreen();
		stage.show();
	}

	private Parent createSceneContent() {
		var sceneContent = new StackPane();
		dashboard = new Dashboard();
		dashboard.build(this);
		pipView = new PiPView();
		pipView.heightPy.bind(Env.pipSceneHeightPy);
		pipView.getRoot().opacityProperty().bind(Env.pipOpacityPy);
		var overlayPane = new BorderPane();
		overlayPane.setLeft(dashboard);
		overlayPane.setRight(new VBox(pipView.getRoot()));
		flashMessageView = new FlashMessageView();
		sceneContent.getChildren().addAll(gameSceneParent, flashMessageView, overlayPane);
		Env.drawModePy.addListener((x, y, z) -> updateMainSceneBackground());
		Env.bgColorPy.addListener((x, y, z) -> updateMainSceneBackground());
		return sceneContent;
	}

	private void initGameLoop() {
		gameLoop.setUpdateTask(() -> {
			gameController.update();
			currentGameScene.onTick();
		});
		gameLoop.setRenderTask(() -> {
			flashMessageView.update();
			dashboard.update();
			updatePipView();
		});
		gameLoop.pausedPy.bind(Env.pausedPy);
		gameLoop.targetFrameratePy.bind(Env.targetFrameratePy);
		gameLoop.measuredPy.bind(Env.timeMeasuredPy);
	}

	void updateGameScene(boolean forcedReload) {
		var gameScene = sceneManager.selectGameScene(gameController, currentGameScene, forcedReload);
		if (gameScene != currentGameScene) {
			setGameScene(gameScene);
		}
	}

	private void setGameScene(GameScene gameScene) {
		currentGameScene = gameScene;
		gameSceneParent.getChildren().setAll(gameScene.fxSubScene());
		gameScene.embedInto(stage.getScene());
		updateMainSceneBackground();
		pipView.init(gameScene.ctx());
		LOGGER.info("Game scene is now %s", gameScene);
	}

	private void updateMainSceneBackground() {
		var bgColor = Env.drawModePy.get() == DrawMode.LINE ? Color.BLACK : Env.bgColorPy.get();
		var sceneRoot = (Region) stage.getScene().getRoot();
		sceneRoot.setBackground(Ufx.colorBackground(bgColor));
	}

	@Override
	public void onGameEvent(GameEvent event) {
		GameEventAdapter.super.onGameEvent(event);
		currentGameScene.onGameEvent(event);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		updateGameScene(false);
	}

	@Override
	public void onUIForceUpdate(GameEvent e) {
		updateGameScene(true);
	}

	public void setPacSteering(Steering steering) {
		Objects.requireNonNull(steering);
		if (currentSteering instanceof KeyboardSteering keySteering) {
			stage.getScene().removeEventHandler(KeyEvent.KEY_PRESSED, keySteering::onKeyPressed);
		}
		currentSteering = steering;
		if (steering instanceof KeyboardSteering keySteering) {
			stage.getScene().addEventHandler(KeyEvent.KEY_PRESSED, keySteering::onKeyPressed);
		}
		gameController.setNormalSteering(currentSteering);
	}

	private void updatePipView() {
		if (Env.pipEnabledPy.get() && sceneManager.isPlayScene(currentGameScene)) {
			pipView.getRoot().setVisible(true);
			pipView.draw();
		} else {
			pipView.getRoot().setVisible(false);
		}
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
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.DIGIT3)) {
			Actions.toggleUse3DScene();
		} else if (Keyboard.pressed(KeyCode.F1)) {
			Actions.toggleDashboardVisible();
		} else if (Keyboard.pressed(KeyCode.F2)) {
			Actions.togglePipViewVisible();
		} else if (Keyboard.pressed(KeyCode.F11)) {
			stage.setFullScreen(true);
		}
		currentGameScene.onKeyPressed();
	}

	public GameController gameController() {
		return gameController;
	}

	public Stage stage() {
		return stage;
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