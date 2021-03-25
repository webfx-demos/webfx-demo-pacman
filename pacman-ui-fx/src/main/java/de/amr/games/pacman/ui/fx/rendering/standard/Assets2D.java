package de.amr.games.pacman.ui.fx.rendering.standard;

import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;

import java.util.EnumMap;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering2D;
import javafx.scene.paint.Color;

public class Assets2D {

	public static final EnumMap<GameVariant, PacManGameRendering2D> RENDERING_2D = new EnumMap<>(GameVariant.class);

	static {
		RENDERING_2D.put(MS_PACMAN, new MsPacMan_StandardRendering());
		RENDERING_2D.put(PACMAN, new PacMan_StandardRendering());
	}

	public static Color getFoodColor(int mazeNumber) {
		switch (mazeNumber) {
		case 1:
			return Color.rgb(222, 222, 255);
		case 2:
			return Color.rgb(255, 255, 0);
		case 3:
			return Color.rgb(255, 0, 0);
		case 4:
			return Color.rgb(222, 222, 255);
		case 5:
			return Color.rgb(0, 255, 255);
		case 6:
			return Color.rgb(222, 222, 255);
		default:
			return Color.WHITE;
		}
	}

	public static Color getMazeWallColor(int mazeNumber) {
		switch (mazeNumber) {
		case 1:
			return Color.rgb(255, 183, 174);
		case 2:
			return Color.rgb(71, 183, 255);
		case 3:
			return Color.rgb(222, 151, 81);
		case 4:
			return Color.rgb(33, 33, 255);
		case 5:
			return Color.rgb(255, 183, 255);
		case 6:
			return Color.rgb(255, 183, 174);
		default:
			return Color.WHITE;
		}
	}

	public static Color getMazeWallBorderColor(int mazeIndex) {
		switch (mazeIndex) {
		case 0:
			return Color.rgb(255, 0, 0);
		case 1:
			return Color.rgb(222, 222, 255);
		case 2:
			return Color.rgb(222, 222, 255);
		case 3:
			return Color.rgb(255, 183, 81);
		case 4:
			return Color.rgb(255, 255, 0);
		case 5:
			return Color.rgb(255, 0, 0);
		default:
			return Color.WHITE;
		}
	}
}
