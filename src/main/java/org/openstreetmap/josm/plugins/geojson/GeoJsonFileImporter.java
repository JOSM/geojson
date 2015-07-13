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

/**
 * @author Ian Dees <ian.dees@gmail.com>
 * @author matthieun <https://github.com/matthieun>
 */
public class GeoJsonFileImporter extends FileImporter
{
    private GeoJsonLayer layer = null;

    public GeoJsonFileImporter()
    {
        super(new ExtensionFileFilter("geojson,json", "geojson", tr("GeoJSON file")
                + " (*.geojson,*.json)"));
    }

    public GeoJsonLayer getLayer()
    {
        return this.layer;
    }

    @Override
    public void importData(final File file, final ProgressMonitor progressMonitor)
    {
        GeoJsonObject object = null;

        System.out.println("Parsing GeoJSON: " + file.getAbsolutePath());
        try
        {
            object = new ObjectMapper().readValue(file, GeoJsonObject.class);
            System.out.println("Found: " + object.getClass());
        }
        catch (final JsonParseException e)
        {
            e.printStackTrace();
        }
        catch (final JsonMappingException e)
        {
            e.printStackTrace();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }

        this.layer = new GeoJsonLayer("GeoJSON: " + file.getName(), object);
        Main.main.addLayer(this.layer);
        System.out.println("Added layer.");
    }
}
