// License: Apache 2.0. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.geojson;

import javax.swing.Icon;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.geojson.DataSetBuilder.BoundedDataSet;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Class that displays a map layer containing GeoJSON data.
 *
 * @author Ian Dees <ian.dees@gmail.com>
 * @author matthieun <https://github.com/matthieun>
 */
public class GeoJsonLayer extends OsmDataLayer {
    private final Bounds bounds;

    public GeoJsonLayer(final String name, final BoundedDataSet data) {
        super(data.getDataSet(), name, null);
        this.bounds = data.getBounds();
    }

    public Bounds getBounds() {
        return this.bounds;
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("data", "way");
    }

    @Override
    public String getToolTipText() {
        return "GeoJSON";
    }

    @Override
    public void visitBoundingBox(final BoundingXYVisitor v) {
        v.visit(this.bounds);
    }
}
