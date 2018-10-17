// License: Apache 2.0. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.geojson;

import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.actions.downloadtasks.DownloadParams;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;

import java.util.concurrent.Future;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * GeoJson download task.
 * @author Omar Vega Ramos <ovruni@riseup.net>
 */
public class GeoJsonDownloadTask extends DownloadOsmTask {

    private static final String PATTERN_COMPRESS = "https?://.*/(.*\\.(json|geojson)(\\.(gz|xz|bz2?|zip))?)";

    @Override
    public String[] getPatterns() {
        return new String[]{PATTERN_COMPRESS};
    }

    @Override
    public String getTitle() {
        return tr("Download GeoJSON");
    }

    @Override
    public Future<?> download(DownloadParams settings, Bounds downloadArea, ProgressMonitor progressMonitor) {
        return null;
    }

    @Override
    public Future<?> loadUrl(DownloadParams settings, String url, ProgressMonitor progressMonitor) {
        downloadTask = new InternalDownloadTask(settings, url, progressMonitor);
        return MainApplication.worker.submit(downloadTask);
    }

    class InternalDownloadTask extends DownloadTask {

        private final String url;

        InternalDownloadTask(DownloadParams settings, String url, ProgressMonitor progressMonitor) {
            super(settings, new GeoJsonServerReader(url), progressMonitor);
            this.url = url;
        }

        @Override
        protected GeoJsonLayer createNewLayer(String layerName) {
            if (layerName == null || layerName.isEmpty()) {
                layerName = settings.getLayerName();
            }
            if (layerName == null || layerName.isEmpty()) {
                layerName = url.substring(url.lastIndexOf('/')+1);
            }
            if (settings.getDownloadPolicy() != null) {
                dataSet.setDownloadPolicy(settings.getDownloadPolicy());
            }
            if (settings.getUploadPolicy() != null) {
                dataSet.setUploadPolicy(settings.getUploadPolicy());
            }
            if (dataSet.isLocked() && !settings.isLocked()) {
                dataSet.unlock();
            } else if (!dataSet.isLocked() && settings.isLocked()) {
                dataSet.lock();
            }
            return new GeoJsonLayer(tr("Data Layer from GeoJSON: {0}", layerName), dataSet, null);
        }
    }
}
