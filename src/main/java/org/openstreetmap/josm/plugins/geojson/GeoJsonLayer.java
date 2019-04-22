// License: Apache 2.0. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.geojson;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.Icon;


/**
 * Class that displays a map layer containing GeoJSON data.
 *
 * @author Ian Dees &lt;ian.dees@gmail.com&gt;
 * @author matthieun &lt;https://github.com/matthieun&gt;
 */
public class GeoJsonLayer extends OsmDataLayer {
    private final Bounds bounds;

    /**
     * Construct a new GeoJSON based {@code OsmDataLayer}.
     *
     * @param name Layer name
     * @param data OSM data
     * @param bounds bounds data
     *
     */
    public GeoJsonLayer(final String name, final DataSet data, final Bounds bounds) {
        super(data, name, null);
        this.bounds = bounds;
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
