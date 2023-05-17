# A JavaFX UI (2D) for Pac-Man and Ms. Pac-Man

## About this project

This is the WebFX version of a JavaFX user interface for my UI-agnostic [Pac-Man / Ms. Pac-Man game](https://github.com/armin-reichert/pacman-basic) implementations. This version contains no 3D scenes. It can be run on all platforms supported by WebFX, especially inside a browser via the GWT implementation.

## How to use

The game can be started by opening the following URL(s) in a browser:
- [Ms. Pac-Man game](https://pacman.webfx.dev/)
- [Pac-Man game](https://pacman.webfx.dev/?game=pacman)

From the intro scene of either game, you can switch to the other game by pressing <kbd>V</kbd>. To start the game, you first have to "insert a coin" by pressing <kbd>5</kbd>. After you have credit, the game can be started by pressing <kbd>1</kbd>. I used these keys because they are also used by the MAME Aracde emulator.

Pac-Man steering:
- Pac-Man is steered using the cursor keys.

Simulation control:
  - <kbd>P</kbd> Toggle pause mode
  - <kbd>Shift+P</kbd> or <kbd>Space</kbd> Single step
  - <kbd>T</kbd> Ten steps
  - <kbd>Alt+Shift+F</kbd>Run faster (in 5 Hz steps)
  - <kbd>Alt+Shift+S</kbd>Run slower (in 5 Hz steps)
  - <kbd>Alt+Shift+0</kbd>Run at 60 Hz

Test modes: (available from intro screens)
  - <kbd>Alt+Shift+T</kbd>Start level test mode (plays each level for some seconds)
  - <kbd>Alt+Shift+C</kbd>Play the cutscenes of the game

Cheats:
  - <kbd>Alt+Shift+A</kbd> Toggle autopilot mode
  - <kbd>Alt+Shift+I</kbd> Toggle immunity mode

Play scene cheats:
  - <kbd>Alt+Shift+E</kbd> Eat all pills except the energizers
  - <kbd>Alt+Shift+L</kbd> Add 3 player lives
  - <kbd>Alt+Shift+N</kbd> Enter next game level
  - <kbd>Alt+Shift+X</kbd> Kill all ghosts outside of the ghosthouse 

## How it looks

![Start Pages](startpages.png)

![Intro Screens](introscreens.png)

![Play Screens](playscreens.png)

