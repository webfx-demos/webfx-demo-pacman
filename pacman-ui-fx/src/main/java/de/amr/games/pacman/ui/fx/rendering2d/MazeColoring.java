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
public class MazeColoring {

    Color foodColor; Color wallTopColor; Color wallBaseColor; Color houseDoorColor;

    public MazeColoring(Color foodColor, Color wallTopColor, Color wallBaseColor, Color houseDoorColor) {
        this.foodColor = foodColor;
        this.wallTopColor = wallTopColor;
        this.wallBaseColor = wallBaseColor;
        this.houseDoorColor = houseDoorColor;
    }

    public Color foodColor() {
        return foodColor;
    }

    public Color wallTopColor() {
        return wallTopColor;
    }

    public Color wallBaseColor() {
        return wallBaseColor;
    }

    public Color houseDoorColor() {
        return houseDoorColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MazeColoring that = (MazeColoring) o;

        if (!Objects.equals(foodColor, that.foodColor)) return false;
        if (!Objects.equals(wallTopColor, that.wallTopColor)) return false;
        if (!Objects.equals(wallBaseColor, that.wallBaseColor))
            return false;
        return Objects.equals(houseDoorColor, that.houseDoorColor);
    }

    @Override
    public int hashCode() {
        int result = foodColor != null ? foodColor.hashCode() : 0;
        result = 31 * result + (wallTopColor != null ? wallTopColor.hashCode() : 0);
        result = 31 * result + (wallBaseColor != null ? wallBaseColor.hashCode() : 0);
        result = 31 * result + (houseDoorColor != null ? houseDoorColor.hashCode() : 0);
        return result;
    }
}
