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
public class ResourceMgr {

	//private final Function<String, URL> urlComputation;
	private final String rootDir;

	public ResourceMgr(String rootDir/*, Function<String, URL> urlComputation*/) {
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
		return dev.webfx.platform.resource.Resource.toUrl(rootDir + relPath, ResourceMgr.class);
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

	public Background colorBackground(Color color) {
		checkNotNull(color);
		return new Background(new BackgroundFill(color, null, null));
	}

	public Background imageBackground(String relPath) {
		return new Background(new BackgroundImage(image(relPath), null, null, null, null));
	}

	/*public PhongMaterial coloredMaterial(Color color) {
		checkNotNull(color);
		var material = new PhongMaterial(color);
		material.setSpecularColor(color.brighter());
		return material;
	}*/

	public Color color(Color color, double opacity) {
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

	public Map<String, String> loadBundle(String relPath) {
		Map<String, String> map = new HashMap<>();
		map.put("app.title.ms_pacman", "Ms. Pac-Man {0}");
		map.put("app.title.ms_pacman.paused", "Ms. Pac-Man {0} (paused)");
		map.put("app.title.pacman", "Pac-Man {0}");
		map.put("app.title.pacman.paused", "Pac-Man {0} (paused)");
		map.put("autopilot_on", "Autopilot ON");
		map.put("autopilot_off", "Autopilot OFF");
		map.put("player_immunity_on", "Player is immune");
		map.put("player_immunity_off", "Player is vulnerable");
		map.put("cheat_add_lives", "You have {0,number,integer} lives now");
		return map;
	}
}