package org.openstreetmap.josm.plugins.geojson;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.Action;
import javax.swing.Icon;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.osm.visitor.paint.MapRendererFactory;
import org.openstreetmap.josm.data.osm.visitor.paint.Rendering;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
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

        this.data = new DataSet();
        if (data instanceof FeatureCollection)
        {
            final FeatureCollection fc = (FeatureCollection) data;
            for (final Feature feature : fc)
            {
                final GeoJsonObject geometry = feature.getGeometry();
                if (geometry instanceof LineString)
                {
                    final Way w = new Way();

                    final List<LngLatAlt> coordinates = ((LineString) geometry).getCoordinates();
                    final List<Node> nodes = new ArrayList<Node>(coordinates.size());
                    for (final LngLatAlt lngLatAlt : coordinates)
                    {
                        final LatLon latlon = new LatLon(lngLatAlt.getLatitude(),
                                lngLatAlt.getLongitude());

                        if (this.bounds == null)
                        {
                            this.bounds = new Bounds(latlon);
                        }
                        else
                        {
                            this.bounds.extend(latlon);
                        }

                        final Node n = new Node(latlon);
                        this.data.addPrimitive(n);
                        nodes.add(n);
                    }
                    w.setNodes(nodes);

                    final Map<String, Object> properties = feature.getProperties();
                    final Map<String, String> tags = new TreeMap<String, String>();
                    for (final Entry<String, Object> entry : properties.entrySet())
                    {
                        tags.put(entry.getKey(), entry.getValue().toString());
                    }
                    w.setKeys(tags);

                    this.data.addPrimitive(w);
                }
                else if (geometry instanceof Polygon)
                {
                    final Way w = new Way();

                    final List<List<LngLatAlt>> coordinates = ((Polygon) geometry).getCoordinates();
                    final List<Node> nodes = new ArrayList<Node>(coordinates.size());
                    for (final List<LngLatAlt> coordinateList : coordinates)
                    {
                        for (final LngLatAlt lngLatAlt : coordinateList)
                        {
                            final LatLon latlon = new LatLon(lngLatAlt.getLatitude(),
                                    lngLatAlt.getLongitude());

                            if (this.bounds == null)
                            {
                                this.bounds = new Bounds(latlon);
                            }
                            else
                            {
                                this.bounds.extend(latlon);
                            }

                            final Node n = new Node(latlon);
                            this.data.addPrimitive(n);
                            nodes.add(n);
                        }
                    }
                    w.setNodes(nodes);

                    final Map<String, Object> properties = feature.getProperties();
                    final Map<String, String> tags = new TreeMap<String, String>();
                    for (final Entry<String, Object> entry : properties.entrySet())
                    {
                        tags.put(entry.getKey(), entry.getValue().toString());
                    }
                    w.setKeys(tags);

                    this.data.addPrimitive(w);
                }
            }
        }
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
