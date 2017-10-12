package org.openstreetmap.josm.plugins.geojson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.GeometryCollection;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.MultiLineString;
import org.geojson.MultiPoint;
import org.geojson.MultiPolygon;
import org.geojson.Point;
import org.geojson.Polygon;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

/**
 * @author matthieun
 */
public class DataSetBuilder {

    public static final int MAX_LINK_LENGTH = 102400;

    /**
     * @author matthieun
     */
    public static class BoundedDataSet {
        private final DataSet dataSet;
        private final Bounds bounds;

        public BoundedDataSet(final DataSet dataSet, final Bounds bounds) {
            this.dataSet = dataSet;
            this.bounds = bounds;
        }

        public Bounds getBounds() {
            return this.bounds;
        }

        public DataSet getDataSet() {
            return this.dataSet;
        }
    }

    private DataSet dataSet;

    public BoundedDataSet build(final GeoJsonObject data) {
        dataSet = new DataSet();

        if (data instanceof FeatureCollection) {
            processFeatureCollection((FeatureCollection) data);
        } else if (data instanceof GeometryCollection) {
            processGeometryCollection(null, (GeometryCollection) data);
        } else if (data instanceof Feature) {
            processFeature((Feature) data);
        } else {
            processGeometry(null, data);
        }

        Bounds bounds = null;
        for (OsmPrimitive osmPrimitive : dataSet.allPrimitives()) {
            bounds = mergeBounds(bounds, osmPrimitive);
        }
        return new BoundedDataSet(dataSet, bounds);
    }

    private void processFeatureCollection(FeatureCollection data) {
        for (final Feature feature : data) {
            processFeature(feature);
        }
    }

    private void processGeometryCollection(Feature feature, GeometryCollection geometryCollection) {
        for (GeoJsonObject geometry : geometryCollection) {
            processGeometry(feature, geometry);
        }
    }

    private void processFeature(Feature feature) {
        processGeometry(feature, feature.getGeometry());
    }

    private void processMultiPoint(Feature feature, MultiPoint multiPoint) {
        for (LngLatAlt point : multiPoint.getCoordinates()) {
            processPoint(feature, point);
        }
    }

    private void processGeometry(Feature feature, GeoJsonObject geometry) {
        if (geometry instanceof Feature) {
            processGeometry((Feature) geometry, ((Feature) geometry).getGeometry());
        } else {
            if (geometry instanceof Point) {
                processPoint(feature, ((Point) geometry).getCoordinates());
            } else if (geometry instanceof LineString) {
                processLineString(feature, ((LineString) geometry).getCoordinates());
            } else if (geometry instanceof Polygon) {
                processPolygon(feature, ((Polygon) geometry).getCoordinates());
            } else if (geometry instanceof MultiPoint) {
                processMultiPoint(feature, (MultiPoint) geometry);
            } else if (geometry instanceof MultiLineString) {
                processMultiLineString(feature, (MultiLineString) geometry);
            } else if (geometry instanceof MultiPolygon) {
                processMultiPolygon(feature, (MultiPolygon) geometry);
            } else if (geometry instanceof GeometryCollection) {
                processGeometryCollection(feature, (GeometryCollection) geometry);
            }
        }
    }

    private void processMultiPolygon(Feature feature, MultiPolygon geometry) {
        for (List<List<LngLatAlt>> polygon : geometry.getCoordinates()) {
            processPolygon(feature, polygon);
        }
    }

    private void processMultiLineString(Feature feature, MultiLineString multiLineString) {
        for (List<LngLatAlt> coordinates : multiLineString.getCoordinates()) {
            processLineString(feature, coordinates);
        }
    }

    private void processPoint(Feature feature, LngLatAlt geometry) {
        final Node node = createNode(geometry);

        fillTagsFromFeature(feature, node);
    }

    private void processLineString(Feature feature, List<LngLatAlt> coordinates) {
        if (coordinates.isEmpty()) {
            return;
        }
        final Way way = createWay(coordinates);

        fillTagsFromFeature(feature, way);
    }

    private void processPolygon(Feature feature, List<List<LngLatAlt>> coordinates) {
        if (coordinates.isEmpty()) {
            return;
        }
        for (List<LngLatAlt> ring : coordinates) {
            if (ring.isEmpty()) {
                return;
            }
        }
        if (coordinates.size() == 1) {
            // create simple way
            final Way way = createWay(coordinates.get(0));

            fillTagsFromFeature(feature, way);

        } else if (coordinates.size() > 1) {
            // create multipolygon
            final Relation multipolygon = new Relation();
            multipolygon.put("type", "multipolygon");
            Way way = createWay(coordinates.get(0));
            multipolygon.addMember(new RelationMember("outer", way));

            for (List<LngLatAlt> interiorRings : coordinates.subList(1, coordinates.size())) {
                way = createWay(interiorRings);
                multipolygon.addMember(new RelationMember("inner", way));
            }
            fillTagsFromFeature(feature, multipolygon);
            dataSet.addPrimitive(multipolygon);
        }
    }

    private void fillTagsFromFeature(Feature feature, OsmPrimitive primitive) {
        if (feature != null) {
            primitive.setKeys(getTags(feature));
        }
    }

    private Node createNode(LngLatAlt point) {
        double[] pt = new double[] {point.getLongitude(), point.getLatitude()};
        final LatLon latlon = new LatLon(pt[1], pt[0]);
        Node node = new Node(latlon);

        dataSet.addPrimitive(node);

        return node;
    }

    private Way createWay(List<LngLatAlt> coordinates) {
        if (coordinates.isEmpty()) {
            return null;
        }
        final Way way = new Way();

        final List<Node> nodes = new ArrayList<>(coordinates.size());

        for (final LngLatAlt point : coordinates) {
            final Node node = createNode(point);

            nodes.add(node);
        }
        way.setNodes(nodes);

        dataSet.addPrimitive(way);

        return way;
    }

    private Map<String, String> getTags(final Feature feature) {
        final Map<String, Object> properties = feature.getProperties();
        final Map<String, String> tags = new TreeMap<>();
        for (final Entry<String, Object> entry : properties.entrySet()) {
            tags.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return tags;
    }

    private Bounds mergeBounds(Bounds bounds, OsmPrimitive osmPrimitive) {
        if (osmPrimitive instanceof Node) { // ways and relations consist of nodes that are already in the dataset
            bounds = mergeBounds(bounds, ((Node)osmPrimitive).getCoor());
        }
        return bounds;
    }

    private Bounds mergeBounds(Bounds bounds, LatLon coords) {
        if (bounds == null) {
            return new Bounds(coords);
        } else {
            bounds.extend(coords);
            return bounds;
        }
    }
}
