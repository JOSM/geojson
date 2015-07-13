package org.openstreetmap.josm.plugins.geojson;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveId;
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
        private final long index;

        public PrintableWay(final long index, final Way way)
        {
            this.way = way;
            this.index = index;
        }

        public long getIndex()
        {
            return this.index;
        }

        public Way getWay()
        {
            return this.way;
        }

        @Override
        public String toString()
        {
            final StringBuilder result = new StringBuilder();
            result.append("Index: ");
            result.append(this.index);
            result.append(", Tags: ");
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
        if (layer != null && layer.getData() != null && layer.getData().allPrimitives() != null
                && !layer.getData().allPrimitives().isEmpty())
        {
            add(this.panel, BorderLayout.CENTER);

            final DefaultListModel<PrintableWay> model = new DefaultListModel<PrintableWay>();
            final Map<Integer, PrimitiveId> indexToIdentifier = new HashMap<Integer, PrimitiveId>();
            int index = 0;

            for (final OsmPrimitive osmPrimitive : layer.getData().allPrimitives())
            {
                if (osmPrimitive instanceof Way)
                {
                    model.addElement(new PrintableWay(index, (Way) osmPrimitive));
                    indexToIdentifier.put(index, osmPrimitive.getPrimitiveId());
                    index++;
                }
            }
            final JList<PrintableWay> list = new JList<PrintableWay>(model);
            list.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(final MouseEvent event)
                {
                    // The index clicked in the list
                    final int index = list.locationToIndex(event.getPoint());
                    layer.getData().setSelected(indexToIdentifier.get(index));
                }
            });
            this.panel.add(new JScrollPane(list), BorderLayout.CENTER);

            System.out.println("Created Dialog");
        }
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
