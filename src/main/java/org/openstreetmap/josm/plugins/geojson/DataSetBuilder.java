package org.openstreetmap.josm.plugins.geojson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.geojson.*;
import org.geojson.jackson.CrsType;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
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

    private MathTransform transform;
    private DataSet dataSet;

    public BoundedDataSet build(final GeoJsonObject data) {
        transform = IdentityTransform.create(2);
        dataSet = new DataSet();

        if (data.getCrs() != null) {
            if (data.getCrs().getType() != CrsType.name) {
                throw new UnsupportedOperationException("Only 'name' CRS are supported");
            }

            try {
                org.opengis.referencing.crs.CoordinateReferenceSystem crs = CRS.decode(data.getCrs().getProperties().get("name").toString(), true);
                org.opengis.referencing.crs.CoordinateReferenceSystem osmCrs = CRS.decode("EPSG:4326");
                transform = CRS.findMathTransform(crs, osmCrs);
            } catch (FactoryException e) {
                throw new UnsupportedOperationException("Unknown CRS " + data.getCrs().getProperties().get("name"), e);
            }
        }

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
        if (coordinates.size() == 0) {
            return;
        }
        final Way way = createWay(coordinates);

        fillTagsFromFeature(feature, way);
    }

    private void processPolygon(Feature feature, List<List<LngLatAlt>> coordinates) {
        if (coordinates.size() == 0) {
            return;
        }
        for (List<LngLatAlt> ring : coordinates) {
            if (ring.size() == 0) {
                return;
            }
        }
        if (coordinates.size() == 1) {
            // create simple way
            createWay(coordinates.get(0));

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
        try {
            transform.transform(pt, 0, pt, 0, 1);
        } catch (TransformException e) {
            throw new UnsupportedOperationException("Cannot transform a point from the input dataset to the EPSG:4326", e);
        }
        final LatLon latlon = new LatLon(pt[1], pt[0]);
        Node node = new Node(latlon);

        dataSet.addPrimitive(node);

        return node;
    }

    private Way createWay(List<LngLatAlt> coordinates) {
        if (coordinates.size() == 0) {
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
