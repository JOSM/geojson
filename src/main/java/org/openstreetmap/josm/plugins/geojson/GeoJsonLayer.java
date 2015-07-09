package org.openstreetmap.josm.plugins.geojson;

import java.awt.Graphics2D;

import javax.swing.Action;
import javax.swing.Icon;

import org.geojson.GeoJsonObject;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.osm.visitor.paint.MapRendererFactory;
import org.openstreetmap.josm.data.osm.visitor.paint.Rendering;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.geojson.DataSetBuilder.BoundedDataSet;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Class that displays a map layer containing GeoJSON data.
 *
 * @author Ian Dees <ian.dees@gmail.com>
 * @author matthieun <https://github.com/matthieun>
 */
public class GeoJsonLayer extends Layer
{

    private final DataSet data;
    private Bounds bounds = null;

    public GeoJsonLayer(final String name, final GeoJsonObject data)
    {
        super(name);
        final BoundedDataSet boundedDataSet = new DataSetBuilder().build(data);
        this.data = boundedDataSet.getDataSet();
        this.bounds = boundedDataSet.getBounds();
    }

    @Override
    public Icon getIcon()
    {
        return ImageProvider.get("data", "way");
    }

    @Override
    public Object getInfoComponent()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Action[] getMenuEntries()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getToolTipText()
    {
        return "GeoJSON";
    }

    @Override
    public boolean isMergable(final Layer other)
    {
        return false;
    }

    @Override
    public void mergeFrom(final Layer from)
    {
        return;
    }

    @Override
    public void paint(final Graphics2D g, final MapView mv, final Bounds box)
    {
        final Rendering painter = MapRendererFactory.getInstance().createActiveRenderer(g, mv,
                false);
        painter.render(this.data, false, box);
    }

    @Override
    public void visitBoundingBox(final BoundingXYVisitor v)
    {
        v.visit(this.bounds);
    }
}
