// File managed by WebFX (DO NOT EDIT MANUALLY)

module pacman.ui.fx {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires pacman.core;
    requires webfx.kit.util.scene;
    requires webfx.platform.resource;
    requires webfx.platform.useragent;
    requires webfx.platform.util;

    // Exported packages
    exports de.amr.games.pacman.ui.fx.app;
    exports de.amr.games.pacman.ui.fx.input;
    exports de.amr.games.pacman.ui.fx.rendering2d;
    exports de.amr.games.pacman.ui.fx.scene;
    exports de.amr.games.pacman.ui.fx.scene2d;
    exports de.amr.games.pacman.ui.fx.sound;
    exports de.amr.games.pacman.ui.fx.util;

    // Resources packages
    opens de.amr.games.pacman.ui.fx.assets.fonts;
    opens de.amr.games.pacman.ui.fx.assets.graphics.icons;
    opens de.amr.games.pacman.ui.fx.assets.graphics.mspacman;
    opens de.amr.games.pacman.ui.fx.assets.graphics.pacman;
    opens de.amr.games.pacman.ui.fx.assets.sound.common;
    opens de.amr.games.pacman.ui.fx.assets.sound.mspacman;
    opens de.amr.games.pacman.ui.fx.assets.sound.pacman;
    opens de.amr.games.pacman.ui.fx.assets.sound.voice;
    opens de.amr.games.pacman.ui.fx.assets.texts;

    // Provided services
    provides javafx.application.Application with de.amr.games.pacman.ui.fx.app.PacManGameAppFX;

}