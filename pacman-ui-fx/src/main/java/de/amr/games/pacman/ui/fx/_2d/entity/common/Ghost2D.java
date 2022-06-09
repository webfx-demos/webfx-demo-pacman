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
package de.amr.games.pacman.ui.fx._2d.entity.common;

import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations.Key;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of a ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost2D {

	public final Ghost ghost;
	public final GhostAnimations animations;

	public Ghost2D(Ghost ghost, Rendering2D r2D) {
		this.ghost = ghost;
		this.animations = new GhostAnimations(ghost.id, r2D);
		animations.select(GhostAnimations.Key.ANIM_COLOR);
	}

	public void startFlashing(int numFlashes, long ticksTotal) {
		long frameTicks = ticksTotal / (numFlashes * animations.flashing.numFrames());
		animations.flashing.frameDuration(frameTicks);
		animations.flashing.repeat(numFlashes);
		animations.flashing.restart();
		animations.select(GhostAnimations.Key.ANIM_FLASHING);
	}

	public void onFrightenedPhaseEnds() {
		if (animations.selectedKey() == Key.ANIM_FLASHING) {
			animations.select(GhostAnimations.Key.ANIM_COLOR);
		}
	}

	public void updateAnimations(boolean recoveringStarts, int numFlashes, long recoveringTicks) {
		if (ghost.velocity.length() == 0) {
			animations.color.stop();
		} else {
			animations.color.run();
		}
		switch (ghost.state) {
		case DEAD -> {
			if (ghost.killIndex == -1) {
				animations.select(GhostAnimations.Key.ANIM_EYES);
			} else {
				animations.select(GhostAnimations.Key.ANIM_VALUE);
			}
		}
		case FRIGHTENED -> {
			if (recoveringStarts) {
				startFlashing(numFlashes, recoveringTicks);
			}
		}
		case LOCKED -> {
			if (recoveringStarts) {
				startFlashing(numFlashes, recoveringTicks);
			}
		}
		case LEAVING_HOUSE -> {
			animations.select(GhostAnimations.Key.ANIM_COLOR);
		}
		default -> {
		}
		}
	}

	public void render(GraphicsContext g, Rendering2D r2D) {
		r2D.drawEntity(g, ghost, animations.currentSprite(ghost));
	}
}