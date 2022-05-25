/*******************************************************************************
 * Copyright (c) 2015, 2020 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreStatisticsModel;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.AbstractSegmentStatisticsAnalysis;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.statistics.Messages;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.filters.FilterTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractTmfTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;

import com.google.common.collect.ImmutableList;

/**
 * An abstract tree viewer implementation for displaying segment store
 * statistics
 *
 * @author Bernd Hufmann
 * @author Geneviève Bastien
 * @since 1.3
 */
public abstract class AbstractSegmentsStatisticsViewer extends AbstractTmfTreeViewer {

    private static final int COL_LABEL = 0;
    private static final int COL_COUNT = 5;
    private static final int COL_TOTAL = 6;

    private static final String BLANK = "---"; //$NON-NLS-1$

    private @Nullable TmfAbstractAnalysisModule fModule;
    private MenuManager fTablePopupMenuManager;
    private @Nullable String fProviderId;

    /**
     * Constructor
     *
     * @param parent
     *            the parent composite
     * @param dataProviderId
     *            the data provider extension point ID.
     * @since 3.0
     */
    public AbstractSegmentsStatisticsViewer(Composite parent, @Nullable String dataProviderId) {
        super(parent, false);
        setLabelProvider(new SegmentStoreStatisticsLabelProvider());
        fTablePopupMenuManager = new MenuManager();
        fTablePopupMenuManager.setRemoveAllWhenShown(true);
        fTablePopupMenuManager.addMenuListener(manager -> {
            TreeViewer viewer = getTreeViewer();
            ISelection selection = viewer.getSelection();
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection sel = (IStructuredSelection) selection;
                if (manager != null) {
                    appendToTablePopupMenu(manager, sel);
                }
            }
        });
        Menu tablePopup = fTablePopupMenuManager.createContextMenu(getTreeViewer().getTree());
        Tree tree = getTreeViewer().getTree();
        tree.setMenu(tablePopup);
        tree.addDisposeListener(e -> {
            if (fModule != null) {
                fModule.dispose();
            }
        });

