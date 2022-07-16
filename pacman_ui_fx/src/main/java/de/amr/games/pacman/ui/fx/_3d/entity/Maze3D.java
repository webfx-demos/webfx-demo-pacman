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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.ArcadeGhostHouse;
import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.RaiseAndLowerWallAnimation;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.util.Duration;

/**
 * 3D-model for a maze. Creates walls/doors using information from the floor plan.
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public static final double FLOOR_THICKNESS = 0.1;

	public record MazeColors(Color wallSideColor, Color wallTopColor, Color doorColor) {
	}

	public record WallData(double brickSize, double height, PhongMaterial baseMaterial, PhongMaterial topMaterial) {
	}

	public final IntegerProperty resolutionPy = new SimpleIntegerProperty(4);
	public final DoubleProperty wallHeightPy = new SimpleDoubleProperty(1.0);
	public final ObjectProperty<Image> floorTexturePy = new SimpleObjectProperty<>();
	public final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>();

	private final World world;
	private final Group foundationGroup = new Group();
	private final Group wallsGroup = new Group();
	private final Group doorsGroup = new Group();

	public Maze3D(World world, MazeColors mazeColors) {
		this.world = world;
		foundationGroup.getChildren().addAll(createFloor(), wallsGroup, doorsGroup);
		build(mazeColors);
		getChildren().add(foundationGroup);
		resolutionPy.addListener((obs, oldVal, newVal) -> build(mazeColors));
		floorTexturePy.addListener((obs, oldVal, newVal) -> updateFloorTexture());
		floorColorPy.addListener((obs, oldVal, newVal) -> updateFloorTexture());
	}

	private Node createFloor() {
		double width = (double) world.numCols() * TS;
		double height = (double) world.numRows() * TS;
		double depth = FLOOR_THICKNESS;
		var floor = new Box(width - 1, height - 1, depth);
		floor.setTranslateX(0.5 * width);
		floor.setTranslateY(0.5 * height);
		floor.setTranslateZ(0.5 * depth);
		floor.drawModeProperty().bind(Env.drawModePy);
		return floor;
	}

	private Box getFloor() {
		return (Box) foundationGroup.getChildren().get(0);
	}

	public void build(MazeColors mazeColors) {
		var floorPlan = new FloorPlan(resolutionPy.get(), world);

		var wallData = new WallData(//
				(double) TS / floorPlan.getResolution(), //
				wallHeightPy.get(), //
				new PhongMaterial(mazeColors.wallSideColor), //
				new PhongMaterial(mazeColors.wallTopColor));
		wallData.baseMaterial.setSpecularColor(mazeColors.wallSideColor.brighter());

		wallsGroup.getChildren().clear();
		addCorners(floorPlan, wallData);
		addHorizontalWalls(floorPlan, wallData);
		addVerticalWalls(floorPlan, wallData);

		doorsGroup.getChildren()
				.setAll(world.ghostHouse().doorTiles().map(doorTile -> createDoor(doorTile, mazeColors.doorColor)).toList());

		LOGGER.info("Built 3D maze (resolution=%d, wall height=%.2f)", floorPlan.getResolution(), wallData.height);
	}

	private void updateFloorTexture() {
		var texture = floorTexturePy.get();
		var color = floorColorPy.get();
		var material = new PhongMaterial();
		if (color != null) {
			material.setDiffuseColor(color);
			material.setSpecularColor(color.brighter());
		}
		material.setDiffuseMap(texture);
		getFloor().setMaterial(material);
	}

	public World getWorld() {
		return world;
	}

	public Stream<Door3D> doors() {
		return doorsGroup.getChildren().stream().map(Node::getUserData).map(Door3D.class::cast);
	}

	public Animation createMazeFlashingAnimation(int times) {
		return times > 0 ? new RaiseAndLowerWallAnimation(times) : new PauseTransition(Duration.seconds(1));
	}

	// should be generalized to work with any ghost house
	public void updateDoorState(Stream<Ghost> ghosts) {
		if (world.ghostHouse() instanceof ArcadeGhostHouse arcadeHouse) {
			boolean accessGranted = isAnyGhostGettingAccess(ghosts, arcadeHouse.doorsCenterPosition());
			doors().forEach(door3D -> door3D.setOpen(accessGranted));
		}
	}

	private boolean isAnyGhostGettingAccess(Stream<Ghost> ghosts, V2d centerPosition) {
		return ghosts //
				.filter(Ghost::isVisible) //
				.filter(ghost -> ghost.is(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)) //
				.anyMatch(ghost -> isGhostGettingAccess(ghost, centerPosition));
	}

	private boolean isGhostGettingAccess(Ghost ghost, V2d doorCenter) {
		return ghost.getPosition().euclideanDistance(doorCenter) <= (ghost.is(LEAVING_HOUSE) ? TS : 3 * TS);
	}

	public void eatPellet(Pellet3D pellet3D) {
		if (pellet3D instanceof Energizer3D energizer) {
			energizer.stopPumping();
		}
		// Delay hiding of pellet for some milliseconds because in case the player approaches the pellet from the right,
		// the pellet disappears too early (collision by same tile in game model is too simplistic).
		var delayHiding = Ufx.pauseSec(0.05, () -> pellet3D.setVisible(false));
		var eatenAnimation = pellet3D.getEatenAnimation();
		if (eatenAnimation.isPresent()) {
			new SequentialTransition(delayHiding, eatenAnimation.get()).play();
		} else {
			delayHiding.play();
		}
	}

	// -------------------------------------------------------------------------------------------

	private Door3D createDoor(V2i tile, Color color) {
		var door = new Door3D(tile, color);
		door.doorHeightPy.bind(wallHeightPy);
		return door;
	}

	private void addHorizontalWalls(FloorPlan floorPlan, WallData wallData) {
		for (int y = 0; y < floorPlan.sizeY(); ++y) {
			int wallStart = -1;
			int wallSize = 0;
			for (int x = 0; x < floorPlan.sizeX(); ++x) {
				if (floorPlan.get(x, y) == FloorPlan.HWALL) {
					if (wallSize > 0) {
						wallSize++;
					} else {
						wallStart = x;
						wallSize = 1;
					}
				} else if (wallSize > 0) {
					addWall(wallStart, y, wallSize, 1, wallData);
					wallSize = 0;
				}
			}
			if (wallSize > 0 && y == floorPlan.sizeY() - 1) {
				addWall(wallStart, y, wallSize, 1, wallData);
			}
		}
	}

	private void addVerticalWalls(FloorPlan floorPlan, WallData wallData) {
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			int wallStart = -1;
			int wallSize = 0;
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.VWALL) {
					if (wallSize > 0) {
						wallSize++;
					} else {
						wallStart = y;
						wallSize = 1;
					}
				} else if (wallSize > 0) {
					addWall(x, wallStart, 1, wallSize, wallData);
					wallSize = 0;
				}
			}
			if (wallSize > 0 && x == floorPlan.sizeX() - 1) {
				addWall(x, wallStart, 1, wallSize, wallData);
			}
		}
	}

	private void addCorners(FloorPlan floorPlan, WallData wallData) {
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.CORNER) {
					addWall(x, y, 1, 1, wallData);
				}
			}
		}
	}

	/**
	 * Adds a wall at given position. A wall consists of a base and a top part which can have different color and
	 * material.
	 * 
	 * @param x          x-coordinate of top-left brick
	 * @param y          y-coordinate of top-left brick
	 * @param numBricksX number of bricks in x-direction
	 * @param numBricksY number of bricks in y-direction
	 * @param data       data on how the wall look like
	 */
	private void addWall(int x, int y, int numBricksX, int numBricksY, WallData data) {
		Box base = new Box();
		base.setWidth(numBricksX * data.brickSize);
		base.setHeight(numBricksY * data.brickSize);
		base.depthProperty().bind(wallHeightPy);
		base.translateZProperty().bind(wallHeightPy.multiply(-0.5));
		base.setMaterial(data.baseMaterial);
		base.drawModeProperty().bind(Env.drawModePy);

		double topHeight = 0.1;
		Box top = new Box();
		top.setWidth(numBricksX * data.brickSize);
		top.setHeight(numBricksY * data.brickSize);
		top.setDepth(topHeight);
		top.translateZProperty().bind(base.translateZProperty().subtract(wallHeightPy.add(topHeight + 0.1).multiply(0.5)));
		top.setMaterial(data.topMaterial);
		top.drawModeProperty().bind(Env.drawModePy);

		var wall = new Group(base, top);
		wall.setTranslateX((x + 0.5 * numBricksX) * data.brickSize);
		wall.setTranslateY((y + 0.5 * numBricksY) * data.brickSize);

		wallsGroup.getChildren().add(wall);
	}
}