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
package de.amr.games.pacman.ui.fx.v3d.objimport;

import org.tinylog.Logger;

import de.amr.games.pacman.ui.fx.v3d.app.AppRes3d;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;

/**
 * @author Armin Reichert
 */
public class ObjModelLoaderTest {

	public static void main(String[] args) {
		if (args.length == 0) {
			Logger.error("Missing model file path (relative to asset folder)");
			return;
		}
		var relPath = args[0];
		var url = AppRes3d.Manager.urlFromRelPath(relPath);
		var model = new Model3D(url);
		Logger.info(model.contentReport());
	}
}