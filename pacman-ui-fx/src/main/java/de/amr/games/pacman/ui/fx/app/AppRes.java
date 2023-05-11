/*
MIT License

Copyright (c) 2023 Armin Reichert

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

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.rendering2d.*;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import de.amr.games.pacman.ui.fx.util.Picker;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import dev.webfx.kit.util.scene.DeviceSceneUtil;
import dev.webfx.platform.util.collection.Collections;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author Armin Reichert
 */
public class AppRes {

	public static final ResourceManager Manager = new ResourceManager("/de/amr/games/pacman/ui/fx/assets/"/*,
			AppRes.class::getResource*/);

	public static void load() {
		long start = System.nanoTime();
		load("graphics", Graphics::load);
		load("sounds", Sounds::load);
		load("fonts", Fonts::load);
		load("texts", Texts::load);
		Logger.info("Loading application resources took {} seconds.", (System.nanoTime() - start) / 1e9f);
	}

	private static void load(String section, Runnable loadingCode) {
		long start = System.nanoTime();
		loadingCode.run();
		Logger.info("Loading {} done ({} seconds).", section, (System.nanoTime() - start) / 1e9f);
	}

	public static class ArcadeTheme {
		public static final Color RED = Color.rgb(255, 0, 0);
		public static final Color YELLOW = Color.rgb(255, 255, 0);
		public static final Color PINK = Color.rgb(252, 181, 255);
		public static final Color CYAN = Color.rgb(0, 255, 255);
		public static final Color ORANGE = Color.rgb(251, 190, 88);
		public static final Color BLACK = Color.rgb(0, 0, 0);
		public static final Color BLUE = Color.rgb(33, 33, 255);
		public static final Color PALE = Color.rgb(222, 222, 255);
		public static final Color ROSE = Color.rgb(252, 187, 179);

		public static final GhostColoring[] GHOST_COLORING = new GhostColoring[4];

		//@formatter:off
		static {
			GHOST_COLORING[GameModel.RED_GHOST] = new GhostColoring(
				RED,  PALE, BLUE, // normal
				BLUE, ROSE, ROSE, // frightened
				PALE, ROSE, RED   // flashing
			);

			GHOST_COLORING[GameModel.PINK_GHOST] = new GhostColoring(
				PINK, PALE, BLUE, // normal
				BLUE, ROSE, ROSE, // frightened
				PALE, ROSE, RED   // flashing
			);

			GHOST_COLORING[GameModel.CYAN_GHOST] = new GhostColoring(
				CYAN, PALE, BLUE, // normal
				BLUE, ROSE, ROSE, // frightened
				PALE, ROSE, RED   // flashing
			);
			
			GHOST_COLORING[GameModel.ORANGE_GHOST] = new GhostColoring(
				ORANGE, PALE, BLUE, // normal
				BLUE,   ROSE, ROSE, // frightened
				PALE,   ROSE, RED   // flashing
			);
		}

		public static final MazeColoring PACMAN_MAZE_COLORS = new MazeColoring(//
				Color.rgb(254, 189, 180), // food color
				Color.rgb(33, 33, 255).darker(), // wall top color
				Color.rgb(33, 33, 255).brighter(), // wall side color
				Color.rgb(252, 181, 255) // ghosthouse door color
		);

		public static final MazeColoring[] MS_PACMAN_MAZE_COLORS = {
			new MazeColoring(Color.rgb(222, 222, 255), Color.rgb(255, 183, 174),  Color.rgb(255,   0,   0), Color.rgb(255, 183, 255)),
			new MazeColoring(Color.rgb(255, 255, 0),   Color.rgb( 71, 183, 255),  Color.rgb(222, 222, 255), Color.rgb(255, 183, 255)),
			new MazeColoring(Color.rgb(255,   0, 0),   Color.rgb(222, 151,  81),  Color.rgb(222, 222, 255), Color.rgb(255, 183, 255)),
			new MazeColoring(Color.rgb(222, 222, 255), Color.rgb( 33,  33, 255),  Color.rgb(255, 183,  81), Color.rgb(255, 183, 255)),
			new MazeColoring(Color.rgb(0,   255, 255), Color.rgb(255, 183, 255),  Color.rgb(255, 255,   0), Color.rgb(255, 183, 255)),
			new MazeColoring(Color.rgb(222, 222, 255), Color.rgb(255, 183, 174),  Color.rgb(255,   0,   0), Color.rgb(255, 183, 255)),
		};

		public static final PacManColoring PACMAN_COLORING = new PacManColoring(
			Color.rgb(255, 255, 0), // head
			Color.rgb(191, 79, 61), // palate
			Color.rgb(33, 33, 33)   // eyes
		);

		public static final MsPacManColoring MS_PACMAN_COLORING = new MsPacManColoring(
			Color.rgb(255, 255, 0), // head
			Color.rgb(191, 79, 61), // palate
			Color.rgb(33, 33, 33),  // eyes
			Color.rgb(255, 0, 0),   // hair bow
			Color.rgb(33, 33, 255)  // hair bow pearls
		);
		//@formatter:on
	}

