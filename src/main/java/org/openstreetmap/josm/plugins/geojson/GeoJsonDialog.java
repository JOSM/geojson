package org.openstreetmap.josm.plugins.geojson;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.Layer;

/**
 * @author matthieun
 */
public class GeoJsonDialog extends ToggleDialog implements LayerChangeListener
{
    /**
     * @author matthieun
     */
    public static class PrintableWay
    {
        private final Way way;

        public PrintableWay(final Way way)
        {
            this.way = way;
        }

        public Way getWay()
        {
            return this.way;
        }

        @Override
        public String toString()
        {
            final StringBuilder result = new StringBuilder();
            result.append(this.way.getUniqueId());
            result.append(this.way.getKeys());
            return result.toString();
        }
    }

    private static final long serialVersionUID = 2182365950017739421L;

    private final GeoJsonLayer layer;
    private final JPanel panel;

    public GeoJsonDialog(final GeoJsonLayer layer)
    {
        super("GeoJson", "activate.png", "Opens the GeoJson plugin pane", null, 150);
        this.layer = layer;
        this.panel = new JPanel(new BorderLayout());
        this.panel.setName("GeoJson Panel");
        add(this.panel, BorderLayout.CENTER);

        final DefaultListModel<PrintableWay> model = new DefaultListModel<PrintableWay>();
        layer.getData().allPrimitives().forEach(osmPrimitive -> {
            if (osmPrimitive instanceof Way)
            {
                final StringBuilder result = new StringBuilder();
                result.append(osmPrimitive.getUniqueId());
                result.append(osmPrimitive.getKeys());
                model.addElement(new PrintableWay((Way) osmPrimitive));
            }
        });
        final JList<PrintableWay> list = new JList<PrintableWay>(model);
        this.panel.add(new JScrollPane(list), BorderLayout.CENTER);

        System.out.println("Created Dialog");
    }

    @Override
    public void activeLayerChange(final Layer layer1, final Layer layer2)
    {
    }

    @Override
    public void layerAdded(final Layer newLayer)
    {
        if (newLayer == this.layer)
        {
            Main.map.mapView.moveLayer(newLayer, 0);
        }
    }

    @Override
    public void layerRemoved(final Layer arg0)
    {
    }
}
