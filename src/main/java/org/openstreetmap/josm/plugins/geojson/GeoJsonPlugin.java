// License: Apache 2.0. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.geojson;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Main class for the geojson plugin.
 *
 * @author Ian Dees <ian.dees@gmail.com>
 * @author matthieun <https://github.com/matthieun>
 */
public class GeoJsonPlugin extends Plugin {
    private final GeoJsonFileImporter geoJsonFileImporter;

    public GeoJsonPlugin(final PluginInformation info) {
        super(info);
        this.geoJsonFileImporter = new GeoJsonFileImporter();
        ExtensionFileFilter.addImporter(this.geoJsonFileImporter);
        ExtensionFileFilter.updateAllFormatsImporter();
        MainApplication.getMenu().openLocation.addDownloadTaskClass(GeoJsonDownloadTask.class);
    }

    @Override
    public void mapFrameInitialized(final MapFrame oldFrame, final MapFrame newFrame) {
        if (newFrame != null) {
            final GeoJsonDialog dialog = new GeoJsonDialog();
            newFrame.addToggleDialog(dialog);
            MainApplication.getLayerManager().addLayerChangeListener(dialog);
            MainApplication.getLayerManager().addActiveLayerChangeListener(dialog);
        }
    }
}
