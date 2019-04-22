// License: Apache 2.0. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.geojson;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.io.OsmTransferException;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * GeoJson server reader.
 * @author Omar Vega Ramos &lt;ovruni@riseup.net&gt;
 */
public class GeoJsonServerReader extends OsmServerReader {

    private final String url;

    public GeoJsonServerReader(String url) {
        this.url = url;
    }

    @Override
    public DataSet parseOsm(ProgressMonitor progressMonitor) throws OsmTransferException {
        try {
            progressMonitor.beginTask(tr("Contacting Server…"), 10);
            return new GeoJsonFileImporter().parseDataSet(url);
        } catch (Exception e) {
            throw new OsmTransferException(e);
        } finally {
            progressMonitor.finishTask();
        }        
    }
}
