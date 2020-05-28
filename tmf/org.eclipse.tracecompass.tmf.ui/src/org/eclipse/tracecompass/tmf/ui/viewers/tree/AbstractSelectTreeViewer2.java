/*******************************************************************************
 * Copyright (c) 2017, 2020 Ericsson, Draeger Auriga
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IElementResolver;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfDataModelSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.ILegendImageProvider2;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.MultiTreePatternFilter;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TriStateFilteredCheckboxTree;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;

/**
 * Abstract viewer for trees with checkboxes. This viewer gets his data from a
 * data provider.
 *
 * @since 6.0
 * @author Loic Prieur-Drevon
 */
public abstract class AbstractSelectTreeViewer2 extends AbstractTmfTreeViewer {

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(AbstractSelectTreeViewer2.class);

    private static final AtomicInteger INSTANCE_ID_SEQUENCE = new AtomicInteger(0);

    /** Timeout between updates in the updateData thread **/
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    /** ID of the checked tree items in the map of data in {@link TmfTraceContext} */
    private static final char SEP = ':';
    private static final @NonNull String CHECKED_ELEMENTS = ".CHECKED_ELEMENTS"; //$NON-NLS-1$
    private static final @NonNull String FILTER_STRING = ".FILTER_STRING"; //$NON-NLS-1$
    private static final @NonNull String UPDATE_CONTENT_JOB_NAME = "AbstractSelectTreeViewer#updateContent Job"; //$NON-NLS-1$
    private static final String FAILED_TO_SLEEP_PREFIX = "Failed to sleep the "; //$NON-NLS-1$
    private static final String LOG_CATEGORY_SUFFIX = " Tree viewer"; //$NON-NLS-1$

