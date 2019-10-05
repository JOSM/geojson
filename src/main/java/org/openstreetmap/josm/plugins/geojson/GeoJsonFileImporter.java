// License: Apache 2.0. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.geojson;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.io.importexport.FileImporter;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.io.CachedFile;
import org.openstreetmap.josm.io.Compression;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.tools.Logging;

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
        Logging.info("Parsing GeoJSON: {0}", file.getAbsolutePath());
        try (InputStream fileInputStream = Compression.getUncompressedFileInputStream(file)) {
            DataSet data = new GeoJsonReader().doParseDataSet(fileInputStream, progressMonitor);
            progressMonitor.worked(1);
            MainApplication.getLayerManager().addLayer(new OsmDataLayer(data, file.getName(), file));
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
}
