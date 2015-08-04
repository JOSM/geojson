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
import org.geojson.Point;
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
                if (geometry instanceof Point)
                {
                    final LngLatAlt coordinate = ((Point) geometry).getCoordinates();
                    final LatLon latlon = new LatLon(coordinate.getLatitude(),
                            coordinate.getLongitude());
                    final Node node = new Node(latlon);
                    node.setKeys(getTags(feature));
                    dataSet.addPrimitive(node);
                }
                if (geometry instanceof LineString)
                {
                    final Way way = new Way();

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

                        final Node node = new Node(latlon);
                        dataSet.addPrimitive(node);
                        nodes.add(node);
                    }
                    way.setNodes(nodes);
                    way.setKeys(getTags(feature));
                    dataSet.addPrimitive(way);
                }
                else if (geometry instanceof Polygon)
                {
                    final Way way = new Way();

                    final List<List<LngLatAlt>> coordinates = ((Polygon) geometry).getCoordinates();
                    final List<Node> nodes = new ArrayList<Node>(coordinates.size());
                    int counter = 0;
                    Node first = null;
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

                            final Node node = new Node(latlon);
                            dataSet.addPrimitive(node);
                            nodes.add(node);
                            if (counter == 0)
                            {
                                first = node;
                            }
                            counter++;
                        }
                    }
                    if (first != null)
                    {
                        nodes.add(first);
                    }
                    way.setNodes(nodes);
                    way.setKeys(getTags(feature));
                    dataSet.addPrimitive(way);
                }
            }
        }
        return new BoundedDataSet(dataSet, bounds);
    }

    private Map<String, String> getTags(final Feature feature)
    {
        final Map<String, Object> properties = feature.getProperties();
        final Map<String, String> tags = new TreeMap<String, String>();
        for (final Entry<String, Object> entry : properties.entrySet())
        {
            tags.put(entry.getKey(), entry.getValue().toString());
        }
        return tags;
    }
}
