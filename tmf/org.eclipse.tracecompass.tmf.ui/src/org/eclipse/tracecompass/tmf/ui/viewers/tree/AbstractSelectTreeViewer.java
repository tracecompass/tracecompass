/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.ILegendImageProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.MultiTreePatternFilter;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TriStateFilteredCheckboxTree;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;

/**
 * Abstract viewer for trees with checkboxes.
 *
 * @since 3.2
 * @author Loic Prieur-Drevon
 */
public abstract class AbstractSelectTreeViewer extends AbstractTmfTreeViewer {

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(AbstractSelectTreeViewer.class);

    /** Timeout between updates in the updateData thread **/
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    /** ID of the checked tree items in the map of data in {@link TmfTraceContext} */
    private static final @NonNull String CHECKED_ELEMENTS = ".CHECKED_ELEMENTS"; //$NON-NLS-1$
    private static final @NonNull String UPDATE_CONTENT_JOB_NAME = "AbstractSelectTreeViewer#updateContent Job"; //$NON-NLS-1$
    private static final String FAILED_TO_SLEEP_PREFIX = "Failed to sleep the "; //$NON-NLS-1$

    private final String fId;

    private static final ViewerComparator COMPARATOR = new ViewerComparator() {
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            TmfTreeViewerEntry entry1 = (TmfTreeViewerEntry) e1;
            TmfTreeViewerEntry entry2 = (TmfTreeViewerEntry) e2;
            String name1 = entry1.getName();
            String name2 = entry2.getName();
            Long longValue1 = Longs.tryParse(name1);
            Long longValue2 = Longs.tryParse(name2);

            return (longValue1 == null || longValue2 == null) ? name1.compareTo(name2) : longValue1.compareTo(longValue2);
        }
    };

    private final class CheckStateChangedListener implements ICheckStateListener {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            if (fChartViewer != null) {
                fChartViewer.handleCheckStateChangedEvent(getCheckedViewerEntries());

                // Legend image might have changed
                refresh();
            }
        }
    }

    private ILegendImageProvider fLegendImageProvider;
    private ICheckboxTreeViewerListener fChartViewer;
    private TriStateFilteredCheckboxTree fCheckboxTree;
    private final int fLegendIndex;

    /**
     * Constructor
     *
     * @param parent
     *            Parent composite
     * @param checkboxTree
     *            <code>TriStateFilteredTree</code> wrapping a
     *            <code>CheckboxTreeViewer</code>
     * @param legendColumnIndex
     *            index of the legend column (-1 if none)
     */
    private AbstractSelectTreeViewer(Composite parent, TriStateFilteredCheckboxTree checkboxTree,
            int legendIndex, String id) {
        super(parent, checkboxTree.getViewer());

        TreeViewer treeViewer = checkboxTree.getViewer();
        treeViewer.setComparator(COMPARATOR);
        if (treeViewer instanceof CheckboxTreeViewer) {
            ((CheckboxTreeViewer) treeViewer).addCheckStateListener(new CheckStateChangedListener());
        }
        fCheckboxTree = checkboxTree;
        fLegendIndex = legendIndex;
        fId = id;
    }

    /**
     * Constructor
     *
     * @param parent
     *            Parent composite
     * @param legendIndex
     *            index of the legend column (-1 if none)
     * @param id
     *            {@link ITmfTreeDataProvider} ID
     */
    public AbstractSelectTreeViewer(Composite parent, int legendIndex, String id) {
        // Create the tree viewer with a filtered checkbox
        this(parent, new TriStateFilteredCheckboxTree(parent,
                SWT.MULTI | SWT.H_SCROLL | SWT.FULL_SELECTION,
                new MultiTreePatternFilter(), true), legendIndex, id);
    }

    /**
     * Tell the chart viewer to listen to changes in the tree viewer
     *
     * @param listener
     *            Chart listening to changes in the tree's selected entries
     */
    public void setTreeListener(ICheckboxTreeViewerListener listener) {
        fChartViewer = listener;
    }

    /**
     * Set the legend image provider (provider tree cells with an image).
     *
     * @param legendImageProvider
     *            Provides an image legend associated with a name
     */
    public void setLegendImageProvider(ILegendImageProvider legendImageProvider) {
        fLegendImageProvider = legendImageProvider;
    }

    /**
     * Return the checked state of an element
     *
     * @param element
     *            the element
     * @return if the element is checked
     */
    public boolean isChecked(Object element) {
        return fCheckboxTree.getChecked(element);
    }

    /**
     * Select previously checked entries when going back to trace.
     */
    @Override
    protected void contentChanged(ITmfTreeViewerEntry rootEntry) {
        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        Set<Long> ids = (Set<Long>) ctx.getData(getClass() + CHECKED_ELEMENTS);
        if (ids != null && rootEntry != null) {
            List<ITmfTreeViewerEntry> checkedElements = new ArrayList<>();
            checkEntries(ids, rootEntry, checkedElements);
            fCheckboxTree.setCheckedElements(checkedElements.toArray());
        }

        if (fChartViewer != null) {
            fChartViewer.handleCheckStateChangedEvent(getCheckedViewerEntries());
        }
        getTreeViewer().refresh();
    }

    private Collection<ITmfTreeViewerEntry> getCheckedViewerEntries() {
        Object[] checkedElements = fCheckboxTree.getCheckedElements();
        return Lists.newArrayList(Iterables.filter(Arrays.asList(checkedElements), ITmfTreeViewerEntry.class));
    }

    /**
     * Recursively find entries which were previously checked and check them again
     * by id.
     *
     * @param ids
     *            Set of previously checked IDs
     * @param root
     *            {@link ITmfTreeViewerEntry} to compare to the set of checked
     *            entries
     * @param checkedElements
     *            list of checked entries to which we add the root entry if it was
     *            previously checked
     */
    private void checkEntries(Set<Long> ids, ITmfTreeViewerEntry root, List<ITmfTreeViewerEntry> checkedElements) {
        if (root instanceof TmfGenericTreeEntry
                && ids.contains(((TmfGenericTreeEntry) root).getModel().getId())) {
            checkedElements.add(root);
        }
        for (ITmfTreeViewerEntry child : root.getChildren()) {
            checkEntries(ids, child, checkedElements);
        }
    }

    @Override
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        saveViewContext();
        super.traceOpened(signal);
    }

    @Override
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        if (signal != null && getTrace() != signal.getTrace()) {
            saveViewContext();
        }
        super.traceSelected(signal);
    }

    @Override
    protected void updateContent(long start, long end, boolean isSelection) {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, UPDATE_CONTENT_JOB_NAME)
                .setCategory("TreeViewer").build()) { //$NON-NLS-1$
            ITmfTrace trace = getTrace();
            if (trace == null) {
                return;
            }
            Job thread = new Job(UPDATE_CONTENT_JOB_NAME) {
                @Override
                public IStatus run(IProgressMonitor monitor) {
                    try (FlowScopeLog runScope = new FlowScopeLogBuilder(LOGGER, Level.FINE, UPDATE_CONTENT_JOB_NAME + " run") //$NON-NLS-1$
                            .setParentScope(scope).build()) {

                        ITmfTreeDataProvider<@NonNull TmfTreeDataModel> provider = getProvider(trace);
                        if (provider == null) {
                            Activator.getDefault().logInfo("Trace: " + trace.getName() + " does not have a data provider for ID: " + fId); //$NON-NLS-1$ //$NON-NLS-2$
                            return Status.OK_STATUS;
                        }

                        TimeQueryFilter filter = getFilter(start, end, isSelection);
                        if (filter == null) {
                            return Status.OK_STATUS;
                        }

                        boolean isComplete = false;
                        do {
                            TmfModelResponse<@NonNull List<@NonNull TmfTreeDataModel>> response;
                            try (FlowScopeLog iterScope = new FlowScopeLogBuilder(LOGGER, Level.FINE, UPDATE_CONTENT_JOB_NAME + " query") //$NON-NLS-1$
                                    .setParentScope(scope).build()) {

                                response = provider.fetchTree(filter, monitor);
                                List<@NonNull TmfTreeDataModel> model = response.getModel();
                                if (model != null) {
                                    updateTree(trace, start, end, model);
                                }
                            }

                            ITmfResponse.Status status = response.getStatus();
                            if (status == ITmfResponse.Status.COMPLETED) {
                                /* Model is complete, no need to request again the data provider */
                                isComplete = true;
                            } else if (status == ITmfResponse.Status.FAILED || status == ITmfResponse.Status.CANCELLED) {
                                /* Error occurred, return */
                                isComplete = true;
                            } else {
                                /**
                                 * Status is RUNNING. Sleeping current thread to wait before request data
                                 * provider again
                                 **/
                                try {
                                    Thread.sleep(BUILD_UPDATE_TIMEOUT);
                                } catch (InterruptedException e) {
                                    /**
                                     * InterruptedException is throw by Thread.Sleep and we should retry querying
                                     * the data provider
                                     **/
                                    runScope.addData(FAILED_TO_SLEEP_PREFIX + getName(), e);
                                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, FAILED_TO_SLEEP_PREFIX + getName());
                                }
                            }
                        } while (!isComplete);

                        return Status.OK_STATUS;
                    }
                }
            };
            thread.setSystem(true);
            thread.schedule();
        }
    }

    private void updateTree(ITmfTrace trace, long start, long end, List<@NonNull TmfTreeDataModel> model) {
        final ITmfTreeViewerEntry rootEntry = modelToTree(start, end, model);
        /* Set the input in main thread only if it didn't change */
        if (rootEntry != null) {
            Display.getDefault().asyncExec(() -> {
                if (!trace.equals(getTrace())) {
                    return;
                }
                TreeViewer treeViewer = getTreeViewer();
                if (treeViewer.getControl().isDisposed()) {
                    return;
                }

                if (!rootEntry.equals(treeViewer.getInput())) {
                    treeViewer.setInput(rootEntry);
                    contentChanged(rootEntry);
                } else {
                    treeViewer.refresh();
                    treeViewer.expandToLevel(treeViewer.getAutoExpandLevel());
                }
                // FIXME should add a bit of padding
                for (TreeColumn column : treeViewer.getTree().getColumns()) {
                    column.pack();
                }
            });
        }
    }

    /**
     * Save the checked entries' ID in the view context before changing trace and
     * check them again in the new tree.
     */
    private void saveViewContext() {
        ITmfTrace previousTrace = getTrace();
        if (previousTrace != null) {
            Object[] checkedElements = fCheckboxTree.getCheckedElements();
            Set<Long> ids = new HashSet<>();
            for (Object checkedElement : checkedElements) {
                if (checkedElement instanceof TmfGenericTreeEntry) {
                    ids.add(((TmfGenericTreeEntry) checkedElement).getModel().getId());
                }
            }
            TmfTraceManager.getInstance().updateTraceContext(previousTrace,
                    builder -> builder.setData(getClass() + CHECKED_ELEMENTS, ids));
        }
    }

    /**
     * Get the legend image for a entry's name
     *
     * @param name
     *            the entry's name (used in both Tree and Chart viewer
     * @return the correctly dimensioned image if there is a legend image provider
     */
    protected Image getLegendImage(@NonNull String name) {
        /* If the image height match the row height, row height will increment */
        ILegendImageProvider legendImageProvider = fLegendImageProvider;
        int legendColumnIndex = fLegendIndex;
        if (legendImageProvider != null && legendColumnIndex >= 0) {
            Tree tree = getTreeViewer().getTree();
            int imageWidth = tree.getColumn(legendColumnIndex).getWidth();
            int imageHeight = tree.getItemHeight() - 1;
            if (imageHeight > 0 && imageWidth > 0) {
                return legendImageProvider.getLegendImage(imageHeight, imageWidth, name);
            }
        }
        return null;
    }

    @Override
    protected ITmfTreeViewerEntry updateElements(ITmfTrace trace, long start, long end, boolean isSelection) {
        throw new UnsupportedOperationException("This method should not be called for AbstractSelectTreeViewers"); //$NON-NLS-1$
    }

    /**
     * Getter for the {@link ITmfTreeDataProvider} to query for this TreeViewer
     *
     * @trace the trace
     * @return the relevant provider, if any
     */
    protected ITmfTreeDataProvider<@NonNull TmfTreeDataModel> getProvider(@NonNull ITmfTrace trace) {
        return DataProviderManager.getInstance().getDataProvider(trace, fId, ITmfTreeDataProvider.class);
    }

    @Override
    protected void initializeDataSource(@NonNull ITmfTrace trace) {
        getProvider(trace);
    }

    /**
     * Get the filter to query the {@link ITmfTreeDataProvider} for the queried parameters
     *
     * @param start
     *            query start time
     * @param end
     *            query end time
     * @param isSelection
     *            if the query is a selection
     * @return the resulting query filter
     */
    protected @Nullable TimeQueryFilter getFilter(long start, long end, boolean isSelection) {
        return new TimeQueryFilter(Long.min(start, end), Long.max(start, end), 2);
    }

    /**
     * Algorithm to convert a model (List of {@link TmfTreeDataModel}) to the tree.
     *
     * @param start
     *            queried start time
     * @param end
     *            queried end time
     * @param model
     *            model to convert
     * @return the resulting {@link TmfTreeViewerEntry}.
     */
    protected ITmfTreeViewerEntry modelToTree(long start, long end, List<TmfTreeDataModel> model) {
        TmfTreeViewerEntry root = new TmfTreeViewerEntry(StringUtils.EMPTY);

        Map<Long, TmfTreeViewerEntry> map = new HashMap<>();
        map.put(-1L, root);
        for (TmfTreeDataModel entry : model) {
            TmfGenericTreeEntry<TmfTreeDataModel> viewerEntry = new TmfGenericTreeEntry<>(entry);
            map.put(entry.getId(), viewerEntry);

            TmfTreeViewerEntry parent = map.get(entry.getParentId());
            if (parent != null && !parent.getChildren().contains(viewerEntry)) {
                parent.addChild(viewerEntry);
            }
        }
        return root;
    }

    /**
     * Create a sortable column
     *
     * @param text
     *            column label
     * @param comparator
     *            comparator to sort {@link TmfGenericTreeEntry}s
     * @return the comparator
     */
    protected static <T extends TmfGenericTreeEntry<? extends TmfTreeDataModel>>
    @NonNull TmfTreeColumnData createColumn(String text, Comparator<T> comparator) {
        TmfTreeColumnData column = new TmfTreeColumnData(text);
        column.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                return comparator.compare((T) e1, (T) e2);
            }
        });
        return column;
    }

    /**
     * Get the full path of the entry, from the trace to itself, to query the
     * presentation provider.
     *
     * @param entry
     *            entry whose legend needs to be resolved.
     * @return the relevant series name.
     */
    protected static @NonNull String getFullPath(TmfGenericTreeEntry<TmfTreeDataModel> entry) {
        String path = entry.getName();
        ITmfTreeViewerEntry parent = entry.getParent();
        while (parent instanceof TmfGenericTreeEntry) {
            path = parent.getName() + '/' + path;
            parent = parent.getParent();
        }
        return path;
    }

}
