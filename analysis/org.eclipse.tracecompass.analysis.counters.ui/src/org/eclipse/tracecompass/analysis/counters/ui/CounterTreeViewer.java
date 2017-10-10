/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tracecompass.analysis.counters.core.CounterDataProvider;
import org.eclipse.tracecompass.analysis.counters.core.CounterEntryModel;
import org.eclipse.tracecompass.internal.analysis.counters.ui.CounterTreeViewerEntry;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.ILegendImageProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractTmfTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TriStateFilteredCheckboxTree;

import com.google.common.collect.Lists;

/**
 * Display the state system as a filtered checkbox tree:
 *
 * <pre>
 * {trace name}
 *   +- Grouped
 *   |   +- {group id}
 *   |   |   +- {group element}
 *   |   |       +- ...
 *   |   +- {group id}
 *   |       +- ...
 *   +- Ungrouped
 *       +- {counter}
 *       +- ...
 * </pre>
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 */
public class CounterTreeViewer extends AbstractSelectTreeViewer {

    private final class CounterTreeLabelProvider extends TreeLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 1 && element instanceof CounterTreeViewerEntry && isChecked(element)) {
                /* If the image height match the row height, row height will increment */
                int imageHeight = getTreeViewer().getTree().getItemHeight() - 1;
                String name = ((CounterTreeViewerEntry) element).getModel().getFullPath();
                ILegendImageProvider legendImageProvider = getLegendImageProvider();
                if (legendImageProvider != null) {
                    return legendImageProvider.getLegendImage(imageHeight, fLegendColumnWidth, name);
                }
                return null;
            }
            return null;
        }
    }
    private Map<ITmfTrace, TmfTreeViewerEntry> fRoots = new HashMap<>();
    private final int fLegendColumnWidth;

    /**
     * Constructor
     *
     * @param parent
     *            Parent composite
     * @param checkboxTree
     *            <code>TriStateFilteredTree</code> wrapping a
     *            <code>CheckboxTreeViewer</code>
     */
    public CounterTreeViewer(Composite parent, TriStateFilteredCheckboxTree checkboxTree) {
        super(parent, checkboxTree);
        setLabelProvider(new CounterTreeLabelProvider());

        /* Legend column is at index 1 */
        TreeColumn legend = getTreeViewer().getTree().getColumn(1);
        legend.pack();
        fLegendColumnWidth = legend.getWidth() != 0 ? legend.getWidth() : 50;
    }

    @Override
    public void initializeDataSource() {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return;
        }

        if (fRoots.containsKey(trace)) {
            // Return if the trace has already been initialized
            return;
        }

        ITmfTreeDataProvider provider = DataProviderManager.getInstance().getDataProvider(trace, CounterDataProvider.ID, ITmfTreeDataProvider.class);
        if (provider == null) {
            return;
        }

        TmfModelResponse<List<CounterEntryModel>> tree = provider.fetchTree(new TimeQueryFilter(0l, Long.MAX_VALUE, 2), null);

        List<CounterEntryModel> model = tree.getModel();
        if (model == null) {
            return;
        }
        TmfTreeViewerEntry root = new TmfTreeViewerEntry(StringUtils.EMPTY);
        fRoots.put(trace, root);

        Map<Long, TmfTreeViewerEntry> map = new HashMap<>();
        map.put(-1L, root);
        for (CounterEntryModel entry : model) {
            String fullPath = entry.getFullPath();
            TmfTreeViewerEntry viewerEntry;
            if (fullPath == null) {
                viewerEntry = new TmfTreeViewerEntry(entry.getName());
            } else {
                viewerEntry = new CounterTreeViewerEntry(entry);
            }
            map.put(entry.getId(), viewerEntry);
            TmfTreeViewerEntry parent = map.get(entry.getParentId());
            if (parent != null && !parent.getChildren().contains(viewerEntry)) {
                parent.addChild(viewerEntry);
            }
        }
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> Lists.newArrayList(new TmfTreeColumnData("Counters"), new TmfTreeColumnData("Legend")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected ITmfTreeViewerEntry updateElements(long start, long end, boolean isSelection) {
        /*
         * The tree displaying the trace's counters does not change when manipulating
         * the corresponding chart.
         */
        return fRoots.get(getTrace());
    }

    /**
     * Method called when the trace is closed
     * <p>
     * renamed so that it does not override
     * {@link AbstractTmfTreeViewer#traceClosed(TmfTraceClosedSignal)}
     * <p>
     * final - do not call
     *
     * @param signal
     *            unused
     */
    @TmfSignalHandler
    public final void traceClosedCounter(@Nullable TmfTraceClosedSignal signal) {
        if (signal != null) {
            fRoots.remove(signal.getTrace());
        }
    }

}
