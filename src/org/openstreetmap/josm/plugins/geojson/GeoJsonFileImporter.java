package org.openstreetmap.josm.plugins.geojson;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;

import org.geojson.GeoJsonObject;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.FileImporter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeoJsonFileImporter extends FileImporter {

	public GeoJsonFileImporter() {
		super(new ExtensionFileFilter("geojson,json", "geojson", tr("GeoJSON file") + " (*.geojson,*.json)"));
	}

	@Override
	public void importData(File file, ProgressMonitor progressMonitor) {
		GeoJsonObject object = null;

		System.out.println("Parsing GeoJSON: " + file.getAbsolutePath());
		try {
			object = new ObjectMapper().readValue(file, GeoJsonObject.class);
			System.out.println("Found: " + object.getClass());
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		GeoJsonLayer layer = new GeoJsonLayer("GeoJSON: " + file.getName(), object);
		Main.main.addLayer(layer);
		System.out.println("Added layer.");
	}

}
