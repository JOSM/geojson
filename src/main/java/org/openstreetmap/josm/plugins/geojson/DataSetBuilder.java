package org.openstreetmap.josm.plugins.geojson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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

/**
 * @author matthieun
 */
public class DataSetBuilder
{
    /**
     * @author matthieun
     */
    public static class BoundedDataSet
    {
        private final DataSet dataSet;
        private final Bounds bounds;

        public BoundedDataSet(final DataSet dataSet, final Bounds bounds)
        {
            this.dataSet = dataSet;
            this.bounds = bounds;
        }

        public Bounds getBounds()
        {
            return this.bounds;
        }

        public DataSet getDataSet()
        {
            return this.dataSet;
        }
    }

    public BoundedDataSet build(final GeoJsonObject data)
    {
        DataSet dataSet = null;
        Bounds bounds = null;
        dataSet = new DataSet();
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

                        if (bounds == null)
                        {
                            bounds = new Bounds(latlon);
                        }
                        else
                        {
                            bounds.extend(latlon);
                        }

                        final Node n = new Node(latlon);
                        dataSet.addPrimitive(n);
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

                    dataSet.addPrimitive(w);
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

                            if (bounds == null)
                            {
                                bounds = new Bounds(latlon);
                            }
                            else
                            {
                                bounds.extend(latlon);
                            }

                            final Node n = new Node(latlon);
                            dataSet.addPrimitive(n);
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

                    dataSet.addPrimitive(w);
                }
            }
        }
        return new BoundedDataSet(dataSet, bounds);
    }
}
