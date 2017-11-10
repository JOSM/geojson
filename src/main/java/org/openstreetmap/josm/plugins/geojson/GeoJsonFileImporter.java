// License: Apache 2.0. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.geojson;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;

import javax.swing.JOptionPane;

import org.geojson.GeoJsonObject;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.io.importexport.FileImporter;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.geojson.DataSetBuilder.BoundedDataSet;
import org.openstreetmap.josm.tools.Logging;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        Logging.info("Parsing GeoJSON: " + file.getAbsolutePath());
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final GeoJsonObject object = mapper.readValue(file, GeoJsonObject.class);

            progressMonitor.worked(1);

            final BoundedDataSet data = new DataSetBuilder().build(object);

            final Layer layer = new GeoJsonLayer(tr("Data Layer from GeoJSON: ") + file.getName(), data);
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
}
