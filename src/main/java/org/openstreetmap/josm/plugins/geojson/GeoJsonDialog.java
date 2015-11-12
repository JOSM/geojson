package org.openstreetmap.josm.plugins.geojson;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.Layer;

/**
 * Dialog that contains a listing of all the features opened by the GeoJson plugin.
 *
 * @author matthieun
 */
public class GeoJsonDialog extends ToggleDialog implements LayerChangeListener {
    /**
     * Wrapper for an item to be displayed in the list
     *
     * @author matthieun
     */
    public static class PrintablePrimitive
    {

        private final OsmPrimitive osmPrimitive;

        private final long index;
        public PrintablePrimitive(final long index, final OsmPrimitive osmPrimitive)
        {
            this.osmPrimitive = osmPrimitive;
            this.index = index;
        }
        public long getIndex()
        {
            return this.index;
        }

        public OsmPrimitive getOsmPrimitive()
        {
            return this.osmPrimitive;
        }

        @Override
        public String toString()
        {
            final StringBuilder result = new StringBuilder();
            result.append("Index: ");
            result.append(this.index);
            result.append(", Tags: ");
            result.append(this.osmPrimitive.getKeys());
            return result.toString();
        }

    }

    private static final long serialVersionUID = 2182365950017739421L;
    private static boolean stopProcessingCallbacks = false;

    private GeoJsonLayer layer;

    private final JPanel panel;
    private Map<Integer, PrimitiveId> indexToIdentifier;
    private Map<PrimitiveId, Integer> identifierToIndex;
    private DefaultListModel<PrintablePrimitive> listModel;

    /**
     * Create the dialog. Wire the map and the list together so clicks on one selects the other.
     */
    public GeoJsonDialog() {

        super("GeoJson object list", "activate.png", "Opens the GeoJson object list pane", null, 150);
        this.panel = new JPanel(new BorderLayout());
        this.panel.setName("GeoJson object list");
        add(this.panel, BorderLayout.CENTER);

        this.listModel = new DefaultListModel<>();
        final JList<PrintablePrimitive> list = new JList<>(listModel);
        this.panel.add(new JScrollPane(list), BorderLayout.CENTER);

        this.indexToIdentifier = new HashMap<>();
        this.identifierToIndex = new HashMap<>();

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent listSelectionEvent) {
                if (stopProcessingCallbacks) {
                    return;
                }
                try {
                    stopProcessingCallbacks = true;
                    JList selectionModel = (JList) listSelectionEvent.getSource();
                    final int selectedIndex = selectionModel.getMinSelectionIndex();
                    final PrimitiveId identifier = indexToIdentifier.get(selectedIndex);
                    layer.getData().setSelected(identifier);
                    zoomTo(layer.getData().getPrimitiveById(identifier));
                } finally {
                    stopProcessingCallbacks = false;
                }
            }
        });
        // The listener for clicks on the map
        DataSet.addSelectionListener(new SelectionChangedListener() {
            @Override
            public void selectionChanged(final Collection<? extends OsmPrimitive> selection) {
                if (stopProcessingCallbacks) {
                    return;
                }
                try {
                    stopProcessingCallbacks = true;
                    for (final OsmPrimitive feature : selection) {
                        if (identifierToIndex.containsKey(feature.getPrimitiveId())) {
                            final int idx = identifierToIndex.get(feature.getPrimitiveId());
                            list.setSelectedIndices(new int[]{idx});
                            list.ensureIndexIsVisible(idx);
                            break;
                        }
                    }
                } finally {
                    stopProcessingCallbacks = false;
                }
            }
        });
    }

    private void showDataFromLayer() {
        listModel.clear();
        indexToIdentifier.clear();
        identifierToIndex.clear();

        if (layer != null && layer.getData() != null && !layer.getData().allPrimitives().isEmpty())
        {
            int index = 0;

            // Build the maps and add the primitives to the list's model
            for (final OsmPrimitive osmPrimitive : layer.getData().allPrimitives())
            {
                if (osmPrimitive instanceof Node && osmPrimitive.getKeys().isEmpty()) { // skip points without tags
                        continue;
                }
                listModel.addElement(new PrintablePrimitive(index, osmPrimitive));
                indexToIdentifier.put(index, osmPrimitive.getPrimitiveId());
                identifierToIndex.put(osmPrimitive.getPrimitiveId(), index);
                index++;
            }
        }
    }

    @Override
    public void activeLayerChange(final Layer oldLayer, final Layer newLayer) {
        if (newLayer instanceof GeoJsonLayer) {
            this.layer = (GeoJsonLayer) newLayer;
            showDataFromLayer();
        }
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

    /**
     * Focus the map on a specific OSM primitive
     *
     * @param primitive
     *            The primitive to zoom to
     */
    private void zoomTo(final OsmPrimitive primitive)
    {
        if (primitive == null) {
            return;
        }
        if (primitive instanceof Node)
        {
            Main.map.mapView.zoomTo(((Node) primitive).getCoor());
            return;
        }
        final BoundingXYVisitor v = new BoundingXYVisitor();
        v.visit((Way) primitive);
        Main.map.mapView.zoomTo(v.getBounds());
    }
}
