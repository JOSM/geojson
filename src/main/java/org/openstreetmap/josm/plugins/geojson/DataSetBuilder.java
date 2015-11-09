package org.openstreetmap.josm.plugins.geojson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.geojson.*;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.*;

/**
 * @author matthieun
 */
public class DataSetBuilder {
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

    public BoundedDataSet build(final GeoJsonObject data) {
        DataSet dataSet = new DataSet();
        if (data instanceof FeatureCollection) {
            processFeatureCollection(dataSet, (FeatureCollection) data);
        } else if (data instanceof GeometryCollection) {
            processGeometryCollection(dataSet, null, (GeometryCollection) data);
        } else if (data instanceof Feature) {
            processFeature(dataSet, (Feature) data);
        } else {
            processGeometry(dataSet, null, data);
        }

        Bounds bounds = null;
        for (OsmPrimitive osmPrimitive : dataSet.allPrimitives()) {
            bounds = mergeBounds(bounds, osmPrimitive);
        }
        return new BoundedDataSet(dataSet, bounds);
    }

    private void processFeatureCollection(DataSet dataSet, FeatureCollection data) {
        for (final Feature feature : data) {
            processFeature(dataSet, feature);
        }
    }

    private void processGeometryCollection(DataSet dataSet, Feature feature, GeometryCollection geometryCollection) {
        for (GeoJsonObject geometry : geometryCollection) {
            processGeometry(dataSet, feature, geometry);
        }
    }

    private void processFeature(DataSet dataSet, Feature feature) {
        processGeometry(dataSet, feature, feature.getGeometry());
    }

    private void processMultiPoint(DataSet dataSet, Feature feature, MultiPoint multiPoint) {
        for (LngLatAlt point : multiPoint.getCoordinates()) {
            processPoint(dataSet, feature, point);
        }
    }

    private void processGeometry(DataSet dataSet, Feature feature, GeoJsonObject geometry) {
        if (geometry instanceof Feature) {
            processGeometry(dataSet, (Feature) geometry, ((Feature) geometry).getGeometry());
        } else {
            if (geometry instanceof Point) {
                processPoint(dataSet, feature, ((Point) geometry).getCoordinates());
            } else if (geometry instanceof LineString) {
                processLineString(dataSet, feature, ((LineString) geometry).getCoordinates());
            } else if (geometry instanceof Polygon) {
                processPolygon(dataSet, feature, ((Polygon) geometry).getCoordinates());
            } else if (geometry instanceof MultiPoint) {
                processMultiPoint(dataSet, feature, (MultiPoint) geometry);
            } else if (geometry instanceof MultiLineString) {
                processMultiLineString(dataSet, feature, (MultiLineString) geometry);
            } else if (geometry instanceof MultiPolygon) {
                processMultiPolygon(dataSet, feature, (MultiPolygon) geometry);
            } else if (geometry instanceof GeometryCollection) {
                processGeometryCollection(dataSet, feature, (GeometryCollection) geometry);
            }
        }
    }

    private void processMultiPolygon(DataSet dataSet, Feature feature, MultiPolygon geometry) {
        for (List<List<LngLatAlt>> polygon : geometry.getCoordinates()) {
            processPolygon(dataSet, feature, polygon);
        }
    }

    private void processMultiLineString(DataSet dataSet, Feature feature, MultiLineString multiLineString) {
        for (List<LngLatAlt> coordinates : multiLineString.getCoordinates()) {
            processLineString(dataSet, feature, coordinates);
        }
    }

    private void processPoint(DataSet dataSet, Feature feature, LngLatAlt geometry) {
        final Node node = createNode(dataSet, geometry);

        fillTagsFromFeature(feature, node);
    }

    private void processLineString(DataSet dataSet, Feature feature, List<LngLatAlt> coordinates) {
        final Way way = createWay(dataSet, coordinates);

        fillTagsFromFeature(feature, way);
    }

    private void processPolygon(DataSet dataSet, Feature feature, List<List<LngLatAlt>> coordinates) {
        if (coordinates.size() == 1) {
            // create simple way
            createWay(dataSet, coordinates.get(0));

        } else if (coordinates.size() > 1) {
            // create multipolygon
            final Relation multipolygon = new Relation();
            multipolygon.put("type", "multipolygon");
            Way way = createWay(dataSet, coordinates.get(0));
            multipolygon.addMember(new RelationMember("outer", way));

            for (List<LngLatAlt> interiorRings : coordinates.subList(1, coordinates.size())) {
                way = createWay(dataSet, interiorRings);
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

    private Node createNode(DataSet dataSet, LngLatAlt point) {
        final LatLon latlon = new LatLon(point.getLatitude(), point.getLongitude());
        Node node = new Node(latlon);

        dataSet.addPrimitive(node);

        return node;
    }

    private Way createWay(DataSet dataSet, List<LngLatAlt> coordinates) {
        final Way way = new Way();

        final List<Node> nodes = new ArrayList<>(coordinates.size());

        for (final LngLatAlt point : coordinates) {
            final Node node = createNode(dataSet, point);

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
            tags.put(entry.getKey(), entry.getValue().toString());
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
