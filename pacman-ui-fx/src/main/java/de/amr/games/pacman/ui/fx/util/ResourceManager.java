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

import dev.webfx.platform.resource.Resource;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.CornerRadii;
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

	public static Background colorBackground(Color color) {
		checkNotNull(color);
		return new Background(new BackgroundFill(color, null, null));
	}

	public static Background colorBackgroundRounded(Color color, double cornerRadius) {
		checkNotNull(color);
		return new Background(new BackgroundFill(color, new CornerRadii(cornerRadius), null));
	}

	public static Background imageBackground(Image image) {
		return new Background(new BackgroundImage(image, null, null, null, null));
	}

	public static Color color(Color color, double opacity) {
		checkNotNull(color);
		return Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
	}


	private String rootDir = "/";
	private final ArrayList<Image> loadedImages = new ArrayList<>();

	public ResourceManager(String rootDir) {
		checkNotNull(rootDir);
		this.rootDir = rootDir;
	}

	//TODO improve webfx resource bundle support
	public Map<String, String> loadBundle() {
		String messages = Resource.getText(urlFromRelPath("texts/messages.properties")); // Text returned immediately because embed
		Map<String, String> map = new HashMap<>();
		for (String line : messages.split("\n")) {
			int p = line.indexOf('=');
			if (p > 0 && !line.trim().startsWith("#"))
				map.put(line.substring(0, p).trim(), line.substring(p + 1).trim());
		}
		return map;
	}

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @return URL of resource addressed by this path
	 */
	public String urlFromRelPath(String relPath) {
		checkNotNull(relPath);
		return dev.webfx.platform.resource.Resource.toUrl(rootDir + relPath, ResourceManager.class);
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

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @return image loaded from resource addressed by this path.
	 */
	public Image image(String relPath) {
		Image image = new Image(urlFromRelPath(relPath), true);
		loadedImages.add(image);
		return image;
	}

	public Image[] getLoadedImages() {
		return loadedImages.toArray(new Image[loadedImages.size()]);
	}
}