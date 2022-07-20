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

import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.RaiseAndLowerWallAnimation;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.util.Duration;

/**
 * 3D-model for a maze. Creates walls/doors using information from the floor plan.
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	public record MazeColors(Color wallSideColor, Color wallTopColor, Color doorColor) {
	}

	public record WallData(double brickSize, double wallHeight, PhongMaterial baseMaterial, PhongMaterial topMaterial) {
	}

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static final double FLOOR_THICKNESS = 0.1;

	private static final Image WALL_TEXTURE = Ufx.image("graphics/wall-texture-64.jpg");

	public final IntegerProperty resolutionPy = new SimpleIntegerProperty(Env.mazeResolutionPy.get());
	public final DoubleProperty wallHeightPy = new SimpleDoubleProperty(Env.mazeWallHeightPy.get());
	public final DoubleProperty wallThicknessPy = new SimpleDoubleProperty(Env.mazeWallThicknessPy.get());
	public final ObjectProperty<Image> floorTexturePy = new SimpleObjectProperty<>();
	public final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(Env.floorColorPy.get());

	private final World world;
	private final Group wallsGroup = new Group();
	private final Group doorsGroup = new Group();

	public Maze3D(World world, MazeColors mazeColors) {
		this.world = world;
		var floor = createFloor();
		getChildren().addAll(floor, wallsGroup, doorsGroup);
		rebuild(new FloorPlan(world, resolutionPy.get()), mazeColors);
		resolutionPy.addListener((obs, oldVal, newVal) -> rebuild(new FloorPlan(world, resolutionPy.get()), mazeColors));
		floorTexturePy.addListener((obs, oldVal, newVal) -> updateFloorMaterial(floor));
		floorColorPy.addListener((obs, oldVal, newVal) -> updateFloorMaterial(floor));
	}

	public Animation createMazeFlashingAnimation(int times) {
		return times > 0 ? new RaiseAndLowerWallAnimation(times) : new PauseTransition(Duration.seconds(1));
	}

	private Box createFloor() {
		double width = (double) world.numCols() * TS;
		double height = (double) world.numRows() * TS;
		double depth = FLOOR_THICKNESS;
		var floor = new Box(width - 1, height - 1, depth);
		floor.setTranslateX(0.5 * width);
		floor.setTranslateY(0.5 * height);
		floor.setTranslateZ(0.5 * depth);
		floor.drawModeProperty().bind(Env.drawModePy);
		updateFloorMaterial(floor);
		return floor;
	}

	private PhongMaterial coloredMaterial(Color diffuseColor) {
		var material = new PhongMaterial(diffuseColor);
		material.setSpecularColor(diffuseColor.brighter());
		return material;
	}

	private PhongMaterial brickMaterial() {
		var material = new PhongMaterial();
		material.setDiffuseMap(WALL_TEXTURE);
		return material;
	}

	public void rebuild(FloorPlan floorPlan, MazeColors mazeColors) {
		var wallData = new WallData(//
				(double) TS / floorPlan.getResolution(), //
				wallHeightPy.get(), //
				coloredMaterial(mazeColors.wallSideColor), //
				coloredMaterial(mazeColors.wallTopColor));

		wallsGroup.getChildren().clear();
		addCorners(floorPlan, wallData);
		addHorizontalWalls(floorPlan, wallData);
		addVerticalWalls(floorPlan, wallData);

		var doors = world.ghostHouse().doorTiles().map(tile -> createDoor(tile, mazeColors.doorColor)).toList();
		doorsGroup.getChildren().setAll(doors);

		LOGGER.info("Built 3D maze (resolution=%d, wall height=%.2f)", floorPlan.getResolution(), wallData.wallHeight);
	}

	private void updateFloorMaterial(Box floor) {
		var material = coloredMaterial(floorColorPy.get());
		material.setDiffuseMap(floorTexturePy.get());
		floor.setMaterial(material);
	}

	public Stream<Door3D> doors() {
		return doorsGroup.getChildren().stream().map(Door3D.class::cast);
	}

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
					if (wallSize == 0) {
						wallStart = x;
					}
					wallSize++;
				} else if (wallSize > 0) {
					addWall(wallStart, y, wallSize, 1, wallData, FloorPlan.HWALL);
					wallSize = 0;
				}
			}
			if (wallSize > 0 && y == floorPlan.sizeY() - 1) {
				addWall(wallStart, y, wallSize, 1, wallData, FloorPlan.HWALL);
			}
		}
	}

	private void addVerticalWalls(FloorPlan floorPlan, WallData wallData) {
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			int wallStart = -1;
			int wallSize = 0;
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.VWALL) {
					if (wallSize == 0) {
						wallStart = y;
					}
					wallSize++;
				} else if (wallSize > 0) {
					addWall(x, wallStart, 1, wallSize, wallData, FloorPlan.VWALL);
					wallSize = 0;
				}
			}
			if (wallSize > 0 && x == floorPlan.sizeX() - 1) {
				addWall(x, wallStart, 1, wallSize, wallData, FloorPlan.VWALL);
			}
		}
	}

	private void addCorners(FloorPlan floorPlan, WallData wallData) {
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.CORNER) {
					addWall(x, y, 1, 1, wallData, FloorPlan.CORNER);
				}
			}
		}
	}

	private Box createHWall(int numBricksX, double brickSize) {
		Box wall = new Box();
		wall.setWidth(numBricksX * brickSize + brickSize);
		wall.heightProperty().bind(wallThicknessPy);
		return wall;
	}

	private Box createVWall(int numBricksY, double brickSize) {
		Box wall = new Box();
		wall.widthProperty().bind(wallThicknessPy);
		wall.setHeight(numBricksY * brickSize + brickSize);
		return wall;
	}

	private Box createCorner() {
		Box corner = new Box();
		corner.widthProperty().bind(wallThicknessPy);
		corner.heightProperty().bind(wallThicknessPy);
		return corner;
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
	 * @param type       if it is a horizontal wall, a vertical wall or a corner
	 */
	private void addWall(int x, int y, int numBricksX, int numBricksY, WallData data, byte type) {

		var base = switch (type) {
		case FloorPlan.HWALL -> createHWall(numBricksX, data.brickSize);
		case FloorPlan.VWALL -> createVWall(numBricksY, data.brickSize);
		case FloorPlan.CORNER -> createCorner();
		default -> throw new IllegalStateException();
		};
		base.depthProperty().bind(wallHeightPy);
		base.translateZProperty().bind(wallHeightPy.multiply(-0.5));
//		base.setMaterial(data.baseMaterial);
		var pattern = new ImagePattern(WALL_TEXTURE, 0, 0, base.getWidth(), WALL_TEXTURE.getHeight(), false);
		var pm = new PhongMaterial();
		pm.setDiffuseMap(pattern.getImage());
		base.setMaterial(pm);
		base.drawModeProperty().bind(Env.drawModePy);

		var top = switch (type) {
		case FloorPlan.HWALL -> createHWall(numBricksX, data.brickSize);
		case FloorPlan.VWALL -> createVWall(numBricksY, data.brickSize);
		case FloorPlan.CORNER -> createCorner();
		default -> throw new IllegalStateException();
		};
		double topHeight = 0.1;
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