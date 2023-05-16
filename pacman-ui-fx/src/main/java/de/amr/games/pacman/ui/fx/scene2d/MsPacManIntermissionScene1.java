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
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.MsPacManIntermission1;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;
import javafx.scene.canvas.GraphicsContext;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermissionScene1 extends GameScene2D {

	private MsPacManIntermission1 im;

	public MsPacManIntermissionScene1(GameController gameController) {
		super(gameController);
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		im = new MsPacManIntermission1(context.gameController());
		im.changeState(MsPacManIntermission1.State.FLAP);

		var r = (MsPacManGameRenderer) context.rendering2D();
		im.context().clapperboard.setAnimation(r.createClapperboardAnimation());
		im.context().msPac.setAnimations(r.createPacAnimations(im.context().msPac));
		im.context().msPac.animations().ifPresent(AnimationMap::ensureRunning);
		im.context().pacMan.setAnimations(r.createPacAnimations(im.context().pacMan));
		im.context().pacMan.animations().ifPresent(animations -> {
			var munching = r.createPacManMunchingAnimationMap(im.context().pacMan);
			animations.put(GameModel.AK_PAC_MUNCHING, munching);
			animations.ensureRunning();
		});
		im.context().inky.setAnimations(r.createGhostAnimations(im.context().inky));
		im.context().inky.animations().ifPresent(AnimationMap::ensureRunning);
		im.context().pinky.setAnimations(r.createGhostAnimations(im.context().pinky));
		im.context().pinky.animations().ifPresent(AnimationMap::ensureRunning);
	}

	@Override
	public void update() {
		im.update();
	}

	@Override
	public void drawScene(GraphicsContext g) {
		var r = (MsPacManGameRenderer) context.rendering2D();
		r.drawClap(g, im.context().clapperboard);
		r.drawPac(g, im.context().msPac);
		r.drawPac(g, im.context().pacMan);
		r.drawGhost(g, im.context().inky);
		r.drawGhost(g, im.context().pinky);
		r.drawEntitySprite(g, im.context().heart, r.heartSprite());
	}
}