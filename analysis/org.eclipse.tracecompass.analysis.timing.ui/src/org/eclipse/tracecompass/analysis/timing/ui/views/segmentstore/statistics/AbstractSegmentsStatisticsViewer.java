/*******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics;

import java.text.Format;
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
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreStatisticsModel;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.AbstractSegmentStatisticsAnalysis;
import org.eclipse.tracecompass.analysis.timing.core.statistics.IStatistics;
import org.eclipse.tracecompass.analysis.timing.core.statistics.Statistics;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.internal.analysis.timing.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.statistics.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.FilterTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
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

    private static final Format FORMATTER = new SubSecondTimeWithUnitFormat();

    private @Nullable TmfAbstractAnalysisModule fModule;
    private MenuManager fTablePopupMenuManager;
    private @Nullable String fProviderId;

    /**
     * Constructor
     *
     * @param parent
     *            the parent composite
     * @deprecated use the constructor with data provider Id instead, to use the
     *             data provider extension point
     */
    @Deprecated
    public AbstractSegmentsStatisticsViewer(Composite parent) {
        this(parent, null);
    }

    /**
     * Constructor
     *
     * @param parent
     *            the parent composite
     * @param dataProviderId
     *            the data provider extension point ID.
     * @since 2.3
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
            if (columnIndex == 0 && element instanceof TmfTreeViewerEntry) {
                return String.valueOf(((TmfTreeViewerEntry) element).getName());
            } else if (element instanceof TmfGenericTreeEntry) {
                SegmentStoreStatisticsModel model = ((TmfGenericTreeEntry<@NonNull SegmentStoreStatisticsModel>) element).getModel();
                if (model.getNbElements() == 0) {
                    return ""; //$NON-NLS-1$
                }
                if (columnIndex == 1) {
                    return toFormattedString(model.getMin());
                } else if (columnIndex == 2) {
                    return String.valueOf(toFormattedString(model.getMax()));
                } else if (columnIndex == 3) {
                    return String.valueOf(toFormattedString(model.getMean()));
                } else if (columnIndex == 4) {
                    return String.valueOf(toFormattedString(model.getStdDev()));
                } else if (columnIndex == 5) {
                    return String.valueOf(model.getNbElements());
                } else if (columnIndex == 6) {
                    return String.valueOf(toFormattedString(model.getTotal()));
                }
            }

            // TODO remove this block when SegmentStoreStatisticsEntry is removed
            if (element instanceof SegmentStoreStatisticsEntry) {
                IStatistics<ISegment> statistics = ((SegmentStoreStatisticsEntry) element).getEntry();
                if (statistics.getNbElements() > 0) {
                    if (columnIndex == 1) {
                        return toFormattedString(statistics.getMin());
                    } else if (columnIndex == 2) {
                        return String.valueOf(toFormattedString(statistics.getMax()));
                    } else if (columnIndex == 3) {
                        return String.valueOf(toFormattedString(statistics.getMean()));
                    } else if (columnIndex == 4) {
                        return String.valueOf(toFormattedString(statistics.getStdDev()));
                    } else if (columnIndex == 5) {
                        return String.valueOf(statistics.getNbElements());
                    } else if (columnIndex == 6) {
                        return String.valueOf(toFormattedString(statistics.getTotal()));
                    }
                }
            }
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Creates the statistics analysis module
     *
     * @return the statistics analysis module
     * @deprecated Use a data provider factory and extension point instead
     */
    @Nullable
    @Deprecated
    protected TmfAbstractAnalysisModule createStatisticsAnalysiModule() {
        return null;
    }

    /**
     * Gets the statistics analysis module
     *
     * @return the statistics analysis module
     * @deprecated Use a data provider factory and extension point instead
     */
    @Nullable
    @Deprecated
    public TmfAbstractAnalysisModule getStatisticsAnalysisModule() {
        return fModule;
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> ImmutableList.of(
                createTmfTreeColumnData(Messages.SegmentStoreStatistics_LevelLabel, Comparator.comparing(TmfTreeViewerEntry::getName)),
                createTmfTreeColumnData(Messages.SegmentStoreStatistics_Statistics_MinLabel, Comparator.comparing(keyExtractor(IStatistics<ISegment>::getMin, SegmentStoreStatisticsModel::getMin))),
                createTmfTreeColumnData(Messages.SegmentStoreStatistics_MaxLabel, Comparator.comparing(keyExtractor(IStatistics<ISegment>::getMax, SegmentStoreStatisticsModel::getMax))),
                createTmfTreeColumnData(Messages.SegmentStoreStatistics_AverageLabel, Comparator.comparing(keyExtractor(IStatistics<ISegment>::getMean, SegmentStoreStatisticsModel::getMean))),
                createTmfTreeColumnData(Messages.SegmentStoreStatisticsViewer_StandardDeviation, Comparator.comparing(keyExtractor(IStatistics<ISegment>::getStdDev, SegmentStoreStatisticsModel::getStdDev))),
                createTmfTreeColumnData(Messages.SegmentStoreStatisticsViewer_Count, Comparator.comparing(keyExtractor(IStatistics<ISegment>::getNbElements, SegmentStoreStatisticsModel::getNbElements))),
                createTmfTreeColumnData(Messages.SegmentStoreStatisticsViewer_Total, Comparator.comparing(keyExtractor(IStatistics<ISegment>::getTotal, SegmentStoreStatisticsModel::getTotal))),
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

    private static <E extends Comparable<E>, M extends SegmentStoreStatisticsModel> Function<TmfTreeViewerEntry, E>
        keyExtractor(Function<IStatistics<@NonNull ISegment>, E> segExtractor, Function<M, E> modelExtractor) {
        return new Function<TmfTreeViewerEntry, E>() {

            @Override
            public E apply(TmfTreeViewerEntry entry) {
                if (entry instanceof SegmentStoreStatisticsEntry) {
                    return segExtractor.apply(((SegmentStoreStatisticsEntry) entry).getEntry());
                } else if (entry instanceof TmfGenericTreeEntry) {
                    return modelExtractor.apply(((TmfGenericTreeEntry<M>) entry).getModel());
                }
                throw new IllegalArgumentException();
            }
        };
    }

    @Override
    public void initializeDataSource(ITmfTrace trace) {
        TmfAbstractAnalysisModule module = createStatisticsAnalysiModule();
        if (module == null) {
            return;
        }
        try {
            module.setTrace(trace);
            module.schedule();
            if (fModule != null) {
                fModule.dispose();
            }
            fModule = module;
        } catch (TmfAnalysisException e) {
            Activator.getDefault().logError("Error initializing statistics analysis module", e); //$NON-NLS-1$
        }
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
        if ((element instanceof TmfGenericTreeEntry || element instanceof SegmentStoreStatisticsEntry) && !(element instanceof HiddenTreeViewerEntry) ) {
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
                        // TODO remove this if / block when SegmentStoreStatisticsEntry is removed
                        ISegment minObject = ((SegmentStoreStatisticsEntry) element).getEntry().getMinObject();
                        start = minObject == null ? 0 : minObject.getStart();
                        end = minObject == null ? 0 : minObject.getEnd();
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
                        // TODO remove this if / block when SegmentStoreStatisticsEntry is removed
                        ISegment maxObject = ((SegmentStoreStatisticsEntry) element).getEntry().getMaxObject();
                        start = maxObject == null ? 0 : maxObject.getStart();
                        end = maxObject == null ? 0 : maxObject.getEnd();
                    }
                    broadcast(new TmfSelectionRangeUpdatedSignal(AbstractSegmentsStatisticsViewer.this, TmfTimestamp.fromNanos(start), TmfTimestamp.fromNanos(end), getTrace()));
                    updateContent(start, end, true);
                }
            };

            manager.add(gotoStartTime);
            manager.add(gotoEndTime);
        }
    }

    /**
     * Formats a double value string
     *
     * @param value
     *            a value to format
     * @return formatted value
     */
    protected static String toFormattedString(double value) {
        /*
         * The cast to long is needed because the formatter cannot truncate the number.
         */
        return String.format("%s", FORMATTER.format(value)); //$NON-NLS-1$
    }

    /**
     * Class for defining an entry in the statistics tree.
     *
     * @deprecated use {@link SegmentStoreStatisticsDataProvider} and
     *             {@link TmfGenericTreeEntry} encapsulating
     *             {@link SegmentStoreStatisticsModel} instead, to keep
     *             {@link IStatistics} and {@link ISegment}s in the back-end
     */
    @Deprecated
    protected class SegmentStoreStatisticsEntry extends TmfTreeViewerEntry {

        private final IStatistics<ISegment> fEntry;

        /**
         * Constructor
         *
         * @param name
         *            name of entry
         *
         * @param entry
         *            segment store statistics object
         */
        public SegmentStoreStatisticsEntry(String name, IStatistics<ISegment> entry) {
            super(name);
            fEntry = entry;
        }

        /**
         * Gets the statistics object
         *
         * @return statistics object
         */
        public IStatistics<ISegment> getEntry() {
            return fEntry;
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
        TmfModelResponse<List<SegmentStoreStatisticsModel>> response = provider.fetchTree(filter, null);
        List<SegmentStoreStatisticsModel> model = response.getModel();
        if (model == null) {
            return null;
        }

        return modelToTree(trace.getName(), model);
    }

    /**
     * Algorithm to convert a model (List of {@link SegmentStoreStatisticsModel}) to
     * the tree.
     *
     * @param traceName
     *            trace / experiment name, we add it to help when debugging.
     * @param model
     *            model to convert
     * @return the resulting {@link TmfTreeViewerEntry}.
     */
    private static @Nullable TmfTreeViewerEntry modelToTree(String traceName, List<SegmentStoreStatisticsModel> model) {
        TmfTreeViewerEntry root = new TmfTreeViewerEntry(traceName);
        Map<Long, TmfTreeViewerEntry> map = new HashMap<>();
        map.put(-1L, root);

        for (TmfTreeDataModel entry : model) {
            TmfTreeViewerEntry viewerEntry;
            if (entry.getParentId() != -1) {
                viewerEntry = new TmfGenericTreeEntry<>(entry);
            } else {
                /*
                 * create a regular TmfTreeViewerEntry to avoid displaying statistics for trace
                 * level entries.
                 */
                viewerEntry = new TmfTreeViewerEntry(entry.getName());
            }
            map.put(entry.getId(), viewerEntry);

            TmfTreeViewerEntry parent = map.get(entry.getParentId());
            if (parent != null && !parent.getChildren().contains(viewerEntry)) {
                parent.addChild(viewerEntry);
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
     * Class to define a level in the tree that doesn't have any values.
     *
     * @deprecated use {@link SegmentStoreStatisticsDataProvider} and
     *             {@link TmfGenericTreeEntry} encapsulating
     *             {@link SegmentStoreStatisticsModel} instead, to keep
     *             {@link IStatistics} and {@link ISegment}s in the back-end
     */
    @Deprecated
    protected class HiddenTreeViewerEntry extends SegmentStoreStatisticsEntry {
        /**
         * Constructor
         *
         * @param name
         *            the name of the level
         */
        public HiddenTreeViewerEntry(String name) {
            super(name, new Statistics<>(ISegment::getLength));
        }
    }

    /**
     * Setter for the provider ID
     *
     * @param newProviderId
     *            the new provider ID to use
     * @since 2.3
     */
    public void setProviderId(String newProviderId) {
        fProviderId = newProviderId;
    }

}
