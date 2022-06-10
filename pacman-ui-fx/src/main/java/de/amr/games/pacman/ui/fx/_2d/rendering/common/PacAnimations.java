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

package de.amr.games.pacman.ui.fx._2d.rendering.common;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.GenericAnimation;
import de.amr.games.pacman.lib.animation.GenericAnimationCollection;
import de.amr.games.pacman.lib.animation.GenericAnimationMap;
import de.amr.games.pacman.lib.animation.SingleGenericAnimation;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.actors.PacAnimationKey;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class PacAnimations extends GenericAnimationCollection<Pac, PacAnimationKey, Rectangle2D> {

	protected GenericAnimationMap<Direction, Rectangle2D> munching;
	protected SingleGenericAnimation<Rectangle2D> dying;

	public PacAnimations(Rendering2D r2D) {
		munching = r2D.createPacMunchingAnimation();
		dying = r2D.createPacDyingAnimation();
		select(PacAnimationKey.ANIM_MUNCHING);
	}

	@Override
	public void ensureRunning() {
		munching.ensureRunning();
	}

	@Override
	public GenericAnimation<Rectangle2D> getByKey(PacAnimationKey key) {
		return switch (key) {
		case ANIM_DYING -> dying;
		case ANIM_MUNCHING -> munching;
		};
	}

	@Override
	public Stream<GenericAnimation<Rectangle2D>> all() {
		return Stream.of(munching, dying);
	}

	@Override
	public Rectangle2D currentSprite(Pac pac) {
		return switch (selectedKey) {
		case ANIM_DYING -> dying.animate();
		case ANIM_MUNCHING -> {
			var munchingToDir = munching.get(pac.moveDir());
			if (!pac.stuck && pac.velocity.length() > 0) {
				munchingToDir.advance();
			}
			yield munchingToDir.frame();
		}
		};
	}
}