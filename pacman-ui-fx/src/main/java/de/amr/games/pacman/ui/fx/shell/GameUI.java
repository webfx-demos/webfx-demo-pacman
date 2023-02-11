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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.event.SoundEvent;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.PacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.scene.GameSceneManager;
import de.amr.games.pacman.ui.fx.sound.common.SoundClip;
import de.amr.games.pacman.ui.fx.sound.common.GameSounds;
import de.amr.games.pacman.ui.fx.sound.mspacman.MsPacManSoundMap;
import de.amr.games.pacman.ui.fx.sound.pacman.PacManSoundMap;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
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

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final Image APP_ICON_PACMAN = ResourceMgr.image("icons/pacman.png");
	private static final Image APP_ICON_MSPACMAN = ResourceMgr.image("icons/mspacman.png");

	public static final double PIP_VIEW_MIN_HEIGHT = ArcadeWorld.SIZE_PX.y();
	public static final double PIP_VIEW_MAX_HEIGHT = ArcadeWorld.SIZE_PX.y() * 2;

	private static final GameSounds MS_PACMAN_SOUNDS = new GameSounds("Ms. Pac-Man Sounds", MsPacManSoundMap.map());
	private static final GameSounds PACMAN_SOUNDS = new GameSounds("Pac-Man Sounds", PacManSoundMap.map());

	public static GameSounds sounds(GameModel game) {
		return switch (game.variant()) {
		case MS_PACMAN -> MS_PACMAN_SOUNDS;
		case PACMAN -> PACMAN_SOUNDS;
		default -> throw new IllegalStateException();
		};
	}

	private static GameRenderer renderer(GameModel game) {
		return switch (game.variant()) {
		case MS_PACMAN -> MsPacManGameRenderer.THE_ONE_AND_ONLY;
		case PACMAN -> PacManGameRenderer.THE_ONE_AND_ONLY;
		default -> throw new IllegalStateException();
		};
	}

	public class Simulation extends GameLoop {

		public Simulation(int fps) {
			super(fps);
		}

		@Override
		public void doUpdate() {
			gameController.update();
			currentGameScene.onTick();
			Keyboard.clear();
		}

		@Override
		public void doRender() {
			flashMessageView.update();
			dashboard.update();
			updatePipView();
		}
	}

	private final Simulation simulation = new Simulation(GameModel.FPS);
	private final GameController gameController;
	private final Stage stage;
	private final Dashboard dashboard = new Dashboard();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	/**
	 * Embedded 2D-view of the current play scene. Activated/deactivated by pressing key F2. Size and transparency can be
	 * controlled using the dashboard.
	 */
	private final PlayScene2D pipPlayScene = new PlayScene2D();
	private final Settings settings;

	private Scene mainScene;
	private GameScene currentGameScene;

	public GameUI(Stage primaryStage, Settings settings) {
		this.settings = settings;
		gameController = new GameController(settings.variant);

		initEnv();

		stage = primaryStage;
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.setFullScreen(settings.fullScreen);
		createMainScene(ArcadeWorld.SIZE_PX.x(), ArcadeWorld.SIZE_PX.y(), settings.zoom);
		stage.setScene(mainScene);

		// keyboard steering of Pac-Man
		var defaultPacSteering = new KeyboardSteering(Keys.PAC_UP, Keys.PAC_DOWN, Keys.PAC_LEFT, Keys.PAC_RIGHT);
		gameController.setManualPacSteering(defaultPacSteering);

		mainScene.addEventHandler(KeyEvent.KEY_PRESSED, defaultPacSteering::onKeyPressed);
		mainScene.setOnKeyPressed(e -> {
			if (Keyboard.accept(e)) {
				onKeyPressed();
			}
		});
		LOG.info("Created game UI, Application settings: %s", settings);
	}

	private void createMainScene(int width, int height, float zoom) {
		if (zoom <= 0) {
			throw new IllegalArgumentException("Zoom value must be positive but is: %.2f".formatted(zoom));
		}
		var overlayPane = new BorderPane();
		overlayPane.setLeft(dashboard);
		overlayPane.setRight(pipPlayScene.fxSubScene());
		var placeHolder = new Pane(); /* placeholder for current game scene */
		var root = new StackPane(placeHolder, flashMessageView, overlayPane);
		mainScene = new Scene(root, width * zoom, height * zoom);
		mainScene.heightProperty()
				.addListener((heightPy, oldHeight, newHeight) -> currentGameScene.resizeToHeight(newHeight.floatValue()));
	}

	private void initEnv() {
		Env.mainSceneBgColorPy.addListener((py, oldVal, newVal) -> updateMainSceneBackground());

		Env.ThreeD.drawModePy.addListener((py, oldVal, newVal) -> updateMainSceneBackground());
		Env.ThreeD.enabledPy.set(settings.use3D);
		Env.ThreeD.perspectivePy.set(settings.perspective);

		Env.PiP.sceneHeightPy.addListener((py, oldVal, newVal) -> pipPlayScene.resizeToHeight(newVal.doubleValue()));
		pipPlayScene.fxSubScene().opacityProperty().bind(Env.PiP.opacityPy);

		Env.Simulation.pausedPy.addListener((py, oldVal, newVal) -> updateStageFrame());
		simulation.pausedPy.bind(Env.Simulation.pausedPy);
		simulation.targetFrameratePy.bind(Env.Simulation.targetFrameratePy);
		simulation.measuredPy.bind(Env.Simulation.timeMeasuredPy);
	}

	public void start() {
		if (simulation.isRunning()) {
			LOG.info("Game has already been started");
			return;
		}
		GameEvents.addListener(this);
		Actions.setUI(this);
		dashboard.init(this);
		gameController.boot(); // after booting, current game scene is initialized
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();

		Ufx.afterSeconds(1.0, Actions::playHelpVoiceMessage).play();

		simulation.start();
		LOG.info("Game started. Game loop target frame rate: %d", simulation.targetFrameratePy.get());
		LOG.info("Window size: %.0f x %.0f, 3D: %s, perspective: %s".formatted(stage.getWidth(), stage.getHeight(),
				U.onOff(Env.ThreeD.enabledPy.get()), Env.ThreeD.perspectivePy.get()));
	}

	public void stop() {
		simulation.stop();
		LOG.info("Game stopped");
	}

	private void updateStageFrame() {
		var paused = Env.Simulation.pausedPy.get() ? " (paused)" : "";
		switch (gameController.game().variant()) {
		case MS_PACMAN -> {
			stage.setTitle("Ms. Pac-Man" + paused);
			stage.getIcons().setAll(APP_ICON_MSPACMAN);
		}
		case PACMAN -> {
			stage.setTitle("Pac-Man" + paused);
			stage.getIcons().setAll(APP_ICON_PACMAN);
		}
		default -> throw new IllegalStateException();
		}
	}

	private void updateMainSceneBackground() {
		var bgColor = Env.ThreeD.drawModePy.get() == DrawMode.LINE ? Color.BLACK : Env.mainSceneBgColorPy.get();
		var sceneRoot = (Region) mainScene.getRoot();
		sceneRoot.setBackground(ResourceMgr.colorBackground(bgColor));
	}

	private void updatePipView() {
		boolean visible = Env.PiP.visiblePy.get() && GameSceneManager.isPlayScene(currentGameScene);
		pipPlayScene.fxSubScene().setVisible(visible);
		if (visible) {
			pipPlayScene.setContext(currentGameScene.context());
			pipPlayScene.draw();
		}
	}

	// public visible such that Actions class can call it
	public void updateGameScene(boolean reload) {
		var use3D = Env.ThreeD.enabledPy.get();
		var variants = GameSceneManager.getSceneVariantsMatchingGameState(gameController);
		var nextGameScene = (use3D && variants.scene3D() != null) ? variants.scene3D() : variants.scene2D();
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found.");
		}
		if (reload || nextGameScene != currentGameScene) {
			changeGameScene(nextGameScene);
		}
		updateMainSceneBackground();
		updateStageFrame();
	}

	private void changeGameScene(GameScene nextGameScene) {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		nextGameScene.setContext(new GameSceneContext(gameController, renderer(gameController.game())));
		nextGameScene.init();
		currentGameScene = nextGameScene;
		// embed game scene into main scene
		StackPane root = (StackPane) mainScene.getRoot();
		root.getChildren().set(0, currentGameScene.fxSubScene());
		currentGameScene.onEmbed(mainScene);
	}

	private void onKeyPressed() {
		if (Keyboard.pressed(Keys.AUTOPILOT)) {
			Actions.toggleAutopilot();
		} else if (Keyboard.pressed(Keys.BOOT)) {
			Actions.reboot();
		} else if (Keyboard.pressed(Keys.DEBUG_INFO)) {
			Ufx.toggle(Env.showDebugInfoPy);
		} else if (Keyboard.pressed(Keys.IMMUNITIY)) {
			Actions.toggleImmunity();
		} else if (Keyboard.pressed(Keys.MUTE)) {
//			Actions.toggleSoundMuted();
		} else if (Keyboard.pressed(Keys.PAUSE)) {
			Actions.togglePaused();
		} else if (Keyboard.pressed(Keys.PAUSE_STEP) || Keyboard.pressed(Keys.SINGLE_STEP)) {
			Actions.oneSimulationStep();
		} else if (Keyboard.pressed(Keys.TEN_STEPS)) {
			Actions.tenSimulationSteps();
		} else if (Keyboard.pressed(Keys.QUIT)) {
			Actions.restartIntro();
		} else if (Keyboard.pressed(Keys.TEST_LEVELS)) {
			Actions.toggleLevelTestMode();
		} else if (Keyboard.pressed(Keys.USE_3D)) {
			Actions.toggleUse3DScene();
		} else if (Keyboard.pressed(Keys.DASHBOARD)) {
			Actions.toggleDashboardVisible();
		} else if (Keyboard.pressed(Keys.PIP_VIEW)) {
			Actions.togglePipViewVisible();
		} else if (Keyboard.pressed(Keys.FULLSCREEN)) {
			stage.setFullScreen(true);
		}
		currentGameScene.onKeyPressed();
	}

	@Override
	public void onGameEvent(GameEvent event) {
		LOG.trace("Event received: %s", event);
		// call event specific handler
		GameEventListener.super.onGameEvent(event);
		if (currentGameScene != null) {
			currentGameScene.onGameEvent(event);
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
	public void onSoundEvent(SoundEvent event) {
		var sounds = sounds(event.game);
		switch (event.soundCommand) {
		case "bonus_eaten" -> sounds.play(SoundClip.BONUS_EATEN);
		case "credit_added" -> sounds.play(SoundClip.CREDIT);
		case "extra_life" -> sounds.play(SoundClip.EXTRA_LIFE);
		case "ghost_eaten" -> sounds.play(SoundClip.GHOST_EATEN);
		case "hunting_phase_started_0" -> sounds.ensureSirenStarted(0);
		case "hunting_phase_started_2" -> sounds.ensureSirenStarted(1);
		case "hunting_phase_started_4" -> sounds.ensureSirenStarted(2);
		case "hunting_phase_started_6" -> sounds.ensureSirenStarted(3);
		case "ready_to_play" -> sounds.play(SoundClip.GAME_READY);
		case "pacman_death" -> sounds.play(SoundClip.PACMAN_DEATH);
		case "pacman_found_food" -> sounds.ensureLoop(SoundClip.PACMAN_MUNCH, AudioClip.INDEFINITE);
		case "pacman_power_starts" -> {
			sounds.stopSirens();
			sounds.ensureLoop(SoundClip.PACMAN_POWER, AudioClip.INDEFINITE);
		}
		case "pacman_power_ends" -> {
			sounds.stop(SoundClip.PACMAN_POWER);
			gameController.game().level().ifPresent(level -> sounds.ensureSirenStarted(level.huntingPhase() / 2));
		}
		case "start_intermission_1" -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> sounds.play(SoundClip.INTERMISSION_1);
			case PACMAN -> sounds.loop(SoundClip.INTERMISSION_1, 2);
			default -> throw new IllegalArgumentException();
			}
		}
		case "start_intermission_2" -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> sounds.play(SoundClip.INTERMISSION_2);
			case PACMAN -> sounds.play(SoundClip.INTERMISSION_1);
			default -> throw new IllegalArgumentException();
			}
		}
		case "start_intermission_3" -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> sounds.play(SoundClip.INTERMISSION_3);
			case PACMAN -> sounds.loop(SoundClip.INTERMISSION_1, 2);
			default -> throw new IllegalArgumentException();
			}
		}
		case "stop_all_sounds" -> sounds.stopAll();
		default -> {
			// ignore
		}
		}
	}

	@Override
	public void onLevelStarting(GameEvent e) {
		gameController.game().level().ifPresent(level -> {
			var r = currentGameScene.context().r2D();
			level.pac().setAnimations(r.createPacAnimations(level.pac()));
			level.ghosts().forEach(ghost -> ghost.setAnimations(r.createGhostAnimations(ghost)));
			level.world().addAnimation("flashing", r.createMazeFlashingAnimation());
			LOG.trace("Created level animations for level #%d", level.number());
		});
		updateGameScene(true);
	}

	public GameController gameController() {
		return gameController;
	}

	public Scene mainScene() {
		return stage.getScene();
	}

	public Simulation simulation() {
		return simulation;
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