    private final String fId;
    private final int fInstanceId;
    private final @NonNull String fLogCategory;

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
            saveViewContext();
            if (!fTreeListeners.isEmpty()) {
                Collection<ITmfTreeViewerEntry> entries = getCheckedViewerEntries();
                for (ICheckboxTreeViewerListener listener : fTreeListeners) {
                    listener.handleCheckStateChangedEvent(entries);
                }
                refresh();
            }

        }
    }

    /**
     * Listener to select a range in other viewers when a cell of the segment
     * store table view is selected
     */
    private class TreeViewerSelectionListener implements ISelectionChangedListener {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection selection = event.getStructuredSelection();
            Object entry = selection.getFirstElement();
            if (entry instanceof TmfGenericTreeEntry) {
                ITmfTreeDataModel model = ((TmfGenericTreeEntry<?>) entry).getModel();
                if (model instanceof IElementResolver) {
                    Multimap<@NonNull String, @NonNull Object> metadata = ((IElementResolver) model).getMetadata();
                    if (!metadata.isEmpty()) {
                        TmfSignalManager.dispatchSignal(new TmfDataModelSelectedSignal(AbstractSelectTreeViewer2.this, metadata));
                    }
                }
            }
        }
    }

    /**
     * Base class to provide the labels for the tree viewer. Views extending
     * this class typically need to override the getColumnText method if they
     * have more than one column to display. It also allows to change the font
     * and colors of the cells.
     * @since 6.0
     */
    protected class DataProviderTreeLabelProvider extends TreeLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == fLegendIndex && element instanceof TmfGenericTreeEntry && isChecked(element)) {
                TmfGenericTreeEntry<TmfTreeDataModel> genericEntry = (TmfGenericTreeEntry<TmfTreeDataModel>) element;
                TmfTreeDataModel model = genericEntry.getModel();
                if (model.hasRowModel()) {
                    return getLegendImage(genericEntry.getModel().getId());
                }
            }
            return null;
        }

    }

    private ILegendImageProvider2 fLegendImageProvider;
    private Set<ICheckboxTreeViewerListener> fTreeListeners = new HashSet<>();
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
    private AbstractSelectTreeViewer2(Composite parent, TriStateFilteredCheckboxTree checkboxTree,
            int legendIndex, String id) {
        super(parent, checkboxTree.getViewer());

        TreeViewer treeViewer = checkboxTree.getViewer();
        treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        treeViewer.setComparator(COMPARATOR);
        if (treeViewer instanceof CheckboxTreeViewer) {
            ((CheckboxTreeViewer) treeViewer).addCheckStateListener(new CheckStateChangedListener());
        }
        treeViewer.addSelectionChangedListener(new TreeViewerSelectionListener());
        checkboxTree.getFilterControl().addModifyListener(e -> saveViewContext());
        fCheckboxTree = checkboxTree;
        fLegendIndex = legendIndex;
        fId = id;
        fLogCategory = fId + LOG_CATEGORY_SUFFIX;
        fInstanceId = INSTANCE_ID_SEQUENCE.incrementAndGet();
        setLabelProvider(new DataProviderTreeLabelProvider());
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
    public AbstractSelectTreeViewer2(Composite parent, int legendIndex, String id) {
        // Create the tree viewer with a filtered checkbox
        this(parent, new TriStateFilteredCheckboxTree(parent,
                SWT.MULTI | SWT.H_SCROLL | SWT.FULL_SELECTION,
                new MultiTreePatternFilter(), true, false), legendIndex, id);
    }

    /**
     * Tell the chart viewer to listen to changes in the tree viewer
     *
     * @param listener
     *            Chart listening to changes in the tree's selected entries
     * @deprecated As of 6.0, there can be more than one listener for the tree,
     *             use {@link #addTreeListener(ICheckboxTreeViewerListener)}
     */
    @Deprecated
    public void setTreeListener(ICheckboxTreeViewerListener listener) {
        addTreeListener(listener);
    }

    /**
     * Add a listener to changes in the tree viewer
     *
     * @param listener
     *            Listener for changes in the tree's selected entries
     * @since 6.0
     */
    public void addTreeListener(ICheckboxTreeViewerListener listener) {
        fTreeListeners.add(listener);
    }

    /**
     * Remove a listener from this tree viewer
     *
     * @param listener
     *            The listener to remove
     * @since 6.0
     */
    public void removeTreeListener(ICheckboxTreeViewerListener listener) {
        fTreeListeners.remove(listener);
    }

    /**
     * Set the legend image provider (provider tree cells with an image).
     *
     * @param legendImageProvider
     *            Provides an image legend associated with a name
     */
    public void setLegendImageProvider(ILegendImageProvider2 legendImageProvider) {
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
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return;
        }
        TmfTraceContext ctx = TmfTraceManager.getInstance().getTraceContext(trace);
        Set<Long> ids = (Set<Long>) ctx.getData(getDataContextId(CHECKED_ELEMENTS));
        if (ids != null && rootEntry != null) {
            List<ITmfTreeViewerEntry> checkedElements = new ArrayList<>();
            checkEntries(ids, rootEntry, checkedElements);
            internalSetCheckedElements(checkedElements.toArray());
        }
        Object filterString = ctx.getData(getDataContextId(FILTER_STRING));
        if (filterString instanceof String) {
            fCheckboxTree.setFilterText((String) filterString);
        } else {
            fCheckboxTree.setFilterText(""); //$NON-NLS-1$
        }
        getTreeViewer().refresh();
    }

    /**
     * Checks all the passed elements and unchecks all the other.
     *
     * @param checkedElements
     *            the elements to check
     */
    protected void setCheckedElements(Object[] checkedElements) {
        internalSetCheckedElements(checkedElements);
        getTreeViewer().refresh();
    }

    private void internalSetCheckedElements(Object[] checkedElements) {
        fCheckboxTree.setCheckedElements(checkedElements);
        saveViewContext();
        for (ICheckboxTreeViewerListener listener : fTreeListeners) {
            listener.handleCheckStateChangedEvent(getCheckedViewerEntries());
        }
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
        restorePatternFilter(signal.getTrace());
        fCheckboxTree.setCheckedElements(new Object[0]);
        super.traceOpened(signal);
    }

    @Override
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        if (getTrace() != signal.getTrace()) {
            saveViewContext();
            restorePatternFilter(signal.getTrace());
            fCheckboxTree.setCheckedElements(new Object[0]);
        }
        super.traceSelected(signal);
    }

    @Override
    public void reset() {
        fCheckboxTree.setCheckedElements(new Object[0]);
        fCheckboxTree.setFilterText(""); //$NON-NLS-1$
        super.reset();
    }

    @Override
    protected void updateContent(long start, long end, boolean isSelection) {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, UPDATE_CONTENT_JOB_NAME)
                .setCategory(fLogCategory).build()) {
            ITmfTrace trace = getTrace();
            if (trace == null) {
                return;
            }
            Job thread = new Job(UPDATE_CONTENT_JOB_NAME) {
                @Override
                public IStatus run(IProgressMonitor monitor) {
                    try (FlowScopeLog runScope = new FlowScopeLogBuilder(LOGGER, Level.FINE, UPDATE_CONTENT_JOB_NAME + " run") //$NON-NLS-1$
                            .setParentScope(scope).build()) {

                        ITmfTreeDataProvider<@NonNull ITmfTreeDataModel> provider = getProvider(trace);
                        if (provider == null) {
                            return Status.OK_STATUS;
                        }

                        Map<String, Object> parameters = getParameters(start, end, isSelection);
                        if (parameters.isEmpty()) {
                            return Status.OK_STATUS;
                        }

                        boolean isComplete = false;
                        do {
                            TmfModelResponse<@NonNull TmfTreeModel<@NonNull ITmfTreeDataModel>> response;
                            try (FlowScopeLog iterScope = new FlowScopeLogBuilder(LOGGER, Level.FINE, UPDATE_CONTENT_JOB_NAME + " query") //$NON-NLS-1$
                                    .setParentScope(scope).build()) {

                                response = provider.fetchTree(parameters, monitor);
                                TmfTreeModel<@NonNull ITmfTreeDataModel> model = response.getModel();
                                if (model != null) {
                                    updateTree(trace, start, end, model.getEntries());
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
                                    Thread.currentThread().interrupt();
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

    private void updateTree(ITmfTrace trace, long start, long end, List<@NonNull ITmfTreeDataModel> model) {
        try (FlowScopeLog parentScope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "AbstractSelectTreeViewer:TreeUpdateRequested" ) //$NON-NLS-1$
                .setCategory(fLogCategory).build()) {
            final ITmfTreeViewerEntry newRootEntry = modelToTree(start, end, model);
            /* Set the input in main thread only if it didn't change */
            if (newRootEntry != null) {
                Display.getDefault().asyncExec(() -> {
                    try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "AbstractSelectTreeViewer:TreeUpdate").setParentScope(parentScope).build()) { //$NON-NLS-1$

                        if (!trace.equals(getTrace())) {
                            return;
                        }
                        TreeViewer treeViewer = getTreeViewer();
                        if (treeViewer.getControl().isDisposed()) {
                            return;
                        }

                        Object currentRootEntry = treeViewer.getInput();
                        if (!(currentRootEntry instanceof ITmfTreeViewerEntry) || !treeEquals(newRootEntry, (ITmfTreeViewerEntry) currentRootEntry)) {
                            updateTreeUI(treeViewer, newRootEntry);
                        } else {
                            treeViewer.refresh();
                        }
                        // FIXME should add a bit of padding
                        for (TreeColumn column : treeViewer.getTree().getColumns()) {
                            column.pack();
                        }
                    }
                });
            }
        }
    }

    /**
     * Recursively compare two trees, as the tree viewer requires a fast method,
     * {@link Object#hashCode()} for example.
     *
     * @param entry1
     *            one of the root tree entries to compare
     * @param entry2
     *            the other tree entry to compare
     * @return if the trees are equal
     */
    private boolean treeEquals(ITmfTreeViewerEntry entry1, ITmfTreeViewerEntry entry2) {
        if (!Objects.equals(entry1.getName(), entry2.getName())) {
            return false;
        }
        List<@NonNull ? extends ITmfTreeViewerEntry> children1 = entry1.getChildren();
        List<@NonNull ? extends ITmfTreeViewerEntry> children2 = entry2.getChildren();
        if (children1.size() != children2.size()) {
            return false;
        }
        int size = children1.size();
        for (int i = 0; i < size; i++) {
            if (!treeEquals(children1.get(i), children2.get(i))) {
                return false;
            }
        }
        return true;
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
            Text filterControl = fCheckboxTree.getFilterControl();
            String filterString = filterControl != null ? filterControl.getText() : null;
            TmfTraceManager.getInstance().updateTraceContext(previousTrace,
                    builder -> builder.setData(getDataContextId(CHECKED_ELEMENTS), ids)
                    .setData(getDataContextId(FILTER_STRING), filterString));
        }
    }

    private @NonNull String getDataContextId(String baseKey) {
        return getClass().getName() + SEP + fInstanceId + baseKey;
    }

    private void restorePatternFilter(ITmfTrace trace) {
        if (trace == null) {
            fCheckboxTree.getPatternFilter().setPattern(null);
            return;
        }
        TmfTraceContext ctx = TmfTraceManager.getInstance().getTraceContext(trace);
        Object filterString = ctx.getData(getDataContextId(FILTER_STRING));
        if (filterString instanceof String) {
            fCheckboxTree.getPatternFilter().setPattern((String) filterString);
        } else {
            fCheckboxTree.getPatternFilter().setPattern(null);
        }
    }

    /**
     * Get the legend image for a entry's ID
     *
     * @param id
     *            the entry's unique ID
     * @return the correctly dimensioned image if there is a legend image provider
     * @since 6.0
     */
    protected Image getLegendImage(@NonNull Long id) {
        /* If the image height match the row height, row height will increment */
        ILegendImageProvider2 legendImageProvider = fLegendImageProvider;
        int legendColumnIndex = fLegendIndex;
        if (legendImageProvider != null && legendColumnIndex >= 0) {
            Tree tree = getTreeViewer().getTree();
            int imageWidth = tree.getColumn(legendColumnIndex).getWidth();
            int imageHeight = tree.getItemHeight() - 1;
            if (imageHeight > 0 && imageWidth > 0) {
                return legendImageProvider.getLegendImage(imageHeight, imageWidth, id);
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
     * @param trace the trace
     * @return the relevant provider, if any
     * @since 4.0
     */
    protected ITmfTreeDataProvider<@NonNull ITmfTreeDataModel> getProvider(@NonNull ITmfTrace trace) {
        return DataProviderManager.getInstance().getDataProvider(trace, fId, ITmfTreeDataProvider.class);
    }

    @Override
    protected void initializeDataSource(@NonNull ITmfTrace trace) {
        getProvider(trace);
    }

    /**
     * Get the map to query the {@link ITmfTreeDataProvider} for the queried
     * parameters
     *
     * @param start
     *            query start time
     * @param end
     *            query end time
     * @param isSelection
     *            if the query is a selection
     * @return the resulting query parameters
     * @since 5.0
     */
    protected @NonNull Map<String, Object> getParameters(long start, long end, boolean isSelection) {
        return FetchParametersUtils.timeQueryToMap(new TimeQueryFilter(Long.min(start, end), Long.max(start, end), 2));
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
    protected ITmfTreeViewerEntry modelToTree(long start, long end, List<ITmfTreeDataModel> model) {
        TmfTreeViewerEntry root = new TmfTreeViewerEntry(StringUtils.EMPTY);

        Map<Long, TmfTreeViewerEntry> map = new HashMap<>();
        map.put(-1L, root);
        for (ITmfTreeDataModel entry : model) {
            TmfGenericTreeEntry<ITmfTreeDataModel> viewerEntry = new TmfGenericTreeEntry<>(entry);
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
     * @deprecated The ID should be used to query the presentation provider
     *             instead of the full path
     */
    @Deprecated
    protected static @NonNull String getFullPath(TmfGenericTreeEntry<TmfTreeDataModel> entry) {
        StringBuilder path = new StringBuilder(entry.getName());
        ITmfTreeViewerEntry parent = entry.getParent();
        while (parent instanceof TmfGenericTreeEntry) {
            path.insert(0, parent.getName() + '/');
            parent = parent.getParent();
        }
        return path.toString();
    }

    /**
     * Get the checkbox tree in this viewer.
     *
     * @return the checkbox tree.
     * @since 4.0
     */
    public TriStateFilteredCheckboxTree getTriStateFilteredCheckboxTree() {
        return fCheckboxTree;
    }

}
