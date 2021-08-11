package de.amr.games.pacman.ui.fx.entities._2d.pacman;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.entities._2d.Renderable2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_PacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Blinky with his dress patched. Used in the third intermission scene in Pac-Man.
 * 
 * @author Armin Reichert
 */
public class BlinkyPatched2D implements Renderable2D {

	private final Ghost blinky;
	private final Rendering2D_PacMan rendering;
	public final TimedSequence<Rectangle2D> animation;

	public BlinkyPatched2D(Ghost blinky, Rendering2D_PacMan rendering) {
		this.blinky = blinky;
		this.rendering = rendering;
		animation = rendering.createBlinkyPatchedAnimation();
	}

	@Override
	public void render(GraphicsContext g) {
		rendering.renderEntity(g, blinky, animation.animate());
	}
}