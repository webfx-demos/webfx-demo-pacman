package de.amr.games.pacman.ui.fx.mspacman;

import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.rendering.MsPacMan_Rendering;
import javafx.scene.canvas.GraphicsContext;

public class Stork extends GameEntity {

	final MsPacMan_Rendering rendering = PacManGameUI_JavaFX.RENDERING_MSPACMAN;

	public Stork() {
		animation = rendering.storkFlying();
	}

	public void draw(GraphicsContext g) {
		rendering.drawStork(g, this);
	}
}