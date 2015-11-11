package org.openstreetmap.josm.plugins.geojson;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.geojson.GeoJsonObject;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.FileImporter;
import org.openstreetmap.josm.plugins.geojson.DataSetBuilder.BoundedDataSet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;

/**
 * @author Ian Dees <ian.dees@gmail.com>
 * @author matthieun <https://github.com/matthieun>
 */
public class GeoJsonFileImporter extends FileImporter {
    public static final Logger LOGGER = Logger.getLogger(GeoJsonFileImporter.class.getName());

    public GeoJsonFileImporter() {
        super(new ExtensionFileFilter("geojson,json", "geojson",
                tr("GeoJSON file") + " (*.geojson,*.json)"));
    }

    @Override
    public void importData(final File file, final ProgressMonitor progressMonitor) {

        progressMonitor.beginTask(tr("Loading json file..."));
        progressMonitor.setTicksCount(2);
        LOGGER.info("Parsing GeoJSON: " + file.getAbsolutePath());
        try {
            GeoJsonObject object = new ObjectMapper().readValue(file, GeoJsonObject.class);

            progressMonitor.worked(1);

            final BoundedDataSet data = new DataSetBuilder().build(object);

            Layer layer = new GeoJsonLayer("GeoJSON: " + file.getName(), data);
            Main.main.addLayer(layer);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error while reading json file!", e);
            JOptionPane.showMessageDialog(null, tr("Error loading geojson file {}", file.getAbsolutePath()), tr("Error"), JOptionPane.WARNING_MESSAGE);
        } finally {
            progressMonitor.finishTask();
        }
    }
}
