/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.ui.fx;

import java.net.URL;
import java.util.MissingResourceException;

/**
 * @author Armin Reichert
 */
public class Resources {

	private Resources() {
	}

	public static String absPath(String relPath) {
		return "/de/amr/games/pacman/ui/fx/" + relPath;
	}

	public static URL urlFromRelPath(String relPath) {
		return url(absPath(relPath));
	}

	public static URL url(String absPath) {
		var url = Resources.class.getResource(absPath);
		if (url == null) {
			throw new MissingResourceException("Missing resource, path=" + absPath, "", absPath);
		}
		return url;
	}
}