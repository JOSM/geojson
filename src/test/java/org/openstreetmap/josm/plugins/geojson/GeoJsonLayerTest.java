package org.openstreetmap.josm.plugins.geojson;

import org.geojson.GeoJsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author matthieun <https://github.com/matthieun>
 */
public class GeoJsonLayerTest
{
    // @Test
    public void testLoadFile()
    {
        try
        {
            final GeoJsonObject object = new ObjectMapper().readValue(
                    GeoJsonLayerTest.class.getResourceAsStream("geo.json"), GeoJsonObject.class);
            @SuppressWarnings("unused")
            final GeoJsonLayer layer = new GeoJsonLayer("Test Layer", object);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
