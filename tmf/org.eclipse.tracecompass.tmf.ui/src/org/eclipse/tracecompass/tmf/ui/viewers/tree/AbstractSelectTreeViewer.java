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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.ILegendImageProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TriStateFilteredCheckboxTree;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;

/**
 * Abstract viewer for trees with checkboxes.
 *
 * @author Loic Prieur-Drevon
 */
public abstract class AbstractSelectTreeViewer extends AbstractTmfTreeViewer {

    /** ID of the checked tree items in the map of data in {@link TmfTraceContext} */
    private static final @NonNull String CHECKED_ELEMENTS = ".CHECKED_ELEMENTS"; //$NON-NLS-1$

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
    private int fLegendColumnIndex;

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
    public AbstractSelectTreeViewer(Composite parent, TriStateFilteredCheckboxTree checkboxTree, int legendColumnIndex) {
        super(parent, checkboxTree.getViewer());

        TreeViewer treeViewer = checkboxTree.getViewer();
        treeViewer.setComparator(COMPARATOR);
        if (treeViewer instanceof CheckboxTreeViewer) {
            ((CheckboxTreeViewer) treeViewer).addCheckStateListener(new CheckStateChangedListener());
        }
        fCheckboxTree = checkboxTree;
        fLegendColumnIndex = legendColumnIndex;
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

    /**
     * Method called when the trace is opened
     * <p>
     * renamed so that it does not override
     * {@link AbstractTmfTreeViewer#traceOpened(TmfTraceOpenedSignal)}
     * <p>
     * final - do not call
     *
     * @param signal
     *            unused
     */
    @TmfSignalHandler
    public final void traceOpenedIntern(@Nullable TmfTraceOpenedSignal signal) {
        saveViewContext();
    }

    /**
     * Method called when the trace is selected
     * <p>
     * renamed so that it does not override
     * {@link AbstractTmfTreeViewer#traceSelected(TmfTraceSelectedSignal)}
     * <p>
     * final - do not call
     *
     * @param signal
     *            unused
     */
    @TmfSignalHandler
    public final void traceSelectedIntern(@Nullable TmfTraceSelectedSignal signal) {
        if (signal != null && getTrace() != signal.getTrace()) {
            saveViewContext();
        }
    }

    @Override
    protected void updateContent(long start, long end, boolean isSelection) {
        /*
         * save the view context before updating the content (windows and selection
         * range update) to save the checked entries' IDs and check them again in the
         * new tree.
         */
        saveViewContext();
        super.updateContent(start, end, isSelection);
    }

    /**
     * Save the checked entries in the view context before changing trace.
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
        int imageHeight = getTreeViewer().getTree().getItemHeight() - 1;
        ILegendImageProvider legendImageProvider = fLegendImageProvider;
        if (legendImageProvider != null && fLegendColumnIndex >= 0) {
            TreeColumn legend = getTreeViewer().getTree().getColumn(fLegendColumnIndex);
            return legendImageProvider.getLegendImage(imageHeight, legend.getWidth(), name);
        }
        return null;
    }

}