        fProviderId = dataProviderId;
    }

    /** Provides label for the Segment Store tree viewer cells */
    protected static class SegmentStoreStatisticsLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(@Nullable Object element, int columnIndex) {
            String label = ""; //$NON-NLS-1$
            if (element instanceof TmfGenericTreeEntry) {
                SegmentStoreStatisticsModel model = ((TmfGenericTreeEntry<@NonNull SegmentStoreStatisticsModel>) element).getModel();
                // Avoid displaying statistics for trace level entries.
                List<String> labels = model.getLabels();
                if ((columnIndex < labels.size()) && (columnIndex <= COL_TOTAL) && (columnIndex == COL_LABEL || (model.getParentId() != -1))) {
                    if (model.getNbElements() != 0 || columnIndex == COL_LABEL || columnIndex == COL_COUNT) {
                        label = labels.get(columnIndex);
                    } else {
                        label = BLANK;
                    }
                }
            }
            return label;
        }
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> ImmutableList.of(
                createTmfTreeColumnData(Messages.SegmentStoreStatistics_LevelLabel, Comparator.comparing(TmfTreeViewerEntry::getName)),
                createTmfTreeColumnData(Messages.SegmentStoreStatistics_Statistics_MinLabel, Comparator.comparing(keyExtractor(SegmentStoreStatisticsModel::getMin))),
                createTmfTreeColumnData(Messages.SegmentStoreStatistics_MaxLabel, Comparator.comparing(keyExtractor(SegmentStoreStatisticsModel::getMax))),
                createTmfTreeColumnData(Messages.SegmentStoreStatistics_AverageLabel, Comparator.comparing(keyExtractor(SegmentStoreStatisticsModel::getMean))),
                createTmfTreeColumnData(Messages.SegmentStoreStatisticsViewer_StandardDeviation, Comparator.comparing(keyExtractor(SegmentStoreStatisticsModel::getStdDev))),
                createTmfTreeColumnData(Messages.SegmentStoreStatisticsViewer_Count, Comparator.comparing(keyExtractor(SegmentStoreStatisticsModel::getNbElements))),
                createTmfTreeColumnData(Messages.SegmentStoreStatisticsViewer_Total, Comparator.comparing(keyExtractor(SegmentStoreStatisticsModel::getTotal))),
                // A dummy column is added to prevent the Total column from
                // taking all the remaining space
                new TmfTreeColumnData("")); //$NON-NLS-1$
    }

    private static TmfTreeColumnData createTmfTreeColumnData(@Nullable String name, Comparator<TmfTreeViewerEntry> comparator) {
        TmfTreeColumnData column = new TmfTreeColumnData(name);
        column.setAlignment(SWT.RIGHT);
        column.setComparator(new ViewerComparator() {
            @Override
            public int compare(@Nullable Viewer viewer, @Nullable Object e1, @Nullable Object e2) {
                if ((e1 == null) || (e2 == null)) {
                    return 0;
                }

                TmfTreeViewerEntry n1 = (TmfTreeViewerEntry) e1;
                TmfTreeViewerEntry n2 = (TmfTreeViewerEntry) e2;

                return comparator.compare(n1, n2);

            }
        });
        return column;
    }

    private static <E extends Comparable<E>, M extends SegmentStoreStatisticsModel> Function<TmfTreeViewerEntry, E> keyExtractor(Function<M, E> modelExtractor) {
        return entry -> {
            if (entry instanceof TmfGenericTreeEntry) {
                return modelExtractor.apply(((TmfGenericTreeEntry<M>) entry).getModel());
            }
            return (E) (Comparable<E>) o -> 0;
        };
    }

    /**
     * Method to add commands to the context sensitive menu.
     *
     * @param manager
     *            the menu manager
     * @param sel
     *            the current selection
     */
    protected void appendToTablePopupMenu(IMenuManager manager, IStructuredSelection sel) {
        Object element = sel.getFirstElement();
        if (element instanceof TmfGenericTreeEntry) {
            IAction gotoStartTime = new Action(Messages.SegmentStoreStatisticsViewer_GotoMinAction) {
                @Override
                public void run() {
                    long start;
                    long end;
                    if (element instanceof TmfGenericTreeEntry) {
                        SegmentStoreStatisticsModel model = ((TmfGenericTreeEntry<SegmentStoreStatisticsModel>) element).getModel();
                        start = model.getMinStart();
                        end = model.getMinEnd();
                    } else {
                        return;
                    }
                    broadcast(new TmfSelectionRangeUpdatedSignal(AbstractSegmentsStatisticsViewer.this, TmfTimestamp.fromNanos(start), TmfTimestamp.fromNanos(end), getTrace()));
                    updateContent(start, end, true);
                }
            };

            IAction gotoEndTime = new Action(Messages.SegmentStoreStatisticsViewer_GotoMaxAction) {
                @Override
                public void run() {
                    long start;
                    long end;
                    if (element instanceof TmfGenericTreeEntry) {
                        SegmentStoreStatisticsModel model = ((TmfGenericTreeEntry<SegmentStoreStatisticsModel>) element).getModel();
                        start = model.getMaxStart();
                        end = model.getMaxEnd();
                    } else {
                        return;
                    }
                    broadcast(new TmfSelectionRangeUpdatedSignal(AbstractSegmentsStatisticsViewer.this, TmfTimestamp.fromNanos(start), TmfTimestamp.fromNanos(end), getTrace()));
                    updateContent(start, end, true);
                }
            };

            manager.add(gotoStartTime);
            manager.add(gotoEndTime);
        }
    }

    @Override
    protected @Nullable ITmfTreeViewerEntry updateElements(ITmfTrace trace, long start, long end, boolean isSelection) {
        ITmfTreeDataProvider<SegmentStoreStatisticsModel> provider = null;

        // first try to get the data provider from the data provider manager.
        String providerId = fProviderId;
        if (providerId != null) {
            provider = DataProviderManager.getInstance().getDataProvider(trace,
                    fProviderId, ITmfTreeDataProvider.class);
        }

        // then try to get it from the legacy way
        TmfAbstractAnalysisModule analysisModule = fModule;
        if (provider == null && analysisModule instanceof AbstractSegmentStatisticsAnalysis && trace.equals(analysisModule.getTrace())) {
            AbstractSegmentStatisticsAnalysis module = (AbstractSegmentStatisticsAnalysis) analysisModule;
            provider = SegmentStoreStatisticsDataProvider.getOrCreate(trace, module);
        }

        if (provider == null) {
            return null;
        }

        FilterTimeQueryFilter filter = new FilterTimeQueryFilter(start, end, 2, isSelection);
        TmfModelResponse<TmfTreeModel<SegmentStoreStatisticsModel>> response = provider.fetchTree(FetchParametersUtils.filteredTimeQueryToMap(filter), null);
        TmfTreeModel<SegmentStoreStatisticsModel> model = response.getModel();
        if (model == null) {
            return null;
        }

        return modelToTree(trace, model.getEntries());
    }

    /**
     * Algorithm to convert a model (List of
     * {@link SegmentStoreStatisticsModel}) to the tree.
     *
     * @param trace
     *            trace / experiment.
     * @param model
     *            model to convert
     * @return the resulting {@link TmfTreeViewerEntry}.
     */
    private @Nullable TmfTreeViewerEntry modelToTree(ITmfTrace trace, List<SegmentStoreStatisticsModel> model) {
        TmfTreeViewerEntry root = getRoot(trace);
        if (root == null) {
            return null;
        }
        synchronized (root) {
            root.getChildren().clear();
            Map<Long, TmfTreeViewerEntry> map = new HashMap<>();
            map.put(-1L, root);
            for (TmfTreeDataModel entry : model) {
                TmfTreeViewerEntry viewerEntry = new TmfGenericTreeEntry<>(entry);
                map.put(entry.getId(), viewerEntry);

                TmfTreeViewerEntry parent = map.get(entry.getParentId());
                if (parent != null && !parent.getChildren().contains(viewerEntry)) {
                    parent.addChild(viewerEntry);
                }
            }
        }
        return root;
    }

    @Override
    @TmfSignalHandler
    public void windowRangeUpdated(@Nullable TmfWindowRangeUpdatedSignal signal) {
        // Do nothing. We do not want to update the view and lose the selection
        // if the window range is updated with current selection outside of this
        // new range.
    }

    @Override
    protected void setSelectionRange(long selectionBeginTime, long selectionEndTime) {
        super.setSelectionRange(selectionBeginTime, selectionEndTime);
        updateContent(selectionBeginTime, selectionEndTime, true);
    }

    /**
     * Get the type label
     *
     * @return the label
     * @since 1.2
     */
    protected String getTypeLabel() {
        return Objects.requireNonNull(Messages.AbstractSegmentStoreStatisticsViewer_types);
    }

    /**
     * Get the total column label
     *
     * @return the totals column label
     * @since 1.2
     */
    protected String getTotalLabel() {
        return Objects.requireNonNull(Messages.AbstractSegmentStoreStatisticsViewer_total);
    }

    /**
     * Get the selection column label
     *
     * @return The selection column label
     * @since 1.2
     */
    protected String getSelectionLabel() {
        return Objects.requireNonNull(Messages.AbstractSegmentStoreStatisticsViewer_selection);
    }

    /**
     * Setter for the provider ID
     *
     * @param newProviderId
     *            the new provider ID to use
     * @since 3.0
     */
    public void setProviderId(String newProviderId) {
        fProviderId = newProviderId;
    }

}
