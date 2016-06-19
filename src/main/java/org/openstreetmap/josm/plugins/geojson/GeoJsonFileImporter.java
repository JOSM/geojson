package org.openstreetmap.josm.plugins.geojson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.GeoJsonObject;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.FileImporter;
import org.openstreetmap.josm.plugins.geojson.DataSetBuilder.BoundedDataSet;

import javax.swing.*;
import java.io.File;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * @author Ian Dees <ian.dees@gmail.com>
 * @author matthieun <https://github.com/matthieun>
 */
public class GeoJsonFileImporter extends FileImporter {

    public GeoJsonFileImporter() {
        super(new ExtensionFileFilter("geojson,json", "geojson",
                tr("GeoJSON file") + " (*.geojson,*.json)"));
    }

    @Override
    public void importData(final File file, final ProgressMonitor progressMonitor) {

        progressMonitor.beginTask(tr("Loading json file..."));
        progressMonitor.setTicksCount(2);
        Main.info("Parsing GeoJSON: " + file.getAbsolutePath());
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            GeoJsonObject object = mapper.readValue(file, GeoJsonObject.class);

            progressMonitor.worked(1);

            final BoundedDataSet data = new DataSetBuilder().build(object);

            Layer layer = new GeoJsonLayer(tr("Data Layer from GeoJSON: ") + file.getName(), data);
            Main.getLayerManager().addLayer(layer);
        } catch (final Exception e) {
            Main.error("Error while reading json file!", e);
            JOptionPane.showMessageDialog(null, tr("Error loading geojson file {0}", file.getAbsolutePath()), tr("Error"), JOptionPane.WARNING_MESSAGE);
        } finally {
            progressMonitor.finishTask();
        }
    }
}
