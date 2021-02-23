package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.model.guys.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.guys.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import javafx.geometry.Rectangle2D;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene1 extends GameScene {

	enum Phase {
		BLINKY_CHASING_PACMAN, BIGPACMAN_CHASING_BLINKY;
	}

	private static final int baselineY = t(20);

	private Ghost blinky;
	private Pac pac;
	private Phase phase;

	public PacMan_IntermissionScene1(double scaling, FXRendering rendering, SoundManager sounds) {
		super(scaling, rendering, sounds);
	}

	@Override
	public void start() {
		pac = new Pac("Pac-Man", Direction.LEFT);
		pac.visible = true;
		pac.setPosition(t(30), baselineY);
		pac.speed = 1;
		rendering.playerMunching(pac).forEach(Animation::restart);

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.setPosition(pac.position.sum(t(3), 0));
		blinky.speed = pac.speed * 1.04f;
		rendering.ghostKicking(blinky, blinky.dir).restart();
		rendering.ghostFrightened(blinky, blinky.dir).restart();

		sounds.loop(PacManGameSound.INTERMISSION_1, 2);

		phase = Phase.BLINKY_CHASING_PACMAN;
	}

	@Override
	public void update() {
		switch (phase) {
		case BLINKY_CHASING_PACMAN:
			if (pac.position.x < -50) {
				pac.dir = RIGHT;
				pac.setPosition(-20, baselineY);
				pac.speed = 0;
				blinky.dir = blinky.wishDir = RIGHT;
				blinky.setPosition(-20, baselineY);
				blinky.speed = 0.8f;
				blinky.state = FRIGHTENED;
				rendering.bigPacMan().restart();
				phase = Phase.BIGPACMAN_CHASING_BLINKY;
			}
			break;
		case BIGPACMAN_CHASING_BLINKY:
			if ((int) blinky.position.x + 4 == t(13)) {
				pac.speed = blinky.speed * 1.8f;
			}
			if (pac.position.x > t(28) + 100) {
				game.state.timer.setDuration(0);
			}
			break;
		default:
			break;
		}
		pac.move();
		blinky.move();
	}

	@Override
	public void renderContent() {
		rendering.drawGhost(g, blinky, false);
		if (phase == Phase.BLINKY_CHASING_PACMAN) {
			rendering.drawPlayer(g, pac);
		} else {
			Rectangle2D sprite = (Rectangle2D) rendering.bigPacMan().animate();
			rendering.drawSprite(g, sprite, pac.position.x - 12, pac.position.y - 22);
		}
		rendering.drawLevelCounter(g, game, t(25), t(34));
	}
}