package org.openstreetmap.josm.plugins.geojson;

import java.awt.Graphics2D;

import javax.swing.Action;
import javax.swing.Icon;

import org.geojson.GeoJsonObject;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
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

	private final GeoJsonObject data;

	public GeoJsonLayer(String name, GeoJsonObject data) {
		super(name);
		this.data = data;
		System.out.println(this.data.getClass());
	}

	@Override
	public void paint(Graphics2D g, MapView mv, Bounds box) {

	}

	@Override
	public Icon getIcon() {
		return ImageProvider.get("data", "Way");
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
		double[] bbox = data.getBbox();
		Bounds bounds = new Bounds(bbox);
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
