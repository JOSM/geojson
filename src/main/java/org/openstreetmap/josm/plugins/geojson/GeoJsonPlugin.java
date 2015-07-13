package org.openstreetmap.josm.plugins.geojson;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Main class for the geojson plugin.
 *
 * @author Ian Dees <ian.dees@gmail.com>
 * @author matthieun <https://github.com/matthieun>
 */
public class GeoJsonPlugin extends Plugin
{
    private final GeoJsonFileImporter geoJsonFileImporter;

    public GeoJsonPlugin(final PluginInformation info)
    {
        super(info);
        this.geoJsonFileImporter = new GeoJsonFileImporter();
        ExtensionFileFilter.importers.add(this.geoJsonFileImporter);
        ExtensionFileFilter.updateAllFormatsImporter();
    }

    @Override
    public void mapFrameInitialized(final MapFrame oldFrame, final MapFrame newFrame)
    {
        if (newFrame != null)
        {
            final GeoJsonDialog dialog = new GeoJsonDialog(this.geoJsonFileImporter.getLayer());
            newFrame.addToggleDialog(dialog);
            System.out.println("Added new toggle dialog");
        }
    }
}
