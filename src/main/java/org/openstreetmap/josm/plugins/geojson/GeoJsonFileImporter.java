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
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.io.CachedFile;
import org.openstreetmap.josm.io.Compression;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.tools.Logging;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * @author Ian Dees &lt;ian.dees@gmail.com&gt;
 * @author matthieun &lt;https://github.com/matthieun&gt;
 */
public class GeoJsonFileImporter extends FileImporter {

    private static final ExtensionFileFilter FILE_FILTER = ExtensionFileFilter.newFilterWithArchiveExtensions(
        "geojson,json", "geojson", tr("GeoJSON file") + " (*.geojson, *.geojson.gz, *.geojson.bz2, *.geojson.xz, *.geojson.zip, *.json)",
        ExtensionFileFilter.AddArchiveExtension.NONE, Arrays.asList("gz", "bz", "bz2", "xz", "zip"));

    /**
     * Constructs a new GeoJSON File importer with an extension filter for .json and .geojson
     */
    public GeoJsonFileImporter() {
        super(FILE_FILTER);
    }

    @Override
    public void importData(final File file, final ProgressMonitor progressMonitor) {

        progressMonitor.beginTask(tr("Loading json file…"));
        progressMonitor.setTicksCount(2);
        Logging.info("Parsing GeoJSON: " + file.getAbsolutePath());
        try (InputStream fileInputStream = Compression.getUncompressedFileInputStream(file)) {
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

    public DataSet parseDataSet(final String source) throws IOException, IllegalDataException {
        try (CachedFile cf = new CachedFile(source)) {
            InputStream fileInputStream = Compression.getUncompressedFileInputStream(cf.getFile());
            return GeoJsonReader.parseDataSet(fileInputStream, NullProgressMonitor.INSTANCE);
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