	public static class Fonts {

		public static Font arcade;
		public static Font handwriting;

		static void load() {
			arcade = Manager.font("fonts/emulogic.ttf", 8);
			handwriting = Manager.font("fonts/RockSalt-Regular.ttf", 8);
		}

		public static Font font(Font font, double size) {
			return Font.font(font.getFamily(), size);
		}

	}

	public static class Texts {

		public static Map<String, String> messageBundle;
		private static Picker<String> messagePickerCheating;

		static void load() {
			// ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.assets.texts.messages")
			messageBundle = Manager.loadBundle();
			messagePickerCheating = Manager.createPicker(messageBundle, "cheating");
		}

		/**
		 * Builds a resource key from the given key pattern and arguments and reads the corresponding message from the
		 * messages resource bundle.
		 * 
		 * @param keyPattern message key pattern
		 * @param args       arguments merged into key pattern
		 * @return message text for composed key or string indicating missing text
		 */
		public static String message(String keyPattern, Object... args) {
			try {
				var pattern = messageBundle.get(keyPattern);
				for (int i= 0; i < args.length; i++) pattern = pattern.replaceAll("\\{" + i + "\\D*}", args[i].toString());
				return pattern; //return MessageFormat.format(pattern, args);
			} catch (Exception x) {
				Logger.error("No text resource found for key '{}'", keyPattern);
				return "missing{%s}"/*.formatted(keyPattern)*/;
			}
		}

		public static String pickCheatingMessage() {
			return messagePickerCheating.next();
		}
	}

	public static class Graphics {

		public static Image msPacManCabinet;
		public static Image wallpaper;
		public static Image iconHelp;

		public static Image iconPacManGame;
		public static Spritesheet spritesheetPacManGame;
		public static Image fullMazePacManGame;
		public static Image emptyMazePacManGame;
		public static Image flashingMazePacManGame;

		public static Image iconMsPacManGame;
		public static Spritesheet spritesheetMsPacManGame;
		public static Image logoMsPacManGame;
		public static Image[] flashingMazesMsPacManGame;

		private static Image emptyMazeMsPacManGame(int i) {
			return spritesheetMsPacManGame.subImage(228, 248 * i, 226, 248);
		}

		private static Image makeFlashingMazeFromEmptyMaze(int i) {
			Color k1 = ArcadeTheme.MS_PACMAN_MAZE_COLORS[i].wallBaseColor();
			Color k2 = ArcadeTheme.MS_PACMAN_MAZE_COLORS[i].wallTopColor();
			return Ufx.colorsExchanged(emptyMazeMsPacManGame(i), Collections.mapOf(k1, Color.WHITE, k2, Color.BLACK));
		}

		static void load() {
			msPacManCabinet = Manager.image("graphics/mspacman/cabinet.jpg");
			wallpaper = Manager.image("graphics/icons/pacman_wallpaper_gray.png"); //TODO not an icon
			iconHelp = Manager.image("graphics/icons/ghost-help-icon.png");

			iconPacManGame = Manager.image("graphics/icons/pacman.png");
			spritesheetPacManGame = new Spritesheet(Manager.image("graphics/pacman/sprites.png"), 16);
			fullMazePacManGame = Manager.image("graphics/pacman/maze_full.png");
			emptyMazePacManGame = Manager.image("graphics/pacman/maze_empty.png");
			flashingMazePacManGame = Manager.image("graphics/pacman/maze_empty_flashing.png");

			iconMsPacManGame = Manager.image("graphics/icons/mspacman.png");
			Image sprites = Manager.image("graphics/mspacman/sprites.png");
			spritesheetMsPacManGame = new Spritesheet(sprites, 16);
			//TODO check if this works
			DeviceSceneUtil.onImagesLoaded(() -> flashingMazesMsPacManGame = IntStream.range(0, 6).mapToObj(Graphics::makeFlashingMazeFromEmptyMaze)
					.toArray(Image[]::new)
					, sprites);
			logoMsPacManGame = Manager.image("graphics/mspacman/midway.png");
		}
	}

	public static class Sounds {

		public static final AudioClip VOICE_HELP = Manager.audioClip("sound/voice/press-key.mp3");
		public static final AudioClip VOICE_AUTOPILOT_OFF = Manager.audioClip("sound/voice/autopilot-off.mp3");
		public static final AudioClip VOICE_AUTOPILOT_ON = Manager.audioClip("sound/voice/autopilot-on.mp3");
		public static final AudioClip VOICE_IMMUNITY_OFF = Manager.audioClip("sound/voice/immunity-off.mp3");
		public static final AudioClip VOICE_IMMUNITY_ON = Manager.audioClip("sound/voice/immunity-on.mp3");

