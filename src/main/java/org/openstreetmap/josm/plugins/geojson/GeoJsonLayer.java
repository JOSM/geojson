package org.openstreetmap.josm.plugins.geojson;

import javax.swing.Icon;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
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
public class GeoJsonLayer extends OsmDataLayer
{
    private final DataSet data;
    private Bounds bounds = null;

    public GeoJsonLayer(final String name, final BoundedDataSet data)
    {
        super(data.getDataSet(), name, null);
        this.data = data.getDataSet();
        this.bounds = data.getBounds();
    }

    public Bounds getBounds()
    {
        return this.bounds;
    }

    public DataSet getData()
    {
        return this.data;
    }

    @Override
    public Icon getIcon()
    {
        return ImageProvider.get("data", "way");
    }

    @Override
    public String getToolTipText()
    {
        return "GeoJSON";
    }

    @Override
    public void visitBoundingBox(final BoundingXYVisitor v)
    {
        v.visit(this.bounds);
    }
}
