// File managed by WebFX (DO NOT EDIT MANUALLY)
package pacman.ui.fx.gwt.embed;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import dev.webfx.platform.resource.spi.impl.gwt.GwtResourceBundleBase;

public interface EmbedResourcesBundle extends ClientBundle {

    EmbedResourcesBundle R = GWT.create(EmbedResourcesBundle.class);
    @Source("de/amr/games/pacman/ui/fx/assets/texts/messages.properties")
    TextResource r1();

    @Source("dev/webfx/platform/meta/exe/exe.properties")
    TextResource r2();



    final class ProvidedGwtResourceBundle extends GwtResourceBundleBase {
        public ProvidedGwtResourceBundle() {
            registerResource("de/amr/games/pacman/ui/fx/assets/texts/messages.properties", R.r1());
            registerResource("dev/webfx/platform/meta/exe/exe.properties", R.r2());

        }
    }
}
