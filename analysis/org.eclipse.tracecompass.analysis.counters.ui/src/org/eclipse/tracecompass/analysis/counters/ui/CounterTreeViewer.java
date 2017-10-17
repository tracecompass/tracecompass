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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.counters.core.CounterDataProvider;
import org.eclipse.tracecompass.analysis.counters.core.CounterEntryModel;
import org.eclipse.tracecompass.internal.analysis.counters.ui.CounterTreeViewerEntry;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer;
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
                String name = ((CounterTreeViewerEntry) element).getModel().getFullPath();
                return getLegendImage(name);
            }
            return null;
        }
    }

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
        super(parent, checkboxTree, 1);
        setLabelProvider(new CounterTreeLabelProvider());
    }

    @Override
    public void initializeDataSource() {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return;
        }
        DataProviderManager.getInstance().getDataProvider(trace, CounterDataProvider.ID, ITmfTreeDataProvider.class);
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> Lists.newArrayList(new TmfTreeColumnData("Counters"), new TmfTreeColumnData("Legend")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected ITmfTreeViewerEntry updateElements(long start, long end, boolean isSelection) {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return null;
        }

        ITmfTreeDataProvider provider = DataProviderManager.getInstance().getDataProvider(trace, CounterDataProvider.ID, ITmfTreeDataProvider.class);
        if (provider == null) {
            return null;
        }

        TmfModelResponse<List<CounterEntryModel>> tree = provider.fetchTree(new TimeQueryFilter(0l, Long.MAX_VALUE, 2), null);

        List<CounterEntryModel> model = tree.getModel();
        if (model == null) {
            return null;
        }
        TmfTreeViewerEntry root = new TmfTreeViewerEntry(StringUtils.EMPTY);

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
        return root;
    }

}
