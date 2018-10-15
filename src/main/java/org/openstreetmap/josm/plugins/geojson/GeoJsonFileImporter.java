// License: Apache 2.0. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.geojson;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.io.importexport.FileImporter;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.Logging;

import javax.swing.JOptionPane;

import java.io.File;
import java.io.FileInputStream;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * @author Ian Dees <ian.dees@gmail.com>
 * @author matthieun <https://github.com/matthieun>
 */
public class GeoJsonFileImporter extends FileImporter {

    /**
     * Constructs a new GeoJSON File importer with an extension filter for .json and .geojson
     */
    public GeoJsonFileImporter() {
        super(new ExtensionFileFilter("geojson,json", "geojson",
            tr("GeoJSON file") + " (*.geojson,*.json)"));
    }

    @Override
    public void importData(final File file, final ProgressMonitor progressMonitor) {

        progressMonitor.beginTask(tr("Loading json file..."));
        progressMonitor.setTicksCount(2);
        Logging.info("Parsing GeoJSON: " + file.getAbsolutePath());
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            final GeoJsonReader geoJsonReader = new GeoJsonReader();
            DataSet data = geoJsonReader.doParseDataSet(fileInputStream, progressMonitor);
            progressMonitor.worked(1);
            Bounds bounds = null;
            for (OsmPrimitive osmPrimitive : data.allPrimitives()) {
                bounds = mergeBounds(bounds, osmPrimitive);
            }
            final Layer layer = new GeoJsonLayer(tr("Data Layer from GeoJSON: ") + file.getName(), data, bounds);
            MainApplication.getLayerManager().addLayer(layer);
        } catch (final Exception e) {
            Logging.error("Error while reading json file!");
            Logging.error(e);
            GuiHelper.runInEDT(() -> JOptionPane.showMessageDialog(
                null, tr("Error loading geojson file {0}", file.getAbsolutePath()), tr("Error"), JOptionPane.WARNING_MESSAGE));
        } finally {
            progressMonitor.finishTask();
        }
    }

    private Bounds mergeBounds(final Bounds bounds, final OsmPrimitive osmPrimitive) {
        if (osmPrimitive instanceof Node) { // ways and relations consist of nodes that are already in the dataset
            return mergeBounds(bounds, ((Node) osmPrimitive).getCoor());
        }
        return bounds;
    }

    private Bounds mergeBounds(final Bounds bounds, final LatLon coords) {
        if (bounds == null) {
            return new Bounds(coords);
        } else {
            bounds.extend(coords);
            return bounds;
        }
    }
}