		//@formatter:off
		private static final Object[][] MS_PACMAN_AUDIO_CLIP_PATHS = { 
				{ AudioClipID.BONUS_EATEN,     "sound/mspacman/Fruit.mp3", 1.0 }, 
				{ AudioClipID.CREDIT,          "sound/mspacman/Credit.mp3", 1.0 }, 
				{ AudioClipID.EXTRA_LIFE,      "sound/mspacman/ExtraLife.mp3", 1.0 }, 
				{ AudioClipID.GAME_READY,      "sound/mspacman/Start.mp3", 1.0 }, 
				{ AudioClipID.GAME_OVER,       "sound/common/game-over.mp3", 1.0 }, 
				{ AudioClipID.GHOST_EATEN,     "sound/mspacman/Ghost.mp3", 1.0 }, 
				{ AudioClipID.GHOST_RETURNING, "sound/mspacman/GhostEyes.mp3", 1.0 }, 
				{ AudioClipID.INTERMISSION_1,  "sound/mspacman/Act1TheyMeet.mp3", 1.0 }, 
				{ AudioClipID.INTERMISSION_2,  "sound/mspacman/Act2TheChase.mp3", 1.0 }, 
				{ AudioClipID.INTERMISSION_3,  "sound/mspacman/Act3Junior.mp3", 1.0 }, 
				{ AudioClipID.LEVEL_COMPLETE,  "sound/common/level-complete.mp3", 1.0 }, 
				{ AudioClipID.PACMAN_DEATH,    "sound/mspacman/Died.mp3", 1.0 }, 
				{ AudioClipID.PACMAN_MUNCH,    "sound/mspacman/Pill.wav", 1.0 }, 
				{ AudioClipID.PACMAN_POWER,    "sound/mspacman/ScaredGhost.mp3", 1.0 }, 
				{ AudioClipID.SIREN_1,         "sound/mspacman/GhostNoise1.wav", 1.0 }, 
				{ AudioClipID.SIREN_2,         "sound/mspacman/GhostNoise1.wav", 1.0 }, 
				{ AudioClipID.SIREN_3,         "sound/mspacman/GhostNoise1.wav", 1.0 }, 
				{ AudioClipID.SIREN_4,         "sound/mspacman/GhostNoise1.wav", 1.0 }, 
				{ AudioClipID.SWEEP,           "sound/common/sweep.mp3", 1.0 }, 
		};

		private static final Object[][] PACMAN_AUDIO_CLIP_PATHS = { 
				{ AudioClipID.BONUS_EATEN,     "sound/pacman/eat_fruit.mp3", 1.0 }, 
				{ AudioClipID.CREDIT,          "sound/pacman/credit.wav", 1.0 }, 
				{ AudioClipID.EXTRA_LIFE,      "sound/pacman/extend.mp3", 1.0 }, 
				{ AudioClipID.GAME_READY,      "sound/pacman/game_start.mp3", 1.0 }, 
				{ AudioClipID.GAME_OVER,       "sound/common/game-over.mp3", 1.0 }, 
				{ AudioClipID.GHOST_EATEN,     "sound/pacman/eat_ghost.mp3", 1.0 }, 
				{ AudioClipID.GHOST_RETURNING, "sound/pacman/retreating.mp3", 1.0 }, 
				{ AudioClipID.INTERMISSION_1,  "sound/pacman/intermission.mp3", 1.0 }, 
				{ AudioClipID.LEVEL_COMPLETE,  "sound/common/level-complete.mp3", 1.0 }, 
				{ AudioClipID.PACMAN_DEATH,    "sound/pacman/pacman_death.wav", 1.0 }, 
				{ AudioClipID.PACMAN_MUNCH,    "sound/pacman/doublemunch.wav", 1.0 }, 
				{ AudioClipID.PACMAN_POWER,    "sound/pacman/ghost-turn-to-blue.mp3", 1.0 }, 
				{ AudioClipID.SIREN_1,         "sound/pacman/siren_1.mp3", 1.0 }, 
				{ AudioClipID.SIREN_2,         "sound/pacman/siren_2.mp3", 1.0 }, 
				{ AudioClipID.SIREN_3,         "sound/pacman/siren_3.mp3", 1.0 }, 
				{ AudioClipID.SIREN_4,         "sound/pacman/siren_4.mp3", 1.0 }, 
				{ AudioClipID.SWEEP,           "sound/common/sweep.mp3", 1.0 }, 
		};
		//@formatter:on

		private static GameSounds gameSoundsMsPacMan;
		private static GameSounds gameSoundsPacMan;

		static void load() {
			gameSoundsMsPacMan = new GameSounds(MS_PACMAN_AUDIO_CLIP_PATHS, true);
			gameSoundsPacMan = new GameSounds(PACMAN_AUDIO_CLIP_PATHS, true);
		}

		public static GameSounds gameSounds(GameVariant variant) {
			switch (variant) {
				case MS_PACMAN: return gameSoundsMsPacMan;
				case PACMAN: return gameSoundsPacMan;
				default: throw new IllegalGameVariantException(variant);
			}
		}
	}
}