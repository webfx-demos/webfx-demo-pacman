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

package de.amr.games.pacman.ui.fx.rendering2d;

import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * @author Armin Reichert
 */
public class PacManColoring {

    Color headColor; Color palateColor; Color eyesColor;

    public PacManColoring(Color headColor, Color palateColor, Color eyesColor) {
        this.headColor = headColor;
        this.palateColor = palateColor;
        this.eyesColor = eyesColor;
    }

    public Color headColor() {
        return headColor;
    }

    public Color palateColor() {
        return palateColor;
    }

    public Color eyesColor() {
        return eyesColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PacManColoring that = (PacManColoring) o;

        if (!Objects.equals(headColor, that.headColor)) return false;
        if (!Objects.equals(palateColor, that.palateColor)) return false;
        return Objects.equals(eyesColor, that.eyesColor);
    }

    @Override
    public int hashCode() {
        int result = headColor != null ? headColor.hashCode() : 0;
        result = 31 * result + (palateColor != null ? palateColor.hashCode() : 0);
        result = 31 * result + (eyesColor != null ? eyesColor.hashCode() : 0);
        return result;
    }
}
