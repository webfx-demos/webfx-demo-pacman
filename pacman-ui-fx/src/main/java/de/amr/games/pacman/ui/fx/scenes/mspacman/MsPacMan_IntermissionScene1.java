package de.amr.games.pacman.ui.fx.scenes.mspacman;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.rendering.standard.Assets2D;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene1_Controller;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they
 * quickly move upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms.
 * Pac-Man face each other at the top of the screen and a big pink heart appears above them. (Played
 * after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene1 extends AbstractGameScene2D {

	private MsPacMan_IntermissionScene1_Controller sceneController;

	public MsPacMan_IntermissionScene1() {
		super(Assets2D.RENDERING_2D.get(GameVariant.MS_PACMAN), SoundAssets.get(GameVariant.MS_PACMAN));
	}

	@Override
	public void start() {
		sceneController = new MsPacMan_IntermissionScene1_Controller(gameController, rendering, sounds);
		sceneController.start();
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		super.update();
		sceneController.update();
		rendering.drawFlap(gc, sceneController.flap);
		rendering.drawPlayer(gc, sceneController.msPac);
		rendering.drawSpouse(gc, sceneController.pacMan);
		rendering.drawGhost(gc, sceneController.inky, false);
		rendering.drawGhost(gc, sceneController.pinky, false);
		rendering.drawHeart(gc, sceneController.heart);
	}
}