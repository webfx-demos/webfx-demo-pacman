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

package de.amr.games.pacman.ui.fx.util;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class ResourceManager {

	//private final Function<String, URL> urlComputation;
	private final String rootDir;

	public ResourceManager(String rootDir/*, Function<String, URL> urlComputation*/) {
		checkNotNull(rootDir);
		//checkNotNull(urlComputation);
		this.rootDir = rootDir;
		//this.urlComputation = urlComputation;
	}

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @return URL of resource addressed by this path
	 */
	public String urlFromRelPath(String relPath) {
		checkNotNull(relPath);
		return dev.webfx.platform.resource.Resource.toUrl(rootDir + relPath, ResourceManager.class);
		//return urlComputation.apply(rootDir + relPath);
	}

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @return audio clip from resource addressed by this path
	 */
	public AudioClip audioClip(String relPath) {
		return new AudioClip(urlFromRelPath(relPath));
	}

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @param size    font size (must be a positive number)
	 * @return font loaded from resource addressed by this path. If no such font can be loaded, a default font is returned
	 */
	public Font font(String relPath, double size) {
		if (size <= 0) {
			throw new IllegalArgumentException("Font size must be positive but is %.2f"/*.formatted(size)*/);
		}
		var url = urlFromRelPath(relPath);
		var font = Font.loadFont(url, size);
		if (font == null) {
			Logger.error("Font with URL '{}' could not be loaded", url);
			return Font.font(Font.getDefault().getFamily(), size);
		}
		return font;
	}

	private final ArrayList<Image> loadedImages = new ArrayList<>();

	public Image[] getLoadedImages() {
		return loadedImages.toArray(new Image[0]);
	}

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @return image loaded from resource addressed by this path.
	 */
	public Image image(String relPath) {
		Image image = new Image(urlFromRelPath(relPath), true);
		loadedImages.add(image);
		return image;
	}

	public static Background colorBackground(Color color) {
		checkNotNull(color);
		return new Background(new BackgroundFill(color, null, null));
	}

	public static Background imageBackground(Image image) {
		return new Background(new BackgroundImage(image, null, null, null, null));
	}

	public static Color color(Color color, double opacity) {
		checkNotNull(color);
		return Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
	}

	public Picker<String> createPicker(Map<String, String> bundle, String prefix) {
		checkNotNull(bundle);
		return new Picker<>(bundle.keySet().stream()//
				.filter(key -> key.startsWith(prefix))//
				.sorted()//
				.map(bundle::get)//
				.toArray(String[]::new));
	}

	// TODO: doesn't webfx support resource bundles at all?
	public Map<String, String> loadBundle() {
		Map<String, String> map = new HashMap<>();
		map.put("app.title.ms_pacman", "Ms. Pac-Man {0}");
		map.put("app.title.ms_pacman.paused", "Ms. Pac-Man {0} (paused)");
		map.put("app.title.pacman", "Pac-Man {0}");
		map.put("app.title.pacman.paused", "Pac-Man {0} (paused)");
		// help panels
		map.put("help.add_credit"   , "ADD CREDIT");
		map.put("help.autopilot_on" , "AUTOPILOT ON");
		map.put("help.cursor_left"  , "CURSOR LEFT");
		map.put("help.cursor_right" , "CURSOR RIGHT");
		map.put("help.cursor_up"    , "CURSOR UP");
		map.put("help.cursor_down"  , "CURSOR DOWN");
		map.put("help.immunity_on"  , "IMMUNITY ON");
		map.put("help.move_left"    , "MOVE LEFT");
		map.put("help.move_right"   , "MOVE RIGHT");
		map.put("help.move_up"      , "MOVE UP");
		map.put("help.move_down"    , "MOVE DOWN");
		map.put("help.ms_pacman"    , "PLAY MS. PAC-MAN");
		map.put("help.pacman"       , "PLAY PAC-MAN");
		map.put("help.show_help"    , "SHOW HELP");
		map.put("help.show_intro"   , "SHOW INTRO");
		map.put("help.start_game"   , "START GAME");
		// others
		map.put("twoD", "2D");
		map.put("threeD", "3D");
		map.put("pip_on", "Picture-In-Picture ON");
		map.put("pip_off", "Picture-In-Picture OFF");
		map.put("level_complete", "Level {0,number,integer} complete");
		map.put("level_starting", "Starting level {0,number,integer}");
		map.put("camera_perspective", "Perspective: {0}");
		map.put("DRONE", "Drone");
		map.put("FOLLOWING_PLAYER", "Following Player");
		map.put("NEAR_PLAYER", "Near Player");
		map.put("TOTAL", "Total");
		map.put("extra_life", "Extra Life!");
		map.put("autopilot_on", "Autopilot ON");
		map.put("autopilot_off", "Autopilot OFF");
		map.put("sound_on", "Sound ON");
		map.put("sound_off", "Sound OFF");
		map.put("player_immunity_on", "Player is immune");
		map.put("player_immunity_off", "Player is vulnerable");
		map.put("use_2D_scene", "Using 2D Scene");
		map.put("use_3D_scene", "Using 3D Scene");
		map.put("pacman.ready.1", "LET'S GO BRANDON!");
		map.put("pacman.ready.2", "YELLOW MAN BAD!");
		map.put("pacman.ready.3", "C'MON MAN!");
		map.put("pacman.ready.4", "Asufutimaehaehfutbw");
		map.put("pacman.ready.5", "You know,the thing");
		map.put("mspacman.ready.1", "LET'S GO BRANDON!");
		map.put("mspacman.ready.2", "GHOST LIVES MATTER!");
		map.put("mspacman.ready.3", "EAT ME TOO!");
		map.put("mspacman.ready.4", "FIGHT YELLOW PRIVILEGE!");
		map.put("mspacman.ready.5", "CIS GHOST BY NATURE!");
		map.put("mspacman.ready.6", "NO GHOSTSHAMING!");
		map.put("level.complete.1", "Well done!");
		map.put("level.complete.2", "Congratulations!");
		map.put("level.complete.3", "Awesome!");
		map.put("level.complete.4", "You really did it!");
		map.put("level.complete.5", "You're the man*in!");
		map.put("level.complete.6", "WTF!");
		map.put("game.over.1", "You stone cold loser!");
		map.put("game.over.2", "I would say you fucked up!");
		map.put("game.over.3", "This game is OVER!");
		map.put("game.over.4", "Go ahead and cry!");
		map.put("game.over.5", "That's all you've got?");
		map.put("cheating.1", "You old cheating bastard!");
		map.put("cheating.2", "I told you, I will erase your hard disk!");
		map.put("cheating.3", "Cheaters are the worst human beings!");
		map.put("cheating.4", "Do you think I will not notice this?");
		map.put("cheating.5", "Ah, Mr. Super-Clever again");
		map.put("cheating.6", "STOP! CHEATING! NOW!");
		map.put("cheat_add_lives", "You have {0,number,integer} lives now");
		return map;
	}
}