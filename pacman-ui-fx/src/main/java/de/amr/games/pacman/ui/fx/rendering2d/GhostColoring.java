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

package de.amr.games.pacman.ui.fx.rendering2d;

import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * @author Armin Reichert
 */
public class GhostColoring {//
		Color dress; Color eyeballs; Color pupils; //
		Color dressFrightened; Color eyeballsFrightened; Color pupilsFrightened; //
		Color dressFlashing; Color eyeballsFlashing; Color pupilsFlashing;

	public GhostColoring(Color dress, Color eyeballs, Color pupils, Color dressFrightened, Color eyeballsFrightened, Color pupilsFrightened, Color dressFlashing, Color eyeballsFlashing, Color pupilsFlashing) {
		this.dress = dress;
		this.eyeballs = eyeballs;
		this.pupils = pupils;
		this.dressFrightened = dressFrightened;
		this.eyeballsFrightened = eyeballsFrightened;
		this.pupilsFrightened = pupilsFrightened;
		this.dressFlashing = dressFlashing;
		this.eyeballsFlashing = eyeballsFlashing;
		this.pupilsFlashing = pupilsFlashing;
	}

	public Color dress() {
		return dress;
	}

	public Color eyeballs() {
		return eyeballs;
	}

	public Color pupils() {
		return pupils;
	}

	public Color getDressFrightened() {
		return dressFrightened;
	}

	public Color eyeballsFrightened() {
		return eyeballsFrightened;
	}

	public Color pupilsFrightened() {
		return pupilsFrightened;
	}

	public Color dressFlashing() {
		return dressFlashing;
	}

	public Color eyeballsFlashing() {
		return eyeballsFlashing;
	}

	public Color pupilsFlashing() {
		return pupilsFlashing;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GhostColoring that = (GhostColoring) o;

		if (!Objects.equals(dress, that.dress)) return false;
		if (!Objects.equals(eyeballs, that.eyeballs)) return false;
		if (!Objects.equals(pupils, that.pupils)) return false;
		if (!Objects.equals(dressFrightened, that.dressFrightened))
			return false;
		if (!Objects.equals(eyeballsFrightened, that.eyeballsFrightened))
			return false;
		if (!Objects.equals(pupilsFrightened, that.pupilsFrightened))
			return false;
		if (!Objects.equals(dressFlashing, that.dressFlashing))
			return false;
		if (!Objects.equals(eyeballsFlashing, that.eyeballsFlashing))
			return false;
		return Objects.equals(pupilsFlashing, that.pupilsFlashing);
	}

	@Override
	public int hashCode() {
		int result = dress != null ? dress.hashCode() : 0;
		result = 31 * result + (eyeballs != null ? eyeballs.hashCode() : 0);
		result = 31 * result + (pupils != null ? pupils.hashCode() : 0);
		result = 31 * result + (dressFrightened != null ? dressFrightened.hashCode() : 0);
		result = 31 * result + (eyeballsFrightened != null ? eyeballsFrightened.hashCode() : 0);
		result = 31 * result + (pupilsFrightened != null ? pupilsFrightened.hashCode() : 0);
		result = 31 * result + (dressFlashing != null ? dressFlashing.hashCode() : 0);
		result = 31 * result + (eyeballsFlashing != null ? eyeballsFlashing.hashCode() : 0);
		result = 31 * result + (pupilsFlashing != null ? pupilsFlashing.hashCode() : 0);
		return result;
	}
}