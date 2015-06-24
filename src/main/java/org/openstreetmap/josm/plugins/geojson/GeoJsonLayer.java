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
 *
 */
public class GeoJsonLayer extends Layer {

	private final DataSet data;
	private Bounds bounds = null;

	public GeoJsonLayer(String name, GeoJsonObject data) {
		super(name);

		this.data = new DataSet();
		if (data instanceof FeatureCollection) {
			FeatureCollection fc = (FeatureCollection) data;
			for (Feature feature : fc) {
				GeoJsonObject geometry = feature.getGeometry();
				if (geometry instanceof LineString) {
					Way w = new Way();

					List<LngLatAlt> coordinates = ((LineString) geometry).getCoordinates();
					List<Node> nodes = new ArrayList<Node>(coordinates.size());
					for (LngLatAlt lngLatAlt : coordinates) {
						LatLon latlon = new LatLon(lngLatAlt.getLatitude(), lngLatAlt.getLongitude());

						if (bounds == null) {
							bounds = new Bounds(latlon);
						} else {
							bounds.extend(latlon);
						}

						Node n = new Node(latlon);
						this.data.addPrimitive(n);
						nodes.add(n);
					}
					w.setNodes(nodes);

					Map<String, Object> properties = feature.getProperties();
					Map<String, String> tags = new TreeMap<String, String>();
					for (Entry<String, Object> entry : properties.entrySet()) {
						tags.put(entry.getKey(), entry.getValue().toString());
					}
					w.setKeys(tags);

					this.data.addPrimitive(w);
				} else if (geometry instanceof Polygon) {
					Way w = new Way();

					List<List<LngLatAlt>> coordinates = ((Polygon) geometry).getCoordinates();
					List<Node> nodes = new ArrayList<Node>(coordinates.size());
					for (LngLatAlt lngLatAlt : coordinates) {
						LatLon latlon = new LatLon(lngLatAlt.getLatitude(), lngLatAlt.getLongitude());

						if (bounds == null) {
							bounds = new Bounds(latlon);
						} else {
							bounds.extend(latlon);
						}

						Node n = new Node(latlon);
						this.data.addPrimitive(n);
						nodes.add(n);
					}
					w.setNodes(nodes);

					Map<String, Object> properties = feature.getProperties();
					Map<String, String> tags = new TreeMap<String, String>();
					for (Entry<String, Object> entry : properties.entrySet()) {
						tags.put(entry.getKey(), entry.getValue().toString());
					}
					w.setKeys(tags);

					this.data.addPrimitive(w);
				}
			}
		}
	}

	@Override
	public void paint(Graphics2D g, MapView mv, Bounds box) {
		Rendering painter = MapRendererFactory.getInstance().createActiveRenderer(g, mv, false);
		painter.render(data, false, box);
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
	public void mergeFrom(Layer from) {
		return;
	}

	@Override
	public boolean isMergable(Layer other) {
		return false;
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor v) {
		v.visit(bounds);
	}

	@Override
	public Object getInfoComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Action[] getMenuEntries() {
		// TODO Auto-generated method stub
		return null;
	}

}
