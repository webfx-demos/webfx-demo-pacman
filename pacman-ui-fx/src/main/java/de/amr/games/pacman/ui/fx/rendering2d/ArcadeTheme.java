package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.model.GameModel;
import javafx.scene.paint.Color;

public class ArcadeTheme {
    public static final Color RED = Color.rgb(255, 0, 0);
    public static final Color YELLOW = Color.rgb(255, 255, 0);
    public static final Color PINK = Color.rgb(252, 181, 255);
    public static final Color CYAN = Color.rgb(0, 255, 255);
    public static final Color ORANGE = Color.rgb(251, 190, 88);
    public static final Color BLACK = Color.rgb(0, 0, 0);
    public static final Color BLUE = Color.rgb(33, 33, 255);
    public static final Color PALE = Color.rgb(222, 222, 255);
    public static final Color ROSE = Color.rgb(252, 187, 179);

    public static final GhostColoring[] GHOST_COLORING = new GhostColoring[4];

    //@formatter:off
    static {
        GHOST_COLORING[GameModel.RED_GHOST] = new GhostColoring(
            RED,  PALE, BLUE, // normal
            BLUE, ROSE, ROSE, // frightened
            PALE, ROSE, RED   // flashing
        );

        GHOST_COLORING[GameModel.PINK_GHOST] = new GhostColoring(
            PINK, PALE, BLUE, // normal
            BLUE, ROSE, ROSE, // frightened
            PALE, ROSE, RED   // flashing
        );

        GHOST_COLORING[GameModel.CYAN_GHOST] = new GhostColoring(
            CYAN, PALE, BLUE, // normal
            BLUE, ROSE, ROSE, // frightened
            PALE, ROSE, RED   // flashing
        );

        GHOST_COLORING[GameModel.ORANGE_GHOST] = new GhostColoring(
            ORANGE, PALE, BLUE, // normal
            BLUE,   ROSE, ROSE, // frightened
            PALE,   ROSE, RED   // flashing
        );
    }

    public static final MazeColoring PACMAN_MAZE_COLORS = new MazeColoring(//
            Color.rgb(254, 189, 180), // food color
            Color.rgb(33, 33, 255).darker(), // wall top color
            Color.rgb(33, 33, 255).brighter(), // wall side color
            Color.rgb(252, 181, 255) // ghosthouse door color
    );

    public static final MazeColoring[] MS_PACMAN_MAZE_COLORS = {
        new MazeColoring(Color.rgb(222, 222, 255), Color.rgb(255, 183, 174),  Color.rgb(255,   0,   0), Color.rgb(255, 183, 255)),
        new MazeColoring(Color.rgb(255, 255, 0),   Color.rgb( 71, 183, 255),  Color.rgb(222, 222, 255), Color.rgb(255, 183, 255)),
        new MazeColoring(Color.rgb(255,   0, 0),   Color.rgb(222, 151,  81),  Color.rgb(222, 222, 255), Color.rgb(255, 183, 255)),
        new MazeColoring(Color.rgb(222, 222, 255), Color.rgb( 33,  33, 255),  Color.rgb(255, 183,  81), Color.rgb(255, 183, 255)),
        new MazeColoring(Color.rgb(0,   255, 255), Color.rgb(255, 183, 255),  Color.rgb(255, 255,   0), Color.rgb(255, 183, 255)),
        new MazeColoring(Color.rgb(222, 222, 255), Color.rgb(255, 183, 174),  Color.rgb(255,   0,   0), Color.rgb(255, 183, 255)),
    };

    public static final PacManColoring PACMAN_COLORING = new PacManColoring(
        Color.rgb(255, 255, 0), // head
        Color.rgb(191, 79, 61), // palate
        Color.rgb(33, 33, 33)   // eyes
    );

    public static final MsPacManColoring MS_PACMAN_COLORING = new MsPacManColoring(
        Color.rgb(255, 255, 0), // head
        Color.rgb(191, 79, 61), // palate
        Color.rgb(33, 33, 33),  // eyes
        Color.rgb(255, 0, 0),   // hair bow
        Color.rgb(33, 33, 255)  // hair bow pearls
    );
    //@formatter:on
}
